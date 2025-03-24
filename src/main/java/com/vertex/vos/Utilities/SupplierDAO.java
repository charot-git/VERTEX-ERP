package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Supplier;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<String> getAllSupplierNames() {
        ObservableList<String> supplierNames = FXCollections.observableArrayList();
        String sqlQuery = "SELECT supplier_name FROM suppliers WHERE isActive = 1";

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

    public ObservableList<Supplier> getAllActiveSuppliers() {
        ObservableList<Supplier> suppliersList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM suppliers WHERE isActive = 1"; // Assuming 'active' is a BOOLEAN or INT column
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
            closeResources(resultSet, preparedStatement, connection);
        }

        return suppliersList;
    }

    // Utility method to close resources
    private void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


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
                resultSet.getString("supplier_shortcut"),
                resultSet.getString("contact_person"),
                resultSet.getString("email_address"),
                resultSet.getString("phone_number"),
                resultSet.getString("address"),
                resultSet.getString("city"),
                resultSet.getString("brgy"),
                resultSet.getString("state_province"),
                resultSet.getString("postal_code"),
                resultSet.getString("country"),
                resultSet.getString("supplier_type"),
                resultSet.getString("tin_number"),
                resultSet.getString("bank_details"),
                resultSet.getString("payment_terms"),
                resultSet.getString("delivery_terms"),
                resultSet.getString("agreement_or_contract"),
                resultSet.getString("preferred_communication_method"),
                resultSet.getString("notes_or_comments"),
                resultSet.getDate("date_added"), // Changed to Date (not Timestamp)
                resultSet.getString("supplier_image"),
                resultSet.getBoolean("isActive")
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
        String updateQuery = "UPDATE suppliers SET supplier_name=?, supplier_shortcut=?, contact_person=?, email_address=?, phone_number=?, "
                + "address=?, city=?, brgy=?, state_province=?, postal_code=?, country=?, supplier_type=?, tin_number=?, "
                + "bank_details=?, payment_terms=?, delivery_terms=?, agreement_or_contract=?, "
                + "preferred_communication_method=?, notes_or_comments=?, date_added=?, supplier_image=?, isActive=? "
                + "WHERE id=?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            // Set values for the SQL query parameters
            preparedStatement.setString(1, supplier.getSupplierName());
            preparedStatement.setString(2, supplier.getSupplierShortcut());
            preparedStatement.setString(3, supplier.getContactPerson());
            preparedStatement.setString(4, supplier.getEmailAddress());
            preparedStatement.setString(5, supplier.getPhoneNumber());

            // Build address string
            String address = String.format("%s, %s, %s", supplier.getStateProvince(), supplier.getCity(), supplier.getBarangay());
            preparedStatement.setString(6, address);

            preparedStatement.setString(7, supplier.getCity());
            preparedStatement.setString(8, supplier.getBarangay());
            preparedStatement.setString(9, supplier.getStateProvince());
            preparedStatement.setString(10, supplier.getPostalCode());
            preparedStatement.setString(11, supplier.getCountry());
            preparedStatement.setString(12, supplier.getSupplierType());
            preparedStatement.setString(13, supplier.getTinNumber());
            preparedStatement.setString(14, supplier.getBankDetails());
            preparedStatement.setString(15, supplier.getPaymentTerms());
            preparedStatement.setString(16, supplier.getDeliveryTerms());
            preparedStatement.setString(17, supplier.getAgreementOrContract());
            preparedStatement.setString(18, supplier.getPreferredCommunicationMethod());
            preparedStatement.setString(19, supplier.getNotesOrComments());
            preparedStatement.setDate(20, supplier.getDateAdded());
            preparedStatement.setString(21, supplier.getSupplierImage());

            preparedStatement.setBoolean(22, supplier.getActive());
            preparedStatement.setInt(23, supplier.getId());

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
        String insertQuery = "INSERT INTO suppliers (supplier_name, contact_person, email_address, phone_number, supplier_shortcut, address, " +
                "city, brgy, state_province, postal_code, country, supplier_type, tin_number, bank_details, payment_terms, " +
                "delivery_terms, agreement_or_contract, preferred_communication_method, notes_or_comments, " +

                "date_added, supplier_image, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            // Set values for the SQL query parameters
            preparedStatement.setString(1, supplier.getSupplierName());
            preparedStatement.setString(2, supplier.getContactPerson());
            preparedStatement.setString(3, supplier.getEmailAddress());
            preparedStatement.setString(4, supplier.getPhoneNumber());
            preparedStatement.setString(5, supplier.getSupplierShortcut());

            // Build address string
            String address = String.format("%s, %s, %s", supplier.getStateProvince(), supplier.getCity(), supplier.getBarangay());
            preparedStatement.setString(6, address);

            preparedStatement.setString(7, supplier.getCity());
            preparedStatement.setString(8, supplier.getBarangay());
            preparedStatement.setString(9, supplier.getStateProvince());
            preparedStatement.setString(10, supplier.getPostalCode());
            preparedStatement.setString(11, supplier.getCountry());
            preparedStatement.setString(12, supplier.getSupplierType());
            preparedStatement.setString(13, supplier.getTinNumber());
            preparedStatement.setString(14, supplier.getBankDetails());
            preparedStatement.setString(15, supplier.getPaymentTerms());
            preparedStatement.setString(16, supplier.getDeliveryTerms());
            preparedStatement.setString(17, supplier.getAgreementOrContract());
            preparedStatement.setString(18, supplier.getPreferredCommunicationMethod());
            preparedStatement.setString(19, supplier.getNotesOrComments());
            preparedStatement.setDate(20, supplier.getDateAdded());
            preparedStatement.setString(21, supplier.getSupplierImage());
            preparedStatement.setBoolean(22, supplier.getActive());

            // Execute the query
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
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
                    supplier.setSupplierShortcut(resultSet.getString("supplier_shortcut"));
                    supplier.setPhoneNumber(resultSet.getString("phone_number"));
                    supplier.setAddress(resultSet.getString("address"));
                    supplier.setCity(resultSet.getString("city"));
                    supplier.setBarangay(resultSet.getString("brgy"));
                    supplier.setStateProvince(resultSet.getString("state_province"));
                    supplier.setPostalCode(resultSet.getString("postal_code"));
                    supplier.setCountry(resultSet.getString("country"));
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
                    supplier.setActive(resultSet.getBoolean("isActive"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return supplier;
    }


    public ObservableList<String> getAllSuppliersWithPayables() {
        String sqlQuery = "SELECT supplier_name FROM suppliers WHERE id IN (SELECT supplier_name FROM purchase_order WHERE payment_status IN (2, 4 ,3, 5, 6))";
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

    public ObservableList<String> getAllSupplierNamesWhereType(String type) {
        String sqlQuery = "SELECT supplier_name FROM suppliers WHERE isActive = 1 AND supplier_type = ?";
        ObservableList<String> supplierNames = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, type);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    supplierNames.add(resultSet.getString("supplier_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return supplierNames;
    }

    public Supplier getSupplierByName(String supplierName) {
        String sqlQuery = "SELECT * FROM suppliers WHERE supplier_name = ?";
        Supplier supplier = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, supplierName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    supplier = new Supplier();
                    supplier.setId(resultSet.getInt("id"));
                    supplier.setSupplierName(resultSet.getString("supplier_name"));
                    supplier.setContactPerson(resultSet.getString("contact_person"));
                    supplier.setEmailAddress(resultSet.getString("email_address"));
                    supplier.setPhoneNumber(resultSet.getString("phone_number"));
                    supplier.setSupplierShortcut(resultSet.getString("supplier_shortcut"));
                    supplier.setAddress(resultSet.getString("address"));
                    supplier.setCity(resultSet.getString("city"));
                    supplier.setBarangay(resultSet.getString("brgy"));
                    supplier.setStateProvince(resultSet.getString("state_province"));
                    supplier.setPostalCode(resultSet.getString("postal_code"));
                    supplier.setCountry(resultSet.getString("country"));
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
                    supplier.setActive(resultSet.getBoolean("isActive"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return supplier;
    }

    public String getProductSupplierNames(int productId) {
        String supplierQuery = "SELECT s.supplier_name FROM suppliers s " +
                "INNER JOIN product_per_supplier pps ON s.id = pps.supplier_id " +
                "WHERE pps.product_id = ? AND s.nonBuy = 0 ";

        String parentQuery = "SELECT parent_id FROM products WHERE product_id = ? AND parent_id IS NOT NULL";
        List<String> supplierNames = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            // Check if the product is a child
            try (PreparedStatement parentStmt = connection.prepareStatement(parentQuery)) {
                parentStmt.setInt(1, productId);
                try (ResultSet parentRs = parentStmt.executeQuery()) {
                    if (parentRs.next()) {
                        productId = parentRs.getInt("parent_id"); // Use parent ID instead
                    }
                }
            }

            // Retrieve suppliers for the (possibly modified) productId
            try (PreparedStatement supplierStmt = connection.prepareStatement(supplierQuery)) {
                supplierStmt.setInt(1, productId);

                try (ResultSet resultSet = supplierStmt.executeQuery()) {
                    while (resultSet.next()) {
                        supplierNames.add(resultSet.getString("supplier_name"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logger instead
        }

        return String.join(", ", supplierNames);
    }


}
