package de.hsrm.mi.enia.mp3player.business;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private String name;
    private List<Track> tracks = new ArrayList<>();

    public Playlist(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public Track getTrack(int index) {
        return tracks.get(index);
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public int size() {
        return tracks.size();
    }

    public boolean isEmpty() {
        return tracks.isEmpty();
    }
}
