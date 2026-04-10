package org.example.wellbeing.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.user.model.User;
import org.example.utils.SessionManager;
import org.example.wellbeing.model.UserWellBeingData;
import org.example.wellbeing.model.StressPrediction;
import org.example.wellbeing.service.WellbeingService;
import org.example.wellbeing.service.StressPredictionService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class WellbeingControllerClientController {

    @FXML private StackPane mainContainer;
    @FXML private VBox dashboardView;
    @FXML private ScrollPane formView;
    @FXML private FlowPane cardsContainer;

    @FXML private Slider sldWorkEnvironment;
    @FXML private Slider sldSleepProblems;
    @FXML private Slider sldHeadaches;
    @FXML private Slider sldRestlessness;
    @FXML private Slider sldHeartbeatPalpitations;
    @FXML private Slider sldLowAcademicConfidence;
    @FXML private Slider sldClassAttendance;
    @FXML private Slider sldAnxietyTension;
    @FXML private Slider sldIrritability;
    @FXML private Slider sldSubjectConfidence;

    private WellbeingService wellbeingService = new WellbeingService();
    private StressPredictionService predictionService = new StressPredictionService();
    private UserWellBeingData editingData;

    @FXML
    public void initialize() {
        showDashboard();
    }

    @FXML
    private void showForm() {
        this.editingData = null; // Clear edit mode
        dashboardView.setVisible(false);
        formView.setVisible(true);
    }

    public void showEditForm(UserWellBeingData data) {
        this.editingData = data;
        sldWorkEnvironment.setValue(data.getWorkEnvironment());
        sldSleepProblems.setValue(data.getSleepProblems());
        sldHeadaches.setValue(data.getHeadaches());
        sldRestlessness.setValue(data.getRestlessness());
        sldHeartbeatPalpitations.setValue(data.getHeartbeatPalpitations());
        sldLowAcademicConfidence.setValue(data.getLowAcademicConfidence());
        sldClassAttendance.setValue(data.getClassAttendance());
        sldAnxietyTension.setValue(data.getAnxietyTension());
        sldIrritability.setValue(data.getIrritability());
        sldSubjectConfidence.setValue(data.getSubjectConfidence());
        
        dashboardView.setVisible(false);
        formView.setVisible(true);
    }

    public void refreshDashboard() {
        loadCards();
    }

    @FXML
    private void showDashboard() {
        formView.setVisible(false);
        dashboardView.setVisible(true);
        loadCards();
    }

    private void loadCards() {
        cardsContainer.getChildren().clear();
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        try {
            List<UserWellBeingData> list = wellbeingService.afficher();
            for (UserWellBeingData data : list) {
                // Check if the record belongs to the current user
                if (data.getUser() != null && data.getUser().getId() == currentUser.getId()) {
                    addCardToDashboard(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCardToDashboard(UserWellBeingData data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wellbeing/frontoffice/WellbeingCard.fxml"));
            Parent card = loader.load();
            
            WellbeingCardController cardController = loader.getController();
            cardController.setData(data, this);
            
            cardsContainer.getChildren().add(0, card); // Add to top
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté.");
            return;
        }

        UserWellBeingData data = new UserWellBeingData(
            (int) sldWorkEnvironment.getValue(),
            (int) sldSleepProblems.getValue(),
            (int) sldHeadaches.getValue(),
            (int) sldRestlessness.getValue(),
            (int) sldHeartbeatPalpitations.getValue(),
            (int) sldLowAcademicConfidence.getValue(),
            (int) sldClassAttendance.getValue(),
            (int) sldAnxietyTension.getValue(),
            (int) sldIrritability.getValue(),
            (int) sldSubjectConfidence.getValue(),
            currentUser
        );

        try {
            if (editingData != null) {
                editingData.setWorkEnvironment((int) sldWorkEnvironment.getValue());
                editingData.setSleepProblems((int) sldSleepProblems.getValue());
                editingData.setHeadaches((int) sldHeadaches.getValue());
                editingData.setRestlessness((int) sldRestlessness.getValue());
                editingData.setHeartbeatPalpitations((int) sldHeartbeatPalpitations.getValue());
                editingData.setLowAcademicConfidence((int) sldLowAcademicConfidence.getValue());
                editingData.setClassAttendance((int) sldClassAttendance.getValue());
                editingData.setAnxietyTension((int) sldAnxietyTension.getValue());
                editingData.setIrritability((int) sldIrritability.getValue());
                editingData.setSubjectConfidence((int) sldSubjectConfidence.getValue());
                
                wellbeingService.modifier(editingData);
                StressPrediction prediction = predictionService.predict(editingData);
                editingData = null;
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation mise à jour !");
            } else {
                UserWellBeingData newData = new UserWellBeingData(
                    (int) sldWorkEnvironment.getValue(),
                    (int) sldSleepProblems.getValue(),
                    (int) sldHeadaches.getValue(),
                    (int) sldRestlessness.getValue(),
                    (int) sldHeartbeatPalpitations.getValue(),
                    (int) sldLowAcademicConfidence.getValue(),
                    (int) sldClassAttendance.getValue(),
                    (int) sldAnxietyTension.getValue(),
                    (int) sldIrritability.getValue(),
                    (int) sldSubjectConfidence.getValue(),
                    currentUser
                );
                wellbeingService.ajouter(newData);
                StressPrediction prediction = predictionService.predict(newData);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation enregistrée !");
            }
            
            showDashboard();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la sauvegarde.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
