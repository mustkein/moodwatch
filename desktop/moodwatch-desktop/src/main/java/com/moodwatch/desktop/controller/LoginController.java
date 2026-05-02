package com.moodwatch.desktop.controller;

import com.moodwatch.desktop.api.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Hyperlink toggleLink;
    @FXML private Label toggleHintLabel;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;

    private boolean isRegisterMode = false;

    @FXML
    private void onToggleMode() {
        isRegisterMode = !isRegisterMode;
        errorLabel.setVisible(false);

        emailField.setVisible(isRegisterMode);
        emailField.setManaged(isRegisterMode);
        loginButton.setVisible(!isRegisterMode);
        loginButton.setManaged(!isRegisterMode);
        registerButton.setVisible(isRegisterMode);
        registerButton.setManaged(isRegisterMode);

        if (isRegisterMode) {
            formTitle.setText("Hesap oluştur");
            formSubtitle.setText("MoodWatch'a ücretsiz kayıt ol");
            toggleHintLabel.setText("Zaten hesabın var mı?");
            toggleLink.setText("Giriş Yap");
        } else {
            formTitle.setText("Hoş geldin");
            formSubtitle.setText("MoodWatch'a devam etmek için giriş yap");
            toggleHintLabel.setText("Hesabın yok mu?");
            toggleLink.setText("Kayıt Ol");
        }
    }

    @FXML
    private void onLoginSubmit() {
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

    @FXML
    private void onRegisterSubmit() {
        errorLabel.setVisible(false);
        registerButton.setDisable(true);

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        Thread.ofVirtual().start(() -> {
            try {
                ApiClient.getInstance().register(username, email, password);
                Platform.runLater(this::loadMainView);
            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    errorLabel.setText(e.getMessage());
                    errorLabel.setVisible(true);
                    registerButton.setDisable(false);
                });
            }
        });
    }

    private void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/moodwatch/desktop/main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 750);
            scene.getStylesheets().add(getClass().getResource("/com/moodwatch/desktop/style.css").toExternalForm());
            MainController controller = loader.getController();
            controller.setUsername(ApiClient.getInstance().getUsername());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            errorLabel.setText("Ana ekran yüklenemedi");
            errorLabel.setVisible(true);
            loginButton.setDisable(false);
            registerButton.setDisable(false);
        }
    }
}
