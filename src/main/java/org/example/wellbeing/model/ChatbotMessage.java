package org.example.wellbeing.model;

import org.example.user.model.User;
import java.time.LocalDateTime;

public class ChatbotMessage {
    private int id;
    private String content;
    private String role; // 'user' or 'assistant'
    private LocalDateTime createdAt;
    private User user;

    public ChatbotMessage() {
        this.createdAt = LocalDateTime.now();
    }

    public ChatbotMessage(String content, String role, User user) {
        this.content = content;
        this.role = role;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
