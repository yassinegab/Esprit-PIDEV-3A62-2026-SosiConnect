package org.example.servicesociaux.frontoffice.controller.controllers;

import org.example.servicesociaux.frontoffice.controller.services.HopitalService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
// Et pour iText7 :
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class HopitalController {

    // ── Statistiques ──
    @FXML private Label statTotalHopitaux;
    @FXML private Label statUrgence;
    @FXML private Label statTotalRdv;
    @FXML private Label statEnAttente;

    // ── Filtres & recherche ──
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterUrgence;
    @FXML private ComboBox<String> sortCombo;

    // ── Tableau ──
    @FXML private TableView<Object[]>            tableView;
    @FXML private TableColumn<Object[], Integer> colId, colNbRdv, colRdvWait;
    @FXML private TableColumn<Object[], String>  colNom, colAdresse, colTel, colSpec, colUrgence;

    // ── Message ──
    @FXML private Label errorLabel;

    private final HopitalService service = new HopitalService();

    // Object[] index :
    //  [0] id (int)
    //  [1] nom (String)
    //  [2] adresse (String)
    //  [3] tel (String)
    //  [4] specialites (String)
    //  [5] ville (String)
    //  [6] nb_rdv (int)
    //  [7] rdv_en_attente (int)
    //  [8] capacite (int)
    //  [9] service_urgence_dispo (boolean)
    private ObservableList<Object[]> masterList  = FXCollections.observableArrayList();
    private FilteredList<Object[]>   filteredList;

    // ──────────────────────────────────────────────
    //  Cellule stylisée (gris à la sélection)
    // ──────────────────────────────────────────────
    private <T> TableCell<Object[], T> createStyledCell() {
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
        PauseTransition p = new PauseTransition(Duration.seconds(4));
        p.setOnFinished(e -> errorLabel.setText(""));
        p.play();
    }

    private void showSuccess(String msg) {
        errorLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-size: 11;");
        errorLabel.setText(msg);
        PauseTransition p = new PauseTransition(Duration.seconds(4));
        p.setOnFinished(e -> errorLabel.setText(""));
        p.play();
    }

    // ──────────────────────────────────────────────
    //  initialize
    // ──────────────────────────────────────────────
    @FXML
    public void initialize() {

        // ── Liaisons colonnes ──
        colId.setCellValueFactory(d ->
                new SimpleIntegerProperty((int) d.getValue()[0]).asObject());
        colNom.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue()[1])));
        colAdresse.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue()[2])));
        colTel.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue()[3])));
        colSpec.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue()[4])));
        colUrgence.setCellValueFactory(d -> {
            Object val = d.getValue()[9];
            boolean urgence = val != null && (boolean) val;
            return new SimpleStringProperty(urgence ? "✔" : "✖");
        });
        colNbRdv.setCellValueFactory(d ->
                new SimpleIntegerProperty((int) d.getValue()[6]).asObject());
        colRdvWait.setCellValueFactory(d ->
                new SimpleIntegerProperty((int) d.getValue()[7]).asObject());

        // ── Style cellules ──
        colId      .setCellFactory(col -> createStyledCell());
        colNom     .setCellFactory(col -> createStyledCell());
        colAdresse .setCellFactory(col -> createStyledCell());
        colTel     .setCellFactory(col -> createStyledCell());
        colSpec    .setCellFactory(col -> createStyledCell());
        colUrgence .setCellFactory(col -> createStyledCell());
        colNbRdv   .setCellFactory(col -> createStyledCell());
        colRdvWait .setCellFactory(col -> createStyledCell());

        // ── Style tableau ──
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

        // ── Filtres ──
        filterUrgence.getItems().addAll("Tous", "Urgence disponible", "Sans urgence");
        filterUrgence.setValue("Tous");

        sortCombo.getItems().addAll(
                "Nom (A → Z)",
                "Nom (Z → A)",
                "Nb RDV (croissant)",
                "Nb RDV (décroissant)",
                "En attente (décroissant)"
        );
        sortCombo.setValue("Nom (A → Z)");

        // ── Listeners ──
        searchField.textProperty().addListener((o, ov, nv) -> appliquerFiltres());
        filterUrgence.setOnAction(e -> appliquerFiltres());
        sortCombo.setOnAction(e -> appliquerFiltres());

        chargerTableau();
    }

    // ──────────────────────────────────────────────
    //  Chargement
    // ──────────────────────────────────────────────
    private void chargerTableau() {
        try {
            List<Object[]> liste = service.afficherAvecRendezVous();
            masterList.setAll(liste);
            filteredList = new FilteredList<>(masterList, p -> true);
            appliquerFiltres();
            mettreAJourStats();
        } catch (SQLException e) {
            showMessage("❌ Erreur chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ──────────────────────────────────────────────
    //  Filtrage + tri
    // ──────────────────────────────────────────────
    private void appliquerFiltres() {
        if (filteredList == null) return;

        String search   = searchField.getText() == null ? ""
                : searchField.getText().toLowerCase().trim();
        String fUrgence = filterUrgence.getValue() == null ? "Tous"
                : filterUrgence.getValue();

        filteredList.setPredicate(row -> {
            String nom     = str(row[1]).toLowerCase();
            String adresse = str(row[2]).toLowerCase();
            String spec    = str(row[4]).toLowerCase();
            String tel     = str(row[3]).toLowerCase();
            String ville   = str(row[5]).toLowerCase();

            boolean matchSearch = search.isEmpty()
                    || nom.contains(search)
                    || adresse.contains(search)
                    || spec.contains(search)
                    || tel.contains(search)
                    || ville.contains(search)
                    || String.valueOf(row[0]).contains(search);

            boolean matchUrgence = true;
            if (fUrgence.equals("Urgence disponible")) {
                matchUrgence = row[9] != null && (boolean) row[9];
            } else if (fUrgence.equals("Sans urgence")) {
                matchUrgence = row[9] == null || !(boolean) row[9];
            }

            return matchSearch && matchUrgence;
        });

        // Tri
        ObservableList<Object[]> sortedData = FXCollections.observableArrayList(filteredList);
        String sort = sortCombo.getValue();
        if (sort != null) {
            Comparator<Object[]> comparator = switch (sort) {
                case "Nom (Z → A)"              -> Comparator.comparing((Object[] r) -> str(r[1])).reversed();
                case "Nb RDV (croissant)"       -> Comparator.comparingInt(r -> (int) r[6]);
                case "Nb RDV (décroissant)"     -> Comparator.comparingInt((Object[] r) -> (int) r[6]).reversed();
                case "En attente (décroissant)" -> Comparator.comparingInt((Object[] r) -> (int) r[7]).reversed();
                default                         -> Comparator.comparing(r -> str(r[1]));
            };
            sortedData.sort(comparator);
        }

        tableView.setItems(sortedData);
    }

    // ──────────────────────────────────────────────
    //  Statistiques
    // ──────────────────────────────────────────────
    private void mettreAJourStats() {
        int total    = masterList.size();
        int urgences = (int) masterList.stream()
                .filter(r -> r[9] != null && (boolean) r[9]).count();
        int totalRdv = masterList.stream().mapToInt(r -> (int) r[6]).sum();
        int attente  = masterList.stream().mapToInt(r -> (int) r[7]).sum();

        statTotalHopitaux.setText(String.valueOf(total));
        statUrgence.setText(String.valueOf(urgences));
        statTotalRdv.setText(String.valueOf(totalRdv));
        statEnAttente.setText(String.valueOf(attente));
    }

    // ──────────────────────────────────────────────
    //  Export CSV — FileChooser pour laisser
    //  l'utilisateur choisir où enregistrer le fichier
    // ──────────────────────────────────────────────
    @FXML
    public void exporterCSV() {
        // Ouvrir une boîte de dialogue "Enregistrer sous"
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'export CSV");
        fileChooser.setInitialFileName("hopitaux_export.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV (*.csv)", "*.csv")
        );

        // Proposer le dossier Téléchargements par défaut
        File downloads = new File(System.getProperty("user.home") + "/Downloads");
        if (!downloads.exists()) downloads = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(downloads);

        Stage stage = (Stage) tableView.getScene().getWindow();
        File fichier = fileChooser.showSaveDialog(stage);

        // L'utilisateur a annulé la boîte de dialogue
        if (fichier == null) return;

        // Écriture du CSV
        try (FileWriter fw = new FileWriter(fichier)) {
            // En-tête
            fw.write("ID,Nom,Adresse,Téléphone,Spécialités,Ville,Urgence,Nb RDV,En attente\n");

            // Lignes — on exporte la liste filtrée visible dans le tableau
            ObservableList<Object[]> data = tableView.getItems();
            for (Object[] row : data) {
                boolean urgence = row[9] != null && (boolean) row[9];
                fw.write(String.format("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s\n",
                        row[0],
                        str(row[1]).replace("\"", "'"),
                        str(row[2]).replace("\"", "'"),
                        str(row[3]).replace("\"", "'"),
                        str(row[4]).replace("\"", "'"),
                        str(row[5]).replace("\"", "'"),
                        urgence ? "Oui" : "Non",
                        row[6],
                        row[7]));
            }

            showSuccess("✅ Export réussi : " + fichier.getName());

        } catch (IOException e) {
            showMessage("❌ Erreur export : " + e.getMessage());
        }
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
            errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12;");
            errorLabel.setText("❌ Erreur navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }
    // ── null-safe String ──
    private String str(Object o) {
        return o != null ? o.toString() : "";
    }
    @FXML
    public void exporterPDF() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("hopitaux_rapport.pdf");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        File file = fc.showSaveDialog((Stage) tableView.getScene().getWindow());
        if (file == null) return;

        try (PdfWriter writer = new PdfWriter(file);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf, PageSize.A4.rotate())) {

            // Titre
            doc.add(new Paragraph("Rapport des Hôpitaux — " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(16).setBold());

            // Stats
            doc.add(new Paragraph("Total : " + masterList.size() + " hôpitaux  |  " +
                    "Avec urgence : " + statUrgence.getText())
                    .setFontSize(11).setFontColor(ColorConstants.GRAY));

            // Tableau
            Table table = new Table(new float[]{1,3,3,2,2,1,1,1});
            String[] headers = {"ID","Nom","Adresse","Téléphone","Spécialités","Urgence","RDV","Attente"};
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setBold())
                        .setBackgroundColor(new DeviceRgb(248, 187, 208)));
            }
            for (Object[] row : tableView.getItems()) {
                boolean urgence = row[9] != null && (boolean) row[9];
                table.addCell(String.valueOf(row[0]));
                table.addCell(str(row[1]));
                table.addCell(str(row[2]));
                table.addCell(str(row[3]));
                table.addCell(str(row[4]));
                table.addCell(urgence ? "✔" : "✖");
                table.addCell(String.valueOf(row[6]));
                table.addCell(String.valueOf(row[7]));
            }
            doc.add(table);
            showSuccess("✅ PDF généré : " + file.getName());
        } catch (Exception e) {
            showMessage("❌ Erreur PDF : " + e.getMessage());
        }
    }
    @FXML
    public void ouvrirCarte() {
        try {
            URL url = getClass().getResource(
                    "/servicesociaux/frontoffice/carteHopital.fxml");
            if (url == null) {
                showMessage("❌ carteHopital.fxml introuvable");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load(), 1000, 700));
            stage.setTitle("🗺️ Carte des Hôpitaux");
            stage.show();
        } catch (Exception e) {
            showMessage("❌ Erreur ouverture carte : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML

    public void calculerItineraire() {
        Object[] selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("⚠️ Sélectionne d'abord un hôpital dans le tableau.");
            return;
        }

        // Récupérer lat/lng depuis la DB via l'id de l'hôpital sélectionné
        int hopId = (int) selected[0];
        double[] coords;
        try {
            coords = service.getCoordonnees(hopId);
        } catch (Exception e) {
            showMessage("❌ Erreur récupération coordonnées : " + e.getMessage());
            return;
        }

        if (coords == null || (coords[0] == 0 && coords[1] == 0)) {
            showMessage("⚠️ Cet hôpital n'a pas de coordonnées GPS en base.");
            return;
        }

        double latHop = coords[0];
        double lngHop = coords[1];

        // Dialog position utilisateur
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Calculer l'itinéraire");
        dialog.setHeaderText("Hôpital : " + str(selected[1]) + "\nEntrez votre position de départ :");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(15));

        TextField latField = new TextField("36.8065");
        TextField lngField = new TextField("10.1815");
        latField.setPromptText("ex: 36.8065");
        lngField.setPromptText("ex: 10.1815");

        grid.add(new Label("Latitude départ :"),  0, 0);
        grid.add(latField, 1, 0);
        grid.add(new Label("Longitude départ :"), 0, 1);
        grid.add(lngField, 1, 1);
        grid.add(new Label("(Tunis = 36.8065, 10.1815)"), 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;
            try {
                double latDep = Double.parseDouble(latField.getText().replace(",", ".").trim());
                double lngDep = Double.parseDouble(lngField.getText().replace(",", ".").trim());

                showSuccess("⏳ Calcul en cours...");

                new Thread(() -> {
                    try {
                        String resultat = service.calculerItineraire(latDep, lngDep, latHop, lngHop);
                        javafx.application.Platform.runLater(() -> showSuccess(resultat));
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() ->
                                showMessage("❌ Erreur itinéraire : " + e.getMessage()));
                    }
                }).start();

            } catch (NumberFormatException e) {
                showMessage("❌ Coordonnées invalides — utilisez des nombres ex: 36.8065");
            }
        });
    }
}
