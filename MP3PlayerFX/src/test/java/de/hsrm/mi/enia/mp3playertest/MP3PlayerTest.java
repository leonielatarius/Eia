package de.hsrm.mi.enia.mp3playertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.hsrm.mi.enia.mp3player.business.Playlist;
import de.hsrm.mi.enia.mp3player.business.PlaylistManager;
import de.hsrm.mi.enia.mp3player.business.Track;

/**
 * Tests fuer die Businessklassen, die ohne Audioausgabe auskommen.
 */
class MP3PlayerTest {

    @TempDir
    Path tempDir;

    @Test
    void formatTimeZeigtMinutenUndSekunden() {
        assertEquals("3:08", Track.formatTime(188));
        assertEquals("0:05", Track.formatTime(5));
        assertEquals("10:00", Track.formatTime(600));
    }

    @Test
    void playlistSammeltTracks() {
        Playlist playlist = new Playlist("Test");
        assertTrue(playlist.isEmpty());
        Track track = new Track("a.mp3", "Titel", "Artist", "Album", 100, null);
        playlist.addTrack(track);
        assertEquals(1, playlist.size());
        assertEquals(track, playlist.getTrack(0));
    }

    @Test
    void m3uMitFehlendenDateienLiefertLeerePlaylistUndFehlermeldung() throws IOException {
        Path m3u = tempDir.resolve("test.m3u");
        Files.writeString(m3u, "#EXTM3U\n#EXTINF:221,Meret Becker - Grau\nmusic/grau.mp3\n", StandardCharsets.UTF_8);

        PlaylistManager manager = new PlaylistManager();
        List<String> errors = new ArrayList<>();
        manager.setErrorHandler(errors::add);

        Playlist playlist = manager.getPlaylist(m3u.toString());
        assertEquals("test.m3u", playlist.getName());
        assertTrue(playlist.isEmpty());
        assertEquals(1, errors.size());
    }

    @Test
    void m3uIgnoriertKommentareUndLeerzeilen() throws IOException {
        // eine "MP3" ohne gueltige Tags: der Dateiname wird zum Titel
        Files.createDirectories(tempDir.resolve("music"));
        Files.write(tempDir.resolve("music/song.mp3"), new byte[] { 0, 1, 2 });
        Path m3u = tempDir.resolve("liste.m3u");
        Files.writeString(m3u, "#EXTM3U\n\n#EXTINF:1,x\nmusic/song.mp3\n", StandardCharsets.UTF_8);

        Playlist playlist = new PlaylistManager().getPlaylist(m3u.toString());
        assertEquals(1, playlist.size());
        assertEquals("song", playlist.getTrack(0).getTitle());
    }

    @Test
    void fehlendeDefaultPlaylistLiefertLeerePlaylist() {
        PlaylistManager manager = new PlaylistManager();
        List<String> errors = new ArrayList<>();
        manager.setErrorHandler(errors::add);
        Playlist playlist = manager.getDefaultPlaylist(tempDir.toString());
        assertTrue(playlist.isEmpty());
        assertEquals(1, errors.size());
    }
}
