package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import org.example.cycle.model.Cycle;
import org.example.cycle.model.Symptome;
import org.example.cycle.service.CycleService;
import org.example.cycle.service.SymptomeService;
import org.example.home.controller.HomeController;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

public class CycleHistoryController {

    @FXML private VBox historyContainer;

    private final CycleService cycleService = new CycleService();
    private final SymptomeService symptomeService = new SymptomeService();
    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        loadHistory();
    }

    private void loadHistory() {
        historyContainer.getChildren().clear();

        List<Cycle> cycles = cycleService.getAllCycles();

        cycles.sort(Comparator.comparing(Cycle::getDate_debut_m));

        if (cycles.isEmpty()) {
            Label noData = new Label("Aucun cycle enregistré.");
            noData.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
            historyContainer.getChildren().add(noData);
            return;
        }

        for (int i = 0; i < cycles.size(); i++) {
            Cycle c = cycles.get(i);
            long days = ChronoUnit.DAYS.between(c.getDate_debut_m().toLocalDate(), c.getDate_fin_m().toLocalDate());
            List<Symptome> symptoms;
            try {
                symptoms = symptomeService.getSymptomesByCycleId(c.getCycle_id());
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                symptoms = java.util.Collections.emptyList();
            }


            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setSpacing(20);
            row.setPadding(new Insets(15, 25, 15, 25));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
            row.setPrefWidth(800);


            Label idxLbl = new Label("#" + (i + 1));
            idxLbl.setStyle("-fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #cbd5e1;");


            VBox datesBox = new VBox(5);
            Label startLbl = new Label("Début: " + c.getDate_debut_m());
            startLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
            Label endLbl = new Label("Fin: " + c.getDate_fin_m());
            endLbl.setStyle("-fx-text-fill: #64748b;");
            datesBox.getChildren().addAll(startLbl, endLbl);
            datesBox.setPrefWidth(200);


            Label durLbl = new Label(days + " jours");
            durLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff4d6d; -fx-font-size: 16px;");
            durLbl.setPrefWidth(150);


            Label sympLbl = new Label(symptoms.size() + " symptôme(s)");
            sympLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #3b82f6;");
            
            row.getChildren().addAll(idxLbl, datesBox, durLbl, sympLbl);
            historyContainer.getChildren().add(row);
        }
    }
}
