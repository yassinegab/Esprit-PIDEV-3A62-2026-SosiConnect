package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;

import java.sql.Date;

public class ClientCycleController {


    @FXML
    private TextField TFdate_debut;

    @FXML
    private TextField TF_date_fin;

    @FXML
    private Button BT_ajouter_menstruation;

    CycleService CycleService = new CycleService();

    // 👉 THIS METHOD IS CALLED WHEN BUTTON IS CLICKED
    @FXML
    public void ajouterCycle() {

        // 1. get values from UI
        String debut = TFdate_debut.getText();
        String fin = TF_date_fin.getText();

        Date dateDebut = Date.valueOf(debut);
        Date dateFin = Date.valueOf(fin);

        // ⚠️ IMPORTANT: you don't have user_id in UI yet
        // so we put a fixed value for now (example: 1)
        int userId = 1;

        // 2. create Cycle object
        Cycle c = new Cycle(dateDebut, dateFin, userId);

        // 3. send to service (database)
        CycleService.addCycle(c);

        // 4. feedback
        System.out.println("Cycle added successfully!");

        // 5. clear fields (nice UX)
        TFdate_debut.clear();
        TF_date_fin.clear();
    }
}