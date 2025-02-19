package com.vertex.vos.DAO;

import com.vertex.vos.Objects.SalesInvoiceType;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalesInvoiceTypeDAO {
    private static final Logger LOGGER = Logger.getLogger(SalesInvoiceTypeDAO.class.getName());

    // Assuming DatabaseConnectionPool.getDataSource() provides the HikariDataSource
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<SalesInvoiceType> getSalesInvoiceTypes() {
        ObservableList<SalesInvoiceType> salesInvoiceTypes = FXCollections.observableArrayList();
        String query = "SELECT * FROM sales_invoice_type";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                salesInvoiceTypes.add(new SalesInvoiceType(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("shortcut")
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching sales invoice types.", e);
        }

        return salesInvoiceTypes;
    }

    public SalesInvoiceType getSalesInvoiceTypeById(int id) {
        String query = "SELECT * FROM sales_invoice_type WHERE id = ?";
        SalesInvoiceType salesInvoiceType = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    salesInvoiceType = new SalesInvoiceType(
                            rs.getInt("id"),
                            rs.getString("type"),
                            rs.getString("shortcut")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching SalesInvoiceType with id: " + id, e);
        }

        return salesInvoiceType;
    }

}
