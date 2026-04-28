package org.example.aideEtdon.service;

import org.example.aideEtdon.model.MapLocation;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MapLocationService {
    private Connection cnx;

    public MapLocationService() {
        try {
            cnx = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS map_location (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "type VARCHAR(50) NOT NULL, " +
                "latitude DOUBLE NOT NULL, " +
                "longitude DOUBLE NOT NULL" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void ajouter(MapLocation location) {
        String query = "INSERT INTO map_location (name, type, latitude, longitude) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, location.getName());
            pst.setString(2, location.getType());
            pst.setDouble(3, location.getLatitude());
            pst.setDouble(4, location.getLongitude());
            pst.executeUpdate();
            System.out.println("Location added to map!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String query = "DELETE FROM map_location WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<MapLocation> afficher() {
        List<MapLocation> list = new ArrayList<>();
        String query = "SELECT * FROM map_location";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new MapLocation(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
