package org.example.wellbeing.backoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * Standard Admin Controller Template for Wellbeing Module.
 * This is a boilerplate for colleagues to adapt to their specific services.
 */
public class WellbeingAdminController {

    @FXML private Label lblTotalUsers;
    @FXML private Label lblStat2;
    @FXML private Label lblStat3;
    @FXML private Label lblStat4;

    @FXML private TableView<Object> tblData; // Generic for template
    @FXML private TableColumn<Object, String> colId;
    @FXML private TableColumn<Object, String> colField1;
    @FXML private TableColumn<Object, String> colField2;
    @FXML private TableColumn<Object, String> colDate;
    @FXML private TableColumn<Object, Void> colActions;

    @FXML
    public void initialize() {
        setupTable();
        // Colleagues will add their service calls here
    }

    private void setupTable() {
        // Placeholder setup
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnView = new Button("👁");
            private final Button btnEdit = new Button("✏");
            private final HBox container = new HBox(5, btnView, btnEdit);

            {
                btnView.setStyle("-fx-background-color: transparent; -fx-text-fill: #6366f1; -fx-cursor: hand;");
                btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #10b981; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(container);
            }
        });
    }

    @FXML
    private void handleApplyFilter() {
        // Sorting/Filtering boilerplate
    }
}
