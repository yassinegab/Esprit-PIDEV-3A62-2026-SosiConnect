package org.example.wellbeing.service;

import org.example.IService.IService;
import org.example.utils.MyConnection;
import org.example.wellbeing.model.UserWellBeingData;
import org.example.wellbeing.model.StressPrediction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class WellbeingService implements IService<UserWellBeingData> {

    private Connection connection;

    public WellbeingService() {
        try {
            this.connection = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ajouter(UserWellBeingData data) throws SQLException {
        String query = "INSERT INTO user_well_being_data (work_environment, sleep_problems, headaches, restlessness, heartbeat_palpitations, low_academic_confidence, class_attendance, anxiety_tension, irritability, subject_confidence, created_at, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, data.getWorkEnvironment());
            ps.setInt(2, data.getSleepProblems());
            ps.setInt(3, data.getHeadaches());
            ps.setInt(4, data.getRestlessness());
            ps.setInt(5, data.getHeartbeatPalpitations());
            ps.setInt(6, data.getLowAcademicConfidence());
            ps.setInt(7, data.getClassAttendance());
            ps.setInt(8, data.getAnxietyTension());
            ps.setInt(9, data.getIrritability());
            ps.setInt(10, data.getSubjectConfidence());
            ps.setTimestamp(11, Timestamp.valueOf(data.getCreatedAt()));
            ps.setInt(12, data.getUser().getId());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    data.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(UserWellBeingData data) throws SQLException {
        String query = "UPDATE user_well_being_data SET work_environment=?, sleep_problems=?, headaches=?, restlessness=?, heartbeat_palpitations=?, low_academic_confidence=?, class_attendance=?, anxiety_tension=?, irritability=?, subject_confidence=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, data.getWorkEnvironment());
            ps.setInt(2, data.getSleepProblems());
            ps.setInt(3, data.getHeadaches());
            ps.setInt(4, data.getRestlessness());
            ps.setInt(5, data.getHeartbeatPalpitations());
            ps.setInt(6, data.getLowAcademicConfidence());
            ps.setInt(7, data.getClassAttendance());
            ps.setInt(8, data.getAnxietyTension());
            ps.setInt(9, data.getIrritability());
            ps.setInt(10, data.getSubjectConfidence());
            ps.setInt(11, data.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Manually delete dependent predictions if no CASCADE is set
        String deletePredictions = "DELETE FROM stress_prediction WHERE user_well_being_data_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(deletePredictions)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        
        String query = "DELETE FROM user_well_being_data WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<UserWellBeingData> afficher() throws SQLException {
        List<UserWellBeingData> list = new ArrayList<>();
        String query = "SELECT * FROM user_well_being_data";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            UserWellBeingData data = new UserWellBeingData();
            data.setId(rs.getInt("id"));
            data.setWorkEnvironment(rs.getInt("work_environment"));
            data.setSleepProblems(rs.getInt("sleep_problems"));
            data.setHeadaches(rs.getInt("headaches"));
            data.setRestlessness(rs.getInt("restlessness"));
            data.setHeartbeatPalpitations(rs.getInt("heartbeat_palpitations"));
            data.setLowAcademicConfidence(rs.getInt("low_academic_confidence"));
            data.setClassAttendance(rs.getInt("class_attendance"));
            data.setAnxietyTension(rs.getInt("anxiety_tension"));
            data.setIrritability(rs.getInt("irritability"));
            data.setSubjectConfidence(rs.getInt("subject_confidence"));
            data.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            org.example.user.model.User user = new org.example.user.model.User();
            user.setId(rs.getInt("user_id"));
            data.setUser(user);
            
            // Load prediction if exists
            data.setStressPrediction(getPredictionByDataId(data.getId()));
            
            list.add(data);
        }
        return list;
    }

    public Map<LocalDate, Double> getAverageStressTrend() throws SQLException {
        Map<LocalDate, Double> trend = new TreeMap<>();
        String query = "SELECT DATE(created_at) as date, AVG(anxiety_tension + sleep_problems + headaches + restlessness) as avg_stress " +
                       "FROM user_well_being_data GROUP BY DATE(created_at) ORDER BY date DESC LIMIT 30";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                trend.put(rs.getDate("date").toLocalDate(), rs.getDouble("avg_stress") / 4.0);
            }
        }
        return trend;
    }

    public Map<String, Integer> getStressDistribution() throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        String query = "SELECT " +
                       "CASE " +
                       "  WHEN (anxiety_tension + sleep_problems + headaches + restlessness) / 4.0 <= 2 THEN 'Faible' " +
                       "  WHEN (anxiety_tension + sleep_problems + headaches + restlessness) / 4.0 <= 3.5 THEN 'Modéré' " +
                       "  ELSE 'Élevé' " +
                       "END as stress_level, COUNT(*) as count " +
                       "FROM user_well_being_data GROUP BY stress_level";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                distribution.put(rs.getString("stress_level"), rs.getInt("count"));
            }
        }
        return distribution;
    }

    public List<UserWellBeingData> getCriticalAlerts() throws SQLException {
        List<UserWellBeingData> alerts = new ArrayList<>();
        String query = "SELECT * FROM user_well_being_data " +
                       "WHERE (anxiety_tension + sleep_problems + headaches + restlessness) / 4.0 >= 4 " +
                       "ORDER BY created_at DESC LIMIT 10";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                UserWellBeingData data = mapRowToData(rs);
                alerts.add(data);
            }
        }
        return alerts;
    }

    public List<UserWellBeingData> getByUserId(int userId) throws SQLException {
        List<UserWellBeingData> list = new ArrayList<>();
        String query = "SELECT * FROM user_well_being_data WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToData(rs));
                }
            }
        }
        return list;
    }

    public UserWellBeingData getLatestByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM user_well_being_data WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToData(rs);
                }
            }
        }
        return null;
    }

    private UserWellBeingData mapRowToData(ResultSet rs) throws SQLException {
        UserWellBeingData data = new UserWellBeingData();
        data.setId(rs.getInt("id"));
        data.setWorkEnvironment(rs.getInt("work_environment"));
        data.setSleepProblems(rs.getInt("sleep_problems"));
        data.setHeadaches(rs.getInt("headaches"));
        data.setRestlessness(rs.getInt("restlessness"));
        data.setHeartbeatPalpitations(rs.getInt("heartbeat_palpitations"));
        data.setLowAcademicConfidence(rs.getInt("low_academic_confidence"));
        data.setClassAttendance(rs.getInt("class_attendance"));
        data.setAnxietyTension(rs.getInt("anxiety_tension"));
        data.setIrritability(rs.getInt("irritability"));
        data.setSubjectConfidence(rs.getInt("subject_confidence"));
        data.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        org.example.user.model.User user = new org.example.user.model.User();
        user.setId(rs.getInt("user_id"));
        data.setUser(user);
        data.setStressPrediction(getPredictionByDataId(data.getId()));
        return data;
    }

    private StressPrediction getPredictionByDataId(int dataId) throws SQLException {
        String query = "SELECT * FROM stress_prediction WHERE user_well_being_data_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, dataId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StressPrediction sp = new StressPrediction();
                    sp.setId(rs.getInt("id"));
                    sp.setPredictedStressType(rs.getString("predicted_stress_type"));
                    sp.setPredictedLabel(rs.getString("predicted_label"));
                    sp.setConfidenceScore(rs.getDouble("confidence_score"));
                    sp.setRecommendation(rs.getString("recommendation"));
                    return sp;
                }
            }
        }
        return null;
    }
}
