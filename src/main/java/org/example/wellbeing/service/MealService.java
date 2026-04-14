package org.example.wellbeing.service;

import org.example.IService.IService;
import org.example.utils.MyConnection;
import org.example.wellbeing.model.Meal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MealService implements IService<Meal> {

    private Connection connection;

    public MealService() {
        try {
            this.connection = MyConnection.getConnection();
            // Simple migration to add stress_insight column if it doesn't exist
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("ALTER TABLE meal ADD COLUMN IF NOT EXISTS stress_insight TEXT");
            } catch (SQLException e) {
                // Ignore if IF NOT EXISTS is not supported or column already exists
                System.out.println("Note: stress_insight column check/add: " + e.getMessage());
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void ajouter(Meal meal) throws SQLException {
        String query = "INSERT INTO meal (image_name, description, ai_analysis, create_at, user_id, calories, sugar, protein, stress_insight) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, meal.getImageName());
            ps.setString(2, meal.getDescription());
            ps.setString(3, meal.getAiAnalysis());
            ps.setTimestamp(4, Timestamp.valueOf(meal.getCreatedAt()));
            ps.setInt(5, meal.getUser().getId());
            if (meal.getCalories() != null) ps.setDouble(6, meal.getCalories()); else ps.setNull(6, Types.DOUBLE);
            if (meal.getSugar() != null) ps.setDouble(7, meal.getSugar()); else ps.setNull(7, Types.DOUBLE);
            if (meal.getProtein() != null) ps.setDouble(8, meal.getProtein()); else ps.setNull(8, Types.DOUBLE);
            ps.setString(9, meal.getStressInsight());
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    meal.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Meal meal) throws SQLException {
        String query = "UPDATE meal SET image_name=?, description=?, ai_analysis=?, calories=?, sugar=?, protein=?, stress_insight=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, meal.getImageName());
            ps.setString(2, meal.getDescription());
            ps.setString(3, meal.getAiAnalysis());
            if (meal.getCalories() != null) ps.setDouble(4, meal.getCalories()); else ps.setNull(4, Types.DOUBLE);
            if (meal.getSugar() != null) ps.setDouble(5, meal.getSugar()); else ps.setNull(5, Types.DOUBLE);
            if (meal.getProtein() != null) ps.setDouble(6, meal.getProtein()); else ps.setNull(6, Types.DOUBLE);
            ps.setString(7, meal.getStressInsight());
            ps.setInt(8, meal.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM meal WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Meal> afficher() throws SQLException {
        List<Meal> list = new ArrayList<>();
        String query = "SELECT * FROM meal ORDER BY create_at DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            Meal meal = new Meal();
            meal.setId(rs.getInt("id"));
            meal.setImageName(rs.getString("image_name"));
            meal.setDescription(rs.getString("description"));
            meal.setAiAnalysis(rs.getString("ai_analysis"));
            meal.setCreatedAt(rs.getTimestamp("create_at").toLocalDateTime());
            meal.setCalories(rs.getObject("calories") != null ? rs.getDouble("calories") : null);
            meal.setSugar(rs.getObject("sugar") != null ? rs.getDouble("sugar") : null);
            meal.setProtein(rs.getObject("protein") != null ? rs.getDouble("protein") : null);
            meal.setStressInsight(rs.getString("stress_insight"));
            
            org.example.user.model.User user = new org.example.user.model.User();
            user.setId(rs.getInt("user_id"));
            meal.setUser(user);
            
            list.add(meal);
        }
        return list;
    }

    public List<Meal> getByUserId(int userId) throws SQLException {
        List<Meal> list = new ArrayList<>();
        String query = "SELECT * FROM meal WHERE user_id = ? ORDER BY create_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Meal meal = new Meal();
                    meal.setId(rs.getInt("id"));
                    meal.setImageName(rs.getString("image_name"));
                    meal.setDescription(rs.getString("description"));
                    meal.setAiAnalysis(rs.getString("ai_analysis"));
                    meal.setCreatedAt(rs.getTimestamp("create_at").toLocalDateTime());
                    meal.setCalories(rs.getObject("calories") != null ? rs.getDouble("calories") : null);
                    meal.setSugar(rs.getObject("sugar") != null ? rs.getDouble("sugar") : null);
                    meal.setProtein(rs.getObject("protein") != null ? rs.getDouble("protein") : null);
                    meal.setStressInsight(rs.getString("stress_insight"));
                    list.add(meal);
                }
            }
        }
        return list;
    }

    /**
     * Filters a list of meals based on search text and calorie bracket.
     * This method is extracted from the controller to allow unit testing.
     */
    public List<Meal> filterMeals(List<Meal> meals, String searchText, String calorieFilter) {
        String lowerSearch = searchText.toLowerCase();
        List<Meal> filtered = new ArrayList<>();

        for (Meal meal : meals) {
            // Apply Search Filter
            boolean matchesSearch = meal.getDescription().toLowerCase().contains(lowerSearch) ||
                    (meal.getAiAnalysis() != null && meal.getAiAnalysis().toLowerCase().contains(lowerSearch));

            // Apply Calorie Filter
            boolean matchesCalorie = true;
            if (calorieFilter != null && !calorieFilter.isEmpty()) {
                double calories = (meal.getCalories() != null) ? meal.getCalories() : 0;
                if (calorieFilter.equals("Léger (< 300 kcal)"))
                    matchesCalorie = calories < 300;
                else if (calorieFilter.equals("Équilibré (300-600 kcal)"))
                    matchesCalorie = calories >= 300 && calories <= 600;
                else if (calorieFilter.equals("Riche (> 600 kcal)"))
                    matchesCalorie = calories > 600;
            }

            if (matchesSearch && matchesCalorie) {
                filtered.add(meal);
            }
        }
        return filtered;
    }
}
