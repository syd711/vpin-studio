# Server Performance Analysis (2026-11-17)

Trigger: user report of FPS drops in VPX while the VPin Studio server runs in the background.
Scope: periodic server work, work triggered by table launch, file watchers, JavaFX overlay/pause menu.

Measurements were taken on the dev machine (16 threads, 336 processes, 60 network interfaces, JDK 25).
A cabinet CPU will be slower. Benchmark sources: `PollBench.java`, `WinBench.java`, `EnumBench.java`
(written to the session scratchpad, recreate if needed).

## Findings, ranked by likely impact

| # | Finding | Impact | Status |
|---|---------|--------|--------|
| 1 | Highscore card on table launch never disposed | High (only if card notification time > 0) | Fixed |
| 2 | Emulator process scan every 5 s, also with highscore monitor disabled | Medium, always on | Fixed |
| 3 | Burst of work on table launch | Medium, first seconds of a game | Open |
| 4 | No JVM GC limits | Low–medium | Partially fixed |
| 5 | Pause menu blocking the FX thread, leftovers after close | Low (menu lag, not VPX FPS) | Fixed |
| 6 | File watchers | Low | Open (minor waste) |
| 7 | InputEventService per-keystroke work | Low | Fixed |

### 1. Highscore card on table launch (fixed)
- `DefaultTableAndFrontendStatusChangeListenerImpl#showTableStartupScreens` (Popper launches) ->
  `ServerFX#showHighscoreCard` -> `FrontendScreensManager#showScreen`.
- Transparent, always-on-top JavaFX stage. Without a display it covers the whole playfield
  (`FrontendScreenController#showCentered`), shown while VPX starts. Transparent stages are layered
  windows (GPU readback per frame while animating); VPX loses its independent flip/exclusive mode while covered.
- After the duration the stage was only hidden, `FrontendScreenController#dispose()` was never called:
  - video via JavaFX `MediaPlayer`: `cycleCount(-1)`, unmuted -> kept decoding for the whole game, one more per launch.
  - video via VLC (`StudioMediaPlayer`, `--video-on-top`): process never killed, kept playing on top.
- Scene was created with depth buffer + `SceneAntialiasing.BALANCED`.
- Fix: `FrontendScreensManager#closeStage` disposes the controller after the fade-out; plain 2D scene.
- Only active when `CardSettings.notificationTime > 0` (default 0).

### 2. Emulator detection (fixed)
- `HighscoreMonitoringService` scheduled `checkEmulatorState` every 5 s whenever `Features.HIGHSCORE_MONITORING`
  is on, regardless of the server setting.
- `SystemService#isPinballEmulatorRunning` built `getProcesses()` (2x `ProcessHandle.info()` per process; `info()`
  is not cached and opens the process + reads its command line), then the VPX check ignored the list and used
  JNA `WindowUtils.getAllWindows` (opens the owning process of every window). Without a running VPX, four more
  passes over all processes.
- Measured before: 35 ms wall / 22 ms CPU per check while VPX runs, 204 ms / 117 ms when idle.
  JNA window enumeration 4.4 ms.
- Fix:
  - VPX check first via direct `EnumWindows` + `GetWindowText` (`WindowsUtil#isProcessRunning`), 0.8 ms;
    short-circuits the process scan.
  - Other emulators: single pass, one `info()` per process (~35 ms idle).
  - `checkEmulatorState` returns immediately when no file changes are pending, respects the 1 s debounce.
- Other callers profiting: `IScoredService`, `NotificationService`, `InputEventService`, recorder jobs,
  `FrontendStatusService` Studio launch monitor (every 3 s, only after Studio launches).

### 3. Table launch burst (open)
All on the "Game Launch Thread" while VPX loads:
- `highscoreService.scanScore(game, TABLE_LAUNCH_EVENT)` and a rescan of the last played game
  (`DefaultTableAndFrontendStatusChangeListenerImpl#tableLaunched`), may spawn PINemHi.exe.
- A detected change fires card generation (`CardService#highscoreUpdated`), Discord, webhooks, iScored, Mania, WOVP.
- `WovpService#tableLaunched` runs a WOVP synchronization for tourney tables.
- Idea: defer the last-game rescan and listener work until table exit, or run it with low thread priority.

### 4. JVM settings (partially fixed)
- `VPin-Studio-Server.l4j.ini` had no heap or GC options; G1 uses one GC worker per core.
- Fixed: `-XX:ParallelGCThreads=2 -XX:ConcGCThreads=1`.
- Not done:
  - `-Xmx` cap: real memory needs unknown, risk of OOM.
  - BELOW_NORMAL process priority: child processes inherit it (VPX launched from Studio,
    Popper restarted by the server and the VPX started by it), which would make things worse.
    Alternative to consider: lower Java thread priority for background threads only.

### 5. Pause menu (fixed)
- Hidden stages don't render; after close nothing kept running except the items below.
- Fixed:
  - `PauseMenu#togglePauseMenu` ran all REST calls and `takeScoreScreenshot` on the FX thread
    -> now background preparation (`preparePauseMenu`) + FX-only `showPauseMenu`; a toggle while preparing is ignored,
    an exit during preparation cancels the show.
  - `Thread.sleep` on the FX thread in `StateMananger#setState`, `PauseMenu#exitPauseMenu`, `MenuController#reset`
    -> `PauseTransition`.
  - `exitPauseMenu` REST/NirCmd/listener calls moved to a background thread.
  - WOVP submit blink (`Animation.INDEFINITE`) kept running if the menu was closed -> stopped in `reset()`.
  - Three nav sound `MediaPlayer`s held for the server lifetime -> `AudioClip`.
  - `GameController` listener list was a plain `ArrayList` modified concurrently -> `CopyOnWriteArrayList`,
    duplicates ignored; delayed listener re-registration could happen after the menu was closed -> session check.
- Open: the overlay (`ServerFX#showOverlay`) still loads FXML/REST on the FX thread (only used in the frontend, not in game).

### 6. File watchers (open, minor)
- Event-driven (`WatchService`), threads block in `take()`; highscore scans are deferred while an emulator runs. Not an FPS cause.
- `FileMonitoringThread` registers the *parent folder* with `FILE_TREE` (recursive). For PinVol's default location
  this is VPin Studio's `resources/` folder, so every DB/cache write wakes two threads (`PinVolService#initListener`).
- `MediaMonitor#notifyFolderChange` re-lists the complete media folder on every modify event; one watcher per
  screen per emulator.

### 7. InputEventService (fixed)
- The emulator check is NOT executed on every keystroke, only for record/screenshot/overlay/pause buttons.
- `isEventDebounced` ran `String#matches` with the key name as regex on every keystroke (fails for `[`, `\`)
  -> `equals`.
- `GameController` polls all DirectInput devices (incl. keyboard) at 20 Hz and starts a thread per button press: cheap.

## Ruled out by measurement
- `DiscoveryBroadcaster`: network interface enumeration every 5 s, 3 ms.
- JNA window enumeration: 4.4 ms (now replaced anyway).
- `CompetitionService` check every 2 min, `ManiaChallengesService` every 5 min, `ShutdownThread` every 60 s: cheap.

## Other notes
- PINemHi ROM monitor (`pinemhi_rom_monitor`) can be auto-started by the server; it is an external in-game overlay
  and worth checking on affected cabinets.
- Verification on a cabinet: leftover `vlc.exe` after a few games, server log entries `Showing highscore card` /
  `VLC is playing`, per-thread CPU in Process Explorer.
- Pre-existing test failures (unrelated): `FrontendStatusServiceTest.notifyFrontendLaunch/Exit_failingListenerDoesNotBlockLaterListeners`.
