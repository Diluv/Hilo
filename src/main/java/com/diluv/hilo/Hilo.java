package com.diluv.hilo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.diluv.hilo.processor.IProcessStep;

public class Hilo {
    
    /**
     * An ArrayList which holds all the processing steps for a given processing
     * procedure. The order of insertion is used as the execution order for the
     * various processing steps.
     */
    private final ArrayList<IProcessStep> processingSteps;
    
    /**
     * A logger instance that can be used by the processor and processing steps
     * to report debug information and errors.
     */
    private final Logger log;
    
    public Hilo (Logger log) {
        
        this.processingSteps = new ArrayList<>();
        this.log = log;
    }
    
    public Logger getLogger () {
        
        return this.log;
    }
    
    public void processFile (File input) {
        
        final UUID processId = UUID.randomUUID();
        final File workingDir = new File(processId.toString());
        
        try {
            
            Files.createDirectories(workingDir.toPath());
        }
        
        catch (IOException e) {
            
            this.getLogger().error("Failed to create the working directory {}.", workingDir.getPath());
            this.getLogger().catching(e);
            return;
        }
        
        Map<String, Object> properties = new HashMap<>();
        
        for (IProcessStep step : this.processingSteps) {
            
            if (step.validate(this, processId, input)) {
                
                step.process(this, processId, workingDir, input, properties);
            }
        }
    }
}