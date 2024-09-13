package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ChatMessage {
    private int messageId;
    private int chatId;
    private int senderId;
    private String messageText;
    private LocalDateTime timestamp;

    public ChatMessage(int messageId, int chatId, int senderId, String messageText, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

}
