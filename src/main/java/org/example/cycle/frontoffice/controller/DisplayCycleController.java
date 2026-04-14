package org.example.cycle.frontoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;
import org.example.home.controller.HomeController;

import java.io.IOException;


public class DisplayCycleController {


    @FXML
    private FlowPane cycleContainer;

    private ObservableList<Cycle> cycles = FXCollections.observableArrayList();
    private HomeController homeController;


    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    private void goToEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/EditCycle.fxml"));
            Parent view = loader.load();

            homeController.setContent(view); // 🔥 magie ici

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        //cycleContainer.setMinWidth(true);
        loadCycles();
    }

    // 🔥 méthode propre de refresh
    public void loadCycles() {

        cycleContainer.getChildren().clear();
        cycles.clear();

        CycleService service = new CycleService();
        cycles.addAll(service.getAllCycles());

        for (Cycle c : cycles) {

            VBox card = new VBox();
            card.setSpacing(10);

// 🔥 IMPORTANT: largeur fixe pour grid
            card.setPrefWidth(250);

            card.getStyleClass().add("cycle-card");

           /*/ card.setStyle("""
                -fx-background-color: white;
                -fx-padding: 15;
                -fx-border-color: #ccc;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
            """);
            card.setMaxWidth(700); // controls card width
            card.setPrefWidth(700);*/

            Label start = new Label("Start Date");
            start.getStyleClass().add("cycle-label-title");

            Label startValue = new Label(c.getDate_debut_m().toString());
            startValue.getStyleClass().add("cycle-label-value");

            Label end = new Label("End Date");
            end.getStyleClass().add("cycle-label-title");

            Label endValue = new Label(c.getDate_fin_m().toString());
            endValue.getStyleClass().add("cycle-label-value");

            Label user = new Label("User ID: " + c.getUser_id());
            user.getStyleClass().add("cycle-label-title");


            // DELETE
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("btn-delete");

            deleteBtn.setOnAction(e -> {
                boolean confirmed = org.example.utils.AlertHelper.showConfirmationAlert(
                        "Confirmation de Suppression", 
                        "Êtes-vous sûr de vouloir supprimer ce cycle ? Tous les symptômes associés pourraient également être supprimés."
                );

                if (confirmed) {
                    new CycleService().deleteCycle(c.getCycle_id());
                    org.example.utils.AlertHelper.showSuccessAlert("Succès", "Le cycle a été supprimé avec succès.");
                    loadCycles(); // 🔥 refresh auto après delete
                }
            });

            // EDIT
            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("btn-edit");

            editBtn.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/cycle/frontoffice/EditCycle.fxml")
                    );

                    Parent view = loader.load();

                    EditCycleController controller = loader.getController();
                    controller.setCycle(c);

                    // 🔥 IMPORTANT : passer HomeController à Edit aussi
                    controller.setHomeController(homeController);

                    // 🔥 NAVIGATION INTERNE (LA BONNE)
                    homeController.setContent(view);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // VIEW SYMPTOMES
            Button symptomesBtn = new Button("Symptômes");
            symptomesBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand;");
            symptomesBtn.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/cycle/frontoffice/display_symptome.fxml")
                    );
                    Parent view = loader.load();
                    DisplaySymptomeController controller = loader.getController();
                    controller.setHomeController(homeController);
                    controller.setCycleId(c.getCycle_id());
                    homeController.setContent(view);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            HBox buttons = new HBox();
            buttons.getStyleClass().add("cycle-buttons");
            buttons.getChildren().addAll(editBtn, deleteBtn, symptomesBtn);
            card.getChildren().addAll(
                    start,
                    startValue,
                    end,
                    endValue,
                    user,
                    buttons
            );

            cycleContainer.getChildren().add(card);
        }
    }

    @FXML
    private void goToAddCycle() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/cycle/frontoffice/CycleClientView.fxml")
            );

            Parent view = loader.load();

            ClientCycleController controller = loader.getController();

            // 🔥 IMPORTANT : garder navigation Home
            controller.setHomeController(homeController);

            homeController.setContent(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAddSymptome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/add_symptome.fxml"));
            Parent view = loader.load();
            org.example.cycle.frontoffice.controller.AddSymptomeController controller = loader.getController();
            controller.setHomeController(homeController);
            if (homeController != null) {
                homeController.setContent(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSymptomes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/display_symptome.fxml"));
            Parent view = loader.load();
            org.example.cycle.frontoffice.controller.DisplaySymptomeController controller = loader.getController();
            controller.setHomeController(homeController);
            if (homeController != null) homeController.setContent(view);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/CycleStatistics.fxml"));
            Parent view = loader.load();
            org.example.cycle.frontoffice.controller.CycleStatisticsController controller = loader.getController();
            controller.setHomeController(homeController);
            if (homeController != null) homeController.setContent(view);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/CycleHistory.fxml"));
            Parent view = loader.load();
            org.example.cycle.frontoffice.controller.CycleHistoryController controller = loader.getController();
            controller.setHomeController(homeController);
            if (homeController != null) homeController.setContent(view);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToCharts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/CycleCharts.fxml"));
            Parent view = loader.load();
            org.example.cycle.frontoffice.controller.CycleChartsController controller = loader.getController();
            controller.setHomeController(homeController);
            if (homeController != null) homeController.setContent(view);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void exportToPdf() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Rapport_Cycles.pdf");

        java.io.File destFile = fileChooser.showSaveDialog(cycleContainer.getScene().getWindow());

        if (destFile != null) {
            try {
                org.example.cycle.service.PdfExportService.exportCyclesToPdf(
                        destFile,
                        new CycleService().getAllCycles(),
                        new org.example.cycle.service.SymptomeService()
                );
                org.example.utils.AlertHelper.showSuccessAlert("Export Réussi", "Le rapport PDF a été sauvegardé avec succès.");
            } catch (Exception e) {
                org.example.utils.AlertHelper.showErrorAlert("Erreur Export", "Échec de l'export: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}