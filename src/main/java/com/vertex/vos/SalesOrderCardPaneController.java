package com.vertex.vos;

import com.vertex.vos.Objects.SalesOrder;
import com.vertex.vos.Objects.SalesOrderDetails;
import com.vertex.vos.Utilities.ConfirmationAlert;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.SalesOrderDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import lombok.Setter;

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
                selectedItem.setSalesOrderDetails(salesOrderDAO.getSalesOrderDetails(selectedItem));
                salesOrderListController.openSalesOrderForConversion(selectedItem);
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
            selectedItem.setSalesOrderDetails(salesOrderDAO.getSalesOrderDetails(selectedItem));
            salesOrderListController.openSalesOrder(selectedItem);
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

    @Setter
    SalesOrderListController salesOrderListController;

}
