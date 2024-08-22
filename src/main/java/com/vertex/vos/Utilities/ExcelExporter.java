package com.vertex.vos.Utilities;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelExporter {

    public static void exportToExcel(TableView<?> tableView, String filePath) throws IOException {
        // Create a new workbook and a sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // Create a font and cell style for the header
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Create a header row with styles
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < tableView.getColumns().size(); i++) {
            TableColumn<?, ?> column = tableView.getColumns().get(i);
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(column.getText());
            cell.setCellStyle(headerCellStyle);
        }

        // Create a regular font and cell style for data rows
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);

        // Add data to the sheet with styles
        ObservableList<?> items = tableView.getItems();
        for (int rowIndex = 0; rowIndex < items.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            for (int columnIndex = 0; columnIndex < tableView.getColumns().size(); columnIndex++) {
                TableColumn<?, ?> column = tableView.getColumns().get(columnIndex);
                Object cellData = column.getCellData(rowIndex);
                Cell cell = row.createCell(columnIndex);

                // Apply data style
                cell.setCellStyle(dataCellStyle);

                // Handle different data types
                if (cellData instanceof Number) {
                    cell.setCellValue(((Number) cellData).doubleValue());
                } else if (cellData instanceof Boolean) {
                    cell.setCellValue((Boolean) cellData);
                } else {
                    cell.setCellValue(cellData == null ? "" : cellData.toString());
                }
            }
        }

        // Auto-size all the columns to fit the content
        for (int i = 0; i < tableView.getColumns().size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the workbook to the file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        // Close the workbook
        workbook.close();
    }
}
