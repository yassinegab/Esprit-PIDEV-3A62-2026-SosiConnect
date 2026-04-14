package org.example.aideEtdon.service;

import org.example.aideEtdon.model.ContactUrgence;
import org.example.utils.MyConnection;

import java.sql.*;

public class ContactUrgenceService {
    private Connection cnx;

    public ContactUrgenceService() {
        try {
            cnx = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS contact_urgence (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "nom VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) NOT NULL, " +
                "telephone VARCHAR(50)" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void enregistrer(ContactUrgence contact) {
        String query = "INSERT INTO contact_urgence (nom, email, telephone) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, contact.getNom());
            pst.setString(2, contact.getEmail());
            pst.setString(3, contact.getTelephone());
            pst.executeUpdate();
            System.out.println("Contact d'urgence enregistré avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifier(ContactUrgence contact) {
        String query = "UPDATE contact_urgence SET nom=?, email=?, telephone=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, contact.getNom());
            pst.setString(2, contact.getEmail());
            pst.setString(3, contact.getTelephone());
            pst.setInt(4, contact.getId());
            pst.executeUpdate();
            System.out.println("Contact d'urgence modifié avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String query = "DELETE FROM contact_urgence WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Contact d'urgence supprimé.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public java.util.List<ContactUrgence> afficherToutes() {
        java.util.List<ContactUrgence> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM contact_urgence ORDER BY id DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new ContactUrgence(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("telephone")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ContactUrgence getContactActuel() {
        String query = "SELECT * FROM contact_urgence ORDER BY id DESC LIMIT 1";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return new ContactUrgence(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("telephone")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
