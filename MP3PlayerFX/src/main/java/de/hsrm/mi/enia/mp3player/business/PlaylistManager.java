package de.hsrm.mi.enia.mp3player.business;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

/**
 * Laedt Playlisten: aus einer M3U-Datei oder durch Scannen eines
 * Verzeichnisses. Die Metadaten der Tracks kommen per mp3agic aus den ID3-Tags.
 */
public class PlaylistManager {

    // Fehlermeldungen gehen an die GUI, ohne dass diese Klasse JavaFX kennt
    private Consumer<String> errorHandler = System.out::println;

    public void setErrorHandler(Consumer<String> errorHandler) {
        this.errorHandler = errorHandler;
    }

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
            errorHandler.accept("Playlist konnte nicht gelesen werden: " + m3uFileName);
            return playlist;
        }

        File baseDir = m3uFile.getAbsoluteFile().getParentFile();
        int missing = 0;
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
            } else {
                missing++;
            }
        }
        if (missing > 0) {
            errorHandler.accept(missing + " Song(s) aus " + m3uFile.getName() + " nicht gefunden");
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

    /**
     * Sucht default.m3u in den angegebenen Verzeichnissen und laedt die erste
     * gefundene. Liefert eine leere Playlist, wenn keine existiert.
     */
    public Playlist getDefaultPlaylist(String... searchDirs) {
        for (String dir : searchDirs) {
            File m3u = new File(dir, "default.m3u");
            if (m3u.isFile()) {
                return getPlaylist(m3u.getPath());
            }
        }
        errorHandler.accept("Keine default.m3u gefunden");
        return new Playlist("Leer");
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
        byte[] cover = null;
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
            // nur ID3v2 kann ein Coverbild enthalten
            if (tag instanceof ID3v2 v2) {
                cover = v2.getAlbumImage();
            }
        } catch (Exception e) {
            // keine lesbaren Tags: Dateiname als Titel verwenden
        }
        return new Track(file.getPath(), title, artist, album, length, cover);
    }
}
