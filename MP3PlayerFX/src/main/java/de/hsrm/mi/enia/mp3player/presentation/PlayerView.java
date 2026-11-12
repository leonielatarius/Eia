package de.hsrm.mi.enia.mp3player.presentation;

import de.hsrm.mi.enia.mp3player.business.MP3Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Haupt-View "Player": Navigation oben, Cover in der Mitte, darunter
 * Fortschritt und Bedienleiste. Fehlermeldungen liegen als eigene Ebene
 * (StackPane) ueber dem Inhalt.
 */
public class PlayerView extends StackPane {

    private MP3Player player;
    private CoverPane coverPane;
    private ProgressPane progressPane;
    private ControlPane controlPane;
    private SnackBar snackBar;

    public PlayerView(MP3Player player, Runnable showPlayer, Runnable showPlaylist) {
        this.player = player;
        coverPane = new CoverPane();
        progressPane = new ProgressPane(player);
        controlPane = new ControlPane(player);

        VBox bottom = new VBox(12, progressPane, controlPane);
        bottom.setPadding(new Insets(16, 0, 0, 0));

        // Ebene 1: der eigentliche Inhalt
        BorderPane content = new BorderPane();
        content.setTop(new NavigationBar(true, showPlayer, showPlaylist));
        content.setCenter(coverPane);
        content.setBottom(bottom);
        content.setPadding(new Insets(8, 16, 16, 16));

        // Ebene 2: Fehlermeldungen
        snackBar = new SnackBar();
        StackPane.setAlignment(snackBar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(snackBar, new Insets(0, 16, 96, 16));

        getChildren().addAll(content, snackBar);
    }

    public void update() {
        coverPane.update(player.getCurrentTrack());
        progressPane.update();
        controlPane.update();
    }

    public void showMessage(String message) {
        snackBar.show(message);
    }
}
