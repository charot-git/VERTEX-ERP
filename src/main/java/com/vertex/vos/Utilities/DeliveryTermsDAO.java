package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.DeliveryTerms;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeliveryTermsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public void insertDeliveryTerm(DeliveryTerms deliveryTerms) throws SQLException {
        String query = "INSERT INTO delivery_terms (id, delivery_name) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, deliveryTerms.getId());
            preparedStatement.setString(2, deliveryTerms.getDeliveryName());

            preparedStatement.executeUpdate();
        }
    }

    public List<DeliveryTerms> getAllDeliveryTerms() throws SQLException {
        List<DeliveryTerms> deliveryTermsList = new ArrayList<>();
        String query = "SELECT id, delivery_name FROM delivery_terms";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                DeliveryTerms deliveryTerms = new DeliveryTerms();
                deliveryTerms.setId(resultSet.getInt("id"));
                deliveryTerms.setDeliveryName(resultSet.getString("delivery_name"));
                deliveryTermsList.add(deliveryTerms);
            }
        }
        return deliveryTermsList;
    }

    public int getDeliveryTermById(int id) throws SQLException {
        String query = "SELECT id, delivery_name FROM delivery_terms WHERE id = ?";
        int deliveryTermId = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    deliveryTermId = resultSet.getInt("id");
                }
            }
        }
        return deliveryTermId;
    }

    public int getDeliveryIdByName(String name) {
        String query = "SELECT id FROM delivery_terms WHERE delivery_name = ?";
        int deliveryId = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    deliveryId = resultSet.getInt("id");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return deliveryId;
    }

    public String getDeliveryNameById(int id) {
        String query = "SELECT delivery_name FROM delivery_terms WHERE id = ?";
        String deliveryName = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    deliveryName = resultSet.getString("delivery_name");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return deliveryName;
    }


    public int getDeliveryTermIdByName(String name)  {
        String query = "SELECT id FROM delivery_terms WHERE delivery_name = ?";
        int deliveryTermId = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    deliveryTermId = resultSet.getInt("id");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return deliveryTermId;
    }


}
