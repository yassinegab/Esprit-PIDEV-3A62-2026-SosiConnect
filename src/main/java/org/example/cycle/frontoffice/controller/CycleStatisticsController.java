package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;
import org.example.home.controller.HomeController;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class CycleStatisticsController {

    @FXML private Label lblTotalCycles;
    @FXML private Label lblAvgDuration;
    @FXML private Label lblShortest;
    @FXML private Label lblLongest;

    private final CycleService cycleService = new CycleService();
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
            lblAvgDuration.setText("0j");
            lblShortest.setText("0j");
            lblLongest.setText("0j");
            return;
        }

        long totalCycles = cycles.size();
        long totalDays = 0;
        long shortest = Long.MAX_VALUE;
        long longest = Long.MIN_VALUE;

        for (Cycle c : cycles) {
            long days = ChronoUnit.DAYS.between(c.getDate_debut_m().toLocalDate(), c.getDate_fin_m().toLocalDate());
            totalDays += days;

            if (days < shortest) shortest = days;
            if (days > longest) longest = days;
        }

        long avg = totalDays / totalCycles;

        lblTotalCycles.setText(String.valueOf(totalCycles));
        lblAvgDuration.setText(avg + "j");
        lblShortest.setText(shortest + "j");
        lblLongest.setText(longest + "j");
    }
}
