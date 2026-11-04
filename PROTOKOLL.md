# Project Log – MP3 Player (Entwicklung interaktiver Anwendungen)

This file documents the project step by step. New explanations are added at the end as new entries.

## Contents

1. [Entry 1 – Übung 2: Player / Businesslogik (2026-10-28)](#entry-1--übung-2-player--businesslogik-2026-10-28)
2. [Entry 2 – The M3U playlist file default.m3u (2026-10-28)](#entry-2--the-m3u-playlist-file-defaultm3u-2026-10-28)
3. [Entry 3 – Übung 3 & 4: JavaFX user interface (2026-11-04)](#entry-3--übung-3--4-javafx-user-interface-2026-11-04)

---

## Entry 1 – Übung 2: Player / Businesslogik (2026-10-28)

### Background
- **Übung 1** built a simple console player: `play <file>`, `pause`, `volume`, `quit` (`MP3Player` + `KeyboardController`).
- **Übung 2** adds data objects (`Track`, `Playlist`) and a `PlaylistManager`. The player can now work with playlists: skip forward and back, shuffle, repeat.
- The library **mp3agic** (`Player/minim_library/mp3agic-0.9.1.jar`) reads the ID3 tags (title, artist, album) from the MP3 files.
- 34 songs are in `Player/music/`. `Player/.gitignore` keeps them out of GitHub.

### 1. Overall structure

The project uses **Variante 2** from the exercise sheet: a separate class loads the playlists, so the player only has to play them.

```
KeyboardController           ← reads what you type in the console
   │ calls                 │ calls
   ▼                       ▼
PlaylistManager          MP3Player              ← the actual player logic
   │ creates               │ plays the current     │ uses
   ▼                       ▼ playlist              ▼
Playlist ◄─────────────────┘                  SimpleAudioPlayer (minim library)
   │ consists of
   ▼
Track
```

Each class has one job:

| Class | Job | Analog version (from the slides) |
|---|---|---|
| `Track` | holds the information about one song | one cut-out song strip |
| `Playlist` | a name plus a list of tracks | the sheet of paper with the strips glued on |
| `PlaylistManager` | builds playlists from files | the person who cuts and glues |
| `MP3Player` | plays, skips, shuffles, repeats | the iPod |
| `KeyboardController` | turns typed text into method calls | your fingers on the buttons |

### 2. The data classes: `Track` and `Playlist`

[Track.java](Player/Track.java) is a simple data object, an **entity**. It stores:
- `title`, `artist`, `album`: read from the MP3's ID3 tags
- `length`: the length in seconds
- `fileName`: the path to the MP3 file, which the player needs to actually play it

It has only getters and a `toString()`, which builds the display text, e.g. `Frank Ocean - Moon River (3:08)`. The `%d:%02d` format turns 188 seconds into `3:08`: the minutes, then the seconds with two digits.

[Playlist.java](Player/Playlist.java) is a name plus an `ArrayList<Track>`. Its methods are `addTrack`, `getTrack(index)`, `size()` and `isEmpty()`. It doesn't do anything clever; it's a container.

### 3. `PlaylistManager`: loading playlists

[PlaylistManager.java](Player/PlaylistManager.java) has two ways to create a `Playlist`.

**a) `getPlaylist(m3uFileName)` reads an M3U file.**
An M3U file is a plain text file:
```
#EXTM3U
#EXTINF:221,Meret Becker - Grau
music/grau.mp3
```
- It reads every line with `Files.readAllLines`.
- Lines starting with `#` are skipped, because they are comments or extra info.
- Every other line is a path to an MP3. If the path is relative, it is resolved against the folder where the `.m3u` file is located (`baseDir`).
- `createTrack` turns each path into a `Track` object.

**b) `getAllTracks(rootDir)` scans a folder (the optional extension on the sheet).**
- `collectTracks` is **recursive**. It lists all files in the folder. A subfolder calls `collectTracks` again for that folder. A file ending in `.mp3` becomes a `Track`.
- `Arrays.sort(files)` keeps the order alphabetical.

**`createTrack(file)` reads the metadata with mp3agic:**
```java
Mp3File mp3 = new Mp3File(file.getPath());
length = mp3.getLengthInSeconds();
ID3v1 tag = mp3.hasId3v2Tag() ? mp3.getId3v2Tag() : mp3.getId3v1Tag();
```
- ID3 tags are information stored inside the MP3 file: title, artist, album and so on. There are two versions. ID3v2 is newer and preferred. Because `ID3v2` extends `ID3v1`, both fit into one `ID3v1` variable (polymorphism).
- **Fallback:** if a song has no tags, which is true for most of the YouTube downloads, the file name without `.mp3` is used as the title.
- If the file doesn't exist, it prints a warning and returns `null`, and that song is skipped.

### 4. `MP3Player`: the player logic

[MP3Player.java](Player/MP3Player.java) is the most important class. The exercise says the player must "manage its state internally", so here are its **fields**:

```java
private SimpleMinim minim;              // audio engine, created once
private SimpleAudioPlayer audioPlayer;  // the currently loaded song
private float currentVolume = 1f;       // remembered so a new song gets the same volume

private Playlist playlist;              // the current playlist
private List<Integer> order;            // playing ORDER (track indexes)
private int position;                   // where we are in "order"
private boolean shuffle, repeat;
private boolean trackLoaded;            // can "play" resume, or must it load a new song?
```

#### The key idea: `order` and `position`
The player doesn't walk through the playlist directly. It walks through a separate **order list** of indexes:

```
Playlist:  [A, B, C, D]     (indexes 0,1,2,3)
shuffle off → order = [0, 1, 2, 3]
shuffle on  → order = [2, 0, 3, 1]   (mixed)
position = 1  → current song = playlist.getTrack(order.get(1))
```
Because of this, shuffle never changes the playlist itself, only the order it is played in. `getCurrentTrack()` always means `playlist.getTrack(order.get(position))`.

#### The methods
- **`SimpleMinim(true)`** in the constructor: `true` turns on asynchronous playback. The music runs in the background, so the console can still accept commands. (Hint on page 11 of the sheet.)
- **`setPlaylist(p)`**: pauses, stores the new playlist, resets `position` to 0 and rebuilds `order`.
- **`play()`**, with no argument: if a song is loaded (`trackLoaded`), it **resumes** from where it paused. Otherwise it starts the current song from the playlist.
- **`play(int trackNumber)`**: the user counts from 1, Java from 0, hence `trackNumber - 1`. `order.indexOf(...)` finds where that song sits in the current order, which matters when shuffle is on.
- **`play(String fileName)`**: plays a single file outside the playlist. This is the method from Übung 1.
- **`skip()` / `skipBack()`**: move `position` forward or back. At the end (or start) of the list: with `repeat` on, it jumps to the other end; without it, it prints a message and stops.
- **`shuffle(on)`**: rebuilds `order`. When turned on, the **current song moves to the front** so the song that is playing doesn't jump. When turned off, `position` goes back to the song's normal index.
- **`volume(value)`**: the library has no working `setVolume`, so the value 0–1 is converted to **decibels** with `gain = 20 · log10(value)`. 1 → 0 dB (no change), 0.5 → about −6 dB, and 0 → −80 dB, which is effectively silent. `log10(0)` would be minus infinity, which is why 0 is handled separately.
- **Private helpers:**
  - `load()` pauses the old song, loads the new one and applies the stored volume.
  - `playCurrent()` loads and plays the current song.
  - `buildOrder()` fills `order` with 0..n-1 and mixes it with `Collections.shuffle` if shuffle is on.
  - `hasTracks()` checks that a playlist is loaded and not empty.

**Known limitation (also stated on the sheet):** when a song ends, the next one does **not** start automatically, because the library can't tell us yet when a song is finished.

### 5. `KeyboardController`: the console

[KeyboardController.java](Player/KeyboardController.java) is the **controller**. It contains no player logic; it only translates input into method calls.

**`start()` runs in this order:**
1. `loadDefaultPlaylist()` looks for `default.m3u` first, then for a `music` folder. It checks both the current folder (`.`) and `Player/`, because VS Code usually starts the program in the `Eia` folder.
2. `printHelp()` prints the list of commands.
3. A loop runs `while (running && scanner.hasNextLine())`:
   - It reads one line and skips it if empty.
   - `line.split("\\s+", 2)` splits it into **at most 2 parts**: the command and everything after it. The limit of 2 matters because file names can contain spaces.
   - A `switch` on the command decides what happens.
4. `quit` sets `running = false`. After the loop ends, it calls `mp3Player.stop()` and `System.exit(0)`.

**Details worth knowing:**
- `play` has three cases. No argument resumes; a number (`argument.matches("\\d+")`, meaning "digits only") plays that track number; anything else is treated as a file name.
- `parseOnOff`: `shuffle on` / `shuffle off` set the value directly, and `shuffle` with nothing after it **toggles** the current state.
- `volume` catches `NumberFormatException` for input like `volume abc` and `NullPointerException` for `volume` with no number.
- `printPlaylist()` marks the current song with `>`.
- The commands are `static final` constants. This avoids typos in the strings and keeps all commands in one place.

#### Console commands

| Input | What happens |
|---|---|
| `list` | show all songs with numbers |
| `play 12` | play song number 12 |
| `play` | start the current song or resume after pause |
| `play <file>` | play a single MP3 file |
| `pause` | pause |
| `next` / `prev` | next / previous song |
| `shuffle [on\|off]` | shuffle on/off (no argument toggles) |
| `repeat [on\|off]` | repeat the playlist on/off |
| `load <file.m3u>` | load an M3U playlist |
| `scan [folder]` | load all MP3s in a folder (default: `music`) |
| `volume 0.5` | volume (0 = silent, 1 = full) |
| `help` | show all commands |
| `quit` | exit |

### 6. A command from start to finish: `play 12`

```
You type "play 12"
 → KeyboardController: split → command="play", argument="12"
 → "12" is a number → mp3Player.play(12)
 → MP3Player: position = order.indexOf(11)
 → playCurrent(): getCurrentTrack() → Track "Frank Ocean - Moon River"
 → load(track.getFileName()) → minim.loadMP3File(...)  + volume
 → audioPlayer.play()  → music plays in the background
 → console is ready for the next command
```

### 7. Files and libraries

- `Player/minim_library/`: the jars for playback (SimpleMinim) and `mp3agic-0.9.1.jar` for the ID3 tags.
- `Player/music/`: the songs. Adding more songs: copy `.mp3` files into this folder and restart, or type `scan Player/music`.
- `Player/.gitignore`: keeps `music/` and `*.mp3` out of GitHub.
- `.vscode/settings.json` tells VS Code where to find the jars.

### Questions you might be asked, and short answers

- **Why Variante 2?** Each class has one job. The player stays small, and features like saving or deleting playlists can go into `PlaylistManager` later without changing the player.
- **Why the `order` list instead of shuffling the playlist?** The playlist stays unchanged, so shuffle can be switched off again and the original order comes back.
- **Why `SimpleMinim(true)`?** Without it, `play()` would block until the song ends, and the console would freeze.
- **Why gain in dB instead of volume?** `setVolume` isn't supported. Gain is logarithmic, which is why the formula uses `20·log10`.
- **What's missing?** Automatically playing the next song at the end of a track, and saving or deleting playlists, which are the greyed-out methods on the sheet.

---

## Entry 2 – The M3U playlist file default.m3u (2026-10-28)

### Why
The exercise sheet (page 8) says: *"Es soll mindestens eine Playlist automatisch geladen werden, die im Filesystem als M3U-Datei definiert ist."* Before this entry, the player only scanned the `music` folder. Now there is a real M3U file: [Player/default.m3u](Player/default.m3u).

### What the file looks like
```
#EXTM3U
#EXTINF:190,6LACK - Inwood Hill Park [Lyric Video]
music/6LACK - Inwood Hill Park [Lyric Video].mp3
#EXTINF:225,A$AP Rocky - Multiply (Official Audio) ft. Juicy J
music/A$AP Rocky - Multiply (Official Audio) ft. Juicy J.mp3
...
```
- `#EXTM3U`: the first line marks the file as an "extended M3U" playlist.
- `#EXTINF:<seconds>,<artist - title>`: extra info about the next song: its length in seconds and a display name.
- The line without `#` is the **path to the MP3**. It is relative to the folder of the `.m3u` file, so `music/...` means `Player/music/...`.
- The file is saved as UTF-8 so special characters (é, ’, ¥) are stored correctly.

It contains all 34 songs from `Player/music`, in alphabetical order. The lengths were read from the MP3s with mp3agic (`Mp3File.getLengthInSeconds()`).

### How it is used
- At startup, `loadDefaultPlaylist()` in `KeyboardController` looks for `default.m3u` **first** (in the current folder or in `Player/`). Only if there is none does it scan the `music` folder. The console shows `Playlist "default.m3u" geladen: 34 Tracks`.
- Loading goes through `PlaylistManager.getPlaylist("…/default.m3u")` (see Entry 1, section 3): lines starting with `#` are skipped, every other line becomes a `Track`. The title and artist shown in the player come from the ID3 tags, not from the `#EXTINF` line.
- The playlist's name is the file name (`default.m3u`).

### Making your own playlists
1. Create a new text file next to `default.m3u`, e.g. `Player/chill.m3u`.
2. Write `#EXTM3U` in the first line, then one `music/<filename>.mp3` per line. The `#EXTINF` lines are optional.
3. In the player: `load Player/chill.m3u` (or `load chill.m3u` if the program runs inside `Player/`).

The order of the lines is the playing order. To change the default playlist, edit `default.m3u`: reorder, delete or add lines.

### Git
`default.m3u` is a small text file and **is** committed (only `music/` and `*.mp3` are in `.gitignore`). On another PC the playlist is there, but the MP3s it points to are not. Copy the `Player/music` folder over, and the player loads the same playlist there.

### Possible questions
- **What is M3U?** A plain text format for playlists: one file path per line, plus optional `#` info lines.
- **Why relative paths?** So the playlist still works if the project folder is somewhere else, e.g. on another PC.
- **What happens if a song in the M3U is missing?** `createTrack` prints `Datei nicht gefunden, wird uebersprungen: …` and that song is left out. The rest of the playlist still loads.

---

## Entry 3 – Übung 3 & 4: JavaFX user interface (2026-11-04)

### What changed
- The console control (`KeyboardController`) is replaced by a **JavaFX application** with two main views: **Player** and **Playlist**.
- New project folder **`MP3PlayerFX/`**, based on the course template `MP3PlayerFxTemplate` from read.mi (Gradle project). The console version in `Player/` stays as it is (Übung 1 & 2).
- The business classes from Übung 2 were moved into the package `de.hsrm.mi.enia.mp3player.business` and extended for the GUI.

### Starting the app
- **VS Code:** Gradle icon in the sidebar → `MP3PlayerFX` → Tasks → application → **run**.
- **Terminal:** in the folder `MP3PlayerFX`: `.\gradlew run`
- Tests: `.\gradlew test`

At startup the app loads `default.m3u`. It looks in `MP3PlayerFX/` first, then in `../Player/` (the playlist from Entry 2 with the songs in `Player/music`). With **"Playlist oeffnen …"** in the Playlist view you can load any other `.m3u` file.

### Project structure
```
MP3PlayerFX/
├── build.gradle              Gradle build: JavaFX plugin, libs/ folder as dependencies
├── libs/                     course jars: SimpleMinim, mp3agic, eia_ui (ImageViewPane)
└── src/
    ├── main/java/de/hsrm/mi/enia/mp3player/
    │   ├── StarterApp.java          main(): starts the JavaFX application
    │   ├── business/                logic, knows nothing about JavaFX
    │   │   └── Track, Playlist, PlaylistManager, MP3Player
    │   └── presentation/            everything you see
    │       ├── MP3PlayerGUI         the Application: creates everything, switches views
    │       ├── PlayerView           main view 1
    │       ├── PlaylistView         main view 2
    │       ├── NavigationBar        tabs "Player" / "Playlist"
    │       ├── CoverPane            cover + title + artist
    │       ├── ProgressPane         0:42 ───●──── 3:10
    │       ├── ControlPane          ⇄  ⏮ ▶ ⏹ ⏭  ↻ 🔈──
    │       ├── VolumePane           volume slider
    │       └── SnackBar             error messages as an overlay layer
    └── test/java/.../MP3PlayerTest.java   unit tests for the business classes
```
**`business` and `presentation` are separate packages.** The business classes don't import anything from JavaFX. The GUI uses the business classes, never the other way round.

**Change to the template's `build.gradle`:** the template requires a Java 21 *toolchain*. Only JDK 24 is installed here, so Gradle couldn't find Java 21. It now uses the installed JDK and compiles for Java 21 (`sourceCompatibility = 21`). JavaFX 21 runs on Java 17 and newer.

### Sitemap (main views and layers)
```
┌──────────────── Window (Scene) ─────────────────┐
│  StackPane (root)                               │
│   ├─ layer 1: BorderPane                        │
│   │     top:    NavigationBar [Player|Playlist] │
│   │     center: PlayerView  OR  PlaylistView    │
│   └─ layer 2: SnackBar (error messages)         │
└─────────────────────────────────────────────────┘
```
- **Player:** the "listening" scenario: what's playing now and how to control it.
- **Playlist:** the "choosing" scenario: all songs, pick one, load a different playlist. The control bar is here too, so you can pause/skip without switching views.
- Error messages appear as a **layer** on top (StackPane) instead of a popup window, as the sheet asks ("keine Popup-Fenster aus den 80ern"). This is the Material Design "Snackbar": a dark bar at the bottom that disappears after 4 seconds.

### The views and components

**Rule from the sheet:** elements that belong together are combined into one component class that **extends a Pane class**. The main views are put together from these components.

| Class | extends | Contains | Elastic behaviour |
|---|---|---|---|
| `PlayerView` | `BorderPane` | center: `CoverPane`, bottom: `ProgressPane` + `ControlPane` | the center gets all extra space |
| `PlaylistView` | `BorderPane` | top: name + "Playlist oeffnen", center: `ListView`, bottom: `ControlPane` | the list grows, long titles get "…" |
| `CoverPane` | `BorderPane` | `ImageViewPane` with cover (or ♪ placeholder), title, artist | cover scales with the window |
| `ProgressPane` | `HBox` | position label, `Slider`, length label | slider stretches (`Hgrow ALWAYS`) |
| `ControlPane` | `HBox` | shuffle, ⏮ ▶/⏸ ⏹ ⏭, repeat, `VolumePane` | spacers grow, buttons stay centred |
| `VolumePane` | `HBox` | icon + slider (0–100) | slider shrinks down to 40 px when narrow |
| `NavigationBar` | `HBox` | two `ToggleButton`s in a `ToggleGroup` | both tabs share the width |
| `SnackBar` | `Label` | message text | max. 360 px wide, wraps lines |

**Reusability:** `ControlPane` is used **twice**, once in the PlayerView and once in the PlaylistView. In JavaFX a node can only have one parent, so there are two instances. Both show the same player state because both read it from the same `MP3Player`.

**`ImageViewPane`** (from `eia_ui.jar`, page 9 of the sheet): a normal `ImageView` doesn't grow with its parent. Wrapped in an `ImageViewPane`, it scales with the window. `setPreserveRatio(true)` keeps the cover from being stretched.

**Cover image:** `PlaylistManager.createTrack` reads it from the ID3v2 tag with `getAlbumImage()` and stores the bytes in `Track.cover`. `CoverPane` turns them into an `Image`. The YouTube downloads have no cover, so the ♪ placeholder is shown.

### Tested window sizes (elastic layout)
Tested at 420×640 (start size), 900×600 (wide) and 360×560 (narrow):
- **Wide:** the cover area gets bigger, the progress slider stretches, the play buttons stay centred, shuffle stays left, repeat and volume stay right.
- **Narrow:** the volume slider gets shorter, long titles end with "…", nothing is cut off and there is no horizontal scrollbar.
- Minimum window size: 340×480 (`setMinWidth/Height`), so the controls always fit.

### How the GUI and the player work together

**1. Start (`MP3PlayerGUI`)**
- `init()` creates the business objects (`PlaylistManager`, `MP3Player`) and loads `default.m3u`. This runs *before* the window exists, so error messages are collected in `startupMessages` and shown later.
- `start(stage)` builds the views, the layers and the scene (420×640) and shows the window.
- `stop()` is called when the window is closed: it stops the timeline and the music (`player.close()`).

**2. Button click → player**
Every component gets the `MP3Player` in its constructor and calls it directly:
```java
skipButton.setOnAction(e -> player.skip());
```
The play button decides: `player.isPlaying()` ? `pause()` : `play()`.

**3. Player → display: the update timeline**
The player doesn't tell the GUI when something changes (no events yet). Instead a `Timeline` runs every 200 ms:
```java
player.update();        // detect end of song → next song
playerView.update();    // cover/title, position, play/pause icon
playlistView.update();  // highlight current song
```
Every `update()` reads the state from the player and adjusts the display. Because of this, the two `ControlPane`s and `VolumePane`s stay in sync.

**4. Errors → SnackBar**
`MP3Player` and `PlaylistManager` have `setErrorHandler(Consumer<String>)`. The GUI passes `snackBar::show`, so messages like "Ende der Playlist erreicht" appear in the window. The business classes still don't know JavaFX; they only call a function. (Default: `System.out::println`, so the tests and the console still work.)

### Changes to the business classes (compared to Übung 2)
| New / changed | Why |
|---|---|
| `Track.cover`, `getCover()` | cover image for the CoverPane |
| `Track.formatTime(seconds)` | "3:08" format, used by several views |
| `MP3Player.stop()` | the sheet asks for Play/Pause/**Stop**: pause + back to 0:00 |
| `getPosition()`, `getLength()` (ms), `seek(ms)` | progress display and seeking with the slider (`SimpleAudioPlayer.position()/length()/cue()`) |
| `playTrack(index)` | click on a song in the list (index starts at 0) |
| `isPlaying()`, `getCurrentIndex()`, `getVolume()` | the GUI reads the state for the display |
| `update()` | **automatically plays the next song**: if `playing` is true but the audio player has stopped near the end of the song (< 1 s left), the song is over → `skip()`. At the end of the playlist without repeat: `stop()`. |
| `skipBack()` | like real players: after more than 3 s it first jumps back to the start of the song |
| `close()` | the old `stop()` (ends Minim), renamed because `stop()` now means "stop button" |
| `setErrorHandler(...)` | messages go to the GUI instead of `System.out` |
| `PlaylistManager.getDefaultPlaylist(dirs...)` | searches `default.m3u` in several folders |

**Why the `playing` flag?** `audioPlayer.isPlaying()` is `false` in two cases: the user paused, *or* the song ended. `playing` stores what the user wants. Only "user wants to play, but nothing is playing, and we're at the end" means the song is over. This was tested: at the end, `isPlaying()` becomes `false` and `position()` is about 80 ms before `length()`.

### Unit tests (`MP3PlayerTest`)
The template's placeholder test always failed on purpose ("bitte loeschen"). It was replaced by 5 real tests for the classes that work without audio output: `formatTime`, `Playlist`, loading an M3U (comments/blank lines skipped, missing files reported), missing `default.m3u`. Run them with `.\gradlew test`.

### Not done yet / next steps
- **Wireframes & sitemap as drawings:** the sheet asks you to design the views with wireframes first (sitemap + detail views at two sizes). The structure above and the tested window sizes can serve as a basis, but drawing the wireframes is your own design task.
- **Skinning / CSS:** the sheet says fine-tuning and skinning come next time. Right now it uses JavaFX's default look (Modena); the icons are Unicode characters (⏮ ▶ ⏸ ⏹ ⏭ ⇄ ↻).
- **Settings view** (optional third main view).
- **Events instead of polling:** the timeline asks the player every 200 ms. A later step could have the player send events (e.g. JavaFX properties / observer), so the GUI only updates when something really changes.

### Possible questions
- **Why `extends BorderPane` / `HBox`?** Each component *is* a layout container that arranges its own children. The main view just inserts it like any other node: modular and reusable.
- **Why is the root a `StackPane` and not the PlayerView?** To have **layers**: the current view at the bottom, the error message on top. The sheet suggests a StackPane for this. The main views are swapped inside it (`mainPane.setCenter(...)`).
- **Why `SimpleMinim(true)`?** Asynchronous playback. Otherwise `play()` would block the JavaFX thread and the window would freeze.
- **Why the Timeline and not a `Thread`?** A Timeline runs on the JavaFX thread, so it can change labels and sliders directly. A separate thread would have to use `Platform.runLater`.
- **Why does the progress slider only seek on release?** While dragging, the timeline would otherwise reset the slider to the playing position, and every intermediate value would jump in the song. `update()` skips the slider while it is pressed.

---
