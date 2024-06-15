package com.vertex.vos;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.SalesInvoice;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.FXMLExporter;
import com.vertex.vos.Utilities.SalesInvoiceDAO;
import com.vertex.vos.Utilities.SalesOrderDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class SalesInvoiceController {
    @FXML
    public Button printButton;
    @FXML
    private VBox POBox;

    @FXML
    private VBox POContent;

    @FXML
    private ComboBox<String> branch;

    @FXML
    private VBox branchBox;

    @FXML
    private Label branchErr;

    @FXML
    private HBox confirmBox;

    @FXML
    private Button confirmButton;

    @FXML
    private ComboBox<String> customer;

    @FXML
    private VBox customerBox;

    @FXML
    private Label customerErr;

    @FXML
    private Label date;

    @FXML
    private DatePicker dateOrdered;

    @FXML
    private VBox dateOrderedBox;

    @FXML
    private Label dateOrderedErr;

    @FXML
    private DatePicker deliveryDate;

    @FXML
    private Label deliveryDateErr;

    @FXML
    private Label discounted;

    @FXML
    private Label dueDateErr;

    @FXML
    private Label grandTotal;

    @FXML
    private Label gross;

    @FXML
    private DatePicker paymentDueDate;

    @FXML
    private VBox paymentDueDateBox;

    @FXML
    private Label paymentTerms;

    @FXML
    private TableView<ProductsInTransact> productsInTransact;

    @FXML
    private Label purchaseOrderNo;

    @FXML
    private ComboBox<String> salesman;

    @FXML
    private VBox salesmanBox;

    @FXML
    private Label salesmanErr;

    @FXML
    private HBox statusBox;

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

    @FXML
    private HBox totalBox;

    @FXML
    private VBox totalBoxLabels;

    @FXML
    private VBox totalVBox;

    @FXML
    private Label vat;

    @FXML
    private Label withholding;

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();
    BranchDAO branchDAO = new BranchDAO();
    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    public void initData(SalesInvoice selectedInvoice) {
        initializeTableView();
        if (selectedInvoice == null) {
            purchaseOrderNo.setText("SALES INVOICE#");
            customer.setValue("");
            salesman.setValue("");
            date.setText("");
            dateOrdered.setValue(null);
            paymentDueDate.setValue(null);
            paymentTerms.setText("");
            statusLabel.setText("");
            gross.setText("");
            vat.setText("");
            discounted.setText("");
            grandTotal.setText("");
            return;
        }

        try {
            purchaseOrderNo.setText("SALES INVOICE#" + selectedInvoice.getOrderId());

            String branchName = salesOrderDAO.getSourceBranchForSO(selectedInvoice.getOrderId());
            branch.setValue(branchName);

            customer.setValue(selectedInvoice.getCustomerName());

            salesman.setValue(selectedInvoice.getSalesmanName());

            if (selectedInvoice.getInvoiceDate() != null) {
                LocalDate invoiceLocalDate = selectedInvoice.getInvoiceDate().toLocalDateTime().toLocalDate();
                date.setText(invoiceLocalDate.toString());
                dateOrdered.setValue(invoiceLocalDate);
            } else {
                date.setText("");
                dateOrdered.setValue(null);
            }

            paymentDueDate.setValue(selectedInvoice.getDueDate() != null ? selectedInvoice.getDueDate().toLocalDate() : null);

            paymentTerms.setText(selectedInvoice.getPaymentTerms());
            statusLabel.setText(selectedInvoice.getStatus());
            gross.setText(selectedInvoice.getTotalAmount() != null ? selectedInvoice.getTotalAmount().toString() : "");
            vat.setText(selectedInvoice.getVatAmount() != null ? selectedInvoice.getVatAmount().toString() : "");
            discounted.setText(selectedInvoice.getDiscountAmount() != null ? selectedInvoice.getDiscountAmount().toString() : "");
            grandTotal.setText(selectedInvoice.getNetAmount() != null ? selectedInvoice.getNetAmount().toString() : "");

            if (selectedInvoice.getStatus().equals("Pending")) {
                confirmButton.setText("Approve");
                approveSI(selectedInvoice);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            ObservableList<ProductsInTransact> salesInvoiceProducts = salesInvoiceDAO.loadSalesInvoiceProducts(selectedInvoice.getOrderId());
            initializeProductTable(salesInvoiceProducts);
            printButton.setOnMouseClicked(mouseEvent -> printSI(selectedInvoice, salesInvoiceProducts));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void printSI(SalesInvoice selectedInvoice, ObservableList<ProductsInTransact> salesInvoiceProducts) {
        try {
            double A4_WIDTH_INCHES = 8;
            double A4_HEIGHT_INCHES = 11.5;
            int PRODUCTS_PER_PAGE = 11;

            double A4_WIDTH_PIXELS = A4_WIDTH_INCHES * 96;
            double A4_HEIGHT_PIXELS = A4_HEIGHT_INCHES * 96;

            String salesInvoiceType = "SALES INVOICE";

            int totalProducts = salesInvoiceProducts.size();
            int totalPages = (int) Math.ceil((double) totalProducts / PRODUCTS_PER_PAGE);

            for (int page = 0; page < totalPages; page++) {
                // Load a new instance of the FXML file for each page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoiceReceiptPrintables.fxml"));
                Parent content = loader.load();

                SalesInvoiceReceiptPrintablesController controller = loader.getController();

                int fromIndex = page * PRODUCTS_PER_PAGE;
                int toIndex = Math.min(fromIndex + PRODUCTS_PER_PAGE, totalProducts);
                ObservableList<ProductsInTransact> pageProducts = FXCollections.observableArrayList(
                        salesInvoiceProducts.subList(fromIndex, toIndex));

                controller.printSalesInvoice(selectedInvoice, pageProducts, salesInvoiceType);

                Stage printStage = new Stage();
                Scene scene = new Scene(content, A4_WIDTH_PIXELS, A4_HEIGHT_PIXELS);

                String fileName = customer.getSelectionModel().getSelectedItem() + selectedInvoice.getOrderId() + "_Page" + (page + 1);

                FXMLExporter.exportToImage(content, fileName, printStage);

                printStage.setScene(scene);
                printStage.initStyle(StageStyle.TRANSPARENT);
                printStage.initStyle(StageStyle.UNDECORATED);
                printStage.setResizable(false);
                printStage.showAndWait();  // Show and wait for the user to close this stage
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private void approveSI(SalesInvoice selectedInvoice) {

    }

    private void initializeTableView() {
        TableColumn<ProductsInTransact, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));

        TableColumn<ProductsInTransact, Double> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());

        TableColumn<ProductsInTransact, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));

        TableColumn<ProductsInTransact, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOrderedQuantity()).asObject());

        TableColumn<ProductsInTransact, Double> totalAmountCol = new TableColumn<>("Total Amount");
        totalAmountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

        productsInTransact.getColumns().addAll(descriptionCol, unitPriceCol, unitCol, quantityCol, totalAmountCol);
    }

    private void initializeProductTable(ObservableList<ProductsInTransact> salesInvoiceProducts) {
        productsInTransact.setItems(salesInvoiceProducts);
    }
}
