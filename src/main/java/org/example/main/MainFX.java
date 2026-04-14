package org.example.main;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
<<<<<<< HEAD
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/user/Login.fxml"));
=======
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/user/Login.fxml")); ///aideEtdon/frontoffice/AideEtdonClientView.fxml
>>>>>>> afab8be (Initial commit - aide et don module)
        javafx.scene.Parent root = loader.load();
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        stage.setScene(scene);
        stage.setTitle("SOSI+ Healthcare - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}