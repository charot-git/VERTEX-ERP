package com.vertex.vos;

import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.LocationCache;
import javafx.application.Application;

import java.io.File;
import java.io.IOException;

public class Launcher {
    private static final String LOCK_FILE_PATH = System.getProperty("user.home") + File.separator + ".vos.lock";

    public static void main(String[] args) {
        LocationCache.initialize();
        Application.launch(Main.class);
    }

    private static boolean isAppAlreadyRunning() {
        File lockFile = new File(LOCK_FILE_PATH);
        try {
            if (lockFile.exists()) {
                return true;
            } else {
                lockFile.createNewFile();
                lockFile.deleteOnExit(); // Delete lock file when JVM exits
            }
        } catch (IOException e) {
            System.err.println("Error creating lock file: " + e.getMessage());
        }
        return false;
    }
}
