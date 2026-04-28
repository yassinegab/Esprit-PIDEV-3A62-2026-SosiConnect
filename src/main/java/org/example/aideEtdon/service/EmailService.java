package org.example.aideEtdon.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // PLACEHOLDERS FOR USER CONFIGURATION
    private static final String SMTP_EMAIL = "votre.email@gmail.com";
    private static final String SMTP_PASSWORD = "votre_mot_de_passe"; 

    public static void sendEmergencyAlert(String targetEmail, String typeBesoin, String timeSent, double lat, double lng) {
        
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_EMAIL, SMTP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(targetEmail));
            message.setSubject("🚨 URGENT: Alerte Médicale SOSIConnect");

            String mapsLink = "https://www.google.com/maps?q=" + lat + "," + lng;
            
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 2px solid #ef4444; border-radius: 10px;'>"
                    + "<h2 style='color: #ef4444;'>🚨 ALERTE: DEMANDE D'AIDE URGENTE</h2>"
                    + "<p>Un utilisateur dont vous êtes le contact de confiance a déclenché une alerte sociale critique.</p>"
                    + "<table style='width: 100%; text-align: left; margin: 20px 0;'>"
                    + "<tr><th>Type de Besoin:</th><td style='color: #b91c1c; font-weight: bold;'>" + typeBesoin + "</td></tr>"
                    + "<tr><th>Heure d'Alerte:</th><td>" + timeSent + "</td></tr>"
                    + "<tr><th>Coordonnées GPS:</th><td>" + lat + ", " + lng + "</td></tr>"
                    + "</table>"
                    + "<a href='" + mapsLink + "' style='background-color: #3b82f6; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>Ouvrir sur Google Maps</a>"
                    + "<p style='margin-top: 30px; font-size: 11px; color: #64748b;'>Message automatisé par SosiConnect. Ne pas répondre.</p>"
                    + "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email successfully sent to: " + targetEmail);

        } catch (MessagingException e) {
            System.err.println("Email dispatch failed exactly at: " + e.getMessage());
            // Intentionally swallowed so standard UI continues normally
        }
    }
}
