package com.vertex.vos;

import com.vertex.vos.Objects.ChatMessage;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.ChatBubble;
import com.vertex.vos.Utilities.ChatDAO;
import com.vertex.vos.Utilities.ImageCircle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class ChatContentController implements Initializable {
    @FXML
    private VBox chatVBox;
    @FXML
    private VBox usersInChatVBox;
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

    private VBox chatMessagesVBox;
    private AnchorPane contentPane;
    private final ChatDAO chatDAO = new ChatDAO();

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatMessagesVBox = new VBox();
        chatMessagesVBox.setSpacing(5);
        chatScrollPane.setContent(chatMessagesVBox);
        usersInChatVBox.setSpacing(5);

        loadUsersInChatVBox();

        chatField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSendMessage(UserSession.getInstance().getUserId());
            }
        });

        sendButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            handleSendMessage(UserSession.getInstance().getUserId());
        });
    }

    private void loadUsersInChatVBox() {
        usersInChatVBox.getChildren().clear();
        int currentUserId = UserSession.getInstance().getUserId();

        CompletableFuture.supplyAsync(() -> {
            List<User> users = Collections.emptyList();
            try {
                users = getUsersFromDatabase(currentUserId);
                users.sort(Comparator.comparingLong(user -> {
                    try {
                        return chatDAO.getLastMessageTimestamp(currentUserId, user.getUser_id());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }));
                Collections.reverse(users);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return users;
        }).thenAccept(users -> {
            Platform.runLater(() -> {
                for (User user : users) {
                    try {
                        HBox userBox = createUserBox(user);
                        usersInChatVBox.getChildren().add(userBox);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }


    private void handleSendMessage(int otherUserId) {
        int sessionId = UserSession.getInstance().getUserId();
        String message = chatField.getText().trim();

        if (!message.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    int chatId = chatDAO.getChatRoomId(sessionId, otherUserId);
                    chatDAO.sendMessage(chatId, sessionId, message);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }).thenRun(() -> {
                Platform.runLater(() -> {
                    chatField.setText("");
                    loadMessages(sessionId, otherUserId);
                    loadUsersInChatVBox();
                });
            });
        }
    }


    private List<User> getUsersFromDatabase(int currentUserId) throws SQLException {
        return chatDAO.getUsersFromDatabase(currentUserId);
    }

    private HBox createUserBox(User user) throws SQLException {
        String name = user.getUser_fname() + " " + user.getUser_lname();
        String position = user.getUser_position();
        String image = user.getUser_image();

        Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/profile.png")));
        ImageView userImageView = new ImageView(defaultImage);
        ImageCircle.circular(userImageView);
        userImageView.setFitWidth(45);
        userImageView.setFitHeight(45);

        String lastMessageFromChat = chatDAO.getLastChatMessageAndSender(UserSession.getInstance().getUserId(), user.getUser_id(), UserSession.getInstance().getUserId());

        VBox labelsVBox = new VBox();
        labelsVBox.getStyleClass().add("labelsVBox");

        Label positionLabel = new Label(position);
        positionLabel.getStyleClass().add("chatPosition");
        positionLabel.setStyle("-fx-text-fill: whitesmoke");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("chatName");
        nameLabel.setStyle("-fx-text-fill: whitesmoke;");

        Label lastMessage = new Label(lastMessageFromChat);
        lastMessage.getStyleClass().add("chatStatus");
        lastMessage.setStyle("-fx-text-fill: whitesmoke;");

        labelsVBox.getChildren().addAll(positionLabel, nameLabel, lastMessage);

        HBox userHBox = new HBox();
        userHBox.getStyleClass().addAll("chatHBox", "userHBox");
        userHBox.setPadding(new Insets(5));
        userHBox.setSpacing(5);
        userHBox.getChildren().addAll(userImageView, labelsVBox);
        userHBox.setAlignment(Pos.CENTER_LEFT);

        CompletableFuture.runAsync(() -> {
            if (image != null && !image.isEmpty()) {
                try {
                    File imageFile = new File(image);
                    String absolutePath = imageFile.toURI().toString();
                    Image userImage = new Image(absolutePath);
                    Platform.runLater(() -> userImageView.setImage(userImage));
                } catch (Exception e) {
                    Platform.runLater(() -> userImageView.setImage(defaultImage));
                    e.printStackTrace();
                }
            } else {
                Platform.runLater(() -> userImageView.setImage(defaultImage));
            }
        });

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
            chatMainBox.setVisible(true);
            chatMainInfo.setVisible(true);

            chatHeaderName.setText(name);
            chatmateStatus.setFill(Color.GREEN);
            chatmateName.setText(name);
            chatmatePosition.setText(user.getUser_position());

            int otherUserId = user.getUser_id();
            handleSendMessage(otherUserId);
            loadMessages(UserSession.getInstance().getUserId(), otherUserId);

            chatField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleSendMessage(otherUserId);
                }
            });

            sendButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                handleSendMessage(otherUserId);
            });
        });
        return userHBox;
    }


    private VBox createLabelsVBox(String name, String position, String lastMessageFromChat) {
        VBox labelsVBox = new VBox();
        labelsVBox.setSpacing(5);

        Label nameLabel = new Label(name);
        Label positionLabel = new Label(position);
        Label lastMessageLabel = new Label(lastMessageFromChat);

        labelsVBox.getChildren().addAll(nameLabel, positionLabel, lastMessageLabel);

        return labelsVBox;
    }

    private HBox createUserHBox(ImageView userImageView, VBox labelsVBox) {
        HBox userHBox = new HBox();
        userHBox.setSpacing(10);
        userHBox.setAlignment(Pos.CENTER_LEFT);
        userHBox.getChildren().addAll(userImageView, labelsVBox);
        return userHBox;
    }

    private ImageView createUserImageView(String imagePath) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(40); // Set desired width
        imageView.setFitHeight(40); // Set desired height
        if (imagePath != null && !imagePath.isEmpty()) {
            loadImageAsync(imageView, imagePath, new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/profile.png")));
        } else {
            // Set a default image if imagePath is null or empty
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/profile.png")));
        }
        return imageView;
    }

    private CompletableFuture<Void> loadImageAsync(ImageView imageView, String imagePath, Image defaultImage) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (imagePath != null && !imagePath.isEmpty()) {
            // Load image asynchronously
            Executors.newCachedThreadPool().submit(() -> {
                try {
                    // Load image from file or URL (replace this with your actual image loading logic)
                    Image image = new Image(new File(imagePath).toURI().toString());
                    // Update UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        imageView.setImage(image);
                        future.complete(null);
                    });
                } catch (Exception e) {
                    // If loading fails, set default image
                    Platform.runLater(() -> {
                        imageView.setImage(defaultImage);
                        future.completeExceptionally(e);
                    });
                    e.printStackTrace(); // Handle the exception according to your application's needs
                }
            });
        } else {
            // Set default image if imagePath is null or empty
            imageView.setImage(defaultImage);
            future.complete(null);
        }
        return future;
    }


    private void handleUserClick(User user, String name) {
        chatHeaderName.setText(name);
        chatMainBox.setVisible(true);
        chatMainInfo.setVisible(true);
        chatMainInfo.toFront();

        if (user.getUser_image() != null && !user.getUser_image().isEmpty()) {
            loadImageAsync(chatmateImage, user.getUser_image(), new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/profile.png"))));
        }

        chatmateName.setText(name);
        chatmatePosition.setText(user.getUser_position());

        loadMessages(UserSession.getInstance().getUserId(), user.getUser_id());
    }

    private void loadMessages(int sessionId, int otherUserId) {
        chatMessagesVBox.getChildren().clear();
        CompletableFuture.supplyAsync(() -> chatDAO.fetchMessages(sessionId, otherUserId))
                .thenAccept(messages -> {
                    Platform.runLater(() -> {
                        for (ChatMessage message : messages) {
                            HBox chatMessageBubble = new ChatBubble(message.getMessageText(), null, message.getSenderId() == sessionId, message.getTimestamp());
                            chatMessagesVBox.getChildren().add(chatMessageBubble);
                        }
                        chatScrollPane.setVvalue(1.0);
                    });
                });
    }

}
