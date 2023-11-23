package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.UserSession;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class PurchaseOrderDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Method to insert purchase order into the database
    public void insertPurchaseOrder(String purchaseOrderNo, String receivingType, String supplierName, String branchName,
                                    String priceType, boolean isReceiptRequired, double vatAmount, double withholdingTaxAmount,
                                    double totalAmount, String transactionType)
            throws SQLException {

        purchaseOrderNo = "PO" + purchaseOrderNo;

        String sql = "INSERT INTO purchase_order (purchase_order_no, receiving_type, supplier_name, branch_name, price_type, \n" +
                "receipt_required, vat_amount, withholding_tax_amount, total_amount, transaction_type, date_encoded, encoder_id) \n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, purchaseOrderNo);
            preparedStatement.setString(2, receivingType);
            preparedStatement.setString(3, supplierName);
            preparedStatement.setString(4, branchName);
            preparedStatement.setString(5, priceType);
            preparedStatement.setBoolean(6, isReceiptRequired);

            if (isReceiptRequired){
                preparedStatement.setDouble(7, vatAmount);
                preparedStatement.setDouble(8, withholdingTaxAmount);
            }
            else {
                preparedStatement.setDouble(7, 0.00);
                preparedStatement.setDouble(8, 0.00);
            }
            preparedStatement.setDouble(9, totalAmount);
            preparedStatement.setString(10, transactionType);
            preparedStatement.setTimestamp(11, new Timestamp(new Date().getTime()));
            preparedStatement.setInt(12, UserSession.getInstance().getUserId());

            preparedStatement.executeUpdate();
        }
    }

}
