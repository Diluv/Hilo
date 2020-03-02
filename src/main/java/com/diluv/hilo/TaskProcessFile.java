package com.diluv.hilo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import com.diluv.confluencia.database.record.ProjectFileRecord;
import com.diluv.hilo.procedure.ProcessingProcedure;
import com.diluv.hilo.utils.FileUtil;

public class TaskProcessFile implements Runnable {

    private final ProcessingProcedure procedure;
    private final ProjectFileRecord record;
    private final Path inputFile;
    private final Path workingDir;
    private int attempts = 0;
    
    public TaskProcessFile(ProjectFileRecord record, ProcessingProcedure procedure) {
        
        this.record = record;
        this.inputFile = FileUtil.getLocation(this.record).toPath();
        this.workingDir = inputFile.getParent();
        this.procedure = procedure;
    }
    
    @Override
    public void run () {

        try {
            
            this.setup();
            this.process();
            this.finish();
            this.cleanup();
        }
        
        catch (Exception e) {

            Main.LOGGER.error("An issue occurred while processing file {} on attempt {}.", record.getId(), this.attempts + 1);
            Main.LOGGER.catching(e);
            
            this.retry();
        }
    } 
    
    private void setup() throws Exception {
        
        if (!this.inputFile.toFile().exists()) {
            
            throw new IllegalStateException("The target file does not exist. Expected a file at " + this.inputFile);
        }
    }
    
    private void process() throws Exception {

        this.procedure.process(this.record, this.inputFile, this.workingDir);
    }
    
    private void finish() throws Exception {
        
    }
    
    private void cleanup() throws Exception {
        
        try (Stream<Path> stream = Files.walk(workingDir)) {

            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }

        catch (final IOException e) {

            // TODO do we want to retry?
        }
    }
    
    private void retry() {
        
        attempts++;
        
        if (attempts > 3) {
            
            // TODO failed
        }
        
        else {
            
            try {
                
                Thread.sleep(1000);
            }
            
            catch (InterruptedException e) {
                
                e.printStackTrace();
            }
                   
            this.run();
        }
    }
}