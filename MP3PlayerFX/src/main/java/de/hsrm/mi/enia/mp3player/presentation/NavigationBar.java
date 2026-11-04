package de.hsrm.mi.enia.mp3player.presentation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Umschalter zwischen den Haupt-Views (Player / Playlist), wie Tabs.
 */
public class NavigationBar extends HBox {

    public NavigationBar(Runnable showPlayer, Runnable showPlaylist) {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(8, 16, 0, 16));

        ToggleGroup group = new ToggleGroup();
        ToggleButton playerButton = createTab("Player", group, showPlayer);
        ToggleButton playlistButton = createTab("Playlist", group, showPlaylist);
        playerButton.setSelected(true);

        // immer genau ein Tab aktiv: erneutes Klicken darf nichts abwaehlen
        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });

        getChildren().addAll(playerButton, playlistButton);
    }

    private ToggleButton createTab(String text, ToggleGroup group, Runnable action) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(group);
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setOnAction(e -> action.run());
        return button;
    }
}
