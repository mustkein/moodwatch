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

    private static MainController instance;

    @FXML private Label usernameLabel;
    @FXML private Label navAvatarLabel;
    @FXML private StackPane centerPane;

    private String lastView = "search.fxml";

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
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

    public void loadDetail(long tmdbId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/moodwatch/desktop/detail.fxml"));
            Node view = loader.load();
            DetailController ctrl = loader.getController();
            ctrl.setMovie(tmdbId);
            centerPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goBack() {
        loadView(lastView);
    }

    void loadView(String fxml) {
        lastView = fxml;
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
