import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hsrm.mi.eibo.simpleplayer.SimpleAudioPlayer;
import de.hsrm.mi.eibo.simpleplayer.SimpleMinim;

public class MP3Player {

    // untere Grenze fuer die Verstaerkung, entspricht praktisch "stumm"
    private static final float MIN_GAIN_DB = -80f;

    private SimpleMinim minim;
    private SimpleAudioPlayer audioPlayer;
    private float currentVolume = 1f;

    // Zustand fuer die Playlist
    private Playlist playlist;
    // Abspielreihenfolge als Indizes in die Playlist (bei shuffle gemischt)
    private List<Integer> order = new ArrayList<>();
    // Position in order, nicht direkt in der Playlist
    private int position = 0;
    private boolean shuffle = false;
    private boolean repeat = false;
    // true, solange der aktuelle Track geladen ist und fortgesetzt werden kann
    private boolean trackLoaded = false;

    public MP3Player() {
        // true = asynchrones Abspielen, damit die Konsole weiter reagiert
        minim = new SimpleMinim(true);
    }

    public void setPlaylist(Playlist actPlaylist) {
        pause();
        playlist = actPlaylist;
        position = 0;
        trackLoaded = false;
        buildOrder();
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    /** Spielt eine einzelne Datei ausserhalb der Playlist ab. */
    public void play(String fileName) {
        // SimpleMinim liefert bei fehlender Datei keinen null-Wert, daher vorher
        // pruefen
        if (!new File(fileName).isFile()) {
            System.out.println("Datei nicht gefunden: " + fileName);
            return;
        }
        load(fileName);
        trackLoaded = true;
        audioPlayer.play();
    }

    /** Spielt den Track mit der angegebenen Nummer (ab 1) der Playlist ab. */
    public void play(int trackNumber) {
        if (!hasTracks()) {
            return;
        }
        if (trackNumber < 1 || trackNumber > playlist.size()) {
            System.out.println("Ungueltige Tracknummer. Erlaubt: 1-" + playlist.size());
            return;
        }
        position = order.indexOf(trackNumber - 1);
        playCurrent();
    }

    /** Setzt den aktuellen Track fort bzw. startet ihn. */
    public void play() {
        if (trackLoaded && audioPlayer != null) {
            audioPlayer.play();
            return;
        }
        if (!hasTracks()) {
            return;
        }
        playCurrent();
    }

    public void pause() {
        if (audioPlayer != null) {
            audioPlayer.pause();
        }
    }

    public void skip() {
        if (!hasTracks()) {
            return;
        }
        if (position + 1 < order.size()) {
            position++;
        } else if (repeat) {
            position = 0;
        } else {
            System.out.println("Ende der Playlist erreicht.");
            return;
        }
        playCurrent();
    }

    public void skipBack() {
        if (!hasTracks()) {
            return;
        }
        if (position > 0) {
            position--;
        } else if (repeat) {
            position = order.size() - 1;
        } else {
            System.out.println("Anfang der Playlist erreicht.");
            return;
        }
        playCurrent();
    }

    public void shuffle(boolean on) {
        shuffle = on;
        if (playlist == null) {
            return;
        }
        // der aktuelle Track bleibt der aktuelle, nur die Reihenfolge danach aendert sich
        int currentIndex = order.isEmpty() ? 0 : order.get(position);
        buildOrder();
        if (shuffle) {
            order.remove(Integer.valueOf(currentIndex));
            order.add(0, currentIndex);
            position = 0;
        } else {
            position = currentIndex;
        }
    }

    public void repeat(boolean on) {
        repeat = on;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public Track getCurrentTrack() {
        if (playlist == null || playlist.isEmpty()) {
            return null;
        }
        return playlist.getTrack(order.get(position));
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

    private void playCurrent() {
        Track track = getCurrentTrack();
        if (!new File(track.getFileName()).isFile()) {
            System.out.println("Datei nicht gefunden: " + track.getFileName());
            return;
        }
        load(track.getFileName());
        trackLoaded = true;
        System.out.println("Spiele: " + track);
        audioPlayer.play();
    }

    private void load(String fileName) {
        if (audioPlayer != null) {
            audioPlayer.pause();
        }
        trackLoaded = false;
        audioPlayer = minim.loadMP3File(fileName);
        volume(currentVolume);
    }

    private void buildOrder() {
        order.clear();
        if (playlist == null) {
            return;
        }
        for (int i = 0; i < playlist.size(); i++) {
            order.add(i);
        }
        if (shuffle) {
            Collections.shuffle(order);
        }
    }

    private boolean hasTracks() {
        if (playlist == null || playlist.isEmpty()) {
            System.out.println("Keine Playlist geladen oder Playlist ist leer.");
            return false;
        }
        return true;
    }
}
