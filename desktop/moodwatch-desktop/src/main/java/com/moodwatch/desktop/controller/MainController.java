package com.moodwatch.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private Label usernameLabel;
    @FXML private StackPane centerPane;

    @FXML
    public void initialize() {
        loadView("search.fxml");
    }

    public void setUsername(String username) {
        usernameLabel.setText(username != null ? username : "");
    }

    @FXML
    private void onNavFilmAra() {
        loadView("search.fxml");
    }

    @FXML
    private void onNavOneriler() {}

    @FXML
    private void onNavFeed() {}

    @FXML
    private void onNavProfil() {}

    @FXML
    private void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/moodwatch/desktop/login.fxml"));
            Scene scene = new Scene(loader.load(), 480, 560);
            scene.getStylesheets().add(getClass().getResource("/com/moodwatch/desktop/style.css").toExternalForm());
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/moodwatch/desktop/" + fxml));
            Node view = loader.load();
            centerPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
