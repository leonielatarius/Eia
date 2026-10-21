import java.io.File;

import de.hsrm.mi.eibo.simpleplayer.SimpleAudioPlayer;
import de.hsrm.mi.eibo.simpleplayer.SimpleMinim;

public class MP3Player {

    // untere Grenze fuer die Verstaerkung, entspricht praktisch "stumm"
    private static final float MIN_GAIN_DB = -80f;

    private SimpleMinim minim;
    private SimpleAudioPlayer audioPlayer;
    private float currentVolume = 1f;

    public MP3Player() {
        // true = asynchrones Abspielen, damit die Konsole weiter reagiert
        minim = new SimpleMinim(true);
    }

    public void play(String fileName) {
        // SimpleMinim liefert bei fehlender Datei keinen null-Wert, daher vorher
        // pruefen
        if (!new File(fileName).isFile()) {
            System.out.println("Datei nicht gefunden: " + fileName);
            return;
        }
        if (audioPlayer != null) {
            audioPlayer.pause();
        }
        audioPlayer = minim.loadMP3File(fileName);
        volume(currentVolume);
        audioPlayer.play();
    }

    public void play() {
        if (audioPlayer == null) {
            System.out.println("Kein Song geladen. Bitte zuerst: play <Datei>");
            return;
        }
        audioPlayer.play();
    }

    public void pause() {
        if (audioPlayer != null) {
            audioPlayer.pause();
        }
    }

    /**
     * Setzt die Lautstaerke im Bereich [0, 1].
     * Da setVolume nicht unterstuetzt wird, wird der Wert in dB umgerechnet
     * und ueber setGain gesetzt: gain = 20 * log10(value).
     */
    public void volume(float value) {
        if (value < 0f || value > 1f) {
            System.out.println("Lautstaerke muss zwischen 0 und 1 liegen.");
            return;
        }
        currentVolume = value;
        if (audioPlayer == null) {
            return;
        }
        float gain = value == 0f ? MIN_GAIN_DB : (float) (20 * Math.log10(value));
        audioPlayer.setGain(Math.max(gain, MIN_GAIN_DB));
    }

    public void stop() {
        if (audioPlayer != null) {
            audioPlayer.pause();
        }
        minim.stop();
    }
}
