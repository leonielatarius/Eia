package de.hsrm.mi.enia.mp3player.presentation;

import de.hsrm.mi.enia.mp3player.business.MP3Player;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Haupt-View "Player": Cover in der Mitte, darunter Fortschritt und Bedienleiste.
 */
public class PlayerView extends BorderPane {

    private MP3Player player;
    private CoverPane coverPane;
    private ProgressPane progressPane;
    private ControlPane controlPane;

    public PlayerView(MP3Player player) {
        this.player = player;
        coverPane = new CoverPane();
        progressPane = new ProgressPane(player);
        controlPane = new ControlPane(player);

        VBox bottom = new VBox(12, progressPane, controlPane);
        bottom.setPadding(new Insets(16, 0, 0, 0));

        setCenter(coverPane);
        setBottom(bottom);
        setPadding(new Insets(16));
    }

    public void update() {
        coverPane.update(player.getCurrentTrack());
        progressPane.update();
        controlPane.update();
    }
}
