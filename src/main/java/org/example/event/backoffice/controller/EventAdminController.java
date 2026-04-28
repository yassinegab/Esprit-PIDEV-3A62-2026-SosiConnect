package org.example.event.backoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.event.model.Event;
import org.example.event.service.EventService;
import org.example.utils.AlertHelper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class EventAdminController {

    @FXML
    private TextField txtTitle;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private DatePicker dpDate;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtLocalisation;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnClear;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Event> eventTable;
    @FXML
    private TableColumn<Event, String> colTitle;
    @FXML
    private TableColumn<Event, String> colType;
    @FXML
    private TableColumn<Event, LocalDate> colDate;
    @FXML
    private TableColumn<Event, String> colDescription;
    @FXML
    private TableColumn<Event, String> colLocalisation;

    private final EventService eventService = new EventService();
    private ObservableList<Event> eventList = FXCollections.observableArrayList();
    private Event selectedEvent = null;

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList(
                "Campagne de sensibilisation",
                "Recommandation saisonnière",
                "autre"
        ));

        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));

        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEvent = newSelection;
                txtTitle.setText(selectedEvent.getTitle());
                cbType.setValue(selectedEvent.getType());
                dpDate.setValue(selectedEvent.getDate());
                txtDescription.setText(selectedEvent.getDescription());
                txtLocalisation.setText(selectedEvent.getLocalisation() != null ? selectedEvent.getLocalisation() : "");
                btnDelete.setDisable(false);
            }
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> filterEvents(newValue));

        loadEvents();
    }

    private void loadEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            eventList.setAll(events);
            eventTable.setItems(eventList);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Erreur", "Impossible de charger les événements.");
        }
    }

    @FXML
    private void saveEvent() {
        try {
            Event event = new Event(
                    txtTitle.getText(),
                    txtDescription.getText(),
                    dpDate.getValue(),
                    cbType.getValue(),
                    txtLocalisation.getText()
            );

            if (selectedEvent == null) {
                eventService.addEvent(event);
                AlertHelper.showSuccessAlert("Succès", "Événement ajouté avec succès.");
            } else {
                event.setId(selectedEvent.getId());
                eventService.updateEvent(event);
                AlertHelper.showSuccessAlert("Succès", "Événement modifié avec succès.");
            }

            clearForm();
            loadEvents();
        } catch (IllegalArgumentException e) {
            AlertHelper.showErrorAlert("Erreur de validation", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Erreur Base de Données", "Impossible d'enregistrer l'événement.");
        }
    }

    @FXML
    private void deleteEvent() {
        if (selectedEvent != null) {
            boolean confirmed = AlertHelper.showConfirmationAlert("Confirmation", "Êtes-vous sûr de vouloir supprimer cet événement ?");
            if (confirmed) {
                try {
                    eventService.deleteEvent(selectedEvent.getId());
                    AlertHelper.showSuccessAlert("Succès", "Événement supprimé avec succès.");
                    clearForm();
                    loadEvents();
                } catch (SQLException e) {
                    e.printStackTrace();
                    AlertHelper.showErrorAlert("Erreur", "Impossible de supprimer l'événement.");
                }
            }
        }
    }

    @FXML
    private void clearForm() {
        selectedEvent = null;
        txtTitle.clear();
        cbType.setValue(null);
        dpDate.setValue(null);
        txtDescription.clear();
        txtLocalisation.clear();
        btnDelete.setDisable(true);
        eventTable.getSelectionModel().clearSelection();
    }

    private void filterEvents(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            eventTable.setItems(eventList);
            return;
        }
        
        String lowerCaseFilter = keyword.toLowerCase();
        ObservableList<Event> filteredList = FXCollections.observableArrayList();
        
        for (Event event : eventList) {
            if (event.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                event.getType().toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(event);
            }
        }
        eventTable.setItems(filteredList);
    }

    @FXML
    private void sortByDate() {
        FXCollections.sort(eventList, Comparator.comparing(Event::getDate));
        eventTable.setItems(eventList);
    }
}
