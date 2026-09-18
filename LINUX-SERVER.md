# Running the VPin Studio Server on Linux

The server runs on Linux in **Standalone** mode, next to a standalone build of Visual Pinball X
(10.8.1, `VPinballX_BGFX` or `VPinballX_GL`) and any frontend. The Studio client connects to it from
another machine like it would to a Windows cabinet.

## What works

- Table management: scanning, uploads, VPS matching, backglasses, ROMs, nvram, deleting, cloning.
- Launching tables from the client.
- Highscores read from nvram (the Java parsers) and from `VPReg.stg`.
- The pause menu and overlay start when the server runs on the cabinet's display. Their key
  bindings need read access to `/dev/input` (the `input` group).

## What does not

These depend on Windows tools or APIs and are switched off by default. The server logs one line when
it skips one.

| Feature | Why |
|---|---|
| Recorder, screenshots of running tables | ffmpeg `gdigrab`/`ddagrab` |
| Highscore monitor | detects running tables by their window title |
| PINemHi | Windows binary |
| DOF, DOF tester, DOFLinx | DirectOutput / DOFLinx are Windows programs |
| PinVol, system volume | PinVol, nircmd |
| `DmdDevice.ini` | dmdext |
| PinUP Popper, PinballX, PinballY | Windows frontends |
| Server self-update, restart, tray icon, idle shutdown | Windows launchers |

A feature can be switched back on with `system.featuresOn` in `resources/system.properties`.

## Table layout

VPX 10.8.1 keeps each table's files in the table's folder, and so does the server when the VPX
installation has no `VPinMAME` folder:

```
tables/
  Twister (1996)/
    Twister (1996).vpx
    Twister (1996).directb2s
    music/
    user/VPReg.stg
    pinmame/
      roms/twst_405.zip
      nvram/twst_405.nv
      altcolor/
      altsound/
```

## Installation

1. Download `VPin-Studio-Server-linux-x64.zip` from the
   [latest release](https://github.com/syd711/vpin-studio/releases) and unzip it. It contains
   `vpin-studio-server.jar`, `VPin-Studio-Server-linux_x64.sh`, the repository's `resources` folder
   and the Zulu FX JRE tarball. Alternatively, [build](#building) `vpin-studio-server.jar` yourself and
   assemble the same folder next to `VPin-Studio-Server-linux_x64.sh`, `resources`, and
   `zulu25.34.17-ca-fx-jre25.0.3-linux_x64.tar.gz` from <https://cdn.azul.com/zulu/bin/>.
   The server downloads its remaining data files on the first start.
2. Edit [`resources/system-linux.properties`](resources/system-linux.properties). On Linux the server
   always reads this file instead of `system.properties` — there is nothing to copy or rename.
   `visualPinball.installationDir` is required: it selects Standalone mode. If it, or any other path
   set in the file, does not exist, the server logs the problem and stops rather than starting
   half-configured.
3. Optional: to import and export table scripts, download the Linux build of
   [vpxtool](https://github.com/francisdb/vpxtool/releases) and put the `vpxtool` binary into
   `resources/`.
4. Start the server with `./VPin-Studio-Server-linux_x64.sh`. It listens on port 8089.

The resources folder and the database are relative to the working directory, which the script sets
to its own folder.

### Starting with the desktop session

The overlay needs the display, so start the server as the desktop user, for example with a systemd
user unit in `~/.config/systemd/user/vpin-studio-server.service`:

```ini
[Unit]
Description=VPin Studio Server
After=graphical-session.target
PartOf=graphical-session.target

[Service]
ExecStart=/home/pinball/vpin-studio/VPin-Studio-Server-linux_x64.sh
Environment=DISPLAY=:0
Restart=on-failure

[Install]
WantedBy=graphical-session.target
```

Enable it with `systemctl --user enable --now vpin-studio-server`.

### Without a display

The server detects this from `DISPLAY` / `WAYLAND_DISPLAY` and starts headless by itself, without
the overlay and pause menu, which is enough to manage the tables. Pass `-Djava.awt.headless=true`
to force it, for instance when the server starts before the session and `DISPLAY` is already set.

## Things to know

- **The server always uses `resources/system-linux.properties` on Linux**, never
  `resources/system.properties` (that file is only read on Windows/macOS). If the file is missing, or
  `visualPinball.installationDir`, `visualPinball.executable`, `visualPinball.configFile` or
  `visualPinball.tablesFolder` is set but does not resolve to an existing path, the server logs every
  problem it found and stops instead of starting with an incomplete configuration.
- **VPX rewrites `VPinballX.ini` when it exits.** Settings changed in the client while a table is
  running are lost. Change them while VPX is stopped.
- The server launches tables with `-Play <table>` (or `-PovEdit`). The standalone builds reject the
  Windows-only `-Minimized` and `-Primary` options.
- The server stops VPX with SIGTERM, and kills it only if it has not exited after 5 seconds.
- When new tables are detected, the server stops a running VPX, as it does on Windows.

## Building

The server builds on Linux and macOS with a JDK 25 that includes JavaFX (e.g. Zulu `jdk+fx`):

```bash
mvn -N install
mvn -pl vpin-studio-server -am -Plinux package -DskipTests
```

The jar is written to `vpin-studio-server/target/vpin-studio-server.jar`. The `linux` profile skips
the Windows launcher and packages the Linux JavaFX libraries.

To build and run without installing a JDK, [vpin-studio-devenv](https://github.com/sfrazer/vpin-studio-devenv)
has a container image with the Zulu FX JDK and a sandbox cabinet — a resources folder without the
Windows tools, tables in the per-table layout, and a fake VPX that logs how it was called, so a
launch can be checked without a cabinet.
