package com.vertex.vos.Utilities;

import com.vertex.vos.ChecklistDTO;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.DialogUtils;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class PicklistPrintables {

    private static final int MAX_ROWS_PER_PAGE = 22;

    public static void exportChecklistToWord(Consolidation consolidation, List<ChecklistDTO> products, User checker, User picker) {
        try (XWPFDocument document = new XWPFDocument()) {
            int totalPages = (int) Math.ceil((double) products.size() / MAX_ROWS_PER_PAGE);
            int currentPage = 1;
            int productIndex = 0;
            XWPFHeaderFooterPolicy headerFooterPolicy = document.createHeaderFooterPolicy();

            // Header
            XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
            XWPFParagraph headerParagraph = header.createParagraph();
            headerParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun headerRun = headerParagraph.createRun();
            headerRun.setText("Picklist - " + consolidation.getConsolidationNo());
            headerRun.setBold(true);
            headerRun.setFontSize(14);

            while (productIndex < products.size()) {
                if (currentPage > 1) {
                    document.createParagraph().setPageBreak(true);
                }
                // Info Table
                XWPFTable infoTable = document.createTable();
                infoTable.setWidth("100%");
                infoTable.setTableAlignment(TableRowAlign.CENTER);
                infoTable.removeBorders();
                XWPFTableRow infoRow = infoTable.getRow(0);
                XWPFTableCell checkerCell = infoRow.getCell(0);
                checkerCell.setText("Checker: " + checker.getUser_fname() + " " + checker.getUser_lname());
                XWPFTableCell pickerCell = infoRow.addNewTableCell();
                pickerCell.setText("Picker: " + picker.getUser_fname() + " " + picker.getUser_lname());

                checkerCell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(5000));
                pickerCell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(5000));

                XWPFParagraph spacer = document.createParagraph();
                XWPFRun spacerRun = spacer.createRun();
                spacerRun.addBreak();

                XWPFTable productsTable = document.createTable();
                productsTable.setWidth("100%");
                productsTable.setTableAlignment(TableRowAlign.CENTER);

                XWPFTableRow productsHeaderRow = productsTable.getRow(0);
                productsHeaderRow.getCell(0).setText("Brand");
                productsHeaderRow.addNewTableCell().setText("Category");
                productsHeaderRow.addNewTableCell().setText("SKU");
                productsHeaderRow.addNewTableCell().setText("UOM");
                productsHeaderRow.addNewTableCell().setText("Ordered");
                productsHeaderRow.addNewTableCell().setText("Served");

                int rowsAdded = 0;
                while (productIndex < products.size() && rowsAdded < MAX_ROWS_PER_PAGE) {
                    ChecklistDTO product = products.get(productIndex++);
                    XWPFTableRow productRow = productsTable.createRow();
                    productRow.getCell(0).setText(product.getProduct().getProductBrandString());
                    productRow.getCell(1).setText(product.getProduct().getProductCategoryString());
                    productRow.getCell(2).setText(product.getProduct().getProductName());
                    productRow.getCell(3).setText(product.getProduct().getUnitOfMeasurementString());
                    productRow.getCell(4).setText(String.valueOf(product.getOrderedQuantity()));
                    productRow.getCell(5).setText(String.valueOf(product.getServedQuantity()));
                    rowsAdded++;
                }



                // Footer
                XWPFFooter footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);
                XWPFParagraph footerParagraph = footer.createParagraph();
                footerParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun footerRun = footerParagraph.createRun();
                footerRun.setText("Page " + currentPage + " of " + totalPages);

                currentPage++;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Checklist");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Document", "*.docx"));
            Stage stage = new Stage();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileOutputStream out = new FileOutputStream(file)) {
                    document.write(out);
                }
            }

        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", e.getMessage());
        }
    }
}