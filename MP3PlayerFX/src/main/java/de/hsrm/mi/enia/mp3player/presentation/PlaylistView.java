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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

/**
 * Haupt-View "Playlist": Liste aller Songs, der aktuelle ist hervorgehoben.
 * Ein Klick auf einen Song spielt ihn ab.
 */
public class PlaylistView extends BorderPane {

    private MP3Player player;
    private PlaylistManager playlistManager;
    private Label nameLabel;
    private ListView<Track> listView;
    private ControlPane controlPane;
    // was zuletzt angezeigt wurde, damit die Liste nur bei Aenderungen neu gezeichnet wird
    private Playlist shownPlaylist;
    private int shownIndex = -1;

    public PlaylistView(MP3Player player, PlaylistManager playlistManager) {
        this.player = player;
        this.playlistManager = playlistManager;

        nameLabel = new Label();
        nameLabel.setFont(Font.font(null, FontWeight.BOLD, 18));
        nameLabel.setMinWidth(0);
        Button openButton = new Button("Playlist oeffnen …");
        openButton.setOnAction(e -> openPlaylist());
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, nameLabel, spacer, openButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 12, 0));

        listView = new ListView<>();
        listView.setCellFactory(list -> new TrackCell());
        listView.setOnMouseClicked(e -> playSelected());
        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                playSelected();
            }
        });

        controlPane = new ControlPane(player);
        controlPane.setPadding(new Insets(12, 0, 0, 0));

        setTop(header);
        setCenter(listView);
        setBottom(controlPane);
        setPadding(new Insets(16));
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

    private void playSelected() {
        int index = listView.getSelectionModel().getSelectedIndex();
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
