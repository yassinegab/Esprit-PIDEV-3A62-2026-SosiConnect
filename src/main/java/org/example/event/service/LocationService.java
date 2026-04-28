package org.example.event.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LocationService {

    // Cache pour éviter de spammer l'API
    private static final Map<String, double[]> locationCache = new HashMap<>();
    private static final HttpClient client = HttpClient.newHttpClient();

    public static double[] getCoordinates(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        String normalizedAddress = address.trim().toLowerCase();
        if (locationCache.containsKey(normalizedAddress)) {
            return locationCache.get(normalizedAddress);
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedAddress;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    // Nominatim requiert un User-Agent valide
                    .header("User-Agent", "SosiProject/1.0 (JavaFX Application)")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                if (jsonArray.length() > 0) {
                    JSONObject location = jsonArray.getJSONObject(0);
                    double lat = location.getDouble("lat");
                    double lon = location.getDouble("lon");
                    
                    double[] coords = new double[]{lat, lon};
                    locationCache.put(normalizedAddress, coords);
                    return coords;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la géolocalisation de l'adresse : " + e.getMessage());
        }

        return null;
    }
}
