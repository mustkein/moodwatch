package com.moodwatch.desktop.controller;

import com.moodwatch.desktop.api.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    private void onLoginClick() {
        errorLabel.setVisible(false);
        loginButton.setDisable(true);

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        Thread.ofVirtual().start(() -> {
            try {
                ApiClient.getInstance().login(username, password);
                Platform.runLater(this::loadMainView);
            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    errorLabel.setText(e.getMessage());
                    errorLabel.setVisible(true);
                    loginButton.setDisable(false);
                });
            }
        });
    }

    private void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/moodwatch/desktop/main.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/com/moodwatch/desktop/style.css").toExternalForm());
            MainController controller = loader.getController();
            controller.setUsername(ApiClient.getInstance().getUsername());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            errorLabel.setText("Failed to load main view");
            errorLabel.setVisible(true);
            loginButton.setDisable(false);
        }
    }
}
