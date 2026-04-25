package org.example.cycle.service;

import org.example.IService.IService;
import org.example.cycle.model.IntensiteSymptome;
import org.example.cycle.model.Symptome;
import org.example.cycle.model.TypeSymptome;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SymptomeService implements IService<Symptome> {

    private Connection conn;

    public SymptomeService() {
        try {
            conn = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public int getLastInsertedCycleId() {
        int id = -1;
        try {
            String sql = "SELECT id_cycle FROM cycle ORDER BY id_cycle DESC LIMIT 1";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                id = rs.getInt("id_cycle");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to fetch last cycle ID: " + e.getMessage());
        }
        return id;
    }

    @Override
    public void ajouter(Symptome s) throws SQLException {
        String sql = "INSERT INTO symptome (cycle_id, type, intensite, date_observation) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, s.getCycleId());
        ps.setString(2, s.getType().name());
        ps.setString(3, s.getIntensite().name());
        ps.setDate(4, s.getDateObservation());

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("✅ Symptome added successfully.");
        }
    }

    @Override
    public void modifier(Symptome s) throws SQLException {
        String sql = "UPDATE symptome SET type=?, intensite=?, date_observation=? WHERE id_symptome=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, s.getType().name());
        ps.setString(2, s.getIntensite().name());
        ps.setDate(3, s.getDateObservation());
        ps.setInt(4, s.getIdSymptome());

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("✅ Symptome updated successfully.");
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM symptome WHERE id_symptome=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("✅ Symptome deleted successfully.");
        }
    }

    @Override
    public List<Symptome> afficher() throws SQLException {
        return getSymptomes("SELECT * FROM symptome");
    }

    public List<Symptome> getSymptomesByCycleId(int cycleId) throws SQLException {
        String sql = "SELECT * FROM symptome WHERE cycle_id=" + cycleId;
        return getSymptomes(sql);
    }

    private List<Symptome> getSymptomes(String sql) throws SQLException {
        List<Symptome> symptomes = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Symptome s = new Symptome();
            s.setIdSymptome(rs.getInt("id_symptome"));
            s.setCycleId(rs.getInt("cycle_id"));

            // Safely parse TypeSymptome
            String dbType = rs.getString("type");
            try {
                // Remove spaces/special accents if needed, or fallback to AUTRE
                if (dbType != null) {
                    // Quick fix for the specific error "Maux de tête"
                    if (dbType.equalsIgnoreCase("Maux de tête") || dbType.contains("Maux")) {
                        s.setType(TypeSymptome.MIGRAINE);
                    } else {
                        s.setType(TypeSymptome.valueOf(dbType));
                    }
                } else {
                    s.setType(TypeSymptome.AUTRE);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Warning: Unknown TypeSymptome -> " + dbType);
                s.setType(TypeSymptome.AUTRE); // Fallback
            }

            // Safely parse IntensiteSymptome
            String dbIntensite = rs.getString("intensite");
            try {
                if (dbIntensite != null) {
                    s.setIntensite(IntensiteSymptome.valueOf(dbIntensite));
                } else {
                    s.setIntensite(IntensiteSymptome.Moderee);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Warning: Unknown IntensiteSymptome -> " + dbIntensite);
                s.setIntensite(IntensiteSymptome.Moderee); // Fallback
            }

            s.setDateObservation(rs.getDate("date_observation"));
            symptomes.add(s);
        }
        return symptomes;
    }
}
