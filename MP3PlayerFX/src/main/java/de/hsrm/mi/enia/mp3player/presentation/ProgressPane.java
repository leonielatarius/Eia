package de.hsrm.mi.enia.mp3player.presentation;

import de.hsrm.mi.enia.mp3player.business.MP3Player;
import de.hsrm.mi.enia.mp3player.business.Track;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Aktuelle Position und Laenge des Songs mit einem Slider zum Spulen.
 */
public class ProgressPane extends HBox {

    private MP3Player player;
    private Label positionLabel;
    private Label lengthLabel;
    private Slider slider;

    public ProgressPane(MP3Player player) {
        super(8);
        this.player = player;
        setAlignment(Pos.CENTER);

        positionLabel = new Label("0:00");
        lengthLabel = new Label("0:00");
        slider = new Slider(0, 1, 0);
        slider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(slider, Priority.ALWAYS);

        // erst beim Loslassen spulen, nicht bei jedem Zwischenwert
        slider.setOnMouseReleased(e -> player.seek((int) slider.getValue()));

        getChildren().addAll(positionLabel, slider, lengthLabel);
    }

    public void update() {
        int length = player.getLength();
        slider.setMax(Math.max(length, 1));
        // waehrend der Benutzer zieht, nicht vom Player ueberschreiben lassen
        if (!slider.isPressed() && !slider.isValueChanging()) {
            slider.setValue(player.getPosition());
        }
        positionLabel.setText(Track.formatTime((long) slider.getValue() / 1000));
        lengthLabel.setText(Track.formatTime(length / 1000));
    }
}
