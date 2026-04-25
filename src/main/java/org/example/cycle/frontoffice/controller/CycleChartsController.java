package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.cycle.model.Cycle;
import org.example.cycle.model.CycleAnalysis;
import org.example.cycle.model.Symptome;
import org.example.cycle.model.TypeSymptome;
import org.example.cycle.service.CycleAnalysisService;
import org.example.cycle.service.CycleService;
import org.example.cycle.service.SymptomeService;
import org.example.home.controller.HomeController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CycleChartsController {

    @FXML private LineChart<String, Number> cycleLineChart;
    @FXML private BarChart<String, Number> menstruationBarChart;
    @FXML private PieChart regularityPieChart;
    @FXML private PieChart symptomePieChart;

    private final CycleService cycleService = new CycleService();
    private final CycleAnalysisService analysisService = new CycleAnalysisService();
    private final SymptomeService symptomeService = new SymptomeService();
    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        populateCharts();
    }

    private void populateCharts() {
        org.example.user.model.User currentUser = org.example.utils.SessionManager.getCurrentUser();
        if (currentUser == null) return;
        List<Cycle> cycles = cycleService.getCyclesByUserId(currentUser.getId());
        List<CycleAnalysis> analyses = analysisService.analyzeAllUserCycles(cycles);

        XYChart.Series<String, Number> cycleSeries = new XYChart.Series<>();
        cycleSeries.setName("Durée Cycle (Jours)");
        
        XYChart.Series<String, Number> mensSeries = new XYChart.Series<>();
        mensSeries.setName("Durée Règles (Jours)");
        
        int regCount = 0;
        int irregCount = 0;

        for (int i = 0; i < analyses.size(); i++) {
            CycleAnalysis ca = analyses.get(i);
            String cycleLabel = "Cycle " + (i + 1);
            
            mensSeries.getData().add(new XYChart.Data<>(cycleLabel, ca.getMenstruationDuration()));
            
            if (ca.getCycleDuration() != -1) {
                cycleSeries.getData().add(new XYChart.Data<>(cycleLabel, ca.getCycleDuration()));
                if (ca.isIrregular()) irregCount++;
                else regCount++;
            }
        }
        
        cycleLineChart.getData().clear();
        cycleLineChart.getData().add(cycleSeries);
        
        menstruationBarChart.getData().clear();
        menstruationBarChart.getData().add(mensSeries);

        // Regularity Pie Chart
        ObservableList<PieChart.Data> regData = FXCollections.observableArrayList(
                new PieChart.Data("Réguliers", regCount),
                new PieChart.Data("Irréguliers", irregCount)
        );
        regularityPieChart.setData(regData);

        // Symptome Pie Chart
        try {
            Map<TypeSymptome, Integer> distribution = new HashMap<>();
            for (Cycle c : cycles) {
                List<Symptome> symptomes = symptomeService.getSymptomesByCycleId(c.getCycle_id());
                for (Symptome s : symptomes) {
                    distribution.put(s.getType(), distribution.getOrDefault(s.getType(), 0) + 1);
                }
            }

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<TypeSymptome, Integer> entry : distribution.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey().name(), entry.getValue()));
            }

            symptomePieChart.setData(pieChartData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
