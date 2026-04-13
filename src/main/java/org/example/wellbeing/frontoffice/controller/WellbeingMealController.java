package org.example.wellbeing.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.utils.AiService;
import org.example.utils.SessionManager;
import org.example.wellbeing.model.Meal;
import org.example.wellbeing.service.MealService;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class WellbeingMealController {

    @FXML private ImageView mealImageView;
    @FXML private VBox placeholderBox;
    @FXML private TextField descriptionField;
    @FXML private Button analyzeBtn;
    @FXML private VBox resultsBox;
    @FXML private Label caloriesLabel;
    @FXML private Label sugarLabel;
    @FXML private Label proteinLabel;
    @FXML private Label analysisText;
    @FXML private Label stressInsightText;

    private File selectedImageFile;
    private Meal editingMeal;
    private final AiService aiService = new AiService();
    private final MealService mealService = new MealService();

    // Constant for storage
    private static final String UPLOAD_DIR = "src/main/resources/assets/meal_images/";

    @FXML
    public void initialize() {
        // Create directory if it doesn't exist
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    public void setEditData(Meal meal) {
        this.editingMeal = meal;
        descriptionField.setText(meal.getDescription());
        
        // Load image
        File imageFile = new File(UPLOAD_DIR + meal.getImageName());
        if (imageFile.exists()) {
            mealImageView.setImage(new Image(imageFile.toURI().toString()));
            placeholderBox.setVisible(false);
            analyzeBtn.setDisable(false);
        }
        
        // Show existing results
        caloriesLabel.setText(String.valueOf(meal.getCalories() != null ? meal.getCalories() : 0));
        sugarLabel.setText(meal.getSugar() + " g");
        proteinLabel.setText(meal.getProtein() + " g");
        analysisText.setText(meal.getAiAnalysis());
        stressInsightText.setText(meal.getStressInsight());
        
        resultsBox.setVisible(true);
        analyzeBtn.setText("Relancer l'Analyse");
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de votre repas");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        
        Stage stage = (Stage) mealImageView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            selectedImageFile = file;
            Image image = new Image(file.toURI().toString());
            mealImageView.setImage(image);
            placeholderBox.setVisible(false);
            analyzeBtn.setDisable(false);
        }
    }

    @FXML
    private void handleAnalyze() {
        // Validation
        if (selectedImageFile == null && editingMeal == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez d'abord sélectionner une image de votre repas.");
            return;
        }

        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez ajouter une courte description pour aider l'IA.");
            descriptionField.requestFocus();
            return;
        }

        analyzeBtn.setDisable(true);
        analyzeBtn.setText("Analyse en cours...");

        // Run AI analysis in a background thread to keep UI responsive
        new Thread(() -> {
            try {
                String imagePathToAnalyze = "";
                String finalImageName = "";

                if (selectedImageFile != null) {
                    // 1. Save new file locally with unique name
                    String extension = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf("."));
                    finalImageName = UUID.randomUUID().toString() + extension;
                    File targetFile = new File(UPLOAD_DIR + finalImageName);
                    Files.copy(selectedImageFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    imagePathToAnalyze = targetFile.getAbsolutePath();
                } else if (editingMeal != null) {
                    // Re-use existing image
                    finalImageName = editingMeal.getImageName();
                    imagePathToAnalyze = new File(UPLOAD_DIR + finalImageName).getAbsolutePath();
                } else {
                    return; // Should not happen due to button disable logic
                }

                // 2. Call AI Service
                String result = aiService.analyzeMeal(imagePathToAnalyze, descriptionField.getText());
                
                final String imageNameForDB = finalImageName;

                // 3. Parse JSON (handling potential Markdown backticks from AI)
                String cleanJson = result.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();
                
                javafx.application.Platform.runLater(() -> {
                    try {
                        JSONObject data = new JSONObject(cleanJson);
                        
                        // Update UI
                        caloriesLabel.setText(String.valueOf(data.optDouble("calories", 0)));
                        sugarLabel.setText(data.optDouble("sugar", 0) + " g");
                        proteinLabel.setText(data.optDouble("protein", 0) + " g");
                        analysisText.setText(data.optString("analysis", "No analysis available."));
                        stressInsightText.setText(data.optString("stress_link", "Aucun insight sur le stress généré."));
                        
                        resultsBox.setVisible(true);

                        // 4. Save/Update to DB
                        Meal meal = (editingMeal != null) ? editingMeal : new Meal();
                        
                        meal.setImageName(imageNameForDB);
                        meal.setDescription(descriptionField.getText());

                        meal.setAiAnalysis(data.optString("analysis"));
                        meal.setCalories(data.optDouble("calories"));
                        meal.setSugar(data.optDouble("sugar"));
                        meal.setProtein(data.optDouble("protein"));
                        meal.setStressInsight(data.optString("stress_link"));
                        meal.setUser(SessionManager.getCurrentUser());

                        if (editingMeal != null) {
                            mealService.modifier(meal);
                        } else {
                            mealService.ajouter(meal);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        analysisText.setText("Erreur de formatage AI: " + result);
                        resultsBox.setVisible(true);
                    } finally {
                        analyzeBtn.setDisable(false);
                        analyzeBtn.setText("Relancer l'Analyse");
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    analyzeBtn.setDisable(false);
                    analyzeBtn.setText("Erreur: Image non sauvegardée");
                });
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
