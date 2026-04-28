package org.example.event.service;

import org.example.event.model.Event;
import org.example.utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventService {

    private final List<String> ALLOWED_TYPES = Arrays.asList("Campagne de sensibilisation", "Recommandation saisonnière", "autre");

    public EventService() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS event (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "description TEXT NOT NULL, " +
                "date DATE NOT NULL, " +
                "type VARCHAR(100) NOT NULL" +
                ")";
        try (Connection conn = MyConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void validateEvent(Event event) throws IllegalArgumentException {
        if (event.getTitle() == null || event.getTitle().trim().length() < 3) {
            throw new IllegalArgumentException("Le titre doit contenir au moins 3 caractères.");
        }
        if (event.getDescription() == null || event.getDescription().trim().length() < 10) {
            throw new IllegalArgumentException("La description doit contenir au moins 10 caractères.");
        }
        if (event.getDate() == null || event.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date doit être aujourd'hui ou dans le futur.");
        }
        if (event.getType() == null || !ALLOWED_TYPES.contains(event.getType())) {
            throw new IllegalArgumentException("Le type d'événement est invalide.");
        }
    }

    public void addEvent(Event event) throws SQLException, IllegalArgumentException {
        validateEvent(event);
        String query = "INSERT INTO event (title, description, date, type) VALUES (?, ?, ?, ?)";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, event.getTitle().trim());
            pstmt.setString(2, event.getDescription().trim());
            pstmt.setDate(3, Date.valueOf(event.getDate()));
            pstmt.setString(4, event.getType());
            pstmt.executeUpdate();
        }
    }

    public void updateEvent(Event event) throws SQLException, IllegalArgumentException {
        validateEvent(event);
        String query = "UPDATE event SET title=?, description=?, date=?, type=? WHERE id=?";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, event.getTitle().trim());
            pstmt.setString(2, event.getDescription().trim());
            pstmt.setDate(3, Date.valueOf(event.getDate()));
            pstmt.setString(4, event.getType());
            pstmt.setInt(5, event.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteEvent(int id) throws SQLException {
        String query = "DELETE FROM event WHERE id=?";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM event ORDER BY date ASC";
        try (Connection conn = MyConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                events.add(new Event(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("type")
                ));
            }
        }
        return events;
    }

    public List<Event> getFutureEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM event WHERE date >= ? ORDER BY date ASC";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(new Event(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type")
                    ));
                }
            }
        }
        return events;
    }
}
