package org.example.cycle.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;
import javafx.scene.control.DatePicker;
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

    // 👉 THIS METHOD IS CALLED WHEN BUTTON IS CLICKED
    @FXML
    public void ajouterCycle() {

        // 1. get values from UI
        LocalDate localDebut = DP_datadebut.getValue();
        LocalDate localFin = DP_datefin.getValue();

        Date dateDebut = Date.valueOf(localDebut);
        Date dateFin = Date.valueOf(localFin);

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
        DP_datadebut.setValue(null);
        DP_datefin.setValue(null);
    }
}