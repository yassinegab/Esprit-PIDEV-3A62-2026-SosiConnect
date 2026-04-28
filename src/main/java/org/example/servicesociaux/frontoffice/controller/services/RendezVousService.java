package org.example.servicesociaux.frontoffice.controller.services;

import org.example.servicesociaux.frontoffice.controller.entities.RendezVous;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.*;

public class RendezVousService {

    private Connection connection;

    public RendezVousService() {
        try {
            connection = MyConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("Erreur connexion : " + e.getMessage());
        }
    }

    // ✅ Ajouter
    public void ajouter(RendezVous rdv) throws SQLException {
        String sql = "INSERT INTO rendez_vous (patient_id, hopital_id, type_consultation, statut, date_rendez_vous, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, rdv.getPatientId());
        ps.setInt(2, rdv.getHopitalId());
        ps.setString(3, rdv.getTypeConsultation());
        ps.setString(4, rdv.getStatut());
        ps.setDate(5, new java.sql.Date(rdv.getDateRendezVous().getTime()));
        ps.setString(6, rdv.getNotes());
        ps.executeUpdate();
    }

    // ✅ Afficher tous avec JOIN hopital pour récupérer le nom
    public List<RendezVous> afficherTous() throws SQLException {
        List<RendezVous> liste = new ArrayList<>();
        String sql = "SELECT r.*, h.nom AS hopital_nom " +
                "FROM rendez_vous r " +
                "LEFT JOIN hopital h ON r.hopital_id = h.id";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            RendezVous rdv = new RendezVous(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    rs.getInt("hopital_id"),
                    rs.getString("type_consultation"),
                    rs.getString("statut"),
                    rs.getDate("date_rendez_vous"),
                    rs.getString("notes")
            );
            rdv.setHopitalNom(rs.getString("hopital_nom"));
            liste.add(rdv);
        }
        return liste;
    }

    // ✅ Modifier
    public void modifier(RendezVous rdv) throws SQLException {
        String sql = "UPDATE rendez_vous SET statut=?, notes=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, rdv.getStatut());
        ps.setString(2, rdv.getNotes());
        ps.setInt(3, rdv.getId());
        ps.executeUpdate();
    }

    // ✅ Supprimer
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM rendez_vous WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ✅ Récupérer la liste des hôpitaux (id → nom)
    public Map<Integer, String> getHopitaux() throws SQLException {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT id, nom FROM hopital ORDER BY nom";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            map.put(rs.getInt("id"), rs.getString("nom"));
        }
        return map;
    }
}