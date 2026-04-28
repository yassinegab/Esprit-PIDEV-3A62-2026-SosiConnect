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
import org.example.user.model.UserRole;
import java.io.IOException;
import java.sql.SQLException;

import org.example.user.model.User;
import org.example.user.service.ServiceUser;


public class RegisterController {

    @FXML private TextField       nomField;
    @FXML private TextField       prenomField;
    @FXML private TextField       emailField;
    @FXML private PasswordField   passwordField;
    @FXML private TextField       telephoneField;
    @FXML private TextField       ageField;
    @FXML private ChoiceBox<String> sexeBox;
    @FXML private TextField       tailleField;
    @FXML private TextField       poidsField;
    @FXML private ChoiceBox<String> roleBox;
    @FXML private CheckBox        handicapBox;
    @FXML private VBox            specialiteContainer;
    @FXML private TextField       specialiteField;
    @FXML private Button          registerButton;

    private final ServiceUser serviceUser = new ServiceUser();

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
        // Validation basique
        if (nomField.getText().isBlank() || prenomField.getText().isBlank()
                || emailField.getText().isBlank() || passwordField.getText().isBlank()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            // ✅ Mapper le choix UI → enum UserRole
            UserRole role = "Professionnel".equals(roleBox.getValue())
                    ? UserRole.MEDECIN    // adapte selon tes valeurs d'enum
                    : UserRole.PATIENT;

            // ✅ Handicap : "Aucun" si non coché, sinon texte du champ spécialité
            String handicap = handicapBox.isSelected() ? "Oui" : "Aucun";

            // ✅ Constructeur qui correspond à mapRow() dans ServiceUser :
            //    (id, nom, prenom, email, password, telephone, UserRole,
            //     age, sexe, poids, taille, handicap, dateCreation, derniereMiseAJour)
            //    → pour la création on utilise le constructeur sans id ni dates
            // ✅ Constructeur complet — id=0 (auto-généré par la DB), dates=null
            User user = new User(
                    0,                                           // id — auto-incrémenté
                    nomField.getText().trim(),                   // nom
                    prenomField.getText().trim(),                // prenom
                    emailField.getText().trim(),                 // email
                    passwordField.getText(),                     // password (hashé dans ServiceUser.add)
                    telephoneField.getText().trim(),             // telephone
                    role,                                        // UserRole enum
                    Integer.parseInt(ageField.getText().trim()), // age
                    sexeBox.getValue(),                          // sexe
                    Double.parseDouble(poidsField.getText().trim()),   // poids
                    Double.parseDouble(tailleField.getText().trim()),   // taille
                    handicap,                                    // handicap
                    null,                                        // dateCreation — gérée par la DB
                    null                                         // derniereMiseAJour — gérée par la DB
            );

            serviceUser.add(user);
            System.out.println("Utilisateur enregistre avec succes !");
            navigateToLogin(event);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Age, taille et poids doivent etre des nombres valides.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur base de donnees", e.getMessage());
        }
    }

    @FXML
    private void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/user/Login.fxml"));
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}