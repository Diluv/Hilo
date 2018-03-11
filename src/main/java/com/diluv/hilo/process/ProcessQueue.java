package com.diluv.hilo.process;

import static com.diluv.hilo.models.Tables.PROJECT;
import static com.diluv.hilo.models.Tables.PROJECT_FILE;
import static com.diluv.hilo.models.Tables.PROJECT_FILE_PROCESSING;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jooq.DSLContext;

import com.diluv.hilo.Hilo;
import com.diluv.hilo.models.tables.records.ProjectFileRecord;
import com.diluv.hilo.models.tables.records.ProjectRecord;

public class ProcessQueue {
    private final DSLContext transaction;
    private final List<IProcess> processList;

    public ProcessQueue (DSLContext transaction) {

        this.transaction = transaction;
        this.processList = new LinkedList<>();
    }

    public void add (IProcess process) {

        this.processList.add(process);
    }

    public void process (Connection conn, ProjectFileRecord dbFileRecord) {

        final String fileName = dbFileRecord.getFileName();
        final File processingDir = new File(System.getenv("projectProcessingDir"), String.valueOf(dbFileRecord.getId()));

        final File processingFile = new File(processingDir, fileName);
        final long projectFileId = dbFileRecord.getId();

        if (!processingFile.exists()) {
            this.transaction.insertInto(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS).values(projectFileId, "File Input", false, "File not found").execute();

            this.transaction.update(PROJECT_FILE).set(PROJECT_FILE.REVIEW_NEEDED, true).where(PROJECT_FILE.ID.eq(projectFileId)).execute();
            return;
        }

        boolean successful = false;
        final long startTime = System.nanoTime();
        for (final IProcess process : this.processList) {
            final String processName = process.getProcessName();
            final StringBuilder logger = new StringBuilder();

            if (!this.transaction.fetchExists(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID.eq(projectFileId).and(PROJECT_FILE_PROCESSING.STATUS.eq(processName)))) {
                successful = process.processFile(processingFile, dbFileRecord, conn, logger);

                this.transaction.insertInto(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS).values(projectFileId, processName, successful, logger.toString()).execute();

                if (!successful) {
                    this.transaction.update(PROJECT_FILE).set(PROJECT_FILE.REVIEW_NEEDED, true).set(PROJECT_FILE.PROCESSING, false).where(PROJECT_FILE.ID.eq(projectFileId)).execute();
                    return;
                }
            }
        }

        if (!successful) {
            return;
        }

        final StringBuilder logger = new StringBuilder();

        try {
            final File releaseDir = new File(System.getenv("projectReleaseDir"), String.valueOf(dbFileRecord.getId()));
            releaseDir.mkdirs();
            final File releaseFile = new File(releaseDir, fileName);

            FileUtils.copyFile(processingFile, releaseFile);
            if (FileUtils.contentEquals(processingFile, releaseFile)) {
                FileUtils.forceDelete(processingDir);

                logger.append("File is released in ").append(System.nanoTime() - startTime);

                this.transaction.insertInto(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS).values(projectFileId, "File Release", true, logger.toString()).execute();

                final ProjectRecord dbProject = this.transaction.selectFrom(PROJECT).where(PROJECT.ID.eq(dbFileRecord.getProjectId())).fetchAny();

                if (dbProject.getNewProject()) {
                    this.transaction.update(PROJECT_FILE).set(PROJECT_FILE.REVIEW_NEEDED, true).where(PROJECT_FILE.ID.eq(dbFileRecord.getId())).execute();
                }
                else {
                    // TODO Change to test CDN url before releasing
                    this.transaction.update(PROJECT_FILE).set(PROJECT_FILE.PUBLIC, true).where(PROJECT_FILE.ID.eq(dbFileRecord.getId())).execute();
                }
            }
            else {
                FileUtils.forceDelete(releaseFile);

                this.transaction.insertInto(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS).values(projectFileId, "File Copying Failed", false, "Process failed, file content didn't match").execute();

                this.transaction.update(PROJECT_FILE).set(PROJECT_FILE.REVIEW_NEEDED, true).where(PROJECT_FILE.ID.eq(projectFileId)).execute();
            }
        }
        catch (final IOException e) {
            
            Hilo.LOG.trace("Could not process", e);

            this.transaction.insertInto(PROJECT_FILE_PROCESSING, PROJECT_FILE_PROCESSING.PROJECT_FILE_ID, PROJECT_FILE_PROCESSING.STATUS, PROJECT_FILE_PROCESSING.SUCCESSFUL, PROJECT_FILE_PROCESSING.LOGS).values(projectFileId, "File Copying Exception", false, e.getLocalizedMessage()).execute();

            this.transaction.update(PROJECT_FILE).set(PROJECT_FILE.REVIEW_NEEDED, true).where(PROJECT_FILE.ID.eq(projectFileId)).execute();
        }
    }
}
