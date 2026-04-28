package org.example.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OpenAITtsService {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static final String API_KEY = dotenv.get("OPENAI_TTS_API_KEY");
    private static final String API_URL = "https://api.openai.com/v1/audio/speech";

    private final HttpClient client;

    public OpenAITtsService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public InputStream generateSpeech(String text) throws Exception {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("OpenAI TTS API Key is missing in .env");
        }

        int maxRetries = 2;
        int retryCount = 0;
        long waitTime = 1000; // 1 second start

        while (retryCount <= maxRetries) {
            JSONObject body = new JSONObject();
            body.put("model", "tts-1");
            body.put("input", text);
            body.put("voice", "nova");
            body.put("response_format", "mp3");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                return response.body();
            } else if (response.statusCode() == 429 && retryCount < maxRetries) {
                retryCount++;
                System.out.println("TTS Rate limit hit. Retrying in " + waitTime + "ms... (Attempt " + retryCount + ")");
                Thread.sleep(waitTime);
                waitTime *= 2; // Exponential backoff
            } else if (response.statusCode() == 429) {
                throw new Exception("Rate limit reached (429). Please wait a moment before trying again.");
            } else {
                throw new Exception("TTS Error: " + response.statusCode());
            }
        }
        throw new Exception("Failed to generate speech after retries.");
    }
}
