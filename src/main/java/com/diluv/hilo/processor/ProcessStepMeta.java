package com.diluv.hilo.processor;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;

import org.apache.logging.log4j.Logger;

import com.diluv.hilo.procedure.FileData;

/**
 * This processing step collects common and non specialized meta data about the
 * file such as it's name and it's size.
 */
public class ProcessStepMeta implements IProcessStep {

    public static final IProcessStep INSTANCE = new ProcessStepMeta();

    private static final String NAME = "Harvest Common Meta";

    /**
     * Can not construct your own. Use {@link #INSTANCE} instead.
     */
    private ProcessStepMeta () {

        super();
    }

    @Override
    public String getProcessName () {

        return NAME;
    }

    @Override
    public void process (Logger log, FileData data, Path workingDir, Path file) throws Exception {

        final File fileHandle = file.toFile();
        data.name = fileHandle.getName();
        data.size = fileHandle.length();
        data.createdAt = new Date();
        data.updatedAt = new Date();
    }

    @Override
    public boolean validate (Logger log, FileData data, Path file) throws Exception {

        return true;
    }
}