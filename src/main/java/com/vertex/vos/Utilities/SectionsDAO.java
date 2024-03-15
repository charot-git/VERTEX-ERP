package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Section;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SectionsDAO {

    public ObservableList<Section> getSectionDetails() {
        ObservableList<Section> sectionList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT section_id, section_name FROM sections";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int section_id = resultSet.getInt("section_id");
                String section_name = resultSet.getString("section_name");

                Section section = new Section(section_id, section_name);
                sectionList.add(section);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return sectionList;
    }

    public boolean addSection(String sectionName) {
        String insertQuery = "INSERT INTO sections (section_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, sectionName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Section created successfully: " + sectionName);
                return true;
            } else {
                System.out.println("Failed to create section: " + sectionName);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
            // Optionally, show an error message dialog to the user
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the section: " + sectionName);
            return false;
        }
    }


    public int getSectionIdByName(String sectionName) {
        String sqlQuery = "SELECT section_id FROM sections WHERE section_name = ?";
        int sectionId = -1; // Set a default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, sectionName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    sectionId = resultSet.getInt("section_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return sectionId;
    }

    public String getSectionNameById(int sectionId) {
        String sqlQuery = "SELECT section_name FROM sections WHERE section_id = ?";
        String sectionName = null; // Default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, sectionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    sectionName = resultSet.getString("section_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }

        return sectionName;
    }

    public ObservableList<String> getSectionNames() {
        ObservableList<String> sectionNames = FXCollections.observableArrayList();
        String sqlQuery = "SELECT section_name FROM sections";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String sectionName = resultSet.getString("section_name");
                sectionNames.add(sectionName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return sectionNames;
    }
}
