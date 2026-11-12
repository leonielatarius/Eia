package de.hsrm.mi.enia.mp3player.presentation;

import java.util.ArrayList;
import java.util.List;

import de.hsrm.mi.enia.mp3player.business.MP3Player;
import de.hsrm.mi.enia.mp3player.business.PlaylistManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX-Applikation. Erzeugt die Businessklassen und die beiden Haupt-Views
 * (Player, Playlist), verwaltet sie zentral und wechselt zwischen ihnen,
 * indem die jeweilige View als Root der Szene gesetzt wird.
 */
public class MP3PlayerGUI extends Application {

    // hier wird default.m3u gesucht. Das Arbeitsverzeichnis haengt davon ab, wie
    // gestartet wird: Gradle startet in MP3PlayerFX/, VS Code im Workspace-Ordner.
    private static final String[] PLAYLIST_DIRS = { ".", "../Player", "Player" };

    private MP3Player player;
    private PlaylistManager playlistManager;
    private PlayerView playerView;
    private PlaylistView playlistView;
    private Scene scene;
    private Timeline updateTimeline;
    // Meldungen aus init(), bevor es eine View gibt, die sie anzeigen kann
    private List<String> startupMessages = new ArrayList<>();

    @Override
    public void init() {
        // Initialisierung der Businessklassen
        playlistManager = new PlaylistManager();
        player = new MP3Player();
        playlistManager.setErrorHandler(startupMessages::add);
        player.setErrorHandler(startupMessages::add);
        player.setPlaylist(playlistManager.getDefaultPlaylist(PLAYLIST_DIRS));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // die Haupt-Views werden hier zentral erzeugt und verwaltet
        playerView = new PlayerView(player, this::showPlayerView, this::showPlaylistView);
        playlistView = new PlaylistView(player, playlistManager, this::showPlayerView, this::showPlaylistView);

        // eine der Haupt-Views ist die Root der Szene
        scene = new Scene(playerView, 420, 640);

        playlistManager.setErrorHandler(this::showMessage);
        player.setErrorHandler(this::showMessage);
        if (!startupMessages.isEmpty()) {
            showMessage(String.join("\n", startupMessages));
        }

        // Anzeige regelmaessig aktualisieren (Position, Songwechsel) und Songende erkennen
        updateTimeline = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            player.update();
            playerView.update();
            playlistView.update();
        }));
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();
        playerView.update();
        playlistView.update();

        primaryStage.setTitle("MP3 Player");
        primaryStage.setMinWidth(340);
        primaryStage.setMinHeight(480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showPlayerView() {
        scene.setRoot(playerView);
    }

    private void showPlaylistView() {
        scene.setRoot(playlistView);
    }

    /** Fehlermeldung in der gerade sichtbaren View als Ebene einblenden. */
    private void showMessage(String message) {
        if (scene.getRoot() == playlistView) {
            playlistView.showMessage(message);
        } else {
            playerView.showMessage(message);
        }
    }

    @Override
    public void stop() {
        // Beenden moeglicher Prozesse - z.B. das Abspielen von Songs
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
        player.close();
    }
}
