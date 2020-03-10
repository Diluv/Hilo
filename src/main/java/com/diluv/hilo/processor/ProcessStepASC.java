package com.diluv.hilo.processor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import com.diluv.confluencia.database.record.ProjectFileRecord;

/**
 * This processing step will generate a new detached signature file for the SHA512 of a given
 * input file.
 */
public class ProcessStepASC implements IProcessStep {
    
    /**
     * The secret key to use when generating signatures.
     */
    final PGPSecretKey pgpSecret;
    
    /**
     * An array containing the password used alongside the secret key.
     */
    final char[] pass;
    
    public ProcessStepASC(PGPSecretKey key, char[] pass) {
        
        this.pgpSecret = key;
        this.pass = pass;
    }
    
    @Override
    public void process (ProjectFileRecord fileRecord, Path toProcess, Path parentDir, String extension) throws Exception {
        
        final Path output = parentDir.resolve(toProcess.getFileName() + ".sha512.asc");

        try (InputStream fileIn = new BufferedInputStream(new FileInputStream(toProcess.toFile()))) {
            
            try (ArmoredOutputStream armorOut = new ArmoredOutputStream(new FileOutputStream(output.toFile())); BCPGOutputStream bcpgOut = new BCPGOutputStream(armorOut);) {
                
                armorOut.setHeader("FileName", fileRecord.getName());
                armorOut.setHeader("SHA-512", fileRecord.getSha512());
                armorOut.setHeader("Uploader", Long.toString(fileRecord.getUserId()));
                armorOut.setHeader("CreatedAt", Long.toString(fileRecord.getCreatedAt()));
                
                final PGPPrivateKey privateKey = this.pgpSecret.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(this.pass));
                final PGPSignatureGenerator sigGenerator = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(this.pgpSecret.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA512).setProvider("BC"));
                
                sigGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);
                
                byte nextByte;
                
                while ((nextByte = (byte) fileIn.read()) != -1) {
                    
                    sigGenerator.update(nextByte);
                }
                
                sigGenerator.generate().encode(bcpgOut);
            }
        }
    }
    
    @Override
    public boolean validate (ProjectFileRecord fileRecord, Path toProcess, Path parentDir, String extension) throws Exception {
        
        return !"asc".equalsIgnoreCase(extension);
    }
}