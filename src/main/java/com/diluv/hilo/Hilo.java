package com.diluv.hilo;

import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.diluv.confluencia.database.record.ProjectFileRecord;
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
     *        one file.
     * @param procedure The procedure to use when processing files.
     */
    public Hilo(int threadCount, ProcessingProcedure procedure) {
        
        this.processingExecutor = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        this.procedure = procedure;
    }
    
    public void start () {
        
        this.poll();
    }
    
    private void poll () {
        
        try {
            
            final List<ProjectFileRecord> projectFiles = Main.DATABASE.fileDAO.getLatestFiles(this.getOpenProcessingThreads());
            Main.LOGGER.info("Enqued {} new files.");
            projectFiles.forEach(file -> this.processingExecutor.submit(new TaskProcessFile(file, this.procedure)));
        }
        
        catch (final SQLTransactionRollbackException e) {
            // TODO Couldn't get a lock (Aka something is causing a lock aka
            // another thread), try again
        }
        
        catch (final SQLException e) {
            
            e.printStackTrace();
        }
        
        try {
            
            Thread.sleep(1000 * 30L);
            this.poll();
        }
        
        catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private int getOpenProcessingThreads () {
        
        return this.processingExecutor.getMaximumPoolSize() - this.processingExecutor.getActiveCount();
    }
}