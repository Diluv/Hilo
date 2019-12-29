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

import com.diluv.hilo.procedure.FileData;

public class ProcessJavaSignature implements IProcessStep {

    private static final String NAME = "Java Signature Check";

    public static final IProcessStep INSTANCE = new ProcessJavaSignature();

    @Override
    public void process (Logger log, FileData data, Path workingDir, Path file) throws Exception {

        SignedStatus signedStatus = SignedStatus.UNREADABLE;
        final Set<CodeSigner> signers = new HashSet<>();

        try (JarFile jar = new JarFile(file.toFile())) {

            final Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {

                final JarEntry entry = entries.nextElement();

                try {

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

        UNSIGNED,
        SIGNED,
        INVALID,
        UNREADABLE;
    }

    @Override
    public String getProcessName () {

        return NAME;
    }

    @Override
    public boolean validate (Logger log, FileData data, Path file) throws Exception {

        // TODO validate file is jar
        return true;
    }
}