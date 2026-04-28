package org.example.home.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.user.model.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HomeController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Label avatarLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox dashboardView;

    @FXML
    private Button btnWellbeing;

    @FXML
    private Button btnServicesSociaux;

    @FXML
    private Button btnJournal;

    @FXML
    private Button btnAideEtdon;

    @FXML
    private Button btnCycle;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        navButtons = Arrays.asList(btnWellbeing, btnServicesSociaux, btnJournal, btnAideEtdon, btnCycle);
    }

    public void setUser(User user) {
        userNameLabel.setText(user.getNom() + " " + user.getPrenom());
        userRoleLabel.setText(user.getRole().name());
        
        // Dynamic Initials
        String initials = "";
        if (user.getNom() != null && !user.getNom().isEmpty()) initials += user.getNom().substring(0, 1).toUpperCase();
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) initials += user.getPrenom().substring(0, 1).toUpperCase();
        avatarLabel.setText(initials);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        contentArea.getChildren().setAll(dashboardView);
        updateActiveButton(null);
    }

    @FXML
    private void showWellbeing() {
        loadView("/wellbeing/frontoffice/WellbeingClientView.fxml", btnWellbeing);
    }

    @FXML
    private void showServicesSociaux() {
        loadView("/servicesociaux/frontoffice/ServicesSociauxClientView.fxml", btnServicesSociaux);
    }

    @FXML
    private void showAideEtdon() {
        loadView("/aideEtdon/frontoffice/AideEtdonClientView.fxml", btnAideEtdon);
    }

    @FXML
    private void showCycle() {
        loadView("/cycle/frontoffice/CycleClientView.fxml", btnCycle);
    }

    private void loadView(String fxmlPath, Button activeBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            updateActiveButton(activeBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateActiveButton(Button activeBtn) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("active-nav");
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("active-nav");
        }
    }
}
