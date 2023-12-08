package com.vertex.vos.Utilities;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.stage.Window;
import javafx.util.Duration;

public class ErrorUtilities {

    public void shakeWindow(Window window) {
        double originalX = window.getX();
        double originalY = window.getY();
        double amplitude = 10;
        int cycleCount = 4;
        double duration = 50;

        Timeline timeline = new Timeline();
        for (int i = 0; i < cycleCount; i++) {
            final double shift = (i % 2 == 0) ? amplitude : -amplitude;
            KeyFrame keyFrameX = new KeyFrame(
                    Duration.millis(duration * (i + 1)),
                    event -> window.setX(originalX + shift)
            );
            KeyFrame keyFrameY = new KeyFrame(
                    Duration.millis(duration * (i + 1)),
                    event -> window.setY(originalY + shift)
            );
            timeline.getKeyFrames().addAll(keyFrameX, keyFrameY);
        }

        timeline.setOnFinished(event -> {
            window.setX(originalX);
            window.setY(originalY);
        });

        timeline.play();
    }
}
