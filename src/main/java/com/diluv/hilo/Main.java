package com.diluv.hilo;

import java.io.IOException;
import java.security.Security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.diluv.clamchowder.ClamClient;
import com.diluv.hilo.procedure.ProcessingProcedure;
import com.diluv.hilo.processor.ProcessStepASC;
import com.diluv.hilo.processor.ProcessStepClamAV;
import com.diluv.hilo.processor.ProcessStepGZip;
import com.diluv.hilo.utils.Constants;

public class Main {

    public static final Logger LOGGER = LogManager.getLogger("Hilo");
    public static final Database DATABASE = new Database();
    public static ClamClient CLAM = new ClamClient(Constants.CLAM_HOSTNAME, Constants.CLAM_PORT, Constants.CLAM_TIMEOUT, Constants.CLAM_SIZE, Constants.CLAM_READ_BUFFER);
    public static final Hilo HILO = new Hilo(8, new ProcessingProcedure(ProcessStepGZip.INSTANCE, new ProcessStepClamAV(), new ProcessStepASC(Constants.SIGNING_KEY, Constants.PGP_PASS)));
    public static boolean RUNNING = true;

    public static void main (String[] args) throws IOException {

        Security.addProvider(new BouncyCastleProvider());

        DATABASE.init(Constants.DB_HOSTNAME, Constants.DB_USERNAME, Constants.DB_PASSWORD);

        if (CLAM.ping()) {

            LOGGER.info("Succesfully connected to ClamAV.");
        }

        else {

            LOGGER.error("Failed to connect with ClamAV.");
        }

        HILO.start();
    }
}