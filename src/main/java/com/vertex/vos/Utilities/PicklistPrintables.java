package com.vertex.vos.Utilities;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vertex.vos.ConsolidationDetails;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.User;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PicklistPrintables {

    private static final int MAX_ROWS_PER_PAGE = 40;

    public static void exportChecklistToPDF(Consolidation consolidation, List<ConsolidationDetails> products, User checker, User picker) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Checklist as PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document", "*.pdf"));
        Stage stage = new Stage();
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) return;

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            PageNumberHelper pageHelper = new PageNumberHelper();
            writer.setPageEvent(pageHelper);

            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Picklist - " + consolidation.getConsolidationNo(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // Info Table (Checker & Picker)
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell(new Phrase("Checker: " + checker.getUser_fname() + " " + checker.getUser_lname()));
            infoTable.addCell(new Phrase("Picker: " + picker.getUser_fname() + " " + picker.getUser_lname()));
            document.add(infoTable);

            document.add(new Paragraph(" "));

            // Product Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 1.8f, 1.8f, 6.5f, 1f, 1f, 1f});

            // Table Header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            String[] headers = {"Supplier", "Brand", "Category", "SKU", "UOM", "Ordered", "Served"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Table Rows
            int rowsAdded = 0;
            for (ConsolidationDetails product : products) {
                table.addCell(new Phrase(product.getProduct().getSupplierName(), new Font(Font.FontFamily.HELVETICA, 10)));
                table.addCell(new Phrase(product.getProduct().getProductBrandString(), new Font(Font.FontFamily.HELVETICA, 10)));
                table.addCell(new Phrase(product.getProduct().getProductCategoryString(), new Font(Font.FontFamily.HELVETICA, 10)));
                table.addCell(new Phrase(product.getProduct().getProductName(), new Font(Font.FontFamily.HELVETICA, 10)));
                table.addCell(new Phrase(product.getProduct().getUnitOfMeasurementString(), new Font(Font.FontFamily.HELVETICA, 10)));
                table.addCell(new Phrase(String.valueOf(product.getOrderedQuantity()), new Font(Font.FontFamily.HELVETICA, 10)));
                table.addCell(new Phrase("", new Font(Font.FontFamily.HELVETICA, 10))); // Served column left blank

                rowsAdded++;
                if (rowsAdded % MAX_ROWS_PER_PAGE == 0) {
                    document.add(table);
                    document.newPage();
                    table.deleteBodyRows();
                }
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            DialogUtils.showErrorMessage("Error", e.getMessage());
        }
    }

    // Custom page event helper for page numbers
    private static class PageNumberHelper extends PdfPageEventHelper {
        private PdfTemplate totalPageTemplate;
        private BaseFont baseFont;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPageTemplate = writer.getDirectContent().createTemplate(50, 50);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.beginText();
            cb.setFontAndSize(baseFont, 10);

            float x = (document.right() + document.left()) / 2;
            float y = document.bottom() - 15;

            // "Page X of "
            cb.setTextMatrix(x - 20, y);
            cb.showText("Page " + writer.getPageNumber() + " of ");
            cb.endText();

            // Placeholder for total page count
            cb.addTemplate(totalPageTemplate, x + 15, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPageTemplate.beginText();
            totalPageTemplate.setFontAndSize(baseFont, 10);
            totalPageTemplate.showText(String.valueOf(writer.getPageNumber() - 1));
            totalPageTemplate.endText();
        }
    }
}
