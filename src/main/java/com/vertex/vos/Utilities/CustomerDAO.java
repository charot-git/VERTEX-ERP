package com.vertex.vos.Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vertex.vos.Constructors.Customer;
import com.zaxxer.hikari.HikariDataSource;

public class CustomerDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean createCustomer(Customer customer) {
        String query = "INSERT INTO customer (id, customer_code, customer_name, customer_image, store_name, store_signage, brgy, city, province, contact_number, customer_email, tel_number, customer_tin, payment_term, store_type, discount_id, encoder_id, date_entered, credit_type, company_code, isActive, isVAT, isEWT, otherDetails) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, customer.getCustomerId());
            statement.setString(2, customer.getCustomerCode());
            statement.setString(3, customer.getCustomerName());
            statement.setString(4, customer.getCustomerImage());
            statement.setString(5, customer.getStoreName());
            statement.setString(6, customer.getStoreSignage());
            statement.setString(7, customer.getBrgy());
            statement.setString(8, customer.getCity());
            statement.setString(9, customer.getProvince());
            statement.setString(10, customer.getContactNumber());
            statement.setString(11, customer.getCustomerEmail());
            statement.setString(12, customer.getTelNumber());
            statement.setString(13, customer.getCustomerTin());
            statement.setByte(14, customer.getPaymentTerm());
            statement.setInt(15, customer.getStoreType());
            statement.setInt(16, customer.getDiscountId());
            statement.setInt(17, customer.getEncoderId());
            statement.setTimestamp(18, customer.getDateEntered());
            statement.setByte(19, customer.getCreditType());
            statement.setByte(20, customer.getCompanyCode());
            statement.setBoolean(21, customer.isActive());
            statement.setBoolean(22, customer.isVAT());
            statement.setBoolean(23, customer.isEWT());
            statement.setString(24, customer.getOtherDetails());
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0; // If rows were inserted, return true
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an exception occurs
        }
    }

    public Customer getCustomer(String customerCode) {
        String query = "SELECT * FROM customer WHERE customer_code = ?";
        Customer customer = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, customerCode);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                customer = mapResultSetToCustomer(resultSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customer;
    }

    public List<Customer> getAllCustomers() {
        String query = "SELECT * FROM customer";
        List<Customer> customers = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Customer customer = mapResultSetToCustomer(resultSet);
                customers.add(customer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    private Customer mapResultSetToCustomer(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerCode(resultSet.getString("customer_code"));
        customer.setCustomerName(resultSet.getString("customer_name"));
        customer.setCustomerImage(resultSet.getString("customer_image"));
        customer.setStoreName(resultSet.getString("store_name"));
        customer.setStoreSignage(resultSet.getString("store_signage"));
        customer.setBrgy(resultSet.getString("brgy"));
        customer.setCity(resultSet.getString("city"));
        customer.setProvince(resultSet.getString("province"));
        customer.setContactNumber(resultSet.getString("contact_number"));
        customer.setCustomerEmail(resultSet.getString("customer_email"));
        customer.setTelNumber(resultSet.getString("tel_number"));
        customer.setCustomerTin(resultSet.getString("customer_tin"));
        customer.setPaymentTerm(resultSet.getByte("payment_term"));
        customer.setStoreType(resultSet.getInt("store_type"));
        customer.setDiscountId(resultSet.getInt("discount_id"));
        customer.setEncoderId(resultSet.getInt("encoder_id"));
        customer.setDateEntered(resultSet.getTimestamp("date_entered"));
        customer.setCreditType(resultSet.getByte("credit_type"));
        customer.setCompanyCode(resultSet.getByte("company_code"));
        customer.setActive(resultSet.getBoolean("isActive"));
        customer.setVAT(resultSet.getBoolean("isVAT"));
        customer.setEWT(resultSet.getBoolean("isEWT"));
        customer.setOtherDetails(resultSet.getString("otherDetails"));
        return customer;
    }

    public int getNextCustomerID() {
        int nextId = 0;
        String updateQuery = "UPDATE customer_id SET id = LAST_INSERT_ID(id + 1)";
        String selectQuery = "SELECT LAST_INSERT_ID()";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {

            // Update the po_no by incrementing it by 1
            updateStatement.executeUpdate();

            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                if (resultSet.next()) {
                    nextId = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return nextId;
    }
}
