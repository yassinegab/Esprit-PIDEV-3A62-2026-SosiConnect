package org.example.backoffice.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AdminBaseController {

    @FXML private StackPane adminContentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnWellbeing;
    @FXML private Button btnMedical;
    @FXML private Button btnAide;
    @FXML private Button btnCycle;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        navButtons = Arrays.asList(btnDashboard, btnUsers, btnWellbeing, btnMedical, btnAide, btnCycle);
        // Load default view
        showWellbeingAdmin(); 
    }

    @FXML
    private void showDashboard() {
        updateActiveButton(btnDashboard);
        // Placeholder for main admin dashboard
    }

    @FXML
    private void showUserAdmin() {
        loadView("/user/backoffice/UserAdminView.fxml", btnUsers);
    }

    @FXML
    private void showWellbeingAdmin() {
        loadView("/wellbeing/backoffice/WellbeingAdminDashboard.fxml", btnWellbeing);
    }

    @FXML
    private void showMedicalAdmin() {
        loadView("/servicesociaux/backoffice/ServicesSociauxAdminView.fxml", btnMedical);
    }

    @FXML
    private void showAideAdmin() {
        loadView("/aideEtdon/backoffice/AideEtdonAdminView.fxml", btnAide);
    }

    @FXML
    private void showCycleAdmin() {
        loadView("/cycle/backoffice/CycleAdminView.fxml", btnCycle);
    }

    private void loadView(String fxmlPath, Button activeBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            adminContentArea.getChildren().setAll(view);
            updateActiveButton(activeBtn);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load FXML: " + fxmlPath);
        }
    }

    private void updateActiveButton(Button activeBtn) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("active");
        }
        if (activeBtn != null) {
            if (!activeBtn.getStyleClass().contains("active")) {
                activeBtn.getStyleClass().add("active");
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) adminContentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
