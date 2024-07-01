package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Branch;
import com.vertex.vos.Objects.PurchaseOrder;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<PurchaseOrder> getAllPurchaseOrders() throws SQLException {
        List<PurchaseOrder> purchaseOrders = new ArrayList<>();
        String query = "SELECT * FROM purchase_order";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                PurchaseOrder purchaseOrder = mapResultSetToPurchaseOrder(resultSet);
                purchaseOrders.add(purchaseOrder);
            }
        }

        return purchaseOrders;
    }

    public List<PurchaseOrder> getPurchaserOrdersForPaymentBySupplier(int supplierId) throws SQLException {
        List<PurchaseOrder> purchaseOrders = new ArrayList<>();
        String query = "SELECT * FROM purchase_order WHERE supplier_name = ? AND payment_status = '2'";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the supplier ID parameter
            preparedStatement.setInt(1, supplierId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    PurchaseOrder purchaseOrder = mapResultSetToPurchaseOrder(resultSet);
                    purchaseOrders.add(purchaseOrder);
                }
            }
        }

        return purchaseOrders;
    }


    public boolean entryGeneralReceive(PurchaseOrder purchaseOrder) throws SQLException {
        String query = "INSERT INTO purchase_order (purchase_order_no, supplier_name, receiving_type, payment_type, " +
                "price_type, date_encoded, date_approved, date_received, encoder_id, approver_id, receiver_id, " +
                "transaction_type, inventory_status, date, time, datetime, receipt_required) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "purchase_order_no = VALUES(purchase_order_no), " +
                "supplier_name = VALUES(supplier_name), " +
                "receiving_type = VALUES(receiving_type), " +
                "payment_type = VALUES(payment_type), " +
                "price_type = VALUES(price_type), " +
                "date_encoded = VALUES(date_encoded), " +
                "date_approved = VALUES(date_approved), " +
                "date_received = VALUES(date_received), " +
                "encoder_id = VALUES(encoder_id), " +
                "approver_id = VALUES(approver_id), " +
                "receiver_id = VALUES(receiver_id), " +
                "transaction_type = VALUES(transaction_type), " +
                "inventory_status = VALUES(inventory_status), " +
                "date = VALUES(date), " +
                "time = VALUES(time), " +
                "datetime = VALUES(datetime), " +
                "receipt_required = VALUES(receipt_required)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit to start transaction

            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                setGeneralReceivePreparedStatementParameters(preparedStatement, purchaseOrder);

                int rowsAffected = preparedStatement.executeUpdate();

                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        purchaseOrder.setPurchaseOrderId(generatedKeys.getInt(1));
                    }
                }

                connection.commit(); // Commit transaction
                return rowsAffected > 0; // Return true if at least one row was affected
            } catch (SQLException e) {
                connection.rollback(); // Rollback transaction if an exception occurs
                throw e; // Re-throw the exception after rollback
            }
        }
    }

    public boolean entryPurchaseOrder(PurchaseOrder purchaseOrder) throws SQLException {
        String query = "INSERT INTO purchase_order (purchase_order_no, supplier_name, receiving_type, payment_type, " +
                "price_type, date_encoded, date, time, datetime, encoder_id, transaction_type, inventory_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            setPurchaseOrderPreparedStatementParameters(preparedStatement, purchaseOrder);

            int rowsAffected = preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    purchaseOrder.setPurchaseOrderId(generatedKeys.getInt(1));
                }
            }
            return rowsAffected > 0;
        }
    }

    public boolean approvePurchaseOrder(PurchaseOrder purchaseOrder, int approverId, boolean receiptRequired, double vatAmount, double withholdingTaxAmount, double totalAmount, double grossAmount, double discountedAmount, LocalDateTime dateApproved, LocalDate receivingDate) throws SQLException {
        String query = "UPDATE purchase_order SET approver_id = ?, receipt_required = ?, vat_amount = ?, withholding_tax_amount = ?, total_amount = ?, gross_amount = ?, discounted_amount = ?, date_approved = ?, inventory_status = ?, payment_status = ?, lead_time_receiving = ? WHERE purchase_order_no = ?";

        int approvedStatus = 3;
        int paymentStatus = 1;
        if (purchaseOrder.getPaymentType() == 2) {
            paymentStatus = 2;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            setApprovePurchaseOrderPreparedStatementParameters(preparedStatement, approverId, receiptRequired, vatAmount, withholdingTaxAmount, totalAmount, grossAmount, discountedAmount, dateApproved, receivingDate, approvedStatus, paymentStatus, purchaseOrder.getPurchaseOrderNo());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if rows were affected (update successful)
        }
    }

    public PurchaseOrder getPurchaseOrderByOrderNo(int orderNo) throws SQLException {
        PurchaseOrder purchaseOrder = null;
        String query = "SELECT * FROM purchase_order WHERE purchase_order_no = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, orderNo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    purchaseOrder = mapResultSetToPurchaseOrder(resultSet);
                }
            }
        }

        return purchaseOrder;
    }

    public List<Branch> getBranchesForPurchaseOrder(int purchaseOrderId) throws SQLException {
        BranchDAO branchDAO = new BranchDAO();
        List<Branch> branches = new ArrayList<>();
        String query = "SELECT DISTINCT branch_id FROM purchase_order_products WHERE purchase_order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, purchaseOrderId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int branchId = resultSet.getInt("branch_id");
                    Branch branch = branchDAO.getBranchById(branchId);
                    if (branch != null) {
                        branches.add(branch);
                    }
                }
            }
        }
        return branches;
    }

    public ObservableList<String> getBranchNamesForPurchaseOrder(int purchaseOrderId) throws SQLException {
        return getBranchNames(purchaseOrderId, "SELECT DISTINCT branch_id FROM purchase_order_products WHERE purchase_order_id = ?");
    }

    public ObservableList<String> getBranchNamesForPurchaseOrderGeneralReceive(int purchaseOrderId) throws SQLException {
        return getBranchNames(purchaseOrderId, "SELECT DISTINCT branch_id FROM purchase_order_receiving WHERE purchase_order_id = ?");
    }

    public ObservableList<String> getAllPOForReceivingFromPO() {
        return getAllPOForReceiving("SELECT purchase_order_no FROM purchase_order WHERE payment_type != '0' AND inventory_status = '3'");
    }

    public ObservableList<String> getAllPOForGeneralReceive() {
        return getAllPOForReceiving("SELECT purchase_order_no FROM purchase_order WHERE payment_type = '0' AND inventory_status = '3'");
    }

    public int getPurchaseOrderIDByPurchaseNO(int poNo) {
        String sql = "SELECT purchase_order_id FROM purchase_order WHERE purchase_order_no = ?";
        int poId = -1;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, poNo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    poId = resultSet.getInt("purchase_order_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
        return poId;
    }

    public boolean receivePurchaseOrder(PurchaseOrder purchaseOrder, boolean receiveStatus) {
        int purchaseOrderId = purchaseOrder.getPurchaseOrderId();
        try {
            if (receiveStatus) {
                updatePurchaseOrderInventoryStatus(purchaseOrderId, 9);
                return false;
            } else {
                updatePurchaseOrderInventoryStatus(purchaseOrderId, 6);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updatePurchaseOrderInventoryStatus(int purchaseOrderId, int status) throws SQLException {
        String query = "UPDATE purchase_order SET inventory_status = ? WHERE purchase_order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, status);
            preparedStatement.setInt(2, purchaseOrderId);
            preparedStatement.executeUpdate();
        }
    }

    public void updatePurchaseOrderReceiverAndDate(int purchaseOrderId, int receiverId, Timestamp dateReceived) throws SQLException {
        String query = "UPDATE purchase_order SET receiver_id = ?, date_received = ? WHERE purchase_order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, receiverId);
            preparedStatement.setTimestamp(2, dateReceived);
            preparedStatement.setInt(3, purchaseOrderId);
            preparedStatement.executeUpdate();
        }
    }


    public void updatePurchaseOrderPaymentStatus(int purchaseOrderId, int status) throws SQLException {
        String query = "UPDATE purchase_order SET payment_status = ? WHERE purchase_order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, status);
            preparedStatement.setInt(2, purchaseOrderId);
            preparedStatement.executeUpdate();
        }
    }

    private void setGeneralReceivePreparedStatementParameters(PreparedStatement preparedStatement, PurchaseOrder purchaseOrder) throws SQLException {
        preparedStatement.setInt(1, purchaseOrder.getPurchaseOrderNo());
        preparedStatement.setInt(2, purchaseOrder.getSupplierName());
        preparedStatement.setInt(3, purchaseOrder.getReceivingType());
        preparedStatement.setInt(4, purchaseOrder.getPaymentType());
        preparedStatement.setString(5, purchaseOrder.getPriceType());

        preparedStatement.setTimestamp(6, purchaseOrder.getDateEncoded() != null ? Timestamp.valueOf(purchaseOrder.getDateEncoded()) : Timestamp.valueOf(LocalDateTime.now()));
        preparedStatement.setTimestamp(7, purchaseOrder.getDateApproved() != null ? Timestamp.valueOf(purchaseOrder.getDateApproved()) : Timestamp.valueOf(LocalDateTime.now()));
        preparedStatement.setTimestamp(8, purchaseOrder.getDateReceived() != null ? Timestamp.valueOf(purchaseOrder.getDateReceived()) : Timestamp.valueOf(LocalDateTime.now()));

        preparedStatement.setInt(9, purchaseOrder.getEncoderId());
        preparedStatement.setInt(10, purchaseOrder.getApproverId());
        preparedStatement.setInt(11, purchaseOrder.getReceiverId());

        preparedStatement.setInt(12, purchaseOrder.getTransactionType());
        preparedStatement.setInt(13, purchaseOrder.getInventoryStatus());

        preparedStatement.setDate(14, purchaseOrder.getDate() != null ? Date.valueOf(purchaseOrder.getDate()) : Date.valueOf(LocalDate.now()));
        preparedStatement.setTime(15, purchaseOrder.getTime() != null ? Time.valueOf(purchaseOrder.getTime()) : Time.valueOf(LocalTime.now()));
        preparedStatement.setTimestamp(16, purchaseOrder.getDatetime() != null ? Timestamp.valueOf(purchaseOrder.getDatetime()) : Timestamp.valueOf(LocalDateTime.now()));
        preparedStatement.setBoolean(17, purchaseOrder.getReceiptRequired());
    }



    private void setPurchaseOrderPreparedStatementParameters(PreparedStatement preparedStatement, PurchaseOrder purchaseOrder) throws SQLException {
        preparedStatement.setInt(1, purchaseOrder.getPurchaseOrderNo());
        preparedStatement.setInt(2, purchaseOrder.getSupplierName());
        preparedStatement.setInt(3, purchaseOrder.getReceivingType());
        preparedStatement.setInt(4, purchaseOrder.getPaymentType());
        preparedStatement.setString(5, purchaseOrder.getPriceType());
        preparedStatement.setTimestamp(6, Timestamp.valueOf(purchaseOrder.getDateEncoded()));
        preparedStatement.setDate(7, Date.valueOf(purchaseOrder.getDate()));
        preparedStatement.setTime(8, Time.valueOf(purchaseOrder.getTime()));
        preparedStatement.setTimestamp(9, Timestamp.valueOf(purchaseOrder.getDatetime()));
        preparedStatement.setInt(10, purchaseOrder.getEncoderId());
        preparedStatement.setInt(11, purchaseOrder.getTransactionType());
        preparedStatement.setInt(12, purchaseOrder.getInventoryStatus());
    }

    private void setApprovePurchaseOrderPreparedStatementParameters(PreparedStatement preparedStatement, int approverId, boolean receiptRequired, double vatAmount, double withholdingTaxAmount, double totalAmount, double grossAmount, double discountedAmount, LocalDateTime dateApproved, LocalDate receivingDate, int approvedStatus, int paymentStatus, int purchaseOrderNo) throws SQLException {
        preparedStatement.setInt(1, approverId);
        preparedStatement.setBoolean(2, receiptRequired);
        preparedStatement.setDouble(3, vatAmount);
        preparedStatement.setDouble(4, withholdingTaxAmount);
        preparedStatement.setDouble(5, totalAmount);
        preparedStatement.setDouble(6, grossAmount);
        preparedStatement.setDouble(7, discountedAmount);
        preparedStatement.setTimestamp(8, Timestamp.valueOf(dateApproved));
        preparedStatement.setInt(9, approvedStatus);
        preparedStatement.setInt(10, paymentStatus);
        preparedStatement.setDate(11, Date.valueOf(receivingDate));
        preparedStatement.setInt(12, purchaseOrderNo);
    }

    private ObservableList<String> getBranchNames(int purchaseOrderId, String query) throws SQLException {
        BranchDAO branchDAO = new BranchDAO();
        ObservableList<String> branchNames = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, purchaseOrderId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int branchId = resultSet.getInt("branch_id");
                    Branch branch = branchDAO.getBranchById(branchId);
                    if (branch != null) {
                        branchNames.add(branch.getBranchName());
                    }
                }
            }
        }

        return branchNames;
    }

    private ObservableList<String> getAllPOForReceiving(String query) {
        ObservableList<String> poList = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String purchaseOrderNo = resultSet.getString("purchase_order_no");
                poList.add(purchaseOrderNo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return poList;
    }

    SupplierDAO supplierDAO = new SupplierDAO();
    TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();
    StatusDAO statusDAO = new StatusDAO();

    private PurchaseOrder mapResultSetToPurchaseOrder(ResultSet resultSet) throws SQLException {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPurchaseOrderId(resultSet.getInt("purchase_order_id"));
        purchaseOrder.setPurchaseOrderNo(resultSet.getInt("purchase_order_no"));
        purchaseOrder.setSupplierName(resultSet.getInt("supplier_name"));
        purchaseOrder.setReceivingType(resultSet.getInt("receiving_type"));
        purchaseOrder.setPaymentType(resultSet.getInt("payment_type"));
        purchaseOrder.setPriceType(resultSet.getString("price_type"));

        Timestamp dateEncoded = resultSet.getTimestamp("date_encoded");
        if (dateEncoded != null) {
            purchaseOrder.setDateEncoded(dateEncoded.toLocalDateTime());
        }

        Timestamp dateApproved = resultSet.getTimestamp("date_approved");
        if (dateApproved != null) {
            purchaseOrder.setDateApproved(dateApproved.toLocalDateTime());
        }

        Timestamp dateReceived = resultSet.getTimestamp("date_received");
        if (dateReceived != null) {
            purchaseOrder.setDateReceived(dateReceived.toLocalDateTime());
        }

        purchaseOrder.setEncoderId(resultSet.getInt("encoder_id"));
        purchaseOrder.setApproverId(resultSet.getInt("approver_id"));
        purchaseOrder.setReceiverId(resultSet.getInt("receiver_id"));
        purchaseOrder.setTransactionType(resultSet.getInt("transaction_type"));
        purchaseOrder.setInventoryStatus(resultSet.getInt("inventory_status"));
        purchaseOrder.setPaymentStatus(resultSet.getInt("payment_status"));

        //accounting
        purchaseOrder.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        Date leadTimePayment = resultSet.getDate("lead_time_payment");
        if (leadTimePayment != null) {
            purchaseOrder.setLeadTimePayment(leadTimePayment.toLocalDate());
        }


        Date date = resultSet.getDate("date");
        if (date != null) {
            purchaseOrder.setDate(date.toLocalDate());
        }

        Time time = resultSet.getTime("time");
        if (time != null) {
            purchaseOrder.setTime(time.toLocalTime());
        }

        Timestamp datetime = resultSet.getTimestamp("datetime");
        if (datetime != null) {
            purchaseOrder.setDatetime(datetime.toLocalDateTime());
        }

        purchaseOrder.setReceiptRequired(resultSet.getBoolean("receipt_required"));

        purchaseOrder.setSupplierNameString(supplierDAO.getSupplierNameById(purchaseOrder.getSupplierName()));
        purchaseOrder.setTransactionTypeString(transactionTypeDAO.getTransactionTypeById(purchaseOrder.getTransactionType()));
        purchaseOrder.setPaymentStatusString(statusDAO.getPaymentStatusById(purchaseOrder.getPaymentStatus()));
        purchaseOrder.setInventoryStatusString(statusDAO.getTransactionStatusById(purchaseOrder.getInventoryStatus()));

        return purchaseOrder;
    }

}
