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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import org.json.JSONObject;

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

    // CHATBOT FXML
    @FXML private VBox chatMessagesBox;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField txtChatInput;
    @FXML private Label lblTyping;
    @FXML private Button btnSendChat;

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
    @FXML private void goToEvents() {
        navigate("/event/frontoffice/EventFrontView.fxml");
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
            } else if (controller instanceof org.example.event.frontoffice.controller.EventFrontController) {
                ((org.example.event.frontoffice.controller.EventFrontController) controller).setHomeController(homeController);
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

    // --- CHATBOT LOGIC ---

    @FXML
    private void sendChatMessage() {
        String message = txtChatInput.getText().trim();
        if (message.isEmpty()) return;

        txtChatInput.clear();
        addUserMessage(message);
        
        lblTyping.setVisible(true);
        lblTyping.setManaged(true);
        btnSendChat.setDisable(true);
        txtChatInput.setDisable(true);

        // API Call via HttpClient
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("message", message);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8082/api/chat"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                String botReply;
                if (response.statusCode() == 200) {
                    JSONObject resJson = new JSONObject(response.body());
                    botReply = resJson.getString("response");
                } else {
                    botReply = "Désolé, je suis indisponible pour le moment.";
                }

                Platform.runLater(() -> {
                    addBotMessage(botReply);
                    lblTyping.setVisible(false);
                    lblTyping.setManaged(false);
                    btnSendChat.setDisable(false);
                    txtChatInput.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    addBotMessage("Erreur de connexion avec l'assistante.");
                    lblTyping.setVisible(false);
                    lblTyping.setManaged(false);
                    btnSendChat.setDisable(false);
                    txtChatInput.setDisable(false);
                });
            }
        }).start();
    }

    private void addUserMessage(String message) {
        Label lblMsg = new Label(message);
        lblMsg.setStyle("-fx-background-color: #c084fc; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 15 15 0 15; -fx-wrap-text: true; -fx-max-width: 400;");
        
        HBox hbox = new HBox(lblMsg);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        
        chatMessagesBox.getChildren().add(hbox);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        Label lblMsg = new Label(message);
        lblMsg.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-padding: 10 15; -fx-background-radius: 15 15 15 0; -fx-wrap-text: true; -fx-max-width: 400;");
        
        HBox hbox = new HBox(lblMsg);
        hbox.setAlignment(Pos.CENTER_LEFT);
        
        chatMessagesBox.getChildren().add(hbox);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatScrollPane.layout();
            chatScrollPane.setVvalue(1.0);
        });
    }
}