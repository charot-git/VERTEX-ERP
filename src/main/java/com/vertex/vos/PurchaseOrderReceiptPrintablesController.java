package com.vertex.vos;

import com.vertex.vos.Objects.Company;
import com.vertex.vos.Objects.ProductsInTransact;
import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Objects.Supplier;
import com.vertex.vos.Utilities.*;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    public void printApprovedPO(int poNumber, ObservableList<ProductsInTransact> items) {
        try {
            // Fetch data
            PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
            SupplierDAO supplierDAO = new SupplierDAO();
            CompanyDAO companyDAO = new CompanyDAO();

            Company company = companyDAO.getCompanyById(9); // Replace with your company ID
            PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(poNumber);
            Supplier supplier = supplierDAO.getSupplierById(purchaseOrder.getSupplierName());
            setupHeader(company, purchaseOrder, supplier);
            saveAsPDF(company, supplier, purchaseOrder, items);

        } catch (Exception e) {
            DialogUtils.showErrorMessage("Error", "Failed to generate Purchase Order receipt.");
            e.printStackTrace();
        }
    }

    private void setupHeader(Company company, PurchaseOrder purchaseOrder, Supplier supplier) {
        barcode.setImage(BarcodePrinter.generateBarcodeCode128(String.valueOf(purchaseOrder.getPurchaseOrderNo())));
        number.setText("PO#" + purchaseOrder.getPurchaseOrderNo());
        headerCompanyText.setText(company.getCompanyName());
        headerCompanyAddress.setText(company.getCompanyFirstAddress());
        headerCompanyAdditionalDetails.setText(company.getCompanyContact());
        headerLogo.setImage(loadImage(company.getCompanyLogo(), "/com/vertex/vos/assets/icons/business-and-trade.png"));

        subHeaderLabel.setText(supplier.getSupplierName());
        subHeaderSubLabel.setText(supplier.getAddress());
        subHeaderAdditionalDetails.setText(supplier.getEmailAddress());
        subHeaderLogo.setImage(loadImage(supplier.getSupplierImage(), "/com/vertex/vos/assets/icons/Supplier Info.png"));
    }

    private Image loadImage(String filePath, String defaultPath) {
        if (filePath != null && !filePath.isEmpty()) {
            return new Image(new File(filePath).toURI().toString());
        }
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream(defaultPath)));
    }


    private void saveAsPDF(Company company, Supplier supplier, PurchaseOrder purchaseOrder, List<ProductsInTransact> products) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            drawItemsTable(document, products, company, supplier, purchaseOrder);
            contentStream.close();
            String pdfFilePath = System.getProperty("user.home") + "/Documents/" + purchaseOrder.getPurchaseOrderNo() + ".pdf";
            document.save(pdfFilePath);
            DialogUtils.showConfirmationDialog("Success", "PDF saved at: " + pdfFilePath);
        }
    }

    private float drawTableHeader(PDPageContentStream contentStream, PDDocument document, float yPosition, float margin, PurchaseOrder purchaseOrder) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);

        // Draw barcode image
        if (barcode.getImage() != null) {
            BufferedImage barcodeBuffered = SwingFXUtils.fromFXImage(barcode.getImage(), null);
            PDImageXObject barcodePDF = LosslessFactory.createFromImage(document, barcodeBuffered);
            contentStream.drawImage(barcodePDF, margin, yPosition - 50, 100, 50);
        }

        contentStream.beginText();
        contentStream.newLineAtOffset(margin + 120, yPosition - 25);
        contentStream.showText("PO#: " + purchaseOrder.getPurchaseOrderNo());
        contentStream.endText();

        return yPosition - 60;
    }

    private float drawDetailsTable(PDPageContentStream contentStream, PDDocument document, float yPosition, float margin, Company company, Supplier supplier) throws IOException {
        // Company details
        if (headerLogo.getImage() != null) {
            BufferedImage companyBuffered = SwingFXUtils.fromFXImage(headerLogo.getImage(), null);
            PDImageXObject companyPDF = LosslessFactory.createFromImage(document, companyBuffered);
            contentStream.drawImage(companyPDF, margin, yPosition - 50, 50, 50);  // Company logo on the left
        }

        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin + 60, yPosition - 20);  // Adjusted text position after company logo
        contentStream.showText(company.getCompanyName());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText(company.getCompanyFirstAddress());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText(company.getCompanyContact());
        contentStream.endText();

        // Adjust Y position after company details
        yPosition -= 70;

        // Supplier details (below company details, aligned to the left)
        if (subHeaderLogo.getImage() != null) {
            BufferedImage supplierBuffered = SwingFXUtils.fromFXImage(subHeaderLogo.getImage(), null);
            PDImageXObject supplierPDF = LosslessFactory.createFromImage(document, supplierBuffered);
            contentStream.drawImage(supplierPDF, margin, yPosition - 50, 50, 50);  // Supplier logo below company logo
        }

        contentStream.beginText();
        contentStream.newLineAtOffset(margin + 60, yPosition - 20);  // Adjusted text position after supplier logo
        contentStream.showText("Supplier: " + supplier.getSupplierName());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText(supplier.getAddress());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText(supplier.getEmailAddress());
        contentStream.endText();

        // Return the updated yPosition after supplier details
        return yPosition - 70;
    }


    private void drawItemsTable(PDDocument document, List<ProductsInTransact> products, Company company, Supplier supplier, PurchaseOrder purchaseOrder) throws IOException {
        final float margin = 50;
        final float yStart = PDRectangle.A4.getHeight() - margin;
        final float yEnd = margin + 50; // Leave space for the footer
        final float rowHeight = 15; // Height for each row
        final float headerHeight = 100; // Estimated height for headers
        final int maxProductsPerPage = 33; // Maximum of 33 products per page

        int totalPages = (int) Math.ceil((double) products.size() / maxProductsPerPage);
        int currentPage = 1;

        for (int i = 0; i < products.size(); i += maxProductsPerPage) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = yStart;

                // Draw headers (company and supplier details) on every page
                yPosition = drawTableHeader(contentStream, document, yPosition, margin, purchaseOrder);
                yPosition = drawDetailsTable(contentStream, document, yPosition, margin, company, supplier);

                // Draw table headers
                yPosition -= 20; // Add some space after the header
                yPosition = drawTableHeaders(contentStream, yPosition, margin);

                // Draw items for this page
                List<ProductsInTransact> pageItems = products.subList(i, Math.min(i + maxProductsPerPage, products.size()));
                for (ProductsInTransact product : pageItems) {
                    yPosition -= rowHeight;
                    drawProductRow(contentStream, yPosition, margin, product);
                }

                // Add footer
                drawFooter(contentStream, currentPage, totalPages, margin);
            }
            currentPage++;
        }
    }


    private float drawTableHeaders(PDPageContentStream contentStream, float yPosition, float margin) throws IOException {
        float[] columnWidths = {70, 70, 270, 30, 40}; // Adjusted widths

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Category");
        contentStream.newLineAtOffset(columnWidths[0], 0);
        contentStream.showText("Brand");
        contentStream.newLineAtOffset(columnWidths[1], 0);
        contentStream.showText("Description");
        contentStream.newLineAtOffset(columnWidths[2], 0);
        contentStream.showText("Unit");
        contentStream.newLineAtOffset(columnWidths[3], 0);
        contentStream.showText("Ordered Quantity");
        contentStream.endText();

        return yPosition - 20; // Move position down for the next row
    }

    private void drawProductRow(PDPageContentStream contentStream, float yPosition, float margin, ProductsInTransact product) throws IOException {
        float[] columnWidths = {70, 70, 270, 30, 40}; // Adjusted widths

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(product.getProductCategoryString() != null ? product.getProductCategoryString() : "N/A");
        contentStream.newLineAtOffset(columnWidths[0], 0);
        contentStream.showText(product.getProductBrandString() != null ? product.getProductBrandString() : "N/A");
        contentStream.newLineAtOffset(columnWidths[1], 0);
        contentStream.showText(product.getDescription());
        contentStream.newLineAtOffset(columnWidths[2], 0);
        contentStream.showText(product.getUnit());
        contentStream.newLineAtOffset(columnWidths[3], 0);
        contentStream.showText(String.valueOf(product.getOrderedQuantity()));
        contentStream.endText();
    }

    private void drawFooter(PDPageContentStream contentStream, int currentPage, int totalPages, float margin) throws IOException {
        float footerYPosition = 30; // Position from the bottom

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(margin, footerYPosition);
        contentStream.showText("Page " + currentPage + " of " + totalPages);
        contentStream.endText();
    }
}
