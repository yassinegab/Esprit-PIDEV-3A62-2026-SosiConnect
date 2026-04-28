package org.example.event.frontoffice.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;
import org.example.event.model.Event;
import org.example.event.service.EventService;
import org.example.event.service.ExternalMailService;
import org.example.event.service.LocationService;
import org.example.home.controller.HomeController;
import org.example.utils.SessionManager;
import org.example.user.model.User;
import javafx.application.Platform;
import javafx.scene.control.Tooltip;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventFrontController {

    @FXML
    private FlowPane eventsContainer;
    @FXML
    private Label lblStatus;

    private HomeController homeController;
    private final EventService eventService = new EventService();
    private final ExternalMailService mailService = new ExternalMailService();

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        loadFutureEvents();
    }

    private void loadFutureEvents() {
        eventsContainer.getChildren().clear();

        try {
            List<Event> events = eventService.getFutureEvents();

            if (events.isEmpty()) {
                Label noEventsLbl = new Label("Aucun événement prévu pour le moment.");
                noEventsLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px; -fx-font-style: italic;");
                eventsContainer.getChildren().add(noEventsLbl);
                return;
            }

            for (Event event : events) {
                VBox card = createEventCard(event);
                eventsContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblStatus.setText("Erreur lors du chargement des événements.");
            lblStatus.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(350);
        card.setPadding(new Insets(20));
        
        // Style de base
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 8); -fx-translate-y: -5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-translate-y: 0;"));

        // En-tête (Type + Date)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        String icon = event.getType().equals("Campagne de sensibilisation") ? "📢" : (event.getType().equals("Recommandation saisonnière") ? "🌤️" : "📌");
        Label typeLabel = new Label(icon + " " + event.getType());
        typeLabel.setStyle("-fx-text-fill: #6366f1; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-color: #e0e7ff; -fx-padding: 5 10; -fx-background-radius: 15;");
        
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label dateLabel = new Label(event.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        header.getChildren().addAll(typeLabel, spacer, dateLabel);

        // Contenu
        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(event.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        descLabel.setWrapText(true);
        descLabel.setPrefHeight(60);

        // Bouton Participer
        Button btnParticipate = new Button("Participer");
        btnParticipate.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
        btnParticipate.setMaxWidth(Double.MAX_VALUE);
        
        btnParticipate.setOnAction(e -> {
            btnParticipate.setDisable(true);
            btnParticipate.setText("Traitement...");
            
            new Thread(() -> {
                User currentUser = SessionManager.getCurrentUser();
                if (currentUser != null && currentUser.getEmail() != null) {
                    String email = currentUser.getEmail();
                    String name = currentUser.getPrenom() != null ? currentUser.getPrenom() : "Utilisateur";
                    mailService.sendEventConfirmation(email, event, name);
                } else {
                    System.err.println("Aucun utilisateur connecté ou e-mail manquant. L'e-mail n'a pas pu être envoyé.");
                }
                
                Platform.runLater(() -> {
                    btnParticipate.setText("✅ Participation confirmée");
                    btnParticipate.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20;");
                    showSuccessMessage("Participation confirmée ! Vérifiez votre email.");
                });
            }).start();
        });

        HBox actionsContainer = new HBox(10);
        actionsContainer.setAlignment(Pos.CENTER);
        
        // Bouton Localisation
        if (event.getLocalisation() != null && !event.getLocalisation().trim().isEmpty()) {
            Button btnLocation = new Button("📍 Localisation");
            btnLocation.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
            btnLocation.setTooltip(new Tooltip("Voir sur la carte"));
            
            btnLocation.setOnAction(e -> {
                btnLocation.setDisable(true);
                btnLocation.setText("Recherche...");
                
                new Thread(() -> {
                    double[] coords = LocationService.getCoordinates(event.getLocalisation());
                    
                    Platform.runLater(() -> {
                        btnLocation.setDisable(false);
                        btnLocation.setText("📍 Localisation");
                        
                        if (coords != null) {
                            showMapModal(event.getLocalisation(), coords[0], coords[1]);
                        } else {
                            showSuccessMessage("Impossible de trouver cette adresse.");
                            lblStatus.setStyle("-fx-text-fill: #ef4444;");
                        }
                    });
                }).start();
            });
            
            HBox.setHgrow(btnParticipate, javafx.scene.layout.Priority.ALWAYS);
            actionsContainer.getChildren().addAll(btnParticipate, btnLocation);
        } else {
            actionsContainer.getChildren().add(btnParticipate);
        }

        card.getChildren().addAll(header, titleLabel, descLabel, actionsContainer);

        return card;
    }
    
    private void showMapModal(String address, double lat, double lng) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/frontoffice/MapDialog.fxml"));
            Parent root = loader.load();
            
            MapController controller = loader.getController();
            controller.initMap(address, lat, lng);
            
            Stage stage = new Stage();
            stage.setTitle("Localisation de l'événement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Impossible d'ouvrir la fenêtre de la carte.");
        }
    }

    private void showSuccessMessage(String msg) {
        lblStatus.setText(msg);
        lblStatus.setStyle("-fx-text-fill: #10b981;");
        
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> lblStatus.setText(""));
        pause.play();
    }
}
