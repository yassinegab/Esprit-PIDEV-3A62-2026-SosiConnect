package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;

import java.sql.Date;

public class EditCycleController {

    @FXML
    private DatePicker startPicker;

    @FXML
    private DatePicker endPicker;

    private Cycle cycle;
    private CycleService service = new CycleService();

    private DisplayCycleController parentController;
    private Stage stage;

    public void setCycle(Cycle cycle) {
        this.cycle = cycle;

        startPicker.setValue(cycle.getDate_debut_m().toLocalDate());
        endPicker.setValue(cycle.getDate_fin_m().toLocalDate());
    }

    public void setParentController(DisplayCycleController parentController) {
        this.parentController = parentController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleSave() {

        cycle.setDate_debut_m(Date.valueOf(startPicker.getValue()));
        cycle.setDate_fin_m(Date.valueOf(endPicker.getValue()));

        service.updateCycle(cycle);

        // 🔥 refresh list
        parentController.loadCycles();

        // 🔥 close window
        stage.close();
    }
}