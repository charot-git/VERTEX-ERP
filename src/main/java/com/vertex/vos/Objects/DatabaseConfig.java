package com.vertex.vos.Objects;

import lombok.Getter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {

    public enum Environment {
        DEVELOPMENT,
        PRODUCTION,
        LOCAL,
        VPN,
        RC2
    }

    public static String DATABASE_URL;
    public static String DATABASE_USERNAME;
    public static String DATABASE_PASSWORD;
    public static String SERVER_DIRECTORY;

    @Getter
    private static Environment environment;

    static {
        loadConfigFromProperties();
    }

    private static void loadConfigFromProperties() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("database.properties")) {
            properties.load(input);

            String env = properties.getProperty("APP_ENV", "PRODUCTION").toUpperCase();
            environment = Environment.valueOf(env);

            DATABASE_URL = properties.getProperty("DB_URL");
            DATABASE_USERNAME = properties.getProperty("DB_USER");
            DATABASE_PASSWORD = properties.getProperty("DB_PASS");
            SERVER_DIRECTORY = properties.getProperty("SERVER_DIR");

            validateConfig();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration!", e);
        }
    }

    private static void validateConfig() {
        if (DATABASE_URL == null || DATABASE_USERNAME == null || DATABASE_PASSWORD == null) {
            throw new RuntimeException("Database credentials are missing from properties file!");
        }
    }
}
