package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Section;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SectionsDAO {
    private static final Map<Integer, Section> sectionCache = new HashMap<>();
    private static boolean isCacheLoaded = false;

    public SectionsDAO() {
        if (!isCacheLoaded) {
            loadSectionCache();
        }
    }

    private void loadSectionCache() {
        sectionCache.clear();
        String sqlQuery = "SELECT section_id, section_name FROM sections";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int sectionId = resultSet.getInt("section_id");
                String sectionName = resultSet.getString("section_name");

                sectionCache.put(sectionId, new Section(sectionId, sectionName));
            }
            isCacheLoaded = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Section> getSectionDetails() {
        return FXCollections.observableArrayList(sectionCache.values());
    }

    public boolean addSection(String sectionName) {
        String insertQuery = "INSERT INTO sections (section_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, sectionName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int sectionId = generatedKeys.getInt(1);
                        Section newSection = new Section(sectionId, sectionName);
                        sectionCache.put(sectionId, newSection); // Update cache
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the section: " + sectionName);
        }
        return false;
    }

    public int getSectionIdByName(String sectionName) {
        for (Section section : sectionCache.values()) {
            if (section.getSection_name().equalsIgnoreCase(sectionName)) {
                return section.getSection_id();
            }
        }
        return -1; // Not found
    }

    public String getSectionNameById(int sectionId) {
        Section section = sectionCache.get(sectionId);
        return (section != null) ? section.getSection_name() : null;
    }

    public ObservableList<String> getSectionNames() {
        ObservableList<String> sectionNames = FXCollections.observableArrayList();
        for (Section section : sectionCache.values()) {
            sectionNames.add(section.getSection_name());
        }
        return sectionNames;
    }

    public void refreshCache() {
        loadSectionCache();
    }
}
