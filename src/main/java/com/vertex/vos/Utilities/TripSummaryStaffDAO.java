package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.TripSummaryStaff;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TripSummaryStaffDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public void saveTripSummaryStaff(TripSummaryStaff staff) throws SQLException {
        String sql = "INSERT INTO trip_summary_staff (trip_id, staff_name, role) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, staff.getTripId());
            preparedStatement.setString(2, staff.getStaffName());
            preparedStatement.setString(3, staff.getRole());

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int staffId = generatedKeys.getInt(1);
                staff.setStaffId(staffId);
            }
        }
    }

    public List<TripSummaryStaff> getStaffByTripId(int tripId) throws SQLException {
        List<TripSummaryStaff> staffList = new ArrayList<>();
        String sql = "SELECT * FROM trip_summary_staff WHERE trip_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, tripId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    TripSummaryStaff staff = mapResultSetToTripSummaryStaff(resultSet);
                    staffList.add(staff);
                }
            }
        }
        return staffList;
    }

    private TripSummaryStaff mapResultSetToTripSummaryStaff(ResultSet resultSet) throws SQLException {
        int staffId = resultSet.getInt("staff_id");
        int tripId = resultSet.getInt("trip_id");
        String staffName = resultSet.getString("staff_name");
        String roleStr = resultSet.getString("role");

        return new TripSummaryStaff(staffId, tripId, staffName, roleStr);
    }
}
