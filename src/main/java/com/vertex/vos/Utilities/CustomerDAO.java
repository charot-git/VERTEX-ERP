package com.vertex.vos.Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.vertex.vos.Constructors.Customer;
import com.zaxxer.hikari.HikariDataSource;

public class CustomerDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public void createCustomer(Customer customer) {
        String query = "INSERT INTO customer (customer_code, customer_name, customer_image, store_name, store_signage, brgy, city, province, contact_number, customer_email, tel_number, customer_tin, payment_term, store_type, discount_id, encoder_id, date_entered, credit_type, company_code, isActive, isVAT, isEWT, otherDetails) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, customer.getCustomerCode());
            statement.setString(2, customer.getCustomerName());
            statement.setString(3, customer.getCustomerImage());
            statement.setString(4, customer.getStoreName());
            statement.setString(5, customer.getStoreSignage());
            statement.setString(6, customer.getBrgy());
            statement.setString(7, customer.getCity());
            statement.setString(8, customer.getProvince());
            statement.setString(9, customer.getContactNumber());
            statement.setString(10, customer.getCustomerEmail());
            statement.setString(11, customer.getTelNumber());
            statement.setString(12, customer.getCustomerTin());
            statement.setByte(13, customer.getPaymentTerm());
            statement.setInt(14, customer.getStoreType());
            statement.setInt(15, customer.getDiscountId());
            statement.setInt(16, customer.getEncoderId());
            statement.setTimestamp(17, customer.getDateEntered());
            statement.setByte(18, customer.getCreditType());
            statement.setByte(19, customer.getCompanyCode());
            statement.setByte(20, customer.getIsActive());
            statement.setByte(21, customer.getIsVAT());
            statement.setByte(22, customer.getIsEWT());
            statement.setString(23, customer.getOtherDetails());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception as needed
        }
    }
}
