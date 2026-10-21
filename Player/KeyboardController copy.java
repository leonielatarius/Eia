import java.util.Scanner;

public class KeyboardController {

    private static final String PLAY = "play";
    private static final String PAUSE = "pause";
    private static final String VOLUME = "volume";
    private static final String QUIT = "quit";

    private MP3Player mp3Player;

    public KeyboardController() {
        mp3Player = new MP3Player();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Befehle: play <Datei> | play | pause | volume <0-1> | quit");

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
                    } else {
                        mp3Player.play(argument);
                    }
                    break;
                case PAUSE:
                    mp3Player.pause();
                    break;
                case VOLUME:
                    try {
                        mp3Player.volume(Float.parseFloat(argument));
                    } catch (NumberFormatException | NullPointerException e) {
                        System.out.println("Bitte einen Wert zwischen 0 und 1 angeben, z.B. volume 0.2");
                    }
                    break;
                case QUIT:
                    running = false;
                    break;
                default:
                    System.out.println("Unbekannter Befehl: " + command);
            }
        }

        mp3Player.stop();
        scanner.close();
        System.exit(0);
    }

    public static void main(String[] args) {
        new KeyboardController().start();
    }
}
