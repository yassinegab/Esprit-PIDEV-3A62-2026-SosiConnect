package org.example.cycle.frontoffice.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import org.example.cycle.model.IntensiteSymptome;
import org.example.cycle.model.Symptome;
import org.example.cycle.model.TypeSymptome;
import org.example.cycle.service.SymptomeService;
import org.example.home.controller.HomeController;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import org.example.utils.AlertHelper;

public class AddSymptomeController {

    @FXML
    private ComboBox<TypeSymptome> typeComboBox;

    @FXML
    private ComboBox<IntensiteSymptome> intensiteComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnCancel;

    private SymptomeService symptomeService;
    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        symptomeService = new SymptomeService();
        

        typeComboBox.setItems(FXCollections.observableArrayList(TypeSymptome.values()));
        intensiteComboBox.setItems(FXCollections.observableArrayList(IntensiteSymptome.values()));
        

        datePicker.setValue(LocalDate.now());
    }

    @FXML
    void handleAddSymptome(ActionEvent event) {

        if (typeComboBox.getValue() == null) {
            AlertHelper.showErrorAlert("Erreur de Saisie", "Veuillez sélectionner un type de symptôme.");
            return;
        }
        if (intensiteComboBox.getValue() == null) {
            AlertHelper.showErrorAlert("Erreur de Saisie", "Veuillez sélectionner un niveau d'intensité.");
            return;
        }
        if (datePicker.getValue() == null) {
            AlertHelper.showErrorAlert("Erreur de Saisie", "Veuillez sélectionner une date d'observation.");
            return;
        }


        int lastCycleId = symptomeService.getLastInsertedCycleId();

        if (lastCycleId == -1) {
            AlertHelper.showErrorAlert("Erreur de Base de Données", "Aucun cycle précédent n'a été trouvé. Veuillez créer un cycle d'abord.");
            return;
        }


        Symptome newSymptome = new Symptome(
                lastCycleId,
                typeComboBox.getValue(),
                intensiteComboBox.getValue(),
                Date.valueOf(datePicker.getValue())
        );


        try {
            symptomeService.ajouter(newSymptome);
            AlertHelper.showSuccessAlert("Succès", "Le symptôme a été ajouté avec succès !");
            

            goToDisplaySymptomes();
        } catch (SQLException e) {
            AlertHelper.showErrorAlert("Erreur technique", "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        goToDisplaySymptomes();
    }

    private void goToDisplaySymptomes() {
        if (homeController != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/display_symptome.fxml"));
                Parent view = loader.load();
                org.example.cycle.frontoffice.controller.DisplaySymptomeController controller = loader.getController();
                controller.setHomeController(homeController);
                homeController.setContent(view);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
