package org.example.servicesociaux.backoffice.services;

import org.example.servicesociaux.backoffice.entities.Hopital;
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

    public Hopital getById(int id) throws SQLException {
        String sql = "SELECT * FROM hopital WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapRow(rs);
        return null;
    }

    public boolean existeDeja(String nom, String ville, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM hopital " +
                "WHERE LOWER(TRIM(nom)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(ville)) = LOWER(TRIM(?)) " +
                "AND id != ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, nom);
        ps.setString(2, ville);
        ps.setInt(3, excludeId < 0 ? -1 : excludeId);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    // ✅ INSERT utilise 'tel' (nom réel de la colonne)
    public void ajouter(Hopital h) throws SQLException {
        String sql = "INSERT INTO hopital " +
                "(nom, adresse, tel, service_urgence_dispo, " +
                " latitude, longitude, capacite, specialites, ville, type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,  h.getNom());
        ps.setString(2,  h.getAdresse());
        ps.setString(3,  h.getTelephone());   // → colonne 'tel'
        ps.setBoolean(4, h.isServiceUrgenceDispo());
        ps.setDouble(5,  h.getLatitude());
        ps.setDouble(6,  h.getLongitude());
        ps.setInt(7,     h.getCapacite());
        ps.setString(8,  h.getSpecialites());
        ps.setString(9,  h.getVille());
        ps.setString(10, h.getType());
        ps.executeUpdate();
    }

    public List<Hopital> afficherTous() throws SQLException {
        List<Hopital> liste = new ArrayList<>();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM hopital");
        while (rs.next()) liste.add(mapRow(rs));
        return liste;
    }

    // ✅ UPDATE utilise 'tel'
    public void modifier(Hopital h) throws SQLException {
        String sql = "UPDATE hopital " +
                "SET nom=?, adresse=?, tel=?, service_urgence_dispo=?, " +
                "    latitude=?, longitude=?, capacite=?, " +
                "    specialites=?, ville=?, type=? " +
                "WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,  h.getNom());
        ps.setString(2,  h.getAdresse());
        ps.setString(3,  h.getTelephone());   // → colonne 'tel'
        ps.setBoolean(4, h.isServiceUrgenceDispo());
        ps.setDouble(5,  h.getLatitude());
        ps.setDouble(6,  h.getLongitude());
        ps.setInt(7,     h.getCapacite());
        ps.setString(8,  h.getSpecialites());
        ps.setString(9,  h.getVille());
        ps.setString(10, h.getType());
        ps.setInt(11,    h.getId());
        ps.executeUpdate();
    }

    // ✅ Supprimer avec suppression des RDV liés d'abord
    public void supprimer(int id) throws SQLException {
        PreparedStatement ps1 = connection.prepareStatement(
                "DELETE FROM rendez_vous WHERE hopital_id = ?");
        ps1.setInt(1, id);
        ps1.executeUpdate();

        PreparedStatement ps2 = connection.prepareStatement(
                "DELETE FROM hopital WHERE id = ?");
        ps2.setInt(1, id);
        ps2.executeUpdate();
    }

    // ✅ JOINTURE — utilise 'h.tel' (nom réel)
    // Object[] index :
    // [0]id [1]nom [2]adresse [3]tel [4]specialites
    // [5]ville [6]nb_rdv [7]rdv_en_attente [8]capacite
    // [9]service_urgence_dispo [10]type
    public List<Object[]> afficherAvecRendezVous() throws SQLException {
        List<Object[]> liste = new ArrayList<>();
        String sql =
                "SELECT " +
                        "  h.id, " +
                        "  COALESCE(h.nom,         '')  AS nom, " +
                        "  COALESCE(h.adresse,     '')  AS adresse, " +
                        "  COALESCE(h.tel,         '')  AS tel, " +          // ✅ 'tel'
                        "  COALESCE(h.specialites, '')  AS specialites, " +
                        "  COALESCE(h.ville,       '')  AS ville, " +
                        "  COUNT(r.id)                  AS nb_rdv, " +
                        "  SUM(CASE WHEN r.statut = 'En attente' THEN 1 ELSE 0 END) AS rdv_en_attente, " +
                        "  COALESCE(h.capacite, 0)               AS capacite, " +
                        "  COALESCE(h.service_urgence_dispo, 0)  AS service_urgence_dispo, " +
                        "  COALESCE(h.type, '')                  AS type " +
                        "FROM hopital h " +
                        "LEFT JOIN rendez_vous r ON h.id = r.hopital_id " +
                        "GROUP BY h.id, h.nom, h.adresse, h.tel, h.specialites, " +  // ✅ 'h.tel'
                        "         h.ville, h.capacite, h.service_urgence_dispo, h.type " +
                        "ORDER BY h.nom ASC";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            liste.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("adresse"),
                    rs.getString("tel"),            // ✅ 'tel'
                    rs.getString("specialites"),
                    rs.getString("ville"),
                    rs.getInt("nb_rdv"),
                    rs.getInt("rdv_en_attente"),
                    rs.getInt("capacite"),
                    rs.getBoolean("service_urgence_dispo"),
                    rs.getString("type")
            });
        }
        return liste;
    }

    // ✅ mapRow utilise 'tel'
    private Hopital mapRow(ResultSet rs) throws SQLException {
        return new Hopital(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("adresse"),
                rs.getString("tel"),               // ✅ 'tel'
                rs.getBoolean("service_urgence_dispo"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getInt("capacite"),
                rs.getString("specialites"),
                rs.getString("ville"),
                rs.getString("type")
        );
    }
}