package com.vertex.vos;

import com.vertex.vos.Constructors.ChatMessage;
import com.vertex.vos.Constructors.User;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.ChatBubble;
import com.vertex.vos.Utilities.ChatDatabaseConnectionPool;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ImageCircle;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ChatContentController implements Initializable {
    private AnchorPane contentPane; // Declare contentPane variable
    @FXML
    private VBox chatVBox;

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private final HikariDataSource chatDataSource = ChatDatabaseConnectionPool.getDataSource();
    @FXML
    private TextField searchChat;
    @FXML
    private AnchorPane chatFieldBox;
    @FXML
    private TextField chatField;
    @FXML
    private Label chatHeaderName;
    @FXML
    private Circle chatmateStatus;
    @FXML
    private ImageView chatmateImage;
    @FXML
    private Label chatmateName;
    @FXML
    private Label chatmatePosition;
    @FXML
    private TextField searchMessage;
    @FXML
    private AnchorPane chatMainBox;
    @FXML
    private AnchorPane chatMainInfo;
    @FXML
    private ImageView emoticonButton;
    @FXML
    private ImageView attachButton;
    @FXML
    private ImageView sendButton;
    @FXML
    private ListView<String> chatListView;
    @FXML
    private ScrollPane chatScrollPane;
    private VBox chatMessagesVBox;  // Declare chatMessagesVBox

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        chatMessagesVBox = new VBox();  // Initialize the VBox
        chatScrollPane.setContent(chatMessagesVBox);  // Set VBox as content of ScrollPane
        try {
            int currentUserId = UserSession.getInstance().getUserId();
            ; // Get the current user's ID (implement this method)
            List<User> users = getUsersFromDatabase(currentUserId);

            // Populate chatVBox with user data (excluding the current user)
            for (User user : users) {
                HBox userBox = createUserBox(user);
                chatVBox.getChildren().add(userBox);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

    }

    private void handleSendMessage(int otherUserId) {
        // Get the sessionId and message from appropriate sources
        int sessionId = UserSession.getInstance().getUserId();
        String message = chatField.getText().trim();

        if (!message.isEmpty()) {
            try {
                // Get or create the chat room and obtain the chatId
                int chatId = createChatRoom(sessionId, otherUserId);

                // Send the message using the obtained chatId and sessionId
                sendMessage(chatId, sessionId, message);
                loadMessages(UserSession.getInstance().getUserId(), otherUserId);
                chatField.setText("");
            } catch (SQLException ex) {
                ex.printStackTrace(); // Handle the exception according to your application's needs
            }
        }
    }

    private List<User> getUsersFromDatabase(int currentUserId) throws SQLException {
        List<User> userList = new ArrayList<>();
        String query = "SELECT * FROM user WHERE user_id != ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, currentUserId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                User user = new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("user_email"),
                        resultSet.getString("user_password"),
                        resultSet.getString("user_fname"),
                        resultSet.getString("user_mname"),
                        resultSet.getString("user_lname"),
                        resultSet.getString("user_contact"),
                        resultSet.getString("user_province"),
                        resultSet.getString("user_city"),
                        resultSet.getString("user_brgy"),
                        resultSet.getString("user_sss"),
                        resultSet.getString("user_philhealth"),
                        resultSet.getString("user_tin"),
                        resultSet.getString("user_position"),
                        resultSet.getString("user_department"),
                        resultSet.getDate("user_dateOfHire"),
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

    private String getLastChatMessageAndSender(int participant1Id, int participant2Id, int currentUserId) throws SQLException {
        String query = "SELECT cm.sender_id, cm.message_text " +
                "FROM chat_messages cm " +
                "WHERE cm.chat_id IN " +
                "(SELECT chat_id FROM chat_rooms WHERE (participant1_id = ? AND participant2_id = ?) OR (participant1_id = ? AND participant2_id = ?)) " +
                "ORDER BY cm.timestamp DESC LIMIT 1";
        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, participant1Id);
            preparedStatement.setInt(2, participant2Id);
            preparedStatement.setInt(3, participant2Id);
            preparedStatement.setInt(4, participant1Id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int senderId = resultSet.getInt("sender_id");
                String messageText = resultSet.getString("message_text");

                if (senderId == currentUserId) {
                    return "You: " + messageText;
                } else {
                    return messageText;
                }
            } else {
                return ""; // Return an empty string if no message is found
            }
        }
    }

    private HBox createUserBox(User user) {
        // Create labels for user details
        String name = user.getUser_fname() + " " + user.getUser_lname();
        String position = user.getUser_position();
        String image = user.getUser_image();

        Image defaultImage = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/profile.png"));
        ImageView userImageView = new ImageView(defaultImage);
        ImageCircle.cicular(userImageView);
        userImageView.setFitWidth(45);
        userImageView.setFitHeight(45);

        String lastMessageFromChat = null;
        try {
            lastMessageFromChat = getLastChatMessageAndSender(UserSession.getInstance().getUserId(), user.getUser_id(), UserSession.getInstance().getUserId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        VBox labelsVBox = new VBox();
        labelsVBox.getStyleClass().add("labelsVBox");

        Label positionLabel = new Label(position);
        positionLabel.getStyleClass().add("chatPosition");
        positionLabel.setStyle("-fx-text-fill: whitesmoke");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("chatName");
        nameLabel.setStyle("-fx-text-fill: whitesmoke;"); // Set text fill to white

        Label lastMessage = new Label(lastMessageFromChat);
        lastMessage.getStyleClass().add("chatStatus");
        lastMessage.setStyle("-fx-text-fill: whitesmoke;"); // Set text fill to white

        labelsVBox.getChildren().addAll(positionLabel, nameLabel, lastMessage);


        // Create new HBox for the user
        HBox userHBox = new HBox();
        userHBox.getStyleClass().addAll("chatHBox", "userHBox"); // Add userHBox style class

        userHBox.setPadding(new Insets(5));
        userHBox.setSpacing(5);
        userHBox.getChildren().addAll(userImageView, labelsVBox);
        userHBox.setAlignment(Pos.CENTER_LEFT);

        if (image != null && !image.isEmpty()) {
            try {
                // Load the user image
                File imageFile = new File(image);
                String absolutePath = imageFile.toURI().toString();
                Image userImage = new Image(absolutePath);
                userImageView.setImage(userImage);
            } catch (Exception e) {
                System.out.println("Error loading user image: " + e.getMessage());
                // If the image loading fails, use the default image
                userImageView.setImage(defaultImage);
            }
        }

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setColor(Color.GRAY);

        userHBox.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            nameLabel.setStyle("-fx-text-fill: #3E4756;");
            lastMessage.setStyle("-fx-text-fill: #3E4756;");
            positionLabel.setStyle("-fx-text-fill: #3E4756;");
            userHBox.setEffect(dropShadow);
        });

        userHBox.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            positionLabel.setStyle("-fx-text-fill: whitesmoke;");
            nameLabel.setStyle("-fx-text-fill: whitesmoke;");
            lastMessage.setStyle("-fx-text-fill: whitesmoke");
        });

        userHBox.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            // Handle the click event here
            // You can access the user information and perform actions accordingly
            chatMainBox.setVisible(true);
            chatMainInfo.setVisible(true);

            chatHeaderName.setText(name);
            chatmateStatus.setFill(Color.GREEN);
            chatmateName.setText(name);
            chatmatePosition.setText(user.getUser_position());

            int otherUserId = user.getUser_id();
            handleSendMessage(otherUserId);

            chatField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    // Handle the event when "Enter" key is pressed
                    handleSendMessage(otherUserId);
                }
            });

            sendButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                handleSendMessage(otherUserId);
            });
            loadMessages(UserSession.getInstance().getUserId(), otherUserId);
        });
        return userHBox;
    }

    private int retrieveChatId(int participant1Id, int participant2Id) throws SQLException {
        int chatId = -1; // Initialize to a default value

        String checkQuery = "SELECT chat_id FROM chat_rooms WHERE (participant1_id = ? AND participant2_id = ?) OR (participant1_id = ? AND participant2_id = ?)";

        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {

            checkStatement.setInt(1, participant1Id);
            checkStatement.setInt(2, participant2Id);
            checkStatement.setInt(3, participant2Id);
            checkStatement.setInt(4, participant1Id);

            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                chatId = resultSet.getInt("chat_id");
            }

            return chatId;
        }
    }

    private void loadMessages(int participant1Id, int participant2Id) {
        try {
            // Retrieve the chatId for the given participants
            int chatId = retrieveChatId(participant1Id, participant2Id);

            // Get chat messages for the obtained chatId from the database
            List<ChatMessage> chatMessages = getChatMessages(chatId);

            // Clear existing messages in the chatMessagesVBox
            chatMessagesVBox.getChildren().clear();

            chatMessagesVBox.setSpacing(5);

            // Add messages to the chatMessagesVBox as ChatBubble instances
            for (ChatMessage chatMessage : chatMessages) {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/profile.png")));
                ChatBubble chatBubble = new ChatBubble(chatMessage.getMessageText(), image, chatMessage.getSenderId() == UserSession.getInstance().getUserId(), chatMessage.getTimestamp());
                chatMessagesVBox.getChildren().add(chatBubble);
            }

            // Bind the Vvalue property of the chatScrollPane to the heightProperty of chatMessagesVBox
            chatScrollPane.vvalueProperty().bind(chatMessagesVBox.heightProperty());

        } catch (SQLException ex) {
            ex.printStackTrace(); // Handle the exception according to your application's needs
        }
    }

    private int createChatRoom(int userId1, int userId2) throws SQLException {
        // Check if a chat room already exists for the participants
        String checkQuery = "SELECT chat_id FROM chat_rooms WHERE (participant1_id = ? AND participant2_id = ?) OR (participant1_id = ? AND participant2_id = ?)";
        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {

            checkStatement.setInt(1, userId1);
            checkStatement.setInt(2, userId2);
            checkStatement.setInt(3, userId2);
            checkStatement.setInt(4, userId1);

            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                int chatId = resultSet.getInt("chat_id");
                return chatId;
            } else {
                // Chat room doesn't exist, create a new one and return its chat_id
                String insertQuery = "INSERT INTO chat_rooms (participant1_id, participant2_id) VALUES (?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

                    insertStatement.setInt(1, userId1);
                    insertStatement.setInt(2, userId2);
                    insertStatement.executeUpdate();

                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int chatId = generatedKeys.getInt(1); // Move the cursor to the first row and retrieve chat_id
                        return chatId; // Return the generated chat_id
                    } else {
                        throw new SQLException("Creating chat room failed, no ID obtained.");
                    }
                }
            }
        }
    }


    private void sendMessage(int chatId, int senderId, String message) throws SQLException {
        String query = "INSERT INTO chat_messages (chat_id, sender_id, message_text, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, chatId);
            preparedStatement.setInt(2, senderId);
            preparedStatement.setString(3, message);
            preparedStatement.executeUpdate();
        }
    }

    private List<ChatMessage> getChatMessages(int chatId) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        String query = "SELECT * FROM chat_messages WHERE chat_id = ?";
        try (Connection connection = chatDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, chatId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int messageId = resultSet.getInt("message_id");
                int senderId = resultSet.getInt("sender_id");
                String messageText = resultSet.getString("message_text");
                LocalDateTime timestamp = resultSet.getTimestamp("timestamp").toLocalDateTime();

                ChatMessage chatMessage = new ChatMessage(messageId, chatId, senderId, messageText, timestamp);
                messages.add(chatMessage);
            }
        }
        return messages;
    }


}
