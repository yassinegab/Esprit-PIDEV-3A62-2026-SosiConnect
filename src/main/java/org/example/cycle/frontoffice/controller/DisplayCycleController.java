package org.example.cycle.frontoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;

public class DisplayCycleController {

    @FXML
    private VBox cycleContainer;

    private ObservableList<Cycle> cycles = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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
            card.setSpacing(5);

            card.setStyle("""
                -fx-background-color: white;
                -fx-padding: 15;
                -fx-border-color: #ccc;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
            """);

            Label start = new Label("Start: " + c.getDate_debut_m());
            Label end = new Label("End: " + c.getDate_fin_m());
            Label user = new Label("User ID: " + c.getUser_id());

            // DELETE
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");

            deleteBtn.setOnAction(e -> {
                new CycleService().deleteCycle(c.getCycle_id());
                loadCycles(); // 🔥 refresh auto après delete
            });

            // EDIT
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: orange; -fx-text-fill: white;");

            editBtn.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/cycle/frontoffice/EditCycle.fxml")
                    );

                    Parent root = loader.load();

                    EditCycleController controller = loader.getController();
                    controller.setCycle(c);
                    controller.setParentController(this);

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Edit Cycle");
                    stage.show();

                    controller.setStage(stage); // 🔥 important

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            HBox buttons = new HBox(10);
            buttons.getChildren().addAll(editBtn, deleteBtn);

            card.getChildren().addAll(start, end, user, buttons);

            cycleContainer.getChildren().add(card);
        }
    }
}