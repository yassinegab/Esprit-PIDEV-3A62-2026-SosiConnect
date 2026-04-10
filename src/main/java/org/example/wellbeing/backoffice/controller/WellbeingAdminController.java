package org.example.wellbeing.backoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.wellbeing.model.UserWellBeingData;
import org.example.wellbeing.service.WellbeingService;

import java.sql.SQLException;
import java.util.List;

public class WellbeingAdminController {

    @FXML private Label lblTotalUsers;
    @FXML private Label lblHighAnxiety;
    @FXML private Label lblAvgSleep;
    @FXML private Label lblAvgHeartbeat;

    @FXML private TableView<UserWellBeingData> tblData;
    @FXML private TableColumn<UserWellBeingData, Integer> colId;
    @FXML private TableColumn<UserWellBeingData, Integer> colWork;
    @FXML private TableColumn<UserWellBeingData, Integer> colSleep;
    @FXML private TableColumn<UserWellBeingData, Integer> colHeadaches;
    @FXML private TableColumn<UserWellBeingData, Integer> colRestless;
    @FXML private TableColumn<UserWellBeingData, Integer> colHeart;
    @FXML private TableColumn<UserWellBeingData, Integer> colAcademic;
    @FXML private TableColumn<UserWellBeingData, Integer> colAttendance;
    @FXML private TableColumn<UserWellBeingData, Integer> colAnxiety;
    @FXML private TableColumn<UserWellBeingData, Integer> colIrritable;
    @FXML private TableColumn<UserWellBeingData, Integer> colSubject;
    @FXML private TableColumn<UserWellBeingData, String> colCreated;

    private WellbeingService wellbeingService = new WellbeingService();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colWork.setCellValueFactory(new PropertyValueFactory<>("workEnvironment"));
        colSleep.setCellValueFactory(new PropertyValueFactory<>("sleepProblems"));
        colHeadaches.setCellValueFactory(new PropertyValueFactory<>("headaches"));
        colRestless.setCellValueFactory(new PropertyValueFactory<>("restlessness"));
        colHeart.setCellValueFactory(new PropertyValueFactory<>("heartbeatPalpitations"));
        colAcademic.setCellValueFactory(new PropertyValueFactory<>("lowAcademicConfidence"));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>("classAttendance"));
        colAnxiety.setCellValueFactory(new PropertyValueFactory<>("anxietyTension"));
        colIrritable.setCellValueFactory(new PropertyValueFactory<>("irritability"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subjectConfidence"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }

    private void loadData() {
        try {
            List<UserWellBeingData> dataList = wellbeingService.afficher();
            ObservableList<UserWellBeingData> observableList = FXCollections.observableArrayList(dataList);
            tblData.setItems(observableList);

            updateStats(dataList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStats(List<UserWellBeingData> dataList) {
        long totalUsers = dataList.stream().map(d -> d.getUser().getId()).distinct().count();
        long highAnxiety = dataList.stream().filter(d -> d.getAnxietyTension() >= 4).count();
        double avgSleep = dataList.stream().mapToInt(UserWellBeingData::getSleepProblems).average().orElse(0);
        double avgHeart = dataList.stream().mapToInt(UserWellBeingData::getHeartbeatPalpitations).average().orElse(0);

        lblTotalUsers.setText(String.valueOf(totalUsers));
        lblHighAnxiety.setText(String.valueOf(highAnxiety));
        lblAvgSleep.setText(String.format("%.2f", avgSleep));
        lblAvgHeartbeat.setText(String.format("%.2f", avgHeart));
    }

    @FXML
    private void handleApplyFilter() {
        // To be implemented: Sorting/Filtering logic
    }
}
