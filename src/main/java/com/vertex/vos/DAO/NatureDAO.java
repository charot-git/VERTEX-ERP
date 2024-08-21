package com.vertex.vos.DAO;

import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NatureDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    /**
     * Retrieves all nature names from the database.
     *
     * @return a List of nature names.
     */
    public List<String> getAllNatureNames() {
        List<String> natureNames = new ArrayList<>();
        String query = "SELECT nature_name FROM nature";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String natureName = resultSet.getString("nature_name");
                natureNames.add(natureName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return natureNames;
    }

    /**
     * Retrieves a nature name by its ID.
     *
     * @param natureId the ID of the nature.
     * @return the nature name or null if not found.
     */
    public String getNatureNameById(int natureId) {
        String natureName = null;
        String query = "SELECT nature_name FROM nature WHERE nature_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, natureId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    natureName = resultSet.getString("nature_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return natureName;
    }

    /**
     * Adds a new nature to the database.
     *
     * @param natureName the name of the nature to add.
     * @return true if the insertion was successful, false otherwise.
     */
    public boolean addNature(String natureName) {
        String query = "INSERT INTO nature (nature_name) VALUES (?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, natureName);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing nature in the database.
     *
     * @param natureId   the ID of the nature to update.
     * @param natureName the new name of the nature.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateNature(int natureId, String natureName) {
        String query = "UPDATE nature SET nature_name = ? WHERE nature_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, natureName);
            statement.setInt(2, natureId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a nature from the database.
     *
     * @param natureId the ID of the nature to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteNature(int natureId) {
        String query = "DELETE FROM nature WHERE nature_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, natureId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
