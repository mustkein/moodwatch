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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class SearchController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ListView<ApiClient.MovieSummary> resultsList;

    @FXML
    public void initialize() {
        resultsList.setCellFactory(lv -> new ListCell<>() {
            private final ImageView posterView = new ImageView();
            private final Rectangle placeholder = new Rectangle(80, 120);
            private final StackPane posterPane;
            private final Label titleLabel = new Label();
            private final Label yearLabel = new Label();
            private final Label ratingLabel = new Label();
            private final HBox layout;

            {
                posterView.setFitWidth(80);
                posterView.setFitHeight(120);
                posterView.setPreserveRatio(true);
                placeholder.setFill(Color.web("#2a3a4a"));
                placeholder.setArcWidth(6);
                placeholder.setArcHeight(6);
                posterPane = new StackPane(placeholder, posterView);

                titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ddeeff; -fx-font-size: 14px;");
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(Double.MAX_VALUE);
                yearLabel.setStyle("-fx-text-fill: #8899aa; -fx-font-size: 12px;");
                ratingLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12px;");

                VBox details = new VBox(6, titleLabel, yearLabel, ratingLabel);
                details.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(details, Priority.ALWAYS);

                layout = new HBox(12, posterPane, details);
                layout.setAlignment(Pos.CENTER_LEFT);
                layout.setPadding(new Insets(8));
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(ApiClient.MovieSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                titleLabel.setText(item.title());
                yearLabel.setText(item.year() != null ? String.valueOf(item.year()) : "");
                ratingLabel.setText(item.rating() != null ? "★ " + String.format("%.1f", item.rating()) : "");

                posterView.setImage(null);
                placeholder.setVisible(true);

                if (item.posterUrl() != null) {
                    Thread.ofVirtual().start(() -> {
                        try {
                            Image img = new Image(item.posterUrl(), 80, 120, true, true);
                            Platform.runLater(() -> {
                                if (item.equals(getItem()) && !img.isError()) {
                                    posterView.setImage(img);
                                    placeholder.setVisible(false);
                                }
                            });
                        } catch (Exception ex) {
                            // placeholder stays visible
                        }
                    });
                }

                setGraphic(layout);
            }
        });

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) onSearch();
        });
    }

    @FXML
    private void onSearch() {
        System.out.println("[Search] onSearch called");
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        searchButton.setDisable(true);
        resultsList.getItems().clear();

        Thread.ofVirtual().start(() -> {
            try {
                List<ApiClient.MovieSummary> results = ApiClient.getInstance().searchMovies(query, 0);
                System.out.println("[Search] results size: " + results.size());
                if (!results.isEmpty()) {
                    System.out.println("[Search] first result: title=" + results.get(0).title()
                            + " rating=" + results.get(0).rating());
                }
                Platform.runLater(() -> {
                    resultsList.getItems().setAll(results);
                    searchButton.setDisable(false);
                });
            } catch (RuntimeException e) {
                e.printStackTrace();
                Platform.runLater(() -> searchButton.setDisable(false));
            }
        });
    }
}
