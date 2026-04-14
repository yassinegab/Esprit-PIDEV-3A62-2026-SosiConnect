package org.example.aideEtdon.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.aideEtdon.model.Demande;
import org.example.aideEtdon.service.DemandeService;

import java.io.IOException;

public class DemandeFormController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField groupeSanguinField;
    @FXML private TextField organeField;
    @FXML private ComboBox<String> urgenceBox;
    
    @FXML private VBox groupeSanguinContainer;
    @FXML private VBox organeContainer;
    @FXML private Label errorLabel;

    private DemandeService demandeService;

    @FXML
    public void initialize() {
        demandeService = new DemandeService();

        // Listen for changes in Type to show/hide dynamic fields
        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Sang".equals(newVal)) {
                groupeSanguinContainer.setVisible(true);
                groupeSanguinContainer.setManaged(true);
                organeContainer.setVisible(false);
                organeContainer.setManaged(false);
                organeField.clear();
            } else if ("Organe".equals(newVal)) {
                organeContainer.setVisible(true);
                organeContainer.setManaged(true);
                groupeSanguinContainer.setVisible(false);
                groupeSanguinContainer.setManaged(false);
                groupeSanguinField.clear();
            } else {
                groupeSanguinContainer.setVisible(false);
                groupeSanguinContainer.setManaged(false);
                organeContainer.setVisible(false);
                organeContainer.setManaged(false);
                groupeSanguinField.clear();
                organeField.clear();
            }
        });
    }

    @FXML
    public void handleValider() {
        errorLabel.setVisible(false);
        String titre = titreField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String type = typeBox.getValue();
        String urgence = urgenceBox.getValue();

        // Validation
        if (titre.isEmpty() || desc.isEmpty() || type == null || urgence == null) {
            showError("Veuillez remplir tous les champs obligatoires (Titre, Description, Type, Urgence).");
            return;
        }

        String groupe = null;
        String organe = null;

        if ("Sang".equals(type)) {
            groupe = groupeSanguinField.getText().trim();
            if (groupe.isEmpty()) {
                showError("Veuillez spécifier le groupe sanguin.");
                return;
            }
        } else if ("Organe".equals(type)) {
            organe = organeField.getText().trim();
            if (organe.isEmpty()) {
                showError("Veuillez spécifier l'organe.");
                return;
            }
        }

        // Create demande Object
        int currentUserId = 1; // Hardcoded requirement
        Demande d = new Demande(titre, desc, type, groupe, organe, urgence, currentUserId);

        try {
            demandeService.ajouter(d);
            
            // Success, navigate back to home
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Votre demande a été créée avec succès !");
            alert.showAndWait();
            
            handleRetour();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur base de données: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    @FXML
    public void handleRetour() {
        AideEtdonControllerClientController.getInstance().showDons();
    }
}
