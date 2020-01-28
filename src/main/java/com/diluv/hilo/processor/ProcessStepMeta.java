package com.diluv.hilo.processor;

import java.io.File;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

import com.diluv.confluencia.database.record.ProjectFileQueueRecord;
import com.diluv.hilo.data.FileData;

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
    public void process (Logger log, FileData data, ProjectFileQueueRecord queueData, Path workingDir, Path file, String extension) throws Exception {

        final File fileHandle = file.toFile();
        data.size = fileHandle.length();
        data.updatedAt = System.currentTimeMillis();
    }

    @Override
    public boolean validate (Logger log, FileData data, ProjectFileQueueRecord queueData, Path file, String extension) throws Exception {

        return true;
    }
}