package org.example.servicesociaux.frontoffice.controller.controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.servicesociaux.frontoffice.controller.services.HopitalService;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.List;

public class CarteHopitalController {

    @FXML private WebView webView;
    @FXML private Label   statusLabel;

    private final HopitalService service = new HopitalService();
    private WebEngine engine;

    @FXML
    public void initialize() {
        statusLabel.setText("⏳ Chargement...");

        engine = webView.getEngine();

        // ✅ Paramètres réseau
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        System.setProperty("javafx.webkit.patched", "true");
        CookieHandler.setDefault(new CookieManager());

        engine.setJavaScriptEnabled(true);

        // Logger erreurs JS
        engine.setOnAlert(e ->
                System.out.println("JS: " + e.getData()));

        engine.getLoadWorker().exceptionProperty().addListener((obs, old, ex) -> {
            if (ex != null) {
                System.err.println("WebView error: " + ex.getMessage());
            }
        });

        // ✅ Charger le HTML
        URL url = getClass().getResource(
                "/servicesociaux/frontoffice/map.html");

        if (url == null) {
            statusLabel.setText("❌ map.html introuvable");
            System.err.println("❌ map.html introuvable");
            return;
        }

        System.out.println("✅ map.html : " + url.toExternalForm());
        engine.load(url.toExternalForm());

        // ✅ Écouter le chargement
        engine.getLoadWorker().stateProperty().addListener(
                (obs, oldState, newState) -> {
                    System.out.println("WebView: " + newState);
                    if (newState == Worker.State.SUCCEEDED) {
                        Platform.runLater(() -> {
                            statusLabel.setText("⏳ Carte OK — attente Leaflet...");
                            attendreLeafletEtInjecter(0);
                        });
                    } else if (newState == Worker.State.FAILED) {
                        Platform.runLater(() ->
                                statusLabel.setText("❌ Échec chargement carte"));
                    }
                });
    }

    // ✅ Attendre que mapReady === true (Leaflet chargé)
    private void attendreLeafletEtInjecter(int tentative) {
        if (tentative > 50) {
            Platform.runLater(() ->
                    statusLabel.setText("❌ Leaflet timeout (vérifier réseau)"));
            return;
        }

        try {
            Object ready = engine.executeScript(
                    "typeof mapReady !== 'undefined' && mapReady === true");
            if (Boolean.TRUE.equals(ready)) {
                Platform.runLater(this::injecterDonnees);
                return;
            }
        } catch (Exception ignored) {}

        // Réessayer dans 150ms
        new Thread(() -> {
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> attendreLeafletEtInjecter(tentative + 1));
        }).start();
    }

    // ✅ Injecter les données
    private void injecterDonnees() {
        try {
            List<Object[]> hopitaux = service.afficherAvecRendezVous();
            System.out.println("✅ Hôpitaux : " + hopitaux.size());

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            for (Object[] h : hopitaux) {
                double lat = 0, lng = 0;
                try {
                    double[] coords = service.getCoordonnees((int) h[0]);
                    if (coords != null) { lat = coords[0]; lng = coords[1]; }
                } catch (Exception e) {
                    System.err.println("Coords manquantes id=" + h[0]);
                }

                boolean urgence = h[9] != null && (boolean) h[9];

                if (!first) json.append(",");
                first = false;

                json.append("{")
                        .append("\"nom\":"        ).append(js(str(h[1]))).append(",")
                        .append("\"adresse\":"    ).append(js(str(h[2]))).append(",")
                        .append("\"tel\":"        ).append(js(str(h[3]))).append(",")
                        .append("\"specialites\":").append(js(str(h[4]))).append(",")
                        .append("\"ville\":"      ).append(js(str(h[5]))).append(",")
                        .append("\"urgence\":"    ).append(js(String.valueOf(urgence))).append(",")
                        .append("\"lat\":"        ).append(lat).append(",")
                        .append("\"lng\":"        ).append(lng)
                        .append("}");
            }
            json.append("]");

            String jsonStr = json.toString();
            System.out.println("JSON: " + jsonStr);

            // ✅ Injecter dans la carte
            engine.executeScript("loadHopitaux(" + jsonStr + ")");

            // ✅ Position utilisateur par défaut (Tunis)
            engine.executeScript("setUserPosition(36.8065, 10.1815)");
// ✅ Injecter la clé ORS — remplace par ta vraie clé
            String orsKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjQ1OGY5YzRmODc5OTRmZjg4NmRlOTg2MDhmM2Q1MDI2IiwiaCI6Im11cm11cjY0In0=";
            engine.executeScript("setOrsKey('" + orsKey + "')");

// ✅ Injecter les données
            engine.executeScript("loadHopitaux(" + jsonStr + ")");

// ✅ Position utilisateur
            engine.executeScript("setUserPosition(36.8065, 10.1815)");
            Platform.runLater(() ->
                    statusLabel.setText("✅ " + hopitaux.size() + " hôpitaux chargés"));

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->
                    statusLabel.setText("❌ Erreur : " + e.getMessage()));
        }
    }

    @FXML
    public void fermer() {
        Stage stage = (Stage) webView.getScene().getWindow();
        stage.close();
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    // ✅ Sérialiser en JSON string sécurisé
    private String js(String s) {
        if (s == null || s.isEmpty()) return "\"\"";
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'",  "\\'")
                .replace("\n", " ")
                .replace("\r", "")
                .replace("\t", " ") + "\"";
    }
}