package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

@Service("PinballX")
public class PinballXConnector extends BaseConnector {
  public final static String PINBALL_X = FrontendType.PinballX.name();

  @Autowired
  private SystemService systemService;

  private final static Logger LOG = LoggerFactory.getLogger(PinballXConnector.class);

  @Autowired
  //private PinballXAssetsAdapter assetsAdapter;
  private PinballXAssetsIndexAdapter assetsAdapter;

  @Autowired
  private PreferencesService preferencesService;

  private Map<String, TableDetails> mapTableDetails = new HashMap<>();

  /** A cache of Playlists indexed by their id */
  private Map<Integer, Playlist> playlists;
  /** map between gameId and stat */
  private Map<Integer, TableAlxEntry> gameStats;
  /** set of favorite gameId */
  private Set<Integer> gameFavs;

  @Override
  public void initializeConnector() {
    PinballXSettings ps = getSettings();
    if (ps != null) {
      assetsAdapter.configureCredentials(ps.getGameExMail(), ps.getGameExPassword());
    }
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

    frontend.setFrontendExe("PinballX.exe");
    frontend.setAdminExe("Settings.exe");
    frontend.setIconName("pinballx.png");
    List<VPinScreen> screens = new ArrayList<>(Arrays.asList(VPinScreen.values()));
    screens.remove(VPinScreen.Other2);
    frontend.setSupportedScreens(screens);
    frontend.setIgnoredValidations(Arrays.asList(GameValidationCode.CODE_NO_OTHER2,
        GameValidationCode.CODE_PUP_PACK_FILE_MISSING,
        GameValidationCode.CODE_ALT_SOUND_FILE_MISSING
    ));

    PinballXSettings ps = getSettings();
    frontend.setAssetSearchEnabled(ps != null && ps.isGameExEnabled());
    frontend.setAssetSearchLabel("GameEx Assets Search for PinballX");
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
      preferencesService.savePreference(PreferenceNames.PINBALLX_SETTINGS, settings);
      // reinitialize the connector with updated settings
      initializeConnector();
    }
    catch (Exception e) {
      LOG.error("Saving pinballX settings failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void clearCache() {
    this.mapTableDetails.clear();
    super.clearCache();
  }

  @Override
  protected List<Emulator> loadEmulators() {
    INIConfiguration iniConfiguration = loadPinballXIni();
    if (iniConfiguration == null) {
      return Collections.emptyList();
    }

    File pinballXFolder = getInstallationFolder();

    List<Emulator> emulators = new ArrayList<>();
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
        Emulator emu = createEmulator(s, pinballXFolder, emuId, emuName);
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
        Emulator emulator = createEmulator(s, pinballXFolder, emuId++, emuname);
        if (emulator != null) {
          emulators.add(emulator);
        }
      }
    }

    //check the launch and exist scripts
    for (Emulator emulator : emulators) {
      initVisualPinballXScripts(emulator, iniConfiguration);
    }

    return emulators;
  }

  @NonNull
  private File getPinballXIni() {
    File pinballXFolder = getInstallationFolder();
    return new File(pinballXFolder, "/Config/PinballX.ini");
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

    try (FileReader fileReader = new FileReader(pinballXIni, Charset.forName(charset))) {
      iniConfiguration.read(fileReader);
    }
    catch (Exception e) {
      LOG.error("cannot parse ini file " + pinballXIni, e);
      return null;
    }

    // check presence of [internal] section
    SubnodeConfiguration s = iniConfiguration.getSection("internal");
    return s.isEmpty() ? null : iniConfiguration;
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
  private Emulator createEmulator(SubnodeConfiguration s, File installDir, int emuId, String emuname) {
    boolean enabled = s.getBoolean("Enabled", false);
    String tablePath = s.getString("TablePath");
    String workingPath = s.getString("WorkingPath");
    String executable = s.getString("Executable");
    //String parameters = s.getString("Parameters");

    String gameext = null;
    if (s.containsKey("SystemType")) {
      int systemType = s.getInt("SystemType");
      switch (systemType) {
        case 1:
          gameext = "vpx";
          break; // Visual Pinball
        case 2:
          gameext = "vpx";
          break; // Future Pinball
        case 4:
          gameext = "exe";
          break; // Custom Exe
      }
    }
    else {
      gameext = getEmulatorExtension(emuname);
    }

    Emulator e = new Emulator();
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    File mediaDir = new File(installDir, "Media/" + emuname);
    if (mediaDir.exists() && mediaDir.isDirectory()) {
      e.setDirMedia(mediaDir.getAbsolutePath());
    }

    if (tablePath == null || !new File(tablePath).exists()) {
      LOG.warn("Skipped loading of \"" + emuname + "\" because the tablePath is invalid");
      return null;
    }

    if (workingPath == null || !new File(workingPath).exists()) {
      LOG.warn("Skipped loading of \"" + emuname + "\" because the workingPath is invalid");
      return null;
    }

    e.setDirGames(tablePath);
    e.setEmuLaunchDir(workingPath);
    e.setExeName(executable);

    e.setGamesExt(gameext);
    e.setVisible(enabled);

    return e;
  }


  @Override
  protected List<String> loadGames(Emulator emu) {
    File pinballXFolder = getInstallationFolder();
    List<String> games = new ArrayList<>();

    File pinballXDb = new File(pinballXFolder, "/Databases/" + emu.getName() + "/" + emu.getName() + ".xml");
    if (pinballXDb.exists()) {
      PinballXTableParser parser = new PinballXTableParser();
      parser.addGames(pinballXDb, games, mapTableDetails, emu);
    }

    return games;
  }

  @Override
  public int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName,
                        @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {

    // pinballX does not support gameName, so force equality with gameFileName
    String gameNameFromFileName = gameFileName;
    return super.importGame(emulatorId, gameNameFromFileName, gameFileName, gameDisplayName, launchCustomVar, dateFileUpdated);
  }

  @Override
  protected void loadStats() {
    // load statistics, method is also caching data
    getAlxData();
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
  protected void commitDb(Emulator emu) {
    File pinballXFolder = getInstallationFolder();
    File pinballXDb = new File(pinballXFolder, "/Databases/" + emu.getName() + "/" + emu.getName() + ".xml");

    PinballXTableParser parser = new PinballXTableParser();
    parser.writeGames(pinballXDb, gamesByEmu.get(emu.getId()), mapTableDetails, emu);
  }

  //------------------------------------------------------------

  @Override
  public MediaAccessStrategy getMediaAccessStrategy() {
    return new PinballXMediaAccessStrategy();
  }

  @Override
  public TableAssetsAdapter getTableAssetAdapter() {
    //return new CacheTableAssetsAdapter(assetsAdapter);
    return assetsAdapter;
  }

  @Override
  public List<FrontendPlayerDisplay> getFrontendPlayerDisplays() {
    INIConfiguration iniConfiguration = loadPinballXIni();
    List<FrontendPlayerDisplay> displayList = new ArrayList<>();
    if (iniConfiguration != null) {
      SubnodeConfiguration display = iniConfiguration.getSection("Display");
      displayList.add(createDisplay(display, VPinScreen.PlayField));

      SubnodeConfiguration topper = iniConfiguration.getSection("Topper");
      displayList.add(createDisplay(topper, VPinScreen.Topper));

      SubnodeConfiguration dmd = iniConfiguration.getSection("DMD");
      displayList.add(createDisplay(dmd, VPinScreen.DMD));

      SubnodeConfiguration backGlass = iniConfiguration.getSection("BackGlass");
      displayList.add(createDisplay(backGlass, VPinScreen.BackGlass));

      SubnodeConfiguration apron = iniConfiguration.getSection("Apron");
      displayList.add(createDisplay(apron, VPinScreen.Menu));
    }
    return displayList;
  }

  //----------------------------------
  // Playlist management

  @Override
  public List<Playlist> getPlayLists() {
    File pinballXFolder = getInstallationFolder();

    List<Playlist> result = new ArrayList<>();

    Playlist favs = getFavPlaylist();
    result.add(favs);

    this.playlists = new HashMap<>();
    int id = 1;
    for (Emulator emu : emulators.values()) {
      File dbfolder = new File(pinballXFolder, "/Databases/" + emu.getName());
      for (File f : dbfolder.listFiles((dir, name) -> StringUtils.endsWithIgnoreCase(name, ".xml"))) {
        String playlistname = FilenameUtils.getBaseName(f.getName());
        if (!StringUtils.equalsIgnoreCase(playlistname, emu.getName())) {

          Playlist p = new Playlist();
          p.setId(id++);
          p.setEmulatorId(emu.getId());
          p.setName(playlistname);
          // don't set mediaName, studio will use the name

          // now cache playlist
          playlists.put(p.getId(), p);

          // get color if set
          File fileconf = getPlaylistConfFile(p.getId());
          Map<String, ?> playlistConf = getPlaylistConf(fileconf);
          p.setMenuColor((Integer) playlistConf.get("menuColor"));
          
          PinballXTableParser parser = new PinballXTableParser();
          List<String> _games = new ArrayList<>();
          Map<String, TableDetails> _tabledetails = new HashMap<>();
          parser.addGames(f, _games, _tabledetails, emu);

          List<PlaylistGame> pg = _games.stream()
              .map(g -> toPlaylistGame(filenameToId(emu.getId(), g)))
              .collect(Collectors.toList());
          p.setGames(pg);

          result.add(p);

        }
      }
    }
    return result;
  }

  private Playlist getFavPlaylist() {
    Playlist favs = new Playlist();
    favs.setId(-1);
    favs.setName("Favorites");
    List<PlaylistGame> favspg = gameFavs.stream().map(id -> toPlaylistGame(id)).collect(Collectors.toList());
    favs.setGames(favspg);
    return favs;
  }

  @NonNull
  @Override
  public Playlist getPlayList(int id) {
    return id == -1 ? getFavPlaylist() : playlists.get(id);
  }

  private File getPlaylistConfFile(int playlistId) {
    Playlist playlist = getPlayList(playlistId);
    if (playlist != null && playlist.getEmulatorId() != null) {
      Emulator emu = getEmulator(playlist.getEmulatorId());
      File pinballXFolder = getInstallationFolder();
      File dbfolder = new File(pinballXFolder, "/Databases/" + emu.getName());
      return new File(dbfolder, playlist.getName() + ".json");
    }
    return null;
  }

  private Map<String, Object> getPlaylistConf(File playlistConfFile) {
    if (playlistConfFile != null && playlistConfFile.exists()) {
      try {
        String content = Files.readString(playlistConfFile.toPath(), Charset.forName("UTF-8"));
        // convert JSON string to Map
        return new ObjectMapper().readValue(content, new TypeReference<>() {});
      }
      catch (IOException ioe) {
        LOG.error("Ignored error, cannot read file " + playlistConfFile.getAbsolutePath(), ioe);
      }
    }
    return new HashMap<>();
  }

  private void savePlaylistConf(File playlistConfFile, Map<String, ?> playlistConf) {
    if (playlistConfFile != null) {
      try {
        String content = new ObjectMapper().writeValueAsString(playlistConf);
        Files.write(playlistConfFile.toPath(), content.getBytes(Charset.forName("UTF-8")));
      }
      catch (IOException ioe) {
        LOG.error("Ignored error, cannot write file " + playlistConfFile.getAbsolutePath(), ioe);
      }
    }
  }
  
  private PlaylistGame toPlaylistGame(int gameId) {
    PlaylistGame pg = new PlaylistGame();
    pg.setId(gameId);

    TableAlxEntry gamestat = gameStats.get(gameId);
    if (gamestat != null) {
      pg.setPlayed(true);
      pg.setFav(gameFavs.contains(gameId));
      pg.setGlobalFav(false);
    }
    else {
      pg.setPlayed(false);
    }

    return pg;
  }

  @Override
  public File getPlaylistMediaFolder(@NonNull Playlist playList, @NonNull VPinScreen screen) {
    File pinballXFolder = getInstallationFolder();
    // not standard but why not...
    File mediaDir = new File(pinballXFolder, "Media/Playlists");
    return new File(mediaDir, screen.getSegment());
  }

  @Override
  public void setPlaylistColor(int playlistId, long color) {
    Playlist playlist = getPlayList(playlistId);
    if (playlist != null) {
      playlist.setMenuColor((int) color);
      File fileconf = getPlaylistConfFile(playlistId);
      Map<String, Object> playlistConf = getPlaylistConf(fileconf);
      playlistConf.put("menuColor", color);
      savePlaylistConf(fileconf, playlistConf);
    }
  }

  @Override
  public void addToPlaylist(int playlistId, int gameId, int favMode) {
    if (playlistId >= 0) {
      Playlist pl = playlists.get(playlistId);
      if (!pl.containsGame(gameId)) {
        pl.getGames().add(toPlaylistGame(gameId));
      }
      savePlaylist(pl);
    } else {
      gameFavs.add(gameId);
      saveFavorite(gameId, true);
    }
  }
  @Override
  public void deleteFromPlaylists(int gameId) {
    for (Integer playlistId : playlists.keySet()) {
      deleteFromPlaylist(playlistId, gameId);
    }
  }
  @Override
  public void deleteFromPlaylist(int playlistId, int gameId) {
    if (playlistId >= 0) {
      Playlist pl = playlists.get(playlistId);
      if (pl.removeGame(gameId)) {
        savePlaylist(pl);
      }
    }
    else {
      if (gameFavs.remove(gameId)) {
        saveFavorite(gameId, false);
      }
    }
  }

  private void saveFavorite(int gameId, boolean favorite) {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    parser.writeAlxData(getGame(gameId), favorite);
  }

  protected void savePlaylist(Playlist pl) {
    if (pl.getEmulatorId() != null) {
      Emulator emu = getEmulator(pl.getEmulatorId());
      PinballXTableParser parser = new PinballXTableParser();
      List<String> games = pl.getGames().stream().map(pg -> getGameFilename(pg.getId())).collect(Collectors.toList());

      File pinballXFolder = getInstallationFolder();
      File playlistDb = new File(pinballXFolder, "/Databases/" + emu.getName() + "/" + pl.getName() + ".xml");
      parser.writeGames(playlistDb, games, mapTableDetails, emu);
    }
  }

  //----------------------------------
  // Statistics

  @NonNull
  @Override
  public List<TableAlxEntry> getAlxData() {
    PinballXStatisticsParser parser = new PinballXStatisticsParser(this);
    List<TableAlxEntry> stats = new ArrayList<>();
    Set<Integer> favs = new HashSet<>();
    parser.getAlxData(stats, favs);
    
    // refresh cache of stats
    gameStats = new HashMap<>();
    for (TableAlxEntry stat : stats) {
      gameStats.put(stat.getGameId(), stat);
    }
    gameFavs = favs;

    return stats;
  }

  /**
   * Leverage cache instead of callling PinballXTableParser.getAlxData(gameId)
   */
  @Override
  public List<TableAlxEntry> getAlxData(int gameId) {
    List<TableAlxEntry> stats = new ArrayList<>();
    TableAlxEntry stat = gameStats.get(gameId);
    if (stat != null) {
      stats.add(stat);
    }
    return stats;
  }

  @Override
  public boolean updateNumberOfPlaysForGame(int gameId, long value) {
    return true;//TODO
  }

  @Override
  public boolean updateSecondsPlayedForGame(int gameId, long seconds) {
    return true;//TODO
  }

  //----------------------------------
  // UI Management

  @Override
  public boolean killFrontend() {
    List<ProcessHandle> vpinProcesses = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() &&
            (
                p.info().command().get().contains("PinballX") ||
                    p.info().command().get().contains("PinUpDisplay") ||
                    p.info().command().get().contains("PinUpPlayer") ||
                    p.info().command().get().contains("VPXStarter") ||
                    p.info().command().get().contains("VPinballX") ||
                    p.info().command().get().startsWith("VPinball") ||
                    p.info().command().get().contains("B2SBackglassServerEXE") ||
                    p.info().command().get().contains("DOF")))
        .collect(Collectors.toList());

    if (vpinProcesses.isEmpty()) {
      LOG.info("No PinballX processes found, termination canceled.");
      return false;
    }

    for (ProcessHandle pinUpProcess : vpinProcesses) {
      String cmd = pinUpProcess.info().command().get();
      boolean b = pinUpProcess.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
    }
    return true;
  }

  @Override
  public boolean isFrontendRunning() {
    List<ProcessHandle> allProcesses = systemService.getProcesses();
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        if (cmdName.contains("PinballX")) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean restartFrontend() {
    killFrontend();

    try {
      List<String> params = Arrays.asList("cmd", "/c", "start", "PinballX.exe");
      SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
      executor.setDir(getInstallationFolder());
      executor.executeCommandAsync();

      //StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("PinballX restart failed: {}", standardErrorFromCommand);
        return false;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to start PinballX again: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  /**
   * Ensures that the VPin Studio Logo is available for PinballX in the launcher.
   */
  public void setVPinStudioAppEnabled(boolean b) {
    File pcWheelFolder = new File(this.getInstallationFolder(), "POPMedia/PC Games/Wheel/");
    if (pcWheelFolder.exists()) {
      File wheelIcon = new File(pcWheelFolder, UIDefaults.APP_TITLE + ".png");
      if (!wheelIcon.exists()) {
        try {
          InputStream resourceAsStream = ResourceLoader.class.getResourceAsStream("logo-500.png");
          FileUtils.copyInputStreamToFile(resourceAsStream, wheelIcon);
          resourceAsStream.close();
          LOG.info("Copied VPin Studio App icon.");

          File thumbsFolder = new File(pcWheelFolder, "pthumbs");
          de.mephisto.vpin.commons.utils.FileUtils.deleteFolder(thumbsFolder);
        }
        catch (Exception e) {
          LOG.info("Failed to copy VPin App wheel icon: " + e.getMessage(), e);
        }
      }
    }
  }

  //---------------- Utilities -----------------------------------------------------------------------------------------


  private void initVisualPinballXScripts(Emulator emulator, INIConfiguration iniConfiguration) {
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

  private FrontendPlayerDisplay createDisplay(SubnodeConfiguration display, VPinScreen screen) {
    FrontendPlayerDisplay player = new FrontendPlayerDisplay();
    player.setName(screen.name());
    player.setMonitor(Integer.parseInt(display.getString("monitor", "0")));
    player.setX(Integer.parseInt(display.getString("x", "0")));
    player.setY(Integer.parseInt(display.getString("y", "0")));
    player.setWidth(Integer.parseInt(display.getString("width", "0")));
    player.setHeight(Integer.parseInt(display.getString("height", "0")));
    player.setRotation(Integer.parseInt(display.getString("rotate", "0")));

    LOG.info("Created PinballX player display \"" + screen.name() + "\"");
    return player;
  }
}
