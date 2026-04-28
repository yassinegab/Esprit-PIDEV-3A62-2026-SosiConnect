package org.example.servicesociaux.backoffice.controllers;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.example.servicesociaux.backoffice.entities.Hopital;
import org.example.servicesociaux.backoffice.services.HopitalService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class HopitalController {

    @FXML private Label statTotalHopitaux;
    @FXML private Label statUrgence;
    @FXML private Label statTotalRdv;
    @FXML private Label statEnAttente;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterUrgence;
    @FXML private ComboBox<String> sortCombo;

    @FXML private TableView<Object[]>            tableView;
    @FXML private TableColumn<Object[], Integer> colId, colNbRdv, colRdvWait, colCapacite;
    @FXML private TableColumn<Object[], String>  colNom, colAdresse, colTel,
            colSpec, colVille, colType, colUrgence;

    @FXML private TextField        fieldNom;
    @FXML private TextField        fieldAdresse;
    @FXML private TextField        fieldTel;
    @FXML private TextField        fieldSpecialites;
    @FXML private TextField        fieldVille;
    @FXML private ComboBox<String> fieldType;
    @FXML private TextField        fieldCapacite;
    @FXML private TextField        fieldLatitude;
    @FXML private TextField        fieldLongitude;
    @FXML private CheckBox         checkUrgence;
    @FXML private Button           btnCarteLatLon;
    @FXML private Button           btnAjouter;
    @FXML private Button           btnModifier;
    @FXML private Button           btnSupprimer;
    @FXML private Button           btnVider;
    @FXML private Label            errorLabel;

    private final HopitalService service = new HopitalService();
    private ObservableList<Object[]> masterList  = FXCollections.observableArrayList();
    private FilteredList<Object[]>   filteredList;
    private int                      selectedId  = -1;

    // ─── Styles champs ───────────────────────────────────────
    private static final String STYLE_NORMAL =
            "-fx-background-color:white;-fx-border-color:#dde3f0;" +
                    "-fx-border-radius:7;-fx-background-radius:7;-fx-font-size:11;-fx-padding:6 8;";
    private static final String STYLE_ERREUR =
            "-fx-background-color:#fff5f5;-fx-border-color:#e53935;" +
                    "-fx-border-radius:7;-fx-background-radius:7;-fx-font-size:11;-fx-padding:6 8;";
    private static final String STYLE_OK =
            "-fx-background-color:#f5fff8;-fx-border-color:#1ea064;" +
                    "-fx-border-radius:7;-fx-background-radius:7;-fx-font-size:11;-fx-padding:6 8;";

    // ══════════════════════════════════════════════════════════
    //  BRIDGE Java ↔ JavaScript
    //  JavaFX expose cet objet au JS sous le nom "javaBridge"
    //  Le JS appelle javaBridge.setCoords(lat, lon) au clic Valider
    // ══════════════════════════════════════════════════════════
    public class JavaBridge {
        private Stage    carteStage;
        private WebEngine engine;

        public JavaBridge(Stage s, WebEngine e) { carteStage = s; engine = e; }

        /** Appelé depuis JavaScript : bridge.setCoords(lat, lon) */
        public void setCoords(String lat, String lon) {
            javafx.application.Platform.runLater(() -> {
                try {
                    double dLat = Double.parseDouble(lat);
                    double dLon = Double.parseDouble(lon);
                    fieldLatitude .setText(String.format("%.6f", dLat));
                    fieldLongitude.setText(String.format("%.6f", dLon));
                    carteStage.close();
                    showSuccess("✅ Position enregistrée : "
                            + String.format("%.5f", dLat) + " / "
                            + String.format("%.5f", dLon));
                } catch (Exception ex) {
                    showError("❌ Erreur coordonnées : " + ex.getMessage());
                }
            });
        }
    }

    @FXML
    public void initialize() {
        fieldType.getItems().addAll(
                "Public","Privé","Clinique","Polyclinique","CHU","CHR","Dispensaire","Autre");

        colId      .setCellValueFactory(d -> new SimpleIntegerProperty((int)  d.getValue()[0]).asObject());
        colNom     .setCellValueFactory(d -> new SimpleStringProperty(str(    d.getValue()[1])));
        colAdresse .setCellValueFactory(d -> new SimpleStringProperty(str(    d.getValue()[2])));
        colTel     .setCellValueFactory(d -> new SimpleStringProperty(str(    d.getValue()[3])));
        colSpec    .setCellValueFactory(d -> new SimpleStringProperty(str(    d.getValue()[4])));
        colVille   .setCellValueFactory(d -> new SimpleStringProperty(str(    d.getValue()[5])));
        colNbRdv   .setCellValueFactory(d -> new SimpleIntegerProperty((int)  d.getValue()[6]).asObject());
        colRdvWait .setCellValueFactory(d -> new SimpleIntegerProperty((int)  d.getValue()[7]).asObject());
        colCapacite.setCellValueFactory(d -> new SimpleIntegerProperty((int)  d.getValue()[8]).asObject());
        colUrgence .setCellValueFactory(d -> {
            boolean u = d.getValue()[9] != null && (boolean) d.getValue()[9];
            return new SimpleStringProperty(u ? "✔" : "✖");
        });
        colType.setCellValueFactory(d -> new SimpleStringProperty(str(d.getValue()[10])));

        colId      .setCellFactory(col -> createCell());
        colNom     .setCellFactory(col -> createCell());
        colAdresse .setCellFactory(col -> createCell());
        colTel     .setCellFactory(col -> createCell());
        colSpec    .setCellFactory(col -> createCell());
        colVille   .setCellFactory(col -> createCell());
        colNbRdv   .setCellFactory(col -> createCell());
        colRdvWait .setCellFactory(col -> createCell());
        colCapacite.setCellFactory(col -> createCell());
        colUrgence .setCellFactory(col -> createUrgenceCell());
        colType    .setCellFactory(col -> createCell());

        tableView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, row) -> { if (row != null) remplirFormulaire(row); });

        tableView.setStyle(
                "-fx-background-color:white;-fx-border-color:#dde3f0;" +
                        "-fx-border-radius:10;-fx-background-radius:10;");
        tableView.getStylesheets().add(
                "data:text/css," +
                        ".table-row-cell:selected{-fx-background-color:%23c8d8f0!important;}" +
                        ".table-row-cell:selected:focused{-fx-background-color:%23c8d8f0!important;}" +
                        ".table-row-cell:selected .table-cell{-fx-background-color:transparent!important;}");

        ajouterValidationTempsReel();
        filterUrgence.getItems().addAll("Tous","Urgence disponible","Sans urgence");
        filterUrgence.setValue("Tous");
        sortCombo.getItems().addAll("Nom (A → Z)","Nom (Z → A)",
                "Nb RDV (croissant)","Nb RDV (décroissant)","En attente (décroissant)");
        sortCombo.setValue("Nom (A → Z)");
        searchField  .textProperty().addListener((o,ov,nv) -> appliquerFiltres());
        filterUrgence.setOnAction(e -> appliquerFiltres());
        sortCombo    .setOnAction(e -> appliquerFiltres());
        chargerTableau();
    }

    // ══════════════════════════════════════════════════════════
    //  CARTE — Bridge Java↔JS pour récupérer les coordonnées
    // ══════════════════════════════════════════════════════════
    @FXML
    public void ouvrirCarte() {
        Stage carteStage = new Stage();
        carteStage.initModality(Modality.APPLICATION_MODAL);
        carteStage.setTitle("📍 Sélectionner la position");

        WebView  webView = new WebView();
        WebEngine engine = webView.getEngine();
        webView.setContextMenuEnabled(false);

        // Coordonnées initiales (formulaire ou Tunis par défaut)
        double lat = 36.8065, lon = 10.1815;
        try {
            String lt = fieldLatitude .getText().trim();
            String lg = fieldLongitude.getText().trim();
            if (!lt.isEmpty()) lat = Double.parseDouble(lt);
            if (!lg.isEmpty()) lon = Double.parseDouble(lg);
        } catch (Exception ignored) {}

        final double initLat = lat;
        final double initLon = lon;

        // Créer le bridge AVANT de charger le HTML
        JavaBridge bridge = new JavaBridge(carteStage, engine);

        // Injecter le bridge dans window.javaBridge dès que le DOM est prêt
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Exposer l'objet Java sous le nom "javaBridge" dans le JS
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", bridge);
                // Forcer le recalcul de la taille de la carte
                engine.executeScript("if(window._map){ window._map.invalidateSize(true); }");
            }
        });

        engine.loadContent(buildMapHtml(initLat, initLon));

        VBox root = new VBox(webView);
        VBox.setVgrow(webView, javafx.scene.layout.Priority.ALWAYS);
        carteStage.setScene(new Scene(root, 820, 600));
        carteStage.setResizable(true);

        // Forcer invalidateSize au redimensionnement
        carteStage.widthProperty() .addListener((o,ov,nv) ->
                engine.executeScript("if(window._map){ window._map.invalidateSize(true); }"));
        carteStage.heightProperty().addListener((o,ov,nv) ->
                engine.executeScript("if(window._map){ window._map.invalidateSize(true); }"));

        carteStage.show();

        // Délai supplémentaire pour le premier rendu
        PauseTransition delay = new PauseTransition(Duration.millis(400));
        delay.setOnFinished(e ->
                engine.executeScript("if(window._map){ window._map.invalidateSize(true); }"));
        delay.play();
    }

    // ══════════════════════════════════════════════════════════
    //  HTML de la carte — bouton Valider appelle javaBridge.setCoords()
    // ══════════════════════════════════════════════════════════
    private String buildMapHtml(double lat, double lon) {
        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<title>Carte</title>
<style>
html,body{margin:0;padding:0;width:100%%;height:100%%;overflow:hidden;
  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f0f4f8;}

/* ── Leaflet CSS minimal inline ── */
.leaflet-pane,.leaflet-tile,.leaflet-marker-icon,.leaflet-marker-shadow,
.leaflet-tile-container,.leaflet-pane>svg,.leaflet-pane>canvas,
.leaflet-zoom-box,.leaflet-image-layer,.leaflet-layer{
  position:absolute;left:0;top:0;}
.leaflet-container{overflow:hidden;}
.leaflet-tile-pane{z-index:200;}
.leaflet-overlay-pane{z-index:400;}
.leaflet-shadow-pane{z-index:500;}
.leaflet-marker-pane{z-index:600;}
.leaflet-tooltip-pane{z-index:650;}
.leaflet-popup-pane{z-index:700;}
.leaflet-map-pane canvas{z-index:1;}
.leaflet-map-pane svg{z-index:2;}
.leaflet-pane{z-index:400;}
.leaflet-tile,.leaflet-marker-icon,.leaflet-marker-shadow{
  user-select:none;-webkit-user-select:none;}
.leaflet-tile::selection{background:transparent;}
.leaflet-tile{visibility:hidden;}
.leaflet-tile-loaded{visibility:inherit;}
.leaflet-top,.leaflet-bottom{position:absolute;z-index:1000;pointer-events:none;}
.leaflet-top{top:0;}.leaflet-bottom{bottom:0;}
.leaflet-left{left:0;}.leaflet-right{right:0;}
.leaflet-control{float:left;clear:both;pointer-events:visiblePainted;position:relative;z-index:800;}
.leaflet-right .leaflet-control{float:right;}
.leaflet-bottom .leaflet-control{margin-bottom:10px;}
.leaflet-top .leaflet-control{margin-top:10px;}
.leaflet-left .leaflet-control{margin-left:10px;}
.leaflet-right .leaflet-control{margin-right:10px;}
.leaflet-bar{box-shadow:0 1px 5px rgba(0,0,0,.65);border-radius:4px;}
.leaflet-bar a{background:#fff;border-bottom:1px solid #ccc;width:26px;height:26px;
  line-height:26px;display:block;text-align:center;text-decoration:none;color:#000;
  font:bold 18px 'Lucida Console',Monaco,monospace;}
.leaflet-bar a:last-child{border-bottom:none;border-radius:0 0 4px 4px;}
.leaflet-bar a:first-child{border-radius:4px 4px 0 0;}
.leaflet-bar a:hover{background:#f4f4f4;}
.leaflet-container{cursor:crosshair;}
.leaflet-grab{cursor:-webkit-grab;cursor:grab;}
.leaflet-dragging .leaflet-grab{cursor:move;cursor:-webkit-grabbing;cursor:grabbing;}
.leaflet-marker-icon,.leaflet-marker-shadow,.leaflet-image-layer,
.leaflet-pane>svg path,.leaflet-tile-container{pointer-events:none;}
.leaflet-marker-icon.leaflet-interactive,.leaflet-image-layer.leaflet-interactive,
.leaflet-pane>svg path.leaflet-interactive{pointer-events:visiblePainted;}
.leaflet-popup{position:absolute;text-align:center;margin-bottom:20px;}
.leaflet-popup-content-wrapper{padding:1px;text-align:left;border-radius:12px;}
.leaflet-popup-content{margin:13px 19px;line-height:1.4;}
.leaflet-popup-tip-container{width:40px;height:20px;position:absolute;left:50%%;
  margin-left:-20px;overflow:hidden;pointer-events:none;}
.leaflet-popup-tip{width:17px;height:17px;padding:1px;margin:-10px auto 0;
  transform:rotate(45deg);}
.leaflet-popup-content-wrapper,.leaflet-popup-tip{
  background:white;color:#333;box-shadow:0 3px 14px rgba(0,0,0,.4);}
.leaflet-container a.leaflet-popup-close-button{
  position:absolute;top:0;right:0;padding:4px 4px 0 0;border:none;
  width:18px;height:14px;font:16px/14px Tahoma,Verdana,sans-serif;
  color:#c3c3c3;text-decoration:none;font-weight:bold;background:transparent;}
.leaflet-container a.leaflet-popup-close-button:hover{color:#999;}
.leaflet-container .leaflet-control-attribution{
  background:rgba(255,255,255,.7);margin:0;padding:0 5px;font-size:11px;color:#333;}
.leaflet-control-attribution a{text-decoration:none;}

/* ── UI ── */
#map{position:absolute;top:0;left:0;right:0;bottom:0;width:100%%;height:100%%;z-index:1;}

#topbar{
  position:absolute;top:12px;left:50%%;transform:translateX(-50%%);
  z-index:2000;display:flex;align-items:center;gap:8px;
  background:white;padding:8px 14px;border-radius:28px;
  box-shadow:0 4px 20px rgba(0,0,0,.20);min-width:580px;max-width:90vw;}

#searchInput{
  flex:1;border:1.5px solid #dde3f0;border-radius:20px;
  padding:8px 16px;font-size:13px;outline:none;min-width:220px;
  color:#1a2744;background:#f5f7fc;transition:border-color .2s;}
#searchInput:focus{border-color:#3a7bd5;background:white;}

.btn{border:none;border-radius:20px;padding:8px 16px;font-size:12px;
  font-weight:700;cursor:pointer;white-space:nowrap;letter-spacing:.3px;
  transition:opacity .15s,transform .1s;}
.btn:hover{opacity:.87;}.btn:active{transform:scale(.97);}
#btnSearch {background:#3a7bd5;color:white;}
#btnGeo    {background:#1ea064;color:white;}
#btnValider{background:#1a2744;color:white;font-size:13px;padding:9px 22px;}

#suggestions{
  position:absolute;top:68px;left:50%%;transform:translateX(-50%%);
  z-index:3000;background:white;border-radius:12px;
  box-shadow:0 6px 24px rgba(0,0,0,.16);max-width:580px;width:90vw;
  max-height:240px;overflow-y:auto;display:none;border:1px solid #e8ecf5;}
.sug-item{padding:10px 16px;font-size:12.5px;cursor:pointer;
  border-bottom:1px solid #f3f3f3;color:#1a2744;
  display:flex;align-items:flex-start;gap:8px;transition:background .1s;}
.sug-item:last-child{border-bottom:none;}
.sug-item:hover{background:#f0f4ff;}
.sug-icon{color:#3a7bd5;font-size:14px;margin-top:1px;flex-shrink:0;}
.sug-text{flex:1;line-height:1.4;}
.sug-main{font-weight:600;color:#1a2744;}
.sug-sub{font-size:11px;color:#888;margin-top:1px;}

#statusBar{
  position:absolute;bottom:32px;left:50%%;transform:translateX(-50%%);
  z-index:2000;background:white;padding:8px 24px;border-radius:20px;
  font-size:13px;font-weight:600;color:#1a2744;
  box-shadow:0 3px 12px rgba(0,0,0,.14);border:1px solid #e0e8f5;
  pointer-events:none;letter-spacing:.3px;}

#loadingOverlay{
  position:absolute;top:0;left:0;right:0;bottom:0;
  background:rgba(240,244,248,.9);z-index:9999;
  display:flex;align-items:center;justify-content:center;
  font-size:15px;color:#3a7bd5;font-weight:600;}
</style>
</head>
<body>

<div id="loadingOverlay">🗺  Chargement de la carte…</div>
<div id="map"></div>

<div id="topbar">
  <input id="searchInput" type="text"
         placeholder="🔍  Rechercher : ville, adresse, hôpital, lieu…"
         onkeydown="if(event.key==='Enter') rechercherAdresse();"/>
  <button class="btn" id="btnSearch"  onclick="rechercherAdresse()">Chercher</button>
  <button class="btn" id="btnGeo"     onclick="meLocaliser()">📍 Ma position</button>
  <button class="btn" id="btnValider" onclick="valider()">✔ Valider</button>
</div>

<div id="suggestions"></div>
<div id="statusBar">Cliquez sur la carte pour choisir une position</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js"></script>

<script>
var _lat = %s, _lon = %s;
var _map, _marker;

function initMap() {
  document.getElementById('loadingOverlay').style.display = 'none';

  _map = L.map('map', {
    center: [_lat, _lon], zoom: 13,
    zoomControl: true, attributionControl: true
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© <a href="https://openstreetmap.org">OpenStreetMap</a>',
    maxZoom: 19
  }).addTo(_map);

  _marker = L.marker([_lat, _lon], { draggable: true })
    .addTo(_map)
    .bindPopup('<b>Position choisie</b><br>Déplacez le marqueur ou cliquez sur la carte');

  _map.invalidateSize(true);
  updateStatus(_lat, _lon);

  _map.on('click', function(e) {
    _lat = e.latlng.lat; _lon = e.latlng.lng;
    _marker.setLatLng([_lat, _lon]).openPopup();
    updateStatus(_lat, _lon);
    hideSuggestions();
  });

  _marker.on('dragend', function() {
    var ll = _marker.getLatLng();
    _lat = ll.lat; _lon = ll.lng;
    updateStatus(_lat, _lon);
  });

  window._map = _map;
}

function updateStatus(lat, lon) {
  document.getElementById('statusBar').textContent =
    '📍  Lat : ' + lat.toFixed(6) + '   |   Lon : ' + lon.toFixed(6);
}

/* ── Recherche Nominatim ──────────────────────────────── */
function rechercherAdresse() {
  var q = document.getElementById('searchInput').value.trim();
  if (!q) return;
  document.getElementById('statusBar').textContent = '🔍  Recherche…';
  hideSuggestions();
  fetch('https://nominatim.openstreetmap.org/search?format=json&limit=8&q='
        + encodeURIComponent(q) + '&accept-language=fr,ar',
        { headers:{'User-Agent':'JavaFXHopitalApp/1.0'} })
    .then(function(r){ return r.json(); })
    .then(function(data){
      if (!data || data.length === 0){
        document.getElementById('statusBar').textContent = '⚠  Aucun résultat pour : ' + q;
        return;
      }
      afficherSuggestions(data);
    })
    .catch(function(){
      document.getElementById('statusBar').textContent = '❌  Erreur réseau';
    });
}

function afficherSuggestions(data) {
  var box = document.getElementById('suggestions');
  box.innerHTML = '';
  data.forEach(function(r){
    var parts = r.display_name.split(',');
    var main  = parts[0].trim();
    var sub   = parts.slice(1,4).join(',').trim();
    var div   = document.createElement('div');
    div.className = 'sug-item';
    div.innerHTML =
      '<span class="sug-icon">📍</span>' +
      '<span class="sug-text">' +
        '<div class="sug-main">' + main + '</div>' +
        '<div class="sug-sub">'  + sub  + '</div>' +
      '</span>';
    div.onclick = function(){
      _lat = parseFloat(r.lat); _lon = parseFloat(r.lon);
      _map.setView([_lat,_lon], 16);
      _marker.setLatLng([_lat,_lon])
             .bindPopup('<b>' + main + '</b>').openPopup();
      updateStatus(_lat, _lon);
      hideSuggestions();
      document.getElementById('searchInput').value = r.display_name;
    };
    box.appendChild(div);
  });
  box.style.display = 'block';
}

function hideSuggestions(){
  document.getElementById('suggestions').style.display = 'none';
}

/* ── Géolocalisation ─────────────────────────────────── */
function meLocaliser(){
  document.getElementById('statusBar').textContent = '📡  Localisation…';
  if (!navigator.geolocation){
    document.getElementById('statusBar').textContent = '❌  Géolocalisation non disponible';
    return;
  }
  navigator.geolocation.getCurrentPosition(
    function(pos){
      _lat = pos.coords.latitude; _lon = pos.coords.longitude;
      _map.setView([_lat,_lon], 16);
      _marker.setLatLng([_lat,_lon])
             .bindPopup('<b>Votre position</b>').openPopup();
      updateStatus(_lat, _lon);
    },
    function(err){
      document.getElementById('statusBar').textContent =
        '❌  ' + (err.code===1 ? 'Permission refusée' : 'Impossible de vous localiser');
    },
    { enableHighAccuracy:true, timeout:10000, maximumAge:0 }
  );
}

document.addEventListener('click', function(e){
  if (!e.target.closest('#topbar') && !e.target.closest('#suggestions'))
    hideSuggestions();
});

/* ══════════════════════════════════════════════════════
   VALIDER — appelle le bridge Java directement
   javaBridge est injecté par Java via window.setMember()
   ══════════════════════════════════════════════════════ */
function valider(){
  try {
    // Appel direct du bridge Java — méthode la plus fiable dans JavaFX WebView
    if (window.javaBridge) {
      window.javaBridge.setCoords(String(_lat), String(_lon));
    } else {
      // Fallback : stocker dans window pour lecture depuis Java
      window._validatedLat = String(_lat);
      window._validatedLon = String(_lon);
      window._validated    = true;
      // Tenter aussi la navigation comme dernier recours
      window.location.href = 'latlon://' + _lat + '/' + _lon;
    }
  } catch(e) {
    window._validatedLat = String(_lat);
    window._validatedLon = String(_lon);
    window._validated    = true;
    window.location.href = 'latlon://' + _lat + '/' + _lon;
  }
}

/* Initialiser après un court délai (laisse JavaFX finir le layout) */
window.addEventListener('load', function(){
  setTimeout(initMap, 200);
});
</script>
</body>
</html>
""".formatted(lat, lon);
    }

    // ══ VALIDATION TEMPS RÉEL ═════════════════════════════════
    private void ajouterValidationTempsReel() {
        fieldNom.textProperty().addListener((o,ov,nv) ->
                fieldNom.setStyle(nv==null||nv.trim().length()<2 ? STYLE_ERREUR : STYLE_OK));
        fieldAdresse.textProperty().addListener((o,ov,nv) ->
                fieldAdresse.setStyle(nv==null||nv.trim().isEmpty() ? STYLE_ERREUR : STYLE_OK));
        fieldTel.textProperty().addListener((o,ov,nv) ->
                fieldTel.setStyle(nv==null||!nv.trim().matches("^[+]?[0-9\\s\\-]{7,20}$")
                        ? STYLE_ERREUR : STYLE_OK));
        fieldVille.textProperty().addListener((o,ov,nv) ->
                fieldVille.setStyle(nv==null||nv.trim().isEmpty() ? STYLE_ERREUR : STYLE_OK));
        fieldCapacite.textProperty().addListener((o,ov,nv) -> {
            if (nv==null||nv.trim().isEmpty()){ fieldCapacite.setStyle(STYLE_NORMAL); return; }
            try { int c=Integer.parseInt(nv.trim());
                fieldCapacite.setStyle(c>=0&&c<=10000?STYLE_OK:STYLE_ERREUR);
            } catch(Exception e){ fieldCapacite.setStyle(STYLE_ERREUR); }
        });
    }

    // ══ VALIDATION COMPLÈTE ═══════════════════════════════════
    private boolean validerFormulaire() {
        boolean ok = true;
        StringBuilder err = new StringBuilder();
        String nom = fieldNom.getText()==null?"":fieldNom.getText().trim();
        if (nom.length()<2){ fieldNom.setStyle(STYLE_ERREUR); err.append("• Nom obligatoire (min 2 car.).\n"); ok=false; }
        else fieldNom.setStyle(STYLE_OK);
        String adresse = fieldAdresse.getText()==null?"":fieldAdresse.getText().trim();
        if (adresse.isEmpty()){ fieldAdresse.setStyle(STYLE_ERREUR); err.append("• Adresse obligatoire.\n"); ok=false; }
        else fieldAdresse.setStyle(STYLE_OK);
        String tel = fieldTel.getText()==null?"":fieldTel.getText().trim();
        if (!tel.matches("^[+]?[0-9\\s\\-]{7,20}$")){ fieldTel.setStyle(STYLE_ERREUR); err.append("• Téléphone invalide.\n"); ok=false; }
        else fieldTel.setStyle(STYLE_OK);
        String ville = fieldVille.getText()==null?"":fieldVille.getText().trim();
        if (ville.isEmpty()){ fieldVille.setStyle(STYLE_ERREUR); err.append("• Ville obligatoire.\n"); ok=false; }
        else fieldVille.setStyle(STYLE_OK);
        if (ok) {
            try {
                if (service.existeDeja(nom,ville,selectedId)){
                    fieldNom.setStyle(STYLE_ERREUR); fieldVille.setStyle(STYLE_ERREUR);
                    err.append("• Hôpital déjà existant dans cette ville.\n"); ok=false;
                }
            } catch(SQLException e){ err.append("• Erreur : "+e.getMessage()+"\n"); ok=false; }
        }
        if (!ok) showError(err.toString().trim());
        return ok;
    }

    // ══ CHARGEMENT ════════════════════════════════════════════
    private void chargerTableau() {
        try {
            masterList.setAll(service.afficherAvecRendezVous());
            filteredList = new FilteredList<>(masterList, p -> true);
            appliquerFiltres(); mettreAJourStats();
        } catch(SQLException e){ showError("❌ Erreur chargement : "+e.getMessage()); e.printStackTrace(); }
    }

    // ══ FILTRAGE ══════════════════════════════════════════════
    private void appliquerFiltres() {
        if (filteredList==null) return;
        String search   = searchField.getText()==null?"":searchField.getText().toLowerCase().trim();
        String fUrgence = filterUrgence.getValue()==null?"Tous":filterUrgence.getValue();
        filteredList.setPredicate(row -> {
            boolean ms = search.isEmpty()
                    ||str(row[1]).toLowerCase().contains(search)
                    ||str(row[2]).toLowerCase().contains(search)
                    ||str(row[3]).toLowerCase().contains(search)
                    ||str(row[4]).toLowerCase().contains(search)
                    ||str(row[5]).toLowerCase().contains(search)
                    ||str(row[10]).toLowerCase().contains(search);
            boolean mu = true;
            if (fUrgence.equals("Urgence disponible")) mu = row[9]!=null&&(boolean)row[9];
            else if (fUrgence.equals("Sans urgence"))  mu = row[9]==null||!(boolean)row[9];
            return ms && mu;
        });
        ObservableList<Object[]> sorted = FXCollections.observableArrayList(filteredList);
        String sort = sortCombo.getValue();
        if (sort!=null){
            Comparator<Object[]> cmp = switch(sort){
                case "Nom (Z → A)"             -> Comparator.comparing((Object[]r)->str(r[1])).reversed();
                case "Nb RDV (croissant)"       -> Comparator.comparingInt(r->(int)r[6]);
                case "Nb RDV (décroissant)"     -> Comparator.comparingInt((Object[]r)->(int)r[6]).reversed();
                case "En attente (décroissant)" -> Comparator.comparingInt((Object[]r)->(int)r[7]).reversed();
                default                         -> Comparator.comparing(r->str(r[1]));
            };
            sorted.sort(cmp);
        }
        tableView.setItems(sorted);
    }

    // ══ STATS ═════════════════════════════════════════════════
    private void mettreAJourStats() {
        statTotalHopitaux.setText(String.valueOf(masterList.size()));
        statUrgence  .setText(String.valueOf(masterList.stream().filter(r->r[9]!=null&&(boolean)r[9]).count()));
        statTotalRdv .setText(String.valueOf(masterList.stream().mapToInt(r->(int)r[6]).sum()));
        statEnAttente.setText(String.valueOf(masterList.stream().mapToInt(r->(int)r[7]).sum()));
    }

    // ══ CRUD ══════════════════════════════════════════════════
    @FXML public void ajouter() {
        if (!validerFormulaire()) return;
        try { service.ajouter(construireHopital(-1)); showSuccess("✅ Hôpital ajouté."); viderFormulaire(); chargerTableau(); }
        catch(SQLException e){ showError("❌ Erreur ajout : "+e.getMessage()); }
    }
    @FXML public void modifier() {
        if (selectedId==-1){ showError("⚠ Sélectionnez un hôpital."); return; }
        if (!validerFormulaire()) return;
        try { service.modifier(construireHopital(selectedId)); showSuccess("✅ Hôpital modifié."); viderFormulaire(); chargerTableau(); }
        catch(SQLException e){ showError("❌ Erreur modification : "+e.getMessage()); }
    }
    @FXML public void supprimer() {
        if (selectedId==-1){ showError("⚠ Sélectionnez un hôpital."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirmation"); c.setHeaderText("Supprimer cet hôpital ?");
        c.setContentText("Les rendez-vous liés seront aussi supprimés.");
        Optional<ButtonType> r = c.showAndWait();
        if (r.isEmpty()||r.get()!=ButtonType.OK) return;
        try { service.supprimer(selectedId); showSuccess("✅ Hôpital supprimé."); viderFormulaire(); chargerTableau(); }
        catch(SQLException e){ showError("❌ Erreur suppression : "+e.getMessage()); }
    }
    @FXML public void viderFormulaire() {
        selectedId=-1;
        fieldNom.clear(); fieldAdresse.clear(); fieldTel.clear();
        fieldSpecialites.clear(); fieldVille.clear(); fieldType.setValue(null);
        fieldCapacite.clear(); fieldLatitude.clear(); fieldLongitude.clear();
        checkUrgence.setSelected(false);
        tableView.getSelectionModel().clearSelection();
        for (TextField f : List.of(fieldNom,fieldAdresse,fieldTel,fieldVille,
                fieldSpecialites,fieldCapacite,fieldLatitude,fieldLongitude))
            f.setStyle(STYLE_NORMAL);
        errorLabel.setText("");
    }
    private void remplirFormulaire(Object[] row) {
        selectedId=(int)row[0];
        try {
            Hopital h = service.getById(selectedId);
            if (h==null){ showError("❌ Hôpital introuvable."); return; }
            fieldNom        .setText(h.getNom()        !=null?h.getNom()        :"");
            fieldAdresse    .setText(h.getAdresse()    !=null?h.getAdresse()    :"");
            fieldTel        .setText(h.getTelephone()  !=null?h.getTelephone()  :"");
            fieldSpecialites.setText(h.getSpecialites()!=null?h.getSpecialites():"");
            fieldVille      .setText(h.getVille()      !=null?h.getVille()      :"");
            fieldCapacite   .setText(String.valueOf(h.getCapacite()));
            fieldLatitude   .setText(h.getLatitude() !=0?String.valueOf(h.getLatitude()) :"");
            fieldLongitude  .setText(h.getLongitude()!=0?String.valueOf(h.getLongitude()):"");
            checkUrgence    .setSelected(h.isServiceUrgenceDispo());
            String type=h.getType();
            if (type!=null&&!type.isEmpty()){
                if (!fieldType.getItems().contains(type)) fieldType.getItems().add(type);
                fieldType.setValue(type);
            } else fieldType.setValue(null);
            for (TextField f : List.of(fieldNom,fieldAdresse,fieldTel,fieldVille,
                    fieldSpecialites,fieldCapacite,fieldLatitude,fieldLongitude))
                f.setStyle(STYLE_NORMAL);
        } catch(SQLException e){ showError("❌ "+e.getMessage()); }
    }
    private Hopital construireHopital(int id) {
        double lat=0,lon=0; int cap=0;
        try{ lat=Double.parseDouble(fieldLatitude .getText().trim()); }catch(Exception ignored){}
        try{ lon=Double.parseDouble(fieldLongitude.getText().trim()); }catch(Exception ignored){}
        try{ cap=Integer.parseInt  (fieldCapacite .getText().trim()); }catch(Exception ignored){}
        return new Hopital(id,fieldNom.getText().trim(),fieldAdresse.getText().trim(),
                fieldTel.getText().trim(),checkUrgence.isSelected(),lat,lon,cap,
                fieldSpecialites.getText().trim(),fieldVille.getText().trim(),
                fieldType.getValue()!=null?fieldType.getValue():"");
    }

    // ══ EXPORT CSV ════════════════════════════════════════════
    @FXML public void exporterCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter CSV"); fc.setInitialFileName("hopitaux.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)","*.csv"));
        File dl = new File(System.getProperty("user.home")+"/Downloads");
        fc.setInitialDirectory(dl.exists()?dl:new File(System.getProperty("user.home")));
        File f = fc.showSaveDialog((Stage)tableView.getScene().getWindow());
        if (f==null) return;
        try(FileWriter fw=new FileWriter(f)){
            fw.write("ID,Nom,Adresse,Téléphone,Spécialités,Ville,Type,Urgence,Nb RDV,En attente,Capacité\n");
            for (Object[] row : tableView.getItems()){
                boolean u=row[9]!=null&&(boolean)row[9];
                fw.write(String.format("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,%s\n",
                        row[0],esc(row[1]),esc(row[2]),esc(row[3]),esc(row[4]),
                        esc(row[5]),esc(row[10]),u?"Oui":"Non",row[6],row[7],row[8]));
            }
            showSuccess("✅ Export réussi : "+f.getName());
        } catch(IOException e){ showError("❌ Export : "+e.getMessage()); }
    }

    // ══ NAVIGATION ════════════════════════════════════════════
    @FXML public void retourAccueil() {
        try {
            URL url=getClass().getResource("/servicesociaux/backoffice/MainMenu.fxml");
            if (url==null){ showError("❌ MainMenu introuvable"); return; }
            Stage stage=(Stage)tableView.getScene().getWindow();
            stage.setScene(new Scene(new FXMLLoader(url).load(),900,660));
            stage.setTitle("⚙ MediCare Admin");
        } catch(Exception e){ showError("❌ Navigation : "+e.getMessage()); }
    }

    // ══ CELLULES ══════════════════════════════════════════════
    private <T> TableCell<Object[],T> createCell() {
        return new TableCell<>(){
            @Override protected void updateItem(T item,boolean empty){
                super.updateItem(item,empty);
                if (empty||item==null){ setText(null); setStyle(""); return; }
                setText(item.toString());
                setStyle(getTableRow()!=null&&getTableRow().isSelected()
                        ?"-fx-text-fill:#1a2744;-fx-font-weight:bold;"
                        :"-fx-text-fill:#333;");
            }
        };
    }
    private <T> TableCell<Object[],T> createUrgenceCell() {
        return new TableCell<>(){
            @Override protected void updateItem(T item,boolean empty){
                super.updateItem(item,empty);
                if (empty||item==null){ setText(null); setStyle(""); return; }
                setText(item.toString());
                setStyle("-fx-text-fill:"+("✔".equals(item.toString())?"#1ea064":"#e53935")
                        +";-fx-font-weight:bold;-fx-alignment:CENTER;");
            }
        };
    }

    // ══ MESSAGES ══════════════════════════════════════════════
    private void showError(String msg){
        errorLabel.setStyle("-fx-text-fill:#c62828;-fx-font-size:11;");
        errorLabel.setText(msg);
        PauseTransition p=new PauseTransition(Duration.seconds(5));
        p.setOnFinished(e->errorLabel.setText(""));
        p.play();
    }
    private void showSuccess(String msg){
        errorLabel.setStyle("-fx-text-fill:#2e7d32;-fx-font-size:11;");
        errorLabel.setText(msg);
        PauseTransition p=new PauseTransition(Duration.seconds(4));
        p.setOnFinished(e->errorLabel.setText(""));
        p.play();
    }
    private String str(Object o){ return o!=null?o.toString():""; }
    private String esc(Object o){ return str(o).replace("\"","'"); }
}