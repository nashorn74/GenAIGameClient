package com.omworldgame.guardianjourney.models;

public class ChatMessage {
    private String sender;
    private String message;

    public ChatMessage() {
        // 기본 생성자
    }

    public ChatMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    // Getter 및 Setter 메서드
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
