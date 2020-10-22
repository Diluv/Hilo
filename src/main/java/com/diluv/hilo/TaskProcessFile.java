package com.diluv.hilo;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import com.diluv.confluencia.Confluencia;
import com.diluv.confluencia.database.record.FileProcessingStatus;
import com.diluv.confluencia.database.record.ProjectFilesEntity;
import com.diluv.hilo.procedure.ProcessingProcedure;
import com.diluv.hilo.utils.FileUtil;

public class TaskProcessFile implements Runnable {

    private final ProcessingProcedure procedure;
    private final ProjectFilesEntity record;
    private final Path inputFile;
    private final Path workingDir;
    private int attempts = 0;

    public TaskProcessFile (ProjectFilesEntity record, ProcessingProcedure procedure) {

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
            Confluencia.getTransaction(session -> {
                if (!Confluencia.FILE.updateStatusById(session, FileProcessingStatus.FAILED_INVALID_FILE, this.record.getId())) {
                    //TODO error to discord or central location
                }
            });

            throw new IllegalStateException("The target file does not exist. Expected a file at " + this.inputFile);
        }
    }

    private void process () throws Exception {

        this.procedure.process(this.record, this.inputFile, this.workingDir);
    }

    private void finish () throws Exception {

        File file = FileUtil.getNodeCDNLocation(this.record);
        file.getParentFile().mkdirs();
        FileUtils.copyDirectory(this.workingDir.toFile(), file);

        Confluencia.getTransaction(session -> {
            if (!Confluencia.FILE.updateStatusById(session, FileProcessingStatus.SUCCESS, this.record.getId())) {
                //TODO error to discord or central location
            }
        });
    }

    private void cleanup () throws Exception {

        FileUtil.delete(this.workingDir);
    }

    private void retry () {

        this.attempts++;

        if (this.attempts > 3) {

            Confluencia.getTransaction(session -> {
                if (!Confluencia.FILE.updateStatusById(session, FileProcessingStatus.FAILED_INTERNAL_SERVER_ERROR, this.record.getId())) {
                    //TODO error to discord or central location
                }
            });
        }

        else {

            this.run();
        }
    }
}