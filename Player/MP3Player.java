import java.io.File;
import de.hsrm.mi.eibo.simpleplayer.SimpleAudioPlayer;
import de.hsrm.mi.eibo.simpleplayer.SimpleMinim;

public class MP3Player {
    // varibalen
    private SimpleMinim minim;
    private SimpleAudioPlayer audioPlayer;
    private float currentVolume = 1f;

    // konstruktor
    public MP3Player() {
        this.minim = new SimpleMinim();
    }

    public void play(String fileName) {
        this.audioPlayer = minim.loadMP3File(fileName);
        if (!new File(fileName).isFile()) {
            System.out.println("Datei nicht gefunden");
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
            System.out.println("Kein Song geladen");
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
            System.out.println("Lautstärke muss zwischen 0 und 1 liegen ");
            return;
        }
        currentVolume = value == 0f ? MIN_GAIN_DB : (float) (20 * Math.log10(value));

    }

}