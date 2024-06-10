package com.vertex.vos;

import com.vertex.vos.Utilities.LocationCache;
import javafx.application.Application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Launcher {
    private static final String LOCK_FILE_PATH = System.getProperty("user.home") + File.separator + ".vos.lock";

    public static void main(String[] args) {
        if (isAppAlreadyRunning()) {
            System.out.println("Application is already running.");
            return;
        }

        LocationCache.initialize();
        Application.launch(Main.class);
    }

    private static boolean isAppAlreadyRunning() {
        Path lockFilePath = Paths.get(LOCK_FILE_PATH);
        try {
            if (Files.exists(lockFilePath)) {
                return true;
            } else {
                Files.createFile(lockFilePath);
                lockFilePath.toFile().deleteOnExit();
            }
        } catch (IOException e) {
            System.err.println("Error creating lock file: " + e.getMessage());
            return true; // Assuming an error in lock file creation means the app is already running
        }
        return false;
    }
}
