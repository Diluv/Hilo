package com.diluv.hilo;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.diluv.confluencia.database.record.FileProcessingStatus;
import com.diluv.confluencia.database.record.ProjectFilesEntity;
import com.diluv.hilo.procedure.ProcessingProcedure;

/**
 * Instances of Hilo are responsible for polling the database for newly created files. When new
 * files are found they are added to a processing pool that applies a processing procedure to
 * them.
 */
public class Hilo {

    /**
     * The thread pool for file processing.
     */
    private final ThreadPoolExecutor processingExecutor;

    /**
     * The procedure to use when processing files.
     */
    private final ProcessingProcedure procedure;

    /**
     * Constructs a new hilo instance.
     *
     * @param threadCount The amount of file processing threads to use. Each thread will handle
     *     one file.
     * @param procedure The procedure to use when processing files.
     */
    public Hilo (int threadCount, ProcessingProcedure procedure) {

        this.processingExecutor = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.procedure = procedure;
    }

    public void start () {

        if (!Main.DATABASE.fileDAO.updateStatusByStatus(FileProcessingStatus.PENDING, FileProcessingStatus.RUNNING)) {

            Main.LOGGER.error("Failed to reset status.");
        }
        this.poll();
    }

    private void poll () {

        final List<ProjectFilesEntity> projectFiles = Main.DATABASE.fileDAO.getLatestFiles(this.getOpenProcessingThreads());
        Main.LOGGER.info("Enqueued {} new files.", projectFiles.size());
        projectFiles.forEach(file -> this.processingExecutor.submit(new TaskProcessFile(file, this.procedure)));

        try {

            if (Main.RUNNING) {
                Thread.sleep(1000 * 30L);
                this.poll();
            }
        }

        catch (final InterruptedException e) {

            // Polling failed
        }
    }

    private int getOpenProcessingThreads () {

        return this.processingExecutor.getMaximumPoolSize() - this.processingExecutor.getActiveCount();
    }
}