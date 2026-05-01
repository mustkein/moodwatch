package com.moodwatch.desktop.controller;

import com.moodwatch.desktop.api.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.List;

public class RecommendationController {

    @FXML private TextArea moodInput;
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
            private final Label titleLabel = new Label();
            private final Label metaLabel = new Label();
            private final Label reasonLabel = new Label();
            private final Button addBtn = new Button("Listeye ekle");
            private final VBox card = new VBox(8, titleLabel, metaLabel, reasonLabel, addBtn);

            {
                card.getStyleClass().add("rec-card");
                card.setPadding(new Insets(16));
                card.setMaxWidth(Double.MAX_VALUE);

                titleLabel.getStyleClass().add("rec-card-title");
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(Double.MAX_VALUE);
                metaLabel.getStyleClass().add("rec-card-meta");
                reasonLabel.getStyleClass().add("rec-card-reason");
                reasonLabel.setWrapText(true);
                reasonLabel.setMaxWidth(Double.MAX_VALUE);
                addBtn.getStyleClass().add("rec-card-add-btn");

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(ApiClient.RecommendationItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                titleLabel.setText(item.title());
                String rating = item.rating() != null ? "★ " + String.format("%.1f", item.rating()) : "";
                String year = item.year() != null ? "  •  " + item.year() : "";
                metaLabel.setText(rating + year);
                reasonLabel.setText(item.reason() != null ? item.reason() : "");
                setGraphic(card);
            }
        };
    }
}
