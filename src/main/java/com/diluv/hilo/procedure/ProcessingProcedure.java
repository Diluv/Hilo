package com.diluv.hilo.procedure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.diluv.hilo.processor.IProcessStep;

/**
 * Instances of this class represent a defined procedure for the processing and
 * handling for different types of files.
 */
public class ProcessingProcedure {

    /**
     * An ArrayList that holds all defined processing steps for the procedure.
     * Order of insertion is used as the execution order for the various
     * processing steps.
     */
    private final ArrayList<IProcessStep> processingSteps;

    /**
     * A logger that is used to report debug and error information generated as
     * the procedure is executed.
     */
    private final Logger log;

    public ProcessingProcedure (Logger log) {

        this.processingSteps = new ArrayList<>();
        this.log = log;
    }

    public Logger getLogger () {

        return this.log;
    }

    public void processFile (File input) {

        // TODO Replace this process ID with a UUID obtained from the database.
        final UUID processId = UUID.randomUUID();

        // Create a new temporary working directory for this file upload. This
        // directory is used for the various processing steps to output their
        // results to. Ultimately the contents of this directory will be moved
        // to the CDN or wherever their final destination is.
        final File workingDir = new File(processId.toString());

        try {

            Files.createDirectories(workingDir.toPath());
        }

        catch (final IOException e) {

            this.getLogger().error("Failed to create the working directory {}.", workingDir.getPath());
            this.getLogger().catching(e);
            return;
        }

        final Map<String, Object> properties = new HashMap<>();

        // Process the file using the given processing steps.
        for (final IProcessStep step : this.processingSteps) {

            if (step.validate(this, processId, input)) {

                step.process(this, processId, workingDir, input, properties);
            }
        }

        // Once all processing steps have been completed, attempt to forcefully
        // delete the directory. This is done to save file space and also
        // prevent potential conflicts with working directories.
        try {

            FileUtils.forceDelete(workingDir);
        }

        catch (final IOException e) {

            this.getLogger().error("Failed to delete working directory for {}.", workingDir.getPath());
            this.getLogger().catching(e);
        }
    }
}