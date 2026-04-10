package org.example.user.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

import org.example.user.service.ServiceUser;
import org.example.user.model.User;
import org.example.home.controller.HomeController;
import org.example.backoffice.controller.AdminBaseController;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill in all fields.");
            return;
        }

        try {
            User user = serviceUser.login(email, password);
            if (user != null) {
                System.out.println("Login Successful: " + user.getNom());
                org.example.utils.SessionManager.setCurrentUser(user);
                navigateAfterLogin(user);
            } else {
                System.out.println("Invalid email or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void navigateAfterLogin(User user) {
        try {
            String fxmlPath = "/home/Home.fxml";
            boolean isAdmin = "ROLE_ADMIN".equals(user.getUser_role());
            
            if (isAdmin) {
                fxmlPath = "/backoffice/AdminLayout.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (!isAdmin) {
                HomeController homeController = loader.getController();
                homeController.setUser(user);
            } else {
                AdminBaseController adminController = loader.getController();
                // adminController.setUser(user);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating after login: " + e.getMessage());
        }
    }

    @FXML
    private void navigateToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Register.fxml"));
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
