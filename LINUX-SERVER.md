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

A feature can be switched back on with `system.featuresOn` in `resources/system-linux.properties`.
The pause menu and overlay preferences are hidden in Standalone mode, so the template enables them with
`system.featuresOn=CONTROLS_ENABLED,OVERLAY_ENABLED`.

## Table layout

VPX 10.8.1 keeps each table's files in the table's folder (see [VPX-10.8.1-FileLayout.md](VPX-10.8.1-FileLayout.md)),
and so does the server, **but only when the VPX installation has no `VPinMAME` folder**:

```
tables/
  Twister (1996)/
    Twister (1996).vpx
    Twister (1996).directb2s
    Twister (1996).ini          table settings override
    music/                      PlayMusic() files
    user/VPReg.stg              highscores of tables that use it
    altsound/<rom>/             AltSound
    serum/<rom>/<rom>.cRZ       Serum colorization
    vni/<rom>/                  VNI and PAL colorization
    pinmame/
      roms/twst_405.zip
      nvram/twst_405.nv
      cfg/
```

The layout is chosen once per VPX installation, by looking for `<visualPinball.installationDir>/VPinMAME`
when the server starts. It is not chosen per table and the two layouts are never mixed:

| | Per-table layout (no `VPinMAME` folder) | Legacy layout (`VPinMAME` folder exists) |
|---|---|---|
| ROMs, nvram, cfg | `<table folder>/pinmame/{roms,nvram,cfg}` | `VPinMAME/{roms,nvram,cfg}` |
| AltSound | `<table folder>/altsound/` | `VPinMAME/altsound/` |
| Colorization | `<table folder>/serum/` (`.cRZ`, `.cROMc`), `<table folder>/vni/` (`.pal`, `.vni`, `.pac`) | `VPinMAME/altcolor/` |
| Music | `<table folder>/music/` | `<installationDir>/Music/` |
| `VPReg.stg` and other user files | `<table folder>/user/` | `<installationDir>/User/` (or `user/`) |

VPX 10.8.1 still runs legacy installations, but discourages them. Use the per-table layout for a new cabinet.

### What to do with VPX 10.8.1

- **Do not keep a `VPinMAME` folder in the VPX installation folder** unless you want the legacy layout. If it is
  there, the server ignores each table's `pinmame/` folder, and ROMs and nvram that Studio uploads or reads
  end up in `VPinMAME/` instead of next to the table. When you switch, move the ROMs, nvram and cfg files
  from `VPinMAME/` into each table's `pinmame/` folder, and restart the server.
- **Put every table in its own folder.** VPX searches next to the table file, so tables that share a folder
  also share `pinmame/`, `user/` and `music/`, and tables with the same ROM overwrite each other's nvram.
- **Backglasses can be named after the table file or after the folder**, like VPX 10.8.1 does. The
  server looks for `<table file>.directb2s` first and for `<folder name>.directb2s` next to it if there is none.
  The `.ini`, `.pov` and `.vbs` files are only found by the table file's name, so name those after the table file.
- **Colorization is split like VPX 10.8.1 splits it.** Studio reads and writes Serum files in
  `<table folder>/serum/<rom>/` and VNI/PAL/PAC files in `<table folder>/vni/<rom>/`, and lists both as
  the table's colorization. A folder that Studio created earlier as `altcolor/` is no longer used; move its files
  into `serum/<rom>/` or `vni/<rom>/`.
- **Leave `VPinballX.ini` where VPX put it.** 10.8.1 stores it per minor version, in
  `~/.local/share/VPinballX/10.8/`. The server uses the newest version folder; set
  `visualPinball.configFile` to use a different one.
- **Old `VPReg.stg` files.** VPX 10.8.1 writes `<table folder>/user/VPReg.stg`, but the server reads
  `<installationDir>/User/VPReg.stg` first (also `user/`). A file left over from an older install can shadow
  the table's own one; delete it if highscores look stale.
- The server does not use `cache/`, `medias/`, `pupvideos/` or the `.info` files.

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
