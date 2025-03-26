package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Brand;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BrandDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final Map<Integer, Brand> brandCache = new HashMap<>();
    private final Map<String, Integer> brandNameToIdCache = new HashMap<>();

    public BrandDAO() {
        loadBrandCache(); // Load cache on initialization
    }

    private void loadBrandCache() {
        String sqlQuery = "SELECT brand_id, brand_name FROM brand";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            brandCache.clear();
            brandNameToIdCache.clear();

            while (resultSet.next()) {
                int id = resultSet.getInt("brand_id");
                String brandName = resultSet.getString("brand_name");

                Brand brand = new Brand(id, brandName);
                brandCache.put(id, brand);
                brandNameToIdCache.put(brandName, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading brand cache");
        }
    }

    public ObservableList<Brand> getBrandDetails() {
        return FXCollections.observableArrayList(brandCache.values());
    }

    public boolean createBrand(String brandName) {
        String insertQuery = "INSERT INTO brand (brand_name) VALUES (?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, brandName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newBrandId = generatedKeys.getInt(1);
                        Brand newBrand = new Brand(newBrandId, brandName);
                        brandCache.put(newBrandId, newBrand);
                        brandNameToIdCache.put(brandName, newBrandId);
                    }
                }
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
        return brandNameToIdCache.getOrDefault(brandName, -1);
    }

    public String getBrandNameById(int brandId) {
        Brand brand = brandCache.get(brandId);
        return (brand != null) ? brand.getBrand_name() : null;
    }

    public ObservableList<String> getBrandNames() {
        return FXCollections.observableArrayList(brandNameToIdCache.keySet());
    }

    public ObservableList<Brand> getAllBrands() {
        return FXCollections.observableArrayList(brandCache.values());
    }
}
