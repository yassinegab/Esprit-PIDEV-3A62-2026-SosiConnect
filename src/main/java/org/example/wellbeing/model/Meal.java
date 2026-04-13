package org.example.wellbeing.model;

import org.example.user.model.User;
import java.time.LocalDateTime;

public class Meal {
    private int id;
    private String imageName;
    private String description;
    private String aiAnalysis;
    private LocalDateTime createdAt;
    private User user;
    private Double calories;
    private Double sugar;
    private Double protein;
    private String stressInsight;

    public Meal() {

        this.createdAt = LocalDateTime.now();
    }

    public Meal(String imageName, String description, User user) {
        this.imageName = imageName;
        this.description = description;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAiAnalysis() { return aiAnalysis; }
    public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Double getCalories() { return calories; }
    public void setCalories(Double calories) { this.calories = calories; }

    public Double getSugar() { return sugar; }
    public void setSugar(Double sugar) { this.sugar = sugar; }

    public Double getProtein() { return protein; }
    public void setProtein(Double protein) { this.protein = protein; }

    public String getStressInsight() { return stressInsight; }
    public void setStressInsight(String stressInsight) { this.stressInsight = stressInsight; }
}

