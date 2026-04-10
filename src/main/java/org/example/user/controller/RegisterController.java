package org.example.user.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

import org.example.user.service.ServiceUser;
import org.example.user.model.User;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField telephoneField;
    @FXML private TextField ageField;
    @FXML private ChoiceBox<String> sexeBox;
    @FXML private TextField tailleField;
    @FXML private TextField poidsField;
    @FXML private ChoiceBox<String> roleBox;
    @FXML private CheckBox handicapBox;
    @FXML private VBox specialiteContainer;
    @FXML private TextField specialiteField;
    @FXML private Button registerButton;

    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        roleBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isPro = "Professionnel".equals(newVal);
            specialiteContainer.setVisible(isPro);
            specialiteContainer.setManaged(isPro);
        });
        roleBox.getSelectionModel().select("Client");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            String selectedRole = roleBox.getValue();
            String dbUserRole = "ROLE_PATIENT"; 
            if ("Professionnel".equals(selectedRole)) {
                dbUserRole = "ROLE_MEDECIN"; 
            }

            User user = new User(
                nomField.getText(),
                prenomField.getText(),
                emailField.getText(),
                passwordField.getText(),
                telephoneField.getText(),
                Integer.parseInt(ageField.getText()),
                sexeBox.getValue(),
                Double.parseDouble(tailleField.getText()),
                Double.parseDouble(poidsField.getText()),
                handicapBox.isSelected(),
                "[\"" + dbUserRole + "\"]", 
                dbUserRole, 
                specialiteField.getText()
            );

            serviceUser.ajouter(user);
            System.out.println("User registered successfully!");
            navigateToLogin(event);
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
            
            stage.getScene().setRoot(root);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
