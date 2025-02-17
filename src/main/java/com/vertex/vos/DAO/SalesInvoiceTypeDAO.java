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
                        rs.getString("type"),
                        rs.getString("shortcut")
                );
                salesInvoiceTypes.add(type);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesInvoiceTypes;
    }

    public SalesInvoiceType getInvoiceIdByType(int typeId) {
        String sql = "SELECT * FROM sales_invoice_type WHERE id = ?";
        SalesInvoiceType invoiceType = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, typeId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String typeName = resultSet.getString("type");
                String shortcut = resultSet.getString("shortcut");

                // Instantiate and populate the InvoiceType object
                invoiceType = new SalesInvoiceType();
                invoiceType.setId(id);
                invoiceType.setName(typeName);
                invoiceType.setShortcut(shortcut);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception for debugging
        }

        return invoiceType;
    }
}
