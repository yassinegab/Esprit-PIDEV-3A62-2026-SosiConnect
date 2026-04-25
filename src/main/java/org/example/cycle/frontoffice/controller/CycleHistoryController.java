package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.example.cycle.model.Cycle;
import org.example.cycle.model.CycleAnalysis;
import org.example.cycle.service.CycleAnalysisService;
import org.example.cycle.service.CycleService;
import org.example.home.controller.HomeController;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CycleHistoryController {

    @FXML private VBox historyContainer;

    private final CycleService cycleService = new CycleService();
    private final CycleAnalysisService analysisService = new CycleAnalysisService();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");

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

        org.example.user.model.User currentUser = org.example.utils.SessionManager.getCurrentUser();
        if (currentUser == null) return;
        List<Cycle> cycles = cycleService.getCyclesByUserId(currentUser.getId());
        
        List<CycleAnalysis> analyses = analysisService.analyzeAllUserCycles(cycles);

        if (analyses.isEmpty()) {
            Label noData = new Label("Aucun cycle enregistré.");
            noData.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
            historyContainer.getChildren().add(noData);
            return;
        }

        // Loop backwards to show newest first!
        for (int i = analyses.size() - 1; i >= 0; i--) {
             CycleAnalysis analysis = analyses.get(i);
             Cycle c = analysis.getCycle();
             
             // Main Card
             HBox card = new HBox();
             card.setAlignment(Pos.CENTER_LEFT);
             card.setSpacing(25);
             card.setPadding(new Insets(20, 30, 20, 30));
             card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);");
             card.setMaxWidth(800);
             card.setPrefWidth(800);
             
             // 1. Status Indicator
             VBox statusBox = new VBox(5);
             statusBox.setAlignment(Pos.CENTER);
             statusBox.setPrefWidth(90);
             
             Label lblStatus = new Label(analysis.getStatus());
             if (analysis.getStatus().equals("Actuel")) {
                 lblStatus.setStyle("-fx-background-color: #ff6b81; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 12px;");
             } else {
                 lblStatus.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 12px;");
             }
             
             Label cycleIndex = new Label("Cycle #" + (i + 1));
             cycleIndex.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: bold; -fx-font-size: 12px;");
             
             statusBox.getChildren().addAll(lblStatus, cycleIndex);
             
             // 2. Dates Box
             VBox datesBox = new VBox(5);
             datesBox.setPrefWidth(180);
             Label startLbl = new Label("Saignements");
             startLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
             Label datesVal = new Label(c.getDate_debut_m().toLocalDate().format(fmt) + " - " + c.getDate_fin_m().toLocalDate().format(fmt));
             datesVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 15px;");
             datesBox.getChildren().addAll(startLbl, datesVal);
             
             // 3. Durations Box
             VBox durationBox = new VBox(5);
             durationBox.setPrefWidth(150);
             Label hdDuration = new Label("Durée");
             hdDuration.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
             
             String totalDurationText = analysis.getCycleDuration() == -1 ? "En cours" : analysis.getCycleDuration() + " j";
             Label valDuration = new Label(totalDurationText + " (" + analysis.getMenstruationDuration() + "j règles)");
             valDuration.setStyle("-fx-font-weight: bold; -fx-text-fill: #3b82f6; -fx-font-size: 14px;");
             
             durationBox.getChildren().addAll(hdDuration, valDuration);
             
             // 4. Ovulation Box
             VBox ovuBox = new VBox(5);
             ovuBox.setPrefWidth(200);
             Label ovuLbl = new Label("Ovulation estimée");
             ovuLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
             Label ovuVal = new Label("🩷 " + analysis.getOvulationDate().format(fmt));
             ovuVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
             ovuBox.getChildren().addAll(ovuLbl, ovuVal);
             
             // Push to right
             Pane spacer = new Pane();
             HBox.setHgrow(spacer, Priority.ALWAYS);
             
             card.getChildren().addAll(statusBox, datesBox, durationBox, ovuBox, spacer);
             
             // Warning Icon if irregular
             if (analysis.isIrregular()) {
                 Label lblWarn = new Label("⚠️");
                 lblWarn.setStyle("-fx-font-size: 20px;");
                 card.getChildren().add(lblWarn);
             }
             
             historyContainer.getChildren().add(card);
        }
    }
}
