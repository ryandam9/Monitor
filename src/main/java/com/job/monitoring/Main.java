package com.job.monitoring;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

import static com.job.monitoring.utils.AppLogging.logger;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        logger.debug("Job Monitor application is getting started!");

        URL url = new File("resources/ui/login.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);
        Scene scene = new Scene(root, 1200, 800);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Job Monitor");
        stage.getIcons().add(new Image(new File("resources/images/circle.png").toURI().toURL().toString()));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}