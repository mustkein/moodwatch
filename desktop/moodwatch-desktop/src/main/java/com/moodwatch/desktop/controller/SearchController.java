package com.moodwatch.desktop.controller;

import com.moodwatch.desktop.api.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class SearchController {

    private static final int COLS = 4;
    private static final double POSTER_W = 140;
    private static final double POSTER_H = 210;

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Label emptyLabel;
    @FXML private ScrollPane resultsScroll;
    @FXML private GridPane resultsGrid;

    @FXML
    public void initialize() {
        for (int i = 0; i < COLS; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            resultsGrid.getColumnConstraints().add(cc);
        }

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) onSearch();
        });
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        searchButton.setDisable(true);
        resultsGrid.getChildren().clear();

        Thread.ofVirtual().start(() -> {
            try {
                List<ApiClient.MovieSummary> results = ApiClient.getInstance().searchMovies(query, 0);
                Platform.runLater(() -> {
                    if (results.isEmpty()) {
                        emptyLabel.setText("Sonuç bulunamadı.");
                        emptyLabel.setVisible(true);
                        emptyLabel.setManaged(true);
                        resultsScroll.setVisible(false);
                        resultsScroll.setManaged(false);
                    } else {
                        emptyLabel.setVisible(false);
                        emptyLabel.setManaged(false);
                        resultsScroll.setVisible(true);
                        resultsScroll.setManaged(true);
                        for (int i = 0; i < results.size(); i++) {
                            VBox card = buildCard(results.get(i));
                            resultsGrid.add(card, i % COLS, i / COLS);
                        }
                    }
                    searchButton.setDisable(false);
                });
            } catch (RuntimeException e) {
                e.printStackTrace();
                Platform.runLater(() -> searchButton.setDisable(false));
            }
        });
    }

    private VBox buildCard(ApiClient.MovieSummary item) {
        Rectangle placeholder = new Rectangle(POSTER_W, POSTER_H);
        placeholder.setArcWidth(10);
        placeholder.setArcHeight(10);
        placeholder.setFill(Color.web("#2a2d3e"));

        ImageView posterView = new ImageView();
        posterView.setFitWidth(POSTER_W);
        posterView.setFitHeight(POSTER_H);
        posterView.setPreserveRatio(false);

        Rectangle clip = new Rectangle(POSTER_W, POSTER_H);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        posterView.setClip(clip);

        StackPane posterPane = new StackPane(placeholder, posterView);
        posterPane.setMaxWidth(POSTER_W);

        Label titleLabel = new Label(item.title());
        titleLabel.getStyleClass().add("movie-card-title");
        titleLabel.setMaxWidth(POSTER_W);
        titleLabel.setWrapText(true);

        Label yearLabel = new Label(item.year() != null ? String.valueOf(item.year()) : "");
        yearLabel.getStyleClass().add("movie-card-meta");

        Label ratingLabel = new Label(item.rating() != null ? "★ " + String.format("%.1f", item.rating()) : "");
        ratingLabel.getStyleClass().add("movie-card-rating");

        HBox meta = new HBox(8, yearLabel, ratingLabel);
        meta.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, posterPane, titleLabel, meta);
        card.getStyleClass().add("movie-card");
        card.setPadding(new Insets(10));
        card.setMaxWidth(Double.MAX_VALUE);

        if (item.posterUrl() != null) {
            Thread.ofVirtual().start(() -> {
                try {
                    Image img = new Image(item.posterUrl(), POSTER_W, POSTER_H, false, true);
                    Platform.runLater(() -> {
                        if (!img.isError()) {
                            posterView.setImage(img);
                            placeholder.setVisible(false);
                        }
                    });
                } catch (Exception ex) {
                    // placeholder stays
                }
            });
        }

        return card;
    }
}
