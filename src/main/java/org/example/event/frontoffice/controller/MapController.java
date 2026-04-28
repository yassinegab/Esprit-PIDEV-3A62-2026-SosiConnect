package org.example.event.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MapController {

    @FXML
    private WebView webView;
    
    @FXML
    private Label lblAddress;

    public void initMap(String address, double lat, double lng) {
        lblAddress.setText(address);
        
        WebEngine webEngine = webView.getEngine();
        
        // HTML content with Leaflet.js
        String htmlContent = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "    <title>Leaflet Map</title>"
                + "    <meta charset=\"utf-8\" />"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />"
                + "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>"
                + "    <style>"
                + "        html, body, #map { height: 100%; margin: 0; padding: 0; }"
                + "    </style>"
                + "</head>"
                + "<body>"
                + "    <div id=\"map\"></div>"
                + "    <script>"
                + "        var map = L.map('map').setView([" + lat + ", " + lng + "], 15);"
                + "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {"
                + "            attribution: '&copy; OpenStreetMap contributors'"
                + "        }).addTo(map);"
                + "        var marker = L.marker([" + lat + ", " + lng + "]).addTo(map);"
                + "        marker.bindPopup(\"<b>" + escapeJsString(address) + "</b>\").openPopup();"
                + "    </script>"
                + "</body>"
                + "</html>";

        webEngine.loadContent(htmlContent);
    }
    
    private String escapeJsString(String text) {
        if (text == null) return "";
        return text.replace("'", "\\'").replace("\"", "\\\"");
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) webView.getScene().getWindow();
        stage.close();
    }
}
