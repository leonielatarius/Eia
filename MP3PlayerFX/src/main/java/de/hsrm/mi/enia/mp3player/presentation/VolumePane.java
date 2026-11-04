package de.hsrm.mi.enia.mp3player.presentation;

import de.hsrm.mi.enia.mp3player.business.MP3Player;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

/**
 * Lautstaerkeregler (0-100 %).
 */
public class VolumePane extends HBox {

    private MP3Player player;
    private Slider slider;

    public VolumePane(MP3Player player) {
        super(4);
        this.player = player;
        setAlignment(Pos.CENTER);

        Label icon = new Label("🔈");
        slider = new Slider(0, 100, player.getVolume() * 100);
        slider.setPrefWidth(100);
        // bei schmalem Fenster darf der Regler schrumpfen
        slider.setMinWidth(40);
        slider.valueProperty().addListener((obs, oldValue, newValue) -> player.volume(newValue.floatValue() / 100f));

        getChildren().addAll(icon, slider);
    }

    public void update() {
        // gleicht mehrere VolumePanes (Player- und Playlist-View) miteinander ab
        if (!slider.isPressed() && !slider.isValueChanging()) {
            slider.setValue(player.getVolume() * 100);
        }
    }
}
