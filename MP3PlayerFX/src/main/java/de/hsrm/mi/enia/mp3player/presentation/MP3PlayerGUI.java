package de.hsrm.mi.enia.mp3player.presentation;

import java.util.ArrayList;
import java.util.List;

import de.hsrm.mi.enia.mp3player.business.MP3Player;
import de.hsrm.mi.enia.mp3player.business.PlaylistManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Hauptfenster fuer PlayerGUI. Erzeugt die beiden Haupt-Views (Player, Playlist)
 * und wechselt zwischen ihnen. Fehlermeldungen liegen als eigene Ebene darueber.
 */
public class MP3PlayerGUI extends Application {

    // hier wird default.m3u gesucht: im Projektordner und im Ordner der Konsolenversion
    private static final String[] PLAYLIST_DIRS = { ".", "../Player" };

    private MP3Player player;
    private PlaylistManager playlistManager;
    private PlayerView playerView;
    private PlaylistView playlistView;
    private SnackBar snackBar;
    private Timeline updateTimeline;
    // Meldungen aus init(), bevor es die SnackBar gibt
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
        playerView = new PlayerView(player);
        playlistView = new PlaylistView(player, playlistManager);

        BorderPane mainPane = new BorderPane();
        NavigationBar navigationBar = new NavigationBar(
                () -> mainPane.setCenter(playerView),
                () -> mainPane.setCenter(playlistView));
        mainPane.setTop(navigationBar);
        mainPane.setCenter(playerView);

        // Ebenen: unten die aktuelle View, oben die Fehlermeldungen
        snackBar = new SnackBar();
        StackPane root = new StackPane(mainPane, snackBar);
        StackPane.setAlignment(snackBar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(snackBar, new Insets(0, 16, 96, 16));

        playlistManager.setErrorHandler(snackBar::show);
        player.setErrorHandler(snackBar::show);
        if (!startupMessages.isEmpty()) {
            snackBar.show(String.join("\n", startupMessages));
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

        Scene scene = new Scene(root, 420, 640);
        primaryStage.setTitle("MP3 Player");
        primaryStage.setMinWidth(340);
        primaryStage.setMinHeight(480);
        primaryStage.setScene(scene);
        primaryStage.show();
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
