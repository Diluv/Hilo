package com.diluv.hilo.processor;

import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

public interface IProcessStep {

    /**
     * Performs the action designated to the processing step. This is performed
     * as part of a larger processing pipeline called a processing procedure.
     *
     * @param log An instance of the logger. This may be used to output debug
     *        and error information.
     * @param fileId A unique id for the file being processed. This should be
     *        included in log outputs.
     * @param workingDir A temporary directory used as shared storage for the
     *        duration of the processing procedure.
     * @param file The file being processed.
     * @throws Exception Any unhandled exception thrown during the processing
     *         step will be caught by the procedure. In this happens the
     *         procedure will end and report that the procedure failed.
     */
    void process (Logger log, long fileId, Path workingDir, Path file) throws Exception;

    /**
     * Gets a name for the process step. This is used to identify the processing
     * step and may be used in logging and other information displays. This is
     * ideally a constant value.
     *
     * @return A name for the processing step.
     */
    String getProcessName ();

    /**
     * Validates whether or not the processing step can be performed on a given
     * file. If this check fails the procedure will simply continue to the next
     * step.
     *
     * @param log An instance of the logger. This may be used to output debug
     *        and error information.
     * @param fileId A unique id for the file being processed. This should be
     *        included in log outputs.
     * @param file The file being processed.
     * @return Returns whether or not the processing step should be performed on
     *         a given file. Returning false will cause the procedure to skip
     *         the step and continue on to the next one.
     * @throws Exception Any unhandled exception thrown during the processing
     *         step will be caught by the procedure. In this happens the
     *         procedure will end and report that the procedure failed.
     */
    boolean validate (Logger log, long fileId, Path file) throws Exception;
}