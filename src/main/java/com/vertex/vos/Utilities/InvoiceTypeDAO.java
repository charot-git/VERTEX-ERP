package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.InvoiceType;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InvoiceTypeDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<InvoiceType> getAllInvoiceTypes() {
        String sql = "SELECT * FROM sales_invoice_type";
        List<InvoiceType> invoiceTypes = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String type = resultSet.getString("type");

                InvoiceType invoiceType = new InvoiceType(id, type);
                invoiceTypes.add(invoiceType);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        }

        return invoiceTypes;
    }

    //get invoice id by type
    public int getInvoiceIdByType(String type) {
        String sql = "SELECT id FROM sales_invoice_type WHERE type = ?";
        int id = 0;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, type);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        }
        return id;
    }

    //get type by id
    public String getTypeById(int id) {
        String sql = "SELECT type FROM sales_invoice_type WHERE id = ?";
        String type = "";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                type = resultSet.getString("type");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        }
        return type;
    }

    public String getInvoiceTypeById(int invoiceType) {
        String sql = "SELECT type FROM sales_invoice_type WHERE id = ?";
        String type = "";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, invoiceType);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                type = resultSet.getString("type");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        }
        return type;
    }
}
