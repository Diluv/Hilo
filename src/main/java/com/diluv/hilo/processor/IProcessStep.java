package com.diluv.hilo.processor;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import com.diluv.hilo.procedure.ProcessingProcedure;

public interface IProcessStep {

    /**
     * Performs a processing step on a file as part of a larger processing
     * pipeline.
     *
     * @param hilo The instance of Hilo which invoked the processing step.
     * @param processID An identifier for the current processing operation that
     *        is unique for each file processed.
     * @param workingDir The current working directory for the current
     *        processing operation. This is created before the file is processed
     *        and will be deleted after all process steps have been completed.
     * @param file The file being processed.
     * @param properties A map of properties read from the file. //TODO replace
     *        with a databse connection.
     */
    void process (ProcessingProcedure hilo, UUID processID, File workingDir, File file, Map<String, Object> properties);

    /**
     * Gets a name for the process. This is used as part of the logger to
     * describe the current processing step stage.
     *
     * @return The name of the process.
     */
    String getProcessName ();

    /**
     * Validates whether or not a process can be ran on a given file. If this
     * check fails processor will simply continue to the next step. If an
     * exception is thrown processing will stop and the file will fail it's
     * processing check.
     *
     * @param hilo The instance of hilo which invoked the processing step.
     * @param processID An identifier for the current processing operation that
     *        is unique for each file processed.
     * @param file The file being processed.
     * @return Whether or not the process step can run for the given file.
     *         Returning false will simply tell the processor to skip to the
     *         next step. If an exception is thrown processing will stop and the
     *         file will fail it's processing check.
     */
    boolean validate (ProcessingProcedure hilo, UUID processID, File file);
}