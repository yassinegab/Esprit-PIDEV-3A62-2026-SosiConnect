package org.example.servicesociaux.frontoffice.controller.controllers;

import org.example.servicesociaux.frontoffice.controller.entities.RendezVous;
import org.example.servicesociaux.frontoffice.controller.services.RendezVousService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class RendezVousController {

    // ── Formulaire ──
    @FXML private ComboBox<String>  hopitalCombo;
    @FXML private ComboBox<String>  typeConsultation;
    @FXML private ComboBox<String>  statut;
    @FXML private DatePicker        datePicker;
    @FXML private ComboBox<String>  heureCombo;
    @FXML private ComboBox<String>  minuteCombo;
    @FXML private TextArea          notesField;
    @FXML private Label             errorLabel;
    @FXML private Button            annulerBtn;

    // ── Statistiques ──
    @FXML private Label statTotal;
    @FXML private Label statEnAttente;
    @FXML private Label statConfirme;
    @FXML private Label statTermine;
    @FXML private Label statAnnule;

    // ── Filtres ──
    @FXML private TextField         searchField;
    @FXML private ComboBox<String>  filterStatut;
    @FXML private ComboBox<String>  filterType;
    @FXML private ComboBox<String>  filterHopital;

    // ── Tableau ──
    @FXML private TableView<RendezVous>            tableView;
    @FXML private TableColumn<RendezVous, Integer> colId;
    @FXML private TableColumn<RendezVous, Integer> colPatient;
    @FXML private TableColumn<RendezVous, String>  colHopital;
    @FXML private TableColumn<RendezVous, String>  colType;
    @FXML private TableColumn<RendezVous, String>  colStatut;
    @FXML private TableColumn<RendezVous, Date>    colDate;
    @FXML private TableColumn<RendezVous, String>  colNotes;

    private final RendezVousService    service     = new RendezVousService();
    private final Map<String, Integer> hopitauxMap = new LinkedHashMap<>();

    private ObservableList<RendezVous> masterList  = FXCollections.observableArrayList();
    private FilteredList<RendezVous>   filteredList;

    private int selectedId        = -1;
    private int selectedHopitalId = -1;

    // ──────────────────────────────────────────────
    //  Cellule stylisée (gris à la sélection)
    // ──────────────────────────────────────────────
    private <T> TableCell<RendezVous, T> createStyledCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item.toString()); applyStyle(); }
            }
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                applyStyle();
            }
            private void applyStyle() {
                TableRow<?> row = getTableRow();
                if (row != null && row.isSelected()) {
                    setStyle("-fx-background-color: #e0e0e0;" +
                            "-fx-text-fill: #212121;" +
                            "-fx-font-weight: bold;" +
                            "-fx-border-color: #bdbdbd;" +
                            "-fx-border-width: 0 0 1 0;");
                } else {
                    setStyle("-fx-background-color: transparent;" +
                            "-fx-text-fill: #444444;" +
                            "-fx-font-weight: normal;");
                }
            }
        };
    }

    private void showMessage(String msg) {
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11;");
        errorLabel.setText(msg);
        PauseTransition p = new PauseTransition(Duration.seconds(2));
        p.setOnFinished(e -> errorLabel.setText(""));
        p.play();
    }

    // ──────────────────────────────────────────────
    //  initialize
    // ──────────────────────────────────────────────
    @FXML
    public void initialize() {

        annulerBtn.setVisible(false);
        annulerBtn.setManaged(false);

        for (int h = 0; h < 24; h++)
            heureCombo.getItems().add(String.format("%02d", h));
        for (int m = 0; m < 60; m += 15)
            minuteCombo.getItems().add(String.format("%02d", m));
        heureCombo.setValue("08");
        minuteCombo.setValue("00");

        try {
            Map<Integer, String> hopitaux = service.getHopitaux();
            hopitaux.forEach((id, nom) -> {
                hopitauxMap.put(nom, id);
                hopitalCombo.getItems().add(nom);
                filterHopital.getItems().add(nom);
            });
        } catch (SQLException e) {
            showMessage("❌ Impossible de charger les hôpitaux");
        }

        hopitalCombo.setOnAction(e -> {
            String nom = hopitalCombo.getValue();
            if (nom != null) selectedHopitalId = hopitauxMap.getOrDefault(nom, -1);
        });

        typeConsultation.getItems().addAll("Présentiel", "Téléconsultation", "Urgence");
        statut.getItems().addAll("En attente", "Confirmé", "Terminé", "Annulé");

        filterStatut.getItems().add("Tous");
        filterStatut.getItems().addAll("En attente", "Confirmé", "Terminé", "Annulé");
        filterStatut.setValue("Tous");

        filterType.getItems().add("Tous");
        filterType.getItems().addAll("Présentiel", "Téléconsultation", "Urgence");
        filterType.setValue("Tous");

        filterHopital.getItems().add(0, "Tous");
        filterHopital.setValue("Tous");

        colId     .setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colHopital.setCellValueFactory(new PropertyValueFactory<>("hopitalNom"));
        colType   .setCellValueFactory(new PropertyValueFactory<>("typeConsultation"));
        colStatut .setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate   .setCellValueFactory(new PropertyValueFactory<>("dateRendezVous"));
        colNotes  .setCellValueFactory(new PropertyValueFactory<>("notes"));

        colId     .setCellFactory(col -> createStyledCell());
        colPatient.setCellFactory(col -> createStyledCell());
        colHopital.setCellFactory(col -> createStyledCell());
        colType   .setCellFactory(col -> createStyledCell());
        colStatut .setCellFactory(col -> createStyledCell());
        colDate   .setCellFactory(col -> createStyledCell());
        colNotes  .setCellFactory(col -> createStyledCell());

        tableView.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        // Forcer la sélection grise
        tableView.getStylesheets().add(
                "data:text/css," +
                        ".table-row-cell:selected { -fx-background-color: %23e0e0e0 !important; }" +
                        ".table-row-cell:selected:focused { -fx-background-color: %23e0e0e0 !important; }" +
                        ".table-row-cell:selected .table-cell { -fx-background-color: transparent !important; }" +
                        ".table-row-cell:focused .table-cell:selected { -fx-background-color: transparent !important; }"
        );

        filteredList = new FilteredList<>(masterList, p -> true);
        tableView.setItems(filteredList);

        searchField.textProperty().addListener((o, ov, nv) -> appliquerFiltres());
        filterStatut.setOnAction(e -> appliquerFiltres());
        filterType.setOnAction(e -> appliquerFiltres());
        filterHopital.setOnAction(e -> appliquerFiltres());

        tableView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected == null) return;
                    selectedId        = selected.getId();
                    selectedHopitalId = selected.getHopitalId();

                    hopitauxMap.forEach((nom, id) -> {
                        if (id == selectedHopitalId) hopitalCombo.setValue(nom);
                    });
                    typeConsultation.setValue(selected.getTypeConsultation());
                    statut.setValue(selected.getStatut());

                    if (selected.getDateRendezVous() != null) {
                        try {
                            java.sql.Timestamp ts = new java.sql.Timestamp(
                                    selected.getDateRendezVous().getTime());
                            datePicker.setValue(ts.toLocalDateTime().toLocalDate());
                            heureCombo.setValue(String.format("%02d",
                                    ts.toLocalDateTime().getHour()));
                            minuteCombo.setValue(String.format("%02d",
                                    (ts.toLocalDateTime().getMinute() / 15) * 15));
                        } catch (Exception e) { datePicker.setValue(null); }
                    } else {
                        datePicker.setValue(null);
                    }

                    notesField.setText(selected.getNotes() != null ? selected.getNotes() : "");
                    annulerBtn.setVisible(true);
                    annulerBtn.setManaged(true);
                });

        chargerTableau();
    }

    // ──────────────────────────────────────────────
    //  Filtrage
    // ──────────────────────────────────────────────
    private void appliquerFiltres() {
        String search  = searchField.getText() == null ? ""
                : searchField.getText().toLowerCase().trim();
        String fStatut = filterStatut.getValue()  == null ? "Tous" : filterStatut.getValue();
        String fType   = filterType.getValue()    == null ? "Tous" : filterType.getValue();
        String fHop    = filterHopital.getValue() == null ? "Tous" : filterHopital.getValue();

        filteredList.setPredicate(rdv -> {
            boolean matchSearch = search.isEmpty()
                    || String.valueOf(rdv.getId()).contains(search)
                    || String.valueOf(rdv.getPatientId()).contains(search)
                    || (rdv.getHopitalNom()       != null && rdv.getHopitalNom().toLowerCase().contains(search))
                    || (rdv.getTypeConsultation() != null && rdv.getTypeConsultation().toLowerCase().contains(search))
                    || (rdv.getStatut()           != null && rdv.getStatut().toLowerCase().contains(search))
                    || (rdv.getNotes()            != null && rdv.getNotes().toLowerCase().contains(search))
                    || (rdv.getDateRendezVous()   != null && rdv.getDateRendezVous().toString().contains(search));

            boolean matchStatut = fStatut.equals("Tous") || fStatut.equals(rdv.getStatut());
            boolean matchType   = fType.equals("Tous")   || fType.equals(rdv.getTypeConsultation());
            boolean matchHop    = fHop.equals("Tous")    || fHop.equals(rdv.getHopitalNom());

            return matchSearch && matchStatut && matchType && matchHop;
        });
    }

    // ──────────────────────────────────────────────
    //  Statistiques (calculées sur masterList entière)
    // ──────────────────────────────────────────────
    private void mettreAJourStats() {
        int total    = masterList.size();
        int attente  = (int) masterList.stream()
                .filter(r -> "En attente".equals(r.getStatut())).count();
        int confirme = (int) masterList.stream()
                .filter(r -> "Confirmé".equals(r.getStatut())).count();
        int termine  = (int) masterList.stream()
                .filter(r -> "Terminé".equals(r.getStatut())).count();
        int annule   = (int) masterList.stream()
                .filter(r -> "Annulé".equals(r.getStatut())).count();

        statTotal.setText(String.valueOf(total));
        statEnAttente.setText(String.valueOf(attente));
        statConfirme.setText(String.valueOf(confirme));
        statTermine.setText(String.valueOf(termine));
        statAnnule.setText(String.valueOf(annule));
    }

    // ──────────────────────────────────────────────
    //  Export CSV — FileChooser
    // ──────────────────────────────────────────────
    @FXML
    public void exporterCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'export CSV");
        fileChooser.setInitialFileName("rendez_vous_export.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV (*.csv)", "*.csv")
        );

        // Dossier Téléchargements par défaut
        File downloads = new File(System.getProperty("user.home") + "/Downloads");
        if (!downloads.exists()) downloads = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(downloads);

        Stage stage = (Stage) tableView.getScene().getWindow();
        File fichier = fileChooser.showSaveDialog(stage);

        if (fichier == null) return; // annulé par l'utilisateur

        // Exporter les lignes visibles dans le tableau (respect des filtres)
        ObservableList<RendezVous> data = tableView.getItems();

        try (FileWriter fw = new FileWriter(fichier)) {
            fw.write("ID,Patient ID,Hôpital,Type,Statut,Date & Heure,Notes\n");
            for (RendezVous rdv : data) {
                fw.write(String.format("%s,%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        rdv.getId(),
                        rdv.getPatientId(),
                        safe(rdv.getHopitalNom()),
                        safe(rdv.getTypeConsultation()),
                        safe(rdv.getStatut()),
                        rdv.getDateRendezVous() != null ? rdv.getDateRendezVous().toString() : "",
                        safe(rdv.getNotes())
                ));
            }
            showMessage("✅ Export réussi : " + fichier.getName());
            errorLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-size: 11;");
        } catch (IOException e) {
            showMessage("❌ Erreur export : " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  CRUD
    // ──────────────────────────────────────────────
    @FXML
    public void ajouter() {
        if (!valider()) return;
        try {
            RendezVous rdv = new RendezVous(0, 7, selectedHopitalId,
                    typeConsultation.getValue(), statut.getValue(),
                    toDateTime(datePicker.getValue()), notesField.getText());
            rdv.setHopitalNom(hopitalCombo.getValue());
            service.ajouter(rdv);
            chargerTableau();
            clearSelection();
            showMessage("✅ Rendez-vous ajouté !");
        } catch (Exception e) { showMessage("❌ Erreur : " + e.getMessage()); }
    }

    @FXML
    public void modifier() {
        if (selectedId == -1) { showMessage("❌ Sélectionne un rendez-vous !"); return; }
        if (!valider()) return;
        try {
            RendezVous rdv = new RendezVous(selectedId, 7, selectedHopitalId,
                    typeConsultation.getValue(), statut.getValue(),
                    toDateTime(datePicker.getValue()), notesField.getText());
            rdv.setHopitalNom(hopitalCombo.getValue());
            service.modifier(rdv);

            for (int i = 0; i < masterList.size(); i++) {
                if (masterList.get(i).getId() == selectedId) {
                    masterList.set(i, rdv); break;
                }
            }
            tableView.refresh();
            mettreAJourStats();
            clearSelection();
            showMessage("✅ Rendez-vous modifié !");
        } catch (Exception e) { showMessage("❌ Erreur : " + e.getMessage()); }
    }

    @FXML
    public void supprimer() {
        if (selectedId == -1) { showMessage("❌ Sélectionne un rendez-vous !"); return; }
        try {
            service.supprimer(selectedId);
            masterList.removeIf(r -> r.getId() == selectedId);
            tableView.refresh();
            mettreAJourStats();
            clearSelection();
            showMessage("✅ Rendez-vous supprimé !");
        } catch (Exception e) { showMessage("❌ Erreur : " + e.getMessage()); }
    }

    @FXML
    public void annuler() {
        clearSelection();
        errorLabel.setText("");
    }

    private void clearSelection() {
        vider();
        selectedId = -1;
        tableView.getSelectionModel().clearSelection();
        annulerBtn.setVisible(false);
        annulerBtn.setManaged(false);
    }

    // ──────────────────────────────────────────────
    //  Navigation
    // ──────────────────────────────────────────────
    @FXML
    public void retourAccueil() {
        try {
            URL url = getClass().getResource("/servicesociaux/frontoffice/mainMenu.fxml");
            if (url == null) {
                System.err.println("❌ mainMenu.fxml introuvable");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 880, 680));
            stage.setTitle("🌸 MediCare — Accueil");
        } catch (Exception e) {
            // ✅ utilise directement errorLabel sans méthode
            errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11;");
            errorLabel.setText("❌ Erreur navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void afficherTous() { chargerTableau(); }

    // ──────────────────────────────────────────────
    //  Chargement
    // ──────────────────────────────────────────────
    private void chargerTableau() {
        try {
            masterList.setAll(service.afficherTous());
            appliquerFiltres();
            mettreAJourStats();
        } catch (SQLException e) { showMessage("❌ Erreur chargement : " + e.getMessage()); }
    }

    // ──────────────────────────────────────────────
    //  Validation
    // ──────────────────────────────────────────────
    private boolean valider() {
        if (hopitalCombo.getValue() == null) {
            showMessage("❌ Veuillez sélectionner un hôpital !"); return false; }
        if (typeConsultation.getValue() == null) {
            showMessage("❌ Le type de consultation est obligatoire !"); return false; }
        if (statut.getValue() == null) {
            showMessage("❌ Le statut est obligatoire !"); return false; }
        if (datePicker.getValue() == null) {
            showMessage("❌ La date est obligatoire !"); return false; }
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showMessage("❌ La date ne peut pas être dans le passé !"); return false; }
        return true;
    }

    private Date toDateTime(LocalDate date) {
        int h = Integer.parseInt(heureCombo.getValue() != null ? heureCombo.getValue() : "08");
        int m = Integer.parseInt(minuteCombo.getValue() != null ? minuteCombo.getValue() : "00");
        return java.sql.Timestamp.valueOf(date.atTime(h, m));
    }

    private void vider() {
        hopitalCombo.setValue(null);
        typeConsultation.setValue(null);
        statut.setValue(null);
        datePicker.setValue(null);
        heureCombo.setValue("08");
        minuteCombo.setValue("00");
        notesField.clear();
    }

    // null-safe String pour le CSV
    private String safe(String s) {
        return s != null ? s.replace("\"", "'") : "";
    }
}
