package com.diluv.hilo.processor;

import java.nio.file.Path;

import com.diluv.clamchowder.ScanResult;
import com.diluv.confluencia.database.record.ProjectFileRecord;
import com.diluv.hilo.Main;

public class ProcessStepClamAV implements IProcessStep {
    
    @Override
    public void process (ProjectFileRecord fileRecord, Path toProcess, Path parentDir, String extension) throws Exception {
        
        final ScanResult result = Main.CLAM.scan(toProcess.toFile());
        
        if (result.getFound() != null) {
            
            Main.LOGGER.error("File {} failed the malware scan. Found: {}.", fileRecord.getId(), result.getFound());
            Main.DATABASE.fileDAO.insertProjectFileAntivirus(fileRecord.getId(), result.getFound());
        }
    }
    
    @Override
    public boolean validate (ProjectFileRecord fileRecord, Path toProcess, Path parentDir, String extension) throws Exception {
        
        return true;
    }
}