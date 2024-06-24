package com.vertex.vos;

import com.vertex.vos.Constructors.HoverAnimation;
import com.vertex.vos.Constructors.PaymentsToSupplier;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
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
        initializeAnimation();
        openCOD.setOnMouseClicked(mouseEvent -> openCashOnDeliveryForm());
    }

    private void openCashOnDeliveryForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("paymentToSupplier.fxml"));
            Parent root = loader.load();
            PaymentToSupplierController controller = loader.getController();

            controller.cashOnDelivery();

            Stage stage = new Stage();
            stage.setTitle("Cash On Delivery Payments");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));

            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initializeAnimation() {
        new HoverAnimation(openAging);
        new HoverAnimation(openCOD);
        new HoverAnimation(openCWO);
        new HoverAnimation(openGR);
    }
}
