package com.diluv.hilo.processor;

import java.nio.file.Path;

import com.diluv.confluencia.database.record.ProjectFileRecord;

public interface IProcessStep {
    
    void process (ProjectFileRecord fileRecord, Path toProcess, Path parentDir, String extension) throws Exception;
    
    boolean validate (ProjectFileRecord fileRecord, Path toProcess, Path parentDir, String extension) throws Exception;
}