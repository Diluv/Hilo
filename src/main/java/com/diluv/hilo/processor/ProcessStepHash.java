package com.diluv.hilo.processor;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.Logger;

import com.diluv.hilo.procedure.FileData;

/**
 * This processing step is responsible for calculating the SHA 512 hash of a
 * file.
 */
public class ProcessStepHash implements IProcessStep {

    /**
     * The name of the processing step as a constant value.
     */
    private static final String NAME = "Calculating SHA 512 Hash";

    /**
     * A global instance of this processing step.
     */
    public static final IProcessStep INSTANCE = new ProcessStepHash();

    /**
     * Can not construct your own. Use {@link #INSTANCE} instead.
     */
    private ProcessStepHash () {

        super();
    }

    @Override
    public void process (Logger log, FileData data, Path workingDir, Path file) throws Exception {

        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file))) {

            data.sha512 = DigestUtils.sha256Hex(bis);
        }
    }

    @Override
    public String getProcessName () {

        return NAME;
    }

    @Override
    public boolean validate (Logger log, FileData data, Path file) throws Exception {

        return true;
    }
}