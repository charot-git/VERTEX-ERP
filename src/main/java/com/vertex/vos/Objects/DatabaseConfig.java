package com.vertex.vos.Objects;

public class DatabaseConfig {
    public static String DATABASE_URL;
    public static String DATABASE_USERNAME;
    public static String DATABASE_PASSWORD;

    private static String environment = "rc2"; // Default environment

    static {
        setEnvironment(environment);
    }

    public static void setEnvironment(String env) {
        environment = env;
        switch (environment) {
            case "development":
                DATABASE_URL = "jdbc:mysql://100.79.208.40:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            case "production":
                DATABASE_URL = "jdbc:mysql://192.168.1.226:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            case "local":
                DATABASE_URL = "jdbc:mysql://localhost:3309/";
                DATABASE_USERNAME = "root";
                DATABASE_PASSWORD = "";
                break;
            case "vpn":
                DATABASE_URL = "jdbc:mysql://VERTEX:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            case "rc2":
                DATABASE_URL = "jdbc:mysql://100.124.175.56:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            default:
                throw new IllegalArgumentException("Unknown environment: " + environment);
        }
    }

    public static String getEnvironment() {
        return environment;
    }
}
