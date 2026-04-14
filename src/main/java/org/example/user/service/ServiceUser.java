package org.example.user.service;

import org.example.IService.IService;
import org.example.user.model.User;
import org.example.utils.MyConnection;
import org.example.user.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {

    private Connection connection;

    public ServiceUser() {
        try {
            this.connection = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String query = "INSERT INTO user (nom, prenom, email, password, telephone, age, sexe, taille, poids, handicap, roles, user_role, specialite, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getTelephone());
        ps.setInt(6, user.getAge());
        ps.setString(7, user.getSexe());
        ps.setDouble(8, user.getTaille());
        ps.setDouble(9, user.getPoids());
        ps.setBoolean(10, user.isHandicap());
        ps.setString(11, user.getRoles());
        ps.setString(12, user.getUser_role());
        ps.setString(13, user.getSpecialite());
        ps.setTimestamp(14, new Timestamp(System.currentTimeMillis()));
        ps.executeUpdate();
    }

    @Override
    public void modifier(User user) throws SQLException {
        String query = "UPDATE user SET nom=?, prenom=?, email=?, password=?, telephone=?, age=?, sexe=?, taille=?, poids=?, handicap=?, roles=?, user_role=?, specialite=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getTelephone());
        ps.setInt(6, user.getAge());
        ps.setString(7, user.getSexe());
        ps.setDouble(8, user.getTaille());
        ps.setDouble(9, user.getPoids());
        ps.setBoolean(10, user.isHandicap());
        ps.setString(11, user.getRoles());
        ps.setString(12, user.getUser_role());
        ps.setString(13, user.getSpecialite());
        ps.setInt(14, user.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM user WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<User> afficher() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            users.add(extractUserFromResultSet(rs));
        }
        return users;
    }

    public User login(String email, String password) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ? AND password = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, email);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return extractUserFromResultSet(rs);
        }
        return null;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setTelephone(rs.getString("telephone"));
        user.setAge(rs.getInt("age"));
        user.setSexe(rs.getString("sexe"));
        user.setTaille(rs.getDouble("taille"));
        user.setPoids(rs.getDouble("poids"));
        user.setHandicap(rs.getBoolean("handicap"));
        user.setRoles(rs.getString("roles"));
        user.setUser_role(rs.getString("user_role"));
        user.setSpecialite(rs.getString("specialite"));
        user.setCreated_at(rs.getTimestamp("created_at"));
        return user;
    }
    public User getUserById(int id) {

        User user = null;

        String sql = "SELECT * FROM user WHERE id = ?";

        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));

                // ⚠️ CHANGE THIS depending on DB column
                user.setNom(rs.getString("prenom"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }
}
