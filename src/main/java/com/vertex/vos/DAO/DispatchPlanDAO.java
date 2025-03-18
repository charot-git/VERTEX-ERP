package com.vertex.vos.DAO;

import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.SalesOrderDAO;
import com.vertex.vos.Utilities.VehicleDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DispatchPlanDAO {
    HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    ClusterDAO clusterDAO = new ClusterDAO();
    VehicleDAO vehicleDAO = new VehicleDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();

    public List<DispatchPlan> getAllDispatchPlans(int offset, int limit, String dispatchNo, Cluster selectedCluster, Vehicle selectedVehicle, DispatchStatus selectedStatus, Timestamp startDate, Timestamp endDate) {
        List<DispatchPlan> dispatchPlans = new ArrayList<>();
        String query = "SELECT * FROM dispatch_plan WHERE 1=1";

        if (dispatchNo != null && !dispatchNo.isEmpty()) {
            query += " AND dispatch_no LIKE ?";
        }
        if (selectedCluster != null) {
            query += " AND cluster_id = ?";
        }
        if (selectedVehicle != null) {
            query += " AND vehicle_id = ?";
        }
        if (selectedStatus != null) {
            query += " AND status = ?";
        }
        if (startDate != null && endDate != null) {
            query += " AND dispatch_date BETWEEN ? AND ?";
        }

        query += " ORDER BY dispatch_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (dispatchNo != null && !dispatchNo.isEmpty()) {
                stmt.setString(paramIndex++, "%" + dispatchNo + "%");
            }
            if (selectedCluster != null) {
                stmt.setInt(paramIndex++, selectedCluster.getId());
            }
            if (selectedVehicle != null) {
                stmt.setInt(paramIndex++, selectedVehicle.getVehicleId());
            }
            if (selectedStatus != null) {
                stmt.setString(paramIndex++, selectedStatus.toString());
            }
            if (startDate != null && endDate != null) {
                stmt.setTimestamp(paramIndex++, startDate);
                stmt.setTimestamp(paramIndex++, endDate);
            }
            stmt.setInt(paramIndex++, limit);
            stmt.setInt(paramIndex++, offset);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DispatchPlan dispatchPlan = new DispatchPlan();
                dispatchPlan.setDispatchId(rs.getInt("dispatch_id"));
                dispatchPlan.setDispatchNo(rs.getString("dispatch_no"));
                dispatchPlan.setDispatchDate(rs.getTimestamp("dispatch_date"));
                dispatchPlan.setTotalAmount(rs.getDouble("total_amount"));
                dispatchPlan.setStatus(DispatchStatus.fromString(rs.getString("status")));
                dispatchPlan.setCreatedAt(rs.getTimestamp("created_at"));
                dispatchPlan.setCluster(clusterDAO.getClusterById(rs.getInt("cluster_id")));
                dispatchPlan.setDriver(employeeDAO.getUserById(rs.getInt("driver_id")));
                dispatchPlan.setCreatedBy(employeeDAO.getUserById(rs.getInt("created_by")));

                dispatchPlans.add(dispatchPlan);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dispatchPlans;
    }

    public String generateDispatchNo() {
        String selectQuery = "SELECT dispatch_no FROM trip_no FOR UPDATE";
        String updateQuery = "UPDATE trip_no SET dispatch_no = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            connection.setAutoCommit(false);

            try (ResultSet resultSet = selectStmt.executeQuery()) {
                if (resultSet.next()) {
                    int no = resultSet.getInt("dispatch_no");
                    int nextNo = no + 1;

                    updateStmt.setInt(1, nextNo);
                    updateStmt.executeUpdate();
                    connection.commit();

                    return String.format("DP-%05d", nextNo);
                }
            }
            connection.rollback();
        } catch (SQLException e) {
            if ("40001".equals(e.getSQLState())) { // Deadlock detected, retry
                return generateDispatchNo();
            }
            throw new RuntimeException("Failed to generate new dispatch number", e);
        }
        return null;
    }

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    public ObservableList<SalesOrder> getSalesOrdersForDispatchPlan(int dispatchId) {
        ObservableList<SalesOrder> salesOrders = FXCollections.observableArrayList();
        String query = "SELECT sales_order_id FROM dispatch_plan_details WHERE dispatch_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, dispatchId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int salesOrderId = rs.getInt("sales_order_id");
                SalesOrder salesOrder = salesOrderDAO.getSalesOrderById(salesOrderId); // Fetch full sales order
                if (salesOrder != null) {
                    salesOrders.add(salesOrder);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging instead
        }

        return salesOrders;
    }


    public ObservableList<SalesOrder> getAvailableOrders(User driver, Cluster cluster, Timestamp dispatchDate) {
        ObservableList<SalesOrder> salesOrders = FXCollections.observableArrayList();
        ObservableList<AreaPerCluster> areaPerClusters = FXCollections.observableArrayList(clusterDAO.getAreasByClusterId(cluster.getId()));

        if (areaPerClusters.isEmpty()) {
            return salesOrders; // No areas in this cluster, return empty list
        }

        Set<String> areaBrgys = areaPerClusters.stream().map(AreaPerCluster::getBaranggay).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> areaCities = areaPerClusters.stream().map(AreaPerCluster::getCity).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> areaProvinces = areaPerClusters.stream().map(AreaPerCluster::getProvince).filter(Objects::nonNull).collect(Collectors.toSet());

        if (areaCities.isEmpty() || areaProvinces.isEmpty()) {
            return salesOrders;
        }

        String customerQuery = """
                    SELECT DISTINCT customer_code FROM customer 
                    WHERE city IN (%s) 
                    AND province IN (%s) 
                    %s
                """.formatted(
                areaCities.stream().map(c -> "?").collect(Collectors.joining(",")),
                areaProvinces.stream().map(p -> "?").collect(Collectors.joining(",")),
                areaBrgys.isEmpty() ? "" : "OR brgy IN (%s)".formatted(areaBrgys.stream().map(b -> "?").collect(Collectors.joining(",")))
        );

        List<String> customerCodes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(customerQuery)) {

            int index = 1;
            for (String city : areaCities) stmt.setString(index++, city);
            for (String province : areaProvinces) stmt.setString(index++, province);
            for (String brgy : areaBrgys) stmt.setString(index++, brgy);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customerCodes.add(rs.getString("customer_code"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return salesOrders;
        }

        if (customerCodes.isEmpty()) {
            return salesOrders; // No customers found
        }

        // Batch processing for IN clause
        String salesOrderQuery = """
                    SELECT so.order_id 
                    FROM sales_order so
                    WHERE so.customer_code IN (%s)
                    AND so.order_status = 'Approved'
                    AND NOT EXISTS (
                        SELECT 1 
                        FROM dispatch_plan_details dpd 
                        WHERE dpd.sales_order_id = so.order_id
                    )
                """.formatted(customerCodes.stream().map(c -> "?").collect(Collectors.joining(",")));

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(salesOrderQuery)) {

            int index = 1;
            for (String code : customerCodes) {
                stmt.setString(index++, code);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    salesOrders.add(salesOrderDAO.getSalesOrderById(rs.getInt("order_id")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesOrders;
    }

    public boolean saveDispatch(DispatchPlan dispatchPlan) {
        String insertDispatchQuery = """
                INSERT INTO dispatch_plan (dispatch_no, dispatch_date, total_amount, status, cluster_id, driver_id, created_by, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String insertDispatchDetailsQuery = """
                INSERT INTO dispatch_plan_details (dispatch_id, sales_order_id)
                VALUES (?, ?)
                """;

        String updateSalesOrderStatusQuery = """
                UPDATE sales_order SET order_status = 'For Consolidation' WHERE order_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement dispatchStmt = conn.prepareStatement(insertDispatchQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement detailsStmt = conn.prepareStatement(insertDispatchDetailsQuery);
             PreparedStatement updateSalesOrderStmt = conn.prepareStatement(updateSalesOrderStatusQuery)) {

            conn.setAutoCommit(false);

            // Insert DispatchPlan
            dispatchStmt.setString(1, dispatchPlan.getDispatchNo());
            dispatchStmt.setTimestamp(2, dispatchPlan.getDispatchDate());
            dispatchStmt.setDouble(3, dispatchPlan.getTotalAmount());
            dispatchStmt.setString(4, dispatchPlan.getStatus().name());
            dispatchStmt.setInt(5, dispatchPlan.getCluster().getId());
            dispatchStmt.setInt(6, dispatchPlan.getDriver().getUser_id());
            dispatchStmt.setInt(7, dispatchPlan.getCreatedBy().getUser_id());
            dispatchStmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));

            int affectedRows = dispatchStmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            // Get generated dispatch ID
            int dispatchId;
            try (ResultSet generatedKeys = dispatchStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    dispatchId = generatedKeys.getInt(1);
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // Insert related Sales Orders into DispatchPlanDetails
            for (SalesOrder salesOrder : dispatchPlan.getSalesOrders()) {
                detailsStmt.setInt(1, dispatchId);
                detailsStmt.setInt(2, salesOrder.getOrderId());
                detailsStmt.addBatch();

                // Update sales order status to "Allocated"
                updateSalesOrderStmt.setInt(1, salesOrder.getOrderId());
                updateSalesOrderStmt.addBatch();
            }

            detailsStmt.executeBatch();
            updateSalesOrderStmt.executeBatch();

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public ObservableList<DispatchPlan> getAllDispatchPlansForConsolidation() {
        ObservableList<DispatchPlan> dispatchPlans = FXCollections.observableArrayList();
        String query = "SELECT * FROM dispatch_plan WHERE status = 'Pending'";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DispatchPlan dispatchPlan = new DispatchPlan();
                dispatchPlan.setDispatchId(rs.getInt("dispatch_id"));
                dispatchPlan.setDispatchNo(rs.getString("dispatch_no"));
                dispatchPlan.setDispatchDate(rs.getTimestamp("dispatch_date"));
                dispatchPlan.setTotalAmount(rs.getDouble("total_amount"));
                dispatchPlan.setStatus(DispatchStatus.fromString(rs.getString("status")));
                dispatchPlan.setCreatedAt(rs.getTimestamp("created_at"));
                dispatchPlan.setCluster(clusterDAO.getClusterById(rs.getInt("cluster_id")));
                dispatchPlan.setDriver(employeeDAO.getUserById(rs.getInt("driver_id")));
                dispatchPlan.setCreatedBy(employeeDAO.getUserById(rs.getInt("created_by")));

                dispatchPlans.add(dispatchPlan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dispatchPlans;
    }

    public DispatchPlan getDispatchPlanDetails(String dispatchNo) {
            String query = "SELECT * FROM dispatch_plan WHERE dispatch_no = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, dispatchNo);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    DispatchPlan dispatchPlan = new DispatchPlan();
                    dispatchPlan.setDispatchId(rs.getInt("dispatch_id"));
                    dispatchPlan.setDispatchNo(rs.getString("dispatch_no"));
                    dispatchPlan.setDispatchDate(rs.getTimestamp("dispatch_date"));
                    dispatchPlan.setTotalAmount(rs.getDouble("total_amount"));
                    dispatchPlan.setStatus(DispatchStatus.fromString(rs.getString("status")));
                    dispatchPlan.setCreatedAt(rs.getTimestamp("created_at"));
                    dispatchPlan.setCluster(clusterDAO.getClusterById(rs.getInt("cluster_id")));
                    dispatchPlan.setDriver(employeeDAO.getUserById(rs.getInt("driver_id")));
                    dispatchPlan.setCreatedBy(employeeDAO.getUserById(rs.getInt("created_by")));
                    return dispatchPlan;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    public boolean updateDispatch(DispatchPlan dispatchPlan) {
        String updateDispatchQuery = """
            UPDATE dispatch_plan 
            SET dispatch_no = ?, dispatch_date = ?, total_amount = ?, status = ?, 
                cluster_id = ?, driver_id = ?
            WHERE dispatch_id = ?
            """;

        String selectOldSalesOrdersQuery = """
            SELECT sales_order_id FROM dispatch_plan_details WHERE dispatch_id = ?
            """;

        String updateSalesOrderToApprovedQuery = """
            UPDATE sales_order SET order_status = 'Approved' WHERE order_id = ?
            """;

        String deleteOldDispatchDetailsQuery = """
            DELETE FROM dispatch_plan_details WHERE dispatch_id = ?
            """;

        String insertDispatchDetailsQuery = """
            INSERT INTO dispatch_plan_details (dispatch_id, sales_order_id)
            VALUES (?, ?)
            """;

        String updateSalesOrderToConsolidationQuery = """
            UPDATE sales_order SET order_status = 'For Consolidation' WHERE order_id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement updateDispatchStmt = conn.prepareStatement(updateDispatchQuery);
             PreparedStatement selectOldOrdersStmt = conn.prepareStatement(selectOldSalesOrdersQuery);
             PreparedStatement updateToApprovedStmt = conn.prepareStatement(updateSalesOrderToApprovedQuery);
             PreparedStatement deleteDetailsStmt = conn.prepareStatement(deleteOldDispatchDetailsQuery);
             PreparedStatement insertDetailsStmt = conn.prepareStatement(insertDispatchDetailsQuery);
             PreparedStatement updateToConsolidationStmt = conn.prepareStatement(updateSalesOrderToConsolidationQuery)) {

            conn.setAutoCommit(false);

            // Step 1: Update DispatchPlan details
            updateDispatchStmt.setString(1, dispatchPlan.getDispatchNo());
            updateDispatchStmt.setTimestamp(2, dispatchPlan.getDispatchDate());
            updateDispatchStmt.setDouble(3, dispatchPlan.getTotalAmount());
            updateDispatchStmt.setString(4, dispatchPlan.getStatus().name());
            updateDispatchStmt.setInt(5, dispatchPlan.getCluster().getId());
            updateDispatchStmt.setInt(6, dispatchPlan.getDriver().getUser_id());
            updateDispatchStmt.setInt(7, dispatchPlan.getDispatchId());

            int affectedRows = updateDispatchStmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            // Step 2: Get the old sales orders associated with this dispatch
            selectOldOrdersStmt.setInt(1, dispatchPlan.getDispatchId());
            try (ResultSet rs = selectOldOrdersStmt.executeQuery()) {
                while (rs.next()) {
                    int oldSalesOrderId = rs.getInt("sales_order_id");

                    // Revert status of previous sales orders to 'Approved'
                    updateToApprovedStmt.setInt(1, oldSalesOrderId);
                    updateToApprovedStmt.addBatch();
                }
                updateToApprovedStmt.executeBatch();
            }

            // Step 3: Delete old dispatch details
            deleteDetailsStmt.setInt(1, dispatchPlan.getDispatchId());
            deleteDetailsStmt.executeUpdate();

            // Step 4: Insert new dispatch details and update their status to 'For Consolidation'
            for (SalesOrder salesOrder : dispatchPlan.getSalesOrders()) {
                insertDetailsStmt.setInt(1, dispatchPlan.getDispatchId());
                insertDetailsStmt.setInt(2, salesOrder.getOrderId());
                insertDetailsStmt.addBatch();

                updateToConsolidationStmt.setInt(1, salesOrder.getOrderId());
                updateToConsolidationStmt.addBatch();
            }
            insertDetailsStmt.executeBatch();
            updateToConsolidationStmt.executeBatch();

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}