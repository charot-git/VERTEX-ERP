package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public Collection getCollectionByDocNo(String docNo) {
        String query = "SELECT * FROM collection WHERE docNo = ?";
        Collection collection = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, docNo);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                collection = mapResultSetToCollection(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return collection;
    }

    private static final Logger LOGGER = Logger.getLogger(CollectionDAO.class.getName());

    public boolean insertCollection(Collection collection) throws SQLException {
        String collectionQuery = "INSERT INTO collection (docNo, collection_date, date_encoded, salesman_id, collected_by, encoder_id, remarks, isPosted, isCancelled, totalAmount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE collection_date = VALUES(collection_date), salesman_id = VALUES(salesman_id), collected_by = VALUES(collected_by), "
                + "encoder_id = VALUES(encoder_id), remarks = VALUES(remarks), isPosted = VALUES(isPosted), isCancelled = VALUES(isCancelled), totalAmount = VALUES(totalAmount)";

        String invoiceQuery = "INSERT INTO collection_invoices (collection_id, invoice_id) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE invoice_id = VALUES(invoice_id)";

        String memoQuery = "INSERT INTO collection_memos (collection_id, memo_id) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE memo_id = VALUES(memo_id)";

        String returnQuery = "INSERT INTO collection_returns (collection_id, return_id) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE return_id = VALUES(return_id)";

        String detailsQuery = "INSERT INTO collection_details (collection_id, type, bank, encoder_id, check_no, chequeDate, amount, remarks, balance_type_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE type = VALUES(type), bank = VALUES(bank), encoder_id = VALUES(encoder_id), check_no = VALUES(check_no), "
                + "chequeDate = VALUES(chequeDate), amount = VALUES(amount), remarks = VALUES(remarks), balance_type_id = VALUES(balance_type_id)";

        String denominationQuery = "INSERT INTO collection_details_denomination (collection_detail_id, denomination_id, quantity) "
                + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false); // Start transaction
            LOGGER.info("Transaction started for inserting collection.");

            int collectionId = -1;

            // Insert into collection table
            try (PreparedStatement statement = connection.prepareStatement(collectionQuery, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, collection.getDocNo());
                statement.setTimestamp(2, collection.getCollectionDate());
                statement.setTimestamp(3, collection.getDateEncoded());
                statement.setInt(4, collection.getSalesman().getId());
                statement.setInt(5, collection.getCollectedBy().getUser_id());
                statement.setInt(6, collection.getEncoderId().getUser_id());
                statement.setString(7, collection.getRemarks());
                statement.setBoolean(8, collection.getIsPosted());
                statement.setBoolean(9, collection.getIsCancelled());
                statement.setDouble(10, collection.getTotalAmount());

                statement.executeUpdate();
                LOGGER.info("Collection inserted/updated successfully.");

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        collectionId = generatedKeys.getInt(1);
                        LOGGER.info("Generated collection ID: " + collectionId);
                    }
                }
            }

            if (collectionId == -1) {
                throw new SQLException("Failed to insert or update collection record.");
            }

            // Insert into related tables
            insertRelatedData(connection, invoiceQuery, collectionId, collection.getSalesInvoiceHeaders().stream().map(SalesInvoiceHeader::getInvoiceId).collect(Collectors.toList()));
            LOGGER.info("Collection invoices inserted successfully.");

            insertRelatedData(connection, memoQuery, collectionId, collection.getCustomerCreditDebitMemos().stream().map(CreditDebitMemo::getId).collect(Collectors.toList()));
            LOGGER.info("Collection memos inserted successfully.");

            insertRelatedData(connection, returnQuery, collectionId, collection.getSalesReturns().stream().map(SalesReturn::getReturnId).collect(Collectors.toList()));
            LOGGER.info("Collection returns inserted successfully.");

            insertCollectionDetails(connection, detailsQuery, denominationQuery, collectionId, collection.getCollectionDetails());
            LOGGER.info("Collection details and denominations inserted successfully.");

            connection.commit();
            LOGGER.info("Transaction committed successfully.");
            return true;
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage());
            if (connection != null) {
                connection.rollback();
                LOGGER.severe("Transaction rolled back due to error: " + e.getMessage());
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
                LOGGER.info("Connection closed and auto-commit reset.");
            }
        }
    }

    private void insertRelatedData(Connection connection, String query, int collectionId, List<Integer> relatedIds) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (Integer id : relatedIds) {
                statement.setInt(1, collectionId);
                statement.setInt(2, id);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertCollectionDetails(Connection connection, String detailsQuery, String denominationQuery, int collectionId, List<CollectionDetail> details) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(detailsQuery, Statement.RETURN_GENERATED_KEYS)) {
            for (CollectionDetail detail : details) {
                statement.setInt(1, collectionId);
                statement.setInt(2, detail.getType().getCoaId());

                // Check if bank is null, then set null, otherwise set the bank ID
                if (detail.getBank() != null) {
                    statement.setInt(3, detail.getBank().getId());
                } else {
                    statement.setNull(3, Types.INTEGER); // Set null if bank is null
                }

                statement.setInt(4, detail.getEncoderId());

                // Check if checkNo is null, then set null, otherwise set the checkNo
                if (detail.getCheckNo() != null) {
                    statement.setString(5, detail.getCheckNo());
                } else {
                    statement.setNull(5, Types.VARCHAR); // Set null if checkNo is null
                }

                // Check if checkDate is null, then set null, otherwise set the checkDate
                if (detail.getCheckDate() != null) {
                    statement.setTimestamp(6, detail.getCheckDate());
                } else {
                    statement.setNull(6, Types.TIMESTAMP); // Set null if checkDate is null
                }

                statement.setDouble(7, detail.getAmount());
                statement.setString(8, detail.getRemarks());

                // Assuming balanceType is not null, as it seems to be an essential field
                statement.setInt(9, detail.getBalanceType().getId());

                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int detailId = generatedKeys.getInt(1);
                        // Assuming detail.getDenominations() returns a List of CollectionDetailsDenomination objects
                        insertDenominations(connection, denominationQuery, detailId, detail.getDenominations());
                    }
                }
            }
        }
    }


    private void insertDenominations(Connection connection, String query, int detailId, List<CollectionDetailsDenomination> denominations) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (CollectionDetailsDenomination denomination : denominations) {
                statement.setInt(1, detailId);
                statement.setInt(2, denomination.getDenomination().getId());
                statement.setInt(3, denomination.getQuantity());  // Assuming CollectionDetailsDenomination has a getQuantity() method
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }


    public boolean updateCollection(Collection collection) throws SQLException {
        String query = "UPDATE collection SET collection_date = ?, date_encoded = ?, salesman_id = ?, collected_by = ?, encoder_id = ?, remarks = ?, isPosted = ?, isCancelled = ?, totalAmount = ? WHERE docNo = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, collection.getCollectionDate());
            statement.setTimestamp(2, collection.getDateEncoded());
            statement.setInt(3, collection.getSalesman().getId());
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
        collection.setSalesman(salesmanDAO.getSalesmanDetails(resultSet.getInt("salesman_id"))); // Assuming you have a method to fetch salesman by ID
        collection.setCollectedBy(employeeDAO.getUserById(resultSet.getInt("collected_by"))); // Assuming you have a method to fetch employee by ID
        collection.setEncoderId(employeeDAO.getUserById(resultSet.getInt("encoder_id")));
        collection.setRemarks(resultSet.getString("remarks"));
        collection.setIsPosted(resultSet.getBoolean("isPosted"));
        collection.setIsCancelled(resultSet.getBoolean("isCancelled"));
        collection.setTotalAmount(resultSet.getDouble("totalAmount"));
        collection.setCollectionDetails(getCollectionDetailsByCollectionId(collection));
        collection.setSalesInvoiceHeaders(getInvoicesByCollectionId(collection));
        return collection;
    }

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();
    SalesInvoicePaymentsDAO salesInvoicePaymentsDAO = new SalesInvoicePaymentsDAO();

    private ObservableList<SalesInvoiceHeader> getInvoicesByCollectionId(Collection collection) {
        ObservableList<SalesInvoiceHeader> invoices = FXCollections.observableArrayList();
        String query = """
                    SELECT si.*
                    FROM sales_invoice si
                    JOIN collection_invoices ci ON si.invoice_id = ci.invoice_id
                    WHERE ci.collection_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, collection.getId());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    SalesInvoiceHeader invoice = new SalesInvoiceHeader();
                    salesInvoiceDAO.mapResultSetToInvoice(resultSet);
                    invoice.setSalesInvoicePayments(FXCollections.observableArrayList(salesInvoicePaymentsDAO.getPaymentsByInvoice(invoice.getInvoiceId())));
                    invoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            DialogUtils.showErrorMessage("Database Error", "Failed to load invoices: " + e.getMessage());
        }
        return invoices;
    }


    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    BankAccountDAO bankAccountDAO = new BankAccountDAO();
    BalanceTypeDAO balanceTypeDAO = new BalanceTypeDAO();

    private ObservableList<CollectionDetail> getCollectionDetailsByCollectionId(Collection collection) {
        ObservableList<CollectionDetail> collectionDetails = FXCollections.observableArrayList();
        String query = "SELECT * FROM collection_details WHERE collection_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, collection.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    CollectionDetail collectionDetail = new CollectionDetail();
                    collectionDetail.setId(resultSet.getInt("id"));
                    collectionDetail.setCollectionId(collection.getId());
                    collectionDetail.setType(chartOfAccountsDAO.getChartOfAccountById(resultSet.getInt("type")));
                    collectionDetail.setBalanceType(balanceTypeDAO.getBalanceTypeById(resultSet.getInt("balance_type_id")));
                    collectionDetail.setBank(bankAccountDAO.getBankNameById(resultSet.getInt("bank")));
                    collectionDetail.setEncoderId(resultSet.getInt("encoder_id"));
                    collectionDetail.setCheckNo(resultSet.getString("check_no"));
                    collectionDetail.setCheckDate(resultSet.getTimestamp("chequeDate"));
                    collectionDetail.setAmount(resultSet.getDouble("amount"));
                    collectionDetail.setRemarks(resultSet.getString("remarks"));
                    collectionDetail.setDenominations(getDenominationsByCollectionDetailId(collectionDetail));
                    collectionDetail.setPayment(collectionDetail.getType().isPayment());
                    collectionDetails.add(collectionDetail);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return collectionDetails;
    }

    DenominationDAO denominationDAO = new DenominationDAO();

    private ObservableList<CollectionDetailsDenomination> getDenominationsByCollectionDetailId(CollectionDetail collectionDetail) {
        ObservableList<CollectionDetailsDenomination> collectionDetailsDenominations = FXCollections.observableArrayList();
        String query = "SELECT * FROM collection_details_denomination WHERE collection_detail_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, collectionDetail.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    CollectionDetailsDenomination collectionDetailsDenomination = new CollectionDetailsDenomination();
                    collectionDetailsDenomination.setId(resultSet.getInt("id"));
                    collectionDetailsDenomination.setCollectionDetail(collectionDetail);
                    collectionDetailsDenomination.setDenomination(denominationDAO.getDenominationById(resultSet.getInt("denomination_id")));
                    collectionDetailsDenomination.setQuantity(resultSet.getInt("quantity"));
                    collectionDetailsDenominations.add(collectionDetailsDenomination);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return collectionDetailsDenominations;
    }
}
