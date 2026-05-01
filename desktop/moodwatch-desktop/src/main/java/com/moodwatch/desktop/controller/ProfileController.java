package com.moodwatch.desktop.controller;

import com.moodwatch.desktop.api.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileController {

    @FXML private Label avatarInitial;
    @FXML private Label profileUsername;

    @FXML
    public void initialize() {
        String username = ApiClient.getInstance().getUsername();
        if (username != null && !username.isEmpty()) {
            avatarInitial.setText(String.valueOf(Character.toUpperCase(username.charAt(0))));
            profileUsername.setText(username);
        }
    }
}
