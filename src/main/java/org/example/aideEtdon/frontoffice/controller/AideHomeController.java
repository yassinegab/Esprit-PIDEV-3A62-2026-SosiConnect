package org.example.aideEtdon.frontoffice.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.aideEtdon.model.ContactUrgence;
import org.example.aideEtdon.service.ContactUrgenceService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AideHomeController {

    @FXML private Label statusIndicator;
    @FXML private ToggleGroup typeGroup;
    @FXML private ToggleButton btnMed, btnDanger, btnOther;
    @FXML private CheckBox chkLocation;
    @FXML private Button btnEmergency;
    @FXML private VBox cancelBox;
    @FXML private Label lblCountdown;
    @FXML private ListView<String> historyList;

    private ContactUrgenceService service = new ContactUrgenceService();
    private ObservableList<String> history = FXCollections.observableArrayList();
    private Timeline countdownTimeline;
    private int secondsRemaining;

    @FXML
    public void initialize() {
        typeGroup = new ToggleGroup();
        btnMed.setToggleGroup(typeGroup);
        btnDanger.setToggleGroup(typeGroup);
        btnOther.setToggleGroup(typeGroup);
        historyList.setItems(history);

        setupToggleStyle(btnMed);
        setupToggleStyle(btnDanger);
        setupToggleStyle(btnOther);
        
        btnMed.setSelected(true); // Default selection

        setupButtonAnimation(btnEmergency);
    }

    private void setupToggleStyle(ToggleButton btn) {
        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                btn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 20 25; -fx-background-radius: 12; -fx-cursor: hand; -fx-text-fill: white; -fx-background-color: #3b82f6;");
            } else {
                btn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 20 25; -fx-background-radius: 12; -fx-cursor: hand; -fx-text-fill: #334155; -fx-background-color: #f1f5f9;");
            }
        });
    }

    private void setupButtonAnimation(Button btn) {
        btn.setOnMouseEntered(e -> { btn.setScaleX(1.05); btn.setScaleY(1.05); });
        btn.setOnMouseExited(e -> { btn.setScaleX(1.0); btn.setScaleY(1.0); });
    }

    @FXML
    private void handleEmergencyAction() {
        ContactUrgence contact = service.getContactActuel();
        if (contact == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Aucun contact configuré !");
            alert.showAndWait();
            return;
        }

        // Sound Notification Beep
        java.awt.Toolkit.getDefaultToolkit().beep();

        // Start Countdown Sequence
        btnEmergency.setVisible(false);
        cancelBox.setVisible(true);
        secondsRemaining = 10;
        lblCountdown.setText("Annulation possible : " + secondsRemaining + "s");

        statusIndicator.setText("ALERTE EN COURS");
        statusIndicator.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

        if (countdownTimeline != null) countdownTimeline.stop();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            lblCountdown.setText("Annulation possible : " + secondsRemaining + "s");

            if (secondsRemaining <= 0) {
                commitAlert(contact);
            }
        }));
        countdownTimeline.setCycleCount(10);
        countdownTimeline.play();
    }

    @FXML
    private void cancelAlert() {
        if (countdownTimeline != null) countdownTimeline.stop();
        resetToNormal();
        history.add(0, "Alerte Annulée - " + getCurrentTime());
    }

    private void commitAlert(ContactUrgence contact) {
        java.awt.Toolkit.getDefaultToolkit().beep(); 
        resetToNormal();
        
        String type = "Général";
        ToggleButton selected = (ToggleButton) typeGroup.getSelectedToggle();
        if (selected != null) type = selected.getText();
        
        String loc = chkLocation.isSelected() ? " [GPS Inclus]" : "";
        String logEntry = "ALERTE: " + type + loc + " -> " + contact.getNom() + " (" + getCurrentTime() + ")";
        
        history.add(0, logEntry);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Urgence Envoyée");
        alert.setHeaderText("Message d'urgence transmis !");
        alert.setContentText("Le message " + type + " a été envoyé.");
        alert.show();
    }

    private void resetToNormal() {
        btnEmergency.setVisible(true);
        cancelBox.setVisible(false);
        statusIndicator.setText("NORMAL");
        statusIndicator.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @FXML
    private void goToContactForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/aideEtdon/frontoffice/ContactFormView.fxml"));
            Node formView = loader.load();
            AideEtdonControllerClientController.getInstance().setView(formView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
