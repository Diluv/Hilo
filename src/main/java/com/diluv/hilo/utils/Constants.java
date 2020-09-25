package com.diluv.hilo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

import com.diluv.hilo.Main;

public class Constants {

    public static final PGPSecretKey SIGNING_KEY = readSigningKey(new File("private.gpg"));
    public static final char[] PGP_PASS = getValueOrError("PGP_PASS").toCharArray();
    public static final String DB_HOSTNAME = getValueOrDefault("DB_HOSTNAME", "jdbc:mariadb://localhost:3306/diluv");
    public static final String DB_USERNAME = getValueOrDefault("DB_USERNAME", "root");
    public static final String DB_PASSWORD = getValueOrDefault("DB_PASSWORD", "");

    public static final String CLAM_HOSTNAME = getValueOrDefault("CLAM_HOSTNAME", "127.0.0.1");
    public static final int CLAM_PORT = getValueOrDefault("CLAM_PORT", 3310);
    public static final int CLAM_TIMEOUT = getValueOrDefault("CLAM_TIMEOUT", (int) TimeUnit.MINUTES.toMillis(1));
    public static final int CLAM_SIZE = getValueOrDefault("CLAM_SIZE", 1024 * 64);
    public static final int CLAM_READ_BUFFER = getValueOrDefault("CLAM_READ_BUFFER", 1024 * 64);

    public static final String PROCESSING_FOLDER = getValueOrDefault("PROCESSING_FOLDER", "processing");
    public static final String NODECDN_FOLDER = getValueOrDefault("NODECDN_FOLDER", "nodecdn");
    public static final String NODECDN_USERNAME = getValueOrDefault("NODECDN_USERNAME", null);
    public static final String NODECDN_PASSWORD = getValueOrDefault("NODECDN_PASSWORD", null);
    public static final String NODECDN_WEBHOOK_URL = getValueOrDefault("NODECDN_WEBHOOK_URL", "https://api.diluv.com");
    public static final String WEBHOOK_URL = getValueOrDefault("WEBHOOK_URL", null);
    public static final String ENVIRONMENT = getValueOrDefault("ENVIRONMENT", "PRODUCTION");

    private static String getValueOrDefault (String env, String defaultValue) {

        final String value = System.getenv(env);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    static PGPSecretKey readSigningKey (File in) {

        try (InputStream input = PGPUtil.getDecoderStream(new ByteArrayInputStream(getStreamAsBytes(new FileInputStream(in))))) {

            final PGPSecretKeyRingCollection keyRings = new PGPSecretKeyRingCollection(input, new BcKeyFingerprintCalculator());

            for (final PGPSecretKeyRing keyRing : keyRings) {

                for (final PGPSecretKey key : keyRing) {

                    if (key.isSigningKey()) {

                        return key;
                    }
                }
            }
        }

        catch (final FileNotFoundException e) {

            Main.LOGGER.error("Failed to read signing key from {}. File does not exist!", in.getName());
        }

        catch (final IOException e) {

            Main.LOGGER.error("Failed to read from file {}.", in.getName());
            Main.LOGGER.catching(e);
        }

        catch (final PGPException e) {

            Main.LOGGER.error("Failed to read from file {} as it is not in the expected PGP format.", in.getName());
            Main.LOGGER.catching(e);
        }

        return null;
    }

    static byte[] getStreamAsBytes (InputStream input) throws IOException {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            IOUtils.copy(input, out, 4096);
            return out.toByteArray();
        }
    }

    static int getValueOrDefault (String name, int defaultValue) {

        final String value = System.getenv(name);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    static String getValueOrError (String name) {

        final String value = System.getenv(name);

        if (value == null) {

            throw new IllegalStateException("The required environmental variable " + name + " was not found.");
        }

        return value;
    }

    public static String getNodeCDNWebhookUrl(String uuid){
        return Constants.NODECDN_WEBHOOK_URL + "/v1/internal/nodecdn/" + uuid;
    }

    public static boolean isDevelopment () {

        return "DEVELOPMENT".equalsIgnoreCase(Constants.ENVIRONMENT);
    }
}