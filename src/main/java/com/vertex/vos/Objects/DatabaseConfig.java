package com.vertex.vos.Objects;

public class DatabaseConfig {
    public static final String DATABASE_URL;
    public static final String DATABASE_USERNAME;
    public static final String DATABASE_PASSWORD;

    // Toggle between configurations based on the environment
    static {
        String environment = "production"; // Set this dynamically based on your environment
        switch (environment) {
            case "development":
                DATABASE_URL = "jdbc:mysql://RSM:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            case "production":
            default:
                DATABASE_URL = "jdbc:mysql://VERTEX:3306/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
            case "local":
                DATABASE_URL = "jdbc:mysql://localhost:3309/";
                DATABASE_USERNAME = "root";
                DATABASE_PASSWORD = "";
                break;

        }
    }
}
