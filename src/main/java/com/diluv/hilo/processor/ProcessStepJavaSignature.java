package com.diluv.hilo.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.security.CodeSigner;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.Logger;

import com.diluv.confluencia.database.record.ProjectFileQueueRecord;
import com.diluv.hilo.data.FileData;

/**
 * This process step will check a jar file for a bundled code signature. If a
 * signature is found it will be verified.
 */
public class ProcessStepJavaSignature implements IProcessStep {

    private static final String NAME = "Java Signature Check";

    public static final IProcessStep INSTANCE = new ProcessStepJavaSignature();

    /**
     * Can not construct your own. Use {@link #INSTANCE} instead.
     */
    private ProcessStepJavaSignature () {

        super();
    }

    @Override
    public void process (Logger log, FileData data, ProjectFileQueueRecord queueData, Path workingDir, Path file, String extension) throws Exception {

        SignedStatus signedStatus = SignedStatus.UNREADABLE;
        final Set<CodeSigner> signers = new HashSet<>();

        try (JarFile jar = new JarFile(file.toFile())) {

            final Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {

                final JarEntry entry = entries.nextElement();

                try {

                    // At least one byte must be read from the stream in order
                    // for code signers to be loaded.
                    jar.getInputStream(entry).read();

                    if (entry.getCodeSigners() != null) {

                        signers.addAll(Arrays.asList(entry.getCodeSigners()));
                    }
                }

                catch (final SecurityException se) {

                    signedStatus = SignedStatus.INVALID;
                    break;
                }
            }

            if (signedStatus != SignedStatus.INVALID) {

                signedStatus = signers.isEmpty() ? SignedStatus.UNSIGNED : SignedStatus.SIGNED;
            }
        }

        catch (final IOException e) {

            signedStatus = SignedStatus.UNREADABLE;
        }
    }

    enum SignedStatus {

        /**
         * The jar file could be read but it had no associated signature.
         */
        UNSIGNED,

        /**
         * The jar file could be read and also has a valid signature.
         */
        SIGNED,

        /**
         * The jar file could be read but did not have a valid signature.
         */
        INVALID,

        /**
         * The jar file could not be read at all. It may not be a jar at all, or
         * possibly corrupted.
         */
        UNREADABLE;
    }

    @Override
    public String getProcessName () {

        return NAME;
    }

    @Override
    public boolean validate (Logger log, FileData data, ProjectFileQueueRecord queueData, Path file, String extension) throws Exception {

        return "jar".equalsIgnoreCase(extension);
    }
}