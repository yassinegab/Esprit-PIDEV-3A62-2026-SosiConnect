package org.example.aideEtdon.frontoffice.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

public class AideEtdonControllerClientController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnAides;

    @FXML
    private Button btnDons;

    private boolean isAideActive = false;
    private boolean isDonActive = false;

    private static AideEtdonControllerClientController instance;

    private final String ACTIVE_STYLE = "-fx-background-color: transparent; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3b82f6; -fx-border-color: #3b82f6; -fx-border-width: 0 0 3 0; -fx-cursor: hand; -fx-padding: 5 10;";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-border-color: transparent; -fx-border-width: 0 0 3 0; -fx-cursor: hand; -fx-padding: 5 10;";

    @FXML
    public void initialize() {
        instance = this;
        showAides();

        setupButtonHoverAnimation(btnAides);
        setupButtonHoverAnimation(btnDons);
    }

    public static AideEtdonControllerClientController getInstance() {
        return instance;
    }

    public void setView(Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
        applyAnimation(node);
    }

    private void setupButtonHoverAnimation(Button btn) {
        btn.setOnMouseEntered(e -> {
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
        });
        btn.setOnMouseExited(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });
    }

    private void applyAnimation(Node pane) {
        pane.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(400), pane);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(400), pane);
        translate.setFromY(20);
        translate.setToY(0);

        fade.play();
        translate.play();
    }

    @FXML
    public void showAides() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/aideEtdon/frontoffice/AideHomeView.fxml"));
            Node aideView = loader.load();
            setView(aideView);
            
            btnAides.setStyle(ACTIVE_STYLE);
            btnDons.setStyle(INACTIVE_STYLE);
            
            isAideActive = true;
            isDonActive = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showDons() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/aideEtdon/frontoffice/DonHomeView.fxml"));
            Node donView = loader.load();
            setView(donView);
            
            btnAides.setStyle(INACTIVE_STYLE);
            btnDons.setStyle(ACTIVE_STYLE);
            isDonActive = true;
            isAideActive = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
