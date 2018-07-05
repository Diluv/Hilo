package com.diluv.hilo.process;

import java.io.File;
import java.sql.Connection;

import com.diluv.hilo.models.tables.records.ProjectFileRecord;

public interface IProcess {

    /**
     * Gets the name of the processor.
     *
     * @return The name of the processor.
     */
    String getProcessName ();

    /**
     * Used to process a file and return information about it.
     *
     * @param fileToProcess The file being requested to process.
     * @param fileRecord An object which represents the files database entry.
     * @param conn A connection to the database.
     * @param logger A string builder which contains the logs for the file while
     *        it is being processed.
     * @return Whether or not the processing was successful.
     */
    boolean processFile (File fileToProcess, ProjectFileRecord fileRecord, Connection conn, StringBuilder logger);
}
