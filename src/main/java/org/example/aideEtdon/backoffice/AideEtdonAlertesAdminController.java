package org.example.aideEtdon.backoffice;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.aideEtdon.model.Alerte;
import org.example.aideEtdon.service.AlerteService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AideEtdonAlertesAdminController {

    @FXML private VBox alertesListContainer;
    private AlerteService alerteService;

    @FXML
    public void initialize() {
        alerteService = new AlerteService();
        loadAlertes();
    }

    @FXML
    private void loadAlertes() {
        if (alertesListContainer == null) return;
        alertesListContainer.getChildren().clear();
        
        List<Alerte> alertes = alerteService.afficher();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (Alerte a : alertes) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #ffffff; -fx-padding: 15 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

            VBox infoBox = new VBox(5);
            Label typeLbl = new Label("🚨 URGENCE: " + a.getTypeBesoin());
            typeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #b91c1c;");
            
            Label dateLbl = new Label("Déclenchée le: " + a.getDateAlerte().format(dtf));
            dateLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
            
            Label locLbl = new Label("Localisation: Lat " + a.getLatitude() + " / Lng " + a.getLongitude());
            locLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            
            infoBox.getChildren().addAll(typeLbl, dateLbl, locLbl);

            Label badge = new Label(a.getStatut());
            if ("En Attente".equals(a.getStatut())) {
                badge.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;");
            } else {
                badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;");
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnResolve = new Button("Marquer Résolue");
            btnResolve.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnResolve.setDisable("Résolue".equals(a.getStatut()));
            btnResolve.setOnAction(e -> {
                alerteService.UPDATE_STATUS(a.getId(), "Résolue");
                loadAlertes();
            });

            Button btnDel = new Button("Supprimer");
            btnDel.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #475569; -fx-cursor: hand;");
            btnDel.setOnAction(e -> {
                alerteService.supprimer(a.getId());
                loadAlertes();
            });

            row.getChildren().addAll(infoBox, spacer, badge, btnResolve, btnDel);
            alertesListContainer.getChildren().add(row);
        }
    }
}
