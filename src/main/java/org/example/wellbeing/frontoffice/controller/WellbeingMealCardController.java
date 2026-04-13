package org.example.wellbeing.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.wellbeing.model.Meal;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class WellbeingMealCardController {

    @FXML private ImageView mealImageView;
    @FXML private Label lblDate;
    @FXML private Label lblDescription;
    @FXML private Label lblCalories;
    @FXML private Label lblProtein;
    @FXML private Label lblSugar;
    @FXML private ProgressBar caloriesProgress;
    @FXML private ProgressBar proteinProgress;
    @FXML private ProgressBar sugarProgress;

    private Meal meal;
    private WellbeingControllerClientController mainController;

    private static final String UPLOAD_DIR = "src/main/resources/assets/meal_images/";

    public void setData(Meal meal, WellbeingControllerClientController mainController) {
        this.meal = meal;
        this.mainController = mainController;

        lblDate.setText(meal.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        lblDescription.setText(meal.getDescription());

        // Set Image
        try {
            File file = new File(UPLOAD_DIR + meal.getImageName());
            if (file.exists()) {
                mealImageView.setImage(new Image(file.toURI().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Metrics
        double cal = meal.getCalories() != null ? meal.getCalories() : 0;
        double pro = meal.getProtein() != null ? meal.getProtein() : 0;
        double sug = meal.getSugar() != null ? meal.getSugar() : 0;

        lblCalories.setText((int)cal + " kcal");
        lblProtein.setText(pro + " g");
        lblSugar.setText(sug + " g");

        // Progress (assuming Max: 1000kcal, 50g protein, 50g sugar for display)
        caloriesProgress.setProgress(Math.min(cal / 1000.0, 1.0));
        proteinProgress.setProgress(Math.min(pro / 50.0, 1.0));
        sugarProgress.setProgress(Math.min(sug / 50.0, 1.0));
    }

    @FXML
    private void handleViewAI() {
        mainController.showMealDetails(meal);
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

