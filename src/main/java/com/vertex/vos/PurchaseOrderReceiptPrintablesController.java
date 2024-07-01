package com.vertex.vos;

import com.vertex.vos.Objects.Company;
import com.vertex.vos.Objects.ProductsInTransact;
import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Objects.Supplier;
import com.vertex.vos.Utilities.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class PurchaseOrderReceiptPrintablesController {

    @FXML
    private ImageView barcode;

    @FXML
    private VBox containerBox;

    @FXML
    private HBox headerBox;

    @FXML
    private Label headerCompanyAdditionalDetails;

    @FXML
    private Label headerCompanyAddress;

    @FXML
    private Label headerCompanyText;

    @FXML
    private ImageView headerLogo;

    @FXML
    private Label number;

    @FXML
    private Label subHeaderAdditionalDetails;

    @FXML
    private HBox subHeaderBox;

    @FXML
    private Label subHeaderLabel;

    @FXML
    private ImageView subHeaderLogo;

    @FXML
    private Label subHeaderSubLabel;

    @FXML
    private TableView<ProductsInTransact> tableView;

    public void printApprovedPO(int po_number) throws SQLException {
        PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
        SupplierDAO supplierDAO = new SupplierDAO();
        CompanyDAO companyDAO = new CompanyDAO();
        Company company = companyDAO.getCompanyById(9); // Replace with your company ID
        PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(po_number);
        Supplier selectedSupplier = supplierDAO.getSupplierById(purchaseOrder.getSupplierName());

        barcode.setImage(BarcodePrinter.generateBarcodeImage(String.valueOf(purchaseOrder.getPurchaseOrderNo())));
        number.setText("PO#" + po_number);
        headerCompanyText.setText(company.getCompanyName());

        String companyLogoURL = company.getCompanyLogo();
        Image companyImage;
        if (companyLogoURL != null && !companyLogoURL.isEmpty()) {
            companyImage = new Image(new File(companyLogoURL).toURI().toString());
        }
        else {
            companyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/business-and-trade.png")));
        }

        headerCompanyAddress.setText(company.getCompanyFirstAddress());
        headerCompanyAdditionalDetails.setText(company.getCompanyContact());
        headerLogo.setImage(companyImage);

        String supplierImageURL = selectedSupplier.getSupplierImage();
        Image supplierImage;
        if (supplierImageURL != null && !supplierImageURL.isEmpty()) {
            supplierImage = new Image(new File(supplierImageURL).toURI().toString());
        } else {
            supplierImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Supplier Info.png")));
        }

        subHeaderLabel.setText(selectedSupplier.getSupplierName());
        subHeaderSubLabel.setText(selectedSupplier.getAddress());
        subHeaderAdditionalDetails.setText(selectedSupplier.getEmailAddress());
        subHeaderLogo.setImage(supplierImage);

        populateTable(purchaseOrder);
    }


    private void populateTable(PurchaseOrder purchaseOrder) throws SQLException {
        tableView.getColumns().clear();
        tableView.getItems().clear();
        PurchaseOrderProductDAO purchaseOrderProductDAO = new PurchaseOrderProductDAO();
        List<ProductsInTransact> productsForApproval = purchaseOrderProductDAO.getProductsForApprovalPrinting(purchaseOrder.getPurchaseOrderNo());
        TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        TableColumn<ProductsInTransact, Integer> orderedQtyColumn = new TableColumn<>("Ordered Quantity");
        orderedQtyColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        tableView.getColumns().addAll(descriptionColumn, unitColumn, orderedQtyColumn);
        tableView.getItems().addAll(productsForApproval);
    }

}

