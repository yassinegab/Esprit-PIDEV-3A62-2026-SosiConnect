package org.example.chatbot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AiService {

    @Value("${gemini.api.key:YOUR_GEMINI_API_KEY_HERE}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private static final String SYSTEM_PROMPT = "Tu es un assistant intelligent spécialisé en santé féminine et cycle menstruel. "
            +
            "Tu aides les utilisatrices à comprendre leurs règles, leur cycle, leur ovulation et leur bien-être. " +
            "Tu réponds de manière simple, douce et empathique. Tu ne fais jamais de diagnostic médical. " +
            "Si un symptôme est grave, tu conseilles de consulter un médecin. " +
            "Si une question n'est pas liée à la santé féminine, au cycle menstruel, aux règles, à l'ovulation, à la fertilité ou au bien-être féminin, tu dis que tu ne peux pas répondre à cette question. (Ne réponds pas du tout à la question dans ce cas).";

    public String getChatbotResponse(String userMessage) {
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY_HERE") || apiKey.isEmpty()) {
            return "Configuration: Veuillez configurer GEMINI_API_KEY dans application.properties ou dans les variables d'environnement.";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            JSONObject systemInstruction = new JSONObject();
            JSONObject systemParts = new JSONObject();
            systemParts.put("text", SYSTEM_PROMPT);
            systemInstruction.put("parts", new JSONArray().put(systemParts));

            JSONObject requestBody = new JSONObject();
            requestBody.put("systemInstruction", systemInstruction);

            JSONObject contents = new JSONObject();
            JSONObject parts = new JSONObject();
            parts.put("text", userMessage);
            contents.put("parts", new JSONArray().put(parts));

            requestBody.put("contents", new JSONArray().put(contents));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                return jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            } else {
                return "Erreur API : " + response.statusCode() + " - " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur s'est produite lors de la connexion à l'IA.";
        }
    }
}
