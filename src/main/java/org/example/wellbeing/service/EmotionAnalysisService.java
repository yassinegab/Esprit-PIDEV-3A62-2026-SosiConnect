package org.example.wellbeing.service;

import java.util.HashMap;
import java.util.Map;

public class EmotionAnalysisService {

    public static class EmotionResult {
        public String mood;
        public int stressScore;

        public EmotionResult(String mood, int stressScore) {
            this.mood = mood;
            this.stressScore = stressScore;
        }
    }

    public EmotionResult analyze(String text) {
        text = text.toLowerCase();
        
        int stressScore = 0;
        String mood = "neutral";
        
        // Mood keywords
        Map<String, String[]> moodKeywords = new HashMap<>();
        moodKeywords.put("joy", new String[]{"happy", "great", "excellent", "wonderful", "joy", "good", "better", "excited", "contente", "heureux"});
        moodKeywords.put("anxious", new String[]{"anxious", "stress", "worried", "nervous", "fear", "scared", "pressure", "anxiété", "stressé"});
        moodKeywords.put("sad", new String[]{"sad", "depressed", "lonely", "unhappy", "cry", "triste", "déprimé", "seul"});
        moodKeywords.put("angry", new String[]{"angry", "mad", "frustrated", "annoyed", "hate", "colère", "énervé", "frustré"});

        for (Map.Entry<String, String[]> entry : moodKeywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword)) {
                    mood = entry.getKey();
                    break;
                }
            }
        }

        // Stress Score calculation (0-100)
        String[] stressHigh = {"panic", "extreme", "burning out", "crisis", "urgent", "too much", "cannot cope"};
        String[] stressMedium = {"tired", "busy", "work", "hard", "difficult", "struggling"};
        
        for (String word : stressHigh) if (text.contains(word)) stressScore += 40;
        for (String word : stressMedium) if (text.contains(word)) stressScore += 20;

        if (mood.equals("anxious")) stressScore += 30;
        if (mood.equals("joy")) stressScore = Math.max(0, stressScore - 20);

        stressScore = Math.min(100, Math.max(5, stressScore)); // Keep it within 5-100

        return new EmotionResult(mood, stressScore);
    }
}
