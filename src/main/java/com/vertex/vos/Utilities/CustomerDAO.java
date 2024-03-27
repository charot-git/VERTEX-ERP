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
            statement.setBoolean(20, customer.isActive());
            statement.setBoolean(21, customer.isVAT());
            statement.setBoolean(22, customer.isEWT());
            statement.setString(23, customer.getOtherDetails());
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
}
