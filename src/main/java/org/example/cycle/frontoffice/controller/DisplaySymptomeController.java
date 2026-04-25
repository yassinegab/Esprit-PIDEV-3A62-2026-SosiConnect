package org.example.cycle.frontoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.cycle.model.Symptome;
import org.example.cycle.service.SymptomeService;
import org.example.home.controller.HomeController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DisplaySymptomeController {

    @FXML
    private FlowPane symptomeContainer;

    @FXML
    private Button btnAddSymptom;

    private ObservableList<Symptome> symptomes = FXCollections.observableArrayList();
    private HomeController homeController;
    private int currentCycleId = -1;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    public void setCycleId(int cycleId) {
        this.currentCycleId = cycleId;
        loadSymptomes();
    }

    @FXML
    public void initialize() {
        if (currentCycleId == -1) {
             loadSymptomes();
        }
    }

    public void loadSymptomes() {
        symptomeContainer.getChildren().clear();
        symptomes.clear();

        SymptomeService service = new SymptomeService();
        try {
            List<Symptome> fetchedSymptomes;
            if (currentCycleId != -1) {
                fetchedSymptomes = service.getSymptomesByCycleId(currentCycleId);
            } else {
                 fetchedSymptomes = service.afficher();
            }
            symptomes.addAll(fetchedSymptomes);

            for (Symptome s : symptomes) {

                VBox card = new VBox();
                card.setSpacing(10);
                card.setPrefWidth(250);

                card.getStyleClass().add("cycle-card"); 

                Label typeLabel = new Label("Type");
                typeLabel.getStyleClass().add("cycle-label-title");

                Label typeValue = new Label(s.getType().name());
                typeValue.getStyleClass().add("cycle-label-value");

                Label intensityLabel = new Label("Intensité");
                intensityLabel.getStyleClass().add("cycle-label-title");

                Label intensityValue = new Label(s.getIntensite().name());
                intensityValue.getStyleClass().add("cycle-label-value");

                Label dateLabel = new Label("Date d'observation");
                dateLabel.getStyleClass().add("cycle-label-title");

                Label dateValue = new Label(s.getDateObservation().toString());
                dateValue.getStyleClass().add("cycle-label-value");


                Button editBtn = new Button("Edit");
                editBtn.getStyleClass().add("btn-edit");
                editBtn.setOnAction(e -> goToEdit(s));


                Button deleteBtn = new Button("Delete");
                deleteBtn.getStyleClass().add("btn-delete");
                deleteBtn.setOnAction(e -> {
                    boolean confirmed = org.example.utils.AlertHelper.showConfirmationAlert(
                            "Confirmation de Suppression", 
                            "Êtes-vous sûr de vouloir supprimer ce symptôme ?"
                    );

                    if (confirmed) {
                        try {
                            service.supprimer(s.getIdSymptome());
                            org.example.utils.AlertHelper.showSuccessAlert("Succès", "Le symptôme a été supprimé avec succès.");
                            loadSymptomes();
                        } catch (SQLException ex) {
                            org.example.utils.AlertHelper.showErrorAlert("Erreur technique", "Échec de la suppression: " + ex.getMessage());
                        }
                    }
                });

                HBox buttons = new HBox();
                buttons.getStyleClass().add("cycle-buttons");
                buttons.getChildren().addAll(editBtn, deleteBtn);

                card.getChildren().addAll(
                        typeLabel, typeValue,
                        intensityLabel, intensityValue,
                        dateLabel, dateValue,
                        buttons
                );

                symptomeContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void goToEdit(Symptome symptome) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/edit_symptome.fxml"));
            Parent view = loader.load();

            EditSymptomeController controller = loader.getController();
            controller.setHomeController(homeController);
            controller.setSymptome(symptome);

            if (homeController != null) {
                homeController.setContent(view);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAddSymptom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cycle/frontoffice/add_symptome.fxml"));
            Parent view = loader.load();

            AddSymptomeController controller = loader.getController();
            controller.setHomeController(homeController);

            if (homeController != null) {
                 homeController.setContent(view);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
