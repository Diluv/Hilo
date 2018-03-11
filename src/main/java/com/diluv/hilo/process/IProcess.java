package com.diluv.hilo.process;

import java.io.File;
import java.sql.Connection;

import com.diluv.hilo.models.tables.records.ProjectFileRecord;

public interface IProcess {

    String getProcessName ();

    boolean processFile (File preReleaseFile, ProjectFileRecord projectFile, Connection conn, StringBuilder logger);
}
