package org.example.servicesociaux.frontoffice.controller.services;

import org.example.servicesociaux.frontoffice.controller.entities.Hopital;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HopitalService {

    private Connection connection;

    public HopitalService() {
        try {
            connection = MyConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("Erreur connexion : " + e.getMessage());
        }
    }

    // ✅ Ajouter
    public void ajouter(Hopital h) throws SQLException {
        String sql = "INSERT INTO hopital (nom, adresse, tel, service_urgence_dispo, " +
                "latitude, longitude, capacite, specialites, ville, type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, h.getNom());
        ps.setString(2, h.getAdresse());
        ps.setString(3, h.getTelephone());
        ps.setBoolean(4, h.isServiceUrgenceDispo());
        ps.setDouble(5, h.getLatitude());
        ps.setDouble(6, h.getLongitude());
        ps.setInt(7, h.getCapacite());
        ps.setString(8, h.getSpecialites());
        ps.setString(9, h.getVille());
        ps.setString(10, h.getType());
        ps.executeUpdate();
    }

    // ✅ Afficher tous (simple)
    public List<Hopital> afficherTous() throws SQLException {
        List<Hopital> liste = new ArrayList<>();
        String sql = "SELECT * FROM hopital";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            liste.add(mapRow(rs));
        }
        return liste;
    }

    // ✅ Modifier
    public void modifier(Hopital h) throws SQLException {
        String sql = "UPDATE hopital SET nom=?, adresse=?, tel=?, " +
                "service_urgence_dispo=?, capacite=?, specialites=?, ville=?, type=? " +
                "WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, h.getNom());
        ps.setString(2, h.getAdresse());
        ps.setString(3, h.getTelephone());
        ps.setBoolean(4, h.isServiceUrgenceDispo());
        ps.setInt(5, h.getCapacite());
        ps.setString(6, h.getSpecialites());
        ps.setString(7, h.getVille());
        ps.setString(8, h.getType());
        ps.setInt(9, h.getId());
        ps.executeUpdate();
    }

    // ✅ Supprimer
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM hopital WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ✅ JOINTURE : Hôpitaux avec nombre de rendez-vous
    //
    // Object[] retourné (index fixes utilisés dans HopitalController) :
    //   [0]  id                    (int)
    //   [1]  nom                   (String)
    //   [2]  adresse               (String)  ← peut être NULL
    //   [3]  tel                   (String)  ← nom réel de la colonne dans la DB
    //   [4]  specialites           (String)  ← peut être NULL
    //   [5]  ville                 (String)  ← peut être NULL
    //   [6]  nb_rdv                (int)
    //   [7]  rdv_en_attente        (int)
    //   [8]  capacite              (int)
    //   [9]  service_urgence_dispo (boolean)
    public List<Object[]> afficherAvecRendezVous() throws SQLException {
        List<Object[]> liste = new ArrayList<>();
        String sql = "SELECT h.id, " +
                "COALESCE(h.nom, '') AS nom, " +
                "COALESCE(h.adresse, '') AS adresse, " +
                "COALESCE(h.tel, '') AS tel, " +
                "COALESCE(h.specialites, '') AS specialites, " +
                "COALESCE(h.ville, '') AS ville, " +
                "COUNT(r.id) AS nb_rdv, " +
                "SUM(CASE WHEN r.statut = 'En attente' THEN 1 ELSE 0 END) AS rdv_en_attente, " +
                "COALESCE(h.capacite, 0) AS capacite, " +
                "COALESCE(h.service_urgence_dispo, 0) AS service_urgence_dispo " +
                "FROM hopital h " +
                "LEFT JOIN rendez_vous r ON h.id = r.hopital_id " +
                "GROUP BY h.id, h.nom, h.adresse, h.tel, h.specialites, " +
                "h.ville, h.capacite, h.service_urgence_dispo " +
                "ORDER BY h.nom ASC";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            liste.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("adresse"),
                    rs.getString("tel"),
                    rs.getString("specialites"),
                    rs.getString("ville"),
                    rs.getInt("nb_rdv"),
                    rs.getInt("rdv_en_attente"),
                    rs.getInt("capacite"),
                    rs.getBoolean("service_urgence_dispo")
            });
        }
        return liste;
    }

    // ✅ JOINTURE : Hôpitaux filtrés par statut de RDV
    public List<Object[]> afficherAvecRendezVousByStatut(String statut) throws SQLException {
        List<Object[]> liste = new ArrayList<>();
        String sql = "SELECT h.id, h.nom, h.ville, h.tel, h.specialites, " +
                "r.id AS rdv_id, r.type_consultation, r.statut, " +
                "r.date_rendez_vous, r.notes, r.patient_id " +
                "FROM hopital h " +
                "INNER JOIN rendez_vous r ON h.id = r.hopital_id " +
                "WHERE r.statut = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, statut);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("ville"),
                    rs.getString("tel"),
                    rs.getString("specialites"),
                    rs.getInt("rdv_id"),
                    rs.getString("type_consultation"),
                    rs.getString("statut"),
                    rs.getDate("date_rendez_vous"),
                    rs.getString("notes"),
                    rs.getInt("patient_id")
            });
        }
        return liste;
    }

    // ✅ JOINTURE : Détail complet d'un hôpital avec tous ses rendez-vous
    public List<Object[]> afficherRendezVousParHopital(int hopitalId) throws SQLException {
        List<Object[]> liste = new ArrayList<>();
        String sql = "SELECT h.nom AS hopital_nom, h.ville, h.specialites, " +
                "r.id AS rdv_id, r.patient_id, r.type_consultation, " +
                "r.statut, r.date_rendez_vous, r.notes " +
                "FROM hopital h " +
                "INNER JOIN rendez_vous r ON h.id = r.hopital_id " +
                "WHERE h.id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, hopitalId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(new Object[]{
                    rs.getString("hopital_nom"),
                    rs.getString("ville"),
                    rs.getString("specialites"),
                    rs.getInt("rdv_id"),
                    rs.getInt("patient_id"),
                    rs.getString("type_consultation"),
                    rs.getString("statut"),
                    rs.getDate("date_rendez_vous"),
                    rs.getString("notes")
            });
        }
        return liste;
    }

    // ✅ Mapper une ligne ResultSet → Hopital (utilise 'tel' comme nom de colonne réel)
    private Hopital mapRow(ResultSet rs) throws SQLException {
        return new Hopital(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("adresse"),
                rs.getString("tel"),           // ← nom réel dans la DB
                rs.getBoolean("service_urgence_dispo"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getInt("capacite"),
                rs.getString("specialites"),
                rs.getString("ville"),
                rs.getString("type")
        );

    }
    // Dans HopitalService.java — appel REST pur Java
    public String calculerItineraire(double latDepart, double lngDepart,
                                     double latArrivee, double lngArrivee) throws Exception {
        String apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjQ1OGY5YzRmODc5OTRmZjg4NmRlOTg2MDhmM2Q1MDI2IiwiaCI6Im11cm11cjY0In0="; // ← colle ta vraie clé complète ici

        String urlStr = "https://api.openrouteservice.org/v2/directions/driving-car"
                + "?start=" + lngDepart + "," + latDepart
                + "&end="   + lngArrivee + "," + latArrivee;

        java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                new java.net.URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept",        "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey); // ← clé dans le header
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("ORS erreur HTTP " + code);
        }

        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();

        // Parse JSON — org.json est déjà dans ton pom.xml !
        org.json.JSONObject json     = new org.json.JSONObject(response.toString());
        org.json.JSONObject summary  = json
                .getJSONArray("features")
                .getJSONObject(0)
                .getJSONObject("properties")
                .getJSONObject("summary");

        double distKm = summary.getDouble("distance") / 1000.0;
        int    durMin = (int)(summary.getDouble("duration") / 60.0);

        return String.format("📍 Distance : %.1f km   ⏱ Durée : %d min", distKm, durMin);
    }
    public double[] getCoordonnees(int hopitalId) throws SQLException {
        String sql = "SELECT latitude, longitude FROM hopital WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, hopitalId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new double[]{rs.getDouble("latitude"), rs.getDouble("longitude")};
        }
        return new double[]{0, 0};
    }
}
