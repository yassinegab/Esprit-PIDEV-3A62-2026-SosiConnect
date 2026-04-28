package org.example.event.service;

import org.example.event.model.Event;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class ExternalMailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private String apiKey;

    public ExternalMailService() {
        loadApiKey();
    }

    private void loadApiKey() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.err.println("Désolé, impossible de trouver application.properties");
                return;
            }
            prop.load(input);
            this.apiKey = prop.getProperty("mail.api.key");
        } catch (Exception ex) {
            System.err.println("Erreur lors du chargement de la clé API Resend : " + ex.getMessage());
        }
    }

    public void sendEventConfirmation(String userEmail, Event event, String userName) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("re_YOUR_RESEND_KEY_HERE")) {
            System.err.println("Clé API Resend manquante ou invalide. L'e-mail n'a pas été envoyé.");
            return;
        }

        try {
            String htmlContent = buildHtmlEmail(event, userName);
            String jsonPayload = buildJsonPayload("onboarding@resend.dev", userEmail, "Confirmation de participation : " + event.getTitle(), htmlContent);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("✅ E-mail de confirmation envoyé avec succès à " + userEmail);
            } else {
                System.err.println("❌ Échec de l'envoi de l'e-mail. Statut HTTP: " + response.statusCode());
                System.err.println("Réponse API : " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'e-mail de confirmation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildJsonPayload(String from, String to, String subject, String htmlBody) {
        // Simple JSON builder to avoid external dependencies like Gson/Jackson if not guaranteed
        // Assuming Jackson or Gson might not be imported in this file directly, manual escaping for safety
        String safeHtml = htmlBody.replace("\"", "\\\"").replace("\n", "").replace("\r", "");
        return String.format(
                "{\"from\":\"%s\", \"to\":[\"%s\"], \"subject\":\"%s\", \"html\":\"%s\"}",
                from, to, subject, safeHtml
        );
    }

    private String buildHtmlEmail(Event event, String userName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }" +
                ".header { background-color: #6366f1; padding: 30px 20px; text-align: center; }" +
                ".header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; }" +
                ".content { padding: 40px 30px; color: #334155; line-height: 1.6; }" +
                ".greeting { font-size: 18px; font-weight: 600; color: #1e293b; margin-bottom: 20px; }" +
                ".event-card { background-color: #f1f5f9; border-radius: 8px; padding: 20px; margin: 25px 0; border-left: 4px solid #6366f1; }" +
                ".event-title { font-size: 20px; font-weight: bold; color: #0f172a; margin-top: 0; }" +
                ".event-detail { margin: 10px 0; font-size: 15px; }" +
                ".label { font-weight: bold; color: #475569; }" +
                ".notice { background-color: #fffbeb; border: 1px solid #fef3c7; border-radius: 8px; padding: 15px; color: #92400e; font-size: 14px; margin-top: 25px; }" +
                ".footer { background-color: #f8fafc; padding: 20px; text-align: center; color: #94a3b8; font-size: 13px; border-top: 1px solid #e2e8f0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "  <div class='header'>" +
                "    <h1>Confirmation de Participation</h1>" +
                "  </div>" +
                "  <div class='content'>" +
                "    <div class='greeting'>Bonjour " + userName + ",</div>" +
                "    <p>Nous vous remercions pour l'intérêt que vous portez à nos événements. Votre participation a été enregistrée avec succès !</p>" +
                "    <div class='event-card'>" +
                "      <h2 class='event-title'>" + event.getTitle() + "</h2>" +
                "      <div class='event-detail'><span class='label'>📅 Date :</span> " + event.getDate() + "</div>" +
                "      <div class='event-detail'><span class='label'>🏷️ Type :</span> " + event.getType() + "</div>" +
                "      <div class='event-detail' style='margin-top: 15px;'><span class='label'>📝 Description :</span><br>" + event.getDescription() + "</div>" +
                "    </div>" +
                "    <div class='notice'>" +
                "      <strong>📍 Information importante :</strong> La localisation exacte de cet événement vous sera communiquée très prochainement. Elle sera également disponible directement dans votre application." +
                "    </div>" +
                "    <p style='margin-top: 30px;'>Nous avons hâte de vous y retrouver !</p>" +
                "  </div>" +
                "  <div class='footer'>" +
                "    © 2024 SosiConnect. Tous droits réservés." +
                "  </div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
