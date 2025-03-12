package com.vertex.vos.DAO;

import com.vertex.vos.Enums.ConsolidationStatus;
import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsolidationDAO {
    HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<Consolidation> getAllConsolidations(int pageSize, int offset, String consolidationNo, User selectedChecker, Timestamp dateFrom, Timestamp dateTo, ConsolidationStatus status) {
        ObservableList<Consolidation> consolidations = FXCollections.observableArrayList();

        String query = "SELECT * FROM consolidator WHERE 1=1";

        List<Object> parameters = new ArrayList<>();

        if (consolidationNo != null && !consolidationNo.isEmpty()) {
            query += " AND consolidator_no LIKE ?";
            parameters.add("%" + consolidationNo + "%");
        }

        if (selectedChecker != null) {
            query += " AND checked_by = ?";
            parameters.add(selectedChecker.getUser_id());
        }

        if (dateFrom != null) {
            query += " AND created_at >= ?";
            parameters.add(dateFrom);
        }

        if (dateTo != null) {
            query += " AND created_at <= ?";
            parameters.add(dateTo);
        }

        if (status != null) {
            query += " AND status = ?";
            parameters.add(status.name());
        }

        // Pagination
        query += " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        parameters.add(pageSize);
        parameters.add(offset);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Consolidation consolidation = mapConsolidation(rs);
                consolidations.add(consolidation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return consolidations;
    }


    EmployeeDAO employeeDAO = new EmployeeDAO();

    private Consolidation mapConsolidation(ResultSet rs) {
        Consolidation consolidation = new Consolidation();
        try {
            consolidation.setId(rs.getInt("id"));
            consolidation.setConsolidationNo(rs.getString("consolidator_no"));
            consolidation.setStatus(ConsolidationStatus.valueOf(rs.getString("status")));
            consolidation.setCreatedBy(employeeDAO.getUserById(rs.getInt("created_by")));
            consolidation.setCheckedBy(employeeDAO.getUserById(rs.getInt("checked_by")));
            consolidation.setCreatedAt(rs.getTimestamp("created_at"));
            consolidation.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return consolidation;
    }

    public Consolidation getConsolidation(int id) {
        return null;
    }

    public Consolidation saveConsolidation(Consolidation consolidation) {
        return null;
    }

    public Consolidation updateConsolidation(Consolidation consolidation) {
        return null;
    }

    public Consolidation deleteConsolidation(Consolidation consolidation) {
        return null;
    }

    public String generateConsolidationNo() {
        String selectQuery = "SELECT consolidation_no FROM pick_list_no FOR UPDATE";
        String updateQuery = "UPDATE pick_list_no SET consolidation_no = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            connection.setAutoCommit(false);

            try (ResultSet resultSet = selectStmt.executeQuery()) {
                if (resultSet.next()) {
                    int no = resultSet.getInt("consolidation_no");
                    int nextNo = no + 1;

                    updateStmt.setInt(1, nextNo);
                    updateStmt.executeUpdate();
                    connection.commit();

                    return String.format("CO-%05d", nextNo);
                }
            }
            connection.rollback();
        } catch (SQLException e) {
            if ("40001".equals(e.getSQLState())) { // Deadlock detected, retry
                return generateConsolidationNo();
            }
            throw new RuntimeException("Failed to generate new dispatch number", e);
        }
        return null;
    }
}
