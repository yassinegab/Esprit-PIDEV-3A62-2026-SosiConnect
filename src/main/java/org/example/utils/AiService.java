package org.example.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;

public class AiService {

<<<<<<< HEAD
    private static final String API_KEY = System.getenv("OPENROUTER_API_KEY");
=======
    private static final String API_KEY = "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";
>>>>>>> main
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
