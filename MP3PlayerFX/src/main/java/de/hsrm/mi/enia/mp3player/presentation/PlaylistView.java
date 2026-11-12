package de.hsrm.mi.enia.mp3player.presentation;

import java.io.File;
import java.util.List;

import de.hsrm.mi.enia.mp3player.business.MP3Player;
import de.hsrm.mi.enia.mp3player.business.Playlist;
import de.hsrm.mi.enia.mp3player.business.PlaylistManager;
import de.hsrm.mi.enia.mp3player.business.Track;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

/**
 * Haupt-View "Playlist": Liste aller Songs, der aktuelle ist hervorgehoben.
 * Ein Klick auf einen Song spielt ihn ab. Fehlermeldungen liegen als eigene
 * Ebene (StackPane) ueber dem Inhalt.
 */
public class PlaylistView extends StackPane {

    private MP3Player player;
    private PlaylistManager playlistManager;
    private Label nameLabel;
    private ListView<Track> listView;
    private ControlPane controlPane;
    private SnackBar snackBar;
    // was zuletzt angezeigt wurde, damit die Liste nur bei Aenderungen neu gezeichnet wird
    private Playlist shownPlaylist;
    private int shownIndex = -1;

    public PlaylistView(MP3Player player, PlaylistManager playlistManager, Runnable showPlayer,
            Runnable showPlaylist) {
        this.player = player;
        this.playlistManager = playlistManager;

        nameLabel = new Label();
        nameLabel.setFont(Font.font(null, FontWeight.BOLD, 18));
        nameLabel.setMinWidth(0);
        Button openButton = new Button("Playlist oeffnen …");
        openButton.setMinWidth(Button.USE_PREF_SIZE);
        openButton.setOnAction(e -> openPlaylist());
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, nameLabel, spacer, openButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 12, 0));

        listView = new ListView<>();
        listView.setCellFactory(list -> new TrackCell());
        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                playTrack(listView.getSelectionModel().getSelectedIndex());
            }
        });

        controlPane = new ControlPane(player);
        controlPane.setPadding(new Insets(12, 0, 0, 0));

        // Ebene 1: der eigentliche Inhalt
        BorderPane content = new BorderPane();
        content.setTop(new VBox(new NavigationBar(false, showPlayer, showPlaylist), header));
        content.setCenter(listView);
        content.setBottom(controlPane);
        content.setPadding(new Insets(8, 16, 16, 16));

        // Ebene 2: Fehlermeldungen
        snackBar = new SnackBar();
        StackPane.setAlignment(snackBar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(snackBar, new Insets(0, 16, 64, 16));

        getChildren().addAll(content, snackBar);
    }

    public void update() {
        Playlist playlist = player.getPlaylist();
        if (playlist != shownPlaylist) {
            shownPlaylist = playlist;
            listView.getItems().setAll(playlist == null ? List.of() : playlist.getTracks());
            nameLabel.setText(playlist == null ? "Keine Playlist" : playlist.getName());
        }
        // Liste neu zeichnen, wenn ein anderer Song laeuft (Hervorhebung wandert mit)
        if (player.getCurrentIndex() != shownIndex) {
            shownIndex = player.getCurrentIndex();
            listView.refresh();
        }
        controlPane.update();
    }

    public void showMessage(String message) {
        snackBar.show(message);
    }

    private void playTrack(int index) {
        if (index >= 0) {
            player.playTrack(index);
        }
    }

    private void openPlaylist() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Playlist oeffnen");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("M3U-Playlist", "*.m3u"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            player.setPlaylist(playlistManager.getPlaylist(file.getPath()));
        }
    }

    /** Eine Zeile der Liste: Titel, Interpret und Laenge; der aktuelle Song fett mit ▶. */
    private class TrackCell extends ListCell<Track> {

        TrackCell() {
            // verhindert horizontales Scrollen bei langen Titeln, sie werden stattdessen gekuerzt
            setPrefWidth(0);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            // nur ein Klick auf einen Song spielt ihn ab (nicht auf Scrollbar oder leere Flaeche)
            setOnMouseClicked(e -> {
                if (!isEmpty() && e.getButton() == MouseButton.PRIMARY) {
                    playTrack(getIndex());
                }
            });
        }

        @Override
        protected void updateItem(Track track, boolean empty) {
            super.updateItem(track, empty);
            if (empty || track == null) {
                setGraphic(null);
                return;
            }
            boolean current = getIndex() == player.getCurrentIndex();

            Label title = new Label((current ? "▶  " : "") + track.getTitle());
            title.setFont(Font.font(null, current ? FontWeight.BOLD : FontWeight.NORMAL, 14));
            Label artist = new Label(track.getArtist().isEmpty() ? "Unbekannter Interpret" : track.getArtist());
            artist.setStyle("-fx-text-fill: #757575;");
            VBox texts = new VBox(2, title, artist);
            texts.setMinWidth(0);
            HBox.setHgrow(texts, Priority.ALWAYS);

            Label length = new Label(Track.formatTime(track.getLength()));
            length.setMinWidth(Label.USE_PREF_SIZE);

            HBox row = new HBox(8, texts, length);
            row.setAlignment(Pos.CENTER_LEFT);
            setGraphic(row);
        }
    }
}
