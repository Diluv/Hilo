package com.diluv.hilo.process;

import com.diluv.hilo.db.models.tables.records.ProjectFileRecord;
import com.diluv.hilo.db.models.tables.records.ProjectRecord;
import org.apache.commons.io.FileUtils;
import org.jooq.DSLContext;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

import static com.diluv.hilo.db.models.Tables.*;

public class ProcessQueue {
    private final DSLContext transaction;
    private List<IProcess> processList;

    public ProcessQueue(DSLContext transaction) {
        this.transaction = transaction;
        this.processList = new LinkedList<>();
    }

    public void add(IProcess process) {
        this.processList.add(process);
    }

    public void process(Connection conn, ProjectFileRecord dbFileRecord) {
        String fileName = dbFileRecord.getFileName();
        File processingDir = new File(System.getenv("PROCESSING_DIR"), String.valueOf(dbFileRecord.getId()));

        File processingFile = new File(processingDir, fileName);
        long projectFileId = dbFileRecord.getId();

        if (!processingFile.exists()) {
            transaction.insertInto(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS)
                    .values(projectFileId, "File Input", false, "File not found")
                    .execute();

            transaction.update(PROJECT_FILE)
                    .set(PROJECT_FILE.REVIEW_NEEDED, true)
                    .where(PROJECT_FILE.ID.eq(projectFileId))
                    .execute();
            return;
        }

        boolean successful = false;
        long startTime = System.nanoTime();
        for (IProcess process : this.processList) {
            String processName = process.getProcessName();
            StringBuilder logger = new StringBuilder();

            if (!transaction.fetchExists(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID.eq(projectFileId).and(PROJECT_FILE_PROCESSING.STATUS.eq(processName)))) {
                successful = process.processFile(processingFile, dbFileRecord, conn, logger);

                transaction.insertInto(PROJECT_FILE_PROCESSING,
                        PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS)
                        .values(projectFileId, processName, successful, logger.toString())
                        .execute();

                if (!successful) {
                    transaction.update(PROJECT_FILE)
                            .set(PROJECT_FILE.REVIEW_NEEDED, true)
                            .set(PROJECT_FILE.PROCESSING, false)
                            .where(PROJECT_FILE.ID.eq(projectFileId))
                            .execute();
                    return;
                }
            }
        }

        if (!successful)
            return;

        StringBuilder logger = new StringBuilder();

        try {
            File releaseDir = new File(System.getenv("RELEASE_DIR"), String.valueOf(dbFileRecord.getId()));
            releaseDir.mkdirs();
            File releaseFile = new File(releaseDir, fileName);

            FileUtils.copyFile(processingFile, releaseFile);
            if (FileUtils.contentEquals(processingFile, releaseFile)) {
                FileUtils.forceDelete(processingDir);

                logger.append("File is released in ").append(System.nanoTime() - startTime);

                transaction.insertInto(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS)
                        .values(projectFileId, "File Release", true, logger.toString())
                        .execute();


                ProjectRecord dbProject = transaction.selectFrom(PROJECT)
                        .where(PROJECT.ID.eq(dbFileRecord.getProjectId()))
                        .fetchAny();

                if (dbProject.getNewProject()) {
                    transaction.update(PROJECT_FILE)
                            .set(PROJECT_FILE.REVIEW_NEEDED, true)
                            .set(PROJECT_FILE.PROCESSING, false)
                            .where(PROJECT_FILE.ID.eq(dbFileRecord.getId()))
                            .execute();
                } else {
                    //TODO Change to test CDN url before releasing
                    transaction.update(PROJECT_FILE)
                            .set(PROJECT_FILE.PUBLIC, true)
                            .where(PROJECT_FILE.ID.eq(dbFileRecord.getId()))
                            .execute();
                }
            } else {
                FileUtils.forceDelete(releaseFile);

                transaction.insertInto(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS)
                        .values(projectFileId, "File Copying Failed", false, "Process failed, file content didn't match")
                        .execute();

                transaction.update(PROJECT_FILE)
                        .set(PROJECT_FILE.REVIEW_NEEDED, true)
                        .set(PROJECT_FILE.PROCESSING, false)
                        .where(PROJECT_FILE.ID.eq(projectFileId))
                        .execute();
            }
        } catch (IOException e) {
            e.printStackTrace();

            transaction.insertInto(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS)
                    .values(projectFileId, "File Copying Exception", false, e.getLocalizedMessage())
                    .execute();

            transaction.update(PROJECT_FILE)
                    .set(PROJECT_FILE.REVIEW_NEEDED, true)
                    .set(PROJECT_FILE.PROCESSING, false)
                    .where(PROJECT_FILE.ID.eq(projectFileId))
                    .execute();
        }
    }
}
