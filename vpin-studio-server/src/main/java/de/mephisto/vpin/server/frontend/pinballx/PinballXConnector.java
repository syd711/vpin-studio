package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.GameEntry;
import de.mephisto.vpin.server.frontend.pinbally.PinballYTableParser;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

@Service("PinballX")
public class PinballXConnector extends BaseConnector {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXConnector.class);

  public final static String PINBALL_X = FrontendType.PinballX.name();

  @Autowired
  private SystemService systemService;

  @Autowired
  //private PinballXAssetsAdapter assetsAdapter;
  private PinballXAssetsIndexAdapter assetsAdapter;

  private Map<String, TableDetails> mapTableDetails = new HashMap<>();


  @Override
  public void initializeConnector() {
    super.setMediaAccessStrategy(new PinballXMediaAccessStrategy(getInstallationFolder()));

    PinballXSettings ps = getSettings();
    if (ps != null && ps.isGameExEnabled()) {
      assetsAdapter.configureCredentials(ps.getGameExMail(), ps.getGameExPassword());
      super.setTableAssetAdapter(assetsAdapter);
      // no effect if already started
      this.assetsAdapter.startRefresh();
    }
    else {
      super.setTableAssetAdapter(null);
      // no effect if already stopped
      this.assetsAdapter.stopRefresh();
    }
/*
    try {
      Class<?> aClass = Class.forName("de.mephisto.vpin.popper.PopperAssetAdapter");
      de.mephisto.vpin.connectors.assets.TableAssetsAdapter assetAdapter = (de.mephisto.vpin.connectors.assets.TableAssetsAdapter) aClass.getDeclaredConstructor().newInstance();
      super.setTableAssetAdapter(new de.mephisto.vpin.server.frontend.CacheTableAssetsAdapter(assetAdapter));
    }
    catch (Exception e) {
      LOG.error("Unable to find PopperAssetAdapter: " + e.getMessage());
    }
*/
    LOG.info("Finished initialization of " + this);
  }

  @NonNull
  @Override
  public File getInstallationFolder() {
    return systemService.getPinballXInstallationFolder();
  }

  public Frontend getFrontend() {
    Frontend frontend = new Frontend();
    frontend.setName("PinballX");
    frontend.setInstallationDirectory(getInstallationFolder().getAbsolutePath());
    frontend.setFrontendType(FrontendType.PinballX);

    frontend.setFrontendExe(getFrontendExe());
    frontend.setAdminExe("Settings.exe");
    frontend.setIconName("pinballx.png");
    List<VPinScreen> screens = new ArrayList<>(Arrays.asList(VPinScreen.values()));
    screens.remove(VPinScreen.Other2);
    frontend.setSupportedScreens(screens);
    frontend.setIgnoredValidations(Arrays.asList(GameValidationCode.CODE_NO_OTHER2,
        GameValidationCode.CODE_PUP_PACK_FILE_MISSING,
        GameValidationCode.CODE_ALT_SOUND_FILE_MISSING
    ));

    frontend.setPlayfieldMediaInverted(true);
    return frontend;
  }

  @Override
  public PinballXSettings getSettings() {
    try {
      return preferencesService.getJsonPreference(PreferenceNames.PINBALLX_SETTINGS, PinballXSettings.class);
    }
    catch (Exception e) {
      LOG.error("Getting pinballX settings failed: " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void saveSettings(@NonNull Map<String, Object> data) {
    try {
      PinballXSettings settings = JsonSettings.objectMapper.convertValue(data, PinballXSettings.class);
      preferencesService.savePreference(settings);
      // reinitialize the connector with updated settings
      initializeConnector();
    }
    catch (Exception e) {
      LOG.error("Saving pinballX settings failed: " + e.getMessage(), e);
    }
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
    INIConfiguration iniConfiguration = loadPinballXIni();
    if (iniConfiguration == null) {
      return emulator;
    }

    SubnodeConfiguration emulatorNode = iniConfiguration.getSection(emulator.getSafeName());
    if (emulatorNode == null || emulatorNode.isEmpty()) {
      LOG.warn("No matching PinballX emulator configuration found for {}, cannot save", emulator.getSafeName());
      return emulator;
    }

    if (isSystemEmulator(emulator)) {
      // if the emulator has changed, rename also the database file
      if (!emulatorNode.getString("Name").equalsIgnoreCase(emulator.getName())) {
        File oldDb = getDatabase(emulatorNode.getString("Name"));
        File newDb = getDatabase(emulator);
        if (!FileUtils.rename(oldDb, newDb)) {
          LOG.warn("Cannot rename database file {}, cannot save", oldDb.getAbsolutePath());
          return emulator;
        }

        // check also if playlists should be moved ??

        // also delete old folder if empty
        if (FileUtils.isEmpty(oldDb.getParentFile())) {
          FileUtils.deleteFolder(oldDb.getParentFile());
        }

        // change now the name
        emulatorNode.setProperty("Name", emulator.getName());
      }
    }

    emulatorNode.setProperty("Enabled", emulator.isEnabled() ? "True" : "False");
    emulatorNode.setProperty("Executable", emulator.getExeName());
    emulatorNode.setProperty("Parameters", emulator.getExeParameters());
    emulatorNode.setProperty("TablePath", emulator.getGamesDirectory());
    emulatorNode.setProperty("WorkingPath", emulator.getInstallationFolder().getAbsolutePath());

    if (emulator.getType().isVpxEmulator()) {
      initVisualPinballXScripts(emulator, iniConfiguration);
    }
    else {
      if (emulator.getLaunchScript() != null) {
        emulatorNode.setProperty("LaunchBeforeEnabled", emulator.getLaunchScript().isEnabled() ? "True" : "False");
        emulatorNode.setProperty("LaunchBeforeExecutable", emulator.getLaunchScript().getExecuteable());
        emulatorNode.setProperty("LaunchBeforeParameters", emulator.getLaunchScript().getExecuteable());
        emulatorNode.setProperty("LaunchBeforeWorkingPath", emulator.getLaunchScript().getWorkingDirectory());
      }

      if (emulator.getExitScript() != null) {
        emulatorNode.setProperty("LaunchAfterEnabled", emulator.getExitScript().isEnabled() ? "True" : "False");
        emulatorNode.setProperty("LaunchAfterExecutable", emulator.getExitScript().getExecuteable());
        emulatorNode.setProperty("LaunchAfterParameters", emulator.getExitScript().getExecuteable());
        emulatorNode.setProperty("LaunchAfterWorkingPath", emulator.getExitScript().getWorkingDirectory());
      }
    }

    saveIni(iniConfiguration);

    return emulator;
  }

  private boolean isSystemEmulator(GameEmulator emu) {
    return StringUtils.startsWith(emu.getSafeName(), "System_");
  }

  @Override
  protected List<GameEmulator> loadEmulators() {
    INIConfiguration iniConfiguration = loadPinballXIni();
    if (iniConfiguration == null) {
      return Collections.emptyList();
    }

    File pinballXFolder = getInstallationFolder();

    List<GameEmulator> emulators = new ArrayList<>();
    mapTableDetails = new HashMap<>();

    // check standard emulators, starts with Visual Pinball as default one
    String[] emuNames = new String[]{
        "Visual Pinball", "Future Pinball", "Zaccaria", "Pinball FX2", "Pinball FX3", "Pinball Arcade"
    };

    int emuId = 1;
    for (String emuName : emuNames) {
      String sectionName = emuName.replaceAll(" ", "");
      SubnodeConfiguration s = iniConfiguration.getSection(sectionName);
      if (!s.isEmpty()) {
        GameEmulator emu = createEmulator(s, pinballXFolder, emuId, emuName);
        if (emu != null) {
          emulators.add(emu);
        }
        emuId++;
      }
    }
    // Add specific ones
    for (int k = 1; k < 20; k++) {
      SubnodeConfiguration s = iniConfiguration.getSection("System_" + k);
      if (!s.isEmpty()) {
        String emuname = s.getString("Name");
        GameEmulator emulator = createEmulator(s, pinballXFolder, emuId++, emuname);
        if (emulator != null) {
          emulators.add(emulator);
        }
      }
    }

    //check the launch and exist scripts
    for (GameEmulator emulator : emulators) {
      initVisualPinballXScripts(emulator, iniConfiguration);
    }

    return emulators;
  }

  @NonNull
  private File getPinballXIni() {
    File pinballXFolder = getInstallationFolder();
    return new File(pinballXFolder, "/Config/PinballX.ini");
  }

  private File getDatabaseFolder(GameEmulator emu) {
    File pinballXFolder = getInstallationFolder();
    return new File(pinballXFolder, "/Databases/" + emu.getName());
  }

  private File getDatabase(GameEmulator emu) {
    return getDatabase(emu.getName());
  }

  private File getDatabase(String emuName) {
    File pinballXFolder = getInstallationFolder();
    return new File(pinballXFolder, "/Databases/" + emuName + "/" + emuName + ".xml");
  }

  private INIConfiguration loadPinballXIni() {
    File pinballXIni = getPinballXIni();
    if (!pinballXIni.exists()) {
      LOG.warn("Ini file not found " + pinballXIni);
      return null;
    }

    // mind pinballX.ini is encoded in UTF-16
    INIConfiguration ini = loadIni(pinballXIni, "UTF-16");
    if (ini == null) {
      // ...but old version could be in UTF-8
      ini = loadIni(pinballXIni, "UTF-8");
    }

    return ini;
  }

  private INIConfiguration loadIni(File pinballXIni, String charset) {
    INIConfiguration iniConfiguration = new INIConfiguration();
    //iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput("=");
    //iniConfiguration.setSeparatorUsedInInput("=");

    try (FileInputStream in = new FileInputStream(pinballXIni)) {
      try (BOMInputStream bOMInputStream = BOMInputStream.builder().setInputStream(in).get()) {
        ByteOrderMark bom = bOMInputStream.getBOM();
        String charsetName = bom == null ? charset : bom.getCharsetName();
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName)) {
          iniConfiguration.read(reader);
          // check presence of [internal] section
          SubnodeConfiguration s = iniConfiguration.getSection("Display");
          return s.isEmpty() ? null : iniConfiguration;
        }
        catch (Exception e) {
          LOG.error("Cannot parse {}", pinballXIni.getAbsolutePath(), e);
        }
      }
      catch (Exception e) {
        LOG.error("Cannot decode charset of {}}", pinballXIni.getAbsolutePath(), e);
      }
    }
    catch (Exception e) {
      LOG.error("Cannot open {}", pinballXIni.getAbsolutePath(), e);
    }
    return null;
  }

  /*
  [System_1]
  Name=System1 - other VPX
  Enabled=True
  SystemType=1
  WorkingPath=C:\Visual Pinball 10.7
  TablePath=C:\Visual Pinball\tables
  Executable=VPinballX.exe
  Parameters=-light
  */
  private GameEmulator createEmulator(SubnodeConfiguration s, File installDir, int emuId, String emuname) {
    String sectionName = s.getRootElementName();
    boolean enabled = s.getBoolean("Enabled", true);
    String tablePath = s.getString("TablePath");
    String workingPath = s.getString("WorkingPath");
    String executable = s.getString("Executable");
    String parameters = s.getString("Parameters");

    boolean beforeEnabled = s.getBoolean("LaunchBeforeEnabled", false);
    boolean beforeHideWindow = s.getBoolean("LaunchBeforeHideWindow", true);
    boolean beforeWaitForExit = s.getBoolean("LaunchBeforeWaitForExit", true);
    String beforeWorkingPath = s.getString("LaunchBeforeWorkingPath");
    String beforeExecutable = s.getString("LaunchBeforeExecutable");
    String beforeParameters = s.getString("LaunchBeforeParameters");

    GameEmulatorScript beforeScript = new GameEmulatorScript();
    beforeScript.setEnabled(beforeEnabled);
    beforeScript.setHideWindow(beforeHideWindow);
    beforeScript.setWaitForExit(beforeWaitForExit);
    beforeScript.setWorkingDirectory(beforeWorkingPath);
    beforeScript.setExecuteable(beforeExecutable);
    beforeScript.setParameters(beforeParameters);

    boolean afterEnabled = s.getBoolean("LaunchAfterEnabled", false);
    boolean afterHideWindow = s.getBoolean("LaunchAfterHideWindow", true);
    boolean afterWaitForExit = s.getBoolean("LaunchAfterWaitForExit", true);
    String afterWorkingPath = s.getString("LaunchAfterWorkingPath");
    String afterExecutable = s.getString("LaunchAfterExecutable");
    String afterParameters = s.getString("LaunchAfterParameters");

    GameEmulatorScript afterScript = new GameEmulatorScript();
    afterScript.setEnabled(afterEnabled);
    afterScript.setHideWindow(afterHideWindow);
    afterScript.setWaitForExit(afterWaitForExit);
    afterScript.setWorkingDirectory(afterWorkingPath);
    afterScript.setExecuteable(afterExecutable);
    afterScript.setParameters(afterParameters);

    EmulatorType type = null;
    // needed for named emulator
    if (s.containsKey("SystemType")) {
      int systemType = s.getInt("SystemType");
      switch (systemType) {
        case 1:
          type = EmulatorType.VisualPinball;
          break; // Visual Pinball
        case 2:
          type = EmulatorType.FuturePinball;
          break; // Future Pinball
        default:
          type = EmulatorType.fromName(emuname);
          break; // Custom Exe, use name
      }
    }
    else {
      type = EmulatorType.fromName(emuname);
    }
    // make sure a valid EmulatorType is guessed
    if (type == null) {
      LOG.error("Cannot determine emulator type from system name {}, so ignoring it", emuname);
      return null;
    }

    GameEmulator e = new GameEmulator();
    e.setLaunchScript(beforeScript);
    e.setExitScript(afterScript);
    e.setType(type);
    e.setId(emuId);
    e.setSafeName(sectionName);
    e.setName(emuname);

    File mediaDir = new File(installDir, "Media/" + emuname);
    if (mediaDir.exists() && mediaDir.isDirectory()) {
      e.setMediaDirectory(mediaDir.getAbsolutePath());
    }

    e.setGamesDirectory(tablePath);
    e.setInstallationDirectory(workingPath);
    e.setExeName(executable);
    e.setExeParameters(parameters);

    e.setGameExt(type.getExtension());
    e.setEnabled(enabled);
    return e;
  }


  @Override
  protected List<String> loadGames(GameEmulator emu) {
    List<String> games = new ArrayList<>();

    File pinballXDb = getDatabase(emu);
    if (pinballXDb.exists()) {
      PinballXSettings settings = preferencesService.getJsonPreference(PreferenceNames.PINBALLX_SETTINGS);
      Charset charset = settings.getCharset() != null ? Charset.forName(settings.getCharset()) : Charset.defaultCharset();
      PinballYTableParser parser = new PinballYTableParser(charset);
      parser.addGames(pinballXDb, games, mapTableDetails, emu);
    }

    return games;
  }

  @Override
  public int importGame(@NonNull TableDetails tableDetails) {
    // pinballX does not support gameName, so force equality with gameFileName
    tableDetails.setGameName(tableDetails.getGameFileName());
    return super.importGame(tableDetails);
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
    // force gameName = gameFileName
    String gameName = FilenameUtils.getBaseName(details.getGameFileName());
    details.setGameName(gameName);
    mapTableDetails.put(compose(emuId, game), details);
  }

  @Override
  protected void dropGameFromDb(int emuId, String game) {
    mapTableDetails.remove(compose(emuId, game));
  }

  @Override
  protected void commitDb(GameEmulator emu) {
    File pinballXDb = getDatabase(emu);
    PinballXSettings settings = preferencesService.getJsonPreference(PreferenceNames.PINBALLX_SETTINGS);
    Charset charset = settings.getCharset() != null ? Charset.forName(settings.getCharset()) : Charset.defaultCharset();
    PinballYTableParser parser = new PinballYTableParser(charset);
    parser.writeGames(pinballXDb, gamesByEmu.get(emu.getId()), mapTableDetails, emu);
  }

  //------------------------------------------------------------

  @Override
  public List<FrontendPlayerDisplay> getFrontendPlayerDisplays() {
    INIConfiguration iniConfiguration = loadPinballXIni();
    List<FrontendPlayerDisplay> displayList = new ArrayList<>();
    if (iniConfiguration != null) {
      createPlayfieldDisplay(iniConfiguration, displayList);
      createDisplay(iniConfiguration, displayList, "BackGlass", VPinScreen.BackGlass, true);
      createDisplay(iniConfiguration, displayList, "DMD", VPinScreen.Menu, false);
      createDisplay(iniConfiguration, displayList, "Topper", VPinScreen.Topper, false);
      createDisplay(iniConfiguration, displayList, "Apron", VPinScreen.Other2, false);
    }
    return displayList;
  }

  private void createPlayfieldDisplay(INIConfiguration iniConfiguration, List<FrontendPlayerDisplay> players) {
    SubnodeConfiguration display = iniConfiguration.getSection("Display");
    int monitor = Integer.parseInt(display.getString("Monitor", display.getString("monitor", "0")));
    GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    // sort by xPosition
    Arrays.sort(gds, (g1, g2) -> g1.getDefaultConfiguration().getBounds().x - g2.getDefaultConfiguration().getBounds().x);

    if (monitor < gds.length) {
      java.awt.Rectangle bounds = gds[monitor].getDefaultConfiguration().getBounds();
      int mX = (int) bounds.getX();
      int mY = (int) bounds.getY();

      FrontendPlayerDisplay player = new FrontendPlayerDisplay();
      player.setName(VPinScreen.PlayField.name());
      player.setScreen(VPinScreen.PlayField);
      player.setMonitor(monitor);
      player.setRotation(Integer.parseInt(display.getString("Rotate", "0")));
      player.setInverted(true);

      boolean windowed = display.getBoolean("Windowed", false);
      if (windowed) {
        player.setX(mX + Integer.parseInt(display.getString("windowx", "0")));
        player.setY(mY + Integer.parseInt(display.getString("windowy", "0")));
        player.setWidth(Integer.parseInt(display.getString("windowwidth", "0")));
        player.setHeight(Integer.parseInt(display.getString("windowheight", "0")));
      }
      else {
        player.setX(mX);
        player.setY(mY);
        player.setWidth((int) bounds.getWidth());
        player.setHeight((int) bounds.getHeight());
      }

      LOG.info("Created PinballX player display {}", player);

      players.add(player);
    }
  }

  private void createDisplay(INIConfiguration iniConfiguration, List<FrontendPlayerDisplay> players, String sectionName, VPinScreen screen, boolean defaultEnabled) {
    SubnodeConfiguration display = iniConfiguration.getSection(sectionName);
    if (!display.isEmpty()) {
      boolean enabled = display.getBoolean("Enabled", defaultEnabled);

      int monitor = Integer.parseInt(display.getString("Monitor", display.getString("monitor", "0")));
      GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

      if (enabled && monitor < gds.length) {
        Rectangle bounds = gds[monitor].getDefaultConfiguration().getBounds();
        int mX = (int) bounds.getX();
        int mY = (int) bounds.getY();

        FrontendPlayerDisplay player = new FrontendPlayerDisplay();
        player.setName(sectionName);
        player.setScreen(screen);
        player.setMonitor(monitor);
        player.setX(mX + Integer.parseInt(display.getString("x", "0")));
        player.setY(mY + Integer.parseInt(display.getString("y", "0")));
        player.setWidth(Integer.parseInt(display.getString("width", "0")));
        player.setHeight(Integer.parseInt(display.getString("height", "0")));

        LOG.info("Created PinballX player display {}", player);
        players.add(player);
      }
    }
  }

  //----------------------------------
  // Playlist management

  @Override
  public boolean deletePlaylist(int playlistId) {
    List<Playlist> playlists = this.loadPlayLists();
    playlists = playlists.stream().filter(p -> p.getId() == playlistId).collect(Collectors.toList());

    for (GameEmulator emu : emulators.values()) {
      File dbfolder = getDatabaseFolder(emu);
      for (File f : dbfolder.listFiles((dir, name) -> StringUtils.endsWithIgnoreCase(name, ".xml"))) {
        String playlistname = FilenameUtils.getBaseName(f.getName());

        for (Playlist playlist : playlists) {
          if (playlist.getName().equals(playlistname)) {
            if (!f.delete()) {
              LOG.info("Failed to delete PinballX playlist {}", f.getAbsolutePath());
            }
            else {
              LOG.info("Deleted PinballX playlist {}", f.getAbsolutePath());
            }
          }
        }
      }
    }
    return super.deletePlaylist(playlistId);
  }

  @Override
  public List<Playlist> loadPlayLists() {
    List<Playlist> result = new ArrayList<>();

    int id = 1;
    for (GameEmulator emu : emulators.values()) {
      File dbfolder = getDatabaseFolder(emu);
      if (dbfolder.exists()) {
        for (File f : dbfolder.listFiles((dir, name) -> StringUtils.endsWithIgnoreCase(name, ".xml"))) {
          String playlistname = FilenameUtils.getBaseName(f.getName());
          if (!StringUtils.equalsIgnoreCase(playlistname, emu.getName())) {

            Playlist playlist = new Playlist();
            playlist.setId(id++);
            playlist.setEmulatorId(emu.getId());
            playlist.setName(playlistname);
            // don't set mediaName, studio will use the name

            PinballXSettings settings = preferencesService.getJsonPreference(PreferenceNames.PINBALLX_SETTINGS);
            Charset charset = settings.getCharset() != null ? Charset.forName(settings.getCharset()) : Charset.defaultCharset();
            PinballYTableParser parser = new PinballYTableParser(charset);
            List<String> _games = new ArrayList<>();
            Map<String, TableDetails> _tabledetails = new HashMap<>();
            parser.addGames(f, _games, _tabledetails, emu);

            List<PlaylistGame> pg = _games.stream()
                .map(g -> toPlaylistGame(findIdFromFilename(emu.getId(), g)))
                .collect(Collectors.toList());
            playlist.setGames(pg);

            result.add(playlist);
          }
        }
      }
    }
    return result;
  }

  @Override
  protected void savePlaylistGame(int gameId, Playlist pl) {
    if (pl.getEmulatorId() != null) {
      GameEmulator emu = getEmulator(pl.getEmulatorId());
      PinballXSettings settings = preferencesService.getJsonPreference(PreferenceNames.PINBALLX_SETTINGS);
      Charset charset = settings.getCharset() != null ? Charset.forName(settings.getCharset()) : Charset.defaultCharset();
      PinballYTableParser parser = new PinballYTableParser(charset);
      List<GameEntry> games = pl.getGames().stream().map(pg -> getGameEntry(pg.getId())).collect(Collectors.toList());

      File playlistDb = new File(getDatabaseFolder(emu), pl.getName() + ".xml");
      parser.writeGames(playlistDb, games, mapTableDetails, emu);
    }
  }

  @Override
  public Playlist savePlaylist(Playlist playlist) {
    try {
      if (playlist.getEmulatorId() != null) {
        GameEmulator emulator = getEmulator(playlist.getEmulatorId());
        File dbfolder = getDatabaseFolder(emulator);

        if (playlist.getId() == -1) {
          String name = FileUtils.replaceWindowsChars(playlist.getName());
          File playlistFile = new File(dbfolder, name + ".xml");

          org.apache.commons.io.FileUtils.write(playlistFile, "<menu>\n</menu>", StandardCharsets.UTF_8);
          LOG.info("Written new playlist file {}", playlistFile.getAbsolutePath());
        }
        else {
          Playlist existingPlaylist = getPlaylist(playlist.getId());
          if (existingPlaylist != null) {
            File existingPlaylistFile = new File(dbfolder, existingPlaylist.getName() + ".xml");
            File updated = new File(dbfolder, playlist.getName() + ".xml");
            if (!existingPlaylistFile.renameTo(updated)) {
              LOG.error("Renaming playlist {} to {} failed.", existingPlaylistFile.getAbsolutePath(), updated.getAbsolutePath());
            }
            else {
              LOG.info("Renamed playlist {} to {}.", existingPlaylistFile.getAbsolutePath(), updated.getAbsolutePath());
            }
            deletePlaylistConf(existingPlaylist);
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to save PinballX/Y playlist: {}", e.getMessage(), e);
    }
    // now save colors and refresh cache
    return super.savePlaylist(playlist);
  }

//----------------------------------
// Favorites

  @Override
  public Set<Integer> loadFavorites() {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    Set<Integer> favs = new HashSet<>();
    parser.getAlxData(emulators.values(), null, favs);
    return favs;
  }

  @Override
  protected void saveFavorite(int gameId, boolean favorite) {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    parser.writeFavorite(getGame(gameId), favorite);
  }

//----------------------------------
// Statistics

  @Override
  public List<TableAlxEntry> loadStats() {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    List<TableAlxEntry> stats = new ArrayList<>();
    parser.getAlxData(emulators.values(), stats, null);
    return stats;
  }

  @Override
  public boolean updateNumberOfPlaysForGame(int gameId, long value) {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    parser.writeNumberOfPlayed(getGame(gameId), value);
    return super.updateNumberOfPlaysForGame(gameId, value);
  }

  @Override
  public boolean updateSecondsPlayedForGame(int gameId, long seconds) {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    parser.writeSecondsPlayed(getGame(gameId), seconds);
    return super.updateSecondsPlayedForGame(gameId, seconds);
  }

//----------------------------------
// UI Management


  @Override
  protected String getFrontendExe() {
    return "PinballX.exe";
  }

//---------------- Utilities -----------------------------------------------------------------------------------------

  private void initVisualPinballXScripts(GameEmulator emulator, INIConfiguration iniConfiguration) {
    if (emulator.getType().isVpxEmulator()) {
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
    if (isAdministrationRunning()) {
      killAdministration();
    }

    try (FileWriter fileWriter = new FileWriter(getPinballXIni(), Charset.forName("UTF-16"))) {
      iniConfiguration.write(fileWriter);
    }
    catch (Exception e) {
      LOG.error("Failed to write PinballX.ini: " + e.getMessage(), e);
    }
  }

  private boolean isAdministrationRunning() {
    List<ProcessHandle> allProcesses = systemService.getProcesses();
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        if (cmdName.contains("Settings.exe")) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean killAdministration() {
    List<ProcessHandle> processes = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() && p.info().command().get().contains("Settings.exe"))
        .collect(Collectors.toList());

    if (processes.isEmpty()) {
      LOG.info("No PinballX processes found, termination canceled.");
      return false;
    }

    for (ProcessHandle p : processes) {
      String cmd = p.info().command().get();
      boolean b = p.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
    }
    return true;
  }
}
