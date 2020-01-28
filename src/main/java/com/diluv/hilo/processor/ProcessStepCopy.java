package com.diluv.hilo.processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

import com.diluv.confluencia.database.record.ProjectFileQueueRecord;
import com.diluv.hilo.data.FileData;

/**
 * This processing step is used to copy the input file into the working
 * directory.
 */
public class ProcessStepCopy implements IProcessStep {

    public static final IProcessStep INSTANCE = new ProcessStepCopy();

    private static final String NAME = "Copy Original To Working Directory";

    /**
     * Can not construct your own. Use {@link #INSTANCE} instead.
     */
    private ProcessStepCopy () {

        super();
    }

    @Override
    public String getProcessName () {

        return NAME;
    }

    @Override
    public void process (Logger log, FileData data, ProjectFileQueueRecord queueData, Path workingDir, Path file, String extension) throws Exception {

        try {

            Files.copy(file, workingDir.resolve(file.getFileName()));
        }

        catch (final IOException e) {

            log.error("Failed to copy file with id {} to working directory.", data.id);
            log.catching(e);
        }
    }

    @Override
    public boolean validate (Logger log, FileData data, ProjectFileQueueRecord queueData, Path file, String extension) throws Exception {

        // File#exists is faster that Files#exists
        return file.toFile().exists();
    }
}