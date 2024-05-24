package com.vertex.vos.Constructors;

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
                DATABASE_URL = "jdbc:mysql://192.168.1.49:3309/";
                DATABASE_USERNAME = "vosSystem";
                DATABASE_PASSWORD = "Meneses81617VOS";
                break;
        }
    }
}
