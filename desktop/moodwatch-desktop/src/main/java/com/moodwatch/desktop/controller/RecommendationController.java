package com.moodwatch.desktop.controller;

import com.moodwatch.desktop.api.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.util.List;

public class RecommendationController {

    @FXML private TextField moodInput;
    @FXML private Slider minRatingSlider;
    @FXML private Label minRatingLabel;
    @FXML private TextField maxRuntimeField;
    @FXML private Button getRecsButton;
    @FXML private ListView<ApiClient.RecommendationItem> resultsList;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        minRatingSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                minRatingLabel.setText(String.format("%.1f", newVal.doubleValue())));
        resultsList.setCellFactory(buildCellFactory());
    }

    @FXML
    private void onGetRecommendations() {
        String mood = moodInput.getText().trim();
        if (mood.isEmpty()) {
            showError("Lütfen ruh halinizi yazın.");
            return;
        }
        double minRating = minRatingSlider.getValue();
        Integer maxRuntime = parseRuntime();

        hideError();
        getRecsButton.setDisable(true);
        resultsList.getItems().clear();

        Thread.ofVirtual().start(() -> {
            try {
                List<ApiClient.RecommendationItem> items =
                        ApiClient.getInstance().getRecommendations(mood, minRating, maxRuntime);
                Platform.runLater(() -> {
                    resultsList.getItems().setAll(items);
                    getRecsButton.setDisable(false);
                    if (items.isEmpty()) showError("Öneri bulunamadı.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Hata: " + e.getMessage());
                    getRecsButton.setDisable(false);
                });
            }
        });
    }

    private Integer parseRuntime() {
        String text = maxRuntimeField.getText().trim();
        if (text.isEmpty()) return null;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private Callback<ListView<ApiClient.RecommendationItem>, ListCell<ApiClient.RecommendationItem>> buildCellFactory() {
        return lv -> new ListCell<>() {
            @Override
            protected void updateItem(ApiClient.RecommendationItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String rating = item.rating() != null ? String.format("%.1f", item.rating()) : "N/A";
                    String year = item.year() != null ? " (" + item.year() + ")" : "";
                    setText(item.title() + year + "  ★ " + rating + "\n" + item.reason());
                }
            }
        };
    }
}
