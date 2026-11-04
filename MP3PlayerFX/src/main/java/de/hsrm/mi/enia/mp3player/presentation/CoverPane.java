package de.hsrm.mi.enia.mp3player.presentation;

import java.io.ByteArrayInputStream;

import de.hsrm.mi.enia.mp3player.business.Track;
import eia.ui.ImageViewPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Coverbild mit Titel und Interpret des aktuellen Songs.
 * Das Bild waechst ueber die ImageViewPane mit der Fenstergroesse mit.
 */
public class CoverPane extends BorderPane {

    private ImageView coverView;
    private ImageViewPane coverViewPane;
    // wird angezeigt, wenn der Song kein Coverbild hat
    private Label placeholder;
    private Label titleLabel;
    private Label artistLabel;
    private Track shownTrack;

    public CoverPane() {
        coverView = new ImageView();
        coverView.setPreserveRatio(true);
        coverView.setSmooth(true);
        // ImageView skaliert nicht von selbst mit, daher in die ImageViewPane einbetten
        coverViewPane = new ImageViewPane(coverView);

        placeholder = new Label("♪");
        placeholder.setFont(Font.font(96));
        placeholder.setStyle("-fx-text-fill: #9e9e9e;");

        StackPane imageArea = new StackPane(placeholder, coverViewPane);
        imageArea.setMinSize(0, 0);

        titleLabel = new Label();
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 18));
        artistLabel = new Label();
        artistLabel.setFont(Font.font(14));

        VBox info = new VBox(4, titleLabel, artistLabel);
        info.setAlignment(Pos.CENTER);
        info.setPadding(new Insets(12, 0, 0, 0));

        setCenter(imageArea);
        setBottom(info);
        show(null);
    }

    /** Zeigt die Daten des Tracks an; macht nichts, wenn er schon angezeigt wird. */
    public void update(Track track) {
        if (track != shownTrack) {
            show(track);
        }
    }

    private void show(Track track) {
        shownTrack = track;
        if (track == null) {
            titleLabel.setText("Kein Song ausgewaehlt");
            artistLabel.setText("");
            coverView.setImage(null);
            placeholder.setVisible(true);
            return;
        }
        titleLabel.setText(track.getTitle());
        artistLabel.setText(track.getArtist().isEmpty() ? "Unbekannter Interpret" : track.getArtist());

        Image cover = null;
        if (track.getCover() != null) {
            cover = new Image(new ByteArrayInputStream(track.getCover()));
            if (cover.isError()) {
                cover = null;
            }
        }
        coverView.setImage(cover);
        placeholder.setVisible(cover == null);
    }
}
