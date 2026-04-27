package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;
import org.example.home.controller.HomeController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class CycleCalendarController {

    @FXML private Label lblMonthYear;
    @FXML private GridPane daysHeaderGrid;
    @FXML private GridPane calendarGrid;

    private HomeController homeController;
    private YearMonth currentYearMonth;
    private final org.example.cycle.service.CycleService cycleService = new org.example.cycle.service.CycleService();
    private final org.example.cycle.service.CycleAnalysisService analysisService = new org.example.cycle.service.CycleAnalysisService();

    private int getCurrentUserId() {
        org.example.user.model.User currentUser = org.example.utils.SessionManager.getCurrentUser();
        return currentUser != null ? currentUser.getId() : -1;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        setupDaysHeader();
        refreshCalendar();
    }

    private void setupDaysHeader() {
        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < days.length; i++) {
            Label lbl = new Label(days[i]);
            lbl.getStyleClass().add("calendar-day-header");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(javafx.geometry.Pos.CENTER);
            GridPane.setHgrow(lbl, Priority.ALWAYS);
            daysHeaderGrid.add(lbl, i, 0);
        }
    }

    public void refreshCalendar() {
        calendarGrid.getChildren().clear();
        
        lblMonthYear.setText(currentYearMonth.getMonth().name() + " " + currentYearMonth.getYear());

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekOfFirst = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        int daysInMonth = currentYearMonth.lengthOfMonth();

        List<Cycle> userCycles = cycleService.getCyclesByUserId(getCurrentUserId());
        List<org.example.cycle.model.CycleAnalysis> analyses = analysisService.analyzeAllUserCycles(userCycles);

        int row = 0;
        int col = dayOfWeekOfFirst - 1;

        // Fill empty slots before 1st day
        for (int i = 0; i < col; i++) {
            VBox emptyBox = new VBox();
            emptyBox.getStyleClass().add("calendar-cell-empty");
            emptyBox.setPrefHeight(100);
            emptyBox.setPrefWidth(120);
            calendarGrid.add(emptyBox, i, row);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if (col > 6) {
                col = 0;
                row++;
            }

            LocalDate currentDate = currentYearMonth.atDay(day);
            VBox dayBox = new VBox();
            dayBox.getStyleClass().add("calendar-cell");
            dayBox.setPrefHeight(100);
            dayBox.setPrefWidth(120);
            
            Label lblDay = new Label(String.valueOf(day));
            lblDay.getStyleClass().add("calendar-day-number");
            dayBox.getChildren().add(lblDay);

            // Check if there is a cycle on this date
            String dayState = analysisService.getDayState(currentDate, analyses);

            if (dayState.equals("MENSTRUATION")) {
                dayBox.getStyleClass().add("cycle-active-day");
                Label lblCycleInfo = new Label("🩸 Règles");
                lblCycleInfo.getStyleClass().add("cycle-label");
                dayBox.getChildren().add(lblCycleInfo);
            } else if (dayState.equals("OVULATION")) {
                dayBox.getStyleClass().add("ovulation-day");
                Label lblOvu = new Label("🌹 Ovulation");
                lblOvu.getStyleClass().add("ovulation-label");
                dayBox.getChildren().add(lblOvu);
            } else if (dayState.equals("FERTILE")) {
                dayBox.getStyleClass().add("fertile-day");
                Label lblFertile = new Label("🩵 Fertile");
                lblFertile.getStyleClass().add("fertile-label");
                dayBox.getChildren().add(lblFertile);
            }

            Cycle activeCycle = analysisService.getCycleForDate(currentDate, userCycles);

            // Click interaction
            dayBox.setOnMouseClicked(e -> showCycleDialog(currentDate, activeCycle));

            calendarGrid.add(dayBox, col, row);
            col++;
        }
        
        // Fill remaining empty slots at end of month
        while (row < 6) {
           if (col > 6) {
               col = 0;
               row++;
               if (row > 5) break; 
           }
           VBox emptyBox = new VBox();
           emptyBox.getStyleClass().add("calendar-cell-empty");
           emptyBox.setPrefHeight(100);
           emptyBox.setPrefWidth(120);
           calendarGrid.add(emptyBox, col, row);
           col++;
        }
    }

    @FXML
    private void prevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        refreshCalendar();
    }

    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        refreshCalendar();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/DisplayCycle.fxml"));
            Parent view = loader.load();
            DisplayCycleController controller = loader.getController();
            controller.setHomeController(homeController);
            if (homeController != null) {
                homeController.setContent(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showCycleDialog(LocalDate clickedDate, Cycle cycle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/CycleSettingsDialog.fxml"));
            Parent root = loader.load();

            CycleSettingsDialogController controller = loader.getController();
            controller.initData(clickedDate, cycle, getCurrentUserId());

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(cycle == null ? "Ajouter un Cycle" : "Gérer le Cycle");
            dialogStage.setScene(new Scene(root));
            
            // Wait for dialog to close
            dialogStage.showAndWait();
            
            // Refresh grid if changes were made
            if (controller.isModified()) {
                refreshCalendar();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
