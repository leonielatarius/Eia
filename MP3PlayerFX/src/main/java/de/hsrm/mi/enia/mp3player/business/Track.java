package de.hsrm.mi.enia.mp3player.business;

public class Track {

    private String title;
    private String artist;
    private String album;
    // Laenge in Sekunden
    private long length;
    private String fileName;
    // Coverbild aus dem ID3v2-Tag, null wenn keins vorhanden
    private byte[] cover;

    public Track(String fileName, String title, String artist, String album, long length, byte[] cover) {
        this.fileName = fileName;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.length = length;
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public long getLength() {
        return length;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getCover() {
        return cover;
    }

    /** Formatiert Sekunden als m:ss, z.B. 188 -> 3:08. */
    public static String formatTime(long seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    public String toString() {
        String text = (artist == null || artist.isEmpty()) ? title : artist + " - " + title;
        return text + " (" + formatTime(length) + ")";
    }
}
