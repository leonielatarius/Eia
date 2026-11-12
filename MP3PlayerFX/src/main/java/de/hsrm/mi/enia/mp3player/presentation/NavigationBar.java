package de.hsrm.mi.enia.mp3player.presentation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Umschalter zwischen den Haupt-Views (Player / Playlist), wie Tabs.
 * Jede Haupt-View hat ihre eigene Instanz, in der ihr eigener Tab aktiv ist.
 */
public class NavigationBar extends HBox {

    public NavigationBar(boolean playerActive, Runnable showPlayer, Runnable showPlaylist) {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(0, 0, 12, 0));
        getChildren().addAll(
                createTab("Player", playerActive, showPlayer),
                createTab("Playlist", !playerActive, showPlaylist));
    }

    private ToggleButton createTab(String text, boolean active, Runnable action) {
        ToggleButton button = new ToggleButton(text);
        button.setSelected(active);
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setOnAction(e -> {
            // die Markierung zeigt immer die View, zu der diese Leiste gehoert
            button.setSelected(active);
            if (!active) {
                action.run();
            }
        });
        return button;
    }
}
