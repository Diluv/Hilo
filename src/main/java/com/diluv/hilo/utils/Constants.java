package com.diluv.hilo.utils;

public class Constants {

    public static final String DB_HOSTNAME = getValueOrDefault("DB_HOSTNAME", "jdbc:mariadb://localhost:3306/diluv");
    public static final String DB_USERNAME = getValueOrDefault("DB_USERNAME", "root");
    public static final String DB_PASSWORD = getValueOrDefault("DB_PASSWORD", "");

    private static String getValueOrDefault (String env, String defaultValue) {

        String value = System.getenv(env);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
