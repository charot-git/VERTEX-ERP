package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Segment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SegmentDAO {

    public ObservableList<Segment> getSegmentDetails() {
        ObservableList<Segment> segmentList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT segment_id, segment_name FROM segment";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int segment_id = resultSet.getInt("segment_id");
                String segment_name = resultSet.getString("segment_name");

                Segment segment = new Segment(segment_id, segment_name);
                segmentList.add(segment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return segmentList;
    }

    public boolean createSegment(String segmentName) {
        String insertQuery = "INSERT INTO segment (segment_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, segmentName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Segment created successfully: " + segmentName);
                return true;
            } else {
                System.out.println("Failed to create segment: " + segmentName);
                return false;
                // Optionally, you can throw an exception or handle failure in a different way
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
            // Optionally, show an error message dialog to the user
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the segment: " + segmentName);
            return false;
        }
    }


    public int getSegmentIdByName(String segmentName) {
        String sqlQuery = "SELECT segment_id FROM segment WHERE segment_name = ?";
        int segmentId = -1; // Default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, segmentName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    segmentId = resultSet.getInt("segment_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }

        return segmentId;
    }
    public String getSegmentNameById(int segmentId) {
        String sqlQuery = "SELECT segment_name FROM segment WHERE segment_id = ?";
        String segmentName = null; // Default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, segmentId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    segmentName = resultSet.getString("segment_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }
        return segmentName;
    }

}
