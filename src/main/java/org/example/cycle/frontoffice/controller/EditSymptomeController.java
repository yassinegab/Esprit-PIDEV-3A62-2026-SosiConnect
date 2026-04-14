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

import org.example.utils.AlertHelper;

public class EditSymptomeController {

    @FXML
    private ComboBox<TypeSymptome> typeComboBox;

    @FXML
    private ComboBox<IntensiteSymptome> intensiteComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnCancel;

    private SymptomeService symptomeService;
    private Symptome currentSymptome;
    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        symptomeService = new SymptomeService();
        
        typeComboBox.setItems(FXCollections.observableArrayList(TypeSymptome.values()));
        intensiteComboBox.setItems(FXCollections.observableArrayList(IntensiteSymptome.values()));
    }

    public void setSymptome(Symptome symptome) {
        this.currentSymptome = symptome;

        // Populate fields with current data
        typeComboBox.setValue(symptome.getType());
        intensiteComboBox.setValue(symptome.getIntensite());
        if (symptome.getDateObservation() != null) {
             datePicker.setValue(symptome.getDateObservation().toLocalDate());
        }
    }

    @FXML
    void handleUpdateSymptome(ActionEvent event) {
        if (currentSymptome == null) {
            AlertHelper.showErrorAlert("Erreur", "Aucune donnée de symptôme chargée pour modification.");
            return;
        }

        // VALIDATION
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

        // Setup the new updated values
        currentSymptome.setType(typeComboBox.getValue());
        currentSymptome.setIntensite(intensiteComboBox.getValue());
        currentSymptome.setDateObservation(Date.valueOf(datePicker.getValue()));

        try {
            symptomeService.modifier(currentSymptome);
            AlertHelper.showSuccessAlert("Succès", "Le symptôme a été mis à jour avec succès !");
            
            // Redirect after success
            handleCancel(event);
            
        } catch (SQLException e) {
            AlertHelper.showErrorAlert("Erreur technique", "Échec de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
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
