package com.vertex.vos.Objects;

import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class DatabaseConfig {

    public enum Environment {
        DEVELOPMENT,
        PRODUCTION,
        LOCAL,
        VPN,
        RC2
    }

    private static final String EXTERNAL_CONFIG_PATH = System.getProperty("user.home")
            + "/.jdeploy/apps/vos-erp-men2/database.properties";
    private static final String INTERNAL_CONFIG_PATH = "/database.properties";

    public static String DATABASE_URL;
    public static String DATABASE_USERNAME;
    public static String DATABASE_PASSWORD;
    public static String SERVER_DIRECTORY;

    @Getter
    private static Environment environment;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties properties = new Properties();

        // 1️⃣ Try loading external config from JDeploy app directory
        if (Files.exists(Paths.get(EXTERNAL_CONFIG_PATH))) {
            try (FileInputStream input = new FileInputStream(EXTERNAL_CONFIG_PATH)) {
                properties.load(input);
                System.out.println("✅ Loaded database configuration from: " + EXTERNAL_CONFIG_PATH);
            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to read external database config!", e);
            }
        } else {
            // 2️⃣ Fallback: Load default config from inside the JAR
            try (InputStream input = DatabaseConfig.class.getResourceAsStream(INTERNAL_CONFIG_PATH)) {
                if (input == null) {
                    throw new RuntimeException("❌ Missing default database.properties inside JAR!");
                }
                properties.load(input);
                System.out.println("⚠ Using default database config (inside JAR).");
            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to load default database configuration!", e);
            }
        }

        // Set environment and credentials
        String env = properties.getProperty("APP_ENV", "PRODUCTION").toUpperCase();
        environment = Environment.valueOf(env);

        DATABASE_URL = properties.getProperty("DB_URL");
        DATABASE_USERNAME = properties.getProperty("DB_USER");
        DATABASE_PASSWORD = properties.getProperty("DB_PASS");
        SERVER_DIRECTORY = properties.getProperty("SERVER_DIR");

        validateConfig();
    }

    private static void validateConfig() {
        if (DATABASE_URL == null || DATABASE_USERNAME == null || DATABASE_PASSWORD == null) {
            throw new RuntimeException("❌ Database credentials are missing in properties file!");
        }
    }
}
