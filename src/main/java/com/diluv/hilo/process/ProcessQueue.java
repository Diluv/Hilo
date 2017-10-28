package com.diluv.hilo.process;

import com.diluv.hilo.models.tables.records.ProjectFileRecord;
import org.apache.commons.io.FileUtils;
import org.jooq.DSLContext;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

import static com.diluv.hilo.models.Tables.PROJECT_FILE;
import static com.diluv.hilo.models.Tables.PROJECT_FILE_PROCESSING;

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

    public void process(Connection conn, ProjectFileRecord dbProject) {
        File preReleaseFile = new File(System.getenv("fileDir"), dbProject.getFileName());
        long projectFileId = dbProject.getId();

        if (!preReleaseFile.exists()) {
            fileIO(projectFileId, false, "File not found");

            transaction.update(PROJECT_FILE)
                    .set(PROJECT_FILE.REVIEW_NEEDED, true)
                    .where(PROJECT_FILE.ID.eq(projectFileId))
                    .execute();
            return;
        }

        long startTime = System.nanoTime();
        for (IProcess process : this.processList) {
            String processName = process.getProcessName();
            StringBuilder logger = new StringBuilder();

            if (!transaction.fetchExists(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID.eq(projectFileId).and(PROJECT_FILE_PROCESSING.STATUS.eq(processName)))) {
                boolean exception = process.processFile(preReleaseFile, dbProject, conn, logger);

                transaction.insertInto(PROJECT_FILE_PROCESSING,
                        PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS)
                        .values(projectFileId, processName, exception, logger.toString())
                        .execute();

                if (!exception) {
                    transaction.update(PROJECT_FILE)
                            .set(PROJECT_FILE.REVIEW_NEEDED, true)
                            .where(PROJECT_FILE.ID.eq(projectFileId))
                            .execute();
                    return;
                }
            }
        }

        StringBuilder logger = new StringBuilder();

        try {
            File releaseDir = new File(System.getenv("fileReleaseDir"), projectFileId + "/" + dbProject.getFileName());
            FileUtils.copyFile(preReleaseFile, releaseDir);
            if (FileUtils.contentEquals(preReleaseFile, releaseDir)) {
                FileUtils.forceDelete(preReleaseFile);

                logger.append("File is released in ").append(System.nanoTime() - startTime);

                fileIO(projectFileId, true, logger.toString());

                transaction.update(PROJECT_FILE)
                        .set(PROJECT_FILE.PROCESSED, true)
                        .where(PROJECT_FILE.ID.eq(dbProject.getId()))
                        .execute();
            } else {
                FileUtils.forceDelete(releaseDir);

                fileIO(projectFileId, false, "Process failed, file content didn't match");

                transaction.update(PROJECT_FILE)
                        .set(PROJECT_FILE.REVIEW_NEEDED, true)
                        .where(PROJECT_FILE.ID.eq(projectFileId))
                        .execute();
            }
        } catch (IOException e) {
            e.printStackTrace();

            fileIO(projectFileId, false, e.getLocalizedMessage());

            transaction.update(PROJECT_FILE)
                    .set(PROJECT_FILE.REVIEW_NEEDED, true)
                    .where(PROJECT_FILE.ID.eq(projectFileId))
                    .execute();
        }
    }

    /**
     * Handles logging all the File I/O errors to the database
     *
     * @param projectFileId The id of the project that needs to be logged
     * @param successful
     * @param error
     */
    public void fileIO(long projectFileId, boolean successful, String error) {
        transaction.insertInto(PROJECT_FILE_PROCESSING,
                PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS)
                .values(projectFileId, "File I/O", successful, error)
                .onDuplicateKeyUpdate()
                .set(PROJECT_FILE_PROCESSING.SUCCESSFUL, successful)
                .set(PROJECT_FILE_PROCESSING.LOGS, error)
                .execute();
    }
}
