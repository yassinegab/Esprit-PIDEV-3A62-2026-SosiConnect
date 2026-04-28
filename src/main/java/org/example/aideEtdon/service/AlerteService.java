package org.example.aideEtdon.service;

import org.example.aideEtdon.model.Alerte;
import org.example.utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlerteService {
    private Connection conn;

    public AlerteService() {
        try {
            this.conn = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Fallback create table if schema script not run
        try {
            Statement st = conn.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS alerte_urgence (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "type_besoin VARCHAR(255) NOT NULL," +
                    "latitude DOUBLE," +
                    "longitude DOUBLE," +
                    "date_alerte DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "statut VARCHAR(50) DEFAULT 'En Attente'" +
                    ")");
        } catch(SQLException e) {}
    }

    public void ajouter(Alerte a) {
        String query = "INSERT INTO alerte_urgence (type_besoin, latitude, longitude, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, a.getTypeBesoin());
            pstmt.setDouble(2, a.getLatitude());
            pstmt.setDouble(3, a.getLongitude());
            pstmt.setString(4, a.getStatut());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Alerte> afficher() {
        List<Alerte> alertes = new ArrayList<>();
        String query = "SELECT * FROM alerte_urgence ORDER BY date_alerte DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Alerte a = new Alerte();
                a.setId(rs.getInt("id"));
                a.setTypeBesoin(rs.getString("type_besoin"));
                a.setLatitude(rs.getDouble("latitude"));
                a.setLongitude(rs.getDouble("longitude"));
                Timestamp ts = rs.getTimestamp("date_alerte");
                if (ts != null) {
                    a.setDateAlerte(ts.toLocalDateTime());
                } else {
                    a.setDateAlerte(LocalDateTime.now());
                }
                a.setStatut(rs.getString("statut"));
                alertes.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alertes;
    }

    public void UPDATE_STATUS(int id, String nouveauStatut) {
        String query = "UPDATE alerte_urgence SET statut = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String query = "DELETE FROM alerte_urgence WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
