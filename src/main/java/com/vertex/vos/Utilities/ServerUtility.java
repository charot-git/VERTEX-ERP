package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.UserSession;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServerUtility {

    private static final String SERVER_DIRECTORY = "\\\\Charot_dev\\server_images";

    private static final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public static boolean uploadImageAndStoreInDB(File imageFile) {
        try (Connection connection = dataSource.getConnection()) {
            // Copy the image to the network server directory
            Path targetPath = Path.of(SERVER_DIRECTORY, imageFile.getName());
            Files.copy(imageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Get the URL or path where the image is stored on the network server
            String imageUrl = targetPath.toString(); // This URL/path should be stored in the database

            // Store the image URL in the database
            return storeImageUrlInDatabase(connection, imageUrl);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Error", "Error occurred: " + e.getMessage());
            return false;
        }
    }

    public static boolean storeImageUrlInDatabase(Connection connection, String imageUrl) {
        try {
            // SQL statement to update the user's image URL in the database
            String sql = "UPDATE user SET user_image = ? WHERE user_id = ?"; // Modify this query accordingly

            // Prepared statement to prevent SQL injection
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, imageUrl);
            preparedStatement.setInt(2, UserSession.getInstance().getUserId()); // Replace userId with the actual user ID
            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();

            // Close resources
            preparedStatement.close();

            // Return true if the update was successful
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean uploadProductImageAndStoreInDB(File imageFile, int productId) {
        try (Connection connection = dataSource.getConnection()) {
            // Copy the image to the network server directory
            Path targetPath = Path.of(SERVER_DIRECTORY, "product_" + productId + "_" + imageFile.getName());
            Files.copy(imageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Get the URL or path where the image is stored on the network server
            String imageUrl = targetPath.toString(); // This URL/path should be stored in the database

            // Store the image URL in the database for the corresponding product
            return storeProductImageUrlInDatabase(connection, imageUrl, productId);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Error", "Error occurred: " + e.getMessage());
            return false;
        }
    }

    public static boolean storeProductImageUrlInDatabase(Connection connection, String imageUrl, int productId) {
        try {
            // SQL statement to update the product's image URL in the database
            String sql = "UPDATE products SET product_image = ? WHERE product_id = ?";

            // Prepared statement to prevent SQL injection
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, imageUrl);
            preparedStatement.setInt(2, productId);
            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();

            // Close resources
            preparedStatement.close();

            // Return true if the update was successful
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
