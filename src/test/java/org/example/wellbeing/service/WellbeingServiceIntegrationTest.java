package org.example.wellbeing.service;

import org.example.wellbeing.model.UserWellBeingData;
import org.example.user.model.User;
import org.example.utils.MyConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WellbeingServiceIntegrationTest {

    private static WellbeingService service;
    private static int existingUserId;
    private static final List<Integer> dataToDelete = new ArrayList<>();

    @BeforeAll
    public static void setUp() throws SQLException {
        service = new WellbeingService();
        
        // Ensure we have a user
        try (Connection conn = MyConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM user LIMIT 1")) {
            if (rs.next()) {
                existingUserId = rs.getInt("id");
            } else {
                st.executeUpdate("INSERT INTO user (nom, prenom, email, password, telephone, age, sexe, taille, poids, handicap, roles, user_role, created_at) " +
                                 "VALUES ('analyst', 'test', 'analyst@example.com', 'pass', '00000000', 40, 'M', 1.75, 80.0, 0, '[]', 'CLIENT', NOW())", Statement.RETURN_GENERATED_KEYS);
                try (ResultSet rsUser = st.getGeneratedKeys()) {
                    if (rsUser.next()) existingUserId = rsUser.getInt(1);
                }
            }
        }
    }

    @AfterAll
    public static void tearDownAll() throws SQLException {
        for (int id : dataToDelete) {
            try {
                service.supprimer(id);
            } catch (Exception ignored) {}
        }
        
        // Cleanup test user
        try (Connection conn = MyConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM user WHERE nom = 'analyst'");
        }
    }

    @Test
    public void testStressDistributionLogic() throws SQLException {
        // 1. Clear some space or just add specific data
        User user = new User();
        user.setId(existingUserId);

        // Add a "Faible" entry
        UserWellBeingData low = new UserWellBeingData(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, user);
        low.setCreatedAt(LocalDateTime.now());
        service.ajouter(low);

        // Add an "Élevé" entry
        UserWellBeingData high = new UserWellBeingData(5, 5, 5, 5, 5, 5, 5, 5, 5, 5, user);
        high.setCreatedAt(LocalDateTime.now());
        service.ajouter(high);

        // 2. Fetch distribution
        Map<String, Integer> dist = service.getStressDistribution();

        // 3. Verify
        assertNotNull(dist);
        assertTrue(dist.containsKey("Faible"), "La distribution doit contenir au moins une entrée 'Faible'");
        assertTrue(dist.containsKey("Élevé"), "La distribution doit contenir au moins une entrée 'Élevé'");
        
        // Clean up
        service.supprimer(low.getId());
        service.supprimer(high.getId());
    }

    @Test
    public void testAverageStressTrend() throws SQLException {
        Map<java.time.LocalDate, Double> trend = service.getAverageStressTrend();
        assertNotNull(trend, "Le trend ne doit pas être nul, même s'il est vide");
    }
}
