package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Brand;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BrandDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<Brand> getBrandDetails() {
        ObservableList<Brand> brandList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT brand_id, brand_name FROM brand";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("brand_id");
                String brandName = resultSet.getString("brand_name");

                Brand brand = new Brand(id, brandName); // Assuming Brands class has a constructor that takes id and name
                brandList.add(brand);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return brandList;
    }

    public boolean createBrand(String brandName) {
        String insertQuery = "INSERT INTO brand (brand_name) VALUES (?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, brandName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Brand created successfully: " + brandName);
                return true;
            } else {
                System.out.println("Failed to create brand: " + brandName);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the brand: " + brandName);
            return false;
        }
    }



    public int getBrandIdByName(String brandName) {
        String sqlQuery = "SELECT brand_id FROM brand WHERE brand_name = ?";
        int brandId = -1; // Set a default value indicating not found

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, brandName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    brandId = resultSet.getInt("brand_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return brandId;
    }



    public String getBrandNameById(int brandId) {
        String sqlQuery = "SELECT brand_name FROM brand WHERE brand_id = ?";
        String brandName = null; // Default value if not found

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, brandId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    brandName = resultSet.getString("brand_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return brandName;
    }

    public ObservableList<String> getBrandNames() {
        ObservableList<String> brandNames = FXCollections.observableArrayList();
        String sqlQuery = "SELECT brand_name FROM brand";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String brandName = resultSet.getString("brand_name");
                brandNames.add(brandName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return brandNames;
    }


}
