package com.diluv.hilo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.diluv.hilo.procedure.ProcessingProcedure;
import com.diluv.hilo.processor.ProcessStepGZip;
import com.diluv.hilo.utils.Constants;

public class Main {

    public static final Logger LOGGER = LogManager.getLogger("Hilo");
    public static final Database DATABASE = new Database();
    public static final Hilo HILO = new Hilo(8, new ProcessingProcedure(ProcessStepGZip.INSTANCE));
    
    public static void main (String[] args) {

        DATABASE.init(Constants.DB_HOSTNAME, Constants.DB_USERNAME, Constants.DB_PASSWORD, false);
        HILO.start();
    }
}