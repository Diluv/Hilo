package com.diluv.hilo.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import com.diluv.confluencia.database.record.ProjectFilesEntity;
import com.diluv.hilo.Main;

public class FileUtil {

    public static File getProcessingLocation (ProjectFilesEntity file) {

        final String gameSlug = file.getProject().getGame().getSlug();
        final String projectTypeSlug = file.getProject().getProjectType().getSlug();
        final long projectId = file.getProject().getId();
        final long projectFileId = file.getId();
        final String name = file.getName();
        return new File(Constants.PROCESSING_FOLDER, String.format("%s/%s/%s/%s/%s", gameSlug, projectTypeSlug, projectId, projectFileId, name));
    }

    public static File getNodeCDNLocation (ProjectFilesEntity file) {

        final String gameSlug = file.getProject().getGame().getSlug();
        final String projectTypeSlug = file.getProject().getProjectType().getSlug();
        final long projectId = file.getProject().getId();
        final long projectFileId = file.getId();

        return new File(Constants.NODECDN_FOLDER, String.format("%s/%s/%s/%s", gameSlug, projectTypeSlug, projectId, projectFileId));
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