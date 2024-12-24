package com.example.realtimemessaging.model;

import com.google.protobuf.Timestamp;

public class ChatMessageModel {

    private String message;
    private String senderId;
    private com.google.firebase.Timestamp timestamp;

    public ChatMessageModel() {
    }

    public ChatMessageModel(String message, String senderId, com.google.firebase.Timestamp timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public com.google.firebase.Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(com.google.firebase.Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
