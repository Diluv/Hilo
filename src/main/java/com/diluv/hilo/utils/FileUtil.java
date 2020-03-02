package com.diluv.hilo.utils;

import java.io.File;

import com.diluv.confluencia.database.record.ProjectFileRecord;

public class FileUtil {
    
    public static File getLocation (ProjectFileRecord fileRecord) {
        
        final String game = "minecraft"; // TODO
        final String type = "mod"; // TODO
        return new File(Constants.PROCESSING_FOLDER, game + "/" + type + "/" + fileRecord.getProjectId() + "/" + fileRecord.getId() + "/" + fileRecord.getName());
    }
}
