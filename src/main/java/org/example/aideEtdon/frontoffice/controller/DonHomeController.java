package org.example.aideEtdon.frontoffice.controller;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class DonHomeController {

    @FXML
    private VBox cardCreate;

    @FXML
    private VBox cardView;

    @FXML
    private VBox cardVideo;

    @FXML
    public void initialize() {
        setupCardHoverAnimation(cardCreate);
        setupCardHoverAnimation(cardView);
        setupCardHoverAnimation(cardVideo);
    }

    private void setupCardHoverAnimation(VBox card) {
        card.setOnMouseEntered(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
            scaleTransition.play();
        });

        card.setOnMouseExited(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
    }

    @FXML
    public void goToCreateDemande() {
        navigateTo("/aideEtdon/frontoffice/DemandeFormView.fxml");
    }

    @FXML
    public void goToViewDemandes() {
        navigateTo("/aideEtdon/frontoffice/DemandeListView.fxml");
    }

    @FXML
    public void goToVideos() {
        navigateTo("/aideEtdon/frontoffice/VideoListView.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            AideEtdonControllerClientController.getInstance().setView(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
