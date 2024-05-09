package com.vertex.vos;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Constructors.Supplier;
import com.vertex.vos.Utilities.PurchaseOrderDAO;
import com.vertex.vos.Utilities.SupplierDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
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
        PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(po_number);
        Supplier selectedSupplier = supplierDAO.getSupplierById(purchaseOrder.getSupplierName());

        barcode.setImage(BarcodePrinter.generateBarcodeImage(String.valueOf(purchaseOrder.getPurchaseOrderNo())));
        number.setText(String.valueOf(po_number));

        String  supplierImageURL = selectedSupplier.getSupplierImage();
        Image supplierImage;
        if (supplierImageURL != null && !supplierImageURL.isEmpty()) {
            supplierImage = new Image(new File(supplierImageURL).toURI().toString());
        } else {
            supplierImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Supplier Info.png")));
        }

        subHeaderLogo.setImage(supplierImage);
    }
}

