package org.example.wellbeing.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.wellbeing.model.Meal;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class WellbeingMealDetailsController {

    @FXML private ImageView mealImageView;
    @FXML private Label lblFullDate;
    @FXML private Label lblDescription;
    @FXML private Label valCalories;
    @FXML private Label valSugar;
    @FXML private Label valProtein;
    @FXML private Label txtAnalysis;
    @FXML private Label txtStressInsight;

    private Meal meal;
    private WellbeingControllerClientController mainController;

    private static final String UPLOAD_DIR = "src/main/resources/assets/meal_images/";

    public void setData(Meal meal, WellbeingControllerClientController mainController) {
        this.meal = meal;
        this.mainController = mainController;

        // Date format: March 4, 2026, 8:47 am
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a", Locale.ENGLISH);
        lblFullDate.setText(meal.getCreatedAt().format(formatter));

        lblDescription.setText(meal.getDescription());
        
        valCalories.setText(String.valueOf(meal.getCalories() != null ? meal.getCalories().intValue() : 0));
        valSugar.setText(String.valueOf(meal.getSugar() != null ? meal.getSugar().intValue() : 0));
        valProtein.setText(String.valueOf(meal.getProtein() != null ? meal.getProtein().intValue() : 0));
        
        txtAnalysis.setText(meal.getAiAnalysis() != null ? meal.getAiAnalysis() : "No analysis available.");
        txtStressInsight.setText(meal.getStressInsight() != null ? meal.getStressInsight() : "No stress insight available for this meal.");

        // Set Image
        try {
            File file = new File(UPLOAD_DIR + meal.getImageName());
            if (file.exists()) {
                mealImageView.setImage(new Image(file.toURI().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        mainController.refreshDashboard(); // This will show the dashboard again
    }

    @FXML
    private void handleEdit() {
        mainController.showMealEditForm(meal);
    }

    @FXML
    private void handleDelete() {
        mainController.deleteMeal(meal);
    }
}

