package com.vertex.vos.DAO;

import com.vertex.vos.Objects.SalesInvoiceType;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalesInvoiceTypeDAO {
    private static final Logger LOGGER = Logger.getLogger(SalesInvoiceTypeDAO.class.getName());
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final Map<Integer, SalesInvoiceType> cacheById = new ConcurrentHashMap<>();
    private ObservableList<SalesInvoiceType> cachedList = FXCollections.observableArrayList();
    private boolean isCacheLoaded = false;

    public ObservableList<SalesInvoiceType> getSalesInvoiceTypes() {
        if (!isCacheLoaded) {
            loadCache();
        }
        return FXCollections.observableArrayList(cachedList);
    }

    public SalesInvoiceType getSalesInvoiceTypeById(int id) {
        if (!isCacheLoaded) {
            loadCache();
        }
        return cacheById.get(id);
    }

    private void loadCache() {
        String query = "SELECT * FROM sales_invoice_type";
        ObservableList<SalesInvoiceType> tempSalesInvoiceTypes = FXCollections.observableArrayList();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                SalesInvoiceType salesInvoiceType = new SalesInvoiceType(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("shortcut")
                );
                tempSalesInvoiceTypes.add(salesInvoiceType);
                cacheById.put(salesInvoiceType.getId(), salesInvoiceType);
            }
            cachedList = tempSalesInvoiceTypes;
            isCacheLoaded = true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching sales invoice types.", e);
        }
    }
}
