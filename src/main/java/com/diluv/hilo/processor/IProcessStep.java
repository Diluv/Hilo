package com.diluv.hilo.processor;

import java.nio.file.Path;

import com.diluv.confluencia.database.record.ProjectFilesEntity;

public interface IProcessStep {

    void process (ProjectFilesEntity fileRecord, Path toProcess, Path parentDir, String extension) throws Exception;

    boolean validate (ProjectFilesEntity fileRecord, Path toProcess, Path parentDir, String extension) throws Exception;
}