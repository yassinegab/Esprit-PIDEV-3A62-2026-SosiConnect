package org.example.wellbeing.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.wellbeing.model.StressPrediction;

public class WellbeingAiPopupController {

    @FXML private Label lblLevel;
    @FXML private Label lblScore;
    @FXML private Label lblRecommendations;
    @FXML private Button btnClose;

    public void setPrediction(StressPrediction prediction) {
        lblLevel.setText(prediction.getPredictedStressType());
        lblScore.setText(String.format("%.1f%%", prediction.getConfidenceScore()));
        lblRecommendations.setText(prediction.getRecommendation());

        // Update color based on level
        switch (prediction.getPredictedStressType().toLowerCase()) {
            case "low":
                lblLevel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #10b981;");
                break;
            case "moderate":
                lblLevel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
                break;
            case "high":
                lblLevel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
                break;
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
