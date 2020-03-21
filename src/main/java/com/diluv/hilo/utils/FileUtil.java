package com.diluv.hilo.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import com.diluv.confluencia.database.record.ProjectFileRecord;
import com.diluv.hilo.Main;

public class FileUtil {

    public static File getLocation (ProjectFileRecord fileRecord) {

        return new File(Constants.PROCESSING_FOLDER, String.format("%s/%s/%s/%s/%s", fileRecord.getGameSlug(), fileRecord.getProjectTypeSlug(), fileRecord.getProjectId(), fileRecord.getId(), fileRecord.getName()));
    }

    public static void delete (Path path) {

        try (Stream<Path> stream = Files.walk(path)) {

            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }

        catch (final IOException e) {

            Main.LOGGER.error("Failed to delete {}.", path);
            Main.LOGGER.catching(e);
        }
    }
}