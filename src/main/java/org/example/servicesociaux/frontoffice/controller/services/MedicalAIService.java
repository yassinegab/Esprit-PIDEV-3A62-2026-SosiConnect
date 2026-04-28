package org.example.servicesociaux.frontoffice.controller.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.user.model.DossierMedical;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * MedicalAIService — Traduction Java du service PHP Symfony.
 *
 * ✅ Groq LLaMA 3.3-70B  : analyse médicale complète avec JSON structuré
 * ✅ HuggingFace BART     : classification zero-shot (format CORRIGÉ)
 * ✅ Fallback             : recommandations calculées localement si API indisponible
 * ✅ Scores dynamiques    : calculés selon les pathologies réelles
 * ✅ Médecins / hôpitaux  : matching par spécialité pertinente
 */
public class MedicalAIService {

    /* ── Config ── */
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String HF_URL   =
            "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";

    private final String GROQ_KEY;
    private final String HF_KEY;

    private final HttpClient    http   = HttpClient.newHttpClient();
    private final ObjectMapper  mapper = new ObjectMapper();

    public MedicalAIService() {
        this.GROQ_KEY = System.getenv().getOrDefault("GROQ_API_KEY", "");
        this.HF_KEY   = System.getenv().getOrDefault("HF_API_KEY",   "");
    }

    public MedicalAIService(String groqKey, String hfKey) {
        this.GROQ_KEY = groqKey;
        this.HF_KEY   = hfKey;
    }

    /* ═══════════════════════════════════════════════
       ANALYSE GROQ
    ═══════════════════════════════════════════════ */

    /**
     * Analyse complète via Groq LLaMA.
     * Retourne un JsonNode avec tous les champs structurés.
     */
    public JsonNode analyserAvecGroq(DossierMedical d) throws Exception {
        if (GROQ_KEY.isBlank()) return getFallbackJson(d);

        String prompt = buildGroqPrompt(d);
        String reqBody = mapper.writeValueAsString(Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "Tu es un médecin expert international. Réponds UNIQUEMENT en JSON français valide, "
                                        + "sans texte avant ou après. Les scores doivent être calculés selon "
                                        + "les pathologies réelles du patient."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.15,
                "max_tokens",  5000
        ));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Authorization", "Bearer " + GROQ_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200)
            throw new RuntimeException("Groq HTTP " + resp.statusCode() + ": " + resp.body());

        String content = mapper.readTree(resp.body()).at("/choices/0/message/content").asText();
        content = content.replaceAll("(?s)```json|```", "").trim();
        int s = content.indexOf('{'), e = content.lastIndexOf('}');
        if (s >= 0 && e > s) content = content.substring(s, e + 1);

        return mapper.readTree(content);
    }

    private String buildGroqPrompt(DossierMedical d) {
        return """
            **ANALYSE MÉDICALE APPROFONDIE**
            
            Patient :
            - Antécédents    : %s
            - Maladies       : %s
            - Allergies      : %s
            - Traitements    : %s
            - Diagnostics    : %s
            - Objectif       : %s
            - Activité       : %s
            
            Génère ce JSON exact (sans texte avant/après) :
            {
              "gravite": "faible|modere|eleve",
              "score_sante": <0-100>,
              "urgence": <true|false>,
              "resume_cas": "<analyse détaillée>",
              "specialites_recommandees": ["..."],
              "recommandations": ["..."],
              "examens_suggeres": ["..."],
              "facteurs_risque": ["..."],
              "plan_suivi": {
                "immediat": "...", "court_terme": "...",
                "moyen_terme": "...", "long_terme": "..."
              },
              "score_details": {
                "cardiovasculaire": <0-100>,
                "metabolique": <0-100>,
                "respiratoire": <0-100>,
                "renal": <0-100>,
                "hepatique": <0-100>,
                "neurologique": <0-100>
              },
              "rendez_vous_suggeres": [
                {"specialite":"...","delai":"Dans X jours","priorite":"urgente|normale|routine"}
              ],
              "medecins_experts": [
                {"nom":"Dr. ...","specialite":"...","institution":"...","pays":"...","expertise":"...","email":"..."}
              ],
              "hopitaux_experts": [
                {"nom":"...","ville":"...","pays":"...","specialites":["..."],"reputation":"...","site_web":"..."}
              ],
              "complications_potentielles": ["..."],
              "indicateurs_performance": {
                "adherence_traitement": <0-100>,
                "controle_pathologie": <0-100>,
                "qualite_vie": <0-100>,
                "risque_complications": <0-100>
              }
            }
            """.formatted(
                nvl(d.getAntecedentsMedicaux(), "Aucun"),
                nvl(d.getMaladiesChroniques(),  "Aucune"),
                nvl(d.getAllergies(),            "Aucune"),
                nvl(d.getTraitementsEnCours(),  "Aucun"),
                nvl(d.getDiagnostics(),         "Aucun"),
                nvl(d.getObjectifSante(),       "Non défini"),
                nvl(d.getNiveauActivite(),      "Non défini")
        );
    }

    /* ═══════════════════════════════════════════════
       ANALYSE HUGGINGFACE — CORRIGÉ
    ═══════════════════════════════════════════════ */

    /**
     * Classification zero-shot multi-dimensionnelle via HuggingFace BART.
     *
     * ✅ Format correct : { "inputs": "...", "parameters": { "candidate_labels": [...] } }
     * ✅ Gestion 503 (modèle en chargement) avec retry automatique
     */
    public Map<String, JsonNode> analyserAvecHuggingFace(DossierMedical d) throws Exception {
        if (HF_KEY.isBlank()) throw new RuntimeException("Clé HuggingFace manquante");

        String texte = buildPatientText(d);
        Map<String, JsonNode> resultats = new LinkedHashMap<>();

        Map<String, List<String>> dimensions = new LinkedHashMap<>();
        dimensions.put("gravite",     List.of("état critique", "état grave", "état modéré", "état léger", "état stable"));
        dimensions.put("cardio",      List.of("risque cardiovasculaire élevé", "risque cardiovasculaire modéré", "risque cardiovasculaire faible"));
        dimensions.put("metabolique", List.of("diabète déséquilibré", "obésité sévère", "métabolisme équilibré", "syndrome métabolique"));
        dimensions.put("respiratoire",List.of("insuffisance respiratoire", "asthme sévère", "BPCO avancée", "fonction respiratoire normale"));
        dimensions.put("urgence",     List.of("hospitalisation urgente nécessaire", "consultation rapide recommandée", "suivi ambulatoire suffisant"));

        for (Map.Entry<String, List<String>> e : dimensions.entrySet()) {
            try {
                resultats.put(e.getKey(), appelHFCategorie(texte, e.getValue()));
            } catch (Exception ex) {
                if (ex.getMessage() != null && ex.getMessage().startsWith("loading:")) {
                    // Modèle en chargement : attendre et réessayer
                    String waitStr = ex.getMessage().replace("loading:", "");
                    long wait = Math.min(Long.parseLong(waitStr.isBlank() ? "20" : waitStr), 30);
                    Thread.sleep(wait * 1000);
                    resultats.put(e.getKey(), appelHFCategorie(texte, e.getValue()));
                } else {
                    // Stocker l'erreur mais continuer les autres dimensions
                    resultats.put(e.getKey() + "_error", mapper.createObjectNode()
                            .put("error", ex.getMessage()));
                }
            }
        }
        return resultats;
    }

    /**
     * ✅ Appel HuggingFace CORRIGÉ — format zero-shot classification
     */
    private JsonNode appelHFCategorie(String texte, List<String> labels) throws Exception {
        // Format correct pour l'API HuggingFace zero-shot
        Map<String, Object> payload = Map.of(
                "inputs", texte,
                "parameters", Map.of(
                        "candidate_labels", labels,
                        "multi_label", false
                )
        );

        String reqBody = mapper.writeValueAsString(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(HF_URL))
                .header("Authorization", "Bearer " + HF_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        // 503 = modèle en chargement
        if (resp.statusCode() == 503) {
            JsonNode err = mapper.readTree(resp.body());
            String estimatedTime = err.path("estimated_time").asText("20");
            throw new RuntimeException("loading:" + estimatedTime);
        }

        if (resp.statusCode() != 200)
            throw new RuntimeException("HF HTTP " + resp.statusCode() + ": " + resp.body());

        return mapper.readTree(resp.body());
    }

    private String buildPatientText(DossierMedical d) {
        return "Patient avec les caractéristiques médicales : "
                + "antécédents : " + nvl(d.getAntecedentsMedicaux(), "aucun") + ". "
                + "Maladies chroniques : " + nvl(d.getMaladiesChroniques(), "aucune") + ". "
                + "Allergies : " + nvl(d.getAllergies(), "aucune") + ". "
                + "Traitements : " + nvl(d.getTraitementsEnCours(), "aucun") + ". "
                + "Diagnostics : " + nvl(d.getDiagnostics(), "aucun") + ". "
                + "Objectif : " + nvl(d.getObjectifSante(), "non défini") + ". "
                + "Activité : " + nvl(d.getNiveauActivite(), "inconnu") + ".";
    }

    /* ═══════════════════════════════════════════════
       MATCHING SPÉCIALITÉS (traduit du PHP)
    ═══════════════════════════════════════════════ */

    /**
     * Vérifie si deux spécialités sont sémantiquement proches.
     * Traduit de la méthode PHP isRelevantSpecialty().
     */
    public boolean isRelevantSpecialty(String spec1, String spec2) {
        if (spec1 == null || spec2 == null) return false;
        String s1 = spec1.toLowerCase();
        String s2 = spec2.toLowerCase();

        if (s1.contains(s2) || s2.contains(s1)) return true;

        Map<String, List<String>> groups = new LinkedHashMap<>();
        groups.put("cardio",      List.of("cardiologie", "cardiovasculaire", "cardiaque", "coeur"));
        groups.put("diabeto",     List.of("diabétologie", "diabète", "endocrinologie", "métabolique", "metabolique"));
        groups.put("nephro",      List.of("néphrologie", "nephro", "rein", "rénal", "renal", "dialyse"));
        groups.put("neuro",       List.of("neurologie", "neurologique", "neuro", "cerveau"));
        groups.put("gastro",      List.of("gastro-entérologie", "digestif", "gastro", "intestin"));
        groups.put("pneumo",      List.of("pneumologie", "respiratoire", "poumon", "respiration", "bpco"));
        groups.put("hepato",      List.of("hépatologie", "hepatologie", "foie", "hépatique", "hepatique"));
        groups.put("rhumato",     List.of("rhumatologie", "arthrite", "articulaire", "articulaire"));
        groups.put("onco",        List.of("oncologie", "cancer", "tumeur", "chimiothérapie"));
        groups.put("immunology",  List.of("immunologie", "allergie", "allergologie", "auto-immune"));

        for (List<String> group : groups.values()) {
            boolean m1 = group.stream().anyMatch(s1::contains);
            boolean m2 = group.stream().anyMatch(s2::contains);
            if (m1 && m2) return true;
        }
        return false;
    }

    /* ═══════════════════════════════════════════════
       SCORES DYNAMIQUES (traduit du PHP)
    ═══════════════════════════════════════════════ */

    /**
     * Calcule des scores par défaut basés sur les données réelles du dossier.
     * Utilisé en fallback si l'IA ne retourne pas de scores.
     */
    public Map<String, Integer> calculateDefaultScores(DossierMedical d) {
        int base = 85;
        String maladies    = nvl(d.getMaladiesChroniques(), "").toLowerCase();
        String antecedents = nvl(d.getAntecedentsMedicaux(), "").toLowerCase();
        String traitements = nvl(d.getTraitementsEnCours(), "").toLowerCase();

        // Pénalités selon pathologies détectées
        int cardioScore    = base;
        int metaScore      = base;
        int respiScore     = base;
        int renalScore     = base;
        int hepatiqueScore = base;
        int neuroScore     = base;

        if (maladies.contains("hypertension") || maladies.contains("cardio") || antecedents.contains("infarctus"))
            cardioScore -= 25;
        if (maladies.contains("diabet") || maladies.contains("obésit") || traitements.contains("insuline"))
            metaScore   -= 30;
        if (maladies.contains("bpco") || maladies.contains("asthme") || maladies.contains("respirat"))
            respiScore  -= 30;
        if (maladies.contains("rénal") || maladies.contains("renal") || maladies.contains("rein"))
            renalScore  -= 35;
        if (maladies.contains("hépatite") || maladies.contains("foie") || maladies.contains("cirrhose"))
            hepatiqueScore -= 30;
        if (maladies.contains("parkinson") || maladies.contains("alzheimer") || maladies.contains("épilepsie"))
            neuroScore  -= 30;

        // Activité physique : bonus ou malus
        String activite = nvl(d.getNiveauActivite(), "").toLowerCase();
        int actBonus = switch (activite) {
            case "très actif", "tres actif" -> 5;
            case "actif"   -> 3;
            case "modere", "modéré" -> 0;
            case "leger", "léger"   -> -2;
            default        -> -5; // sédentaire
        };

        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("cardiovasculaire", Math.max(0, Math.min(100, cardioScore    + actBonus)));
        scores.put("metabolique",      Math.max(0, Math.min(100, metaScore      + actBonus)));
        scores.put("respiratoire",     Math.max(0, Math.min(100, respiScore     + actBonus)));
        scores.put("renal",            Math.max(0, Math.min(100, renalScore)));
        scores.put("hepatique",        Math.max(0, Math.min(100, hepatiqueScore)));
        scores.put("neurologique",     Math.max(0, Math.min(100, neuroScore     + actBonus)));
        return scores;
    }

    public Map<String, Integer> calculatePerformanceIndicators(DossierMedical d) {
        boolean hasTraitement = d.getTraitementsEnCours() != null && !d.getTraitementsEnCours().isBlank();
        boolean hasMaladie    = d.getMaladiesChroniques()  != null && !d.getMaladiesChroniques().isBlank();
        boolean hasObjectif   = d.getObjectifSante()       != null && !d.getObjectifSante().isBlank();

        int adherence  = hasTraitement ? 75 : 50;
        int controle   = hasMaladie    ? 60 : 85;
        int qualiteVie = hasObjectif   ? 70 : 55;
        int risque     = 100 - controle;

        Map<String, Integer> ind = new LinkedHashMap<>();
        ind.put("adherence_traitement", adherence);
        ind.put("controle_pathologie",  controle);
        ind.put("qualite_vie",          qualiteVie);
        ind.put("risque_complications", risque);
        return ind;
    }

    /* ═══════════════════════════════════════════════
       FALLBACK JSON
    ═══════════════════════════════════════════════ */
    private JsonNode getFallbackJson(DossierMedical d) throws Exception {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("gravite",       "modere");
        fallback.put("score_sante",   70);
        fallback.put("urgence",       false);
        fallback.put("resume_cas",    "Service IA temporairement indisponible. "
                + "Consultez un médecin généraliste pour une évaluation complète.");
        fallback.put("recommandations",    List.of("Consulter un médecin généraliste", "Réaliser un bilan de santé complet"));
        fallback.put("examens_suggeres",   List.of("Bilan sanguin complet", "Électrocardiogramme"));
        fallback.put("facteurs_risque",    List.of("Données insuffisantes pour une analyse précise"));
        fallback.put("score_details",      calculateDefaultScores(d));
        fallback.put("indicateurs_performance", calculatePerformanceIndicators(d));
        return mapper.valueToTree(fallback);
    }

    private String nvl(String s, String def) {
        return (s != null && !s.isBlank()) ? s : def;
    }
}