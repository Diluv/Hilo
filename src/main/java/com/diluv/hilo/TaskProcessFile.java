package com.diluv.hilo;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;

import com.diluv.confluencia.database.record.FileProcessingStatus;
import com.diluv.confluencia.database.record.ProjectFileRecord;
import com.diluv.hilo.procedure.ProcessingProcedure;
import com.diluv.hilo.utils.FileUtil;

import org.apache.commons.io.FileUtils;

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

        File file = FileUtil.getNodeCDNLocation(this.record);
        file.getParentFile().mkdirs();
        FileUtils.copyDirectory(this.workingDir.toFile(), file);

        if (!Main.DATABASE.fileDAO.updateStatusById(FileProcessingStatus.SUCCESS, this.record.getId())) {
            //TODO
        }
    }

    private void cleanup () throws Exception {

        if (Main.CLEAN_UP) {
            FileUtil.delete(this.workingDir);
        }
    }

    private void retry () {

        this.attempts++;

        if (this.attempts > 3) {

            try {
                if (!Main.DATABASE.fileDAO.updateStatusById(FileProcessingStatus.FAILED_INTERNAL_SERVER_ERROR, this.record.getId())) {
                    //TODO
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
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