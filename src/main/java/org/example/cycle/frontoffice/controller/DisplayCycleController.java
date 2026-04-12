package org.example.cycle.frontoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;

    public class DisplayCycleController {

        @FXML
        private VBox cycleContainer;

        private ObservableList<Cycle> cycles = FXCollections.observableArrayList();

        @FXML
        public void initialize() {

            CycleService service = new CycleService();

            // 1. GET DATA FROM DATABASE
            cycles.addAll(service.getAllCycles());

            // 2. CREATE CARDS
            for (Cycle c : cycles) {

                VBox card = new VBox();
                card.setSpacing(5);

                // STYLE CARD
                card.setStyle("""
                -fx-background-color: white;
                -fx-padding: 15;
                -fx-border-color: #ccc;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
            """);

                // CREATE LABELS
                Label start = new Label("Start: " + c.getDate_debut_m());
                Label end = new Label("End: " + c.getDate_fin_m());
                Label user = new Label("User ID: " + c.getUser_id());

                card.getChildren().addAll(start, end, user);

                // ADD CARD TO SCREEN
                cycleContainer.getChildren().add(card);
            }
        }
    }

