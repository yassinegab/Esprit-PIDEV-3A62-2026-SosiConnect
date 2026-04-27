package org.example.main;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/user/Login.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        stage.setScene(scene);
        stage.setTitle("SOSI+ Healthcare - Login");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0); // Assure la fermeture de Spring Boot / Tomcat
    }

    public static void main(String[] args) {
        // Démarrer Spring Boot en arrière-plan
        org.springframework.boot.SpringApplication.run(org.example.SosiApplication.class, args);
        // Lancer l'interface JavaFX
        launch(args);
    }
}