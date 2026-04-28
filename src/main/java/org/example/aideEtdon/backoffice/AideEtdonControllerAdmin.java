package org.example.aideEtdon.backoffice;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.aideEtdon.model.Demande;
import org.example.aideEtdon.model.Video;
import org.example.aideEtdon.model.MapLocation;
import org.example.aideEtdon.service.DemandeService;
import org.example.aideEtdon.service.VideoService;
import org.example.aideEtdon.service.MapLocationService;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

import java.util.List;
import javafx.collections.FXCollections;

public class AideEtdonControllerAdmin {

    @FXML private VBox adminDemandesContainer;
    @FXML private VBox adminVideosContainer;
    
    @FXML private TextField videoTitleField;
    @FXML private TextField videoUrlField;
    @FXML private Label videoErrorLabel;

    @FXML private VBox adminMapContainer;
    @FXML private VBox mapLocationsContainer;
    @FXML private TextField locNameField;
    @FXML private TextField locLatField;
    @FXML private TextField locLngField;
    @FXML private ComboBox<String> locTypeBox;
    @FXML private Label locErrorLabel;
    
    private JavaAdminConnector mapConnector = new JavaAdminConnector();

    // Master Navigation
    @FXML private Button btnMasterAide;
    @FXML private Button btnMasterDon;
    @FXML private VBox masterAidePane;
    @FXML private VBox masterDonPane;

    // Inner Navigation
    @FXML private Button btnViewAide;
    @FXML private Button btnViewDon;
    @FXML private VBox demandesViewBox;
    @FXML private VBox videosViewBox;

    private DemandeService demandeService;
    private VideoService videoService;
    private MapLocationService mapLocationService;

    @FXML
    public void initialize() {
        demandeService = new DemandeService();
        videoService = new VideoService();
        mapLocationService = new MapLocationService();
        
        if (locTypeBox != null) {
            locTypeBox.setItems(FXCollections.observableArrayList("Pharmacie", "Urgence", "Hôpital"));
        }
        
        loadMapLocations();
        initAdminMap();
        
        // Setup initial default views
        showMasterDon();
        showDemandesView();
    }

    @FXML
    private void showMasterAide() {
        if (!btnMasterAide.getStyleClass().contains("active")) btnMasterAide.getStyleClass().add("active");
        btnMasterDon.getStyleClass().remove("active");
        
        switchView(masterAidePane, masterDonPane);
    }

    @FXML
    private void showMasterDon() {
        if (!btnMasterDon.getStyleClass().contains("active")) btnMasterDon.getStyleClass().add("active");
        btnMasterAide.getStyleClass().remove("active");
        
        switchView(masterDonPane, masterAidePane);
    }

    @FXML
    private void showDemandesView() {
        if (!btnViewAide.getStyleClass().contains("active")) {
            btnViewAide.getStyleClass().add("active");
        }
        btnViewDon.getStyleClass().remove("active");
        
        switchView(demandesViewBox, videosViewBox);
        loadDemandes();
    }

    @FXML
    private void showVideosView() {
        if (!btnViewDon.getStyleClass().contains("active")) {
            btnViewDon.getStyleClass().add("active");
        }
        btnViewAide.getStyleClass().remove("active");
        
        switchView(videosViewBox, demandesViewBox);
        loadVideos();
    }

    private void switchView(VBox show, VBox hide) {
        if (show.isVisible() && !hide.isVisible()) return;

        hide.setVisible(false);
        show.setVisible(true);
        show.setOpacity(0);

        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(450), show);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(450), show);
        tt.setFromY(25);
        tt.setToY(0);

        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, tt);
        pt.play();
    }

    private void loadDemandes() {
        try {
            List<Demande> demandes = demandeService.afficher();
            adminDemandesContainer.getChildren().clear();

            for (Demande d : demandes) {
                HBox row = new HBox(20);
                row.getStyleClass().add("admin-row");
                row.setAlignment(Pos.CENTER_LEFT);

                VBox textBox = new VBox(5);
                Label titleLbl = new Label(d.getTitre());
                titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                Label descLbl = new Label(d.getDescription());
                descLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                descLbl.setMaxWidth(400); 
                textBox.getChildren().addAll(titleLbl, descLbl);

                Label typeLabel = new Label("Type: " + d.getType());
                typeLabel.setStyle("-fx-text-fill: #6366f1; -fx-font-weight: bold;");
                
                Label badge = new Label(d.getUrgence());
                badge.getStyleClass().add("Urgent".equalsIgnoreCase(d.getUrgence()) ? "urgent-badge" : "modern-badge");

                Button btnDel = new Button("Supprimer");
                btnDel.getStyleClass().add("danger-button");
                btnDel.setOnAction(e -> {
                    try {
                        demandeService.supprimer(d.getId());
                        loadDemandes(); 
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                HBox.setHgrow(textBox, Priority.ALWAYS);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                row.getChildren().addAll(textBox, typeLabel, badge, spacer, btnDel);
                adminDemandesContainer.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadVideos() {
        try {
            List<Video> videos = videoService.afficher();
            adminVideosContainer.getChildren().clear();

            for (Video v : videos) {
                HBox row = new HBox(15);
                row.getStyleClass().add("admin-row");
                row.setAlignment(Pos.CENTER_LEFT);

                Label info = new Label(v.getTitle());
                info.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                Label url = new Label(v.getYoutubeUrl());
                url.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 13px;");
                
                VBox infoBox = new VBox(5, info, url);

                Button btnDel = new Button("Supprimer");
                btnDel.getStyleClass().add("danger-button");
                btnDel.setOnAction(e -> {
                    try {
                        videoService.supprimer(v.getId());
                        loadVideos(); 
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                row.getChildren().addAll(infoBox, spacer, btnDel);
                adminVideosContainer.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddVideo() {
        videoErrorLabel.setVisible(false);
        String title = videoTitleField.getText().trim();
        String url = videoUrlField.getText().trim();

        if (title.isEmpty() || url.isEmpty()) {
            videoErrorLabel.setVisible(true);
            return;
        }

        try {
            Video v = new Video(title, url);
            videoService.ajouter(v);
            
            videoTitleField.clear();
            videoUrlField.clear();
            loadVideos(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMapLocations() {
        if (mapLocationsContainer == null) return;
        try {
            List<MapLocation> locs = mapLocationService.afficher();
            mapLocationsContainer.getChildren().clear();
            for (MapLocation m : locs) {
                HBox row = new HBox(15);
                row.getStyleClass().add("admin-row");
                row.setAlignment(Pos.CENTER_LEFT);

                VBox infoBox = new VBox(5);
                Label nameLbl = new Label(m.getName());
                nameLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                Label coords = new Label("Lat: " + m.getLatitude() + " | Lng: " + m.getLongitude());
                coords.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                infoBox.getChildren().addAll(nameLbl, coords);

                Label typeLbl = new Label(m.getType());
                typeLbl.getStyleClass().add("Urgence".equals(m.getType()) ? "urgent-badge" : "modern-badge");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button btnDel = new Button("Supprimer");
                btnDel.getStyleClass().add("danger-button");
                btnDel.setOnAction(e -> {
                    mapLocationService.supprimer(m.getId());
                    loadMapLocations();
                });

                row.getChildren().addAll(infoBox, typeLbl, spacer, btnDel);
                mapLocationsContainer.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddMapLocation() {
        if (locErrorLabel == null) return;
        locErrorLabel.setVisible(false);
        String name = locNameField.getText().trim();
        String latStr = locLatField.getText().trim();
        String lngStr = locLngField.getText().trim();
        String type = locTypeBox.getValue();

        if (name.isEmpty() || latStr.isEmpty() || lngStr.isEmpty() || type == null) {
            locErrorLabel.setText("Tous les champs sont requis!");
            locErrorLabel.setVisible(true);
            return;
        }

        try {
            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);
            MapLocation m = new MapLocation(name, type, lat, lng);
            mapLocationService.ajouter(m);

            locNameField.clear();
            locLatField.clear();
            locLngField.clear();
            locTypeBox.getSelectionModel().clearSelection();
            loadMapLocations();
        } catch (NumberFormatException e) {
            locErrorLabel.setText("Lat/Lng invalides!");
            locErrorLabel.setVisible(true);
        }
    }

    public class JavaAdminConnector {
        public void setCoordinates(double lat, double lng) {
            javafx.application.Platform.runLater(() -> {
                if (locLatField != null && locLngField != null) {
                    locLatField.setText(String.format(java.util.Locale.US, "%.6f", lat));
                    locLngField.setText(String.format(java.util.Locale.US, "%.6f", lng));
                }
            });
        }
    }

    private void initAdminMap() {
        if (adminMapContainer != null) {
            adminMapContainer.getChildren().clear();
            WebView mapWebView = new WebView();
            mapWebView.setMinHeight(350);
            javafx.scene.layout.VBox.setVgrow(mapWebView, Priority.ALWAYS);
            adminMapContainer.getChildren().add(mapWebView);
            
            WebEngine webEngine = mapWebView.getEngine();
            webEngine.setJavaScriptEnabled(true);
            
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");
                    window.setMember("javaConnector", mapConnector);
                }
            });
            
            StringBuilder jsonBuilder = new StringBuilder("[");
            List<org.example.aideEtdon.model.MapLocation> locations = mapLocationService.afficher();
            for (int i = 0; i < locations.size(); i++) {
                org.example.aideEtdon.model.MapLocation loc = locations.get(i);
                jsonBuilder.append("{")
                        .append("\"lat\":").append(loc.getLatitude()).append(",")
                        .append("\"lng\":").append(loc.getLongitude()).append(",")
                        .append("\"name\":\"").append(loc.getName().replace("\"", "\\\"")).append("\",")
                        .append("\"type\":\"").append(loc.getType().replace("\"", "\\\"")).append("\"")
                        .append("}");
                if (i < locations.size() - 1) jsonBuilder.append(",");
            }
            jsonBuilder.append("]");

            String htmlContent = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <title>Admin Map Picker</title>
                        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                        <script>window.L_DISABLE_3D = true;</script>
                        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                        <style>
                            body, html { margin: 0; padding: 0; width: 100%%; height: 100%%; overflow: hidden; }
                            #map { position: absolute; top: 0; bottom: 0; left: 0; right: 0; border-radius: 8px; }
                        </style>
                    </head>
                    <body>
                    <div id="map"></div>
                    <script>
                        try {
                            var map = L.map('map', {
                                zoomAnimation: false, fadeAnimation: false, markerZoomAnimation: false,
                                minZoom: 5, maxZoom: 19
                            }).setView([36.8065, 10.1815], 13);
                            
                            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                attribution: '&copy; OpenStreetMap', noWrap: true
                            }).addTo(map);
                            
                            var dbLocations = %s;
                            dbLocations.forEach(function(loc) {
                                L.marker([loc.lat, loc.lng]).addTo(map)
                                    .bindPopup("<b>" + loc.name + "</b><br>" + loc.type);
                            });
                            
                            var currentMarker = null;
                            map.on('click', function(e) {
                                if(currentMarker) { map.removeLayer(currentMarker); }
                                currentMarker = L.marker([e.latlng.lat, e.latlng.lng]).addTo(map);
                                if (window.javaConnector) { window.javaConnector.setCoordinates(e.latlng.lat, e.latlng.lng); }
                            });
                            
                            setTimeout(function() { map.invalidateSize({pan: false}); }, 450);
                        } catch(e) { console.error(e); }
                    </script>
                    </body>
                    </html>
                    """.formatted(jsonBuilder.toString());
            webEngine.loadContent(htmlContent);
        }
    }
}

