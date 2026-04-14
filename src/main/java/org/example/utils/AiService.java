package org.example.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import io.github.cdimascio.dotenv.Dotenv;
import java.time.Duration;

public class AiService {

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static final String API_KEY = dotenv.get("OPENROUTER_API_KEY");
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final HttpClient client;

    public AiService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String analyzeText(String prompt) {
        return analyzeText(prompt, null);
    }

    public String analyzeText(String prompt, String context) {
        try {
            JSONObject body = new JSONObject();
            body.put("model", "qwen/qwen-2.5-vl-72b-instruct");
            
            JSONArray messages = new JSONArray();
            
            // System prompt with context if available
            StringBuilder systemContent = new StringBuilder("You are SosiApp's Wellbeing Assistant. " +
                    "You must ONLY talk about user wellbeing, health, mental health, diet, stress management, and physical activity. " +
                    "If a user asks about anything unrelated, politely explain that you are specialized in wellbeing.\n\n");
            
            if (context != null && !context.isEmpty()) {
                systemContent.append("USER DATA CONTEXT:\n").append(context).append("\n\n");
                systemContent.append("Use this data to provide personalized and accurate advice. Reference specific facts from this context when relevant.");
            }

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemContent.toString());
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);
            
            body.put("messages", messages);

            System.out.println("[AI Service] Sending request to: " + API_URL);
            System.out.println("[AI Service] API Key present: " + (API_KEY != null && !API_KEY.isEmpty()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("HTTP-Referer", "http://localhost")
                    .header("X-Title", "SosiApp")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            System.out.println("[AI Service] Request Body: " + body.toString());
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[AI Service] Response Code: " + response.statusCode());
            System.out.println("[AI Service] Response Body: " + response.body());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else {
                return "Erreur AI: Code " + response.statusCode() + " - " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur AI: " + e.getMessage();
        }
    }

    public String analyzeMeal(String imagePath, String description) {
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String extension = imagePath.substring(imagePath.lastIndexOf(".") + 1).toLowerCase();
            String mimeType = "image/jpeg";
            if (extension.equals("png")) mimeType = "image/png";
            else if (extension.equals("webp")) mimeType = "image/webp";

            JSONObject body = new JSONObject();
            body.put("model", "qwen/qwen-2.5-vl-72b-instruct");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");

            JSONArray content = new JSONArray();
            
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            String prompt = "Analyze this meal photo. ";
            if (description != null && !description.isEmpty()) {
                prompt += "User description: " + description + ". ";
            }
            prompt += "Provide nutritional insights and health recommendations. \n" +
                      "You MUST respond in JSON format with the following keys:\n" +
                      "- calories: estimated calories (number)\n" +
                      "- sugar: estimated sugar in grams (number)\n" +
                      "- protein: estimated protein in grams (number)\n" +
                      "- analysis: a concise and encouraging textual analysis\n" +
                      "- stress_link: a brief insight on how this meal might affect stress or restlessness (e.g., 'High sugar might increase restlessness').\n" +
                      "\n" +
                      "Keep it professional and empathetic.";
            textContent.put("text", prompt);
            content.put(textContent);

            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:" + mimeType + ";base64," + base64Image);
            imageContent.put("image_url", imageUrl);
            content.put(imageContent);

            message.put("content", content);
            messages.put(message);
            body.put("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("HTTP-Referer", "http://localhost")
                    .header("X-Title", "SosiApp")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else {
                return "Erreur AI: Code " + response.statusCode() + " - " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur AI Error: " + e.getMessage();
        }
    }
}
