package com.vertex.vos;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class LoadingScreenController {

    @FXML
    private ImageView icon;

    @FXML
    ProgressBar loadingProgress;

    @FXML
    private Label subText;

    public void setLoadingProgress(double progress) {
        loadingProgress.setProgress(progress);
    }

    public void setSubText(String text) {
        subText.setText(text);
    }

    public void pulsateImage() {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1), icon);
        scaleTransition.setByX(0.2); // Scale factor for X
        scaleTransition.setByY(0.2); // Scale factor for Y
        scaleTransition.setCycleCount(Animation.INDEFINITE);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
        scaleTransition.play();
    }
}
