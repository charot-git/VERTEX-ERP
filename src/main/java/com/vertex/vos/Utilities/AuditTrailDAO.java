package com.vertex.vos.Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditTrailDAO {
    private static final String INSERT_AUDIT_TRAIL_ENTRY = "INSERT INTO audit_trail_table (timestamp, user_id, action, table_name, record_id, field_name, old_value, new_value) VALUES (?,?,?,?,?,?,?,?)";

    public void insertAuditTrailEntry(AuditTrailEntry entry) {
        try (Connection connection = AuditTrailDatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_AUDIT_TRAIL_ENTRY)) {
            preparedStatement.setTimestamp(1, entry.getTimestamp());
            preparedStatement.setInt(2, entry.getUserId());
            preparedStatement.setString(3, entry.getAction());
            preparedStatement.setString(4, entry.getTableName());
            preparedStatement.setInt(5, entry.getRecordId());
            preparedStatement.setString(6, entry.getFieldName());
            preparedStatement.setString(7, entry.getOldValue());
            preparedStatement.setString(8, entry.getNewValue());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }

}