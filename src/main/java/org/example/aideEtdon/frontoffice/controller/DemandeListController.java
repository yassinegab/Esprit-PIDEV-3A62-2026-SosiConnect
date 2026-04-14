package org.example.aideEtdon.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.aideEtdon.model.Demande;
import org.example.aideEtdon.service.DemandeService;

import java.io.IOException;
import java.util.List;

public class DemandeListController {

    @FXML private FlowPane cardsContainer;
    private DemandeService demandeService;

    @FXML
    public void initialize() {
        demandeService = new DemandeService();
        loadDemandes();
    }

    private void loadDemandes() {
        try {
            List<Demande> demandes = demandeService.afficher();
            cardsContainer.getChildren().clear();

            for (Demande d : demandes) {
                VBox card = createCard(d);
                cardsContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createCard(Demande d) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(300);
        card.setPrefHeight(250);

        boolean isUrgent = "Urgent".equalsIgnoreCase(d.getUrgence());
        String borderColor = isUrgent ? "#ef4444" : "transparent"; 
        String dropShadow = "dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5)";
        
        card.getStyleClass().add("modern-card");
        if (isUrgent) {
            card.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 16;");
        }

        Label title = new Label(d.getTitre());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label typeAndDesc = new Label("Type: " + d.getType() + "\n\n" + d.getDescription());
        typeAndDesc.setWrapText(true);
        typeAndDesc.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px;");
        typeAndDesc.setMaxHeight(80);

        Label badge = new Label(d.getUrgence());
        if (isUrgent) {
            badge.getStyleClass().add("badge-urgent");
        } else {
            badge.getStyleClass().add("badge-normal");
        }

        HBox topArea = new HBox(title, new Region(), badge);
        HBox.setHgrow(topArea.getChildren().get(1), Priority.ALWAYS);

        Button btnReact = new Button("Faire un don");
        btnReact.getStyleClass().add("primary-button");
        btnReact.setOnAction(e -> handleReact(d));

        VBox bottomArea = new VBox(btnReact);
        bottomArea.setAlignment(Pos.CENTER);
        
        card.getChildren().addAll(topArea, typeAndDesc, new Region(), bottomArea);
        VBox.setVgrow(card.getChildren().get(2), Priority.ALWAYS);

        return card;
    }

    private void handleReact(Demande d) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/aideEtdon/frontoffice/DonReactionView.fxml"));
            Parent root = loader.load();
            
            DonReactionController controller = loader.getController();
            controller.initData(d);

            AideEtdonControllerClientController.getInstance().setView(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRetour() {
        AideEtdonControllerClientController.getInstance().showDons();
    }
}
