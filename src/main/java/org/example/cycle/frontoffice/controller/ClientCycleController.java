package org.example.cycle.frontoffice.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;
import javafx.scene.control.DatePicker;
import org.example.home.controller.HomeController;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

public class ClientCycleController {




    @FXML
    private DatePicker DP_datadebut;


    @FXML
    private DatePicker DP_datefin;

    @FXML
    private Button BT_ajouter_menstruation;

    CycleService CycleService = new CycleService();


    @FXML
    public void ajouterCycle(ActionEvent event) {

        LocalDate localDebut = DP_datadebut.getValue();
        LocalDate localFin = DP_datefin.getValue();


        if (localDebut == null) {
            org.example.utils.AlertHelper.showErrorAlert("Erreur de Saisie", "La date de début ne peut pas être vide.");
            return;
        }
        if (localFin == null) {
            org.example.utils.AlertHelper.showErrorAlert("Erreur de Saisie", "La date de fin ne peut pas être vide.");
            return;
        }
        if (localFin.isBefore(localDebut)) {
            org.example.utils.AlertHelper.showErrorAlert("Erreur de Saisie", "La date de fin doit être postérieure à la date de début.");
            return;
        }

        Date dateDebut = Date.valueOf(localDebut);
        Date dateFin = Date.valueOf(localFin);

        int userId = 1;



        if (CycleService.cycleExists(dateDebut, userId)) {
            org.example.utils.AlertHelper.showErrorAlert(
                    "Doublon détecté",
                    "Un cycle existe déjà pour cette date."
            );
            return;
        }
        Cycle c = new Cycle(dateDebut, dateFin, userId);

        CycleService.addCycle(c);

        org.example.utils.AlertHelper.showSuccessAlert("Succès", "Le cycle a été ajouté avec succès !");


        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/cycle/frontoffice/DisplayCycle.fxml")
            );

            Parent view = loader.load();

            DisplayCycleController controller = loader.getController();
            controller.setHomeController(homeController);

            homeController.setContent(view);

        } catch (IOException e) {
            org.example.utils.AlertHelper.showErrorAlert("Erreur", "Problème lors du retour: " + e.getMessage());
        }
    }

    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
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