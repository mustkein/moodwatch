package com.moodwatch.desktop.controller;

import com.moodwatch.desktop.api.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class WatchlistController {

    private static final double POSTER_W = 60;
    private static final double POSTER_H = 90;
    private static final DateTimeFormatter INPUT_FMT = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter OUTPUT_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("tr-TR"));

    @FXML private ListView<ApiClient.WatchedMovie> watchlistView;
    @FXML private Label emptyLabel;

    @FXML
    public void initialize() {
        watchlistView.setCellFactory(lv -> new WatchedCell());

        System.out.println("[Watchlist] loading, userId=" + ApiClient.getInstance().getUserId());

        Thread.ofVirtual().start(() -> {
            try {
                List<ApiClient.WatchedMovie> movies = ApiClient.getInstance().getWatchedMovies();
                System.out.println("[Watchlist] received " + movies.size() + " movies");
                Platform.runLater(() -> {
                    if (movies.isEmpty()) {
                        emptyLabel.setVisible(true);
                        emptyLabel.setManaged(true);
                        watchlistView.setVisible(false);
                        watchlistView.setManaged(false);
                    } else {
                        watchlistView.getItems().setAll(movies);
                    }
                });
            } catch (RuntimeException e) {
                System.out.println("[Watchlist] error: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    emptyLabel.setText("Liste yüklenemedi");
                    emptyLabel.setVisible(true);
                    emptyLabel.setManaged(true);
                    watchlistView.setVisible(false);
                    watchlistView.setManaged(false);
                });
            }
        });
    }

    private String formatDate(String watchedAt) {
        if (watchedAt == null) return "";
        try {
            return LocalDateTime.parse(watchedAt, INPUT_FMT).format(OUTPUT_FMT);
        } catch (Exception e) {
            return watchedAt;
        }
    }

    private class WatchedCell extends ListCell<ApiClient.WatchedMovie> {

        private final Rectangle placeholder = new Rectangle(POSTER_W, POSTER_H);
        private final ImageView posterView = new ImageView();
        private final StackPane posterPane;
        private final Label titleLabel = new Label();
        private final Label dateLabel = new Label();
        private final Button removeBtn = new Button("Kaldır");
        private final HBox card;

        private volatile String currentTitle = null;

        WatchedCell() {
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

            titleLabel.getStyleClass().add("movie-title");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);

            dateLabel.getStyleClass().add("movie-meta");

            removeBtn.getStyleClass().add("logout-btn");

            VBox info = new VBox(6, titleLabel, dateLabel, removeBtn);
            info.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(info, Priority.ALWAYS);

            card = new HBox(12, posterPane, info);
            card.getStyleClass().add("movie-cell");
            card.setPadding(new Insets(10));
            card.setAlignment(Pos.CENTER_LEFT);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(ApiClient.WatchedMovie item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                currentTitle = null;
                return;
            }

            String title = item.title() != null ? item.title() : String.valueOf(item.tmdbId());
            titleLabel.setText(title);
            dateLabel.setText(formatDate(item.watchedAt()));

            posterView.setImage(null);
            placeholder.setVisible(true);
            currentTitle = title;

            removeBtn.setDisable(false);
            removeBtn.setOnAction(e -> {
                removeBtn.setDisable(true);
                long tmdbId = item.tmdbId();
                Thread.ofVirtual().start(() -> {
                    try {
                        ApiClient.getInstance().removeWatched(tmdbId);
                        SearchController.removeFromWatched(tmdbId);
                        Platform.runLater(() -> getListView().getItems().remove(item));
                    } catch (RuntimeException ex) {
                        Platform.runLater(() -> removeBtn.setDisable(false));
                    }
                });
            });

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
