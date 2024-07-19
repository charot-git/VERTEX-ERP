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
    private static final int MAX_MESSAGE_WIDTH = 300;
    private static final int IMAGE_VIEW_WIDTH = 30;
    private static final int SPACING = 10;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    public ChatBubble(String message, Image image, boolean isSentByUser, LocalDateTime timestamp) {
        setSpacing(SPACING);

        VBox messageDetailsContainer = createMessageDetailsContainer(message, timestamp);
        ImageView imageView = createImageView(image);

        if (isSentByUser) {
            styleUserMessage(messageDetailsContainer, imageView);
        } else {
            styleOtherUserMessage(messageDetailsContainer, imageView);
        }
    }

    private VBox createMessageDetailsContainer(String message, LocalDateTime timestamp) {
        VBox container = new VBox();
        container.setSpacing(SPACING);
        container.setPadding(new Insets(SPACING));

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(MAX_MESSAGE_WIDTH);

        Label timestampLabel = new Label(timestamp.format(TIME_FORMATTER));
        timestampLabel.setStyle("-fx-text-fill: #808080; -fx-font-size: 10");

        container.getChildren().addAll(messageLabel, timestampLabel);
        return container;
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(IMAGE_VIEW_WIDTH);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }

    private void styleUserMessage(VBox messageDetailsContainer, ImageView imageView) {
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(imageView, messageDetailsContainer);
        messageDetailsContainer.setAlignment(Pos.TOP_LEFT);
        messageDetailsContainer.setStyle("-fx-background-color: #155D99; -fx-background-radius: 20px");
        messageDetailsContainer.getChildren().get(0).setStyle("-fx-text-fill: whitesmoke");
        messageDetailsContainer.getChildren().get(1).setStyle("-fx-text-fill: rgba(245,245,245,0.85); -fx-font-size: 10");
    }

    private void styleOtherUserMessage(VBox messageDetailsContainer, ImageView imageView) {
        setAlignment(Pos.TOP_RIGHT);
        getChildren().addAll(messageDetailsContainer, imageView);
        messageDetailsContainer.setStyle("-fx-border-color: #155D99; -fx-border-radius: 20px");
        messageDetailsContainer.setAlignment(Pos.TOP_RIGHT);
    }
}
