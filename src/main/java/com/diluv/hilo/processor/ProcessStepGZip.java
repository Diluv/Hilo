package com.diluv.hilo.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import com.diluv.confluencia.database.record.ProjectFileRecord;
import com.diluv.hilo.utils.FileUtil;

/**
 * This processing step will write a file using GZip compression.
 */
public class ProcessStepGZip implements IProcessStep {
    
    public static final IProcessStep INSTANCE = new ProcessStepGZip();
    
    /**
     * Can not construct your own. Use {@link #INSTANCE} instead.
     */
    private ProcessStepGZip() {
        
        super();
    }
    
    private static class GZipCompressionStream extends GZIPOutputStream {
        
        public GZipCompressionStream(OutputStream out) throws IOException {
            
            super(out);
            this.def.setLevel(Deflater.BEST_COMPRESSION);
        }
    }
    
    @Override
    public void process (ProjectFileRecord fileRecord, Path toProcess, Path parentDir, String extension) throws Exception {
        
        final Path gzipOutput = parentDir.resolve(toProcess.getFileName() + ".gz");
        
        if (gzipOutput.toFile().exists()) {
            
            FileUtil.delete(gzipOutput);
        }
        
        try (GZIPOutputStream gzipOut = new GZipCompressionStream(Files.newOutputStream(gzipOutput)); InputStream inputStream = Files.newInputStream(toProcess)) {
            
            IOUtils.copy(inputStream, gzipOut);
            gzipOut.finish();
        }
        
        catch (final IOException e) {
            
            // TODO reconsider retry behavior
        }
    }
    
    @Override
    public boolean validate (ProjectFileRecord fileRecord, Path toProcess, Path parentDir, String extension) throws Exception {
        
        return !"gz".equalsIgnoreCase(extension) && !"br".equalsIgnoreCase(extension);
    }
}