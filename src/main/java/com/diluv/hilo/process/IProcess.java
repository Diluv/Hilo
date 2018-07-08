package com.diluv.hilo.process;

import com.diluv.hilo.db.models.tables.records.ProjectFileRecord;

import java.io.File;
import java.sql.Connection;

public interface IProcess {

    String getProcessName();

    boolean processFile(File preReleaseFile, ProjectFileRecord projectFile, Connection conn, StringBuilder logger);
}
