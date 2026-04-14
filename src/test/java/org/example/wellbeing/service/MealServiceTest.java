package org.example.wellbeing.service;

import org.example.wellbeing.model.Meal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MealServiceTest {

    private MealService service;
    private List<Meal> testData;

    @BeforeEach
    public void setUp() {
        service = new MealService();
        testData = new ArrayList<>();

        Meal m1 = new Meal();
        m1.setDescription("Salade césar");
        m1.setCalories(250.0);
        m1.setAiAnalysis("Un plat léger mais complet.");

        Meal m2 = new Meal();
        m2.setDescription("Burger frites");
        m2.setCalories(850.0);
        m2.setAiAnalysis("Un repas riche en lipides.");

        Meal m3 = new Meal();
        m3.setDescription("Pasta Carbonara");
        m3.setCalories(450.0);
        m3.setAiAnalysis("Un plat équilibré en glucides.");

        testData.add(m1);
        testData.add(m2);
        testData.add(m3);
    }

    @Test
    public void testFilteringBySearchText() {
        List<Meal> results = service.filterMeals(testData, "Salade", "");
        assertEquals(1, results.size());
        assertEquals("Salade césar", results.get(0).getDescription());
    }

    @Test
    public void testFilteringByCalorieLéger() {
        List<Meal> results = service.filterMeals(testData, "", "Léger (< 300 kcal)");
        assertEquals(1, results.size());
        assertEquals(250.0, results.get(0).getCalories());
    }

    @Test
    public void testFilteringByCalorieÉquilibré() {
        List<Meal> results = service.filterMeals(testData, "", "Équilibré (300-600 kcal)");
        assertEquals(1, results.size());
        assertEquals(450.0, results.get(0).getCalories());
    }

    @Test
    public void testFilteringByCalorieRiche() {
        List<Meal> results = service.filterMeals(testData, "", "Riche (> 600 kcal)");
        assertEquals(1, results.size());
        assertEquals(850.0, results.get(0).getCalories());
    }

    @Test
    public void testCombinedFiltering() {
        // Search for "Pasta" in "Riche" category (should be empty)
        List<Meal> results = service.filterMeals(testData, "Pasta", "Riche (> 600 kcal)");
        assertEquals(0, results.size());
    }
}
