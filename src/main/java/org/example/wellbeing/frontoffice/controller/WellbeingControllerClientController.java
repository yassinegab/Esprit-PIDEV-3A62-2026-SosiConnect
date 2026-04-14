package org.example.wellbeing.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.chart.*;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.user.model.User;
import org.example.utils.SessionManager;
import org.example.wellbeing.model.UserWellBeingData;
import org.example.wellbeing.model.Meal;
import org.example.wellbeing.model.StressPrediction;
import org.example.wellbeing.service.WellbeingService;
import org.example.wellbeing.service.StressPredictionService;
import org.example.wellbeing.service.MealService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class WellbeingControllerClientController {

    @FXML
    private StackPane mainContainer;
    @FXML
    private VBox dashboardView;
    @FXML
    private ScrollPane formView;
    @FXML
    private VBox mealViewContainer;
    @FXML
    private StackPane mealContentArea;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private FlowPane mealsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterCombo;
    @FXML
    private VBox chatbotContainer;
    @FXML
    private Button chatbotButton;
    @FXML
    private javafx.scene.shape.Circle chatbotBadge;

    @FXML
    private AreaChart<String, Number> stressTrendChart;
    @FXML
    private LineChart<String, Number> moodTrendChart;
    @FXML
    private BarChart<String, Number> calorieChart;
    @FXML
    private BarChart<String, Number> frequencyChart;

    @FXML
    private Label lblCurrentStress;
    @FXML
    private Label lblMoodStatus;
    @FXML
    private Label lblTodayCalories;
    @FXML
    private Label lblTotalLogs;

    @FXML
    private Slider sldWorkEnvironment;
    @FXML
    private Slider sldSleepProblems;
    @FXML
    private Slider sldHeadaches;
    @FXML
    private Slider sldRestlessness;
    @FXML
    private Slider sldHeartbeatPalpitations;
    @FXML
    private Slider sldLowAcademicConfidence;
    @FXML
    private Slider sldClassAttendance;
    @FXML
    private Slider sldAnxietyTension;
    @FXML
    private Slider sldIrritability;
    @FXML
    private Slider sldSubjectConfidence;

    private WellbeingService wellbeingService = new WellbeingService();
    private StressPredictionService predictionService = new StressPredictionService();
    private MealService mealService = new MealService();
    private org.example.wellbeing.service.ChatbotMessageService messageService = new org.example.wellbeing.service.ChatbotMessageService();
    private UserWellBeingData editingData;

    @FXML
    public void initialize() {
        showDashboard();
        setupFilters();
        checkForUnreadAlerts();
    }

    private void setupFilters() {
        filterCombo.getItems().addAll("Tous les repas", "Léger (< 300 kcal)", "Équilibré (300-600 kcal)",
                "Riche (> 600 kcal)");
        filterCombo.setValue("Tous les repas");

        // Real-time search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadCards());

        // Filter change listener
        filterCombo.valueProperty().addListener((observable, oldValue, newValue) -> loadCards());
    }

    @FXML
    private void showForm() {
        this.editingData = null; // Clear edit mode
        dashboardView.setVisible(false);
        formView.setVisible(true);
    }

    public void showEditForm(UserWellBeingData data) {
        this.editingData = data;
        sldWorkEnvironment.setValue(data.getWorkEnvironment());
        sldSleepProblems.setValue(data.getSleepProblems());
        sldHeadaches.setValue(data.getHeadaches());
        sldRestlessness.setValue(data.getRestlessness());
        sldHeartbeatPalpitations.setValue(data.getHeartbeatPalpitations());
        sldLowAcademicConfidence.setValue(data.getLowAcademicConfidence());
        sldClassAttendance.setValue(data.getClassAttendance());
        sldAnxietyTension.setValue(data.getAnxietyTension());
        sldIrritability.setValue(data.getIrritability());
        sldSubjectConfidence.setValue(data.getSubjectConfidence());

        dashboardView.setVisible(false);
        formView.setVisible(true);
    }

    public void refreshDashboard() {
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        hideAllViews();
        dashboardView.setVisible(true);
        loadCards();
    }

    @FXML
    private void showMealAnalysis() {
        hideAllViews();
        mealViewContainer.setVisible(true);

        // Always reload or get controller to reset state
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wellbeing/frontoffice/WellbeingMealView.fxml"));
            Parent mealView = loader.load();
            mealContentArea.getChildren().clear();
            mealContentArea.getChildren().add(mealView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleChatbot() {
        if (chatbotContainer.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/wellbeing/frontoffice/ChatbotView.fxml"));
                Parent chatView = loader.load();
                chatbotContainer.getChildren().add(chatView);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le chatbot.");
                return;
            }
        }

        boolean isVisible = chatbotContainer.isVisible();
        chatbotContainer.setVisible(!isVisible);

        if (!isVisible) {
            chatbotContainer.toFront();
            if (chatbotBadge != null) chatbotBadge.setVisible(false);
            // Ensure first child (the chat view) is visible if it was hidden by its own controller
            if (!chatbotContainer.getChildren().isEmpty()) {
                chatbotContainer.getChildren().get(0).setVisible(true);
            }
            chatbotButton.toFront();
        }
    }

    public void showMealDetails(Meal meal) {
        hideAllViews();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/wellbeing/frontoffice/WellbeingMealDetailsView.fxml"));
            Parent detailsView = loader.load();

            WellbeingMealDetailsController controller = loader.getController();
            controller.setData(meal, this);

            mainContainer.getChildren().add(detailsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMealEditForm(Meal meal) {
        showMealAnalysis(); // This shows the form container
        try {
            // Get the controller of the loaded WellbeingMealView
            WellbeingMealController controller = null;
            if (!mealContentArea.getChildren().isEmpty()) {
                // If already loaded, we need to find the controller.
                // Alternatively, just reload it to be sure.
                mealContentArea.getChildren().clear();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wellbeing/frontoffice/WellbeingMealView.fxml"));
            Parent mealView = loader.load();
            mealContentArea.getChildren().add(mealView);

            controller = loader.getController();
            controller.setEditData(meal);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteMeal(Meal meal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer ce repas ?");
        alert.setContentText("Cette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Delete from DB
                mealService.supprimer(meal.getId());

                // Delete Image File
                File imageFile = new File("src/main/resources/assets/meal_images/" + meal.getImageName());
                if (imageFile.exists()) {
                    imageFile.delete();
                }

                refreshDashboard();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le repas.");
            }
        }
    }

    private void hideAllViews() {
        dashboardView.setVisible(false);
        formView.setVisible(false);
        mealViewContainer.setVisible(false);
        // Remove any dynamically added detail views
        mainContainer.getChildren()
                .removeIf(node -> node != dashboardView && node != formView && node != mealViewContainer);
    }

    private void loadCards() {
        cardsContainer.getChildren().clear();
        mealsContainer.getChildren().clear();
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null)
            return;

        try {
            // Load Stress Cards
            List<UserWellBeingData> list = wellbeingService.afficher();
            for (UserWellBeingData data : list) {
                if (data.getUser() != null && data.getUser().getId() == currentUser.getId()) {
                    addCardToDashboard(data);
                }
            }

            // Load Meal Cards with Filtering
            List<Meal> mealList = mealService.getByUserId(currentUser.getId());
            String searchText = searchField.getText();
            String filter = filterCombo.getValue();

            List<Meal> filteredView = mealService.filterMeals(mealList, searchText, filter);

            for (Meal meal : filteredView) {
                addMealCardToDashboard(meal);
            }

            // Populate Statistics Charts
            populateCharts(currentUser.getId(), list, mealList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCardToDashboard(UserWellBeingData data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wellbeing/frontoffice/WellbeingCard.fxml"));
            Parent card = loader.load();

            WellbeingCardController cardController = loader.getController();
            cardController.setData(data, this);

            cardsContainer.getChildren().add(card); // Add stress card
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateCharts(int userId, List<UserWellBeingData> wellbeingData, List<Meal> mealData) {
        try {
            // 1. Stress Trend
            XYChart.Series<String, Number> stressSeries = new XYChart.Series<>();
            stressSeries.setName("Stress Score");

            List<StressPrediction> predictions = predictionService.getByUserId(userId);
            for (StressPrediction sp : predictions) {
                String date = sp.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"));
                XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(date, sp.getConfidenceScore());
                stressSeries.getData().add(dataPoint);
            }
            stressTrendChart.getData().clear();
            stressTrendChart.getData().add(stressSeries);

            if (!predictions.isEmpty()) {
                double lastScore = predictions.get(predictions.size() - 1).getConfidenceScore();
                lblCurrentStress.setText(String.format("%.1f%%", lastScore));
            }

            // 2. Mood Trends
            XYChart.Series<String, Number> anxietySeries = new XYChart.Series<>();
            anxietySeries.setName("Anxiety");
            XYChart.Series<String, Number> irritabilitySeries = new XYChart.Series<>();
            irritabilitySeries.setName("Irritability");

            int lastAnxiety = 0;
            int lastIrritability = 0;

            for (UserWellBeingData data : wellbeingData) {
                if (data.getUser().getId() == userId) {
                    String date = data.getCreatedAt()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"));
                    anxietySeries.getData().add(new XYChart.Data<>(date, data.getAnxietyTension()));
                    irritabilitySeries.getData().add(new XYChart.Data<>(date, data.getIrritability()));
                    lastAnxiety = data.getAnxietyTension();
                    lastIrritability = data.getIrritability();
                }
            }
            moodTrendChart.getData().clear();
            moodTrendChart.getData().addAll(anxietySeries, irritabilitySeries);
            lblMoodStatus.setText(lastAnxiety > 3 ? "Tendu" : "Calme");

            // 3. Calorie History
            XYChart.Series<String, Number> calorieSeries = new XYChart.Series<>();
            java.util.Map<java.time.LocalDate, Double> calMap = new java.util.TreeMap<>();
            java.time.LocalDate today = java.time.LocalDate.now();
            double todayTotal = 0;

            for (Meal m : mealData) {
                java.time.LocalDate date = m.getCreatedAt().toLocalDate();
                double cal = (m.getCalories() != null ? m.getCalories() : 0);
                calMap.put(date, calMap.getOrDefault(date, 0.0) + cal);
                if (date.equals(today))
                    todayTotal += cal;
            }
            for (java.util.Map.Entry<java.time.LocalDate, Double> entry : calMap.entrySet()) {
                calorieSeries.getData()
                        .add(new XYChart.Data<>(
                                entry.getKey().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")),
                                entry.getValue()));
            }
            calorieChart.getData().clear();
            calorieChart.getData().add(calorieSeries);
            lblTodayCalories.setText(String.format("%.0f kcal", todayTotal));

            // 4. Log Frequency
            XYChart.Series<String, Number> freqSeries = new XYChart.Series<>();
            java.util.Map<java.time.LocalDate, Integer> freqMap = new java.util.TreeMap<>();
            int totalLogs = 0;
            for (UserWellBeingData d : wellbeingData) {
                if (d.getUser().getId() == userId) {
                    freqMap.put(d.getCreatedAt().toLocalDate(),
                            freqMap.getOrDefault(d.getCreatedAt().toLocalDate(), 0) + 1);
                    totalLogs++;
                }
            }
            for (Meal m : mealData) {
                freqMap.put(m.getCreatedAt().toLocalDate(),
                        freqMap.getOrDefault(m.getCreatedAt().toLocalDate(), 0) + 1);
                totalLogs++;
            }

            for (java.util.Map.Entry<java.time.LocalDate, Integer> entry : freqMap.entrySet()) {
                freqSeries.getData()
                        .add(new XYChart.Data<>(
                                entry.getKey().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")),
                                entry.getValue()));
            }
            frequencyChart.getData().clear();
            frequencyChart.getData().add(freqSeries);
            lblTotalLogs.setText(totalLogs + " logs");

            // Add Tooltips after series are added and rendered
            javafx.application.Platform.runLater(() -> {
                addTooltips(stressSeries, "%");
                addTooltips(anxietySeries, "/5");
                addTooltips(irritabilitySeries, "/5");
                addTooltips(calorieSeries, "kcal");
                addTooltips(freqSeries, "entries");
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTooltips(XYChart.Series<String, Number> series, String unit) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                Tooltip tooltip = new Tooltip(data.getYValue() + " " + unit);
                Tooltip.install(data.getNode(), tooltip);
                data.getNode().setCursor(javafx.scene.Cursor.HAND);

                // Add hover effect
                data.getNode().setOnMouseEntered(e -> data.getNode().setScaleX(1.2));
                data.getNode().setOnMouseExited(e -> data.getNode().setScaleX(1.0));
            }
        }
    }

    private void addMealCardToDashboard(Meal meal) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wellbeing/frontoffice/WellbeingMealCard.fxml"));
            Parent card = loader.load();

            WellbeingMealCardController cardController = loader.getController();
            cardController.setData(meal, this);

            mealsContainer.getChildren().add(card); // Add meal card
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté.");
            return;
        }

        UserWellBeingData data = new UserWellBeingData(
                (int) sldWorkEnvironment.getValue(),
                (int) sldSleepProblems.getValue(),
                (int) sldHeadaches.getValue(),
                (int) sldRestlessness.getValue(),
                (int) sldHeartbeatPalpitations.getValue(),
                (int) sldLowAcademicConfidence.getValue(),
                (int) sldClassAttendance.getValue(),
                (int) sldAnxietyTension.getValue(),
                (int) sldIrritability.getValue(),
                (int) sldSubjectConfidence.getValue(),
                currentUser);

        try {
            if (editingData != null) {
                editingData.setWorkEnvironment((int) sldWorkEnvironment.getValue());
                editingData.setSleepProblems((int) sldSleepProblems.getValue());
                editingData.setHeadaches((int) sldHeadaches.getValue());
                editingData.setRestlessness((int) sldRestlessness.getValue());
                editingData.setHeartbeatPalpitations((int) sldHeartbeatPalpitations.getValue());
                editingData.setLowAcademicConfidence((int) sldLowAcademicConfidence.getValue());
                editingData.setClassAttendance((int) sldClassAttendance.getValue());
                editingData.setAnxietyTension((int) sldAnxietyTension.getValue());
                editingData.setIrritability((int) sldIrritability.getValue());
                editingData.setSubjectConfidence((int) sldSubjectConfidence.getValue());

                wellbeingService.modifier(editingData);
                StressPrediction prediction = predictionService.predict(editingData);
                editingData = null;

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation mise à jour !");
            } else {
                UserWellBeingData newData = new UserWellBeingData(
                        (int) sldWorkEnvironment.getValue(),
                        (int) sldSleepProblems.getValue(),
                        (int) sldHeadaches.getValue(),
                        (int) sldRestlessness.getValue(),
                        (int) sldHeartbeatPalpitations.getValue(),
                        (int) sldLowAcademicConfidence.getValue(),
                        (int) sldClassAttendance.getValue(),
                        (int) sldAnxietyTension.getValue(),
                        (int) sldIrritability.getValue(),
                        (int) sldSubjectConfidence.getValue(),
                        currentUser);
                wellbeingService.ajouter(newData);
                StressPrediction prediction = predictionService.predict(newData);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation enregistrée !");
            }

            showDashboard();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la sauvegarde.");
        }
    }

    private void checkForUnreadAlerts() {
        User user = org.example.utils.SessionManager.getCurrentUser();
        if (user == null) return;
        
        try {
            List<org.example.wellbeing.model.ChatbotMessage> history = messageService.getByUserId(user.getId());
            if (!history.isEmpty()) {
                org.example.wellbeing.model.ChatbotMessage lastMsg = history.get(history.size() - 1);
                // Check if the last message is an admin alert
                if ("assistant".equals(lastMsg.getRole()) && lastMsg.getContent().startsWith("⚠️ ASSISTANCE ADMIN")) {
                    if (chatbotBadge != null) chatbotBadge.setVisible(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
