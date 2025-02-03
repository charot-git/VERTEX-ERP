package com.vertex.vos.DAO;

import com.vertex.vos.Objects.Collection;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.SalesmanDAO;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectionDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public int generateCollectionNumber() throws SQLException {
        String sqlSelect = "SELECT no FROM collection_no LIMIT 1 FOR UPDATE";
        String sqlUpdate = "UPDATE collection_no SET no = no + 1 LIMIT 1";
        int stockTransferNumber = 0;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            ResultSet resultSet = statement.executeQuery(sqlSelect);
            if (resultSet.next()) {
                stockTransferNumber = resultSet.getInt("no");
                statement.executeUpdate(sqlUpdate);
                connection.commit();
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("40001")) { // Deadlock retry
                return generateCollectionNumber();
            }
            throw e;
        }
        return stockTransferNumber;
    }

    public Collection getCollectionByDocNo(String docNo) throws SQLException {
        String query = "SELECT * FROM collection WHERE docNo = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, docNo);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToCollection(resultSet);
            }
        }
        return null;
    }

    public boolean insertCollection(Collection collection) throws SQLException {
        String query = "INSERT INTO collection (docNo, collection_date, date_encoded, salesman_id, collected_by, encoder_id, remarks, isPosted, isCancelled, totalAmount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, collection.getDocNo());
            statement.setTimestamp(2, collection.getCollectionDate());
            statement.setTimestamp(3, collection.getDateEncoded());
            statement.setInt(4, collection.getSalesmanId().getId());
            statement.setInt(5, collection.getCollectedBy().getUser_id());
            statement.setInt(6, collection.getEncoderId().getUser_id());
            statement.setString(7, collection.getRemarks());
            statement.setBoolean(8, collection.getIsPosted());
            statement.setBoolean(9, collection.getIsCancelled());
            statement.setDouble(10, collection.getTotalAmount());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateCollection(Collection collection) throws SQLException {
        String query = "UPDATE collection SET collection_date = ?, date_encoded = ?, salesman_id = ?, collected_by = ?, encoder_id = ?, remarks = ?, isPosted = ?, isCancelled = ?, totalAmount = ? WHERE docNo = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, collection.getCollectionDate());
            statement.setTimestamp(2, collection.getDateEncoded());
            statement.setInt(3, collection.getSalesmanId().getId());
            statement.setInt(4, collection.getCollectedBy().getUser_id());
            statement.setInt(5, collection.getEncoderId().getUser_id());
            statement.setString(6, collection.getRemarks());
            statement.setBoolean(7, collection.getIsPosted());
            statement.setBoolean(8, collection.getIsCancelled());
            statement.setDouble(9, collection.getTotalAmount());
            statement.setString(10, collection.getDocNo());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteCollection(String docNo) throws SQLException {
        String query = "DELETE FROM collection WHERE docNo = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, docNo);
            return statement.executeUpdate() > 0;
        }
    }

    public List<Collection> getAllCollections() throws SQLException {
        List<Collection> collections = new ArrayList<>();
        String query = "SELECT * FROM collection";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                collections.add(mapResultSetToCollection(resultSet));
            }
        }
        return collections;
    }

    SalesmanDAO salesmanDAO = new SalesmanDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();

    private Collection mapResultSetToCollection(ResultSet resultSet) throws SQLException {
        Collection collection = new Collection();
        collection.setId(resultSet.getInt("id"));
        collection.setDocNo(resultSet.getString("docNo"));
        collection.setCollectionDate(resultSet.getTimestamp("collection_date"));
        collection.setDateEncoded(resultSet.getTimestamp("date_encoded"));
        // Add logic to map related objects like Salesman, User, etc.

        collection.setSalesmanId(salesmanDAO.getSalesmanDetails(resultSet.getInt("salesman_id"))); // Assuming you have a method to fetch salesman by ID
        collection.setCollectedBy(employeeDAO.getUserById(resultSet.getInt("collected_by"))); // Assuming you have a method to fetch employee by ID
        collection.setEncoderId(employeeDAO.getUserById(resultSet.getInt("encoder_id")));
        collection.setRemarks(resultSet.getString("remarks"));
        collection.setIsPosted(resultSet.getBoolean("isPosted"));
        collection.setIsCancelled(resultSet.getBoolean("isCancelled"));
        collection.setTotalAmount(resultSet.getDouble("totalAmount"));
        return collection;
    }
}
