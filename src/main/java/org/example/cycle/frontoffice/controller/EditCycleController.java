package org.example.cycle.frontoffice.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;
import org.example.home.controller.HomeController;

import java.io.IOException;
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
    private HomeController homeController;
    @FXML private AnchorPane root;

    @FXML
    public void initialize() {
        root.setOpacity(0);

        FadeTransition ft = new FadeTransition(Duration.millis(400), root);
        ft.setToValue(1);
        ft.play();
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

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
        if (startPicker.getValue() == null) {
            org.example.utils.AlertHelper.showErrorAlert("Erreur de Saisie", "La date de début ne peut pas être vide.");
            return;
        }
        if (endPicker.getValue() == null) {
            org.example.utils.AlertHelper.showErrorAlert("Erreur de Saisie", "La date de fin ne peut pas être vide.");
            return;
        }
        if (endPicker.getValue().isBefore(startPicker.getValue())) {
            org.example.utils.AlertHelper.showErrorAlert("Erreur de Saisie", "La date de fin doit être postérieure à la date de début.");
            return;
        }

        cycle.setDate_debut_m(Date.valueOf(startPicker.getValue()));
        cycle.setDate_fin_m(Date.valueOf(endPicker.getValue()));

        service.updateCycle(cycle);

        org.example.utils.AlertHelper.showSuccessAlert("Succès", "Le cycle a été mis à jour avec succès !");

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/cycle/frontoffice/DisplayCycle.fxml")
            );

            Parent view = loader.load();

            DisplayCycleController controller = loader.getController();
            controller.setHomeController(homeController);

            homeController.setContent(view); // 🔥 NAVIGATION BACK

        } catch (IOException e) {
            org.example.utils.AlertHelper.showErrorAlert("Erreur", "Problème lors du retour: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/cycle/frontoffice/DisplayCycle.fxml")
            );

            Parent view = loader.load();

            DisplayCycleController controller = loader.getController();
            controller.setHomeController(homeController);

            homeController.setContent(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}