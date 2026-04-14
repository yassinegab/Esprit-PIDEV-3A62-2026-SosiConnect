package org.example.aideEtdon.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.aideEtdon.model.Video;
import org.example.aideEtdon.service.VideoService;

import java.io.IOException;
import java.util.List;

public class VideoListController {

    @FXML private FlowPane videosContainer;
    private VideoService videoService;

    @FXML
    public void initialize() {
        videoService = new VideoService();
        loadVideos();
    }

    private void loadVideos() {
        try {
            List<Video> videos = videoService.afficher();
            videosContainer.getChildren().clear();

            for (Video v : videos) {
                VBox card = createVideoCard(v);
                videosContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createVideoCard(Video v) {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(450);
        card.getStyleClass().add("modern-card");
        card.setPadding(new javafx.geometry.Insets(25));

        Label title = new Label(v.getTitle());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        WebView webView = new WebView();
        webView.setPrefSize(400, 225); // 16:9 ratio
        
        // Ensure URL is an embed url
        String url = v.getYoutubeUrl();
        if (url != null && url.contains("watch?v=")) {
            url = url.replace("watch?v=", "embed/");
            // remove trailing parameters if any
            if(url.contains("&")) {
                url = url.substring(0, url.indexOf("&"));
            }
        }
        
        webView.getEngine().load(url);

        card.getChildren().addAll(title, webView);
        return card;
    }

    @FXML
    public void handleRetour() {
        AideEtdonControllerClientController.getInstance().showDons();
    }
}
