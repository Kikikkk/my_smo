package com.example;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
    public void start(Stage primaryStage) throws Exception {
        Scene root = FXMLLoader.load(getClass().getResource("main_page.fxml"));
       // primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.setTitle("Queueing system");
        primaryStage.setScene(root);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
