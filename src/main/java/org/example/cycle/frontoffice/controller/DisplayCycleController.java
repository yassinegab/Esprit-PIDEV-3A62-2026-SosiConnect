package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleAnalysisService;
import org.example.cycle.service.CycleService;
import org.example.home.controller.HomeController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DisplayCycleController {

    @FXML private Label lblWelcome;
    @FXML private Label lblNextPeriod;
    @FXML private Label lblDaysRemaining;
    @FXML private Label lblOvulation;
    @FXML private Label lblFertile;
    
    @FXML private Label lblAvgCycle;
    @FXML private Label lblAvgMenstruation;
    @FXML private Label lblRegularity;
    
    @FXML private VBox alertBox;
    @FXML private Label lblAlertMsg;

    private HomeController homeController;
    private final CycleService cycleService = new CycleService();
    private final CycleAnalysisService analysisService = new CycleAnalysisService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        org.example.user.model.User currentUser = org.example.utils.SessionManager.getCurrentUser();
        if (currentUser == null) return;

        lblWelcome.setText("Bonjour " + currentUser.getPrenom() + " 👋, voici votre suivi menstruel.");
        
        List<Cycle> cycles = cycleService.getCyclesByUserId(currentUser.getId());
        
        if (cycles.isEmpty()) {
            lblNextPeriod.setText("Aucune donnée");
            lblDaysRemaining.setText("Ajoutez un cycle");
            lblOvulation.setText("-");
            lblFertile.setText("-");
            lblAvgCycle.setText("- j");
            lblAvgMenstruation.setText("- j");
            lblRegularity.setText("-");
            return;
        }

        // Calculations
        int avgCycle = analysisService.getAverageCycleLength(cycles);
        int avgPeriod = analysisService.getAverageMenstruationLength(cycles);
        double regularity = analysisService.getRegularityRate(cycles);
        
        LocalDate nextPeriod = analysisService.predictNextPeriod(cycles);
        LocalDate today = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(today, nextPeriod);
        
        LocalDate ovulation = nextPeriod.minusDays(14);
        LocalDate fertileStart = ovulation.minusDays(5);
        LocalDate fertileEnd = ovulation.plusDays(1);

        // UI Updates
        lblAvgCycle.setText(avgCycle + " j");
        lblAvgMenstruation.setText(avgPeriod + " j");
        lblRegularity.setText(String.format("%.0f%%", regularity));
        
        lblNextPeriod.setText(nextPeriod.format(formatter));
        lblOvulation.setText(ovulation.format(formatter));
        lblFertile.setText(fertileStart.format(formatter) + " - " + fertileEnd.format(formatter));
        
        if (daysDiff == 0) {
            lblDaysRemaining.setText("Aujourd'hui !");
        } else if (daysDiff < 0) {
            lblDaysRemaining.setText("En retard de " + Math.abs(daysDiff) + " jours");
        } else {
            lblDaysRemaining.setText("Dans " + daysDiff + " jours");
        }

        // Alert
        if (regularity < 70.0) { // If many cycles are irregular
            alertBox.setVisible(true);
            alertBox.setManaged(true);
        } else {
            alertBox.setVisible(false);
            alertBox.setManaged(false);
        }
    }

    @FXML private void goToAddCycle() {
        navigate("/cycle/frontoffice/CycleClientView.fxml");
    }
    @FXML private void goToStats() {
        navigate("/cycle/frontoffice/CycleStatistics.fxml");
    }
    @FXML private void goToCharts() {
        navigate("/cycle/frontoffice/CycleCharts.fxml");
    }
    @FXML private void goToHistory() {
        navigate("/cycle/frontoffice/CycleHistory.fxml");
    }
    @FXML private void goToCalendar() {
        navigate("/cycle/frontoffice/CycleCalendarView.fxml");
    }
    @FXML private void goToSymptomes() {
        navigate("/cycle/frontoffice/display_symptome.fxml");
    }

    private void navigate(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            // Re-bind controller if exists
            Object controller = loader.getController();
            if (controller instanceof ClientCycleController) {
                ((ClientCycleController) controller).setHomeController(homeController);
            } else if (controller instanceof CycleStatisticsController) {
                ((CycleStatisticsController) controller).setHomeController(homeController);
            } else if (controller instanceof CycleChartsController) {
                ((CycleChartsController) controller).setHomeController(homeController);
            } else if (controller instanceof CycleHistoryController) {
                ((CycleHistoryController) controller).setHomeController(homeController);
            } else if (controller instanceof CycleCalendarController) {
                ((CycleCalendarController) controller).setHomeController(homeController);
            } else if (controller instanceof DisplaySymptomeController) {
                ((DisplaySymptomeController) controller).setHomeController(homeController);
            }

            if (homeController != null) {
                homeController.setContent(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToPdf() {
        org.example.user.model.User currentUser = org.example.utils.SessionManager.getCurrentUser();
        if (currentUser == null) {
            org.example.utils.AlertHelper.showErrorAlert("Erreur", "Veuillez vous connecter pour exporter vos données.");
            return;
        }
        
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Rapport_Cycles.pdf");

        java.io.File destFile = fileChooser.showSaveDialog(lblWelcome.getScene().getWindow());

        if (destFile != null) {
            try {
                org.example.cycle.service.PdfExportService.exportCyclesToPdf(
                        destFile,
                        new CycleService().getCyclesByUserId(currentUser.getId()),
                        new org.example.cycle.service.SymptomeService()
                );
                org.example.utils.AlertHelper.showSuccessAlert("Export Réussi", "Le rapport PDF a été sauvegardé avec succès.");
            } catch (Exception e) {
                org.example.utils.AlertHelper.showErrorAlert("Erreur Export", "Échec de l'export: " + e.getMessage());
            }
        }
    }
}