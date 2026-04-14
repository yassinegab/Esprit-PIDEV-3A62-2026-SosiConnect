package org.example.aideEtdon.service;

import org.example.IService.IService;
import org.example.aideEtdon.model.Demande;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandeService implements IService<Demande> {
    private Connection cnx;

    public DemandeService() {
        try {
            cnx = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ajouter(Demande demande) throws SQLException {
        // Business Rules
        if ("Sang".equalsIgnoreCase(demande.getType()) && (demande.getGroupeSanguin() == null || demande.getGroupeSanguin().trim().isEmpty())) {
            throw new IllegalArgumentException("Le groupe sanguin est obligatoire pour un don de sang.");
        }
        if ("Organe".equalsIgnoreCase(demande.getType()) && (demande.getOrgane() == null || demande.getOrgane().trim().isEmpty())) {
            throw new IllegalArgumentException("L'organe est expressément requis pour un don d'organe.");
        }

        String req = "INSERT INTO demande (titre, description, type, groupe_sanguin, organe, urgence, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, demande.getTitre());
        ps.setString(2, demande.getDescription());
        ps.setString(3, demande.getType());
        ps.setString(4, demande.getGroupeSanguin());
        ps.setString(5, demande.getOrgane());
        ps.setString(6, demande.getUrgence());
        ps.setInt(7, demande.getUserId());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Demande demande) throws SQLException {
        // Business Rules
        if ("Sang".equalsIgnoreCase(demande.getType()) && (demande.getGroupeSanguin() == null || demande.getGroupeSanguin().trim().isEmpty())) {
            throw new IllegalArgumentException("Le groupe sanguin est obligatoire pour un don de sang.");
        }
        if ("Organe".equalsIgnoreCase(demande.getType()) && (demande.getOrgane() == null || demande.getOrgane().trim().isEmpty())) {
            throw new IllegalArgumentException("L'organe est expressément requis pour un don d'organe.");
        }

        String req = "UPDATE demande SET titre=?, description=?, type=?, groupe_sanguin=?, organe=?, urgence=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, demande.getTitre());
        ps.setString(2, demande.getDescription());
        ps.setString(3, demande.getType());
        ps.setString(4, demande.getGroupeSanguin());
        ps.setString(5, demande.getOrgane());
        ps.setString(6, demande.getUrgence());
        ps.setInt(7, demande.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM demande WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Demande> afficher() throws SQLException {
        List<Demande> list = new ArrayList<>();
        // Business Rule: Prioritize urgent requests
        String req = "SELECT * FROM demande ORDER BY CASE WHEN urgence = 'Urgent' THEN 1 ELSE 2 END, date_creation DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new Demande(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getString("type"),
                    rs.getString("groupe_sanguin"),
                    rs.getString("organe"),
                    rs.getString("urgence"),
                    rs.getTimestamp("date_creation"),
                    rs.getInt("user_id")
            ));
        }
        return list;
    }

    // Return only urgent requests
    public List<Demande> getDemandesUrgentes() throws SQLException {
        List<Demande> list = new ArrayList<>();
        String req = "SELECT * FROM demande WHERE urgence = 'Urgent' ORDER BY date_creation DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new Demande(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getString("type"),
                    rs.getString("groupe_sanguin"),
                    rs.getString("organe"),
                    rs.getString("urgence"),
                    rs.getTimestamp("date_creation"),
                    rs.getInt("user_id")
            ));
        }
        return list;
    }
}
