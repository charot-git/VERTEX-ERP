package com.vertex.vos.Objects;

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
    private static final String REMEMBER_ME_FILE_PATH = System.getProperty("user.home") + "/remember.properties";

    private static Environment environment = Environment.PRODUCTION; // Default environment

    static {
        // Load environment from properties file if available
        loadEnvironmentFromProperties();
        setEnvironment(environment);
    }

    private static void loadEnvironmentFromProperties() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(REMEMBER_ME_FILE_PATH)) {
            properties.load(input);
            String env = properties.getProperty("environment", "PRODUCTION"); // Default to DEVELOPMENT if not found
            environment = Environment.valueOf(env.toUpperCase());
        } catch (IOException e) {
            System.err.println("Failed to load environment from properties file: " + e.getMessage());
            // If loading fails, default environment remains DEVELOPMENT
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid environment in properties file, defaulting to DEVELOPMENT.");
        }
    }

    public static void setEnvironment(Environment env) {
        environment = env;
        switch (environment) {
            case DEVELOPMENT:
                DATABASE_URL = "jdbc:mysql://100.79.208.40:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            case PRODUCTION:
                DATABASE_URL = "jdbc:mysql://192.168.1.226:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            case LOCAL:
                DATABASE_URL = "jdbc:mysql://localhost:3309/";
                DATABASE_USERNAME = "root";
                DATABASE_PASSWORD = "";
                break;
            case VPN:
                DATABASE_URL = "jdbc:mysql://VERTEX:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            case RC2:
                DATABASE_URL = "jdbc:mysql://100.124.175.56:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            default:
                throw new IllegalArgumentException("Unknown environment: " + environment);
        }
    }

    public static Environment getEnvironment() {
        return environment;
    }
}
