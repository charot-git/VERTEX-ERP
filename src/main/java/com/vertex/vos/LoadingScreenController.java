package com.vertex.vos;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

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
}
