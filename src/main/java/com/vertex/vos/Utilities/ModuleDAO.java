package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.vertex.vos.Objects.Module;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ModuleDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Create a new module
    public boolean createModule(int taskbarId, String moduleCode, String moduleLabel) {
        String sql = "INSERT INTO vos_modules (taskbar_id, module_code, module_label) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskbarId);
            stmt.setString(2, moduleCode);
            stmt.setString(3, moduleLabel);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Read a module by ID
    public Module getModuleById(int moduleId) {
        String sql = "SELECT * FROM vos_modules WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moduleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Module(
                        rs.getInt("id"),
                        rs.getInt("taskbar_id"),
                        rs.getString("module_code"),
                        rs.getString("module_label")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update a module
    public boolean updateModule(int moduleId, int taskbarId, String moduleCode, String moduleLabel) {
        String sql = "UPDATE vos_modules SET taskbar_id = ?, module_code = ?, module_label = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskbarId);
            stmt.setString(2, moduleCode);
            stmt.setString(3, moduleLabel);
            stmt.setInt(4, moduleId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a module
    public boolean deleteModule(int moduleId) {
        String sql = "DELETE FROM vos_modules WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moduleId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all modules
    public ObservableList<Module> getAllModules() {
        ObservableList<Module> modules = FXCollections.observableArrayList();
        String sql = "SELECT * FROM vos_modules";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                modules.add(new Module(
                        rs.getInt("id"),
                        rs.getInt("taskbar_id"),
                        rs.getString("module_code"),
                        rs.getString("module_label")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    public List<Integer> getModulesForUser(int userId) {
        List<Integer> modules = new ArrayList<>();
        String sql = "SELECT module_id FROM vos_user_to_module WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                modules.add(rs.getInt("module_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    public Module getModuleByLabel(String selectedModuleLabel) {
        String sql = "SELECT * FROM vos_modules WHERE module_label = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selectedModuleLabel);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Module(
                        rs.getInt("id"),
                        rs.getInt("taskbar_id"),
                        rs.getString("module_code"),
                        rs.getString("module_label")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeModuleFromUser(int userId, int id) {
        String sql = "DELETE FROM vos_user_to_module WHERE user_id = ? AND module_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void assignModuleToUser(int userId, int id) {
        String sql = "INSERT INTO vos_user_to_module (user_id, module_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
