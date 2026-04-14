package org.example.wellbeing.service;

import org.example.wellbeing.model.Meal;
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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MealServiceIntegrationTest {

    private static MealService service;
    private static int testMealId = -1;
    private static int existingUserId;
    private static final List<Integer> mealsToDelete = new ArrayList<>();

    @BeforeAll
    public static void setUp() throws SQLException {
        service = new MealService();
        
        // Fetch an existing user ID to link the test meal
        try (Connection conn = MyConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM user LIMIT 1")) {
            
            if (rs.next()) {
                existingUserId = rs.getInt("id");
            } else {
                // If NO user exists, we create a temporary one for the test
                st.executeUpdate("INSERT INTO user (nom, prenom, email, password, telephone, age, sexe, taille, poids, handicap, roles, user_role, created_at) " +
                                 "VALUES ('testuser', 'test', 'test@example.com', 'pass', '12345678', 25, 'M', 1.80, 75.0, 0, '[]', 'CLIENT', NOW())", Statement.RETURN_GENERATED_KEYS);
                try (ResultSet rsUser = st.getGeneratedKeys()) {
                    if (rsUser.next()) existingUserId = rsUser.getInt(1);
                }
            }
        }
    }

    @AfterAll
    public static void tearDownAll() throws SQLException {
        // Nettoyage final de sécurité pour tous les IDs créés durant les tests
        for (int id : mealsToDelete) {
            try {
                service.supprimer(id);
            } catch (Exception ignored) {}
        }

        // Nettoyage de l'utilisateur de test si créé
        try (Connection conn = MyConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM user WHERE nom IN ('testuser', 'victim', 'analyst')");
        }
    }

    @Test
    @Order(1)
    public void testAjouterMeal() throws SQLException {
        User user = new User();
        user.setId(existingUserId);

        Meal meal = new Meal();
        meal.setDescription("Repas de Test JUnit");
        meal.setImageName("test_image.jpg");
        meal.setAiAnalysis("Une analyse de test très saine.");
        meal.setCalories(450.0);
        meal.setProtein(25.0);
        meal.setSugar(10.0);
        meal.setStressInsight("Manger sain réduit le stress.");
        meal.setCreatedAt(LocalDateTime.now());
        meal.setUser(user);

        service.ajouter(meal);

        assertTrue(meal.getId() > 0, "L'ID du repas devrait être généré après l'insertion");
        testMealId = meal.getId();
    }

    @Test
    @Order(2)
    public void testAfficherEtVerifier() throws SQLException {
        List<Meal> meals = service.afficher();
        
        boolean found = false;
        for (Meal m : meals) {
            if (m.getId() == testMealId) {
                assertEquals("Repas de Test JUnit", m.getDescription());
                assertEquals(450.0, m.getCalories());
                found = true;
                break;
            }
        }
        assertTrue(found, "Le repas ajouté devrait être présent dans la liste 'afficher'");
    }

    @Test
    @Order(3)
    public void testModifierMeal() throws SQLException {
        // Fetch current state
        List<Meal> meals = service.afficher();
        Meal toUpdate = null;
        for (Meal m : meals) {
            if (m.getId() == testMealId) {
                toUpdate = m;
                break;
            }
        }
        
        assertNotNull(toUpdate);
        toUpdate.setDescription("Description Modifiée par Test");
        toUpdate.setCalories(500.0);
        
        service.modifier(toUpdate);
        
        // Re-verify
        List<Meal> updatedList = service.afficher();
        for (Meal m : updatedList) {
            if (m.getId() == testMealId) {
                assertEquals("Description Modifiée par Test", m.getDescription());
                assertEquals(500.0, m.getCalories());
                return;
            }
        }
        fail("Le repas modifié n'a pas été retrouvé avec ses nouvelles valeurs");
    }

    @Test
    @Order(4)
    public void testSupprimerMeal() throws SQLException {
        service.supprimer(testMealId);
        
        // Verify deletion
        List<Meal> meals = service.afficher();
        for (Meal m : meals) {
            assertNotEquals(testMealId, m.getId(), "Le repas devrait avoir été supprimé de la base");
        }
    }

    @Test
    @Order(5)
    public void testUserIsolationSecurity() throws SQLException {
        // 1. Create a second user
        int secondUserId = -1;
        try (Connection conn = MyConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("INSERT INTO user (nom, prenom, email, password, telephone, age, sexe, taille, poids, handicap, roles, user_role, created_at) " +
                             "VALUES ('victim', 'test', 'victim@example.com', 'pass', '87654321', 30, 'F', 1.70, 65.0, 0, '[]', 'CLIENT', NOW())", Statement.RETURN_GENERATED_KEYS);
            try (ResultSet rsUser = st.getGeneratedKeys()) {
                if (rsUser.next()) secondUserId = rsUser.getInt(1);
            }
        }
        
        try {
            // 2. Add a meal for User B
            User userB = new User();
            userB.setId(secondUserId);
            Meal secretMeal = new Meal();
            secretMeal.setDescription("Repas Secret de B");
            secretMeal.setImageName("secret.jpg");
            secretMeal.setCreatedAt(LocalDateTime.now());
            secretMeal.setUser(userB);
            service.ajouter(secretMeal);
            
            // 3. User A tries to fetch their own meals
            List<Meal> userAMeals = service.getByUserId(existingUserId);
            
            // 4. Verify that User A CANNOT see User B's secret meal
            for (Meal m : userAMeals) {
                assertNotEquals("Repas Secret de B", m.getDescription(), "SÉCURITÉ : L'utilisateur A ne doit pas voir les repas de l'utilisateur B !");
            }
            
            // Clean up secret meal
            service.supprimer(secretMeal.getId());
        } finally {
            // Clean up second user
            try (Connection conn = MyConnection.getConnection();
                 Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM user WHERE id = " + secondUserId);
            }
        }
    }
}
