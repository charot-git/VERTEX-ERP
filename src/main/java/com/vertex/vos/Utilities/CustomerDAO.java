package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Customer;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean updateCustomer(Customer customer) {
        String query = "UPDATE customer SET customer_code = ?, customer_name = ?, customer_image = ?, store_name = ?, store_signage = ?, brgy = ?, city = ?, province = ?, contact_number = ?, customer_email = ?, tel_number = ?, customer_tin = ?, payment_term = ?, store_type = ?, encoder_id = ?, date_entered = ?, credit_type = ?, company_code = ?, isActive = ?, isVAT = ?, isEWT = ?, otherDetails = ?, price_type = ? WHERE id = ?";

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
            statement.setInt(15, customer.getEncoderId());
            statement.setTimestamp(16, customer.getDateEntered());
            statement.setByte(17, customer.getCreditType());
            statement.setByte(18, customer.getCompanyCode());
            statement.setBoolean(19, customer.isActive());
            statement.setBoolean(20, customer.isVAT());
            statement.setBoolean(21, customer.isEWT());
            statement.setString(22, customer.getOtherDetails());
            statement.setString(23, customer.getPriceType());
            statement.setInt(24, customer.getCustomerId());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0; // If rows were updated, return true
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an exception occurs
        }
    }

    public boolean createCustomer(Customer customer) {
        String query = "INSERT INTO customer (id, customer_code, customer_name, customer_image, store_name, store_signage, brgy, city, province, contact_number, customer_email, tel_number, customer_tin, payment_term, store_type, encoder_id, date_entered, credit_type, company_code, isActive, isVAT, isEWT, otherDetails, price_type) VALUES (?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Corrected parameter indices
            statement.setInt(1, customer.getCustomerId());                // id
            statement.setString(2, "MAIN-" + customer.getCustomerId());    // customer_code
            statement.setString(3, customer.getCustomerName());            // customer_name
            statement.setString(4, customer.getCustomerImage());           // customer_image
            statement.setString(5, customer.getStoreName());               // store_name
            statement.setString(6, customer.getStoreSignage());            // store_signage
            statement.setString(7, customer.getBrgy());                    // brgy
            statement.setString(8, customer.getCity());                    // city
            statement.setString(9, customer.getProvince());                // province
            statement.setString(10, customer.getContactNumber());          // contact_number
            statement.setString(11, customer.getCustomerEmail());          // customer_email
            statement.setString(12, customer.getTelNumber());              // tel_number
            statement.setString(13, customer.getCustomerTin());            // customer_tin
            statement.setByte(14, customer.getPaymentTerm());              // payment_term
            statement.setInt(15, customer.getStoreType());                 // store_type
            statement.setInt(16, customer.getEncoderId());                 // encoder_id
            statement.setTimestamp(17, customer.getDateEntered());         // date_entered
            statement.setByte(18, customer.getCreditType());               // credit_type
            statement.setByte(19, customer.getCompanyCode());              // company_code
            statement.setBoolean(20, customer.isActive());                 // isActive
            statement.setBoolean(21, customer.isVAT());                    // isVAT
            statement.setBoolean(22, customer.isEWT());                    // isEWT
            statement.setString(23, customer.getOtherDetails());           // otherDetails
            statement.setString(24, customer.getPriceType());              // price_type

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0; // If rows were inserted, return true
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an exception occurs
        }
    }


    public Customer getCustomer(int customerId) {
        String query = "SELECT * FROM customer WHERE id = ?";
        Customer customer = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                customer = mapResultSetToCustomer(resultSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customer;
    }

    public Customer getCustomerByStoreName(String storeName) {
        String query = "SELECT * FROM customer WHERE store_name = ?";
        Customer customer = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, storeName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                customer = mapResultSetToCustomer(resultSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customer;
    }


    public Customer getCustomerByCode(String customerCode) {
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

    public int getCustomerIdByStoreName(String storeName) {
        int customerId = 0;
        String query = "SELECT id FROM customer WHERE store_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, storeName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                customerId = resultSet.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customerId;
    }

    public String getCustomerCodeByStoreName(String storeName) {
        String customerCode = null;
        String query = "SELECT customer_code FROM customer WHERE store_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, storeName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                customerCode = resultSet.getString("customer_code");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customerCode;
    }

    public String getStoreNameById(int customerId) {
        String query = "SELECT store_name FROM customer WHERE id = ?";
        String storeName = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                storeName = resultSet.getString("store_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return storeName;
    }


    public ObservableList<Customer> getAllCustomers() {
        String query = "SELECT * FROM customer ORDER BY province";
        ObservableList<Customer> customers = FXCollections.observableArrayList();

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
        customer.setCustomerId(resultSet.getInt("id"));
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
        customer.setPriceType(resultSet.getString("price_type"));
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

    public ObservableList<String> getCustomerStoreNames() {
        ObservableList<String> storeNames = FXCollections.observableArrayList();
        String query = "SELECT store_name FROM customer WHERE isActive = 1 ORDER BY store_name ASC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                storeNames.add(resultSet.getString("store_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return storeNames;
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

    public String getCustomerStoreNameByCode(String targetId) {
        String storeName = "";
        String selectQuery = "SELECT store_name FROM customer WHERE customer_code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setString(1, targetId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    storeName = resultSet.getString("store_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return storeName;
    }

    public String getCustomerStoreNameById(int targetId) {
        String storeName = "";
        String selectQuery = "SELECT store_name FROM customer WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, targetId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    storeName = resultSet.getString("store_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return storeName;
    }

    public int getCustomerIdByCustomerCode(String customerCode) {
        int customerId = -1; // Initialize with a default value

        String selectQuery = "SELECT id FROM customer WHERE customer_code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setString(1, customerCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    customerId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return customerId;
    }

    public int getCustomerIdByCode(String customerName) {
        int customerId = -1; // Initialize with a default value

        String selectQuery = "SELECT id FROM customer WHERE customer_code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setString(1, customerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    customerId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return customerId;
    }

    public ObservableList<Customer> getAllActiveCustomers() {
        String query = "SELECT * FROM customer WHERE isActive = 1 ORDER BY province ";
        ObservableList<Customer> customers = FXCollections.observableArrayList();

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

    public List<String> getCustomerStoreNamesWithInvoices() {
        List<String> storeNames = new ArrayList<>();
        String query = "SELECT DISTINCT c.store_name FROM customer c " +
                "JOIN sales_invoice si ON c.customer_code = si.customer_code " +
                "ORDER BY c.store_name";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                storeNames.add(resultSet.getString("store_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storeNames;
    }

}
