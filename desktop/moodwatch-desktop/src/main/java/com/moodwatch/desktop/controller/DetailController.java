package com.moodwatch.desktop.controller;

import com.moodwatch.desktop.api.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class DetailController {

    @FXML private StackPane backdropPane;
    @FXML private ImageView backdropView;
    @FXML private ImageView posterView;
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private FlowPane genreBox;
    @FXML private Label overviewLabel;
    @FXML private HBox castBox;
    @FXML private Button trailerButton;

    private String trailerUrl;

    @FXML
    public void initialize() {
        backdropView.fitWidthProperty().bind(backdropPane.widthProperty());
    }

    public void setMovie(long tmdbId) {
        Thread.ofVirtual().start(() -> {
            try {
                ApiClient.MovieDetailDto detail = ApiClient.getInstance().getMovieDetail(tmdbId);
                Platform.runLater(() -> populate(detail));
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        });
    }

    private void populate(ApiClient.MovieDetailDto detail) {
        titleLabel.setText(detail.title() != null ? detail.title() : "");
        metaLabel.setText(buildMeta(detail));
        overviewLabel.setText(detail.overview() != null ? detail.overview() : "");

        genreBox.getChildren().clear();
        if (detail.genres() != null) {
            for (String genre : detail.genres()) {
                Label chip = new Label(genre);
                chip.getStyleClass().add("genre-chip");
                genreBox.getChildren().add(chip);
            }
        }

        trailerUrl = detail.trailerUrl();
        trailerButton.setVisible(trailerUrl != null);
        trailerButton.setManaged(trailerUrl != null);

        if (detail.cast() != null) {
            populateCast(detail.cast());
        }

        if (detail.backdropUrl() != null) {
            Thread.ofVirtual().start(() -> {
                try {
                    Image img = new Image(detail.backdropUrl(), true);
                    Platform.runLater(() -> {
                        if (!img.isError()) backdropView.setImage(img);
                    });
                } catch (Exception ignored) {}
            });
        }

        if (detail.posterUrl() != null) {
            Thread.ofVirtual().start(() -> {
                try {
                    Image img = new Image(detail.posterUrl(), 140, 210, false, true);
                    Platform.runLater(() -> {
                        if (!img.isError()) posterView.setImage(img);
                    });
                } catch (Exception ignored) {}
            });
        }
    }

    private String buildMeta(ApiClient.MovieDetailDto detail) {
        StringBuilder sb = new StringBuilder();
        if (detail.rating() != null && detail.rating() > 0) {
            sb.append("⭐ ").append(String.format("%.1f", detail.rating()));
        }
        if (detail.runtime() != null && detail.runtime() > 0) {
            if (sb.length() > 0) sb.append("  •  ");
            sb.append(detail.runtime()).append(" dk");
        }
        if (detail.releaseDate() != null && detail.releaseDate().length() >= 4) {
            if (sb.length() > 0) sb.append("  •  ");
            sb.append(detail.releaseDate(), 0, 4);
        }
        return sb.toString();
    }

    private void populateCast(List<ApiClient.CastMember> cast) {
        castBox.getChildren().clear();
        for (ApiClient.CastMember member : cast) {
            castBox.getChildren().add(buildCastCard(member));
        }
    }

    private VBox buildCastCard(ApiClient.CastMember member) {
        Rectangle placeholder = new Rectangle(70, 70);
        placeholder.setArcWidth(35);
        placeholder.setArcHeight(35);
        placeholder.setFill(Color.web("#2a2d3e"));

        ImageView profileView = new ImageView();
        profileView.setFitWidth(70);
        profileView.setFitHeight(70);
        profileView.setPreserveRatio(false);

        Rectangle clip = new Rectangle(70, 70);
        clip.setArcWidth(35);
        clip.setArcHeight(35);
        profileView.setClip(clip);

        StackPane imagePane = new StackPane(placeholder, profileView);
        imagePane.setMaxWidth(70);
        imagePane.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(member.name() != null ? member.name() : "");
        nameLabel.getStyleClass().add("cast-name");
        nameLabel.setMaxWidth(84);
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        Label charLabel = new Label(member.character() != null ? member.character() : "");
        charLabel.getStyleClass().add("cast-character");
        charLabel.setMaxWidth(84);
        charLabel.setWrapText(true);
        charLabel.setAlignment(Pos.CENTER);

        VBox card = new VBox(6, imagePane, nameLabel, charLabel);
        card.getStyleClass().add("cast-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(90);

        if (member.profileUrl() != null) {
            Thread.ofVirtual().start(() -> {
                try {
                    Image img = new Image(member.profileUrl(), 70, 70, false, true);
                    Platform.runLater(() -> {
                        if (!img.isError()) profileView.setImage(img);
                    });
                } catch (Exception ignored) {}
            });
        }

        return card;
    }

    @FXML
    private void onBack() {
        MainController main = MainController.getInstance();
        if (main != null) main.loadView("search.fxml");
    }

    @FXML
    private void onTrailer() {
        if (trailerUrl == null) return;
        try {
            Desktop.getDesktop().browse(new URI(trailerUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
