package com.diluv.hilo.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import com.diluv.hilo.Hilo;

public class ProcessStepGZip implements IProcessStep {

    private static final String NAME = "Static GZip Compression";

    @Override
    public void process (Hilo hilo, UUID processID, File workingDir, File file, Map<String, Object> properties) {

        final File outputFile = new File(workingDir, file.getName() + ".gz");

        try (GZIPOutputStream gzipOut = new GZipCompressionStream(new FileOutputStream(outputFile)); FileInputStream inputStream = new FileInputStream(file)) {

            IOUtils.copy(inputStream, gzipOut);

            gzipOut.finish();
        }

        catch (final IOException e) {

            hilo.getLogger().error("Failed to write input to GZip output stream.");
            hilo.getLogger().catching(e);
        }
    }

    @Override
    public String getProcessName () {

        return NAME;
    }

    @Override
    public boolean validate (Hilo hilo, UUID processID, File file) {

        // TODO double check that GZip can be applied to any file.
        return true;
    }

    private static class GZipCompressionStream extends GZIPOutputStream {

        public GZipCompressionStream (OutputStream out) throws IOException {

            super(out);
            this.def.setLevel(Deflater.BEST_COMPRESSION);
        }
    }
}