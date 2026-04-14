package org.example.aideEtdon.service;

import org.example.IService.IService;
import org.example.aideEtdon.model.Don;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonService implements IService<Don> {
    private Connection cnx;

    public DonService() {
        try {
            cnx = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ajouter(Don don) throws SQLException {
        String req = "INSERT INTO don (demande_id, donor_id, message) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, don.getDemandeId());
        ps.setInt(2, don.getDonorId());
        ps.setString(3, don.getMessage());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Don don) throws SQLException {
        String req = "UPDATE don SET message=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, don.getMessage());
        ps.setInt(2, don.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM don WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Don> afficher() throws SQLException {
        List<Don> list = new ArrayList<>();
        String req = "SELECT * FROM don ORDER BY date DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new Don(
                    rs.getInt("id"),
                    rs.getInt("demande_id"),
                    rs.getInt("donor_id"),
                    rs.getString("message"),
                    rs.getTimestamp("date")
            ));
        }
        return list;
    }

    public List<Don> afficherParDemande(int demandeId) throws SQLException {
        List<Don> list = new ArrayList<>();
        String req = "SELECT * FROM don WHERE demande_id=? ORDER BY date DESC";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, demandeId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Don(
                    rs.getInt("id"),
                    rs.getInt("demande_id"),
                    rs.getInt("donor_id"),
                    rs.getString("message"),
                    rs.getTimestamp("date")
            ));
        }
        return list;
    }
}
