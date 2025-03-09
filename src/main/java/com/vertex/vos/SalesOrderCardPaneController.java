package com.vertex.vos;

import com.vertex.vos.Objects.SalesOrder;
import com.vertex.vos.Objects.SalesOrderDetails;
import com.vertex.vos.Utilities.ConfirmationAlert;
import com.vertex.vos.Utilities.DateTimeDialog;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.SalesOrderDAO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SalesOrderCardPaneController {

    public Button holdButton;
    @FXML
    private Button approveButton;

    @FXML
    private Label branchName;

    @FXML
    private Label discountAmount;

    @FXML
    private Label grossAmount;

    @FXML
    private Label netAmount;

    @FXML
    private Button openButton;

    @FXML
    private Label orderNoLabel;

    @FXML
    private Label receiptType;

    @FXML
    private Label salesmanName;

    @FXML
    private Label status;

    @FXML
    private Label storeName;

    @FXML
    private Label supplierName;

    @FXML
    private Label totalAmount;

    @FXML
    private Label vatAmount;

    @FXML
    private ButtonBar buttonBar;

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    public void setData(SalesOrder selectedItem) {
        orderNoLabel.setText(selectedItem.getOrderNo());
        supplierName.setText(selectedItem.getSupplier().getSupplierName());
        storeName.setText(selectedItem.getCustomer().getStoreName());
        salesmanName.setText(selectedItem.getSalesman().getSalesmanName());
        branchName.setText(selectedItem.getBranch().getBranchName());
        receiptType.setText(selectedItem.getInvoiceType().getName());
        grossAmount.setText(String.format("%.2f", selectedItem.getNetAmount() + selectedItem.getDiscountAmount()));
        discountAmount.setText(String.format("%.2f", selectedItem.getDiscountAmount()));
        netAmount.setText(String.format("%.2f", selectedItem.getNetAmount()));
        totalAmount.setText(String.format("%.2f", selectedItem.getTotalAmount()));
        status.setText(selectedItem.getOrderStatus().getDbValue());

        if (selectedItem.getOrderStatus().equals(SalesOrder.SalesOrderStatus.FOR_APPROVAL)) {
            approveButton.setText("Approve");
        } else {
            buttonBar.getButtons().removeAll(approveButton, holdButton);
        }

        if (selectedItem.getOrderStatus().equals(SalesOrder.SalesOrderStatus.ON_HOLD)) {
            buttonBar.getButtons().add(approveButton);
        }

        if (selectedItem.getOrderStatus().equals(SalesOrder.SalesOrderStatus.PENDING)) {
            Button convertButton = new Button("Convert");

            convertButton.setOnAction(actionEvent -> {
                convertButton.setDisable(true);
                animateButtonBorder(convertButton); // Start animation

                CompletableFuture.runAsync(() -> {
                    selectedItem.setSalesOrderDetails(salesOrderDAO.getSalesOrderDetails(selectedItem));
                    salesOrderListController.openSalesOrderForConversion(selectedItem);
                }).thenRun(() -> Platform.runLater(() -> {
                    convertButton.setDisable(false);
                    convertButton.setStyle(null); // Reset style after task completion
                }));
            });

            buttonBar.getButtons().add(convertButton);
        }

        approveButton.setOnAction(actionEvent -> {
            approveSalesOrder(selectedItem);
        });

        holdButton.setOnAction(actionEvent -> {
            holdSalesOrder(selectedItem);
        });

        openButton.setOnAction(actionEvent -> {
            openButton.setDisable(true);
            animateButtonBorder(openButton); // Start animation

            CompletableFuture.runAsync(() -> {
                selectedItem.setSalesOrderDetails(salesOrderDAO.getSalesOrderDetails(selectedItem));
                salesOrderListController.openSalesOrder(selectedItem);
            }).thenRun(() -> Platform.runLater(() -> {
                openButton.setDisable(false);
                openButton.setStyle(null); // Reset style after task completion
            }));
        });
    }

    private void holdSalesOrder(SalesOrder selectedItem) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Hold" + selectedItem.getOrderNo(), "Hold " + selectedItem.getOrderNo() + " with supplier " + selectedItem.getSupplier().getSupplierName(), "Please verify balance of " + selectedItem.getCustomer().getStoreName() + " before holding", true);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            boolean approved = salesOrderDAO.holdSalesOrder(selectedItem);
            if (approved) {
                DialogUtils.showCompletionDialog("Held", selectedItem.getOrderNo() + " has been held");
                salesOrderListController.loadSalesOrder();
                buttonBar.getButtons().remove(holdButton);
            } else {
                DialogUtils.showErrorMessage("Error", "Sales Order Holding Error, Please contact system developer.");
            }
        }
    }

    private void approveSalesOrder(SalesOrder selectedItem) {
        DateTimeDialog dateTimeDialog = new DateTimeDialog("Select Due Date", "Please select due date for " + selectedItem.getOrderNo(), "Due Date",
                selectedItem.getDueDate() != null ? selectedItem.getDueDate().toLocalDateTime().toLocalDate() : LocalDate.now());
        Optional<LocalDate> dueDate = dateTimeDialog.showAndWait();
        if (dueDate.isPresent()) {
            selectedItem.setDueDate(Timestamp.valueOf(dueDate.get().atStartOfDay()));
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Approve" + selectedItem.getOrderNo(), "Approve " + selectedItem.getOrderNo() + " with supplier " + selectedItem.getSupplier().getSupplierName(), "Please verify balance of " + selectedItem.getCustomer().getStoreName() + " before approval", true);
            boolean confirmed = confirmationAlert.showAndWait();
            if (confirmed) {
                boolean approved = salesOrderDAO.approveSalesOrder(selectedItem);
                if (approved) {
                    DialogUtils.showCompletionDialog("Approved", selectedItem.getOrderNo() + " has been approved");
                    salesOrderListController.loadSalesOrder();
                    buttonBar.getButtons().remove(approveButton);
                } else {
                    DialogUtils.showErrorMessage("Error", "Sales Order Approval Error, Please contact system developer.");
                }
            }
        }
    }

    @Setter
    SalesOrderListController salesOrderListController;

    private void animateButtonBorder(Button button) {
        final Timeline timeline = new Timeline();

        KeyFrame start = new KeyFrame(Duration.ZERO,
                new KeyValue(button.styleProperty(), "-fx-border-color: #F44336; -fx-border-width: 2px;"));

        KeyFrame mid = new KeyFrame(Duration.millis(300),
                new KeyValue(button.styleProperty(), "-fx-border-color: #FF9800; -fx-border-width: 2px;"));

        KeyFrame end = new KeyFrame(Duration.millis(600),
                new KeyValue(button.styleProperty(), "-fx-border-color: #F44336; -fx-border-width: 2px;"));

        timeline.getKeyFrames().addAll(start, mid, end);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // Stop animation when button is re-enabled
        button.disabledProperty().addListener((obs, wasDisabled, isNowEnabled) -> {
            if (!isNowEnabled) {
                timeline.stop();
                button.setStyle(null); // Reset style
            }
        });
    }


}
