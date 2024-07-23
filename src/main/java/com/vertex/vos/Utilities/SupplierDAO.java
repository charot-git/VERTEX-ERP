package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Supplier;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SupplierDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<String> getAllSupplierNames() {
        ObservableList<String> supplierNames = FXCollections.observableArrayList();
        String sqlQuery = "SELECT supplier_name FROM suppliers";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String supplierName = resultSet.getString("supplier_name");
                supplierNames.add(supplierName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return supplierNames;
    }

    DiscountDAO discountDAO = new DiscountDAO();

    public ObservableList<Supplier> getAllSuppliers() {
        ObservableList<Supplier> suppliersList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM suppliers";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnectionPool.getDataSource().getConnection();
            preparedStatement = connection.prepareStatement(sqlQuery);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Supplier supplier = extractSupplierFromResultSet(resultSet);
                suppliersList.add(supplier);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception properly in your application
        } finally {
            // Close the resources in reverse order of creation to avoid resource leaks
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return suppliersList;
    }

    private Supplier extractSupplierFromResultSet(ResultSet resultSet) throws SQLException {
        return new Supplier(
                resultSet.getInt("id"),
                resultSet.getString("supplier_name"),
                resultSet.getString("contact_person"),
                resultSet.getString("email_address"),
                resultSet.getString("phone_number"),
                resultSet.getString("address"),
                resultSet.getString("city"),
                resultSet.getString("brgy"),
                resultSet.getString("state_province"),
                resultSet.getString("postal_code"),
                resultSet.getString("country"),
                resultSet.getInt("discount_type"),
                resultSet.getString("supplier_type"),
                resultSet.getString("tin_number"),
                resultSet.getString("bank_details"),
                resultSet.getString("payment_terms"),
                resultSet.getString("delivery_terms"),
                discountDAO.getDiscountTypeById(resultSet.getInt("discount_type")),
                resultSet.getString("agreement_or_contract"),
                resultSet.getString("preferred_communication_method"),
                resultSet.getString("notes_or_comments"),
                resultSet.getDate("date_added"),
                resultSet.getString("supplier_image")
        );
    }

    public int getSupplierIdByName(String supplierName) {
        String sqlQuery = "SELECT id FROM suppliers WHERE supplier_name = ?";
        int supplierId = -1; // Set a default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, supplierName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    supplierId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return supplierId;
    }

    public String getSupplierNameById(int supplierId) {
        String sqlQuery = "SELECT supplier_name FROM suppliers WHERE id = ?";
        String supplierName = null; // Default value if not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, supplierId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    supplierName = resultSet.getString("supplier_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return supplierName;
    }

    public boolean updateSupplier(Supplier supplier) {
        String updateQuery = "UPDATE suppliers SET supplier_name=?, contact_person=?, email_address=?, phone_number=?, " +
                "address=?, city=?, brgy=?, state_province=?, postal_code=?, country=?, supplier_type=?, tin_number=?, " +
                "bank_details=?, payment_terms=?, delivery_terms=?, discount_type=?, agreement_or_contract=?, " +
                "preferred_communication_method=?, notes_or_comments=?, date_added=?, supplier_image=? WHERE id=?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            // Set values for the SQL query parameters
            preparedStatement.setString(1, supplier.getSupplierName());
            preparedStatement.setString(2, supplier.getContactPerson());
            preparedStatement.setString(3, supplier.getEmailAddress());
            preparedStatement.setString(4, supplier.getPhoneNumber());

            // Build address string
            String address = String.format("%s, %s, %s", supplier.getStateProvince(), supplier.getCity(), supplier.getBarangay());
            preparedStatement.setString(5, address);

            preparedStatement.setString(6, supplier.getCity());
            preparedStatement.setString(7, supplier.getBarangay());
            preparedStatement.setString(8, supplier.getStateProvince());
            preparedStatement.setString(9, supplier.getPostalCode());
            preparedStatement.setString(10, supplier.getCountry());
            preparedStatement.setString(11, supplier.getSupplierType());
            preparedStatement.setString(12, supplier.getTinNumber());
            preparedStatement.setString(13, supplier.getBankDetails());
            preparedStatement.setString(14, supplier.getPaymentTerms());
            preparedStatement.setString(15, supplier.getDeliveryTerms());
            preparedStatement.setInt(16, supplier.getDiscountType());
            preparedStatement.setString(17, supplier.getAgreementOrContract());
            preparedStatement.setString(18, supplier.getPreferredCommunicationMethod());
            preparedStatement.setString(19, supplier.getNotesOrComments());
            preparedStatement.setDate(20, supplier.getDateAdded());
            preparedStatement.setString(21, supplier.getSupplierImage());
            preparedStatement.setInt(22, supplier.getId());

            // Execute the query
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception or re-throw it
            return false;
        }
    }


    public boolean registerSupplier(Supplier supplier) {
        String insertQuery = "INSERT INTO suppliers (supplier_name, contact_person, email_address, phone_number, address, " +
                "city, brgy, state_province, postal_code, country, supplier_type, tin_number, bank_details, payment_terms, " +
                "delivery_terms, discount_type, agreement_or_contract, preferred_communication_method, notes_or_comments, " +
                "date_added, supplier_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            // Set values for the SQL query parameters
            preparedStatement.setString(1, supplier.getSupplierName());
            preparedStatement.setString(2, supplier.getContactPerson());
            preparedStatement.setString(3, supplier.getEmailAddress());
            preparedStatement.setString(4, supplier.getPhoneNumber());

            // Build address string
            String address = String.format("%s, %s, %s", supplier.getStateProvince(), supplier.getCity(), supplier.getBarangay());
            preparedStatement.setString(5, address);

            preparedStatement.setString(6, supplier.getCity());
            preparedStatement.setString(7, supplier.getBarangay());
            preparedStatement.setString(8, supplier.getStateProvince());
            preparedStatement.setString(9, supplier.getPostalCode());
            preparedStatement.setString(10, supplier.getCountry());
            preparedStatement.setString(11, supplier.getSupplierType());
            preparedStatement.setString(12, supplier.getTinNumber());
            preparedStatement.setString(13, supplier.getBankDetails());
            preparedStatement.setString(14, supplier.getPaymentTerms());
            preparedStatement.setString(15, supplier.getDeliveryTerms());
            preparedStatement.setInt(16, supplier.getDiscountType());
            preparedStatement.setString(17, supplier.getAgreementOrContract());
            preparedStatement.setString(18, supplier.getPreferredCommunicationMethod());
            preparedStatement.setString(19, supplier.getNotesOrComments());
            preparedStatement.setDate(20, supplier.getDateAdded());
            preparedStatement.setString(21, supplier.getSupplierImage());

            // Execute the query
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception or re-throw it
            return false;
        }
    }

    public Supplier getSupplierById(int supplierId) {
        String sqlQuery = "SELECT * FROM suppliers WHERE id = ?";
        Supplier supplier = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, supplierId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    supplier = new Supplier();
                    supplier.setId(resultSet.getInt("id"));
                    supplier.setSupplierName(resultSet.getString("supplier_name"));
                    supplier.setContactPerson(resultSet.getString("contact_person"));
                    supplier.setEmailAddress(resultSet.getString("email_address"));
                    supplier.setPhoneNumber(resultSet.getString("phone_number"));
                    supplier.setAddress(resultSet.getString("address"));
                    supplier.setCity(resultSet.getString("city"));
                    supplier.setBarangay(resultSet.getString("brgy"));
                    supplier.setStateProvince(resultSet.getString("state_province"));
                    supplier.setPostalCode(resultSet.getString("postal_code"));
                    supplier.setCountry(resultSet.getString("country"));
                    supplier.setDiscountType(resultSet.getInt("discount_type"));
                    supplier.setSupplierType(resultSet.getString("supplier_type"));
                    supplier.setTinNumber(resultSet.getString("tin_number"));
                    supplier.setBankDetails(resultSet.getString("bank_details"));
                    supplier.setPaymentTerms(resultSet.getString("payment_terms"));
                    supplier.setDeliveryTerms(resultSet.getString("delivery_terms"));
                    supplier.setAgreementOrContract(resultSet.getString("agreement_or_contract"));
                    supplier.setPreferredCommunicationMethod(resultSet.getString("preferred_communication_method"));
                    supplier.setNotesOrComments(resultSet.getString("notes_or_comments"));
                    supplier.setDateAdded(resultSet.getDate("date_added"));
                    supplier.setSupplierImage(resultSet.getString("supplier_image"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return supplier;
    }

    PurchaseOrderPaymentDAO purchaseOrderPaymentDAO = new PurchaseOrderPaymentDAO();

    public ObservableList<String> getAllSuppliersWithPayables() {
        String sqlQuery = "SELECT supplier_name FROM suppliers WHERE id IN (SELECT supplier_name FROM purchase_order WHERE payment_status IN (2, 3, 5, 6))";
        ObservableList<String> suppliersWithPayables = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String supplierName = resultSet.getString("supplier_name");
                    suppliersWithPayables.add(supplierName);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suppliersWithPayables;
    }
}
