import java.io.File;
import java.util.List;
import java.util.Scanner;

public class KeyboardController {

    private static final String DEFAULT_PLAYLIST = "default.m3u";
    private static final String DEFAULT_MUSIC_DIR = "music";

    private static final String PLAY = "play";
    private static final String PAUSE = "pause";
    private static final String NEXT = "next";
    private static final String PREV = "prev";
    private static final String SHUFFLE = "shuffle";
    private static final String REPEAT = "repeat";
    private static final String LIST = "list";
    private static final String LOAD = "load";
    private static final String SCAN = "scan";
    private static final String VOLUME = "volume";
    private static final String HELP = "help";
    private static final String QUIT = "quit";

    private MP3Player mp3Player;
    private PlaylistManager playlistManager;

    public KeyboardController() {
        mp3Player = new MP3Player();
        playlistManager = new PlaylistManager();
    }

    public void start() {
        loadDefaultPlaylist();
        printHelp();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running && scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            // Befehl und Rest (Dateiname darf Leerzeichen enthalten)
            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String argument = parts.length > 1 ? parts[1] : null;

            switch (command) {
                case PLAY:
                    if (argument == null) {
                        mp3Player.play();
                    } else if (argument.matches("\\d+")) {
                        mp3Player.play(Integer.parseInt(argument));
                    } else {
                        mp3Player.play(argument);
                    }
                    break;
                case PAUSE:
                    mp3Player.pause();
                    break;
                case NEXT:
                    mp3Player.skip();
                    break;
                case PREV:
                    mp3Player.skipBack();
                    break;
                case SHUFFLE:
                    mp3Player.shuffle(parseOnOff(argument, mp3Player.isShuffle()));
                    System.out.println("Shuffle: " + (mp3Player.isShuffle() ? "an" : "aus"));
                    break;
                case REPEAT:
                    mp3Player.repeat(parseOnOff(argument, mp3Player.isRepeat()));
                    System.out.println("Repeat: " + (mp3Player.isRepeat() ? "an" : "aus"));
                    break;
                case LIST:
                    printPlaylist();
                    break;
                case LOAD:
                    if (argument == null) {
                        System.out.println("Bitte M3U-Datei angeben, z.B. load default.m3u");
                    } else {
                        setPlaylist(playlistManager.getPlaylist(argument));
                    }
                    break;
                case SCAN:
                    setPlaylist(playlistManager.getAllTracks(argument == null ? DEFAULT_MUSIC_DIR : argument));
                    break;
                case VOLUME:
                    try {
                        mp3Player.volume(Float.parseFloat(argument));
                    } catch (NumberFormatException | NullPointerException e) {
                        System.out.println("Bitte einen Wert zwischen 0 und 1 angeben, z.B. volume 0.2");
                    }
                    break;
                case HELP:
                    printHelp();
                    break;
                case QUIT:
                    running = false;
                    break;
                default:
                    System.out.println("Unbekannter Befehl: " + command + " (help fuer Hilfe)");
            }
        }

        mp3Player.stop();
        scanner.close();
        System.exit(0);
    }

    /**
     * Laedt default.m3u, falls vorhanden, sonst alle MP3s aus dem Ordner music.
     * Gesucht wird im aktuellen Verzeichnis und in Player/, da VS Code das
     * Programm meist im Workspace-Ordner startet.
     */
    private void loadDefaultPlaylist() {
        for (String baseDir : new String[] { ".", "Player" }) {
            File m3u = new File(baseDir, DEFAULT_PLAYLIST);
            File musicDir = new File(baseDir, DEFAULT_MUSIC_DIR);
            if (m3u.isFile()) {
                setPlaylist(playlistManager.getPlaylist(m3u.getPath()));
                return;
            }
            if (musicDir.isDirectory()) {
                setPlaylist(playlistManager.getAllTracks(musicDir.getPath()));
                return;
            }
        }
        System.out.println("Keine Standard-Playlist gefunden (" + DEFAULT_PLAYLIST
                + " oder Ordner " + DEFAULT_MUSIC_DIR + ").");
    }

    private void setPlaylist(Playlist playlist) {
        mp3Player.setPlaylist(playlist);
        System.out.println("Playlist \"" + playlist.getName() + "\" geladen: "
                + playlist.size() + " Tracks");
    }

    private void printPlaylist() {
        Playlist playlist = mp3Player.getPlaylist();
        if (playlist == null) {
            System.out.println("Keine Playlist geladen.");
            return;
        }
        System.out.println("Playlist: " + playlist.getName());
        Track current = mp3Player.getCurrentTrack();
        List<Track> tracks = playlist.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            String marker = tracks.get(i) == current ? "> " : "  ";
            System.out.println(marker + (i + 1) + ". " + tracks.get(i));
        }
    }

    /** "on"/"off" auswerten, ohne Argument wird umgeschaltet. */
    private boolean parseOnOff(String argument, boolean current) {
        if (argument == null) {
            return !current;
        }
        return argument.equalsIgnoreCase("on") || argument.equalsIgnoreCase("an");
    }

    private void printHelp() {
        System.out.println("Befehle:");
        System.out.println("  play [Nr | Datei]   abspielen / fortsetzen, Track Nr. oder einzelne Datei");
        System.out.println("  pause               pausieren");
        System.out.println("  next | prev         naechster / vorheriger Track");
        System.out.println("  shuffle [on|off]    Zufallswiedergabe");
        System.out.println("  repeat [on|off]     Playlist wiederholen");
        System.out.println("  list                Playlist anzeigen");
        System.out.println("  load <Datei.m3u>    Playlist laden");
        System.out.println("  scan [Ordner]       alle MP3s eines Ordners laden (Standard: music)");
        System.out.println("  volume <0-1>        Lautstaerke");
        System.out.println("  quit                beenden");
    }

    public static void main(String[] args) {
        new KeyboardController().start();
    }
}
