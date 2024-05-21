package com.vertex.vos;

import com.vertex.vos.Constructors.HoverAnimation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PayablesNavigationController implements Initializable {
    AnchorPane contentPane;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }
    @FXML
    private VBox openAging;

    @FXML
    private VBox openCOD;

    @FXML
    private VBox openCWO;

    @FXML
    private VBox openGR;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new HoverAnimation(openAging);
        new HoverAnimation(openCOD);
        new HoverAnimation(openCWO);
        new HoverAnimation(openGR);
    }
}
