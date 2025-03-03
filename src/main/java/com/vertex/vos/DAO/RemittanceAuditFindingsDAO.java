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
                auditFindings.add(audit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return auditFindings;
    }

    // Insert a new Remittance Audit Finding
    public int insertAuditFinding(RemittanceAuditFinding audit) {
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
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        int auditId = rs.getInt(1); // Generated ID
                        for (CollectionDetail detail : audit.getCollectionDetails()) {
                            linkCollectionDetail(auditId, detail.getId());
                        }
                        conn.commit();
                        return auditId;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Insert failed
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

    // Link Collection Details to an Audit Finding
    public int linkCollectionDetail(int auditId, int collectionDetailId) {
        String sql = """
                INSERT INTO remittance_audit_finding_details (remittance_audit_finding_id, collection_detail_id)
                VALUES (?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, auditId);
            stmt.setInt(2, collectionDetailId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // Generated ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Insert failed
    }
}
