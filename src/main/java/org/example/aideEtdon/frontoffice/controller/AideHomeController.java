package org.example.aideEtdon.frontoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.example.aideEtdon.model.ContactUrgence;
import org.example.aideEtdon.model.MapLocation;
import org.example.aideEtdon.service.ContactUrgenceService;
import org.example.aideEtdon.service.MapLocationService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AideHomeController {

    @FXML private Label statusIndicator;
    @FXML private ToggleGroup typeGroup;
    @FXML private ToggleButton btnMed, btnDanger, btnOther;
    @FXML private CheckBox chkLocation;
    @FXML private Button btnEmergency;
    @FXML private ListView<String> historyList;
    @FXML private VBox mapContainer;
    @FXML private ComboBox<String> mapFilterCombo;
    
    private WebView mapWebView;

    private ContactUrgenceService contactService = new ContactUrgenceService();
    private MapLocationService mapLocationService = new MapLocationService();
    private ObservableList<String> history = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        typeGroup = new ToggleGroup();
        btnMed.setToggleGroup(typeGroup);
        btnDanger.setToggleGroup(typeGroup);
        btnOther.setToggleGroup(typeGroup);
        historyList.setItems(history);

        setupToggleStyle(btnMed);
        setupToggleStyle(btnDanger);
        setupToggleStyle(btnOther);
        
        btnMed.setSelected(true); // Default selection
        setupButtonAnimation(btnEmergency);
        
        if (mapFilterCombo != null) {
            mapFilterCombo.setItems(FXCollections.observableArrayList("Tous les services", "Pharmacies Uniquement", "Urgences & Hôpitaux"));
            mapFilterCombo.getSelectionModel().selectFirst();
            mapFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (mapWebView != null && mapWebView.getEngine() != null) {
                    mapWebView.getEngine().executeScript("if(window.filterMarkers) window.filterMarkers('" + newVal.replace("'", "\\'") + "');");
                }
            });
        }
        
        initMap();
    }

    private void initMap() {
        if (mapContainer != null) {
            mapContainer.getChildren().clear();
            mapContainer.setMinHeight(600);
            
            WebView mapWebView = new WebView();
            mapWebView.setMinHeight(600);
            mapWebView.setPrefHeight(700);
            mapWebView.setMaxWidth(Double.MAX_VALUE);
            javafx.scene.layout.VBox.setVgrow(mapWebView, javafx.scene.layout.Priority.ALWAYS);
            mapContainer.getChildren().add(mapWebView);
            
            WebEngine webEngine = mapWebView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            // Fetch map data synchronously right now, BEFORE sending it to the layout engine
            List<MapLocation> locationsList = mapLocationService.afficher();
            JSONArray array = new JSONArray();
            for (MapLocation loc : locationsList) {
                JSONObject obj = new JSONObject();
                obj.put("name", loc.getName());
                obj.put("lat", loc.getLatitude());
                obj.put("lng", loc.getLongitude());
                obj.put("type", loc.getType());
                array.put(obj);
            }
            String jsonArrayStr = array.toString();

            String htmlContent = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <title>SosiConnect Map</title>
                        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                        <script>
                            // THE ULTIMATE FIX: Force Software Coordinate Positioning instead of CSS 3D Transforms!
                            // This guarantees JavaFX WebView Prism Composer never scrambles the canvas due to GPU tearing!
                            window.L_DISABLE_3D = true;
                        </script>
                        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                        <style>
                            body, html { margin: 0; padding: 0; width: 100%%; height: 100%%; overflow: hidden; background-color: #f1f5f9; }
                            #map { position: absolute; top: 0; bottom: 0; left: 0; right: 0; border-radius: 12px; }
                        </style>
                    </head>
                    <body>
                    <div id="map"></div>
                    <script>
                        try {
                            var map = L.map('map', {
                                zoomAnimation: false, 
                                fadeAnimation: false, 
                                markerZoomAnimation: false,
                                minZoom: 5,
                                maxZoom: 19
                            }).setView([36.8065, 10.1815], 13);
                            
                            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                attribution: '&copy; OpenStreetMap',
                                noWrap: true
                            }).addTo(map);
                            
                            var markersLayer = L.layerGroup().addTo(map);
                            var locations = %s;
                            
                            window.filterMarkers = function(filterType) {
                                try {
                                    markersLayer.clearLayers();
                                    var activeBounds = [];
                                    locations.forEach(function(loc) {
                                        var show = false;
                                        if (filterType === 'Tous les services') {
                                            show = true;
                                        } else if (filterType === 'Pharmacies Uniquement' && loc.type.toLowerCase() === 'pharmacie') {
                                            show = true;
                                        } else if (filterType === 'Urgences & Hôpitaux' && (loc.type.toLowerCase() === 'urgence' || loc.type.toLowerCase() === 'hôpital')) {
                                            show = true;
                                        }
                                        
                                        if (show) {
                                            L.marker([loc.lat, loc.lng]).addTo(markersLayer)
                                                .bindPopup("<b>" + loc.name + "</b><br>" + loc.type);
                                            activeBounds.push([loc.lat, loc.lng]);
                                        }
                                    });
                                    if (activeBounds.length > 0) {
                                        map.fitBounds(activeBounds, { padding: [50, 50], maxZoom: 15 });
                                    }
                                } catch(e) { console.error(e); }
                            };
                            
                            // Initialize default view
                            setTimeout(function() {
                                map.invalidateSize({pan: false});
                                window.filterMarkers('Tous les services');
                            }, 450);

                        } catch(e) { 
                            console.error("Map Init Error:", e); 
                        }
                    </script>
                    </body>
                    </html>
                    """.formatted(jsonArrayStr);

            webEngine.loadContent(htmlContent);
        }
    }

    private void setupToggleStyle(ToggleButton btn) {
        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                btn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 20 25; -fx-background-radius: 12; -fx-cursor: hand; -fx-text-fill: white; -fx-background-color: #3b82f6;");
            } else {
                btn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 20 25; -fx-background-radius: 12; -fx-cursor: hand; -fx-text-fill: #334155; -fx-background-color: #f1f5f9;");
            }
        });
    }

    private void setupButtonAnimation(Button btn) {
        btn.setOnMouseEntered(e -> { btn.setScaleX(1.05); btn.setScaleY(1.05); });
        btn.setOnMouseExited(e -> { btn.setScaleX(1.0); btn.setScaleY(1.0); });
    }

    @FXML
    private void handleEmergencyAction() {
        List<ContactUrgence> contacts = contactService.afficherToutes();
        if (contacts.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Aucun contact configuré !");
            alert.setContentText("Veuillez d'abord configurer des contacts de confiance.");
            alert.showAndWait();
            return;
        }

        java.awt.Toolkit.getDefaultToolkit().beep();

        String type = "Assistance";
        javafx.scene.control.ToggleButton selected = (javafx.scene.control.ToggleButton) typeGroup.getSelectedToggle();
        if (selected != null) type = selected.getText();
        
        final String finalType = type;
        final String time = getCurrentTime();

        String logEntry = "AIDE DEMANDÉE: " + finalType + " (" + time + ")";
        history.add(0, logEntry);

        statusIndicator.setText("RÉCUPÉRATION GPS...");
        statusIndicator.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #eab308;");

        javafx.concurrent.Task<Void> emergencyTask = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // 1. Resolve geographic location
                double lat = 36.8065;
                double lng = 10.1815;
                if (chkLocation.isSelected()) {
                    System.out.println("Triangulating true geographical coordinates via IP...");
                    org.example.aideEtdon.service.GeoLocationService.Coordinate coords = org.example.aideEtdon.service.GeoLocationService.fetchUserLocation();
                    lat = coords.lat;
                    lng = coords.lng;
                }

                // 2. Persist Emergency safely
                try {
                    org.example.aideEtdon.service.AlerteService alertSvc = new org.example.aideEtdon.service.AlerteService();
                    alertSvc.ajouter(new org.example.aideEtdon.model.Alerte(finalType, lat, lng));
                    System.out.println("Alerte DB " + finalType + " enregistrée avec les coordonnées réelles.");
                } catch (Exception ex) {
                    System.err.println("Erreur sauvegarde alerte: " + ex.getMessage());
                }

                // 3. Automated SMTP Firing
                System.out.println("====== DÉBUT DE LA DIFFUSION E-MAIL ======");
                for (org.example.aideEtdon.model.ContactUrgence contact : contacts) {
                    System.out.println("[Dispatch] Émission vers: " + contact.getEmail());
                    org.example.aideEtdon.service.EmailService.sendEmergencyAlert(contact.getEmail(), finalType, time, lat, lng);
                }
                
                final int cSize = contacts.size();
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Demande d'aide transmise");
                    alert.setHeaderText("Alerte diffusée à " + cSize + " contact(s) !");
                    alert.setContentText("Votre demande de type [" + finalType + "] a été sécurisée avec vos coordonnées GPS exactes.");
                    alert.show();

                    statusIndicator.setText("AIDE DEMANDÉE");
                    statusIndicator.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
                });
                return null;
            }
        };

        Thread asyncThread = new Thread(emergencyTask);
        asyncThread.setDaemon(true);
        asyncThread.start();
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @FXML
    private void goToContactForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/aideEtdon/frontoffice/ContactFormView.fxml"));
            Node formView = loader.load();
            AideEtdonControllerClientController.getInstance().setView(formView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
