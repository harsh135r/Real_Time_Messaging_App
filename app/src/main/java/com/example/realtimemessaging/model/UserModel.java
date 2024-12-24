package com.example.realtimemessaging.model;

import com.google.firebase.Timestamp;

public class UserModel {
    private String phone;
    private String username;
    private com.google.firebase.Timestamp createdTimestamp;
    private String userId;

    public UserModel() {
    }

    public UserModel(String phone, String username, com.google.firebase.Timestamp createdTimestamp,String userId) {
        this.phone = phone;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId= userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public com.google.firebase.Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

}
