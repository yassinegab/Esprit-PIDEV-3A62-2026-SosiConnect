package org.example.servicesociaux.frontoffice.controller.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.user.model.DossierMedical;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalyseIAController {

    /* ── FXML ── */
    @FXML private Label   titreLabel;
    @FXML private Label   statusLabel;
    @FXML private Label   errorLabel;
    @FXML private Label   engineLabel;
    @FXML private VBox    recapPanel;
    @FXML private TabPane tabPane;

    @FXML private Tab tabGroq;
    @FXML private Tab tabHF;
    @FXML private Tab tabVision;
    @FXML private Tab tabGemini;
    @FXML private Tab tabVoix;

    @FXML private VBox groqPanel;
    @FXML private VBox hfPanel;
    @FXML private VBox visionPanel;
    @FXML private VBox geminiPanel;
    @FXML private VBox voixPanel;

    @FXML private Button btnGroq;
    @FXML private Button btnHF;
    @FXML private Button btnVision;
    @FXML private Button btnGemini;
    @FXML private Button btnVoix;
    @FXML private Label  imageChoisieLabel;

    /* ── Clés API ── */
    private static final String GROQ_KEY   = System.getenv("GROQ_KEY");
    private static final String HF_KEY     = System.getenv("HF_KEY");
    private static final String GEMINI_KEY = System.getenv("GEMINI_KEY");

    private static final String GROQ_URL         = "https://api.groq.com/openai/v1/chat/completions";
    private static final String HF_URL_BASE       = "https://api-inference.huggingface.co/models/";
    private static final String GEMINI_URL        = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private static final String GROQ_VISION_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";

    // ✅ CORRECTIF HF : un seul modèle fiable + fallback rapide
    private static final String HF_MODEL_PRIMARY   = "facebook/bart-large-mnli";
    private static final String HF_MODEL_FALLBACK1 = "cross-encoder/nli-deberta-v3-base";
    private static final String HF_MODEL_FALLBACK2 = "typeform/distilbert-base-uncased-mnli";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(20))
            .build();

    /* ── State ── */
    private DossierMedical dossier;
    private String         mode;
    private File           imageMedicaleChoisie;
    private Process        ttsProcess;

    // ══════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        System.out.println("[INIT] AnalyseIAController initialise");
        viderTousLesPanels();
    }

    public void setDossier(DossierMedical d, String mode) {
        this.dossier = d;
        this.mode    = mode;
        System.out.println("[INIT] Dossier recu : #" + d.getId() + " | mode : " + mode);
        titreLabel.setText("Analyse IA — Dossier #" + d.getId());
        remplirRecap(d);
        switch (mode) {
            case "GROQ"   -> lancerGroq();
            case "HF"     -> lancerHF();
            case "GEMINI" -> lancerGemini();
            default       -> {}
        }
    }

    private void viderTousLesPanels() {
        for (VBox p : List.of(groqPanel, hfPanel, visionPanel, geminiPanel, voixPanel)) {
            if (p != null) {
                p.getChildren().clear();
                afficherPlaceholder(p, "Cliquez sur le bouton pour lancer l'analyse.");
            }
        }
    }

    // ══ RECAP ══════════════════════════════════════════════════
    private void remplirRecap(DossierMedical d) {
        recapPanel.getChildren().clear();
        addRecapRow(recapPanel, "Dossier",     "#" + d.getId());
        addRecapRow(recapPanel, "Cree le",     d.getDateCreationFormatee());
        addRecapRow(recapPanel, "Activite",    nvl(d.getNiveauActivite(),      "—"));
        addRecapRow(recapPanel, "Maladies",    nvl(d.getMaladiesChroniques(),  "Aucune"));
        addRecapRow(recapPanel, "Traitements", nvl(d.getTraitementsEnCours(),  "Aucun"));
        addRecapRow(recapPanel, "Allergies",   nvl(d.getAllergies(),            "Aucune"));
        addRecapRow(recapPanel, "Diagnostics", nvl(d.getDiagnostics(),         "—"));
        addRecapRow(recapPanel, "Objectif",    nvl(d.getObjectifSante(),       "—"));
    }

    private void addRecapRow(VBox parent, String label, String valeur) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding:4 0;-fx-border-color:transparent transparent #f0e6ff transparent;");
        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-font-size:10;-fx-text-fill:#9c27b0;-fx-min-width:85;-fx-font-weight:bold;");
        String v = (valeur != null && valeur.length() > 38)
                ? valeur.substring(0, 38) + "…" : (valeur != null ? valeur : "—");
        Label val = new Label(v);
        val.setStyle("-fx-font-size:11;-fx-text-fill:#333;");
        val.setWrapText(true);
        row.getChildren().addAll(lbl, val);
        parent.getChildren().add(row);
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║  MOTEUR 1 — GROQ LLAMA                              ║
    // ╚══════════════════════════════════════════════════════╝
    @FXML
    public void lancerGroq() {
        tabPane.getSelectionModel().select(tabGroq);
        engineLabel.setText("Groq LLaMA 3.3-70B");
        setAllBtnsDisabled(true);
        setStatus("Analyse clinique Groq en cours...");
        groqPanel.getChildren().clear();
        afficherChargement(groqPanel, "Interrogation de Groq LLaMA 3.3-70B...");
        System.out.println("[GROQ] Lancement analyse");

        CompletableFuture.supplyAsync(() -> {
            try { return appelGroq(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }).thenAccept(json -> Platform.runLater(() -> {
            System.out.println("[GROQ] Reponse recue, affichage...");
            setStatus("Analyse Groq terminee ✓");
            afficherResultatsGroq(json);
            setAllBtnsDisabled(false);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("[GROQ ERROR] " + rootMsg(ex));
                showError("Erreur Groq : " + rootMsg(ex));
                setAllBtnsDisabled(false);
            });
            return null;
        });
    }

    private JsonNode appelGroq() throws Exception {
        Map<String, Object> sysMsg = new LinkedHashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content",
                "Tu es un medecin expert international. Analyse les dossiers medicaux avec precision. "
                        + "Reponds UNIQUEMENT en JSON francais valide, sans texte avant/apres.");

        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", construirePromptGroq());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", "llama-3.3-70b-versatile");
        body.put("messages", List.of(sysMsg, userMsg));
        body.put("temperature", 0.15);
        body.put("max_tokens", 5000);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Authorization", "Bearer " + GROQ_KEY)
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();

        System.out.println("[GROQ] Envoi requete...");
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[GROQ] Status : " + resp.statusCode());

        if (resp.statusCode() != 200)
            throw new RuntimeException("HTTP " + resp.statusCode() + " : " + resp.body());

        String content = MAPPER.readTree(resp.body())
                .at("/choices/0/message/content").asText();
        content = content.replaceAll("(?s)```json|```", "").trim();
        int s = content.indexOf('{'), e = content.lastIndexOf('}');
        if (s >= 0 && e > s) content = content.substring(s, e + 1);
        System.out.println("[GROQ] JSON parse OK");
        return MAPPER.readTree(content);
    }

    private String construirePromptGroq() {
        return "ANALYSE MEDICALE APPROFONDIE\n"
                + "- Antecedents    : " + nvl(dossier.getAntecedentsMedicaux(), "Aucun")      + "\n"
                + "- Maladies       : " + nvl(dossier.getMaladiesChroniques(),  "Aucune")     + "\n"
                + "- Allergies      : " + nvl(dossier.getAllergies(),            "Aucune")     + "\n"
                + "- Traitements    : " + nvl(dossier.getTraitementsEnCours(),  "Aucun")      + "\n"
                + "- Diagnostics    : " + nvl(dossier.getDiagnostics(),         "Aucun")      + "\n"
                + "- Objectif sante : " + nvl(dossier.getObjectifSante(),       "Non defini") + "\n"
                + "- Activite       : " + nvl(dossier.getNiveauActivite(),      "Non defini") + "\n\n"
                + "Genere exactement ce JSON (sans texte avant/apres) :\n"
                + "{\n"
                + "  \"gravite\": \"faible|modere|eleve\",\n"
                + "  \"score_sante\": 0-100,\n"
                + "  \"urgence\": true/false,\n"
                + "  \"resume_cas\": \"analyse detaillee\",\n"
                + "  \"recommandations\": [\"...\"],\n"
                + "  \"examens_suggeres\": [\"...\"],\n"
                + "  \"facteurs_risque\": [\"...\"],\n"
                + "  \"complications_potentielles\": [\"...\"],\n"
                + "  \"plan_suivi\": {\"immediat\":\"...\",\"court_terme\":\"...\","
                + "    \"moyen_terme\":\"...\",\"long_terme\":\"...\"},\n"
                + "  \"score_details\": {\"cardiovasculaire\":0-100,\"metabolique\":0-100,"
                + "    \"respiratoire\":0-100,\"renal\":0-100,\"hepatique\":0-100,\"neurologique\":0-100},\n"
                + "  \"medecins_experts\": [{\"nom\":\"...\",\"specialite\":\"...\","
                + "    \"institution\":\"...\",\"pays\":\"...\"}],\n"
                + "  \"hopitaux_experts\": [{\"nom\":\"...\",\"ville\":\"...\","
                + "    \"pays\":\"...\",\"specialites\":[\"...\"],\"reputation\":\"...\"}]\n"
                + "}";
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║  MOTEUR 2 — HUGGINGFACE CORRIGE ET AMELIORE         ║
    // ╚══════════════════════════════════════════════════════╝

    // ── Définition des 6 dimensions médicales ──
    // Labels EN ANGLAIS courts pour maximiser la compatibilité MNLI
    private static final LinkedHashMap<String, String[]> HF_DIMENSIONS = new LinkedHashMap<>() {{
        put("gravite",      new String[]{"critical condition", "severe condition", "moderate condition", "mild condition", "stable condition"});
        put("cardio",       new String[]{"high cardiovascular risk", "moderate cardiovascular risk", "low cardiovascular risk"});
        put("metabolique",  new String[]{"severe metabolic disorder", "diabetes or obesity", "metabolic syndrome", "healthy metabolism"});
        put("respiratoire", new String[]{"severe respiratory failure", "breathing difficulty", "normal breathing"});
        put("urgence",      new String[]{"immediate hospitalization needed", "urgent medical consultation", "routine follow-up"});
        put("immunite",     new String[]{"immunodeficiency", "moderate immune deficit", "healthy immune system"});
    }};

    // ── Traductions FR pour l'affichage ──
    private static final Map<String, String> TRADUCTIONS_FR = Map.ofEntries(
            Map.entry("critical condition",             "Etat critique"),
            Map.entry("severe condition",               "Etat grave"),
            Map.entry("moderate condition",             "Etat modere"),
            Map.entry("mild condition",                 "Etat leger"),
            Map.entry("stable condition",               "Etat stable"),
            Map.entry("high cardiovascular risk",       "Risque cardio eleve"),
            Map.entry("moderate cardiovascular risk",   "Risque cardio modere"),
            Map.entry("low cardiovascular risk",        "Risque cardio faible"),
            Map.entry("severe metabolic disorder",      "Trouble metabolique severe"),
            Map.entry("diabetes or obesity",            "Diabete / obesite"),
            Map.entry("metabolic syndrome",             "Syndrome metabolique"),
            Map.entry("healthy metabolism",             "Metabolisme equilibre"),
            Map.entry("severe respiratory failure",     "Insuffisance respiratoire"),
            Map.entry("breathing difficulty",           "Difficulte respiratoire"),
            Map.entry("normal breathing",               "Respiration normale"),
            Map.entry("immediate hospitalization needed","Hospitalisation urgente"),
            Map.entry("urgent medical consultation",    "Consultation urgente"),
            Map.entry("routine follow-up",              "Suivi de routine"),
            Map.entry("immunodeficiency",               "Immunodeficience"),
            Map.entry("moderate immune deficit",        "Deficit immunitaire modere"),
            Map.entry("healthy immune system",          "Immunite normale")
    );

    @FXML
    public void lancerHF() {
        tabPane.getSelectionModel().select(tabHF);
        engineLabel.setText("HuggingFace — Zero-Shot Classification");
        setAllBtnsDisabled(true);
        hfPanel.getChildren().clear();

        // ✅ FIX : Afficher la progression en temps réel
        VBox progressPanel = new VBox(10);
        progressPanel.setStyle("-fx-padding:20;");
        Label titreProgress = new Label("Classification medicale en cours...");
        titreProgress.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#6a1b9a;");
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent:#9c27b0;");
        Label progressLabel = new Label("Initialisation...");
        progressLabel.setStyle("-fx-font-size:11;-fx-text-fill:#666;");
        Label etapeLabel = new Label("0 / 6 dimensions analysees");
        etapeLabel.setStyle("-fx-font-size:10;-fx-text-fill:#999;");
        progressPanel.getChildren().addAll(titreProgress, progressBar, progressLabel, etapeLabel);
        hfPanel.getChildren().add(progressPanel);

        setStatus("Classification HuggingFace en cours...");
        System.out.println("[HF] Lancement classification corrigee");

        String texte = construireTextePatient();
        System.out.println("[HF] Texte a classer : " + texte);

        // ✅ FIX PRINCIPAL : séquence synchrone sur thread unique
        // (évite les race conditions et les 429 par parallélisme excessif)
        Map<String, JsonNode> resultats = new LinkedHashMap<>();
        AtomicInteger etape = new AtomicInteger(0);

        CompletableFuture.supplyAsync(() -> {
            for (Map.Entry<String, String[]> entry : HF_DIMENSIONS.entrySet()) {
                String dimKey    = entry.getKey();
                String[] labels  = entry.getValue();
                int num          = etape.incrementAndGet();
                double pctDone   = (double)(num - 1) / HF_DIMENSIONS.size();

                Platform.runLater(() -> {
                    progressBar.setProgress(pctDone);
                    progressLabel.setText("Analyse : " + dimKey + "...");
                    etapeLabel.setText((num - 1) + " / " + HF_DIMENSIONS.size() + " dimensions");
                });

                System.out.println("[HF] Dimension " + num + "/6 : " + dimKey);
                JsonNode resultatDim = analyserDimensionAvecFallback(texte, labels, dimKey);

                if (resultatDim != null) {
                    resultats.put(dimKey, resultatDim);
                    System.out.println("[HF] ✓ " + dimKey + " OK");
                } else {
                    System.err.println("[HF] ✗ " + dimKey + " echec total — mode local");
                    resultats.put(dimKey + "_local", estimerLocalement(dimKey));
                }

                // ✅ Pause entre requêtes pour éviter le rate limit
                try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
            }
            return resultats;

        }).thenAccept(res -> Platform.runLater(() -> {
            System.out.println("[HF] Toutes dimensions traitees : " + res.keySet());
            progressBar.setProgress(1.0);
            etapeLabel.setText("6 / 6 dimensions analysees ✓");
            setStatus("Classification HuggingFace terminee ✓");
            hfPanel.getChildren().clear();
            afficherResultatsHF(res);
            setAllBtnsDisabled(false);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("[HF FATAL] " + rootMsg(ex));
                showError("HuggingFace indisponible — affichage estimation locale");
                hfPanel.getChildren().clear();
                afficherResultatsHFEstimation();
                setAllBtnsDisabled(false);
            });
            return null;
        });
    }

    /**
     * ✅ CORRECTIF PRINCIPAL :
     * Essaye le modèle primary, puis les fallbacks, avec gestion 503/loading.
     * Retourne null seulement si TOUS les modèles échouent.
     */
    private JsonNode analyserDimensionAvecFallback(String texte, String[] labels, String dimKey) {
        String[] models = {HF_MODEL_PRIMARY, HF_MODEL_FALLBACK1, HF_MODEL_FALLBACK2};

        for (String model : models) {
            System.out.println("[HF] Tentative modele : " + model + " pour " + dimKey);
            try {
                JsonNode res = appelHFModele(texte, Arrays.asList(labels), model);
                if (res != null && res.has("labels") && res.get("labels").size() > 0) {
                    System.out.println("[HF] ✓ Succes avec " + model);
                    return res;
                }
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "";
                System.err.println("[HF] Echec " + model + " : " + msg);

                if (msg.contains("loading") || msg.contains("503")) {
                    // Modèle en démarrage → attendre puis retry
                    System.out.println("[HF] Modele en cold start, attente 20s...");
                    try { Thread.sleep(20_000); } catch (InterruptedException ignored) {}
                    // Retry une fois
                    try {
                        JsonNode retry = appelHFModele(texte, Arrays.asList(labels), model);
                        if (retry != null && retry.has("labels")) {
                            System.out.println("[HF] ✓ Retry reussi pour " + model);
                            return retry;
                        }
                    } catch (Exception e2) {
                        System.err.println("[HF] Retry echec : " + e2.getMessage());
                    }
                } else if (msg.contains("429")) {
                    System.out.println("[HF] Rate limit, attente 12s...");
                    try { Thread.sleep(12_000); } catch (InterruptedException ignored) {}
                }
                // Essayer le modèle suivant
            }
        }
        return null; // Tous les modèles ont échoué
    }

    private JsonNode appelHFModele(String texte, List<String> labels, String model) throws Exception {
        String url = HF_URL_BASE + model;

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("candidate_labels", labels);
        params.put("multi_label", false);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inputs", texte);
        body.put("parameters", params);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + HF_KEY)
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(90))
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        int code = resp.statusCode();
        System.out.println("[HF] " + model + " → HTTP " + code);

        if (code == 503) {
            String bodyStr = resp.body();
            System.out.println("[HF] 503 body : " + bodyStr.substring(0, Math.min(200, bodyStr.length())));
            throw new RuntimeException("loading");
        }
        if (code == 429) throw new RuntimeException("429 rate limit");
        if (code == 401) throw new RuntimeException("401 cle invalide");
        if (code != 200) throw new RuntimeException("HTTP " + code + " : " +
                resp.body().substring(0, Math.min(200, resp.body().length())));

        String bodyStr = resp.body();
        System.out.println("[HF] Reponse OK (100 chars) : " +
                bodyStr.substring(0, Math.min(100, bodyStr.length())));
        return MAPPER.readTree(bodyStr);
    }

    /**
     * ✅ NOUVEAU : Estimation locale basée sur les données du dossier
     * quand l'API est totalement indisponible — retourne un JsonNode
     * formaté identiquement à la réponse HF pour un affichage uniforme.
     */
    private JsonNode estimerLocalement(String dimKey) {
        String maladies = nvl(dossier.getMaladiesChroniques(), "").toLowerCase();
        String diag     = nvl(dossier.getDiagnostics(), "").toLowerCase();
        String traite   = nvl(dossier.getTraitementsEnCours(), "").toLowerCase();
        boolean hasCardio = maladies.contains("hypertension") || maladies.contains("cardiaque")
                || diag.contains("hypertension") || diag.contains("cardiaque");
        boolean hasDiab   = maladies.contains("diabet") || maladies.contains("obesit")
                || diag.contains("diabet");
        boolean hasAsthme = maladies.contains("asthme") || maladies.contains("respirat")
                || diag.contains("asthme");
        boolean hasImmu   = maladies.contains("immun") || maladies.contains("autoimmun")
                || traite.contains("immunosuppresseur");

        String[] lbls; double[] scrs;
        switch (dimKey) {
            case "gravite" -> {
                lbls = new String[]{"critical condition","severe condition","moderate condition","mild condition","stable condition"};
                boolean grave = hasCardio && hasDiab;
                scrs = grave
                        ? new double[]{0.05, 0.25, 0.45, 0.15, 0.10}
                        : hasCardio || hasDiab
                        ? new double[]{0.02, 0.10, 0.50, 0.25, 0.13}
                        : new double[]{0.01, 0.04, 0.20, 0.35, 0.40};
            }
            case "cardio" -> {
                lbls = new String[]{"high cardiovascular risk","moderate cardiovascular risk","low cardiovascular risk"};
                scrs = hasCardio
                        ? new double[]{0.65, 0.25, 0.10}
                        : new double[]{0.10, 0.30, 0.60};
            }
            case "metabolique" -> {
                lbls = new String[]{"severe metabolic disorder","diabetes or obesity","metabolic syndrome","healthy metabolism"};
                scrs = hasDiab
                        ? new double[]{0.10, 0.60, 0.20, 0.10}
                        : new double[]{0.05, 0.10, 0.15, 0.70};
            }
            case "respiratoire" -> {
                lbls = new String[]{"severe respiratory failure","breathing difficulty","normal breathing"};
                scrs = hasAsthme
                        ? new double[]{0.15, 0.55, 0.30}
                        : new double[]{0.05, 0.15, 0.80};
            }
            case "urgence" -> {
                lbls = new String[]{"immediate hospitalization needed","urgent medical consultation","routine follow-up"};
                boolean urgence = hasCardio && hasDiab;
                scrs = urgence
                        ? new double[]{0.20, 0.50, 0.30}
                        : hasCardio || hasDiab
                        ? new double[]{0.05, 0.40, 0.55}
                        : new double[]{0.02, 0.13, 0.85};
            }
            default -> { // immunite
                lbls = new String[]{"immunodeficiency","moderate immune deficit","healthy immune system"};
                scrs = hasImmu
                        ? new double[]{0.50, 0.30, 0.20}
                        : new double[]{0.05, 0.15, 0.80};
            }
        }

        // Construire un JsonNode au format HF
        ArrayNode labelsNode = MAPPER.createArrayNode();
        ArrayNode scoresNode = MAPPER.createArrayNode();
        for (String l : lbls) labelsNode.add(l);
        for (double s : scrs) scoresNode.add(s);
        var obj = MAPPER.createObjectNode();
        obj.set("labels", labelsNode);
        obj.set("scores", scoresNode);
        obj.put("sequence", construireTextePatient());
        obj.put("_local_estimate", true);
        return obj;
    }

    private String construireTextePatient() {
        return "Patient : antecedents=" + nvl(dossier.getAntecedentsMedicaux(), "aucun")
                + ". Maladies=" + nvl(dossier.getMaladiesChroniques(), "aucune")
                + ". Allergies=" + nvl(dossier.getAllergies(), "aucune")
                + ". Traitements=" + nvl(dossier.getTraitementsEnCours(), "aucun")
                + ". Diagnostics=" + nvl(dossier.getDiagnostics(), "aucun")
                + ". Objectif=" + nvl(dossier.getObjectifSante(), "non defini")
                + ". Activite=" + nvl(dossier.getNiveauActivite(), "inconnu") + ".";
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║  MOTEUR 3 — GROQ VISION                             ║
    // ╚══════════════════════════════════════════════════════╝
    @FXML
    public void choisirImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image medicale");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg","*.jpeg","*.png","*.bmp"));
        Stage stage = (Stage) visionPanel.getScene().getWindow();
        File f = fc.showOpenDialog(stage);
        if (f != null) {
            imageMedicaleChoisie = f;
            System.out.println("[VISION] Image choisie : " + f.getName());
            imageChoisieLabel.setText(f.getName() + " (" + (f.length()/1024) + " KB)");
            imageChoisieLabel.setStyle("-fx-text-fill:#2e7d32;-fx-font-size:11;-fx-font-weight:bold;");
        }
    }

    @FXML
    public void lancerVision() {
        if (imageMedicaleChoisie == null) {
            showError("Veuillez d'abord importer une image medicale.");
            return;
        }
        tabPane.getSelectionModel().select(tabVision);
        engineLabel.setText("Groq Vision — Llama 4 Scout");
        setAllBtnsDisabled(true);
        setStatus("Analyse image en cours...");
        visionPanel.getChildren().clear();
        afficherChargement(visionPanel, "Groq Llama-4-Scout analyse votre image...");
        System.out.println("[VISION] Lancement analyse image : " + imageMedicaleChoisie.getName());

        final File imgFile = imageMedicaleChoisie;
        CompletableFuture.supplyAsync(() -> {
            try { return analyserImageGroqVision(imgFile); }
            catch (Exception e) { throw new RuntimeException(e); }
        }).thenAccept(json -> Platform.runLater(() -> {
            System.out.println("[VISION] Analyse terminee");
            setStatus("Analyse image terminee ✓");
            afficherResultatsVision(json, imgFile);
            setAllBtnsDisabled(false);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("[VISION ERROR] " + rootMsg(ex));
                showError("Erreur Vision : " + rootMsg(ex));
                setAllBtnsDisabled(false);
            });
            return null;
        });
    }

    private JsonNode analyserImageGroqVision(File imageFile) throws Exception {
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        if (imageBytes.length > 4_000_000)
            throw new RuntimeException("Image trop grande (max 4MB).");
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String ext = imageFile.getName().toLowerCase();
        String mediaType = ext.endsWith(".png") ? "image/png" : "image/jpeg";

        Map<String, Object> imageUrl = new LinkedHashMap<>();
        imageUrl.put("url", "data:" + mediaType + ";base64," + base64Image);
        Map<String, Object> imageContent = new LinkedHashMap<>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", imageUrl);
        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text",
                "Tu es un radiologue expert. Analyse cette image medicale et reponds UNIQUEMENT en JSON valide:\n"
                        + "{\"type_image\":\"radiographie|irm|ecg|scanner|photo_clinique|autre\","
                        + "\"region_anatomique\":\"...\",\"qualite_image\":\"bonne|moyenne|mauvaise\","
                        + "\"anomalies_detectees\":[{\"description\":\"...\",\"localisation\":\"...\",\"severite\":\"legere|moderee|severe\"}],"
                        + "\"elements_normaux\":[\"...\"],\"gravite_visuelle\":\"normale|legere|moderee|severe|critique\","
                        + "\"recommandation_urgence\":true,\"description_detaillee\":\"analyse complete\","
                        + "\"diagnostic_differentiel\":[\"...\"],\"examens_complementaires\":[\"...\"],"
                        + "\"confiance_analyse\":85,\"conclusion\":\"conclusion clinique\"}");

        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", List.of(imageContent, textContent));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", GROQ_VISION_MODEL);
        body.put("max_tokens", 2000);
        body.put("temperature", 0.1);
        body.put("messages", List.of(userMsg));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Authorization", "Bearer " + GROQ_KEY)
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[VISION] Status : " + resp.statusCode());
        if (resp.statusCode() != 200)
            throw new RuntimeException("HTTP " + resp.statusCode() + " : " + resp.body());

        String content = MAPPER.readTree(resp.body())
                .at("/choices/0/message/content").asText();
        content = content.replaceAll("(?s)```json|```", "").trim();
        int s = content.indexOf('{'), e = content.lastIndexOf('}');
        if (s >= 0 && e > s) content = content.substring(s, e + 1);
        return MAPPER.readTree(content);
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║  MOTEUR 4 — GOOGLE GEMINI                           ║
    // ╚══════════════════════════════════════════════════════╝
    @FXML
    public void lancerGemini() {
        tabPane.getSelectionModel().select(tabGemini);
        engineLabel.setText("Google Gemini 1.5 Flash");
        setAllBtnsDisabled(true);
        setStatus("Generation plan de vie en cours...");
        geminiPanel.getChildren().clear();
        afficherChargement(geminiPanel, "Google Gemini genere votre plan 30 jours...");
        System.out.println("[GEMINI] Lancement");

        CompletableFuture.supplyAsync(() -> {
            try { return appelGemini(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }).thenAccept(json -> Platform.runLater(() -> {
            System.out.println("[GEMINI] Reponse recue");
            setStatus("Plan de vie genere ✓");
            afficherResultatsGemini(json);
            setAllBtnsDisabled(false);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("[GEMINI ERROR] " + rootMsg(ex));
                showError("Erreur Gemini : " + rootMsg(ex));
                afficherPlanGeminiDemo();
                setAllBtnsDisabled(false);
            });
            return null;
        });
    }

    private JsonNode appelGemini() throws Exception {
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("text", construirePromptGemini());
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("role", "user");
        content.put("parts", List.of(part));
        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("maxOutputTokens", 4000);
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 40);

        List<Map<String, Object>> safetySettings = List.of(
                Map.of("category","HARM_CATEGORY_DANGEROUS_CONTENT","threshold","BLOCK_NONE"),
                Map.of("category","HARM_CATEGORY_HARASSMENT",       "threshold","BLOCK_NONE"),
                Map.of("category","HARM_CATEGORY_HATE_SPEECH",      "threshold","BLOCK_NONE"),
                Map.of("category","HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold","BLOCK_NONE")
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(content));
        body.put("generationConfig", generationConfig);
        body.put("safetySettings", safetySettings);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL + "?key=" + GEMINI_KEY))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(90))
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();

        System.out.println("[GEMINI] Envoi requete...");
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[GEMINI] Status : " + resp.statusCode());
        if (resp.statusCode() != 200)
            throw new RuntimeException("HTTP " + resp.statusCode() + " : "
                    + resp.body().substring(0, Math.min(400, resp.body().length())));

        JsonNode root = MAPPER.readTree(resp.body());
        String text = root.at("/candidates/0/content/parts/0/text").asText();
        if (text.isBlank()) throw new RuntimeException("Reponse Gemini vide.");
        text = text.replaceAll("(?s)```json|```", "").trim();
        int s = text.indexOf('{'), e = text.lastIndexOf('}');
        if (s >= 0 && e > s) text = text.substring(s, e + 1);
        return MAPPER.readTree(text);
    }

    private String construirePromptGemini() {
        return "Tu es un nutritionniste et coach sportif medical expert.\n\n"
                + "Patient :\n"
                + "- Maladies chroniques : " + nvl(dossier.getMaladiesChroniques(), "Aucune")      + "\n"
                + "- Allergies : "            + nvl(dossier.getAllergies(),           "Aucune")      + "\n"
                + "- Traitements : "          + nvl(dossier.getTraitementsEnCours(),  "Aucun")       + "\n"
                + "- Niveau activite : "      + nvl(dossier.getNiveauActivite(),      "Sedentaire")  + "\n"
                + "- Objectif sante : "       + nvl(dossier.getObjectifSante(),       "Ameliorer la sante") + "\n\n"
                + "Genere un plan de vie personnalise 30 jours en JSON valide UNIQUEMENT :\n"
                + "{\"resume_plan\":\"...\",\"calories_journalieres_recommandees\":2000,"
                + "\"plan_nutrition\":{\"aliments_recommandes\":[\"...\"],\"aliments_interdits\":[\"...\"],"
                + "\"hydratation_litres_jour\":2.5},"
                + "\"plan_exercice\":{\"seances_par_semaine\":3,\"types_exercice\":[\"...\"],"
                + "\"programme\":[{\"jour\":\"Lundi\",\"exercice\":\"...\",\"duree_min\":45,\"calories_brulees\":300}]},"
                + "\"conseils_sommeil\":[\"...\"],\"gestion_stress\":[\"...\"],"
                + "\"supplements_vitamines\":[\"...\"],\"avertissements_medicaux\":[\"...\"]}";
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║  MOTEUR 5 — TTS VOCAL                               ║
    // ╚══════════════════════════════════════════════════════╝
    @FXML
    public void lancerVoix() {
        tabPane.getSelectionModel().select(tabVoix);
        setAllBtnsDisabled(true);
        voixPanel.getChildren().clear();
        afficherChargement(voixPanel, "Preparation de l'interface vocale...");

        String texte = construireTexteVocal();
        Platform.runLater(() -> {
            setStatus("Interface vocale prete");
            afficherInterfaceVoix(texte);
            setAllBtnsDisabled(false);
        });
    }

    private void afficherInterfaceVoix(String texte) {
        voixPanel.getChildren().clear();
        String os = System.getProperty("os.name").toLowerCase();
        String ttsInfo = os.contains("windows") ? "VBScript SAPI (Windows)"
                : os.contains("mac") ? "say (macOS)" : "espeak (Linux)";

        VBox headerCard = card("#fce4ec", "#c2185b");
        addLabel(headerCard, "Resume Medical Vocal — Dossier #" + dossier.getId(), 13, "#880e4f", true);
        addLabel(headerCard, "Moteur TTS : " + ttsInfo, 10, "#888", false);
        voixPanel.getChildren().add(headerCard);

        VBox texteCard = card("#f3e5f5", "#6a1b9a");
        addLabel(texteCard, "Contenu du resume", 12, "#4a148c", true);
        for (String phrase : texte.split("\\. ")) {
            if (phrase.isBlank()) continue;
            Label l = new Label("▶  " + phrase.trim() + ".");
            l.setStyle("-fx-font-size:12;-fx-text-fill:#333;-fx-padding:2 0;");
            l.setWrapText(true);
            texteCard.getChildren().add(l);
        }
        voixPanel.getChildren().add(texteCard);

        VBox ctrlCard = card("#e8eaf6", "#283593");
        addLabel(ctrlCard, "Controles de lecture", 12, "#1a237e", true);
        Label indic = new Label("Pret — cliquez 'Lire' pour demarrer");
        indic.setStyle("-fx-font-size:11;-fx-text-fill:#666;-fx-padding:4 0;");

        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER_LEFT);
        Button btnLire   = new Button("▶  Lire a voix haute");
        btnLire.setStyle(btnStyle("#1a237e", "white"));
        Button btnStop   = new Button("■  Arreter");
        btnStop.setStyle(btnStyle("#c62828", "white"));
        btnStop.setDisable(true);
        Button btnCopier = new Button("⎘  Copier");
        btnCopier.setStyle(btnStyle("#2e7d32", "white"));
        controls.getChildren().addAll(btnLire, btnStop, btnCopier);
        ctrlCard.getChildren().addAll(controls, indic);
        voixPanel.getChildren().add(ctrlCard);

        btnLire.setOnAction(e -> {
            btnLire.setDisable(true);
            btnStop.setDisable(false);
            indic.setText("Lecture en cours...");
            indic.setStyle("-fx-font-size:11;-fx-text-fill:#1a237e;-fx-font-weight:bold;");
            CompletableFuture.runAsync(() -> {
                boolean ok = lancerTTSSystemeAvecRetour(texte);
                Platform.runLater(() -> {
                    indic.setText(ok ? "Lecture terminee ✓" : "TTS non disponible — voir texte ci-dessus");
                    indic.setStyle("-fx-font-size:11;-fx-text-fill:" + (ok ? "#2e7d32" : "#c62828") + ";");
                    btnLire.setDisable(false);
                    btnStop.setDisable(true);
                });
            });
        });

        btnStop.setOnAction(e -> {
            if (ttsProcess != null && ttsProcess.isAlive()) {
                ttsProcess.destroy();
                indic.setText("Lecture arretee");
                indic.setStyle("-fx-font-size:11;-fx-text-fill:#c62828;");
            }
            btnLire.setDisable(false);
            btnStop.setDisable(true);
        });

        btnCopier.setOnAction(e -> {
            javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
            cc.putString(texte);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(cc);
            btnCopier.setText("Copie !");
            PauseTransition pt = new PauseTransition(Duration.seconds(2));
            pt.setOnFinished(ev -> btnCopier.setText("⎘  Copier"));
            pt.play();
        });

        animerEntreeElements(voixPanel);
    }

    private boolean lancerTTSSystemeAvecRetour(String texte) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            ProcessBuilder pb;
            if (os.contains("mac")) {
                pb = new ProcessBuilder("say", "-v", "Thomas", "-r", "150", texte);
            } else if (os.contains("linux")) {
                pb = new ProcessBuilder("espeak", "-v", "fr", "-s", "140", "-a", "200", texte);
            } else {
                String texteEscaped = texte.replace("\"", " ").replace("\n", " ").replace("\r", " ");
                File vbsFile = File.createTempFile("tts_medicare_", ".vbs");
                vbsFile.deleteOnExit();
                try (FileWriter fw = new FileWriter(vbsFile)) {
                    fw.write("Dim sapi\nSet sapi = CreateObject(\"SAPI.SpVoice\")\n"
                            + "sapi.Rate = 0\nsapi.Volume = 100\n"
                            + "sapi.Speak \"" + texteEscaped + "\"\nWScript.Quit 0\n");
                }
                pb = new ProcessBuilder("cscript", "//NoLogo", vbsFile.getAbsolutePath());
            }
            pb.redirectErrorStream(true);
            ttsProcess = pb.start();
            int exitCode = ttsProcess.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("[TTS] Exception : " + e.getMessage());
            return false;
        }
    }

    private String construireTexteVocal() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resume medical du dossier numero ").append(dossier.getId()).append(". ");
        String m = nvl(dossier.getMaladiesChroniques(), null);
        if (m != null) sb.append("Maladies chroniques : ").append(m).append(". ");
        String t = nvl(dossier.getTraitementsEnCours(), null);
        if (t != null) sb.append("Traitements en cours : ").append(t).append(". ");
        String a = nvl(dossier.getAllergies(), null);
        if (a != null) sb.append("Allergies : ").append(a).append(". ");
        String o = nvl(dossier.getObjectifSante(), null);
        if (o != null) sb.append("Objectif de sante : ").append(o).append(". ");
        return sb.toString();
    }

    // ══ AFFICHAGE GROQ ════════════════════════════════════════
    private void afficherResultatsGroq(JsonNode json) {
        groqPanel.getChildren().clear();

        int score   = json.path("score_sante").asInt(70);
        String grav = json.path("gravite").asText("modere");
        boolean urg = json.path("urgence").asBoolean(false);
        afficherScoreGlobal(groqPanel, score, grav, urg);

        String resume = json.path("resume_cas").asText("");
        if (!resume.isBlank())
            addCardSection(groqPanel, "Analyse clinique detaillee", resume, "#4a148c", "#f3e5f5");

        afficherScoresSystemes(groqPanel, json.path("score_details"));
        afficherListe(groqPanel, "Recommandations",
                json.path("recommandations"),            "#1565c0","#e3f2fd","○");
        afficherListe(groqPanel, "Examens suggeres",
                json.path("examens_suggeres"),           "#2e7d32","#e8f5e9",">");
        afficherListe(groqPanel, "Facteurs de risque",
                json.path("facteurs_risque"),            "#e65100","#fff3e0","!");
        afficherListe(groqPanel, "Complications potentielles",
                json.path("complications_potentielles"), "#c62828","#ffebee","!");
        afficherPlanSuivi(groqPanel, json.path("plan_suivi"));
        afficherMedecins(groqPanel,  json.path("medecins_experts"));
        afficherHopitaux(groqPanel,  json.path("hopitaux_experts"));
        animerEntreeElements(groqPanel);
    }

    // ══ AFFICHAGE HF CORRIGE ══════════════════════════════════
    private void afficherResultatsHF(Map<String, JsonNode> results) {
        hfPanel.getChildren().clear();
        System.out.println("[HF-UI] Affichage resultats. Cles : " + results.keySet());

        // ── Header ──
        VBox header = card("#e8eaf6", "#283593");
        addLabel(header, "Classification Medicale — HuggingFace Zero-Shot", 13, "#1a237e", true);
        int nbReelles = (int) results.keySet().stream().filter(k -> !k.endsWith("_local")).count();
        int nbLocales = (int) results.keySet().stream().filter(k -> k.endsWith("_local")).count();
        String sourceInfo = nbReelles + " via API" + (nbLocales > 0 ? " + " + nbLocales + " estimation locale" : "");
        addLabel(header, "BART-Large-MNLI | " + sourceInfo, 11, "#5c6bc0", false);
        hfPanel.getChildren().add(header);

        // ── Données analysées ──
        VBox txtBox = card("#fafafa", "#9e9e9e");
        addLabel(txtBox, "Donnees analysees", 11, "#555", true);
        Label tl = new Label(construireTextePatient());
        tl.setStyle("-fx-font-size:10;-fx-text-fill:#666;-fx-font-style:italic;");
        tl.setWrapText(true);
        txtBox.getChildren().add(tl);
        hfPanel.getChildren().add(txtBox);

        // ── Bannière si estimations locales ──
        if (nbLocales > 0) {
            VBox avertCard = card("#fff8e1", "#f9a825");
            addLabel(avertCard, "⚠ " + nbLocales + " dimension(s) estimee(s) localement", 11, "#e65100", true);
            addLabel(avertCard, " "
                    + "Resultats bases sur les donnees du dossier.", 10, "#888", false);
            hfPanel.getChildren().add(avertCard);
        }

        // ── Config des dimensions ──
        Map<String, String[]> dimConfig = new LinkedHashMap<>();
        dimConfig.put("gravite",      new String[]{"Gravite Globale",         "#c62828","#ffebee"});
        dimConfig.put("cardio",       new String[]{"Risque Cardiovasculaire", "#d32f2f","#fce4ec"});
        dimConfig.put("metabolique",  new String[]{"Etat Metabolique",        "#f57f17","#fff8e1"});
        dimConfig.put("respiratoire", new String[]{"Fonction Respiratoire",   "#1565c0","#e3f2fd"});
        dimConfig.put("urgence",      new String[]{"Urgence Medicale",        "#e65100","#fff3e0"});
        dimConfig.put("immunite",     new String[]{"Etat Immunitaire",        "#2e7d32","#e8f5e9"});

        int affichees = 0;
        for (Map.Entry<String, String[]> dim : dimConfig.entrySet()) {
            String key       = dim.getKey();
            String[] cfg     = dim.getValue();
            String  titre    = cfg[0];
            String  couleur  = cfg[1];
            String  bgColor  = cfg[2];

            // Cherche d'abord la clé réelle, puis la clé locale
            JsonNode data = results.getOrDefault(key, results.get(key + "_local"));

            if (data != null) {
                boolean isLocal = results.containsKey(key + "_local") && !results.containsKey(key);
                afficherDimensionHF(hfPanel, titre, data, couleur, bgColor, isLocal);
                affichees++;
            } else {
                afficherDimensionErreur(hfPanel, titre, couleur);
            }
        }

        // ── Synthèse ──
        VBox synth = card("#e8f5e9", "#2e7d32");
        addLabel(synth, "Synthese", 12, "#1b5e20", true);
        addLabel(synth, affichees + "/6 dimensions affichees. "
                + (nbLocales > 0 ? "Certaines valeurs sont estimees localement. " : "")
                + "Consultez un medecin pour un diagnostic officiel.", 11, "#444", false);
        hfPanel.getChildren().add(synth);
        animerEntreeElements(hfPanel);
    }

    /**
     * ✅ CORRECTIF : afficheDimensionHF accepte maintenant un flag isLocal
     * pour afficher un badge "Estimation" si la valeur n'est pas de l'API.
     */
    private void afficherDimensionHF(VBox parent, String titre, JsonNode data,
                                     String mainColor, String bgColor, boolean isLocal) {
        VBox box = card(bgColor, mainColor);

        HBox titreRow = new HBox(8);
        titreRow.setAlignment(Pos.CENTER_LEFT);
        Label titreLabel = new Label(titre);
        titreLabel.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + mainColor + ";");
        titreRow.getChildren().add(titreLabel);
        if (isLocal) {
            Label badge = new Label("Estimation locale");
            badge.setStyle("-fx-background-color:#fff3e0;-fx-text-fill:#e65100;"
                    + "-fx-padding:2 8;-fx-background-radius:10;-fx-font-size:9;");
            titreRow.getChildren().add(badge);
        }
        box.getChildren().add(titreRow);

        JsonNode labels = data.path("labels");
        JsonNode scores = data.path("scores");

        if (!labels.isArray() || !scores.isArray() || labels.size() == 0) {
            addLabel(box, "Donnees non disponibles", 10, "#999", false);
            parent.getChildren().add(box);
            return;
        }

        for (int i = 0; i < labels.size(); i++) {
            String labelEn = labels.get(i).asText();
            String labelFr = TRADUCTIONS_FR.getOrDefault(labelEn.toLowerCase(), labelEn);
            double scr = (i < scores.size()) ? scores.get(i).asDouble() : 0.0;
            int pct    = (int)(scr * 100);
            String color      = (i == 0) ? mainColor : "#9e9e9e";
            boolean isDominant = (i == 0);

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color:" + (isDominant ? mainColor + "18" : "white")
                    + ";-fx-background-radius:8;-fx-padding:8 10;"
                    + (isDominant ? "-fx-border-color:" + mainColor + "44;-fx-border-radius:8;" : ""));

            Label rank = new Label(String.valueOf(i + 1));
            rank.setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:" + color + ";-fx-min-width:18;");

            Label lb = new Label(labelFr);
            lb.setStyle("-fx-font-size:11;-fx-text-fill:#333;" + (isDominant ? "-fx-font-weight:bold;" : ""));
            lb.setMinWidth(185);
            lb.setWrapText(true);

            HBox bg = new HBox();
            bg.setStyle("-fx-background-color:#e0e0e0;-fx-background-radius:6;");
            bg.setPrefHeight(10); bg.setPrefWidth(150);
            HBox fill = new HBox();
            fill.setPrefHeight(10); fill.setPrefWidth(0);
            fill.setStyle("-fx-background-color:" + color + ";-fx-background-radius:6;");
            bg.getChildren().add(fill);

            // Animation barre
            final double target = 150.0 * scr;
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO,       new KeyValue(fill.prefWidthProperty(), 0)),
                    new KeyFrame(Duration.millis(700 + i * 60), new KeyValue(fill.prefWidthProperty(), target))
            );
            tl.setDelay(Duration.millis(150));
            tl.play();

            Label pctLbl = new Label(pct + "%");
            pctLbl.setStyle("-fx-font-size:12;" + (isDominant ? "-fx-font-weight:bold;" : "")
                    + "-fx-text-fill:" + color + ";-fx-min-width:38;");

            row.getChildren().addAll(rank, lb, bg, pctLbl);
            box.getChildren().add(row);
        }
        parent.getChildren().add(box);
    }

    private void afficherDimensionErreur(VBox parent, String titre, String color) {
        VBox box = card("#f5f5f5", "#9e9e9e");
        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label("✗  " + titre + " — Non disponible");
        l.setStyle("-fx-font-size:11;-fx-text-fill:#757575;");
        row.getChildren().add(l);
        box.getChildren().add(row);
        parent.getChildren().add(box);
    }

    /**
     * ✅ Mode estimation complète (quand TOUT échoue).
     */
    private void afficherResultatsHFEstimation() {
        hfPanel.getChildren().clear();
        VBox alert = card("#fff8e1", "#f9a825");
        addLabel(alert, "⚠ API HuggingFace indisponible — Estimation complete", 12, "#e65100", true);
        addLabel(alert, "Tous les resultats sont bases sur l'analyse locale du dossier.", 10, "#888", false);
        hfPanel.getChildren().add(alert);

        // Générer toutes les estimations locales
        Map<String, JsonNode> estimations = new LinkedHashMap<>();
        for (String key : HF_DIMENSIONS.keySet()) {
            estimations.put(key + "_local", estimerLocalement(key));
        }
        afficherResultatsHF(estimations);
    }

    // ══ AFFICHAGE VISION ══════════════════════════════════════
    private void afficherResultatsVision(JsonNode json, File imageFile) {
        visionPanel.getChildren().clear();

        VBox header = card("#fff3e0", "#e65100");
        addLabel(header, "Analyse Image Medicale — Groq Llama-4-Scout Vision", 13, "#bf360c", true);
        addLabel(header, "Fichier : " + imageFile.getName()
                + " (" + (imageFile.length()/1024) + " KB)", 10, "#666", false);
        visionPanel.getChildren().add(header);

        String grav = json.path("gravite_visuelle").asText("normale");
        boolean urg = json.path("recommandation_urgence").asBoolean(false);
        String gc = switch (grav.toLowerCase()) {
            case "severe","critique" -> "#c62828";
            case "moderee"           -> "#e65100";
            case "legere"            -> "#f57f17";
            default                  -> "#2e7d32";
        };
        VBox gravCard = card(gc + "11", gc);
        addLabel(gravCard, "Gravite visuelle : " + grav.toUpperCase(), 13, gc, true);
        if (urg)
            addLabel(gravCard, "CONSULTATION MEDICALE URGENTE RECOMMANDEE", 12, "#c62828", true);
        visionPanel.getChildren().add(gravCard);

        String desc = json.path("description_detaillee").asText("");
        if (!desc.isBlank())
            addCardSection(visionPanel, "Description detaillee", desc, "#4a148c","#f3e5f5");

        afficherListe(visionPanel, "Anomalies detectees",
                anomaliesToArray(json.path("anomalies_detectees")), "#c62828","#ffebee","!");
        afficherListe(visionPanel, "Diagnostic differentiel",
                json.path("diagnostic_differentiel"),  "#1565c0","#e3f2fd",">");
        afficherListe(visionPanel, "Examens complementaires",
                json.path("examens_complementaires"),  "#6a1b9a","#f3e5f5",">");
        String conclusion = json.path("conclusion").asText("");
        if (!conclusion.isBlank())
            addCardSection(visionPanel, "Conclusion clinique", conclusion, "#2e7d32","#e8f5e9");
        animerEntreeElements(visionPanel);
    }

    private JsonNode anomaliesToArray(JsonNode anomalies) {
        ArrayNode arr = MAPPER.createArrayNode();
        if (anomalies != null && anomalies.isArray()) {
            for (JsonNode a : anomalies) {
                String desc = a.path("description").asText("")
                        + " — " + a.path("localisation").asText("");
                arr.add(desc);
            }
        }
        return arr;
    }

    // ══ AFFICHAGE GEMINI ══════════════════════════════════════
    private void afficherResultatsGemini(JsonNode json) {
        geminiPanel.getChildren().clear();

        VBox header = card("#e8f5e9", "#2e7d32");
        addLabel(header, "Plan de Vie 30 Jours — Google Gemini 1.5 Flash", 13, "#1b5e20", true);
        String resume = json.path("resume_plan").asText("");
        if (!resume.isBlank()) addLabel(header, resume, 11, "#333", false);
        int cal = json.path("calories_journalieres_recommandees").asInt(0);
        if (cal > 0) addLabel(header, cal + " kcal/jour recommandees", 12, "#2e7d32", true);
        geminiPanel.getChildren().add(header);

        JsonNode nutrition = json.path("plan_nutrition");
        if (!nutrition.isMissingNode()) {
            VBox nutCard = card("#fff8e1", "#f9a825");
            addLabel(nutCard, "Plan Nutrition Medical", 12, "#e65100", true);
            afficherListeInline(nutCard, "Aliments recommandes",
                    nutrition.path("aliments_recommandes"), "#2e7d32");
            afficherListeInline(nutCard, "Aliments interdits",
                    nutrition.path("aliments_interdits"),   "#c62828");
            double hydra = nutrition.path("hydratation_litres_jour").asDouble(2.0);
            addLabel(nutCard, "Hydratation : " + hydra + " L/jour minimum", 11, "#1565c0", true);
            geminiPanel.getChildren().add(nutCard);
        }

        JsonNode exercice = json.path("plan_exercice");
        if (!exercice.isMissingNode()) {
            VBox exCard = card("#e3f2fd", "#1565c0");
            addLabel(exCard, "Plan Exercice Medical", 12, "#0d47a1", true);
            addLabel(exCard, exercice.path("seances_par_semaine").asInt(3) + " seances/semaine",
                    11, "#333", false);
            afficherListeInline(exCard, "Exercices recommandes",
                    exercice.path("types_exercice"), "#2e7d32");

            // Programme détaillé
            JsonNode prog = exercice.path("programme");
            if (prog.isArray() && prog.size() > 0) {
                addLabel(exCard, "Programme de la semaine :", 11, "#1565c0", true);
                for (JsonNode j : prog) {
                    HBox row = new HBox(10);
                    row.setStyle("-fx-background-color:white;-fx-background-radius:6;-fx-padding:6 10;");
                    Label jour = new Label(j.path("jour").asText(""));
                    jour.setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:#1565c0;-fx-min-width:60;");
                    Label ex = new Label(j.path("exercice").asText(""));
                    ex.setStyle("-fx-font-size:10;-fx-text-fill:#333;");
                    HBox.setHgrow(ex, Priority.ALWAYS);
                    Label dur = new Label(j.path("duree_min").asInt(0) + " min");
                    dur.setStyle("-fx-font-size:10;-fx-text-fill:#888;");
                    row.getChildren().addAll(jour, ex, dur);
                    exCard.getChildren().add(row);
                }
            }
            geminiPanel.getChildren().add(exCard);
        }

        afficherListe(geminiPanel, "Conseils Sommeil",        json.path("conseils_sommeil"),        "#37474f","#eceff1","•");
        afficherListe(geminiPanel, "Gestion du Stress",       json.path("gestion_stress"),          "#7b1fa2","#f3e5f5","•");
        afficherListe(geminiPanel, "Supplements et Vitamines",json.path("supplements_vitamines"),   "#1565c0","#e3f2fd",">");
        afficherListe(geminiPanel, "Avertissements Medicaux", json.path("avertissements_medicaux"), "#c62828","#ffebee","!");
        animerEntreeElements(geminiPanel);
    }

    private void afficherPlanGeminiDemo() {
        geminiPanel.getChildren().clear();
        VBox alert = card("#fff8e1", "#f9a825");
        addLabel(alert, "Google Gemini indisponible — Plan general", 12, "#e65100", true);
        geminiPanel.getChildren().add(alert);
        String[][] conseils = {
                {"Nutrition","Privilegiez legumes, fruits, proteines maigres.","#2e7d32","#e8f5e9"},
                {"Exercice", "30 minutes de marche rapide 5x/semaine.",          "#1565c0","#e3f2fd"},
                {"Sommeil",  "7-8 heures par nuit. Couchez-vous a heures fixes.","#37474f","#eceff1"},
                {"Stress",   "Meditation 10 min/jour. Respiration profonde.",    "#7b1fa2","#f3e5f5"}
        };
        for (String[] c : conseils) {
            VBox box = card(c[3], c[2]);
            addLabel(box, c[0], 12, c[2], true);
            addLabel(box, c[1], 11, "#333", false);
            geminiPanel.getChildren().add(box);
        }
    }

    // ══ WIDGETS COMMUNS ═══════════════════════════════════════
    private void afficherScoreGlobal(VBox parent, int score, String gravite, boolean urgence) {
        String color = score >= 75 ? "#2e7d32" : score >= 50 ? "#f57f17" : "#c62828";
        VBox box = card("#fff8e1", "#f9a825");
        HBox row = new HBox(24); row.setAlignment(Pos.CENTER_LEFT);
        VBox scoreBox = new VBox(4); scoreBox.setAlignment(Pos.CENTER);
        Label scoreLbl = new Label(score + "/100");
        scoreLbl.setStyle("-fx-font-size:28;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Label scoreTitle = new Label("Score de Sante");
        scoreTitle.setStyle("-fx-font-size:10;-fx-text-fill:#888;");
        scoreBox.getChildren().addAll(scoreTitle, scoreLbl);
        VBox infos = new VBox(6); infos.setAlignment(Pos.CENTER_LEFT);
        Label gravLbl = new Label("Gravite : " + gravite.toUpperCase());
        gravLbl.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        infos.getChildren().add(gravLbl);
        if (urgence) {
            Label urgLbl = new Label("URGENCE — Consultation immediate");
            urgLbl.setStyle("-fx-font-size:11;-fx-text-fill:#c62828;-fx-font-weight:bold;"
                    + "-fx-background-color:#ffebee;-fx-padding:5 12;-fx-background-radius:8;");
            infos.getChildren().add(urgLbl);
        }
        row.getChildren().addAll(scoreBox, infos);
        box.getChildren().add(row);
        parent.getChildren().add(box);
    }

    private void afficherScoresSystemes(VBox parent, JsonNode scores) {
        if (scores.isMissingNode()) return;
        VBox box = card("#f3e5f5", "#7b1fa2");
        addLabel(box, "Scores par Systemes Corporels", 12, "#4a148c", true);
        HBox grid = new HBox(10); grid.setAlignment(Pos.CENTER_LEFT);
        String[][] sys = {
                {"cardiovasculaire","Cardio"},{"metabolique","Metabolo"},
                {"respiratoire","Respi"},{"renal","Renal"},
                {"hepatique","Hepato"},{"neurologique","Neuro"}
        };
        for (String[] s : sys) grid.getChildren().add(scoreCell(s[1], scores.path(s[0]).asInt(75)));
        box.getChildren().add(grid);
        parent.getChildren().add(box);
    }

    private VBox scoreCell(String label, int val) {
        VBox cell = new VBox(5); cell.setAlignment(Pos.CENTER);
        cell.setStyle("-fx-background-color:white;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:10;");
        cell.setPrefWidth(100);
        String color = val >= 75 ? "#2e7d32" : val >= 50 ? "#f57f17" : "#c62828";
        Label nm = new Label(label); nm.setStyle("-fx-font-size:10;-fx-text-fill:#666;");
        Label vl = new Label(val + "/100"); vl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        HBox barBg = new HBox(); barBg.setStyle("-fx-background-color:#eee;-fx-background-radius:5;");
        barBg.setPrefHeight(8); barBg.setPrefWidth(80);
        HBox barFill = new HBox(); barFill.setPrefHeight(8); barFill.setPrefWidth(0);
        barFill.setStyle("-fx-background-color:" + color + ";-fx-background-radius:5;");
        barBg.getChildren().add(barFill);
        cell.getChildren().addAll(nm, vl, barBg);
        new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(barFill.prefWidthProperty(), 0)),
                new KeyFrame(Duration.millis(900), new KeyValue(barFill.prefWidthProperty(), 80.0 * val / 100))
        ).play();
        return cell;
    }

    private void afficherPlanSuivi(VBox parent, JsonNode plan) {
        if (plan.isMissingNode()) return;
        VBox box = card("#e3f2fd", "#1565c0");
        addLabel(box, "Plan de Suivi Medical", 12, "#0d47a1", true);
        String[][] phases = {
                {"immediat","Immediat","#c62828"},{"court_terme","Court terme","#e65100"},
                {"moyen_terme","Moyen terme","#1565c0"},{"long_terme","Long terme","#2e7d32"}
        };
        for (String[] p : phases) {
            String v = plan.path(p[0]).asText("");
            if (v.isBlank()) continue;
            HBox row = new HBox(12); row.setAlignment(Pos.TOP_LEFT);
            row.setStyle("-fx-background-color:white;-fx-background-radius:7;-fx-padding:9;");
            Label lbl = new Label(p[1]);
            lbl.setStyle("-fx-font-size:11;-fx-font-weight:bold;-fx-text-fill:" + p[2] + ";-fx-min-width:90;");
            Label vl = new Label(v); vl.setStyle("-fx-font-size:11;-fx-text-fill:#333;"); vl.setWrapText(true);
            HBox.setHgrow(vl, Priority.ALWAYS);
            row.getChildren().addAll(lbl, vl);
            box.getChildren().add(row);
        }
        parent.getChildren().add(box);
    }

    private void afficherMedecins(VBox parent, JsonNode medecins) {
        if (medecins == null || medecins.isMissingNode() || !medecins.isArray() || medecins.isEmpty()) return;
        VBox box = card("#e8eaf6", "#283593");
        addLabel(box, "Medecins Experts Recommandes", 12, "#1a237e", true);
        for (JsonNode m : medecins) {
            VBox c = new VBox(4);
            c.setStyle("-fx-background-color:white;-fx-border-color:#c5cae9;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:12;");
            Label nom = new Label(m.path("nom").asText("Medecin"));
            nom.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#1a237e;");
            Label spec = new Label(m.path("specialite").asText("") + "  |  " + m.path("pays").asText(""));
            spec.setStyle("-fx-font-size:10;-fx-text-fill:#666;");
            Label inst = new Label(m.path("institution").asText(""));
            inst.setStyle("-fx-font-size:11;-fx-text-fill:#333;");
            c.getChildren().addAll(nom, spec, inst);
            box.getChildren().add(c);
        }
        parent.getChildren().add(box);
    }

    private void afficherHopitaux(VBox parent, JsonNode hopitaux) {
        if (hopitaux == null || hopitaux.isMissingNode() || !hopitaux.isArray() || hopitaux.isEmpty()) return;
        VBox box = card("#f9fbe7", "#558b2f");
        addLabel(box, "Hopitaux d'Excellence Recommandes", 12, "#33691e", true);
        for (JsonNode h : hopitaux) {
            VBox c = new VBox(4);
            c.setStyle("-fx-background-color:white;-fx-border-color:#c5e1a5;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:12;");
            Label nom = new Label(h.path("nom").asText("Hopital"));
            nom.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#33691e;");
            Label loc = new Label(h.path("ville").asText("") + ", " + h.path("pays").asText(""));
            loc.setStyle("-fx-font-size:10;-fx-text-fill:#666;");
            Label rep = new Label(h.path("reputation").asText(""));
            rep.setStyle("-fx-font-size:11;-fx-text-fill:#558b2f;-fx-font-weight:bold;");
            c.getChildren().addAll(nom, loc, rep);
            box.getChildren().add(c);
        }
        parent.getChildren().add(box);
    }

    private void afficherListe(VBox parent, String titre, JsonNode items,
                               String color, String bg, String bullet) {
        if (items == null || items.isMissingNode() || !items.isArray() || items.isEmpty()) return;
        VBox box = card(bg, color);
        addLabel(box, titre, 12, color, true);
        for (JsonNode item : items) {
            Label l = new Label(bullet + "  " + item.asText());
            l.setStyle("-fx-font-size:11;-fx-text-fill:#333;");
            l.setWrapText(true);
            box.getChildren().add(l);
        }
        parent.getChildren().add(box);
    }

    private void afficherListeInline(VBox parent, String titre, JsonNode items, String color) {
        if (items == null || items.isMissingNode() || !items.isArray() || items.isEmpty()) return;
        StringBuilder sb = new StringBuilder(titre + " : ");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(" • ");
            sb.append(items.get(i).asText());
        }
        Label l = new Label(sb.toString());
        l.setStyle("-fx-font-size:11;-fx-text-fill:" + color + ";");
        l.setWrapText(true);
        parent.getChildren().add(l);
    }

    private void addCardSection(VBox parent, String titre, String contenu, String color, String bg) {
        VBox box = card(bg, color);
        addLabel(box, titre, 12, color, true);
        Label c = new Label(contenu); c.setStyle("-fx-font-size:11;-fx-text-fill:#333;"); c.setWrapText(true);
        box.getChildren().add(c);
        parent.getChildren().add(box);
    }

    private void afficherChargement(VBox parent, String msg) {
        VBox box = new VBox(16); box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding:70 0;");
        ProgressIndicator pi = new ProgressIndicator();
        pi.setPrefSize(48, 48);
        pi.setStyle("-fx-accent:#9c27b0;");
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-font-size:12;-fx-text-fill:#9c27b0;");
        lbl.setWrapText(true); lbl.setAlignment(Pos.CENTER);
        box.getChildren().addAll(pi, lbl);
        parent.getChildren().add(box);
    }

    private void afficherPlaceholder(VBox parent, String msg) {
        VBox box = new VBox(10); box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding:50 0;");
        Label l = new Label(msg);
        l.setStyle("-fx-font-size:12;-fx-text-fill:#bbb;");
        l.setWrapText(true); l.setAlignment(Pos.CENTER);
        box.getChildren().add(l);
        parent.getChildren().add(box);
    }

    private VBox card(String bg, String border) {
        VBox v = new VBox(8);
        v.setStyle("-fx-background-color:" + bg + ";"
                + "-fx-border-color:" + border + "22;"
                + "-fx-border-radius:10;-fx-background-radius:10;"
                + "-fx-padding:14;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),6,0,0,2);");
        return v;
    }

    private void addLabel(VBox parent, String text, int size, String color, boolean bold) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + ";-fx-text-fill:" + color + ";"
                + (bold ? "-fx-font-weight:bold;" : ""));
        l.setWrapText(true);
        parent.getChildren().add(l);
    }

    private String btnStyle(String bg, String fg) {
        return "-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";"
                + "-fx-background-radius:8;-fx-font-size:11;-fx-font-weight:bold;"
                + "-fx-padding:9 18;-fx-cursor:hand;-fx-border-color:transparent;";
    }

    private void animerEntreeElements(VBox parent) {
        int delai = 0;
        for (var child : parent.getChildren()) {
            child.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(280), child);
            ft.setFromValue(0); ft.setToValue(1);
            ft.setDelay(Duration.millis(delai));
            ft.play();
            delai += 70;
        }
    }

    // ══ NAVIGATION & UTILS ════════════════════════════════════
    @FXML
    public void retour() {
        if (ttsProcess != null && ttsProcess.isAlive()) ttsProcess.destroy();
        ((Stage) groqPanel.getScene().getWindow()).close();
    }

    private void setAllBtnsDisabled(boolean disabled) {
        for (Button b : List.of(btnGroq, btnHF, btnVision, btnGemini, btnVoix))
            if (b != null) b.setDisable(disabled);
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
        System.out.println("[STATUS] " + msg);
    }

    private void showError(String msg) {
        if (errorLabel == null) return;
        errorLabel.setStyle("-fx-text-fill:#c62828;");
        errorLabel.setText(msg);
        System.err.println("[ERROR] " + msg);
        PauseTransition p = new PauseTransition(Duration.seconds(8));
        p.setOnFinished(e -> errorLabel.setText(""));
        p.play();
    }

    private String rootMsg(Throwable ex) {
        Throwable c = ex;
        while (c.getCause() != null) c = c.getCause();
        return c.getMessage() != null ? c.getMessage() : ex.getMessage();
    }

    private String nvl(String s, String def) {
        return (s != null && !s.isBlank()) ? s : def;
    }
}