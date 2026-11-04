package de.hsrm.mi.enia.mp3player.presentation;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Fehlermeldungen als eingeblendete Ebene (Material-Design "Snackbar")
 * statt als Popup-Fenster. Verschwindet nach einigen Sekunden von selbst.
 */
public class SnackBar extends Label {

    private PauseTransition hideTimer = new PauseTransition(Duration.seconds(4));

    public SnackBar() {
        setWrapText(true);
        setMaxWidth(360);
        setPadding(new Insets(12, 16, 12, 16));
        setStyle("-fx-background-color: #323232; -fx-background-radius: 4; -fx-text-fill: white;");
        // Klicks gehen durch die Meldung hindurch an die View darunter
        setMouseTransparent(true);
        setVisible(false);
        hideTimer.setOnFinished(e -> setVisible(false));
    }

    public void show(String message) {
        setText(message);
        setVisible(true);
        hideTimer.playFromStart();
    }
}
