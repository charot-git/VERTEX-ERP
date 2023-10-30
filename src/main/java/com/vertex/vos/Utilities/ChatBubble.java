package com.vertex.vos.Utilities;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatBubble extends HBox {
    private final VBox messageDetailsContainer;
    private final ImageView imageView;

    public ChatBubble(String message, Image image, boolean isSentByUser, LocalDateTime timestamp) {

        setSpacing(10);

        messageDetailsContainer = new VBox();
        messageDetailsContainer.setSpacing(10);
        messageDetailsContainer.setPadding(new Insets(10));

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300); // Set the maximum width for the messageLabel

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        String formattedTimestamp = timestamp.format(formatter);
        Label timestampLabel = new Label(formattedTimestamp);
        timestampLabel.setStyle("-fx-text-fill: #808080; -fx-font-size: 10"); // Set timestamp label style

        messageDetailsContainer.getChildren().addAll(messageLabel, timestampLabel);

        imageView = new ImageView(image);
        imageView.setFitWidth(30); // Set the width of the image
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);


        // Customize the appearance based on whether the message is sent by the user or received from others
        if (isSentByUser) {
            // Style for user's messages
            setAlignment(Pos.TOP_LEFT);
            getChildren().addAll(imageView, messageDetailsContainer);
            messageDetailsContainer.setAlignment(Pos.TOP_LEFT);
            messageDetailsContainer.setStyle("-fx-background-color: #155D99; -fx-text-fill: whitesmoke; -fx-background-radius: 20px");
            messageLabel.setStyle("-fx-text-fill: whitesmoke");
            timestampLabel.setStyle("-fx-text-fill: rgba(245,245,245,0.85); -fx-font-size: 10");
        } else {
            // Style for other users' messages
            setAlignment(Pos.TOP_RIGHT);
            getChildren().addAll(messageDetailsContainer,imageView);
            messageDetailsContainer.setStyle("-fx-border-color: #155D99; -fx-border-radius: 20px");
            messageDetailsContainer.setAlignment(Pos.TOP_RIGHT);
        }
    }
}
