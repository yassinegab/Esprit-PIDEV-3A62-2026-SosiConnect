package org.example.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DeepgramService {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static final String API_KEY = dotenv.get("DEEPGRAM_API_KEY");
    private static final String API_URL = "https://api.deepgram.com/v1/listen?model=nova-2&smart_format=true";

    private final HttpClient client;

    public DeepgramService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String transcribe(byte[] audioData) throws Exception {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("Deepgram API Key is missing in .env");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Token " + API_KEY)
                .header("Content-Type", "audio/wav") // Or audio/webm if supported
                .POST(HttpRequest.BodyPublishers.ofByteArray(audioData))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return json.getJSONObject("results")
                    .getJSONArray("channels")
                    .getJSONObject(0)
                    .getJSONArray("alternatives")
                    .getJSONObject(0)
                    .getString("transcript");
        } else {
            throw new Exception("STT Error: " + response.statusCode() + " - " + response.body());
        }
    }
}
