package com.diluv.hilo.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import com.diluv.hilo.data.FileData;
import com.diluv.hilo.data.QueueData;

/**
 * This processing step will write a file using GZip compression.
 */
public class ProcessStepGZip implements IProcessStep {

    public static final IProcessStep INSTANCE = new ProcessStepGZip();

    private static final String NAME = "Static GZip Compression";

    /**
     * Can not construct your own. Use {@link #INSTANCE} instead.
     */
    private ProcessStepGZip () {

        super();
    }

    @Override
    public String getProcessName () {

        return NAME;
    }

    private static class GZipCompressionStream extends GZIPOutputStream {

        public GZipCompressionStream (OutputStream out) throws IOException {

            super(out);
            this.def.setLevel(Deflater.BEST_COMPRESSION);
        }
    }

    @Override
    public void process (Logger log, FileData data, QueueData queueData, Path workingDir, Path file, String extension) throws Exception {

        final Path outputPath = workingDir.resolve(file.getFileName() + ".gz");

        try (GZIPOutputStream gzipOut = new GZipCompressionStream(Files.newOutputStream(outputPath)); InputStream inputStream = Files.newInputStream(file)) {

            IOUtils.copy(inputStream, gzipOut);
            gzipOut.finish();
        }

        catch (final IOException e) {

            log.error("Failed to write input to GZip output stream.");
            log.catching(e);
        }
    }

    @Override
    public boolean validate (Logger log, FileData data, QueueData queueData, Path file, String extension) throws Exception {

        return !"gz".equalsIgnoreCase(extension) && !"br".equalsIgnoreCase(extension);
    }
}