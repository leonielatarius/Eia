public class Track {

    private String title;
    private String artist;
    private String album;
    // Laenge in Sekunden
    private long length;
    private String fileName;

    public Track(String fileName, String title, String artist, String album, long length) {
        this.fileName = fileName;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.length = length;
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

    @Override
    public String toString() {
        String time = String.format("%d:%02d", length / 60, length % 60);
        String text = (artist == null || artist.isEmpty()) ? title : artist + " - " + title;
        if (album != null && !album.isEmpty()) {
            text += " [" + album + "]";
        }
        return text + " (" + time + ")";
    }
}
