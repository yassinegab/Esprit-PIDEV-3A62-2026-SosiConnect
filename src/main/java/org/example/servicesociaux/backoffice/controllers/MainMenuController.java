package org.example.servicesociaux.backoffice.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.servicesociaux.backoffice.services.HopitalService;
import org.example.servicesociaux.backoffice.services.RendezVousService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class MainMenuController {

    @FXML private Label                    countHopital;
    @FXML private Label                    countRdv;
    @FXML private Label                    countEnAttente;
    @FXML private BarChart<String, Number> barChart;

    private final HopitalService    hopitalService = new HopitalService();
    private final RendezVousService rdvService     = new RendezVousService();

    @FXML
    public void initialize() {
        chargerStats();
        chargerBarChart();
    }

    // ══ STATS ════════════════════════════════════════════════════
    private void chargerStats() {
        try {
            countHopital.setText(String.valueOf(hopitalService.afficherTous().size()));
        } catch (Exception e) { countHopital.setText("—"); }

        try {
            List<Object[]> rows = hopitalService.afficherAvecRendezVous();
            int totalRdv  = rows.stream().mapToInt(r -> (int) r[6]).sum();
            int enAttente = rows.stream().mapToInt(r -> (int) r[7]).sum();
            countRdv      .setText(String.valueOf(totalRdv));
            countEnAttente.setText(String.valueOf(enAttente));
        } catch (Exception e) {
            countRdv.setText("—");
            countEnAttente.setText("—");
        }
    }

    // ══ BARCHART — beige (#f5e6d3) + orange (#e09030) ════════════
    private void chargerBarChart() {
        try {
            List<Object[]> rows = hopitalService.afficherAvecRendezVous();

            XYChart.Series<String, Number> serieTotal     = new XYChart.Series<>();
            XYChart.Series<String, Number> serieEnAttente = new XYChart.Series<>();
            serieTotal    .setName("Total RDV");
            serieEnAttente.setName("En attente");

            for (Object[] r : rows) {
                String nom   = r[1] != null ? (String) r[1] : "?";
                String label = nom.length() > 10 ? nom.substring(0, 10) + "…" : nom;
                serieTotal    .getData().add(new XYChart.Data<>(label, (int) r[6]));
                serieEnAttente.getData().add(new XYChart.Data<>(label, (int) r[7]));
            }

            barChart.getData().clear();
            barChart.getData().addAll(serieTotal, serieEnAttente);
            barChart.setLegendVisible(true);

            // Beige doux (#f5e6d3) pour Total RDV
            // Orange (#e09030) pour En attente
            barChart.sceneProperty().addListener((obs, old, newScene) -> {
                if (newScene != null) {
                    newScene.getRoot().applyCss();
                    barChart.lookupAll(".default-color0.chart-bar")
                            .forEach(n -> n.setStyle("-fx-bar-fill: #f5e6d3;"));
                    barChart.lookupAll(".default-color1.chart-bar")
                            .forEach(n -> n.setStyle("-fx-bar-fill: #e09030;"));
                }
            });

        } catch (SQLException e) {
            System.err.println("Erreur barChart admin : " + e.getMessage());
        }
    }

    // ══ NAVIGATION ════════════════════════════════════════════════
    @FXML public void ouvrirHopital() {
        ouvrirFenetre("/servicesociaux/backoffice/hopital.fxml",
                "🏥 Administration — Hôpitaux", 1000, 700);
    }

    @FXML public void ouvrirRendezVous() {
        ouvrirFenetre("/servicesociaux/backoffice/rendezVous.fxml",
                "📅 Administration — Rendez-vous", 1000, 700);
    }

    private void ouvrirFenetre(String cheminFxml, String titre, int w, int h) {
        try {
            URL url = getClass().getResource(cheminFxml);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + cheminFxml);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Stage stage = (Stage) countHopital.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), w, h));
            stage.setTitle(titre);
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}