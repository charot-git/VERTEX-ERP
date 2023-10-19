package com.example.vos;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class LocationPickerController {
    @FXML
    private Button getAddressButton;
    @FXML
    private Label coordinates;
    @FXML
    private Label brgy;
    @FXML
    private Label city;
    @FXML
    private Label province;
    @FXML
    private WebView webView;

    public void initialize() {
    }
}
