package com.diluv.hilo.processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

public class ProcessStepCopy implements IProcessStep {

    public static final IProcessStep INSTANCE = new ProcessStepCopy();

    private static final String NAME = "Copy Original To Working Directory";

    @Override
    public String getProcessName () {

        return NAME;
    }

    @Override
    public void process (Logger log, long fileId, Path workingDir, Path file) throws Exception {

        try {

            Files.copy(file, workingDir.resolve(file.getFileName()));
        }

        catch (final IOException e) {

            log.error("Failed to copy file with id {} to working directory.", fileId);
            log.catching(e);
        }
    }

    @Override
    public boolean validate (Logger log, long fileId, Path file) throws Exception {

        return Files.exists(file);
    }
}