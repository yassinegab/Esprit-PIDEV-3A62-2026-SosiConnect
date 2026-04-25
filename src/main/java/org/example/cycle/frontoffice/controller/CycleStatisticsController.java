package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.cycle.model.Cycle;
import org.example.cycle.model.CycleAnalysis;
import org.example.cycle.service.CycleAnalysisService;
import org.example.cycle.service.CycleService;
import org.example.home.controller.HomeController;

import java.util.List;

public class CycleStatisticsController {

    @FXML private Label lblTotalCycles;
    @FXML private Label lblAvgDuration;
    @FXML private Label lblShortest;
    @FXML private Label lblLongest;
    @FXML private Label lblRegularity;
    @FXML private Label lblAvgPeriod;

    private final CycleService cycleService = new CycleService();
    private final CycleAnalysisService analysisService = new CycleAnalysisService();
    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        calculateStats();
    }

    private void calculateStats() {
        org.example.user.model.User currentUser = org.example.utils.SessionManager.getCurrentUser();
        if (currentUser == null) return;
        List<Cycle> cycles = cycleService.getCyclesByUserId(currentUser.getId());

        if (cycles.isEmpty()) {
            lblTotalCycles.setText("0");
            lblAvgDuration.setText("0 j");
            lblShortest.setText("0 j");
            lblLongest.setText("0 j");
            lblRegularity.setText("0%");
            lblAvgPeriod.setText("0 j");
            return;
        }
        
        List<CycleAnalysis> analyses = analysisService.analyzeAllUserCycles(cycles);

        long totalCycles = cycles.size();
        long shortest = Long.MAX_VALUE;
        long longest = Long.MIN_VALUE;

        for (CycleAnalysis ca : analyses) {
            long duration = ca.getCycleDuration();
            if (duration != -1) {
                if (duration < shortest) shortest = duration;
                if (duration > longest) longest = duration;
            }
        }
        
        if (shortest == Long.MAX_VALUE) shortest = 0;
        if (longest == Long.MIN_VALUE) longest = 0;

        lblTotalCycles.setText(String.valueOf(totalCycles));
        lblAvgDuration.setText(analysisService.getAverageCycleLength(cycles) + " j");
        lblShortest.setText(shortest + " j");
        lblLongest.setText(longest + " j");
        lblAvgPeriod.setText(analysisService.getAverageMenstruationLength(cycles) + " j");
        lblRegularity.setText(String.format("%.0f%%", analysisService.getRegularityRate(cycles)));
    }
}
