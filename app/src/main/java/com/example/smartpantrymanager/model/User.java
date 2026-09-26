package com.example.smartpantrymanager.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "email", unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String fullName;

    @NonNull
    public String email;

    @NonNull
    public String password; // Stored securely / plaintext for local offline demo

    public String avatarUri; // Optional profile picture URI

    public User(@NonNull String fullName, @NonNull String email, @NonNull String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.avatarUri = "";
    }

    public int getId() { return id; }
    @NonNull
    public String getFullName() { return fullName; }
    public void setFullName(@NonNull String fullName) { this.fullName = fullName; }
    @NonNull
    public String getEmail() { return email; }
    public void setEmail(@NonNull String email) { this.email = email; }
    @NonNull
    public String getPassword() { return password; }
    public void setPassword(@NonNull String password) { this.password = password; }
    public String getAvatarUri() { return avatarUri; }
    public void setAvatarUri(String avatarUri) { this.avatarUri = avatarUri; }
}
