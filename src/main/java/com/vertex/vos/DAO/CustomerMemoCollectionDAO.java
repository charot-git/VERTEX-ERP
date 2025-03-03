package com.vertex.vos.DAO;

import com.vertex.vos.Objects.CustomerMemo;
import com.vertex.vos.Objects.MemoCollectionApplication;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerMemoCollectionDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean linkMemoToCollection(ObservableList<MemoCollectionApplication> collectionsForMemo) {
        String sql = "INSERT INTO collection_memos (collection_id, memo_id, amount, date_linked) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE amount = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (MemoCollectionApplication memoCollection : collectionsForMemo) {
                stmt.setInt(1, memoCollection.getCollection().getId());
                stmt.setInt(2, memoCollection.getCustomerMemo().getId());
                stmt.setDouble(3, memoCollection.getAmount());
                stmt.setTimestamp(4, memoCollection.getDateLinked());
                stmt.setDouble(5, memoCollection.getAmount());
                stmt.addBatch();
            }
            int[] affectedRows = stmt.executeBatch();
            return affectedRows.length > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unlinkMemosFromCollections(ObservableList<MemoCollectionApplication> deletedCollections) {
        String sql = "DELETE FROM collection_memos WHERE collection_id = ? AND memo_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (MemoCollectionApplication memoCollection : deletedCollections) {
                stmt.setInt(1, memoCollection.getCollection().getId());
                stmt.setInt(2, memoCollection.getCustomerMemo().getId());
                stmt.addBatch();
            }
            int[] affectedRows = stmt.executeBatch();
            return affectedRows.length > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    CollectionDAO collectionDAO = new CollectionDAO();

    public ObservableList<MemoCollectionApplication> getCollectionsByMemoId(CustomerMemo customerMemo) {
        return collectionDAO.getCollectionsByMemoId(customerMemo);
    }
}
