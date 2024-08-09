package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Taskbar;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskbarDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Create a new taskbar
    public boolean createTaskbar(Taskbar taskbar) {
        String sql = "INSERT INTO vos_taskbar (taskbar_code, taskbar_label) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, taskbar.getTaskbarCode());
            stmt.setString(2, taskbar.getTaskbarLabel());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Read a taskbar by ID
    public Taskbar getTaskbarById(int id) {
        String sql = "SELECT * FROM vos_taskbar WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Taskbar(
                        rs.getInt("id"),
                        rs.getString("taskbar_code"),
                        rs.getString("taskbar_label")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Read all taskbars
    public ObservableList<Taskbar> getAllTaskbars() {
        ObservableList<Taskbar> taskbars = FXCollections.observableArrayList();
        String sql = "SELECT * FROM vos_taskbar";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Taskbar taskbar = new Taskbar(
                        rs.getInt("id"),
                        rs.getString("taskbar_code"),
                        rs.getString("taskbar_label")
                );
                taskbars.add(taskbar);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return taskbars;
    }

    // Update an existing taskbar
    public boolean updateTaskbar(Taskbar taskbar) {
        String sql = "UPDATE vos_taskbar SET taskbar_code = ?, taskbar_label = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, taskbar.getTaskbarCode());
            stmt.setString(2, taskbar.getTaskbarLabel());
            stmt.setInt(3, taskbar.getId());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a taskbar by ID
    public boolean deleteTaskbar(int id) {
        String sql = "DELETE FROM vos_taskbar WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Assign a taskbar to a user
    public boolean assignTaskbarToUser(int userId, int taskbarId) {
        String sql = "INSERT INTO vos_user_to_taskbar (user_id, taskbar_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, taskbarId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove a taskbar from a user
    public boolean removeTaskbarFromUser(int userId, int taskbarId) {
        String sql = "DELETE FROM vos_user_to_taskbar WHERE user_id = ? AND taskbar_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, taskbarId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all taskbars assigned to a user
    public List<Integer> getTaskbarsForUser(int userId) {
        List<Integer> taskbars = new ArrayList<>();
        String sql = "SELECT taskbar_id FROM vos_user_to_taskbar WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                taskbars.add(rs.getInt("taskbar_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return taskbars;
    }

    // Remove all taskbars assigned to a user
    public boolean removeAllTaskbarsFromUser(int userId) {
        String sql = "DELETE FROM vos_user_to_taskbar WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Taskbar getTaskbarByLabel(String selectedTaskbarLabel) {
        String sql = "SELECT * FROM vos_taskbar WHERE taskbar_label = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selectedTaskbarLabel);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Taskbar(
                        rs.getInt("id"),
                        rs.getString("taskbar_code"),
                        rs.getString("taskbar_label")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
