package com.vertex.vos.Utilities;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelExporter {

    public static void exportToExcel(TableView<?> tableView, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // Create styles
        CellStyle headerCellStyle = createHeaderCellStyle(workbook);
        CellStyle dataCellStyle = createDataCellStyle(workbook);

        // Get all columns (including sub-columns)
        List<TableColumn<?, ?>> flatColumns = new ArrayList<>();
        extractColumns(tableView.getColumns(), flatColumns);

        // Create header rows
        int headerDepth = getMaxColumnDepth(tableView.getColumns());
        for (int rowIndex = 0; rowIndex < headerDepth; rowIndex++) {
            sheet.createRow(rowIndex);
        }
        createHeader(sheet, tableView.getColumns(), headerCellStyle, 0, 0);

        // Fill data rows
        ObservableList<?> items = tableView.getItems();
        for (int rowIndex = 0; rowIndex < items.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + headerDepth);
            for (int columnIndex = 0; columnIndex < flatColumns.size(); columnIndex++) {
                TableColumn<?, ?> column = flatColumns.get(columnIndex);
                Object cellData = column.getCellData(rowIndex);
                Cell cell = row.createCell(columnIndex);
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

        // Auto-size columns
        for (int i = 0; i < flatColumns.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    private static void extractColumns(List<? extends TableColumn<?, ?>> columns, List<TableColumn<?, ?>> flatList) {
        for (TableColumn<?, ?> column : columns) {
            if (column.getColumns().isEmpty()) {
                flatList.add(column);
            } else {
                extractColumns(column.getColumns(), flatList);
            }
        }
    }

    private static int createHeader(Sheet sheet, List<? extends TableColumn<?, ?>> columns, CellStyle style, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        int currentColumn = colIndex;

        for (TableColumn<?, ?> column : columns) {
            int colSpan = countLeafColumns(column);
            Cell cell = row.createCell(currentColumn);
            cell.setCellValue(column.getText());
            cell.setCellStyle(style);

            if (colSpan > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, currentColumn, currentColumn + colSpan - 1));
            }

            if (!column.getColumns().isEmpty()) {
                createHeader(sheet, column.getColumns(), style, rowIndex + 1, currentColumn);
            }

            currentColumn += colSpan;
        }
        return currentColumn;
    }

    private static int countLeafColumns(TableColumn<?, ?> column) {
        if (column.getColumns().isEmpty()) return 1;
        return column.getColumns().stream().mapToInt(ExcelExporter::countLeafColumns).sum();
    }

    private static int getMaxColumnDepth(List<? extends TableColumn<?, ?>> columns) {
        int maxDepth = 1;
        for (TableColumn<?, ?> column : columns) {
            if (!column.getColumns().isEmpty()) {
                maxDepth = Math.max(maxDepth, 1 + getMaxColumnDepth(column.getColumns()));
            }
        }
        return maxDepth;
    }

    private static CellStyle createHeaderCellStyle(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);

        CellStyle style = workbook.createCellStyle();
        style.setFont(headerFont);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
}
