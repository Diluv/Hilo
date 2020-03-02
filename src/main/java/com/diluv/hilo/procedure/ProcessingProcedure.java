package com.diluv.hilo.procedure;

import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import com.diluv.confluencia.database.record.ProjectFileRecord;
import com.diluv.hilo.processor.IProcessStep;

/**
 * Instances of this class represent a defined procedure for the processing and handling for
 * different types of files.
 */
public class ProcessingProcedure {
    
    /**
     * An ArrayList that holds all defined processing steps for the procedure. Order of
     * insertion is used as the execution order for the various processing steps.
     */
    private final ArrayList<IProcessStep> processingSteps;
    
    /**
     * Creates a new procedure instance.
     *
     * @param log The logger to log debug and error information to.
     * @param steps The processing steps to initialize the procedure with.
     */
    public ProcessingProcedure(IProcessStep... steps) {
        
        this.processingSteps = new ArrayList<>();
        this.addStep(steps);
    }
    
    public ProcessingProcedure addStep (IProcessStep... steps) {
        
        for (final IProcessStep step : steps) {
            
            this.processingSteps.add(step);
        }
        
        return this;
    }
    
    public void process (ProjectFileRecord fileRecord, Path toProcess, Path parentDir) throws Exception {
        
        final String extension = FilenameUtils.getExtension(toProcess.toString());
        
        for (final IProcessStep step : this.processingSteps) {
            
            if (step.validate(fileRecord, toProcess, parentDir, extension)) {
                
                step.process(fileRecord, toProcess, parentDir, extension);
            }
        }
    }
}