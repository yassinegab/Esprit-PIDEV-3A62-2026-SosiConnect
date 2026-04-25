package org.example.cycle.frontoffice.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;

import java.time.LocalDate;

public class CycleSettingsDialogController {

    @FXML private Label lblTitle;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private Label lblError;
    @FXML private Button btnDelete;
    @FXML private Button btnSave;

    private final CycleService cycleService = new CycleService();
    private Cycle currentCycle;
    private int currentUserId;
    private boolean isModified = false;

    public void initData(LocalDate selectedDate, Cycle cycle, int userId) {
        this.currentUserId = userId;
        this.currentCycle = cycle;

        if (cycle != null) {
            lblTitle.setText("Modifier le Cycle");
            dpStart.setValue(cycle.getDate_debut_m().toLocalDate());
            dpEnd.setValue(cycle.getDate_fin_m().toLocalDate());
            btnDelete.setVisible(true);
        } else {
            lblTitle.setText("Ajouter un Cycle");
            dpStart.setValue(selectedDate);
            btnDelete.setVisible(false);
        }
    }

    public boolean isModified() {
        return isModified;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        if (start == null || end == null) {
            showError("Veuillez remplir toutes les dates.");
            return;
        }

        if (start.isAfter(end)) {
            showError("La date de début ne peut pas être supérieure à la date de fin.");
            return;
        }

        if (currentCycle != null) {
            // Update
            currentCycle.setDate_debut_m(java.sql.Date.valueOf(start));
            currentCycle.setDate_fin_m(java.sql.Date.valueOf(end));
            cycleService.updateCycle(currentCycle);
        } else {
            // Check overlapping if needed, but for now just add
            Cycle newCycle = new Cycle();
            newCycle.setUser_id(currentUserId);
            newCycle.setDate_debut_m(java.sql.Date.valueOf(start));
            newCycle.setDate_fin_m(java.sql.Date.valueOf(end));
            cycleService.addCycle(newCycle);
        }

        isModified = true;
        closeDialog();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (currentCycle != null) {
            cycleService.deleteCycle(currentCycle.getCycle_id());
            isModified = true;
            closeDialog();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    private void closeDialog() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}
