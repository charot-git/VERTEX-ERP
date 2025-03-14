package com.vertex.vos.Utilities;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class LoadingButton {
    private final Timeline timeline;
    private final Button button;

    public LoadingButton(Button button) {
        this.button = button;
        this.timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(button.styleProperty(), "-fx-border-color: #F44336; -fx-border-width: 2px;")),
                new KeyFrame(Duration.millis(300), new KeyValue(button.styleProperty(), "-fx-border-color: #FF9800; -fx-border-width: 2px;")),
                new KeyFrame(Duration.millis(600), new KeyValue(button.styleProperty(), "-fx-border-color: #F44336; -fx-border-width: 2px;"))
        );
        this.timeline.setCycleCount(Animation.INDEFINITE);

        // Stop animation when button is re-enabled
        button.disabledProperty().addListener((obs, wasDisabled, isNowEnabled) -> {
            if (!isNowEnabled) {
                stop();
            }
        });
    }

    public void start() {
        button.setDisable(true);
        timeline.play();
    }

    public void stop() {
        button.setDisable(false);
        timeline.stop();
        button.setStyle(""); // Reset style
    }
}
