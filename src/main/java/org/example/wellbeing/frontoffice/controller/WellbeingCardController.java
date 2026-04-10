package org.example.wellbeing.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import org.example.wellbeing.model.UserWellBeingData;

public class WellbeingCardController {

    @FXML private Label lblDate;
    @FXML private ProgressBar stressProgressBar;
    @FXML private Label lblStressLevel;

    private UserWellBeingData wellBeingData;
    private WellbeingControllerClientController mainController;

    public void setData(UserWellBeingData data, WellbeingControllerClientController mainController) {
        this.wellBeingData = data;
        this.mainController = mainController;
        updateUI();
    }

    private void updateUI() {
        double stressScore = wellBeingData.getStressScore();
        lblDate.setText(wellBeingData.getCreatedAt().toString().replace("T", " ").substring(0, 16));
        
        // stressScore is 0.0 to 1.0 (average of 1-5 indicators normalized)
        stressProgressBar.setProgress(stressScore);
        
        if (stressScore < 0.33) {
            lblStressLevel.setText("Stress Level: Normal");
            stressProgressBar.setStyle("-fx-accent: #10b981;"); // Green
        } else if (stressScore < 0.66) {
            lblStressLevel.setText("Stress Level: Moderate");
            stressProgressBar.setStyle("-fx-accent: #f59e0b;"); // Orange
        } else {
            lblStressLevel.setText("Stress Level: High");
            stressProgressBar.setStyle("-fx-accent: #ef4444;"); // Red
        }

        // Show AI recommendation in tooltip if available
        if (wellBeingData.getStressPrediction() != null && wellBeingData.getStressPrediction().getRecommendation() != null) {
            Tooltip tooltip = new Tooltip(wellBeingData.getStressPrediction().getRecommendation());
            tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #1e293b; -fx-text-fill: white; -fx-padding: 10;");
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(300);
            lblStressLevel.setTooltip(tooltip);
        }
    }

    @FXML
    private void handleEdit() {
        if (mainController != null) {
            mainController.showEditForm(wellBeingData);
        }
    }

    @FXML
    private void handleViewAI() {
        if (wellBeingData.getStressPrediction() == null) return;

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/wellbeing/frontoffice/WellbeingAiPopup.fxml"));
            javafx.scene.Parent root = loader.load();
            
            WellbeingAiPopupController controller = loader.getController();
            controller.setPrediction(wellBeingData.getStressPrediction());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Stress Analysis");
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            
            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer cette évaluation ?");
        alert.setContentText("Cette action est irréversible et supprimera également l'analyse IA associée.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    new org.example.wellbeing.service.WellbeingService().supprimer(wellBeingData.getId());
                    if (mainController != null) {
                        mainController.refreshDashboard();
                    }
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
