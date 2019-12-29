package com.diluv.hilo.processor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.diluv.hilo.procedure.ProcessingProcedure;

public class ProcessStepCopy implements IProcessStep {

    private static final String NAME = "Copy Original To Working Directory";

    @Override
    public void process (ProcessingProcedure hilo, UUID processID, File workingDir, File file, Map<String, Object> properties) {

        try {

            FileUtils.copyFileToDirectory(file, workingDir);
        }

        catch (final IOException e) {

            hilo.getLogger().error("Failed to copy file with id {} to working directory.", processID);
            hilo.getLogger().catching(e);
        }
    }

    @Override
    public String getProcessName () {

        return NAME;
    }

    @Override
    public boolean validate (ProcessingProcedure hilo, UUID processID, File file) {

        return file.exists();
    }
}