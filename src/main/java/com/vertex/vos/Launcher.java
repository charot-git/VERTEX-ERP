package com.vertex.vos;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import java.io.IOException;
import java.net.ServerSocket;

public class Launcher {
    private static final int LOCK_PORT = 9999; // Choose an unused port
    private static ServerSocket lockSocket;

    public static void main(String[] args) {
        if (isAppAlreadyRunning()) {
            showAlreadyRunningMessage();
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> releaseLock()));
        Application.launch(Main.class);
    }

    private static boolean isAppAlreadyRunning() {
        try {
            lockSocket = new ServerSocket(LOCK_PORT);
            return false; // App is not running
        } catch (IOException e) {
            return true; // Port is already in use, meaning the app is running
        }
    }

    private static void releaseLock() {
        try {
            if (lockSocket != null) {
                lockSocket.close();
            }
        } catch (IOException ignored) {
        }
    }

    private static void showAlreadyRunningMessage() {
        new Thread(() -> Application.launch(AlreadyRunningAlert.class)).start();
    }

}
