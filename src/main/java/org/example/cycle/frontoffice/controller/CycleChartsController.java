package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.cycle.model.Cycle;
import org.example.cycle.model.Symptome;
import org.example.cycle.model.TypeSymptome;
import org.example.cycle.service.CycleService;
import org.example.cycle.service.SymptomeService;
import org.example.home.controller.HomeController;

import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CycleChartsController {

    @FXML private BarChart<String, Number> cycleBarChart;
    @FXML private PieChart symptomePieChart;

    private final CycleService cycleService = new CycleService();
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
        // 1. Populate BarChart (Durations)
        List<Cycle> cycles = cycleService.getAllCycles();
        cycles.sort(Comparator.comparing(Cycle::getDate_debut_m));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Durée (Jours)");
        int cycleIndex = 1;
        
        for (Cycle c : cycles) {
            long days = ChronoUnit.DAYS.between(c.getDate_debut_m().toLocalDate(), c.getDate_fin_m().toLocalDate());
            series.getData().add(new XYChart.Data<>("Cycle " + cycleIndex, days));
            cycleIndex++;
        }
        cycleBarChart.getData().clear();
        cycleBarChart.getData().add(series);

        // 2. Populate PieChart (Symptoms)
        try {
            List<Symptome> symptomes = symptomeService.afficher(); // fetches all
            Map<TypeSymptome, Integer> distribution = new HashMap<>();

            for (Symptome s : symptomes) {
                distribution.put(s.getType(), distribution.getOrDefault(s.getType(), 0) + 1);
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
