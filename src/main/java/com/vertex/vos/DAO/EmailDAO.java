package com.vertex.vos.DAO;

import com.vertex.vos.Objects.EmailCredentials;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmailDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    /**
     * Inserts new user email credentials into the user_email table.
     *
     * @param credentials The EmailCredentials object containing the email settings.
     * @param userId      The ID of the user (foreign key).
     * @throws SQLException If an error occurs during the database interaction.
     */
    public void addUserEmail(EmailCredentials credentials, int userId) throws SQLException {
        String query = "INSERT INTO user_email (email, host, port, password, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, credentials.getEmail());
            stmt.setString(2, credentials.getHost());
            stmt.setInt(3, credentials.getPort());
            stmt.setString(4, credentials.getPassword());
            stmt.setInt(5, userId);

            stmt.executeUpdate();
        }
    }

    /**
     * Updates existing user email credentials.
     *
     * @param credentials The EmailCredentials object containing the updated email settings.
     * @param userId      The ID of the user (foreign key).
     * @throws SQLException If an error occurs during the database interaction.
     */
    public void updateUserEmail(EmailCredentials credentials, int userId) throws SQLException {
        String query = "UPDATE user_email SET host = ?, port = ?, password = ? WHERE email = ? AND user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, credentials.getHost());
            stmt.setInt(2, credentials.getPort());
            stmt.setString(3, credentials.getPassword());
            stmt.setString(4, credentials.getEmail());
            stmt.setInt(5, userId);

            stmt.executeUpdate();
        }
    }

    /**
     * Fetches the email credentials for a user by their user ID.
     *
     * @param userId The ID of the user.
     * @return An EmailCredentials object containing the email settings, or null if no email is found.
     * @throws SQLException If an error occurs during the database interaction.
     */
    public EmailCredentials getUserEmailByUserId(int userId) throws SQLException {
        String query = "SELECT email, host, port, password FROM user_email WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    EmailCredentials credentials = new EmailCredentials();
                    credentials.setEmail(rs.getString("email"));
                    credentials.setHost(rs.getString("host"));
                    credentials.setPort(rs.getInt("port"));
                    credentials.setPassword(rs.getString("password"));
                    return credentials;
                }
            }
        }
        return null;
    }

    /**
     * Deletes an email record by email address.
     *
     * @param email The email address to delete.
     * @param userId The ID of the user.
     * @throws SQLException If an error occurs during the database interaction.
     */
    public void deleteUserEmail(String email, int userId) throws SQLException {
        String query = "DELETE FROM user_email WHERE email = ? AND user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setInt(2, userId);

            stmt.executeUpdate();
        }
    }
}
