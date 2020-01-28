package com.diluv.hilo.processor;

import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

import com.diluv.confluencia.database.record.ProjectFileQueueRecord;
import com.diluv.hilo.data.FileData;

public interface IProcessStep {

    /**
     * Performs the action designated to the processing step. This is performed
     * as part of a larger processing pipeline called a processing procedure.
     *
     * @param log An instance of the logger. This may be used to output debug
     *     and error information.
     * @param data A shared data object that is used to collect data for the
     *     database entry that corresponds to the file.
     * @param queueData Data that is associated with the file when it was
     *     uploaded to the processing queue.
     * @param workingDir A temporary directory used as shared storage for the
     *     duration of the processing procedure.
     * @param file The file being processed.
     * @param extension The extension of the file being processed. This is
     *     considered the text after the last period in the file name. The
     *     period is not included.
     * @throws Exception Any unhandled exception thrown during the processing
     *     step will be caught by the procedure. In this happens the
     *     procedure will end and report that the procedure failed.
     */
    void process (Logger log, FileData data, ProjectFileQueueRecord queueData, Path workingDir, Path file, String extension) throws Exception;

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
     *     and error information.
     * @param data A shared data object that is used to collect data for the
     *     database entry that corresponds to the file.
     * @param queueData Data that is associated with the file when it was
     *     uploaded to the processing queue.
     * @param file The file being processed.
     * @param extension The extension of the file being processed. This is
     *     considered the text after the last period in the file name. The
     *     period is not included.
     * @return Returns whether or not the processing step should be performed on
     *     a given file. Returning false will cause the procedure to skip
     *     the step and continue on to the next one.
     * @throws Exception Any unhandled exception thrown during the processing
     *     step will be caught by the procedure. In this happens the
     *     procedure will end and report that the procedure failed.
     */
    boolean validate (Logger log, FileData data, ProjectFileQueueRecord queueData, Path file, String extension) throws Exception;
}