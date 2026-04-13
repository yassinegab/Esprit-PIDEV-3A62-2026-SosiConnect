package org.example.wellbeing.service;

import org.example.utils.MyConnection;
import org.example.wellbeing.model.ChatbotMessage;
import org.example.user.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatbotMessageService {

    private Connection connection;

    public ChatbotMessageService() {
        try {
            connection = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveMessage(ChatbotMessage message) throws SQLException {
        String req = "INSERT INTO chatbot_message (user_id, content, role, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getUser().getId());
            ps.setString(2, message.getContent());
            ps.setString(3, message.getRole());
            ps.setTimestamp(4, Timestamp.valueOf(message.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    message.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<ChatbotMessage> getByUserId(int userId) throws SQLException {
        List<ChatbotMessage> messages = new ArrayList<>();
        String req = "SELECT * FROM chatbot_message WHERE user_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatbotMessage msg = new ChatbotMessage();
                    msg.setId(rs.getInt("id"));
                    msg.setContent(rs.getString("content"));
                    msg.setRole(rs.getString("role"));
                    msg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    msg.setUser(user);
                    
                    messages.add(msg);
                }
            }
        }
        return messages;
    }

    public void clearHistory(int userId) throws SQLException {
        String req = "DELETE FROM chatbot_message WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
