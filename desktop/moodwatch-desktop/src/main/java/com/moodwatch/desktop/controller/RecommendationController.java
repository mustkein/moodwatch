package com.moodwatch.desktop.controller;

import com.moodwatch.desktop.api.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RecommendationController {

    private static final double POSTER_W = 60;
    private static final double POSTER_H = 90;

    @FXML private TextArea moodInput;
    @FXML private Slider minRatingSlider;
    @FXML private Label minRatingLabel;
    @FXML private TextField maxRuntimeField;
    @FXML private Button getRecsButton;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ListView<ApiClient.RecommendationItem> resultsList;
    @FXML private Label errorLabel;

    private List<ApiClient.RecommendationItem> allItems = new ArrayList<>();

    @FXML
    public void initialize() {
        minRatingSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                minRatingLabel.setText(String.format("%.1f", newVal.doubleValue())));
        resultsList.setCellFactory(lv -> new RecCell());
        sortCombo.getItems().addAll(
                "Varsayılan",
                "Puan: Yüksek → Düşük",
                "Puan: Düşük → Yüksek",
                "İsim: A → Z",
                "İsim: Z → A"
        );
        sortCombo.setValue("Varsayılan");
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
        allItems.clear();

        Thread.ofVirtual().start(() -> {
            try {
                List<ApiClient.RecommendationItem> items =
                        ApiClient.getInstance().getRecommendations(mood, minRating, maxRuntime);
                Platform.runLater(() -> {
                    allItems = new ArrayList<>(items);
                    applySort();
                    boolean hasResults = !items.isEmpty();
                    sortCombo.setVisible(hasResults);
                    sortCombo.setManaged(hasResults);
                    getRecsButton.setDisable(false);
                    if (!hasResults) showError("Öneri bulunamadı.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Hata: " + e.getMessage());
                    getRecsButton.setDisable(false);
                });
            }
        });
    }

    @FXML
    private void onSortChanged() {
        applySort();
    }

    private void applySort() {
        if (allItems.isEmpty()) return;
        List<ApiClient.RecommendationItem> sorted = new ArrayList<>(allItems);
        String selected = sortCombo.getValue();
        if ("Puan: Yüksek → Düşük".equals(selected)) {
            sorted.sort(Comparator.comparingDouble((ApiClient.RecommendationItem i) ->
                    i.rating() != null ? i.rating() : 0.0).reversed());
        } else if ("Puan: Düşük → Yüksek".equals(selected)) {
            sorted.sort(Comparator.comparingDouble((ApiClient.RecommendationItem i) ->
                    i.rating() != null ? i.rating() : 0.0));
        } else if ("İsim: A → Z".equals(selected)) {
            sorted.sort(Comparator.comparing((ApiClient.RecommendationItem i) ->
                    i.title() != null ? i.title() : ""));
        } else if ("İsim: Z → A".equals(selected)) {
            sorted.sort(Comparator.comparing((ApiClient.RecommendationItem i) ->
                    i.title() != null ? i.title() : "").reversed());
        }
        resultsList.getItems().setAll(sorted);
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

    private class RecCell extends ListCell<ApiClient.RecommendationItem> {

        private final Rectangle placeholder = new Rectangle(POSTER_W, POSTER_H);
        private final ImageView posterView = new ImageView();
        private final StackPane posterPane;
        private final Label titleLabel = new Label();
        private final Label metaLabel = new Label();
        private final Label reasonLabel = new Label();
        private final HBox card;

        private volatile String currentTitle = null;

        RecCell() {
            placeholder.setArcWidth(8);
            placeholder.setArcHeight(8);
            placeholder.setFill(Color.web("#2a2d3e"));

            posterView.setFitWidth(POSTER_W);
            posterView.setFitHeight(POSTER_H);
            posterView.setPreserveRatio(false);
            Rectangle clip = new Rectangle(POSTER_W, POSTER_H);
            clip.setArcWidth(8);
            clip.setArcHeight(8);
            posterView.setClip(clip);

            posterPane = new StackPane(placeholder, posterView);
            posterPane.setMinWidth(POSTER_W);
            posterPane.setMaxWidth(POSTER_W);

            titleLabel.getStyleClass().add("rec-card-title");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);

            metaLabel.getStyleClass().add("rec-card-meta");

            reasonLabel.getStyleClass().add("rec-card-reason");
            reasonLabel.setWrapText(true);
            reasonLabel.setMaxWidth(Double.MAX_VALUE);

            VBox info = new VBox(6, titleLabel, metaLabel, reasonLabel);
            info.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(info, Priority.ALWAYS);

            card = new HBox(12, posterPane, info);
            card.getStyleClass().add("movie-cell");
            card.setPadding(new Insets(10));
            card.setAlignment(Pos.CENTER_LEFT);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(ApiClient.RecommendationItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                currentTitle = null;
                return;
            }

            titleLabel.setText(item.title());
            String rating = item.rating() != null ? "★ " + String.format("%.1f", item.rating()) : "";
            String year = item.year() != null ? "  •  " + item.year() : "";
            metaLabel.setText(rating + year);
            reasonLabel.setText(item.reason() != null ? item.reason() : "");

            posterView.setImage(null);
            placeholder.setVisible(true);
            currentTitle = item.title();

            if (item.title() != null) {
                String snap = item.title();
                Thread.ofVirtual().start(() -> {
                    try {
                        List<ApiClient.MovieSummary> results =
                                ApiClient.getInstance().searchMovies(snap, 0);
                        if (!snap.equals(currentTitle)) return;
                        if (results.isEmpty() || results.get(0).posterUrl() == null) return;
                        Image img = new Image(results.get(0).posterUrl(), POSTER_W, POSTER_H, false, true);
                        if (!snap.equals(currentTitle) || img.isError()) return;
                        Platform.runLater(() -> {
                            if (!snap.equals(currentTitle)) return;
                            posterView.setImage(img);
                            placeholder.setVisible(false);
                        });
                    } catch (Exception ignored) {}
                });
            }

            setGraphic(card);
        }
    }
}
