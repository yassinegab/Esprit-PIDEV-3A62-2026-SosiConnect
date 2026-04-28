package org.example.wellbeing.frontoffice.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.example.utils.*;
import org.example.wellbeing.service.EmotionAnalysisService;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatwellAssistantController {

    @FXML private WebView webView;
    @FXML private VBox messageContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField inputField;
    @FXML private Button btnRecord;
    @FXML private Button btnClose;
    @FXML private HBox emotionBox;
    @FXML private Label lblMood;
    @FXML private Label lblStress;
    @FXML private Circle statusCircle;

    private final AiService aiService = new AiService();
    private final DeepgramService sttService = new DeepgramService();
    private final OpenAITtsService ttsService = new OpenAITtsService();
    private final EmotionAnalysisService emotionService = new EmotionAnalysisService();
    
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    
    // Keep a strong reference to MediaPlayer to prevent premature garbage collection
    private javafx.scene.media.MediaPlayer currentMediaPlayer;
    
    // Audio Recording
    private TargetDataLine targetLine;
    private boolean isRecording = false;
    private ByteArrayOutputStream audioOutput;

    @FXML
    public void initialize() {
        setupWebView();
        addMessage("assistant", "Hello! I'm ChatWell, your wellness companion. How can I support you today?");
    }

    private void setupWebView() {
        String url = getClass().getResource("/org/example/wellbeing/avatar_view.html").toExternalForm();
        webView.getEngine().load(url);
        
        // Background transparency (workaround for JavaFX WebView)
        webView.setPageFill(Color.TRANSPARENT);
    }

    private void setAvatarTalking(boolean talking) {
        Platform.runLater(() -> {
            webView.getEngine().executeScript("setTalking(" + talking + ")");
        });
    }

    private void setAvatarStatus(String status) {
        Platform.runLater(() -> {
            webView.getEngine().executeScript("setStatus('" + status + "')");
        });
    }

    @FXML
    private void handleSend() {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            inputField.clear();
            processUserInput(text);
        }
    }

    @FXML
    private void handleRecord() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                showError("Microphone not supported.");
                return;
            }

            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            targetLine.start();
            
            isRecording = true;
            btnRecord.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 50%; -fx-min-width: 60; -fx-min-height: 60;");
            setAvatarStatus("Listening...");
            statusCircle.setFill(Color.RED);

            audioOutput = new ByteArrayOutputStream();
            
            executor.execute(() -> {
                byte[] buffer = new byte[1024];
                while (isRecording) {
                    int read = targetLine.read(buffer, 0, buffer.length);
                    if (read > 0) audioOutput.write(buffer, 0, read);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Mic entry error: " + e.getMessage());
        }
    }

    private void stopRecording() {
        isRecording = false;
        if (targetLine != null) {
            targetLine.stop();
            targetLine.close();
        }

        btnRecord.setStyle("-fx-background-color: #6366f1; -fx-background-radius: 50%; -fx-min-width: 60; -fx-min-height: 60;");
        statusCircle.setFill(Color.web("#10b981"));
        setAvatarStatus("Processing...");

        processAudioInput();
    }

    private void processAudioInput() {
        executor.execute(() -> {
            try {
                byte[] audioData = audioOutput.toByteArray();
                // Wrap in WAV for Deepgram
                byte[] wavData = convertToWav(audioData);
                
                String transcript = sttService.transcribe(wavData);
                Platform.runLater(() -> processUserInput(transcript));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Failed to understand: " + e.getMessage()));
            }
        });
    }

    private byte[] convertToWav(byte[] audioData) throws IOException {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream ais = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, out);
        return out.toByteArray();
    }

    private void processUserInput(String text) {
        addMessage("user", text);
        setAvatarStatus("Thinking...");
        
        executor.execute(() -> {
            try {
                // 1. Get AI Response
                String response = aiService.analyzeText(text);
                
                // 2. Perform Emotion Analysis
                EmotionAnalysisService.EmotionResult emotion = emotionService.analyze(text);
                
                Platform.runLater(() -> {
                    addMessage("assistant", response);
                    updateEmotionDisplay(emotion);
                    setAvatarStatus("Speaking...");
                });

                // 3. TTS and Playback
                playTts(response);

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("AI Error: " + e.getMessage()));
            }
        });
    }

    private final DeepgramTtsService deepgramTtsService = new DeepgramTtsService();

    private void playTts(String text) {
        executor.execute(() -> {
            InputStream audioStream = null;
            try {
                // Try OpenAI First
                try {
                    audioStream = ttsService.generateSpeech(text);
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("429")) {
                        System.out.println("OpenAI Throttled, falling back to Deepgram...");
                        audioStream = deepgramTtsService.generateSpeech(text);
                    } else {
                        throw e;
                    }
                }
                
                if (audioStream == null) return;

                File tempFile = File.createTempFile("chatwell_tts", ".mp3");
                tempFile.deleteOnExit();
                
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = audioStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                }

                Platform.runLater(() -> {
                    setAvatarTalking(true);
                    javafx.scene.media.Media media = new javafx.scene.media.Media(tempFile.toURI().toString());
                    
                    if (currentMediaPlayer != null) {
                        currentMediaPlayer.stop();
                        currentMediaPlayer.dispose();
                    }
                    
                    currentMediaPlayer = new javafx.scene.media.MediaPlayer(media);
                    currentMediaPlayer.setOnEndOfMedia(() -> {
                        setAvatarTalking(false);
                        setAvatarStatus("Ready");
                    });
                    currentMediaPlayer.setOnError(() -> {
                        System.err.println("Media Player Error: " + currentMediaPlayer.getError().getMessage());
                        setAvatarTalking(false);
                    });
                    currentMediaPlayer.play();
                });

            } catch (Exception e) {
                if (e.getMessage() == null || !e.getMessage().contains("429")) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    setAvatarTalking(false);
                    if (e.getMessage() != null && (e.getMessage().contains("429") || e.getMessage().contains("Deepgram TTS Error"))) {
                        showError("I'm having a little trouble with my voice right now, but I can still chat with you here!");
                    }
                });
            }
        });
    }

    private void updateEmotionDisplay(EmotionAnalysisService.EmotionResult emotion) {
        Platform.runLater(() -> {
            emotionBox.setVisible(true);
            lblMood.setText(emotion.mood.toUpperCase());
            lblStress.setText(emotion.stressScore + "%");
            
            String moodColor = switch(emotion.mood) {
                case "joy" -> "#10b981";
                case "anxious" -> "#f59e0b";
                case "sad" -> "#6366f1";
                case "angry" -> "#ef4444";
                default -> "#64748b";
            };
            lblMood.setStyle("-fx-text-fill: " + moodColor + "; -fx-font-weight: bold;");
        });
    }

    private void addMessage(String role, String content) {
        Platform.runLater(() -> {
            HBox hBox = new HBox();
            hBox.setAlignment(role.equals("user") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5, 0, 5, 0));

            Label label = new Label(content);
            label.setWrapText(true);
            label.setMaxWidth(300);
            label.setPadding(new Insets(10, 15, 10, 15));

            if (role.equals("user")) {
                label.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 15 15 2 15;");
            } else {
                label.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 15 15 15 2;");
            }

            hBox.getChildren().add(label);
            messageContainer.getChildren().add(hBox);
            
            scrollPane.setVvalue(1.0);
        });
    }

    private void showError(String message) {
        addMessage("assistant", "⚠️ Error: " + message);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
