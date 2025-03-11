package com.vertex.vos.DAO;

import com.vertex.vos.Enums.DocumentType;
import com.vertex.vos.Enums.PickListItemStatus;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.vertex.vos.Utilities.WarehouseBrandLinkDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PickListItemDAO {
    HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    WarehouseBrandLinkDAO warehouseBrandLinkDAO = new WarehouseBrandLinkDAO();
    ProductDAO productDAO = new ProductDAO();

    ObservableList<SalesInvoiceDetail> salesInvoiceDetails = FXCollections.observableArrayList();
    ObservableList<StockTransfer> stockTransferDetails = FXCollections.observableArrayList();

    public ObservableList<PickListItem> getPickListItemsForEncoding(Branch branch, User pickedBy, Timestamp pickDate, PickList pickList) {
        ObservableList<Integer> brandIds = warehouseBrandLinkDAO.getLinkedBrands(pickedBy.getUser_id());
        ObservableList<PickListItem> pickListItems = FXCollections.observableArrayList();

        if (brandIds.isEmpty()) {
            System.out.println("No brands linked to pickedBy user: " + pickedBy.getUser_id());
            return pickListItems; // No brands linked, return empty list
        }

        System.out.println("Fetching sales invoice details for branch: " + branch.getId());
        salesInvoiceDetails.setAll(getSalesInvoiceDetailsForPicking(brandIds, branch.getId()));

        System.out.println("Fetching stock transfer details for branch: " + branch.getId());
        stockTransferDetails.setAll(getStockTransferDetailsForPicking(brandIds, branch.getId()));

        System.out.println("Converting sales invoice details to PickListItem objects.");
        for (SalesInvoiceDetail detail : salesInvoiceDetails) {
            PickListItem pickListItem = getPickListItemFromInvoices(pickList, detail);
            pickListItems.add(pickListItem);
        }

        System.out.println("Converting stock transfer details to PickListItem objects.");
        for (StockTransfer detail : stockTransferDetails) {
            PickListItem pickListItem = getPickListItemFromStockTransfer(pickList, detail);
            pickListItems.add(pickListItem);
        }

        System.out.println("Returning " + pickListItems.size() + " PickListItem objects.");
        return pickListItems;
    }

    private PickListItem getPickListItemFromStockTransfer(PickList pickList, StockTransfer detail) {
        PickListItem pickListItem = new PickListItem();
        pickListItem.setPickList(pickList);
        pickListItem.setDocType(DocumentType.STOCK_TRANSFER);
        pickListItem.setDocNo(detail.getOrderNo());
        pickListItem.setProduct(detail.getProduct());
        pickListItem.setOrderedQuantity(detail.getOrderedQuantity());
        pickListItem.setPickedQuantity(0);
        pickListItem.setStatus(PickListItemStatus.PENDING);
        pickListItem.setCreatedAt(Timestamp.valueOf(detail.getDateRequested().toLocalDate().atStartOfDay()));
        pickListItem.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return pickListItem;
    }

    private static PickListItem getPickListItemFromInvoices(PickList pickList, SalesInvoiceDetail detail) {
        PickListItem pickListItem = new PickListItem();
        pickListItem.setPickList(pickList);
        pickListItem.setDocType(DocumentType.SALES_INVOICE);
        pickListItem.setDocNo(detail.getSalesInvoiceNo().getInvoiceNo());
        pickListItem.setProduct(detail.getProduct());
        pickListItem.setOrderedQuantity(detail.getQuantity());
        pickListItem.setPickedQuantity(0);
        pickListItem.setStatus(PickListItemStatus.PENDING);
        pickListItem.setCreatedAt(detail.getCreatedAt());
        pickListItem.setUpdatedAt(detail.getModifiedAt());
        return pickListItem;
    }

    private ObservableList<StockTransfer> getStockTransferDetailsForPicking(ObservableList<Integer> brandIds, int sourceBranch) {
        ObservableList<StockTransfer> stockTransfers = FXCollections.observableArrayList();

        if (brandIds.isEmpty()) {
            return stockTransfers;
        }

        // Create placeholders dynamically for the IN clause
        String placeholders = brandIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = """
                    SELECT st.id, st.order_no, st.source_branch, st.target_branch, st.product_id, 
                           st.ordered_quantity, st.received_quantity, st.amount, st.date_requested, 
                           st.lead_date, st.status, st.date_received, st.receiver_id, st.encoder_id 
                    FROM stock_transfer st
                    JOIN products p ON st.product_id = p.product_id
                    WHERE st.status = 'FOR PICKING' 
                      AND st.source_branch = ?
                      AND p.product_brand IN (%s)
                """.formatted(placeholders);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sourceBranch);

            for (int i = 0; i < brandIds.size(); i++) {
                pstmt.setInt(i + 2, brandIds.get(i)); // Start at index 2 because index 1 is sourceBranch
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StockTransfer stockTransfer = new StockTransfer();
                    stockTransfer.setOrderNo(rs.getString("order_no"));
                    stockTransfer.setSourceBranch(rs.getInt("source_branch"));
                    stockTransfer.setTargetBranch(rs.getInt("target_branch"));
                    stockTransfer.setOrderedQuantity(rs.getInt("ordered_quantity"));
                    stockTransfer.setReceivedQuantity(rs.getInt("received_quantity"));
                    stockTransfer.setAmount(rs.getDouble("amount"));
                    stockTransfer.setDateRequested(rs.getDate("date_requested"));
                    stockTransfer.setLeadDate(rs.getDate("lead_date"));
                    stockTransfer.setStatus(rs.getString("status"));
                    stockTransfer.setDateReceived(rs.getTimestamp("date_received"));
                    stockTransfer.setReceiverId(rs.getInt("receiver_id"));
                    stockTransfer.setEncoderId(rs.getInt("encoder_id"));

                    // Fetch the product details
                    Product product = productDAO.getProductById(rs.getInt("product_id"));
                    stockTransfer.setProduct(product);

                    stockTransfers.add(stockTransfer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with logger in production
        }
        return stockTransfers;
    }

    public ObservableList<SalesInvoiceDetail> getSalesInvoiceDetailsForPicking(ObservableList<Integer> brandIds, int branchCode) {
        ObservableList<SalesInvoiceDetail> invoiceDetails = FXCollections.observableArrayList();

        if (brandIds.isEmpty()) {
            return invoiceDetails;
        }

        // Create SQL query with placeholders
        String placeholders = String.join(", ", Collections.nCopies(brandIds.size(), "?"));

        String sql = """
                SELECT sid.detail_id, sid.order_id, si.invoice_no AS sales_invoice_no, 
                       sid.product_id, sid.quantity, sid.unit_price, sid.created_date,
                       p.product_name, u.unit_name
                FROM sales_invoice_details sid
                JOIN sales_invoice si ON sid.invoice_no = si.invoice_id  -- Correct join
                JOIN products p ON sid.product_id = p.product_id
                JOIN units u ON p.unit_of_measurement = u.unit_id
                JOIN salesman s ON si.salesman_id = s.id
                WHERE si.transaction_status = 'Picking'
                  AND p.product_brand IN (%s)
                  AND s.branch_code = ?
            """.formatted(placeholders);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set brand IDs
            for (int i = 0; i < brandIds.size(); i++) {
                pstmt.setInt(i + 1, brandIds.get(i));
            }
            // Set branch code
            pstmt.setInt(brandIds.size() + 1, branchCode);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SalesInvoiceDetail detail = new SalesInvoiceDetail();
                    detail.setSalesInvoiceDetailId(rs.getInt("detail_id"));
                    detail.setOrderId(rs.getString("order_id"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setUnitPrice(rs.getDouble("unit_price"));

                    SalesInvoiceHeader salesInvoiceHeader = new SalesInvoiceHeader();
                    salesInvoiceHeader.setInvoiceNo(rs.getString("sales_invoice_no")); // Correct invoice number
                    detail.setSalesInvoiceNo(salesInvoiceHeader);
                    detail.setCreatedAt(rs.getTimestamp("created_date"));

                    // Set product details
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setUnitOfMeasurementString(rs.getString("unit_name")); // Now using unit_name
                    detail.setProduct(product);

                    invoiceDetails.add(detail);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error fetching sales invoice details", e);
        }
        return invoiceDetails;
    }





}
