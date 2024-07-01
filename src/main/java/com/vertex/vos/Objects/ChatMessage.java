package com.vertex.vos.Objects;

import java.time.LocalDateTime;

public class ChatMessage {
    private int messageId;
    private int chatId;
    private int senderId;
    private String messageText;
    private LocalDateTime timestamp;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public ChatMessage(int messageId, int chatId, int senderId, String messageText, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

}
