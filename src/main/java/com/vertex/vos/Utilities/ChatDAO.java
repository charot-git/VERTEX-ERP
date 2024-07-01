package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.User;
import com.zaxxer.hikari.HikariDataSource;
import com.vertex.vos.Objects.ChatMessage;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {
    private final HikariDataSource chatDataSource = ChatDatabaseConnectionPool.getDataSource();
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();


    public long getLastMessageTimestamp(int currentUserId, int otherUserId) throws SQLException {
        String query = "SELECT MAX(timestamp) AS last_timestamp FROM chat_messages WHERE (sender_id = ? AND chat_id IN (SELECT chat_id FROM chat_rooms WHERE (participant1_id = ? AND participant2_id = ?) OR (participant1_id = ? AND participant2_id = ?)))";

        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, currentUserId);
            preparedStatement.setInt(2, currentUserId);
            preparedStatement.setInt(3, otherUserId);
            preparedStatement.setInt(4, otherUserId);
            preparedStatement.setInt(5, currentUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Timestamp lastTimestamp = resultSet.getTimestamp("last_timestamp");
                if (lastTimestamp != null) {
                    return lastTimestamp.getTime();
                }
            }
        }
        return 0;
    }


    public List<ChatMessage> fetchMessages(int sessionId, int otherUserId) {
        List<ChatMessage> messages = new ArrayList<>();
        String query = "SELECT message_id, chat_id, sender_id, message_text, timestamp FROM chat_messages WHERE chat_id = ?";

        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int chatId = getChatRoomId(sessionId, otherUserId);

            preparedStatement.setInt(1, chatId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ChatMessage chatMessage = new ChatMessage(
                        resultSet.getInt("message_id"),
                        resultSet.getInt("chat_id"),
                        resultSet.getInt("sender_id"),
                        resultSet.getString("message_text"),
                        resultSet.getTimestamp("timestamp").toLocalDateTime()
                );
                messages.add(chatMessage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public String getLastChatMessageAndSender(int currentUserId, int otherUserId, int sessionId) throws SQLException {
        String query = "SELECT sender_id, message_text FROM chat_messages WHERE chat_id = (SELECT chat_id FROM chat_rooms WHERE (participant1_id = ? AND participant2_id = ?) OR (participant1_id = ? AND participant2_id = ?)) ORDER BY timestamp DESC LIMIT 1;\n";

        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, currentUserId);
            preparedStatement.setInt(2, otherUserId);
            preparedStatement.setInt(3, otherUserId);
            preparedStatement.setInt(4, currentUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int senderId = resultSet.getInt("sender_id");
                String message = resultSet.getString("message_text");

                String sender = senderId == sessionId ? "You: " : "";
                return sender + message;
            }
        }
        return "";
    }

    public void sendMessage(int chatId, int senderId, String message) throws SQLException {
        String query = "INSERT INTO chat_messages (chat_id, sender_id, message_text, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, chatId);
            preparedStatement.setInt(2, senderId);
            preparedStatement.setString(3, message);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.executeUpdate();
        }
    }

    public int getChatRoomId(int user1Id, int user2Id) throws SQLException {
        String query = "SELECT chat_id FROM chat_rooms WHERE (participant1_id = ? AND participant2_id = ?) OR (participant1_id = ? AND participant2_id = ?);\n";

        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, user1Id);
            preparedStatement.setInt(2, user2Id);
            preparedStatement.setInt(3, user2Id);
            preparedStatement.setInt(4, user1Id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("chat_id");
            } else {
                return createChatRoom(user1Id, user2Id);
            }
        }
    }

    private int createChatRoom(int user1Id, int user2Id) throws SQLException {
        String query = "INSERT INTO chat_rooms (participant1_id, participant2_id) VALUES (?, ?);\n";

        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, user1Id);
            preparedStatement.setInt(2, user2Id);
            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                throw new SQLException("Creating chat room failed, no ID obtained.");
            }
        }
    }

    public List<User> getUsersFromDatabase(int currentUserId) throws SQLException {
        List<User> userList = new ArrayList<>();
        String query = "SELECT user_id, user_email, user_fname, user_mname, user_lname," +
                "user_contact, user_province, user_city, user_brgy," +
                "user_position, user_department, user_tags, user_bday," +
                "role_id, user_image FROM user WHERE user_id != ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, currentUserId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                User user = new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("user_email"),
                        resultSet.getString("user_fname"),
                        resultSet.getString("user_mname"),
                        resultSet.getString("user_lname"),
                        resultSet.getString("user_contact"),
                        resultSet.getString("user_province"),
                        resultSet.getString("user_city"),
                        resultSet.getString("user_brgy"),
                        resultSet.getString("user_position"),
                        resultSet.getInt("user_department"),
                        resultSet.getString("user_tags"),
                        resultSet.getDate("user_bday"),
                        resultSet.getInt("role_id"),
                        resultSet.getString("user_image")
                );
                userList.add(user);
            }
        }
        return userList;
    }
}
