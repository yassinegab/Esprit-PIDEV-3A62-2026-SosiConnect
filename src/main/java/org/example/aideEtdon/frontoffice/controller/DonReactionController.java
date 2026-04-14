package org.example.aideEtdon.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.aideEtdon.model.Demande;
import org.example.aideEtdon.model.Don;
import org.example.aideEtdon.service.DonService;

import java.io.IOException;

public class DonReactionController {

    @FXML private Label demandeTitleLabel;
    @FXML private Label demandeDescLabel;
    @FXML private TextArea messageArea;
    @FXML private Label errorLabel;

    private Demande demande;
    private DonService donService;

    @FXML
    public void initialize() {
        donService = new DonService();
    }

    public void initData(Demande d) {
        this.demande = d;
        demandeTitleLabel.setText(d.getTitre());
        demandeDescLabel.setText(d.getDescription());
    }

    @FXML
    public void handleValider() {
        String msg = messageArea.getText().trim();
        if (msg.isEmpty()) {
            errorLabel.setVisible(true);
            return;
        }

        int currentDonorId = 2; // Hardcoded requirement
        Don reaction = new Don(demande.getId(), currentDonorId, msg);

        try {
            donService.ajouter(reaction);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Votre proposition de don a été enregistrée avec succès !");
            alert.showAndWait();
            
            handleRetour();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur base de données.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    public void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/aideEtdon/frontoffice/DemandeListView.fxml"));
            Parent root = loader.load();
            AideEtdonControllerClientController.getInstance().setView(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
