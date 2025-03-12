package com.vertex.vos.DAO;

import com.vertex.vos.Enums.PickListStatus;
import com.vertex.vos.Objects.PickList;
import com.vertex.vos.Objects.PickListItem;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import java.sql.*;

public class PickListDAO {

    HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<PickList> getAllPickLists(int pageSize, int currentPage, String text, User selectedPicker, Timestamp dateTo, Timestamp dateFrom, PickListStatus status) {
        ObservableList<PickList> pickLists = FXCollections.observableArrayList();
        StringBuilder query = new StringBuilder("SELECT * FROM pick_list WHERE 1=1");

        if (StringUtils.isNotEmpty(text)) {
            query.append(" AND pick_no LIKE ?");
        }
        if (selectedPicker != null) {
            query.append(" AND picked_by = ?");
        }
        if (dateFrom != null && dateTo != null) {
            query.append(" AND pick_date BETWEEN ? AND ?");
        }
        if (status != null) {
            query.append(" AND status = ?");
        }

        query.append(" LIMIT ? OFFSET ?");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int index = 1;
            if (StringUtils.isNotEmpty(text)) {
                stmt.setString(index++, "%" + text + "%");
            }
            if (selectedPicker != null) {
                stmt.setInt(index++, selectedPicker.getUser_id());
            }
            if (dateFrom != null && dateTo != null) {
                stmt.setTimestamp(index++, dateFrom);
                stmt.setTimestamp(index++, dateTo);
            }
            if (status != null) {
                stmt.setString(index++, status.name());
            }
            stmt.setInt(index++, pageSize);
            stmt.setInt(index++, pageSize * currentPage);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PickList pickList = mapPickList(rs);
                pickLists.add(pickList);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider proper logging
        }
        return pickLists;
    }

    BranchDAO branchDAO = new BranchDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();

    private PickList mapPickList(ResultSet rs) throws SQLException {
        PickList pickList = new PickList();
        pickList.setId(rs.getInt("id"));
        pickList.setPickNo(rs.getString("pick_no"));
        pickList.setPickedBy(employeeDAO.getUserById(rs.getInt("picked_by")));
        pickList.setCreatedBy(employeeDAO.getUserById(rs.getInt("created_by")));
        pickList.setPickDate(rs.getTimestamp("pick_date"));
        pickList.setBranch(branchDAO.getBranchById(rs.getInt("branch_id")));
        pickList.setStatus(PickListStatus.valueOf(rs.getString("status")));
        pickList.setCreatedAt(rs.getTimestamp("created_at"));
        pickList.setUpdatedAt(rs.getTimestamp("updated_at"));
        pickList.setPrinted(rs.getBoolean("isPrinted"));
        return pickList;
    }

    public String generateNextPickListNo() {
        String pickListNo = null;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT pick_list_no FROM pick_list_no FOR UPDATE")) {
            if (rs.next()) {
                int currentNo = rs.getInt("pick_list_no");
                stmt.executeUpdate("UPDATE pick_list_no SET pick_list_no = pick_list_no + 1");
                pickListNo = "PL-" + String.format("%04d", currentNo + 1);
            } else {
                stmt.executeUpdate("INSERT INTO pick_list_no (no) VALUES (1)");
                pickListNo = "PL-0001";
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider proper logging
        }
        return pickListNo;
    }

    PickListItemDAO pickListItemDAO = new PickListItemDAO();

    public ObservableList<PickListItem> getPickListItemsForEncoding(PickList pickList) {
        return pickListItemDAO.getPickListItemsForEncoding(pickList.getBranch(), pickList.getPickedBy(), pickList.getPickDate(), pickList);
    }
}
