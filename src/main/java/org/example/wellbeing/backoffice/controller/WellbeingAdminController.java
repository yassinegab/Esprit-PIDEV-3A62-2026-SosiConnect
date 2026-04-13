package org.example.wellbeing.backoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.wellbeing.model.UserWellBeingData;
import org.example.wellbeing.service.WellbeingService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class WellbeingAdminController {

    @FXML private Label lblTotalUsers;
    @FXML private Label lblHighAnxiety;
    @FXML private Label lblAvgSleep;
    @FXML private Label lblAvgHeartbeat;
    @FXML private Label lblCriticalActive;

    @FXML private LineChart<String, Double> chartTrend;
    @FXML private PieChart chartDistribution;

    @FXML private TableView<UserWellBeingData> tblData;
    @FXML private TableColumn<UserWellBeingData, Integer> colId;
    @FXML private TableColumn<UserWellBeingData, Double> colWork; // Repurposed for stress score
    @FXML private TableColumn<UserWellBeingData, Integer> colAnxiety;
    @FXML private TableColumn<UserWellBeingData, String> colCreated;
    @FXML private TableColumn<UserWellBeingData, Void> colActions;

    private WellbeingService wellbeingService = new WellbeingService();
    private org.example.wellbeing.service.ChatbotMessageService messageService = new org.example.wellbeing.service.ChatbotMessageService();
    private boolean isFiltered = false;
    private List<UserWellBeingData> allData;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colWork.setCellValueFactory(cellData -> {
            UserWellBeingData d = cellData.getValue();
            double avg = (d.getAnxietyTension() + d.getSleepProblems() + d.getHeadaches() + d.getRestlessness()) / 4.0;
            return new javafx.beans.property.SimpleDoubleProperty(avg).asObject();
        });
        colAnxiety.setCellValueFactory(new PropertyValueFactory<>("anxietyTension"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnView = new Button("👁");
            private final Button btnAlert = new Button("🔔");
            private final HBox container = new HBox(8, btnView, btnAlert);

            {
                btnView.setStyle("-fx-background-color: transparent; -fx-text-fill: #6366f1; -fx-cursor: hand; -fx-font-size: 14;");
                btnAlert.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-font-size: 14;");
                
                btnView.setTooltip(new Tooltip("Détails de l'évaluation"));
                btnAlert.setTooltip(new Tooltip("Signaler une situation critique"));

                btnView.setOnAction(e -> {
                    UserWellBeingData data = getTableView().getItems().get(getIndex());
                    showDetailsPopup(data);
                });

                btnAlert.setOnAction(e -> {
                    UserWellBeingData data = getTableView().getItems().get(getIndex());
                    handleFlagAlert(data);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void loadData() {
        try {
            allData = wellbeingService.afficher();
            if (isFiltered) {
                applyCriticalFilter();
            } else {
                tblData.setItems(FXCollections.observableArrayList(allData));
            }

            updateStats(allData);
            loadCharts();
            
            List<UserWellBeingData> alerts = wellbeingService.getCriticalAlerts();
            lblCriticalActive.setText(String.valueOf(alerts.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showDetailsPopup(UserWellBeingData data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wellbeing/backoffice/WellbeingDetailPopup.fxml"));
            Parent root = loader.load();
            
            WellbeingDetailPopupController controller = loader.getController();
            controller.setData(data);
            
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED); // Modern look
            popupStage.setScene(new Scene(root));
            
            // Add slight shadow to the stage if possible, or handle via FXML root
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails.");
        }
    }

    private void handleFlagAlert(UserWellBeingData data) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Signalement de Situation");
        alert.setHeaderText("Souhaitez-vous signaler cette situation ?");
        alert.setContentText("Le système enverra une notification de suivi (via Chatbot) à l'utilisateur.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String alertMessage = "⚠️ ASSISTANCE ADMIN : Bonjour. Notre équipe a remarqué un niveau de stress élevé lors de votre dernière évaluation. Nous sommes là pour vous accompagner si vous souhaitez en discuter.";
                    org.example.wellbeing.model.ChatbotMessage msg = new org.example.wellbeing.model.ChatbotMessage(alertMessage, "assistant", data.getUser());
                    messageService.saveMessage(msg);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "L'utilisateur a été notifié via son assistant chatbot.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'envoyer la notification.");
                }
            }
        });
    }

    @FXML
    private void handleViewCriticalAlerts() {
        isFiltered = !isFiltered;
        if (isFiltered) {
            applyCriticalFilter();
        } else {
            tblData.setItems(FXCollections.observableArrayList(allData));
        }
    }

    private void applyCriticalFilter() {
        List<UserWellBeingData> critical = allData.stream()
            .filter(d -> ((d.getAnxietyTension() + d.getSleepProblems() + d.getHeadaches() + d.getRestlessness()) / 4.0) >= 4.0)
            .toList();
        tblData.setItems(FXCollections.observableArrayList(critical));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    private void loadCharts() {
        try {
            // Trend Chart
            Map<LocalDate, Double> trendData = wellbeingService.getAverageStressTrend();
            XYChart.Series<String, Double> trendSeries = new XYChart.Series<>();
            trendSeries.setName("Indice de Stress");
            
            for (Map.Entry<LocalDate, Double> entry : trendData.entrySet()) {
                trendSeries.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
            }
            chartTrend.getData().clear();
            chartTrend.getData().add(trendSeries);

            // Distribution Chart
            Map<String, Integer> distData = wellbeingService.getStressDistribution();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : distData.entrySet()) {
                pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            chartDistribution.setData(pieData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStats(List<UserWellBeingData> dataList) {
        if (dataList == null || dataList.isEmpty()) return;
        
        long totalUsers = dataList.size();
        long highAnxietyCount = dataList.stream().filter(d -> d.getAnxietyTension() >= 4).count();
        double avgSleep = dataList.stream().mapToInt(UserWellBeingData::getSleepProblems).average().orElse(0);
        double avgHeart = dataList.stream().mapToInt(UserWellBeingData::getHeartbeatPalpitations).average().orElse(0);

        lblTotalUsers.setText(String.valueOf(totalUsers));
        lblHighAnxiety.setText(String.valueOf(highAnxietyCount));
        lblAvgSleep.setText(String.format("%.2f", avgSleep));
        lblAvgHeartbeat.setText(String.format("%.2f", avgHeart));
    }

    @FXML
    private void handleApplyFilter() {
        loadData();
    }
}
