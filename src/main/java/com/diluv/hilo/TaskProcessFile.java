package com.diluv.hilo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.diluv.confluencia.database.record.ProjectFileRecord;
import com.diluv.hilo.procedure.ProcessingProcedure;
import com.diluv.hilo.utils.FileUtil;

public class TaskProcessFile implements Runnable {

    private final ProcessingProcedure procedure;
    private final ProjectFileRecord record;
    private final Path inputFile;
    private final Path workingDir;
    private int attempts = 0;

    public TaskProcessFile (ProjectFileRecord record, ProcessingProcedure procedure) {

        this.record = record;
        this.inputFile = FileUtil.getProcessingLocation(this.record).toPath();
        this.workingDir = this.inputFile.getParent();
        this.procedure = procedure;
    }

    @Override
    public void run () {

        try {

            this.setup();
            this.process();
            this.finish();
            this.cleanup();
        }

        catch (final Exception e) {

            Main.LOGGER.error("An issue occurred while processing file {} on attempt {}.", this.record.getId(), this.attempts + 1);
            Main.LOGGER.catching(e);

            this.retry();
        }
    }

    private void setup () throws Exception {

        if (!this.inputFile.toFile().exists()) {

            throw new IllegalStateException("The target file does not exist. Expected a file at " + this.inputFile);
        }
    }

    private void process () throws Exception {

        this.procedure.process(this.record, this.inputFile, this.workingDir);
    }

    private void finish () throws Exception {

        try {

            File file = FileUtil.getNodeCDNLocation(this.record);
            file.getParentFile().mkdirs();
            Files.copy(this.inputFile, file.toPath());
        }

        catch (final IOException e) {

            Main.LOGGER.error("Failed to copy file with id {} to NodeCDN directory.", this.record.getId());
            Main.LOGGER.catching(e);
        }
    }

    private void cleanup () throws Exception {

        FileUtil.delete(this.workingDir);
    }

    private void retry () {

        this.attempts++;

        if (this.attempts > 3) {

            // TODO failed
        }

        else {

            try {

                Thread.sleep(1000);
            }

            catch (final InterruptedException e) {

                e.printStackTrace();
            }

            this.run();
        }
    }
}