package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.VersionControl;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VersionControlDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public VersionControl getActiveVersion() {
        VersionControl activeVersion = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM app_version WHERE isActive = ?")) {
            statement.setBoolean(1, true);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String versionName = resultSet.getString("version_name");
                    boolean active = resultSet.getBoolean("isActive");
                    activeVersion = new VersionControl(id, versionName, active);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activeVersion;
    }

    public VersionControl getVersionById(int versionId) {
        VersionControl version = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM app_version WHERE id = ?")) {
            statement.setInt(1, versionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String versionName = resultSet.getString("version_name");
                    boolean active = resultSet.getBoolean("isActive");
                    version = new VersionControl(id, versionName, active);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return version;
    }
}
