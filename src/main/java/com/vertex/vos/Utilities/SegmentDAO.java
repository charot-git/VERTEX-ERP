package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Segment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SegmentDAO {
    private static final Map<Integer, Segment> segmentCache = new HashMap<>();
    private static boolean isCacheLoaded = false;

    public SegmentDAO() {
        if (!isCacheLoaded) {
            loadSegmentCache();
        }
    }

    private void loadSegmentCache() {
        segmentCache.clear();
        String sqlQuery = "SELECT segment_id, segment_name FROM segment";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int segmentId = resultSet.getInt("segment_id");
                String segmentName = resultSet.getString("segment_name");

                segmentCache.put(segmentId, new Segment(segmentId, segmentName));
            }
            isCacheLoaded = true;
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }
    }

    public ObservableList<Segment> getSegmentDetails() {
        return FXCollections.observableArrayList(segmentCache.values());
    }

    public boolean createSegment(String segmentName) {
        String insertQuery = "INSERT INTO segment (segment_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, segmentName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int segmentId = generatedKeys.getInt(1);
                        Segment newSegment = new Segment(segmentId, segmentName);
                        segmentCache.put(segmentId, newSegment); // Update cache
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the segment: " + segmentName);
        }
        return false;
    }

    public int getSegmentIdByName(String segmentName) {
        for (Segment segment : segmentCache.values()) {
            if (segment.getSegment_name().equalsIgnoreCase(segmentName)) {
                return segment.getSegment_id();
            }
        }
        return -1; // Not found
    }

    public String getSegmentNameById(int segmentId) {
        Segment segment = segmentCache.get(segmentId);
        return (segment != null) ? segment.getSegment_name() : null;
    }

    public ObservableList<String> getSegmentNames() {
        ObservableList<String> segmentNames = FXCollections.observableArrayList();
        for (Segment segment : segmentCache.values()) {
            segmentNames.add(segment.getSegment_name());
        }
        return segmentNames;
    }

    public void refreshCache() {
        loadSegmentCache();
    }
}
