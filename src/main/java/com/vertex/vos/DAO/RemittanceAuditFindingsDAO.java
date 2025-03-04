package com.vertex.vos.DAO;

import com.vertex.vos.Objects.RemittanceAuditFinding;
import com.vertex.vos.Objects.CollectionDetail;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.SalesmanDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class RemittanceAuditFindingsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final EmployeeDAO userDAO = new EmployeeDAO();
    private final CollectionDAO collectionDetailsDAO = new CollectionDAO();

    public String generateNewDocNo() {

        String selectQuery = "SELECT no FROM raf_no FOR UPDATE";
        String updateQuery = "UPDATE raf_no SET no = no + 1";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            try (ResultSet resultSet = statement.executeQuery(selectQuery)) {
                if (resultSet.next()) {
                    int no = resultSet.getInt("no");
                    statement.executeUpdate(updateQuery);
                    connection.commit();
                    return String.format("RAF-%05d", no + 1);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("40001")) { // Deadlock retry
                return generateNewDocNo();
            }
            throw new RuntimeException("Failed to generate new document number", e);
        }
        return null;
    }

    SalesmanDAO salesmanDAO = new SalesmanDAO();


    public ObservableList<RemittanceAuditFinding> getAllAuditFindings() {
        ObservableList<RemittanceAuditFinding> auditFindings = FXCollections.observableArrayList();
        String sql = "SELECT * FROM remittance_audit_finding";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                RemittanceAuditFinding audit = new RemittanceAuditFinding();
                audit.setId(rs.getInt("id"));
                audit.setDocNo(rs.getString("doc_no"));
                audit.setDateAudited(rs.getTimestamp("date_audited"));
                audit.setDateFrom(rs.getTimestamp("date_from"));
                audit.setDateTo(rs.getTimestamp("date_to"));
                audit.setAuditee(salesmanDAO.getSalesmanDetails(rs.getInt("auditee_id")));
                audit.setAuditor(userDAO.getUserById(rs.getInt("auditor_id")));
                audit.setAmount(rs.getDouble("amount"));
                audit.setDateCreated(rs.getTimestamp("date_created"));
                auditFindings.add(audit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return auditFindings;
    }

    // Insert a new Remittance Audit Finding
    public boolean insertAuditFinding(RemittanceAuditFinding audit) {
        String sql = """
                INSERT INTO remittance_audit_finding 
                (doc_no, date_audited, date_from, date_to, auditee_id, auditor_id, amount)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, audit.getDocNo());
                stmt.setTimestamp(2, audit.getDateAudited());
                stmt.setTimestamp(3, audit.getDateFrom());
                stmt.setTimestamp(4, audit.getDateTo());
                stmt.setInt(5, audit.getAuditee().getId());
                stmt.setInt(6, audit.getAuditor().getUser_id());
                stmt.setDouble(7, audit.getAmount());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int auditId = rs.getInt(1); // Generated ID

                            // ✅ Step 1: Get current details from DB
                            Set<Integer> existingDetailIds = getExistingCollectionDetails(conn, auditId);

                            // ✅ Step 2: Add or keep existing details
                            Set<Integer> newDetailIds = new HashSet<>();
                            for (CollectionDetail detail : audit.getCollectionDetails()) {
                                newDetailIds.add(detail.getId());
                                if (!existingDetailIds.contains(detail.getId())) {
                                    if (!linkCollectionDetail(conn, auditId, detail.getId())) {
                                        conn.rollback();
                                        return false;
                                    }
                                }
                            }

                            // ✅ Step 3: Remove details that are no longer in the new list
                            for (int existingId : existingDetailIds) {
                                if (!newDetailIds.contains(existingId)) {
                                    if (!unlinkCollectionDetail(conn, auditId, existingId)) {
                                        conn.rollback();
                                        return false;
                                    }
                                }
                            }

                            conn.commit();
                            return true;
                        }
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Insert failed
    }

    private boolean unlinkCollectionDetail(Connection conn, int auditId, int collectionDetailId) throws SQLException {
        String sql = "DELETE FROM remittance_audit_finding_details WHERE remittance_audit_finding_id = ? AND collection_detail_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, auditId);
            stmt.setInt(2, collectionDetailId);
            return stmt.executeUpdate() > 0;
        }
    }


    private Set<Integer> getExistingCollectionDetails(Connection conn, int auditId) throws SQLException {
        String sql = "SELECT collection_detail_id FROM remittance_audit_finding_details WHERE remittance_audit_finding_id = ?";
        Set<Integer> existingIds = new HashSet<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, auditId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    existingIds.add(rs.getInt("collection_detail_id"));
                }
            }
        }
        return existingIds;
    }


    public boolean linkCollectionDetail(Connection conn, int auditId, int collectionDetailId) {
        String sql = """
                INSERT INTO remittance_audit_finding_details (remittance_audit_finding_id, collection_detail_id)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE collection_detail_id = VALUES(collection_detail_id)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) { // ✅ No need for RETURN_GENERATED_KEYS
            stmt.setInt(1, auditId);
            stmt.setInt(2, collectionDetailId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Insert failed
    }


    // Retrieve a Remittance Audit Finding by ID (including linked collection details)
    public RemittanceAuditFinding getAuditFindingById(int id) {
        String sql = "SELECT * FROM remittance_audit_finding WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                RemittanceAuditFinding audit = new RemittanceAuditFinding();
                audit.setId(rs.getInt("id"));
                audit.setDocNo(rs.getString("doc_no"));
                audit.setDateAudited(rs.getTimestamp("date_audited"));
                audit.setDateFrom(rs.getTimestamp("date_from"));
                audit.setDateTo(rs.getTimestamp("date_to"));
                audit.setDateCreated(rs.getTimestamp("date_created"));
                audit.setDateUpdated(rs.getTimestamp("date_updated"));
                audit.setAmount(rs.getDouble("amount"));

                // Fetch User objects instead of just IDs
                audit.setAuditee(salesmanDAO.getSalesmanDetails(rs.getInt("auditee_id")));
                audit.setAuditor(userDAO.getUserById(rs.getInt("auditor_id")));

                // Fetch linked collection details
                audit.setCollectionDetails(getCollectionDetailsByAuditId(audit.getId()));

                return audit;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Retrieve Collection Details linked to a Remittance Audit Finding
    private ObservableList<CollectionDetail> getCollectionDetailsByAuditId(int auditId) {
        ObservableList<CollectionDetail> detailsList = FXCollections.observableArrayList();
        String sql = """
                SELECT collection_detail_id FROM remittance_audit_finding_details 
                WHERE remittance_audit_finding_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, auditId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CollectionDetail detail = collectionDetailsDAO.getCollectionDetailsById(rs.getInt("collection_detail_id"));
                if (detail != null) {
                    detailsList.add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detailsList;
    }


    // Update an existing Remittance Audit Finding
    public boolean updateRemittanceAuditFinding(RemittanceAuditFinding audit) {
        String sql = """
            UPDATE remittance_audit_finding 
            SET doc_no = ?, date_audited = ?, date_from = ?, date_to = ?, amount = ?, 
                auditee_id = ?, auditor_id = ?, date_created = ?, date_updated = ? 
            WHERE id = ?
            """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); // ✅ Start a transaction

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, audit.getDocNo());
                stmt.setTimestamp(2, Timestamp.valueOf(audit.getDateAudited().toLocalDateTime()));
                stmt.setTimestamp(3, Timestamp.valueOf(audit.getDateFrom().toLocalDateTime()));
                stmt.setTimestamp(4, Timestamp.valueOf(audit.getDateTo().toLocalDateTime()));
                stmt.setDouble(5, audit.getAmount());
                stmt.setInt(6, audit.getAuditee().getId());
                stmt.setInt(7, audit.getAuditor().getUser_id());
                stmt.setTimestamp(8, Timestamp.valueOf(audit.getDateCreated().toLocalDateTime()));
                stmt.setTimestamp(9, Timestamp.valueOf(audit.getDateUpdated().toLocalDateTime()));
                stmt.setInt(10, audit.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    updateCollectionDetails(conn, audit); // ✅ Ensure this runs within the same transaction
                    conn.commit(); // ✅ Commit only if everything succeeds
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback(); // ✅ Rollback if an exception occurs
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Update failed
    }

    private void updateCollectionDetails(Connection conn, RemittanceAuditFinding audit) throws SQLException {
        Set<Integer> existingIds = getExistingCollectionDetails(conn, audit.getId());
        ObservableList<CollectionDetail> collectionDetails = audit.getCollectionDetails();

        for (CollectionDetail detail : collectionDetails) {
            if (existingIds.contains(detail.getId())) {
                existingIds.remove(detail.getId()); // ✅ Already exists, just remove from tracking list
            } else {
                linkCollectionDetail(conn, audit.getId(), detail.getId()); // ✅ Add new link
            }
        }

        // ✅ Remove any details that no longer exist in the updated list
        for (int id : existingIds) {
            unlinkCollectionDetail(conn, audit.getId(), id);
        }
    }


    public ObservableList<CollectionDetail> getCollectionDetails(RemittanceAuditFinding selectedRaf) {
        ObservableList<CollectionDetail> detailsList = FXCollections.observableArrayList();
        String sql = "SELECT collection_detail_id FROM remittance_audit_finding_details WHERE remittance_audit_finding_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedRaf.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CollectionDetail detail = collectionDetailsDAO.getCollectionDetailsById(rs.getInt("collection_detail_id"));
                    detailsList.add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detailsList;
    }

    // Link Collection Details to an Audit Finding

}
