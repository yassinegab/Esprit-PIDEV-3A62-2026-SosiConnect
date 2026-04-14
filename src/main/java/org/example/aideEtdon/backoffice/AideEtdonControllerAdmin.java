package org.example.aideEtdon.backoffice;

<<<<<<< HEAD
public class AideEtdonControllerAdmin {
=======
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.aideEtdon.model.Demande;
import org.example.aideEtdon.model.Video;
import org.example.aideEtdon.service.DemandeService;
import org.example.aideEtdon.service.VideoService;

import java.util.List;

public class AideEtdonControllerAdmin {

    @FXML private VBox adminDemandesContainer;
    @FXML private VBox adminVideosContainer;
    
    @FXML private TextField videoTitleField;
    @FXML private TextField videoUrlField;
    @FXML private Label videoErrorLabel;

    // Master Navigation
    @FXML private Button btnMasterAide;
    @FXML private Button btnMasterDon;
    @FXML private VBox masterAidePane;
    @FXML private VBox masterDonPane;

    // Inner Navigation
    @FXML private Button btnViewAide;
    @FXML private Button btnViewDon;
    @FXML private VBox demandesViewBox;
    @FXML private VBox videosViewBox;

    private DemandeService demandeService;
    private VideoService videoService;

    @FXML
    public void initialize() {
        demandeService = new DemandeService();
        videoService = new VideoService();
        
        // Setup initial default views
        showMasterDon();
        showDemandesView();
    }

    @FXML
    private void showMasterAide() {
        if (!btnMasterAide.getStyleClass().contains("active")) btnMasterAide.getStyleClass().add("active");
        btnMasterDon.getStyleClass().remove("active");
        
        switchView(masterAidePane, masterDonPane);
    }

    @FXML
    private void showMasterDon() {
        if (!btnMasterDon.getStyleClass().contains("active")) btnMasterDon.getStyleClass().add("active");
        btnMasterAide.getStyleClass().remove("active");
        
        switchView(masterDonPane, masterAidePane);
    }

    @FXML
    private void showDemandesView() {
        if (!btnViewAide.getStyleClass().contains("active")) {
            btnViewAide.getStyleClass().add("active");
        }
        btnViewDon.getStyleClass().remove("active");
        
        switchView(demandesViewBox, videosViewBox);
        loadDemandes();
    }

    @FXML
    private void showVideosView() {
        if (!btnViewDon.getStyleClass().contains("active")) {
            btnViewDon.getStyleClass().add("active");
        }
        btnViewAide.getStyleClass().remove("active");
        
        switchView(videosViewBox, demandesViewBox);
        loadVideos();
    }

    private void switchView(VBox show, VBox hide) {
        if (show.isVisible() && !hide.isVisible()) return;

        hide.setVisible(false);
        show.setVisible(true);
        show.setOpacity(0);

        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(450), show);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(450), show);
        tt.setFromY(25);
        tt.setToY(0);

        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, tt);
        pt.play();
    }

    private void loadDemandes() {
        try {
            List<Demande> demandes = demandeService.afficher();
            adminDemandesContainer.getChildren().clear();

            for (Demande d : demandes) {
                HBox row = new HBox(20);
                row.getStyleClass().add("admin-row");
                row.setAlignment(Pos.CENTER_LEFT);

                VBox textBox = new VBox(5);
                Label titleLbl = new Label(d.getTitre());
                titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                Label descLbl = new Label(d.getDescription());
                descLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                descLbl.setMaxWidth(400); 
                textBox.getChildren().addAll(titleLbl, descLbl);

                Label typeLabel = new Label("Type: " + d.getType());
                typeLabel.setStyle("-fx-text-fill: #6366f1; -fx-font-weight: bold;");
                
                Label badge = new Label(d.getUrgence());
                badge.getStyleClass().add("Urgent".equalsIgnoreCase(d.getUrgence()) ? "urgent-badge" : "modern-badge");

                Button btnDel = new Button("Supprimer");
                btnDel.getStyleClass().add("danger-button");
                btnDel.setOnAction(e -> {
                    try {
                        demandeService.supprimer(d.getId());
                        loadDemandes(); 
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                HBox.setHgrow(textBox, Priority.ALWAYS);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                row.getChildren().addAll(textBox, typeLabel, badge, spacer, btnDel);
                adminDemandesContainer.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadVideos() {
        try {
            List<Video> videos = videoService.afficher();
            adminVideosContainer.getChildren().clear();

            for (Video v : videos) {
                HBox row = new HBox(15);
                row.getStyleClass().add("admin-row");
                row.setAlignment(Pos.CENTER_LEFT);

                Label info = new Label(v.getTitle());
                info.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                Label url = new Label(v.getYoutubeUrl());
                url.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 13px;");
                
                VBox infoBox = new VBox(5, info, url);

                Button btnDel = new Button("Supprimer");
                btnDel.getStyleClass().add("danger-button");
                btnDel.setOnAction(e -> {
                    try {
                        videoService.supprimer(v.getId());
                        loadVideos(); 
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                row.getChildren().addAll(infoBox, spacer, btnDel);
                adminVideosContainer.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddVideo() {
        videoErrorLabel.setVisible(false);
        String title = videoTitleField.getText().trim();
        String url = videoUrlField.getText().trim();

        if (title.isEmpty() || url.isEmpty()) {
            videoErrorLabel.setVisible(true);
            return;
        }

        try {
            Video v = new Video(title, url);
            videoService.ajouter(v);
            
            videoTitleField.clear();
            videoUrlField.clear();
            loadVideos(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
>>>>>>> afab8be (Initial commit - aide et don module)
}
