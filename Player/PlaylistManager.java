import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.Mp3File;

/**
 * Laedt Playlisten (Variante 2): aus einer M3U-Datei oder durch Scannen
 * eines Verzeichnisses. Die Metadaten der Tracks kommen per mp3agic aus
 * den ID3-Tags.
 */
public class PlaylistManager {

    /**
     * Laedt eine M3U-Datei. Relative Pfade in der Datei werden relativ zum
     * Verzeichnis der M3U-Datei aufgeloest.
     */
    public Playlist getPlaylist(String m3uFileName) {
        File m3uFile = new File(m3uFileName);
        Playlist playlist = new Playlist(m3uFile.getName());
        List<String> lines;
        try {
            lines = Files.readAllLines(m3uFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Playlist konnte nicht gelesen werden: " + m3uFileName);
            return playlist;
        }

        File baseDir = m3uFile.getAbsoluteFile().getParentFile();
        for (String line : lines) {
            line = line.trim();
            // #EXTM3U, #EXTINF usw. sind Kommentare, die Infos lesen wir aus den ID3-Tags
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            File mp3 = new File(line);
            if (!mp3.isAbsolute()) {
                mp3 = new File(baseDir, line);
            }
            Track track = createTrack(mp3);
            if (track != null) {
                playlist.addTrack(track);
            }
        }
        return playlist;
    }

    /**
     * Durchsucht rootDir rekursiv nach MP3-Dateien und liefert sie als Playlist.
     */
    public Playlist getAllTracks(String rootDir) {
        Playlist playlist = new Playlist("Alle Tracks");
        collectTracks(new File(rootDir), playlist);
        return playlist;
    }

    private void collectTracks(File dir, Playlist playlist) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files);
        for (File file : files) {
            if (file.isDirectory()) {
                collectTracks(file, playlist);
            } else if (file.getName().toLowerCase().endsWith(".mp3")) {
                Track track = createTrack(file);
                if (track != null) {
                    playlist.addTrack(track);
                }
            }
        }
    }

    private Track createTrack(File file) {
        if (!file.isFile()) {
            System.out.println("Datei nicht gefunden, wird uebersprungen: " + file.getPath());
            return null;
        }
        String title = file.getName().replaceFirst("(?i)\\.mp3$", "");
        String artist = "";
        String album = "";
        long length = 0;
        try {
            Mp3File mp3 = new Mp3File(file.getPath());
            length = mp3.getLengthInSeconds();
            // ID3v2 bevorzugen, sonst ID3v1 (ID3v2 erweitert ID3v1)
            ID3v1 tag = mp3.hasId3v2Tag() ? mp3.getId3v2Tag() : mp3.getId3v1Tag();
            if (tag != null) {
                if (tag.getTitle() != null && !tag.getTitle().isEmpty()) {
                    title = tag.getTitle();
                }
                artist = tag.getArtist() != null ? tag.getArtist() : "";
                album = tag.getAlbum() != null ? tag.getAlbum() : "";
            }
        } catch (Exception e) {
            // keine lesbaren Tags: Dateiname als Titel verwenden
        }
        return new Track(file.getPath(), title, artist, album, length);
    }
}
