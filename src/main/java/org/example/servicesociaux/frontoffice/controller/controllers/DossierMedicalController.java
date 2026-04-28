package org.example.servicesociaux.frontoffice.controller.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.user.model.DossierMedical;
import org.example.user.service.ServiceDossierMedical;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DossierMedicalController {

    // ── Liste ──
    @FXML private VBox   listPanel;
    @FXML private VBox   dossiersListBox;

    // ── Formulaire ──
    @FXML private VBox   formPanel;
    @FXML private TextArea antecedentsArea, maladiesArea, allergiesArea;
    @FXML private TextArea traitementsArea, diagnosticsArea, notesArea, objectifArea;
    @FXML private ComboBox<String> activiteCombo;
    @FXML private Label  formTitleLabel;

    // ── Detail dossier ──
    @FXML private VBox   detailPanel;
    @FXML private Label  detailTitleLabel;
    @FXML private Button btnGroq;
    @FXML private Button btnHF;

    // ── Statut / erreur ──
    @FXML private Label  statusLabel;
    @FXML private Label  errorLabel;

    private final ServiceDossierMedical service = new ServiceDossierMedical();
    private DossierMedical dossierEnCours;
    private DossierMedical dossierSelectionne;  // dossier affiché à droite
    private int userId = -1;

    // ══════════════════════════════════════════════
    @FXML
    public void initialize() {
        activiteCombo.getItems().addAll(
                "Sedentaire", "Leger", "Modere", "Actif", "Tres actif");
        activiteCombo.setValue("Sedentaire");

        formPanel.setVisible(false);
        formPanel.setManaged(false);

        try {
            var user = SessionManager.getCurrentUser();
            if (user != null) userId = user.getId();
        } catch (Exception ignored) {}

        chargerListe();
    }

    // ══ LISTE ═════════════════════════════════════
    private void chargerListe() {
        dossiersListBox.getChildren().clear();
        try {
            List<DossierMedical> liste = userId > 0
                    ? service.findAllByUserId(userId)
                    : service.findAll();

            statusLabel.setText(liste.size() + " dossier(s)");

            if (liste.isEmpty()) {
                Label empty = new Label("Aucun dossier medical. Cliquez sur + Nouveau.");
                empty.setStyle("-fx-font-size:12;-fx-text-fill:#aaa;-fx-padding:30;");
                dossiersListBox.getChildren().add(empty);
                return;
            }

            for (DossierMedical d : liste) {
                dossiersListBox.getChildren().add(creerCarte(d));
            }

            // Selectionner le premier par defaut
            afficherDetail(liste.get(0));

        } catch (SQLException e) {
            showError("Erreur chargement : " + e.getMessage());
        }
    }

    // ══ CARTE DOSSIER ══════════════════════════════
    private VBox creerCarte(DossierMedical d) {
        VBox card = new VBox(6);
        card.setStyle(styleCard(false));
        card.setCursor(javafx.scene.Cursor.HAND);

        // Ligne 1 : ID + date
        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label idLbl = new Label("Dossier #" + d.getId());
        idLbl.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#6a1b9a;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label dateLbl = new Label(d.getDateCreationFormatee());
        dateLbl.setStyle("-fx-font-size:10;-fx-text-fill:#aaa;");
        top.getChildren().addAll(idLbl, sp, dateLbl);

        // Ligne 2 : maladies (apercu)
        String maladie = d.getMaladiesChroniques() != null ? d.getMaladiesChroniques() : "Aucune maladie renseignee";
        Label malLbl = new Label(maladie.length() > 55 ? maladie.substring(0, 55) + "..." : maladie);
        malLbl.setStyle("-fx-font-size:11;-fx-text-fill:#555;");
        malLbl.setWrapText(true);

        // Ligne 3 : badge activite + boutons
        HBox bot = new HBox(8);
        bot.setAlignment(Pos.CENTER_LEFT);
        Label actBadge = new Label(nvl(d.getNiveauActivite(), "—"));
        actBadge.setStyle("-fx-background-color:#ede7f6;-fx-text-fill:#6a1b9a;" +
                "-fx-padding:2 10;-fx-background-radius:12;-fx-font-size:10;");
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);

        Button edit = smallBtn("Modifier", "#ede7f6", "#6a1b9a");
        edit.setOnAction(e -> ouvrirFormulaire(d));

        Button del = smallBtn("Supprimer", "#fce4ec", "#c62828");
        del.setOnAction(e -> supprimerDossier(d));

        bot.getChildren().addAll(actBadge, sp2, edit, del);
        card.getChildren().addAll(top, malLbl, bot);

        // Clic → afficher detail à droite
        card.setOnMouseClicked(e -> afficherDetail(d));
        card.setOnMouseEntered(e -> card.setStyle(styleCard(true)));
        card.setOnMouseExited(e  -> card.setStyle(styleCard(false)));

        return card;
    }

    private String styleCard(boolean hover) {
        return "-fx-background-color:white;" +
                "-fx-border-color:" + (hover ? "#9c27b0" : "#e0d6f5") + ";" +
                "-fx-border-radius:10;-fx-background-radius:10;" +
                "-fx-padding:12;-fx-effect:dropshadow(gaussian," +
                (hover ? "rgba(0,0,0,0.10)" : "rgba(0,0,0,0.04)") + ",6,0,0,2);";
    }

    private Button smallBtn(String txt, String bg, String fg) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";" +
                "-fx-background-radius:6;-fx-font-size:10;" +
                "-fx-padding:4 10;-fx-cursor:hand;-fx-border-color:transparent;");
        return b;
    }

    // ══ DETAIL DOSSIER (colonne droite) ═══════════
    private void afficherDetail(DossierMedical d) {
        dossierSelectionne = d;
        detailTitleLabel.setText("Dossier #" + d.getId());
        btnGroq.setVisible(true); btnGroq.setManaged(true);
        btnHF.setVisible(true);   btnHF.setManaged(true);

        detailPanel.getChildren().clear();

        // Carte score/dates
        HBox metaBox = new HBox(16);
        metaBox.setStyle("-fx-background-color:white;-fx-border-color:#e0d6f5;" +
                "-fx-border-radius:10;-fx-background-radius:10;-fx-padding:14;");

        VBox dateCreBox = metaVBox("Date de creation", d.getDateCreationFormatee(), "#6a1b9a");
        VBox dateMajBox = metaVBox("Derniere modification", d.getDerniereMiseAJourFormatee(), "#888");
        VBox actBox     = metaVBox("Niveau d'activite", nvl(d.getNiveauActivite(), "Non renseigne"), "#388e3c");

        metaBox.getChildren().addAll(dateCreBox, dateMajBox, actBox);
        detailPanel.getChildren().add(metaBox);

        // Sections medicales
        addSection(detailPanel, "Maladies chroniques",    d.getMaladiesChroniques(),    "#c62828", "#ffebee");
        addSection(detailPanel, "Antecedents medicaux",   d.getAntecedentsMedicaux(),   "#6a1b9a", "#f3e5f5");
        addSection(detailPanel, "Allergies",               d.getAllergies(),              "#e65100", "#fff3e0");
        addSection(detailPanel, "Traitements en cours",   d.getTraitementsEnCours(),    "#1565c0", "#e3f2fd");
        addSection(detailPanel, "Diagnostics",             d.getDiagnostics(),           "#2e7d32", "#e8f5e9");
        addSection(detailPanel, "Notes du medecin",        d.getNotesMedecin(),          "#37474f", "#eceff1");
        addSection(detailPanel, "Objectif sante",          d.getObjectifSante(),         "#6a1b9a", "#f3e5f5");

        // Boutons IA en bas du detail
        HBox iaBtns = new HBox(10);
        iaBtns.setStyle("-fx-background-color:white;-fx-border-color:#e0d6f5;" +
                "-fx-border-radius:10;-fx-background-radius:10;-fx-padding:14;");
        iaBtns.setAlignment(Pos.CENTER);

        Button g = new Button("Analyser avec Groq LLaMA");
        g.setStyle("-fx-background-color:#1a237e;-fx-text-fill:white;" +
                "-fx-background-radius:8;-fx-font-size:12;-fx-font-weight:bold;" +
                "-fx-padding:10 20;-fx-cursor:hand;-fx-border-color:transparent;");
        g.setOnAction(e -> ouvrirAnalyseGroq());

        Button h = new Button("Analyser avec HuggingFace");
        h.setStyle("-fx-background-color:#e65100;-fx-text-fill:white;" +
                "-fx-background-radius:8;-fx-font-size:12;-fx-font-weight:bold;" +
                "-fx-padding:10 20;-fx-cursor:hand;-fx-border-color:transparent;");
        h.setOnAction(e -> ouvrirAnalyseHF());

        iaBtns.getChildren().addAll(g, h);
        detailPanel.getChildren().add(iaBtns);
    }

    private void addSection(VBox parent, String titre, String contenu,
                            String color, String bgColor) {
        String texte = contenu != null && !contenu.isBlank()
                ? contenu : "Non renseigne";

        VBox box = new VBox(8);
        box.setStyle("-fx-background-color:" + bgColor + ";" +
                "-fx-border-color:" + color + ";" +
                "-fx-border-width:0 0 0 4;" +
                "-fx-border-radius:0 8 8 0;-fx-background-radius:0 8 8 0;" +
                "-fx-padding:12 14;");

        Label titre_lbl = new Label(titre.toUpperCase());
        titre_lbl.setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:" + color + ";");

        Label contenu_lbl = new Label(texte);
        contenu_lbl.setWrapText(true);
        contenu_lbl.setStyle("-fx-font-size:12;-fx-text-fill:#333;-fx-line-spacing:2;");

        box.getChildren().addAll(titre_lbl, contenu_lbl);
        parent.getChildren().add(box);
    }

    private VBox metaVBox(String label, String valeur, String color) {
        VBox v = new VBox(3);
        HBox.setHgrow(v, Priority.ALWAYS);
        Label l = new Label(label);
        l.setStyle("-fx-font-size:10;-fx-text-fill:#aaa;");
        Label val = new Label(valeur != null ? valeur : "—");
        val.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        v.getChildren().addAll(l, val);
        return v;
    }

    // ══ OUVRIR PAGE ANALYSE ════════════════════════
    @FXML
    public void ouvrirAnalyseGroq() {
        if (dossierSelectionne == null) { showError("Selectionnez un dossier."); return; }
        ouvrirAnalyse("GROQ");
    }

    @FXML
    public void ouvrirAnalyseHF() {
        if (dossierSelectionne == null) { showError("Selectionnez un dossier."); return; }
        ouvrirAnalyse("HF");
    }

    private void ouvrirAnalyse(String mode) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/servicesociaux/frontoffice/analyseAI.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load(), 1050, 680));
            stage.setTitle("Analyse IA — Dossier #" + dossierSelectionne.getId());

            AnalyseIAController ctrl = loader.getController();
            ctrl.setDossier(dossierSelectionne, mode);

            stage.show();
        } catch (IOException e) {
            showError("Erreur ouverture analyse : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══ FORMULAIRE ════════════════════════════════
    @FXML
    public void afficherFormulaireNouveauDossier() {
        dossierEnCours = null;
        vider();
        formTitleLabel.setText("Nouveau dossier medical");
        formPanel.setVisible(true);  formPanel.setManaged(true);
        listPanel.setVisible(false); listPanel.setManaged(false);
    }

    private void ouvrirFormulaire(DossierMedical d) {
        dossierEnCours = d;
        antecedentsArea.setText(nvl(d.getAntecedentsMedicaux(), ""));
        maladiesArea   .setText(nvl(d.getMaladiesChroniques(),  ""));
        allergiesArea  .setText(nvl(d.getAllergies(),            ""));
        traitementsArea.setText(nvl(d.getTraitementsEnCours(),  ""));
        diagnosticsArea.setText(nvl(d.getDiagnostics(),         ""));
        notesArea      .setText(nvl(d.getNotesMedecin(),        ""));
        objectifArea   .setText(nvl(d.getObjectifSante(),       ""));
        String act = d.getNiveauActivite();
        activiteCombo.setValue(act != null && !act.isBlank() ? act : "Sedentaire");
        formTitleLabel.setText("Modifier dossier #" + d.getId());
        formPanel.setVisible(true);  formPanel.setManaged(true);
        listPanel.setVisible(false); listPanel.setManaged(false);
    }

    @FXML
    public void fermerFormulaire() {
        formPanel.setVisible(false); formPanel.setManaged(false);
        listPanel.setVisible(true);  listPanel.setManaged(true);
        chargerListe();
    }

    @FXML
    public void sauvegarder() {
        try {
            if (dossierEnCours == null) {
                dossierEnCours = new DossierMedical();
                dossierEnCours.setUserId(userId > 0 ? userId : 0);
            }
            dossierEnCours.setAntecedentsMedicaux(antecedentsArea.getText().trim());
            dossierEnCours.setMaladiesChroniques (maladiesArea   .getText().trim());
            dossierEnCours.setAllergies          (allergiesArea  .getText().trim());
            dossierEnCours.setTraitementsEnCours (traitementsArea.getText().trim());
            dossierEnCours.setDiagnostics        (diagnosticsArea.getText().trim());
            dossierEnCours.setNotesMedecin       (notesArea      .getText().trim());
            dossierEnCours.setObjectifSante      (objectifArea   .getText().trim());
            dossierEnCours.setNiveauActivite     (activiteCombo.getValue());

            if (dossierEnCours.getId() == 0) service.add(dossierEnCours);
            else                              service.update(dossierEnCours);

            showSuccess("Dossier sauvegarde !");
            fermerFormulaire();
        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    private void supprimerDossier(DossierMedical d) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le dossier #" + d.getId() + " ?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.delete(d.getId());
                    showSuccess("Dossier supprime.");
                    detailPanel.getChildren().clear();
                    btnGroq.setVisible(false); btnGroq.setManaged(false);
                    btnHF.setVisible(false);   btnHF.setManaged(false);
                    detailTitleLabel.setText("Selectionnez un dossier");
                    dossierSelectionne = null;
                    chargerListe();
                } catch (Exception e) {
                    showError("Erreur : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void vider() {
        antecedentsArea.clear(); maladiesArea.clear();
        allergiesArea.clear();   traitementsArea.clear();
        diagnosticsArea.clear(); notesArea.clear();
        objectifArea.clear();    activiteCombo.setValue("Sedentaire");
    }

    @FXML
    public void retourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/servicesociaux/frontoffice/mainMenu.fxml"));
            Stage stage = (Stage) detailPanel.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 900, 650));
            stage.setTitle("MediCare — Accueil");
        } catch (Exception e) {
            showError("Navigation : " + e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setStyle("-fx-text-fill:#c62828;");
        errorLabel.setText(msg);
        PauseTransition p = new PauseTransition(Duration.seconds(5));
        p.setOnFinished(e -> errorLabel.setText(""));
        p.play();
    }

    private void showSuccess(String msg) {
        errorLabel.setStyle("-fx-text-fill:#2e7d32;");
        errorLabel.setText(msg);
        PauseTransition p = new PauseTransition(Duration.seconds(4));
        p.setOnFinished(e -> errorLabel.setText(""));
        p.play();
    }

    private String nvl(String s, String def) { return (s != null && !s.isBlank()) ? s : def; }
}