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
    @FXML private Label navAvatarLabel;
    @FXML private StackPane centerPane;

    @FXML
    public void initialize() {
        loadView("search.fxml");
    }

    public void setUsername(String username) {
        usernameLabel.setText(username != null ? username : "");
        if (username != null && !username.isEmpty()) {
            navAvatarLabel.setText(String.valueOf(Character.toUpperCase(username.charAt(0))));
        }
    }

    @FXML
    private void onNavFilmAra() {
        loadView("search.fxml");
    }

    @FXML
    private void onNavOneriler() {
        loadView("recommendation.fxml");
    }

    @FXML
    private void onNavFeed() {}

    @FXML
    private void onNavWatchlist() {
        loadView("watchlist.fxml");
    }

    @FXML
    private void onNavProfil() {
        loadView("profile.fxml");
    }

    @FXML
    private void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/moodwatch/desktop/login.fxml"));
            Scene scene = new Scene(loader.load(), 840, 560);
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
