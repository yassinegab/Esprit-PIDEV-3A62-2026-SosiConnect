package org.example.aideEtdon.service;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeoLocationService {

    public static class Coordinate {
        public double lat;
        public double lng;
        
        public Coordinate(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    /**
     * Fetches real-world geographic coordinates triangulated from the active IP address.
     * Guaranteed to return a coordinate pair. Defaults to Tunis center (36.8, 10.18) if offline.
     */
    public static Coordinate fetchUserLocation() {
        try {
            URL url = new URL("http://ip-api.com/json/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); 
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                if ("success".equals(json.optString("status", ""))) {
                    double lat = json.getDouble("lat");
                    double lon = json.getDouble("lon");
                    return new Coordinate(lat, lon);
                }
            }
        } catch (Exception e) {
            System.err.println("GeoLocation Api failed silently: " + e.getMessage());
        }
        
        // Failsafe Default coordinates
        return new Coordinate(36.8065, 10.1815);
    }
}
