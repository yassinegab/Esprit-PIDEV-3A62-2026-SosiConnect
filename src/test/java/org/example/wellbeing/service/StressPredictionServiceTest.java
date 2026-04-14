package org.example.wellbeing.service;

import org.example.wellbeing.model.UserWellBeingData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StressPredictionServiceTest {

    private StressPredictionService service;

    @BeforeEach
    public void setUp() {
        // We use a partial instance or mock if possible, 
        // but since we extracted static-like methods, we can just instantiate.
        // Even if DB connection fails in constructor, we try-catch it there.
        service = new StressPredictionService();
    }

    @Test
    public void testLowStressCalculation() {
        // Scenario: All indicators at minimum (1)
        UserWellBeingData data = new UserWellBeingData(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, null);
        
        double score = service.calculateNormalizedScore(data);
        String label = service.classifyStress(score);
        
        assertEquals(20.0, score, 0.1, "Minimum values should result in 20% score (1/5)");
        assertEquals("Low", label, "20% should be classified as Low stress");
    }

    @Test
    public void testHighStressCalculation() {
        // Scenario: All indicators at maximum (5)
        UserWellBeingData data = new UserWellBeingData(5, 5, 5, 5, 5, 5, 5, 5, 5, 5, null);
        
        double score = service.calculateNormalizedScore(data);
        String label = service.classifyStress(score);
        
        assertEquals(100.0, score, 0.1, "Maximum values should result in 100% score");
        assertEquals("High", label, "100% should be classified as High stress");
    }

    @Test
    public void testModerateStressCalculation() {
        // Scenario: Indicators at middle (3)
        UserWellBeingData data = new UserWellBeingData(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, null);
        
        double score = service.calculateNormalizedScore(data);
        String label = service.classifyStress(score);
        
        assertEquals(60.0, score, 0.1, "Middle values (3/5) should result in 60% score");
        assertEquals("Moderate", label, "60% should be classified as Moderate stress");
    }

    @Test
    public void testWeightedStressThreshold() {
        // Scenario: High anxiety/tension (5) but everything else low (1)
        UserWellBeingData data = new UserWellBeingData(1, 1, 1, 1, 1, 1, 1, 5, 1, 1, null);
        
        double score = service.calculateNormalizedScore(data);
        String label = service.classifyStress(score);
        
        assertTrue(score > 20.0, "Score should be higher than baseline 20%");
        assertEquals("Low", label, "31.7% should still be Low stress");
    }

    @Test
    public void testEdgeCaseCalculation() {
        // Scenario: Values at zero (should not happen in UI but could in code)
        UserWellBeingData data = new UserWellBeingData(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null);
        double score = service.calculateNormalizedScore(data);
        assertEquals(0.0, score, "Zero inputs should result in 0% score");
        assertEquals("Low", service.classifyStress(score));

        // Scenario: Values out of bounds (10)
        data = new UserWellBeingData(10, 10, 10, 10, 10, 10, 10, 10, 10, 10, null);
        score = service.calculateNormalizedScore(data);
        assertEquals(200.0, score, "Inputs of 10 should result in 200% score (algorithm is linear)");
        assertEquals("High", service.classifyStress(score));
    }

    @Test
    public void testNullSafety() {
        // Verification that the service handles unexpected null gracefully (if possible)
        // Note: Field primitives (int) in model won't be null, but object references might.
        assertThrows(NullPointerException.class, () -> {
            service.calculateNormalizedScore(null);
        }, "Should throw NPE if data is null");
    }
}
