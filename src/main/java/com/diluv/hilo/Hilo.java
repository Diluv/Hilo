package com.diluv.hilo;

import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.diluv.confluencia.database.record.ProjectFileRecord;
import com.diluv.hilo.procedure.ProcessingProcedure;

public class Hilo {

    private final ThreadPoolExecutor processingExecutor;

    private final ProcessingProcedure procedure;
    
    public Hilo (int threadCount, ProcessingProcedure procedure) {
        
        this.processingExecutor = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        this.procedure = procedure;
    }
    
    public void start() {
        
        this.poll();
    }
    
    private void poll() {
              
        try {
            
            final List<ProjectFileRecord> projectFiles = Main.DATABASE.fileDAO.getLatestFiles(this.getOpenProcessingThreads());
            Main.LOGGER.info("Enqued {} new files.");
            projectFiles.forEach(file -> processingExecutor.submit(new TaskProcessFile(file, this.procedure)));
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
        
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    private int getOpenProcessingThreads() {
        
        return this.processingExecutor.getMaximumPoolSize() - this.processingExecutor.getActiveCount();
    }
}