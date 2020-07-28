package com.diluv.hilo.processor;

import java.nio.file.Path;

import com.diluv.clamchowder.ScanResult;
import com.diluv.confluencia.database.record.ProjectFileAntivirusEntity;
import com.diluv.confluencia.database.record.ProjectFilesEntity;
import com.diluv.hilo.Main;

public class ProcessStepClamAV implements IProcessStep {

    @Override
    public void process (ProjectFilesEntity fileRecord, Path toProcess, Path parentDir, String extension) throws Exception {

        final ScanResult result = Main.CLAM.scan(toProcess.toFile());

        if (result.getFound() != null) {

            Main.LOGGER.error("File {} failed the malware scan. Found: {}.", fileRecord.getId(), result.getFound());
            ProjectFileAntivirusEntity projectFileAntivirus = new ProjectFileAntivirusEntity();
            projectFileAntivirus.setProjectFile(fileRecord);
            projectFileAntivirus.setMalware(result.getFound());
            if (!Main.DATABASE.fileDAO.insertProjectFileAntivirus(projectFileAntivirus)) {
                throw new RuntimeException("Inserting project file failed");
            }
        }
    }

    @Override
    public boolean validate (ProjectFilesEntity fileRecord, Path toProcess, Path parentDir, String extension) throws Exception {

        return true;
    }
}