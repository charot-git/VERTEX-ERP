package com.vertex.vos;

import com.vertex.vos.Utilities.LocationCache;
import javafx.application.Application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

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
                // Read the PID from the lock file
                List<String> lines = Files.readAllLines(lockFilePath);
                if (!lines.isEmpty()) {
                    String pid = lines.get(0);
                    // Check if the process with this PID is running
                    if (isProcessRunning(pid)) {
                        return true;
                    }
                }
            }

            String pid = String.valueOf(ProcessHandle.current().pid());
            Files.write(lockFilePath, pid.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            lockFilePath.toFile().deleteOnExit();

        } catch (IOException e) {
            System.err.println("Error handling lock file: " + e.getMessage());
            return true; // Assuming an error in lock file handling means the app is already running
        }

        return false;
    }

    private static boolean isProcessRunning(String pid) {
        try {
            long pidLong = Long.parseLong(pid);
            return ProcessHandle.of(pidLong).isPresent();
        } catch (NumberFormatException e) {
            System.err.println("Invalid PID in lock file: " + pid);
            return false;
        }
    }
}
