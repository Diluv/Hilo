package com.diluv.hilo.processor;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.Logger;

import com.diluv.hilo.procedure.FileData;

public class ProcessStepHash implements IProcessStep {

    public static final IProcessStep INSTANCE = new ProcessStepHash();

    @Override
    public void process (Logger log, FileData data, Path workingDir, Path file) throws Exception {

        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        final MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

        final byte[] buffer = new byte[8192];
        int count;

        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file))) {

            while ((count = bis.read(buffer)) > 0) {

                md5.update(buffer, 0, count);
                sha256.update(buffer, 0, count);
                sha512.update(buffer, 0, count);
            }

            data.md5 = DatatypeConverter.printHexBinary(md5.digest()).toLowerCase();
            data.sha256 = DatatypeConverter.printHexBinary(sha256.digest()).toLowerCase();
            data.sha512 = DatatypeConverter.printHexBinary(sha512.digest()).toLowerCase();
        }
    }

    @Override
    public String getProcessName () {

        return "Collecting Hashes";
    }

    @Override
    public boolean validate (Logger log, FileData data, Path file) throws Exception {

        return true;
    }
}