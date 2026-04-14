package org.example.wellbeing.frontoffice.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.user.model.User;
import org.example.utils.AiService;
import org.example.utils.SessionManager;
import org.example.wellbeing.model.ChatbotMessage;
import org.example.wellbeing.model.Meal;
import org.example.wellbeing.model.UserWellBeingData;
import org.example.wellbeing.service.ChatbotMessageService;
import org.example.wellbeing.service.MealService;
import org.example.wellbeing.service.WellbeingService;


import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ChatbotController {

    @FXML private VBox messageContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField inputField;
    @FXML private Button btnHistory;
    @FXML private Button btnClose;
    @FXML private HBox typingIndicator;

    private ChatbotMessageService messageService = new ChatbotMessageService();
    private AiService aiService = new AiService();
    private MealService mealService = new MealService();
    private WellbeingService wellbeingService = new WellbeingService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            loadHistory();
        }
    }

    private void loadHistory() {
        try {
            List<ChatbotMessage> history = messageService.getByUserId(currentUser.getId());
            for (ChatbotMessage msg : history) {
                addMessageToUI(msg.getContent(), msg.getRole().equals("user"));
            }
            scrollToBottom();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSend() {
        String content = inputField.getText().trim();
        if (content.isEmpty()) return;

        inputField.clear();
        processMessage(content);
    }

    private void processMessage(String content) {
        try {
            // 1. Save User Message
            ChatbotMessage userMsg = new ChatbotMessage(content, "user", currentUser);
            messageService.saveMessage(userMsg);
            
            // 2. Add to UI
            addMessageToUI(content, true);
            scrollToBottom();

            // 3. Call AI asynchronously
            callAiService(content);
        } catch (SQLException e) {
            e.printStackTrace();
            addMessageToUI("Erreur: Impossible de sauvegarder le message.", false);
        }
    }

    private void callAiService(String content) {
        typingIndicator.setVisible(true);
        typingIndicator.setManaged(true);

        new Thread(() -> {
            try {
                // Fetch User Context
                String context = buildUserContext();
                String aiResponse = aiService.analyzeText(content, context);
                
                Platform.runLater(() -> {
                    try {
                        typingIndicator.setVisible(false);
                        typingIndicator.setManaged(false);

                        // Save and Add AI Response
                        ChatbotMessage assistantMsg = new ChatbotMessage(aiResponse, "assistant", currentUser);
                        messageService.saveMessage(assistantMsg);
                        addMessageToUI(aiResponse, false);
                        scrollToBottom();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        addMessageToUI(aiResponse, false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    typingIndicator.setVisible(false);
                    typingIndicator.setManaged(false);
                    addMessageToUI("Erreur AI: " + e.getMessage(), false);
                });
            }
        }).start();
    }

    private String buildUserContext() {
        if (currentUser == null) return "";
        
        StringBuilder sb = new StringBuilder();
        try {
            // 1. Get Wellness Data
            UserWellBeingData latestWellbeing = wellbeingService.getLatestByUserId(currentUser.getId());
            if (latestWellbeing != null) {
                sb.append("LATEST WELLBEING DATA:\n");
                sb.append("- Anxiety/Tension Score: ").append(latestWellbeing.getAnxietyTension()).append("/5\n");
                sb.append("- Sleep Problems Score: ").append(latestWellbeing.getSleepProblems()).append("/5\n");
                sb.append("- Irritability: ").append(latestWellbeing.getIrritability()).append("/5\n");
                if (latestWellbeing.getStressPrediction() != null) {
                    sb.append("- Stress Prediction: ").append(latestWellbeing.getStressPrediction().getPredictedLabel())
                      .append(" (").append(String.format("%.1f", latestWellbeing.getStressPrediction().getConfidenceScore())).append("% confidence)\n");
                    sb.append("- Recommendation: ").append(latestWellbeing.getStressPrediction().getRecommendation()).append("\n");
                }
                sb.append("\n");
            }

            // 2. Get Recent Meals
            List<Meal> recentMeals = mealService.getByUserId(currentUser.getId());
            if (recentMeals != null && !recentMeals.isEmpty()) {
                sb.append("RECENT MEALS (Last 3):\n");
                recentMeals.stream().limit(3).forEach(meal -> {
                    sb.append("- ").append(meal.getDescription())
                      .append(" (").append(meal.getCalories() != null ? meal.getCalories().intValue() : "?").append(" kcal, ")
                      .append(meal.getProtein() != null ? meal.getProtein().intValue() : "?").append("g Protein)\n");
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Note: User data unavailable due to error.";
        }
        
        return sb.toString();
    }

    private void addMessageToUI(String text, boolean isUser) {
        HBox row = new HBox();
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));

        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(300);
        
        // Clean Premium Styles (No external CSS dependency)
        if (isUser) {
            label.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-padding: 10 15; " +
                          "-fx-background-radius: 15 15 2 15; -fx-font-size: 13px; -fx-font-weight: 500;");
        } else {
            label.setStyle("-fx-background-color: white; -fx-text-fill: #1e293b; -fx-padding: 10 15; " +
                          "-fx-background-radius: 15 15 15 2; -fx-font-size: 13px; -fx-font-weight: 500; " +
                          "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        }

        row.getChildren().add(label);
        
        if (Platform.isFxApplicationThread()) {
            messageContainer.getChildren().add(row);
        } else {
            Platform.runLater(() -> messageContainer.getChildren().add(row));
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
    }

    @FXML
    private void handleClearHistory() {
        if (currentUser == null) return;
        try {
            messageService.clearHistory(currentUser.getId());
            messageContainer.getChildren().clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Node source = (Node) btnClose;
        Scene scene = source.getScene();
        if (scene != null) {
            Node container = scene.lookup("#chatbotContainer");
            if (container != null) {
                container.setVisible(false);
            }
        }
    }
}
