package org.example.wellbeing.backoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.wellbeing.model.UserWellBeingData;

import java.time.format.DateTimeFormatter;

public class WellbeingDetailPopupController {

    @FXML private Label lblSessionDate;
    @FXML private Label lblStressScore;
    @FXML private Label lblStressStatus;
    
    @FXML private Label valAnxiety;
    @FXML private Label valSleep;
    @FXML private Label valIrritability;
    @FXML private Label valConfidence;
    @FXML private Label valHeadache;
    @FXML private Label valHeart;

    @FXML private Label lblAiLabel;
    @FXML private Label lblAiRec;
    @FXML private Button btnAction;

    public void setData(UserWellBeingData data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm");
        lblSessionDate.setText("Session du " + data.getCreatedAt().format(formatter));

        double stressScore = (data.getAnxietyTension() + data.getSleepProblems() + data.getHeadaches() + data.getRestlessness()) / 4.0;
        lblStressScore.setText(String.format("%.1f", stressScore));

        if (stressScore <= 2.0) {
            lblStressStatus.setText("Faible");
            lblStressStatus.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 12; -fx-font-weight: bold;");
        } else if (stressScore <= 3.5) {
            lblStressStatus.setText("Modéré");
            lblStressStatus.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 12; -fx-font-weight: bold;");
        } else {
            lblStressStatus.setText("Élevé");
            lblStressStatus.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 12; -fx-font-weight: bold;");
        }

        valAnxiety.setText(data.getAnxietyTension() + " / 5");
        valSleep.setText(data.getSleepProblems() + " / 5");
        valIrritability.setText(data.getIrritability() + " / 5");
        valConfidence.setText(data.getSubjectConfidence() + " / 5");
        valHeadache.setText(data.getHeadaches() + " / 5");
        valHeart.setText(data.getHeartbeatPalpitations() + " / 5");

        if (data.getStressPrediction() != null) {
            lblAiLabel.setText(data.getStressPrediction().getPredictedLabel());
            lblAiRec.setText(data.getStressPrediction().getRecommendation());
        } else {
            lblAiLabel.setText("Aucune analyse IA disponible");
            lblAiRec.setText("Les prédictions n'étaient pas activées pour cette session.");
        }

        if (stressScore >= 4.0) {
            btnAction.setVisible(true);
        } else {
            btnAction.setVisible(false);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblSessionDate.getScene().getWindow();
        stage.close();
    }
}
