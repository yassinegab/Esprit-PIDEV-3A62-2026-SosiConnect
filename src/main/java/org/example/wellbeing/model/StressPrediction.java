package org.example.wellbeing.model;

import java.time.LocalDateTime;

public class StressPrediction {
    private int id;
    private String predictedStressType;
    private String predictedLabel;
    private double confidenceScore;
    private String modelVersion;
    private String recommendation;
    private LocalDateTime createdAt;
    private UserWellBeingData userWellBeingData;

    public StressPrediction() {
        this.createdAt = LocalDateTime.now();
    }

    public StressPrediction(String predictedStressType, String predictedLabel, double confidenceScore, String modelVersion, String recommendation, UserWellBeingData userWellBeingData) {
        this.predictedStressType = predictedStressType;
        this.predictedLabel = predictedLabel;
        this.confidenceScore = confidenceScore;
        this.modelVersion = modelVersion;
        this.recommendation = recommendation;
        this.userWellBeingData = userWellBeingData;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPredictedStressType() { return predictedStressType; }
    public void setPredictedStressType(String predictedStressType) { this.predictedStressType = predictedStressType; }

    public String getPredictedLabel() { return predictedLabel; }
    public void setPredictedLabel(String predictedLabel) { this.predictedLabel = predictedLabel; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserWellBeingData getUserWellBeingData() { return userWellBeingData; }
    public void setUserWellBeingData(UserWellBeingData userWellBeingData) { this.userWellBeingData = userWellBeingData; }
}
