package de.mephisto.vpin.server.frontend.pinbally;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.pinballx.PinballXMediaAccessStrategy;
import de.mephisto.vpin.server.frontend.pinballx.PinballXTableParser;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
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
    frontend.setIgnoredValidations(Arrays.asList(GameValidationCode.CODE_NO_OTHER2,
        GameValidationCode.CODE_PUP_PACK_FILE_MISSING,
        GameValidationCode.CODE_ALT_SOUND_FILE_MISSING
    ));

    // recordings screens
    frontend.setSupportedRecordingScreens(Arrays.asList(VPinScreen.PlayField, VPinScreen.BackGlass, VPinScreen.DMD,
      VPinScreen.Topper, VPinScreen.Menu));

    frontend.setPlayfieldMediaInverted(true);
    return frontend;
  }

  @Override
  public void clearCache() {
    this.mapTableDetails.clear();
    super.clearCache();
  }

  @Override
  protected List<Emulator> loadEmulators() {
    Properties settings = loadPinballYSettings();
    if (settings == null) {
      return Collections.emptyList();
    }

    File pinballYFolder = getInstallationFolder();

    List<Emulator> emulators = new ArrayList<>();
    mapTableDetails = new HashMap<>();

    // Add specific ones
    for (int emuId = 1; emuId < 20; emuId++) {
      String system = settings.getProperty("System" + emuId);
      if (StringUtils.isNotEmpty(system)) {
        Emulator emulator = createEmulator(settings, pinballYFolder, emuId, system);
        if (emulator != null) {
          emulators.add(emulator);
        }
      }
    }

    //check the launch and exist scripts
    for (Emulator emulator : emulators) {
      initVisualPinballYScripts(emulator, settings);
    }

    return emulators;
  }

  @NonNull
  private File getPinballYSettings() {
    File pinballYFolder = getInstallationFolder();
    return new File(pinballYFolder, "/Settings.txt");
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
  private Emulator createEmulator(Properties s, File pinballYFolder, int emuId, String emuname) {
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

    Emulator e = new Emulator(type);
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    String mediaFolder = StringUtils.defaultIfEmpty(s.getProperty(system + ".MediaDir"), emuname);
    File mediaDir = new File(getMediaPath(s), mediaFolder);
    e.setDirMedia(mediaDir.getAbsolutePath());

    String databaseName = StringUtils.defaultIfEmpty(s.getProperty(system + ".DatabaseDir"), emuname);
    File databaseFile = new File(getTableDatabasePath(s), databaseName + "/" + databaseName + ".xml");
    e.setDatabase(databaseFile.getAbsolutePath());

    // exe can be a full path, or just the exename that has to be resolved with default folder or empty
    String executable = s.getProperty(system + ".Exe");
    File resolved = resolveExe(type);

    File exe = StringUtils.isNotEmpty(executable) ? new File(executable) : null;
    if (exe == null || !exe.exists()) {
      exe = StringUtils.isNotEmpty(executable) && resolved != null ? new File(resolved.getParentFile(), executable) : null;
      if (exe == null || !exe.exists()) {
        exe = resolved;
      }
    }

    if (exe != null) {
      e.setEmuLaunchDir(exe.getParentFile().getAbsolutePath());
      e.setExeName(exe.getName());
    }
    else {
      LOG.error("Executable not set for " + emuname + " in pinballY options, studio won't be able to lauch tables. "
        + "Please fill in the full path to executable !");
    }

    e.setGamesExt(type.getExtension());
    e.setVisible(enabled);

    String tablePath = StringUtils.defaultIfEmpty(s.getProperty(system + ".TablePath"), "Tables");
    File dirGames = new File(tablePath);
    if (!dirGames.exists()) {
      dirGames = resolved != null ? new File(resolved.getParentFile(), tablePath) : null;
      if (dirGames == null || !dirGames.exists()) {
        dirGames = null;
      }
    }

    if (dirGames != null) {
      e.setDirGames(dirGames.getAbsolutePath());
    }
    else { 
      LOG.warn("Skipped loading of \"" + emuname + "\" because the tablePath is invalid");
      return null;
    }
  
    return e;
  }

  private File getMediaPath(Properties s) {
    return getPath(s, "MediaPath");
  }
  private File getTableDatabasePath(Properties s) {
    return getPath(s,"TableDatabasePath");
  }
  private File getPath(Properties s, String variable) {
    String path = s.getProperty(variable);
    if (path.contains("[PinballX]")) {
      SystemInfo si = new SystemInfo();
      path = path.replace("[PinballX]", si.resolvePinballXInstallationFolder().getAbsolutePath());
    }

    File f = new File(path);
    if (!f.exists()) {
      f = new File(getInstallationFolder(), path);
    }
    return f.exists() ? f : null;
  }

  @Override
  protected List<String> loadGames(Emulator emu) {
    List<String> games = new ArrayList<>();
    File pinballXDb = new File(emu.getDatabase());
    if (pinballXDb.exists()) {
      PinballXTableParser parser = new PinballXTableParser();
      parser.addGames(pinballXDb, games, mapTableDetails, emu);
    }
    return games;
  }

  @Override
  public int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName,
                        @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {
    // pinballY does not support gameName, so force equality with gameFileName
    String gameNameFromFileName = gameFileName;
    return super.importGame(emulatorId, gameNameFromFileName, gameFileName, gameDisplayName, launchCustomVar, dateFileUpdated);
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
  protected void commitDb(Emulator emu) {
    File pinballXDb = new File(emu.getDatabase());
    PinballXTableParser parser = new PinballXTableParser();
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
      createDisplay(displayList, settings, "DMDWindow", VPinScreen.DMD, "DMD", false);
      createDisplay(displayList, settings, "TopperWindow", VPinScreen.Topper, "Topper", false);
      createDisplay(displayList, settings, "InstCardWindow", VPinScreen.Menu, "Apron", false);
    }
    return displayList;
  }

  /**
PlayfieldWindow.Position = 583,13,1664,758
PlayfieldWindow.Rotation = 90
PlayfieldWindow.MirrorHorz = 0
PlayfieldWindow.MirrorVert = 0
PlayfieldWindow.FullScreen = 0
PlayfieldWindow.Maximized = 0
PlayfieldWindow.Minimized = 0
   */
  private void createDisplay(List<FrontendPlayerDisplay> players, Properties display, String sectionName, VPinScreen screen, String name, boolean defaultVisibility) {
    String visible = display.getProperty(sectionName + ".Visible");
    boolean isVisible =  StringUtils.isEmpty(visible) ? defaultVisibility : StringUtils.equals(visible, "1");
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
        GraphicsDevice gd  = gds[i];
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
  protected void savePlaylist(int gameId, Playlist pl) {
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

  private void initVisualPinballYScripts(Emulator emulator, Properties iniConfiguration) {
/*
    if (emulator.isVisualPinball()) {
      //VPX scripts
      SubnodeConfiguration visualPinball = iniConfiguration.getSection("VisualPinball");
      visualPinball.setProperty("LaunchBeforeEnabled", "True");
      visualPinball.setProperty("LaunchBeforeExecutable", "emulator-launch.bat");
      visualPinball.setProperty("LaunchBeforeParameters", "\"[TABLEPATH]\\[TABLEFILE]\"");
      visualPinball.setProperty("LaunchBeforeWorkingPath", new File(RESOURCES + "/scripts").getAbsolutePath());

      visualPinball.setProperty("LaunchAfterEnabled", "True");
      visualPinball.setProperty("LaunchAfterExecutable", "emulator-exit.bat");
      visualPinball.setProperty("LaunchAfterParameters", "\"[TABLEPATH]\\[TABLEFILE]\"");
      visualPinball.setProperty("LaunchAfterWorkingPath", new File(RESOURCES + "/scripts").getAbsolutePath());

      //frontend launch script
      SubnodeConfiguration startup = iniConfiguration.getSection("StartupProgram");
      startup.setProperty("Enabled", " True");
      startup.setProperty("Executable", "frontend-launch.bat");
      startup.setProperty("WorkingPath", new File(RESOURCES + "/scripts").getAbsolutePath());

      saveIni(iniConfiguration);
    }
  }

  private void saveIni(INIConfiguration iniConfiguration) {

    try (FileWriter fileWriter = new FileWriter(getPinballYSettings(), Charset.forName("UTF-16"))) {
      //iniConfiguration.write(fileWriter);
    }
    catch (Exception e) {
      LOG.error("Failed to write PinballX.ini: " + e.getMessage(), e);
    }

  */
  }

}
