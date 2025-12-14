package de.mephisto.vpin.server.frontend.pinbally;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.frontend.pinbally.PinballYSettings;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.pinballx.PinballXMediaAccessStrategy;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

@Service("PinballY")
public class PinballYConnector extends BaseConnector {
  private final static Logger LOG = LoggerFactory.getLogger(PinballYConnector.class);

  public final static String PINBALL_Y = FrontendType.PinballY.name();

  @Autowired
  private SystemService systemService;


  private Map<String, TableDetails> mapTableDetails = new HashMap<>();


  @Override
  public void initializeConnector() {
    super.setMediaAccessStrategy(new PinballXMediaAccessStrategy(getInstallationFolder()));
  }

  @NonNull
  @Override
  public File getInstallationFolder() {
    return systemService.getPinballYInstallationFolder();
  }

  public Frontend getFrontend() {
    Frontend frontend = new Frontend();
    frontend.setName("PinballY");
    frontend.setInstallationDirectory(getInstallationFolder().getAbsolutePath());
    frontend.setFrontendType(FrontendType.PinballY);

    frontend.setFrontendExe(getFrontendExe());
    frontend.setIconName("pinbally.png");
    List<VPinScreen> screens = new ArrayList<>(Arrays.asList(VPinScreen.values()));
    screens.remove(VPinScreen.Other2);
    frontend.setSupportedScreens(screens);
    frontend.setIgnoredValidations(Arrays.asList(GameValidationCode.CODE_NO_OTHER2));

    frontend.setPlayfieldMediaInverted(true);
    return frontend;
  }

  @Override
  public void reloadCache() {
    this.mapTableDetails.clear();
    super.reloadCache();
  }

  @Override
  public boolean deleteEmulator(int emulatorId) {
    throw new UnsupportedOperationException("Deletion of emulators not supported");
  }

  @Override
  public GameEmulator saveEmulator(GameEmulator emulator) {
    return null;
  }

  @Override
  protected List<GameEmulator> loadEmulators() {
    Properties settings = loadPinballYSettings();
    if (settings == null) {
      return Collections.emptyList();
    }

    File pinballYFolder = getInstallationFolder();

    List<GameEmulator> emulators = new ArrayList<>();
    mapTableDetails = new HashMap<>();

    var settingsFileLines = readSettingsFileLines();

    Boolean settingsFileChanged = false;
    // Add specific ones
    for (int emuId = 1; emuId < 20; emuId++) {
      String system = settings.getProperty("System" + emuId);
      if (StringUtils.isNotEmpty(system)) {
        GameEmulator emulator = createEmulator(settings, pinballYFolder, emuId, system);
        if (emulator != null) {
          emulators.add(emulator);

          if (!emulator.getType().isVpxEmulator())
            continue;

          // Update RunBefore en RunAfter in settings.txt for VPX emulators
          for (var runType : new String[]{"RunBefore", "RunAfter"}) {
            var settingValue = settings.getProperty("System" + emuId + "." + runType);
            if (StringUtils.isAllBlank(settingValue) || settingValue.endsWith("& :: Added by VPin Studio")) {
              settingsFileChanged |= updateSetting(settingsFileLines, emuId, system, runType);
            }
          }
        }
      }
    }

    if (settingsFileChanged) {
      writeSettingsFileLines(settingsFileLines);
    }

    return emulators;
  }

  @NonNull
  private File getPinballYSettings() {
    File pinballYFolder = getInstallationFolder();
    return new File(pinballYFolder, "/Settings.txt");
  }

  @Override
  public PinballYSettings getSettings() {
    try {
      return preferencesService.getJsonPreference(PreferenceNames.PINBALLY_SETTINGS, PinballYSettings.class);
    }
    catch (Exception e) {
      LOG.error("Getting pinballY settings failed: " + e.getMessage(), e);
      return null;
    }
  }

  private Properties loadPinballYSettings() {
    File pinballYSettings = getPinballYSettings();
    if (!pinballYSettings.exists()) {
      LOG.warn("Settings.txt file not found " + pinballYSettings);
      return null;
    }
    PinballYSettingsParser parser = new PinballYSettingsParser();
    return parser.loadSettings(pinballYSettings);
  }

  /*
System1 = Visual Pinball X
System1.Class = VP
# SystemN.Class = system type:  type of system, one of the following:
#   VPX    - Visual Pinball 10
#   VP     - Visual Pinball 9 or earlier
#   FP     - Future Pinball
#   STEAM  - Steam-based program (Pinball Arcade, Pinball FX2, etc)
#   Other  - Any other system [this is the default]
System1.MediaDir = Visual Pinball
System1.DatabaseDir = Visual Pinball
System1.Enabled = 0
System1.Exe = 
System1.ShowWindow = SW_SHOWMINIMIZED
System1.Environment = 
System1.TablePath = Tables
System1.Parameters = /play -"[TABLEPATH]\[TABLEFILE]"
System1.DefExt = .vpx
System1.RunBefore = cmd /c echo Example RunBefore command! Path=[TABLEPATH], file=[TABLEFILE] && pause
System1.RunAfter = cmd /c echo Example Run After command! Path=[TABLEPATH], file=[TABLEFILE] && pause
  */
  private GameEmulator createEmulator(Properties s, File pinballYFolder, int emuId, String emuname) {
    String system = "System" + emuId;

    String sEnable = s.getProperty(system + ".Enabled");
    boolean enabled = sEnable == null || StringUtils.equals(sEnable, "1");

    EmulatorType type = null;
    String systemClass = s.getProperty(system + ".Class");
    if (systemClass != null) {
      switch (systemClass) {
        case "VP":
          type = EmulatorType.VisualPinball9;
          break; // Visual Pinball 9
        case "VPX":
          type = EmulatorType.VisualPinball;
          break; // Visual Pinball
        case "FP":
          type = EmulatorType.FuturePinball;
          break; // Future Pinball
        case "STEAM":
          type = EmulatorType.ZenFX;
          break; // Future Pinball
        default:
          type = EmulatorType.OTHER;
          break; // Custom Exe
      }
    }
    else {
      type = EmulatorType.fromName(emuname);
    }

    GameEmulator e = new GameEmulator();
    e.setType(type);
    e.setId(emuId);
    e.setSafeName(emuname);
    e.setName(emuname);

    String mediaFolder = StringUtils.defaultIfEmpty(s.getProperty(system + ".MediaDir"), emuname);
    File mediaDir = new File(getMediaPath(s), mediaFolder);
    e.setMediaDirectory(mediaDir.getAbsolutePath());

    String databaseName = StringUtils.defaultIfEmpty(s.getProperty(system + ".DatabaseDir"), emuname);
    File databaseFile = new File(getTableDatabasePath(s), databaseName + "/" + databaseName + ".xml");
    e.setDatabase(databaseFile.getAbsolutePath());

    // exe can be a full path, or just the exename that has to be resolved with default folder or empty
    String executable = s.getProperty(system + ".Exe");
    File resolvedExe = resolveExe(type);

    File exe = StringUtils.isNotEmpty(executable) ? new File(executable) : null;
    if (exe == null || !exe.exists()) {
      exe = StringUtils.isNotEmpty(executable) && resolvedExe != null ? new File(resolvedExe.getParentFile(), executable) : null;
      if (exe == null || !exe.exists()) {
        exe = resolvedExe;
      }
    }

    if (exe != null) {
      e.setInstallationDirectory(exe.getParentFile().getAbsolutePath());
      e.setExeName(exe.getName());
    }
    else {
      LOG.error("Executable '" + executable + "' not or wrongly set for " + emuname + " in PinballY options "
          + "default exe couldn't be determined. VPin Studio won't be able to launch tables. "
          + "Please fill in the full path to executable !");
    }

    e.setGameExt(type.getExtension());
    e.setEnabled(enabled);

    String tablePath = StringUtils.defaultIfEmpty(s.getProperty(system + ".TablePath"), "Tables");
    File dirGames = new File(tablePath);
    if (!dirGames.exists()) {
      dirGames = resolvedExe != null ? new File(resolvedExe.getParentFile(), tablePath) : null;
      if (dirGames == null || !dirGames.exists()) {
        dirGames = null;
      }
    }

    if (dirGames != null) {
      e.setGamesDirectory(dirGames.getAbsolutePath());
    }
    else if (exe != null && exe.exists() && (EmulatorType.VisualPinball.equals(type) || EmulatorType.VisualPinball9.equals(type))) {
      File tablesDir = new File(exe.getParentFile(), tablePath);
      if (tablesDir.exists()) {
        e.setGamesDirectory(tablesDir.getAbsolutePath());
        LOG.warn("PinballY is using default fallback folder {} as games directory.", tablesDir.getAbsolutePath());
      }
      else {
        LOG.warn("No games directory set for {}", emuname);
      }
    }
    else {
      LOG.warn("No games directory set for {}", emuname);
    }

    //always return the emulator, otherwise it can't be managed.
    return e;
  }

  private File getMediaPath(Properties s) {
    return getPath(s, "MediaPath");
  }

  private File getTableDatabasePath(Properties s) {
    return getPath(s, "TableDatabasePath");
  }

  private File getPath(Properties s, String variable) {
    String path = s.getProperty(variable);
    if (path.contains("[PinballX]")) {
      path = path.replace("[PinballX]", systemService.resolvePinballXInstallationFolder().getAbsolutePath());
    }

    File f = new File(path);
    if (!f.exists()) {
      f = new File(getInstallationFolder(), path);
    }
    return f.exists() ? f : null;
  }

  @Override
  protected List<String> loadGames(GameEmulator emu) {
    List<String> games = new ArrayList<>();
    File pinballXDb = new File(emu.getDatabase());
    if (pinballXDb.exists()) {
      PinballYSettings settings = preferencesService.getJsonPreference(PreferenceNames.PINBALLY_SETTINGS);
      Charset charset = settings.getCharset() != null ? Charset.forName(settings.getCharset()) : Charset.defaultCharset();
      PinballYTableParser parser = new PinballYTableParser(charset);
      parser.addGames(pinballXDb, games, mapTableDetails, emu);
    }
    return games;
  }

  //---------------------------------------------------
  public static String compose(int emuId, String game) {
    return emuId + "@" + game;
  }

  @Override
  protected TableDetails getGameFromDb(int emuId, String game) {
    return mapTableDetails.get(compose(emuId, game));
  }

  @Override
  protected void updateGameInDb(int emuId, String game, TableDetails details) {
    mapTableDetails.put(compose(emuId, game), details);
  }

  @Override
  protected void dropGameFromDb(int emuId, String game) {
    mapTableDetails.remove(compose(emuId, game));
  }

  @Override
  protected void commitDb(GameEmulator emu) {
    File pinballXDb = new File(emu.getDatabase());

    PinballYSettings settings = preferencesService.getJsonPreference(PreferenceNames.PINBALLY_SETTINGS);
    Charset charset = settings.getCharset() != null ? Charset.forName(settings.getCharset()) : Charset.defaultCharset();
    PinballYTableParser parser = new PinballYTableParser(charset);
    parser.writeGames(pinballXDb, gamesByEmu.get(emu.getId()), mapTableDetails, emu);
  }

  //------------------------------------------------------------

  @Override
  public List<FrontendPlayerDisplay> getFrontendPlayerDisplays() {
    Properties settings = loadPinballYSettings();
    List<FrontendPlayerDisplay> displayList = new ArrayList<>();
    if (settings != null) {
      createDisplay(displayList, settings, "PlayfieldWindow", VPinScreen.PlayField, "PlayField", true);
      createDisplay(displayList, settings, "BackglassWindow", VPinScreen.BackGlass, "Backglass", false);
      createDisplay(displayList, settings, "DMDWindow", VPinScreen.Menu, "DMD", false);
      createDisplay(displayList, settings, "TopperWindow", VPinScreen.Topper, "Topper", false);
      createDisplay(displayList, settings, "InstCardWindow", VPinScreen.Other2, "Apron", false);
    }
    return displayList;
  }

  /**
   * PlayfieldWindow.Position = 583,13,1664,758
   * PlayfieldWindow.Rotation = 90
   * PlayfieldWindow.MirrorHorz = 0
   * PlayfieldWindow.MirrorVert = 0
   * PlayfieldWindow.FullScreen = 0
   * PlayfieldWindow.Maximized = 0
   * PlayfieldWindow.Minimized = 0
   */
  private void createDisplay(List<FrontendPlayerDisplay> players, Properties display, String sectionName, VPinScreen screen, String name, boolean defaultVisibility) {
    String visible = display.getProperty(sectionName + ".Visible");
    boolean isVisible = StringUtils.isEmpty(visible) ? defaultVisibility : StringUtils.equals(visible, "1");
    if (isVisible) {
      FrontendPlayerDisplay player = new FrontendPlayerDisplay();
      player.setName(name);
      player.setScreen(screen);

      String position = display.getProperty(sectionName + ".Position");
      String[] positions = StringUtils.split(position, ",");
      String rotation = StringUtils.defaultString(display.getProperty(sectionName + ".Rotation"), "0");
      if (VPinScreen.PlayField.equals(screen)) {
        player.setInverted(true);
      }

      boolean fullScreen = "1".equals(display.getProperty(sectionName + ".FullScreen"));

      // identify the screen that contains our top let corner
      GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
      for (int i = 0; i < gds.length; i++) {
        GraphicsDevice gd = gds[i];
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();
        if (bounds.contains(Integer.parseInt(positions[0]), Integer.parseInt(positions[1]))) {
          player.setMonitor(i);

          if (fullScreen) {
            player.setX((int) bounds.getX());
            player.setY((int) bounds.getY());
            player.setWidth((int) bounds.getWidth());
            player.setHeight((int) bounds.getHeight());
          }
          else {
            player.setX(Integer.parseInt(positions[0]));
            player.setY(Integer.parseInt(positions[1]));
            player.setWidth(Integer.parseInt(positions[2]) - player.getX());
            player.setHeight(Integer.parseInt(positions[3]) - player.getY());
          }
          break;
        }
      }
      player.setRotation(Integer.parseInt(rotation));

      LOG.info("Created PinballY player display {}", player);
      players.add(player);
    }
  }

  //----------------------------------
  // Playlist management

  @Override
  public List<Playlist> loadPlayLists() {
    PinballYStatisticsParser parser = new PinballYStatisticsParser(this);

    List<Playlist> result = new ArrayList<>();
    parser.getAlxData(emulators.values(), null, null, result);
    return result;
  }

  @Override
  public Playlist savePlaylist(Playlist playlist) {
    if (playlist.getId() >= -1) {
      // delete first the old playlist, to insert new one
      if (playlist.getId() >= 0) {
        deletePlaylist(playlist.getId());
      }
      // pesrsist all games
      for (PlaylistGame pg : playlist.getGames()) {
        savePlaylistGame(pg.getId(), playlist);
      }
    }
    // then add to cache
    return super.savePlaylist(playlist);
  }

  @Override
  public boolean deletePlaylist(int playlistId) {
    Playlist playlist = getPlaylist(playlistId);
    if (playlist != null) {
      List<PlaylistGame> oldgames = playlist.removeGames();
      for (PlaylistGame pg : oldgames) {
        savePlaylistGame(pg.getId(), playlist);
      }
      return super.deletePlaylist(playlistId);
    }
    // already deleted
    return false;
  }

  @Override
  protected void savePlaylistGame(int gameId, Playlist pl) {
    PinballYStatisticsParser parser = new PinballYStatisticsParser(this);
    parser.writePlaylistGame(getGame(gameId), pl);
  }

  //----------------------------------
  // Favorites

  @Override
  public Set<Integer> loadFavorites() {
    PinballYStatisticsParser parser = new PinballYStatisticsParser(this);
    Set<Integer> favs = new HashSet<>();
    parser.getAlxData(emulators.values(), null, favs, null);
    return favs;
  }

  @Override
  protected void saveFavorite(int gameId, boolean favorite) {
    PinballYStatisticsParser parser = new PinballYStatisticsParser(this);
    parser.writeFavorite(getGame(gameId), favorite);
  }

  //----------------------------------
  // Statistics

  @Override
  public List<TableAlxEntry> loadStats() {
    PinballYStatisticsParser parser = new PinballYStatisticsParser(this);
    List<TableAlxEntry> stats = new ArrayList<>();
    parser.getAlxData(emulators.values(), stats, null, null);
    return stats;
  }

  @Override
  public boolean updateNumberOfPlaysForGame(int gameId, long value) {
    PinballYStatisticsParser parser = new PinballYStatisticsParser(this);
    parser.writeNumberOfPlayed(getGame(gameId), value);
    return super.updateNumberOfPlaysForGame(gameId, value);
  }

  @Override
  public boolean updateSecondsPlayedForGame(int gameId, long seconds) {
    PinballYStatisticsParser parser = new PinballYStatisticsParser(this);
    parser.writeSecondsPlayed(getGame(gameId), seconds);
    return super.updateSecondsPlayedForGame(gameId, seconds);
  }

  //----------------------------------
  // UI Management

  @Override
  protected String getFrontendExe() {
    return "PinballY.exe";
  }

  //---------------- Utilities -----------------------------------------------------------------------------------------

  private Boolean updateSetting(List<String> lines, int emuId, String emulatorName, String runType) {
    // Construct the new settings line
    StringBuilder newSettingsLine = new StringBuilder();
    newSettingsLine.append("System" + emuId + "." + runType + " = ");
    newSettingsLine.append("[NOWAIT HIDE]curl -X POST http://localhost:" + SystemUtil.getPort() + "/service/");
    newSettingsLine.append(runType == "RunBefore" ? "gameLaunch" : "gameExit");
    newSettingsLine.append(" --data-urlencode \"table=[TABLEPATH]\\[TABLEFILE]\"");
    newSettingsLine.append(" --data-urlencode \"emu=" + emulatorName + "\"");
    newSettingsLine.append(" & :: Added by VPin Studio"); /* used to detect earlier update by VPin Studio */

    var updateAt = -1; // to find the "SystemX.RunBefore" / "SystemX.RunAfter" line to update
    var insertAfter = -1; // to find the last "SystemX" line to insert after

    for (var t = 0; t < lines.size(); t++) {

      var regex = "^System" + emuId + "\\." + runType + "\\s*=.*";
      if (lines.get(t).matches(regex)) {
        updateAt = t;
      }

      if (lines.get(t).startsWith("System" + emuId)) {
        insertAfter = t;
      }
    }

    // update the existing line
    if (updateAt != -1) {
      // but only if it is different from the original
      if (!lines.get(updateAt).equals(newSettingsLine.toString())) {
        lines.set(updateAt, newSettingsLine.toString());
        return true;
      }
      return false; // there was no change
    }

    // create a new line if there was no existing line
    if (insertAfter != -1) {
      lines.add(insertAfter + 1, newSettingsLine.toString());
      return true;
    }

    return false; // there was no change
  }

  private List<String> readSettingsFileLines() {
    // Read the PinballY settings.txt file, return null if something went wrong
    try {
      return Files.readAllLines(getPinballYSettings().toPath(), StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      return null;
    }
  }

  private void writeSettingsFileLines(List<String> settingsFileLines) {
    // Write back to the PinballY settings.txt file
    if (settingsFileLines != null) {
      try {
        Files.write(getPinballYSettings().toPath(), settingsFileLines, StandardCharsets.UTF_8);
      }
      catch (IOException e) {
        LOG.error("Failed to update settings.txt with RunBefore and RunAfter commands: " + e.getMessage(), e);
      }
    }
  }
}
