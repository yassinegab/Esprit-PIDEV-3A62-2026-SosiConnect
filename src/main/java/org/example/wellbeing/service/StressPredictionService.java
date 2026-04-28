package org.example.wellbeing.service;

import org.example.wellbeing.model.StressPrediction;
import org.example.wellbeing.model.UserWellBeingData;
import org.example.utils.MyConnection;
import org.example.utils.AiService;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StressPredictionService {

    private Connection connection;
    private AiService aiService;

    public StressPredictionService() {
        try {
            connection = MyConnection.getConnection();
            aiService = new AiService();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public StressPrediction predict(UserWellBeingData data) throws SQLException {
        // 1. Calculate weighted score
        Map<String, Double> weights = new HashMap<>();
        weights.put("anxietyTension", 2.5);
        weights.put("headaches", 2.0);
        weights.put("heartbeatPalpitations", 2.0);
        weights.put("sleepProblems", 1.5);
        weights.put("restlessness", 1.5);
        weights.put("irritability", 1.5);
        weights.put("workEnvironment", 1.5);
        weights.put("classAttendance", 1.0);
        weights.put("lowAcademicConfidence", 1.5);
        weights.put("subjectConfidence", 2.0);

        double totalScore = 0;
        double maxPossibleRaw = 0;

        totalScore += data.getAnxietyTension() * weights.get("anxietyTension");
        totalScore += data.getHeadaches() * weights.get("headaches");
        totalScore += data.getHeartbeatPalpitations() * weights.get("heartbeatPalpitations");
        totalScore += data.getSleepProblems() * weights.get("sleepProblems");
        totalScore += data.getRestlessness() * weights.get("restlessness");
        totalScore += data.getIrritability() * weights.get("irritability");
        totalScore += data.getWorkEnvironment() * weights.get("workEnvironment");
        totalScore += data.getClassAttendance() * weights.get("classAttendance");
        totalScore += data.getLowAcademicConfidence() * weights.get("lowAcademicConfidence");
        totalScore += data.getSubjectConfidence() * weights.get("subjectConfidence");

        for (Double w : weights.values()) {
            maxPossibleRaw += 5 * w;
        }

        double normalizedScore = (totalScore / maxPossibleRaw) * 100;

        // 2. Classify
        String label = "Moderate";
        if (normalizedScore <= 35) {
            label = "Low";
        } else if (normalizedScore >= 70) {
            label = "High";
        }

        // 3. Create Entity
        StressPrediction prediction = new StressPrediction();
        prediction.setUserWellBeingData(data);
        prediction.setPredictedStressType(label);
        prediction.setPredictedLabel(label);
        prediction.setConfidenceScore((float) normalizedScore);
        prediction.setModelVersion("v1.0-weighted-java");

        // 4. Generate AI Recommendation
        String recommendation = generateAiRecommendation(prediction, data);
        prediction.setRecommendation(recommendation);

        // 5. Save to database
        ajouter(prediction);

        return prediction;
    }

    private String generateAiRecommendation(StressPrediction prediction, UserWellBeingData data) {
        String prompt = String.format(
            "Based on the following well-being data, provide 3 brief, actionable recommendations to reduce stress.\n" +
            "Stress Level: %s (Score: %.1f/100).\n" +
            "Factors (1-5 scale): Sleep: %d, Headaches: %d, Anxiety: %d, Heartbeat: %d.\n" +
            "Keep it professional and empathetic. Use bullet points.",
            prediction.getPredictedStressType(),
            prediction.getConfidenceScore(),
            data.getSleepProblems(),
            data.getHeadaches(),
            data.getAnxietyTension(),
            data.getHeartbeatPalpitations()
        );

        return aiService.analyzeText(prompt);
    }

    public void ajouter(StressPrediction sp) throws SQLException {
        String req = "INSERT INTO stress_prediction (user_well_being_data_id, predicted_stress_type, predicted_label, confidence_score, recommendation, model_version, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sp.getUserWellBeingData().getId());
            ps.setString(2, sp.getPredictedStressType());
            ps.setString(3, sp.getPredictedLabel());
            ps.setDouble(4, sp.getConfidenceScore());
            ps.setString(5, sp.getRecommendation());
            ps.setString(6, sp.getModelVersion());
            ps.setTimestamp(7, Timestamp.valueOf(sp.getCreatedAt()));
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    sp.setId(rs.getInt(1));
                }
            }
        }
    }

    public void modifier(StressPrediction sp) throws SQLException {
        String req = "UPDATE stress_prediction SET predicted_stress_type=?, predicted_label=?, confidence_score=?, recommendation=?, model_version=? WHERE user_well_being_data_id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, sp.getPredictedStressType());
            ps.setString(2, sp.getPredictedLabel());
            ps.setDouble(3, sp.getConfidenceScore());
            ps.setString(4, sp.getRecommendation());
            ps.setString(5, sp.getModelVersion());
            ps.setInt(6, sp.getUserWellBeingData().getId());
            ps.executeUpdate();
        }
    }
}
