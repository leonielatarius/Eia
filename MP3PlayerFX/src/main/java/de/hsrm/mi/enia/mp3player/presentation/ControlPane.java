package de.hsrm.mi.enia.mp3player.presentation;

import de.hsrm.mi.enia.mp3player.business.MP3Player;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Bedienleiste: Shuffle | Zurueck, Play/Pause, Stop, Weiter | Repeat, Lautstaerke.
 * Wird in der PlayerView und in der PlaylistView wiederverwendet.
 */
public class ControlPane extends HBox {

    private static final String PLAY = "▶";
    private static final String PAUSE = "⏸";

    private MP3Player player;
    private ToggleButton shuffleButton;
    private Button skipBackButton;
    private Button playButton;
    private Button stopButton;
    private Button skipButton;
    private ToggleButton repeatButton;
    private VolumePane volumePane;

    public ControlPane(MP3Player player) {
        super(6);
        this.player = player;
        setAlignment(Pos.CENTER);

        shuffleButton = new ToggleButton("⇄");
        shuffleButton.setTooltip(new Tooltip("Shuffle"));
        shuffleButton.setOnAction(e -> player.shuffle(shuffleButton.isSelected()));

        skipBackButton = new Button("⏮");
        skipBackButton.setTooltip(new Tooltip("Zurueck"));
        skipBackButton.setOnAction(e -> player.skipBack());

        playButton = new Button(PLAY);
        playButton.setTooltip(new Tooltip("Play / Pause"));
        playButton.setOnAction(e -> {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
            update();
        });

        stopButton = new Button("⏹");
        stopButton.setTooltip(new Tooltip("Stop"));
        stopButton.setOnAction(e -> {
            player.stop();
            update();
        });

        skipButton = new Button("⏭");
        skipButton.setTooltip(new Tooltip("Weiter"));
        skipButton.setOnAction(e -> player.skip());

        repeatButton = new ToggleButton("↻");
        repeatButton.setTooltip(new Tooltip("Repeat"));
        repeatButton.setOnAction(e -> player.repeat(repeatButton.isSelected()));

        volumePane = new VolumePane(player);

        // die Abstaende links und rechts wachsen mit, die Knoepfe bleiben in der Mitte
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        getChildren().addAll(shuffleButton, leftSpacer, skipBackButton, playButton, stopButton, skipButton,
                rightSpacer, repeatButton, volumePane);
    }

    public void update() {
        playButton.setText(player.isPlaying() ? PAUSE : PLAY);
        shuffleButton.setSelected(player.isShuffle());
        repeatButton.setSelected(player.isRepeat());
        volumePane.update();
    }
}
