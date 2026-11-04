package de.hsrm.mi.enia.mp3player.business;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import de.hsrm.mi.eibo.simpleplayer.SimpleAudioPlayer;
import de.hsrm.mi.eibo.simpleplayer.SimpleMinim;

/**
 * Businesslogik des Players: verwaltet die aktuelle Playlist und spielt sie ab.
 * Kennt kein JavaFX, die GUI fragt den Zustand ueber die Getter ab.
 */
public class MP3Player {

    // untere Grenze fuer die Verstaerkung, entspricht praktisch "stumm"
    private static final float MIN_GAIN_DB = -80f;
    // skipBack startet den Song neu, wenn er schon laenger als 3 s laeuft
    private static final int RESTART_THRESHOLD_MS = 3000;
    // so nah am Songende gilt ein gestoppter Song als "zu Ende gespielt"
    private static final int END_TOLERANCE_MS = 1000;

    private SimpleMinim minim;
    private SimpleAudioPlayer audioPlayer;
    private float currentVolume = 0.7f;

    // Zustand fuer die Playlist
    private Playlist playlist;
    // Abspielreihenfolge als Indizes in die Playlist (bei shuffle gemischt)
    private List<Integer> order = new ArrayList<>();
    // Position in order, nicht direkt in der Playlist
    private int orderPosition = 0;
    private boolean shuffle = false;
    private boolean repeat = false;
    // true, solange der aktuelle Track geladen ist und fortgesetzt werden kann
    private boolean trackLoaded = false;
    // true nach play, false nach pause/stop: so unterscheiden wir
    // "vom Benutzer pausiert" von "Song ist zu Ende"
    private boolean playing = false;

    private Consumer<String> errorHandler = System.out::println;

    public MP3Player() {
        // true = asynchrones Abspielen, damit die GUI weiter reagiert
        minim = new SimpleMinim(true);
    }

    public void setErrorHandler(Consumer<String> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setPlaylist(Playlist actPlaylist) {
        stop();
        playlist = actPlaylist;
        orderPosition = 0;
        trackLoaded = false;
        buildOrder();
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    /** Setzt den aktuellen Track fort bzw. startet ihn. */
    public void play() {
        if (trackLoaded && audioPlayer != null) {
            audioPlayer.play();
            playing = true;
            return;
        }
        if (!hasTracks()) {
            return;
        }
        playCurrent();
    }

    /** Spielt den Track mit dem angegebenen Index (ab 0) der Playlist ab. */
    public void playTrack(int playlistIndex) {
        if (!hasTracks()) {
            return;
        }
        if (playlistIndex < 0 || playlistIndex >= playlist.size()) {
            errorHandler.accept("Ungueltiger Track: " + playlistIndex);
            return;
        }
        orderPosition = order.indexOf(playlistIndex);
        playCurrent();
    }

    public void pause() {
        if (audioPlayer != null) {
            audioPlayer.pause();
        }
        playing = false;
    }

    /** Anhalten und zurueck an den Songanfang. */
    public void stop() {
        pause();
        if (audioPlayer != null && trackLoaded) {
            audioPlayer.cue(0);
        }
    }

    public void skip() {
        if (!hasTracks()) {
            return;
        }
        if (orderPosition + 1 < order.size()) {
            orderPosition++;
        } else if (repeat) {
            orderPosition = 0;
        } else {
            errorHandler.accept("Ende der Playlist erreicht");
            return;
        }
        playCurrent();
    }

    public void skipBack() {
        if (!hasTracks()) {
            return;
        }
        // wie bei ueblichen Playern: erst an den Anfang des aktuellen Songs
        if (trackLoaded && getPosition() > RESTART_THRESHOLD_MS) {
            seek(0);
            return;
        }
        if (orderPosition > 0) {
            orderPosition--;
        } else if (repeat) {
            orderPosition = order.size() - 1;
        } else {
            seek(0);
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
        int currentIndex = order.isEmpty() ? 0 : order.get(orderPosition);
        buildOrder();
        if (shuffle) {
            order.remove(Integer.valueOf(currentIndex));
            order.add(0, currentIndex);
            orderPosition = 0;
        } else {
            orderPosition = currentIndex;
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

    public boolean isPlaying() {
        return playing;
    }

    public Track getCurrentTrack() {
        if (playlist == null || playlist.isEmpty()) {
            return null;
        }
        return playlist.getTrack(order.get(orderPosition));
    }

    /** Index des aktuellen Tracks in der Playlist, -1 wenn keiner. */
    public int getCurrentIndex() {
        if (playlist == null || playlist.isEmpty()) {
            return -1;
        }
        return order.get(orderPosition);
    }

    /** Aktuelle Abspielposition in Millisekunden. */
    public int getPosition() {
        return trackLoaded ? audioPlayer.position() : 0;
    }

    /** Laenge des aktuellen Songs in Millisekunden. */
    public int getLength() {
        if (trackLoaded) {
            return audioPlayer.length();
        }
        Track track = getCurrentTrack();
        return track == null ? 0 : (int) track.getLength() * 1000;
    }

    /** Springt an eine Position im Song (Millisekunden). */
    public void seek(int milliseconds) {
        if (trackLoaded) {
            audioPlayer.cue(milliseconds);
        }
    }

    /**
     * Muss regelmaessig aufgerufen werden (z.B. von einer Timeline der GUI).
     * Erkennt das Songende und schaltet automatisch weiter.
     */
    public void update() {
        if (!playing || !trackLoaded || audioPlayer.isPlaying()) {
            return;
        }
        if (getPosition() < audioPlayer.length() - END_TOLERANCE_MS) {
            return;
        }
        if (orderPosition + 1 < order.size() || repeat) {
            skip();
        } else {
            // letzter Song zu Ende: anhalten und auf den Anfang zuruecksetzen
            stop();
        }
    }

    /**
     * Setzt die Lautstaerke im Bereich [0, 1].
     * Da setVolume nicht unterstuetzt wird, wird der Wert in dB umgerechnet
     * und ueber setGain gesetzt: gain = 20 * log10(value).
     */
    public void volume(float value) {
        if (value < 0f || value > 1f) {
            errorHandler.accept("Lautstaerke muss zwischen 0 und 1 liegen");
            return;
        }
        currentVolume = value;
        if (audioPlayer == null) {
            return;
        }
        float gain = value == 0f ? MIN_GAIN_DB : (float) (20 * Math.log10(value));
        audioPlayer.setGain(Math.max(gain, MIN_GAIN_DB));
    }

    public float getVolume() {
        return currentVolume;
    }

    /** Beendet das Abspielen endgueltig, z.B. beim Schliessen des Fensters. */
    public void close() {
        pause();
        minim.stop();
    }

    private void playCurrent() {
        Track track = getCurrentTrack();
        if (!new File(track.getFileName()).isFile()) {
            errorHandler.accept("Datei nicht gefunden: " + track.getFileName());
            return;
        }
        load(track.getFileName());
        trackLoaded = true;
        audioPlayer.play();
        playing = true;
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
            errorHandler.accept("Keine Playlist geladen oder Playlist ist leer");
            return false;
        }
        return true;
    }
}
