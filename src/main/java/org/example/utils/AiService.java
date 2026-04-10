package org.example.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;

public class AiService {

    private static final String API_KEY = System.getenv("OPENROUTER_API_KEY");
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final HttpClient client;

    public AiService() {
        this.client = HttpClient.newHttpClient();
    }

    public String analyzeText(String prompt) {
        try {
            JSONObject body = new JSONObject();
            body.put("model", "qwen/qwen-2.5-vl-72b-instruct");
            
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);
            
            body.put("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("HTTP-Referer", "http://localhost")
                    .header("X-Title", "SosiApp")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
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
            return "Erreur AI: " + e.getMessage();
        }
    }
}
