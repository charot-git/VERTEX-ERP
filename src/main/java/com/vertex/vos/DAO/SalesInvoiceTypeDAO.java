package com.vertex.vos.DAO;

import com.vertex.vos.Objects.SalesInvoiceType;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class SalesInvoiceTypeDAO {

    // Assuming DatabaseConnectionPool.getDataSource() provides the HikariDataSource
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<SalesInvoiceType> getSalesInvoiceTypes() {
        ObservableList<SalesInvoiceType> salesInvoiceTypes = FXCollections.observableArrayList();

        String query = "SELECT * FROM sales_invoice_type"; // Query to get all sales return types

        // Using try-with-resources to automatically close the connection, statement, and result set
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                SalesInvoiceType type = new SalesInvoiceType(
                        rs.getInt("id"),
                        rs.getString("type")
                );
                salesInvoiceTypes.add(type);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesInvoiceTypes;
    }
}
