package com.vertex.vos.DAO;

import com.vertex.vos.Objects.Branch;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.ProductLedger;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class ProductLedgerDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    ProductDAO productDAO = new ProductDAO();

    public ObservableList<ProductLedger> getProductLedger(Timestamp startDate, Timestamp endDate, ObservableList<Product> products, Branch branch) {
        ObservableList<ProductLedger> productLedgers = FXCollections.observableArrayList();
        productLedgers.addAll(getPurchaseOrderLedger(startDate, endDate, products, branch));
        productLedgers.addAll(getSalesInvoiceLedger(startDate, endDate, products, branch));
        productLedgers.addAll(getStockTransferLedger(startDate.toLocalDateTime().toLocalDate(), endDate.toLocalDateTime().toLocalDate(), products, branch));
        productLedgers.addAll(getSalesReturnLedger(startDate, endDate, products, branch));
        productLedgers.addAll(getPhysicalInventoryLedger(startDate, endDate, products, branch));
        return productLedgers;
    }

    public ObservableList<ProductLedger> getPurchaseOrderLedger(Timestamp startDate, Timestamp endDate, ObservableList<Product> products, Branch branch) {
        ObservableList<ProductLedger> productLedgers = FXCollections.observableArrayList();

        String sql = "SELECT po.*, por.received_quantity, por.unit_price, por.total_amount " +
                "FROM purchase_order po " +
                "JOIN purchase_order_receiving por ON po.purchase_order_no = por.purchase_order_id " +
                "WHERE (po.inventory_status IN (6, 8, 9)) " +
                "AND (po.date_received BETWEEN ? AND ?) " +
                "AND (por.product_id = ?) " +
                "AND (por.branch_id = ?)";

        try (Connection conn = dataSource.getConnection()) {
            for (Product product : products) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setTimestamp(1, startDate);
                    stmt.setTimestamp(2, endDate);
                    stmt.setInt(3, product.getProductId());
                    stmt.setInt(4, branch.getId());

                    System.out.println("Executing query for product ID: " + product.getProductId() + " and branch ID: " + branch.getId());

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ProductLedger ledger = new ProductLedger();
                        ledger.setProduct(product);
                        ledger.setQuantity(rs.getInt("received_quantity"));
                        ledger.setDocumentNo(rs.getString("purchase_order_no"));
                        ledger.setDocumentDate(rs.getDate("date_received"));
                        ledger.setDocumentType("Purchase Order");
                        ledger.setDocumentDescription(branch.getBranchCode());
                        ledger.setIn(product.getUnitOfMeasurementCount() * ledger.getQuantity());
                        ledger.setOut(0);
                        productLedgers.add(ledger);

                        System.out.println("Added ledger entry for product ID: " + product.getProductId() + ", quantity: " + rs.getInt("received_quantity"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error while fetching purchase order ledger: " + e.getMessage());
        }

        System.out.println("Total ledgers retrieved: " + productLedgers.size());
        return productLedgers;
    }

    public ObservableList<ProductLedger> getSalesInvoiceLedger(Timestamp startDate, Timestamp endDate, ObservableList<Product> products, Branch branch) {
        ObservableList<ProductLedger> productLedgers = FXCollections.observableArrayList();

        if (products.isEmpty()) {
            return productLedgers; // Return empty if no products are provided
        }

        String sql = "SELECT si.*, sid.product_id, sid.unit_price, sid.quantity, sid.total_amount, c.store_name " +
                "FROM sales_invoice si " +
                "JOIN sales_invoice_details sid ON si.invoice_id = sid.invoice_no " +
                "JOIN salesman s ON si.salesman_id = s.id " +
                "JOIN customer c ON si.customer_code = c.customer_code " +
                "WHERE si.isDispatched = 1 " +
                "AND si.dispatch_date BETWEEN ? AND ? " +
                "AND s.branch_code = ? " +
                "AND sid.product_id = ?"; // Use a placeholder for product ID

        try (Connection conn = dataSource.getConnection()) {
            for (Product product : products) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setTimestamp(1, startDate);
                    stmt.setTimestamp(2, endDate);
                    stmt.setInt(3, branch.getId()); // Assuming Branch has a method getId()
                    stmt.setInt(4, product.getProductId()); // Set the product ID

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ProductLedger ledger = new ProductLedger();
                        ledger.setProduct(product); // Assuming Product has a constructor that takes product ID
                        ledger.setQuantity(rs.getInt("quantity"));
                        ledger.setDocumentNo(rs.getString("invoice_no"));
                        ledger.setDocumentDate(rs.getTimestamp("invoice_date"));
                        ledger.setDocumentType("Sales Invoice");
                        ledger.setDocumentDescription(rs.getString("store_name")); // Set description to store name
                        ledger.setOut(product.getUnitOfMeasurementCount() * ledger.getQuantity()); // Assuming this is the quantity sold
                        ledger.setIn(0); // Set to 0 or calculate based on your logic
                        productLedgers.add(ledger);
                    }
                }
            }
        } catch (SQLException e) {
            // Log the exception (consider using a logging framework)
            System.err.println("SQL error while fetching sales invoice ledger: " + e.getMessage());
        }

        return productLedgers; // Return the list, which may be empty if no records were found
    }

    public ObservableList<ProductLedger> getPhysicalInventoryLedger(Timestamp startDate, Timestamp endDate, ObservableList<Product> products, Branch branch) {
        ObservableList<ProductLedger> productLedgers = FXCollections.observableArrayList();

        if (products.isEmpty()) {
            return productLedgers; // Return empty if no products are provided
        }

        String sql = "SELECT pi.ph_no, pi.date_encoded, pi.cutOff_date, " +
                "pid.product_id, p.description, pid.physical_count, " +
                "pid.unit_price, pid.amount " +
                "FROM physical_inventory pi " +
                "JOIN physical_inventory_details pid ON pi.id = pid.ph_id " +
                "JOIN products p ON pid.product_id = p.product_id " +
                "WHERE pi.isComitted = 1 " +
                "AND pi.cutOff_date BETWEEN ? AND ? " +
                "AND pi.branch_id = ? " +
                "AND pid.product_id = ?"; // Use a placeholder for product ID

        try (Connection conn = dataSource.getConnection()) {
            for (Product product : products) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setTimestamp(1, startDate);
                    stmt.setTimestamp(2, endDate);
                    stmt.setInt(3, branch.getId()); // Assuming Branch has a method getId()
                    stmt.setInt(4, product.getProductId()); // Set the product ID

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ProductLedger ledger = new ProductLedger();
                        ledger.setProduct(product); // Assuming Product has a constructor that takes product ID
                        ledger.setQuantity(rs.getInt("physical_count")); // Physical count
                        ledger.setDocumentNo(rs.getString("ph_no"));
                        ledger.setDocumentDate(rs.getTimestamp("date_encoded"));
                        ledger.setDocumentType("Physical Inventory");
                        ledger.setDocumentDescription("Cut-off Date: " + rs.getTimestamp("cutOff_date"));

                        productLedgers.add(ledger);
                    }
                }
            }
        } catch (SQLException e) {
            // Log the exception (consider using a logging framework)
            System.err.println("SQL error while fetching physical inventory ledger: " + e.getMessage());
        }

        return productLedgers; // Return the list, which may be empty if no records were found
    }

    public ObservableList<ProductLedger> getSalesReturnLedger(Timestamp startDate, Timestamp endDate, ObservableList<Product> products, Branch branch) {
        ObservableList<ProductLedger> productLedgers = FXCollections.observableArrayList();

        if (products.isEmpty()) {
            return productLedgers; // Return empty if no products are provided
        }

        String sql = "SELECT sr.*, srd.product_id, srd.quantity, c.store_name " +
                "FROM sales_return sr " +
                "JOIN sales_return_details srd ON sr.return_number = srd.return_no " +
                "JOIN salesman s ON sr.salesman_id = s.id " +
                "JOIN customer c ON sr.customer_code = c.customer_code " + // Join with customer table
                "WHERE sr.isReceived = 1 " +
                "AND sr.return_date BETWEEN ? AND ? " +
                "AND s.branch_code = ? " +
                "AND srd.product_id = ?"; // Use a placeholder for product ID

        try (Connection conn = dataSource.getConnection()) {
            for (Product product : products) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setTimestamp(1, startDate);
                    stmt.setTimestamp(2, endDate);
                    stmt.setInt(3, branch.getId()); // Assuming Branch has a method getId()
                    stmt.setInt(4, product.getProductId()); // Set the product ID

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ProductLedger ledger = new ProductLedger();
                        ledger.setProduct(product); // Assuming Product has a constructor that takes product ID
                        ledger.setQuantity(rs.getInt("quantity"));
                        ledger.setDocumentNo(rs.getString("return_number"));
                        ledger.setDocumentDate(rs.getTimestamp("return_date"));
                        ledger.setDocumentType("Sales Return");
                        ledger.setDocumentDescription("Store: " + rs.getString("store_name")); // Set description to include store name
                        ledger.setIn(product.getUnitOfMeasurementCount() * ledger.getQuantity());
                        ledger.setOut(0); // Set to 0 or calculate based on your logic
                        productLedgers.add(ledger);
                    }
                }
            }
        } catch (SQLException e) {
            // Log the exception (consider using a logging framework)
            System.err.println("SQL error while fetching sales return ledger: " + e.getMessage());
        }

        return productLedgers; // Return the list, which may be empty if no records were found
    }


    public ObservableList<ProductLedger> getStockTransferLedger(LocalDate startDate, LocalDate endDate, ObservableList<Product> products, Branch branch) {
        ObservableList<ProductLedger> productLedgers = FXCollections.observableArrayList();

        if (products.isEmpty()) {
            return productLedgers; // Return empty if no products are provided
        }

        String sql = "SELECT st.*, b1.branch_code AS source_branch_code, b2.branch_code AS target_branch_code " +
                "FROM stock_transfer st " +
                "JOIN branches b1 ON st.source_branch = b1.id " +
                "JOIN branches b2 ON st.target_branch = b2.id " +
                "WHERE st.date_requested BETWEEN ? AND ? " +
                "AND st.target_branch = ? " +
                "AND st.product_id = ?"; // Use a placeholder for product ID

        try (Connection conn = dataSource.getConnection()) {
            for (Product product : products) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setDate(1, Date.valueOf(startDate));
                    stmt.setDate(2, Date.valueOf(endDate));
                    stmt.setInt(3, branch.getId()); // Assuming Branch has a method getId()
                    stmt.setInt(4, product.getProductId()); // Set the product ID

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ProductLedger ledger = new ProductLedger();
                        ledger.setProduct(product); // Assuming Product has a constructor that takes product ID
                        ledger.setQuantity(rs.getInt("ordered_quantity"));
                        ledger.setDocumentNo(rs.getString("order_no"));
                        ledger.setDocumentDate(rs.getDate("date_requested"));
                        ledger.setDocumentType("Stock Transfer");
                        ledger.setDocumentDescription(rs.getString("source_branch_code")); // Set description to include branch codes
                        ledger.setIn(product.getUnitOfMeasurementCount() * ledger.getQuantity()); // Set to 0 or calculate based on your logic
                        ledger.setOut(0); // Assuming this is the quantity transferred
                        productLedgers.add(ledger);
                    }
                }
            }
        } catch (SQLException e) {
            // Log the exception (consider using a logging framework)
            System.err.println("SQL error while fetching stock transfer ledger: " + e.getMessage());
        }
        return productLedgers; // Return the list, which may be empty if no records were found
    }

    public List<String> getAllProductNames() {
        return productDAO.getAllProductNames();
    }
}
