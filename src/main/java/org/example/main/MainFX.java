package org.example.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ⚠️ CHOISIS ICI l’interface que tu veux lancer

        // 👉 OPTION 1 (Login - équipe)
        //javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
               // getClass().getResource("/user/Login.fxml")
        //);

        // 👉 OPTION 2 (ton menu)
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/servicesociaux/frontoffice/mainMenu.fxml")
         );

        Scene scene = new Scene(loader.load(), 880, 680);
        stage.setTitle("SOSI+ Healthcare");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}