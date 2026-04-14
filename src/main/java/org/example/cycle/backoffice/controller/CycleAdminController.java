package org.example.cycle.backoffice.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.cycle.model.Cycle;
import org.example.cycle.service.CycleService;
import org.example.user.model.User;
import org.example.user.service.ServiceUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CycleAdminController {

    @FXML private Label lblTotalTrackers;
    @FXML private Label lblTotalCycles;
    @FXML private TableView<UserCycleMetric> cycleUsageTable;
    @FXML private TableColumn<UserCycleMetric, Integer> colUserId;
    @FXML private TableColumn<UserCycleMetric, Integer> colCycleCount;
    @FXML private TableColumn<UserCycleMetric, String> colUserName;


    private final CycleService cycleService = new CycleService();
    private final ServiceUser userService = new ServiceUser();

    @FXML
    public void initialize() {

        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colCycleCount.setCellValueFactory(new PropertyValueFactory<>("cycleCount"));

        loadAnalytics();
    }

    private void loadAnalytics() {

        List<Cycle> cycles = cycleService.getAllCycles();

        Map<Integer, Integer> userUsageMap = new HashMap<>();

        for (Cycle c : cycles) {
            int userId = c.getUser_id();
            userUsageMap.put(userId, userUsageMap.getOrDefault(userId, 0) + 1);
        }

        ObservableList<UserCycleMetric> metrics = FXCollections.observableArrayList();

        for (Map.Entry<Integer, Integer> entry : userUsageMap.entrySet()) {

            int userId = entry.getKey();

            // ✅ correct instance call
            User user = userService.getUserById(userId);

            String userName = (user != null) ? user.getNom() : "Unknown";

            metrics.add(new UserCycleMetric(
                    userId,
                    userName,
                    entry.getValue()
            ));
        }

        // ✅ THESE MUST BE INSIDE THE METHOD
        lblTotalTrackers.setText(String.valueOf(userUsageMap.size()));
        lblTotalCycles.setText(String.valueOf(cycles.size()));

        cycleUsageTable.setItems(metrics);
    }

    // Inner class specifically built to map analytical data securely to JavaFX TableView
    public static class UserCycleMetric {

        private final int userId;
        private final String userName;
        private final int cycleCount;

        public UserCycleMetric(int userId, String userName, int cycleCount) {
            this.userId = userId;
            this.userName = userName;
            this.cycleCount = cycleCount;
        }

        public int getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public int getCycleCount() {
            return cycleCount;
        }
    }
}
