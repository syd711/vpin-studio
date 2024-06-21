package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.assets.TableAssetsService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.WinRegistry;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FrontendService implements InitializingBean {

  private final static Logger LOG = LoggerFactory.getLogger(FrontendService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private TableAssetsService tableAssetsService;

  @Autowired
  private Map<String, FrontendConnector> frontendsMap; // autowiring of Frontends

  private final Map<Integer, GameEmulator> emulators = new LinkedHashMap<>();

  public FrontendService(Map<String, FrontendConnector> frontends) {
    this.frontendsMap = frontends;
  }


  public FrontendConnector getFrontendConnector() {
    FrontendType frontendType = getFrontendType();
    return frontendsMap.get(frontendType.name());
  }

  public FrontendType getFrontendType() {
    return FrontendType.PinballX;
  }

  public Frontend getFrontend() {
    return getFrontendConnector().getFrontend();
  }

  //----------------------------------------
  // Access to cached emulators

  public GameEmulator getGameEmulator(int emulatorId) {
    return this.emulators.get(emulatorId);
  }

  public List<GameEmulator> getGameEmulators() {
    return new ArrayList<>(this.emulators.values());
  }

  public List<GameEmulator> getVpxGameEmulators() {
    return this.emulators.values().stream().filter(e -> e.isVpxEmulator()).collect(Collectors.toList());
  }

  public List<GameEmulator> getBackglassGameEmulators() {
    List<GameEmulator> gameEmulators = new ArrayList<>(this.emulators.values());
    return gameEmulators.stream().filter(e -> {
      return e.getB2STableSettingsXml().exists();
    }).collect(Collectors.toList());
  }

  public GameEmulator getDefaultGameEmulator() {
    Collection<GameEmulator> values = emulators.values();
    for (GameEmulator value : values) {
      if (value.getDescription() != null && value.isVpxEmulator() && value.getDescription().contains("default")) {
        return value;
      }
    }

    for (GameEmulator value : values) {
      if (value.isVpxEmulator() && value.getNvramFolder().exists()) {
        return value;
      }
      else {
        LOG.error(value + " has no nvram folder \"" + value.getNvramFolder().getAbsolutePath() + "\"");
      }
    }
    LOG.error("Failed to determine emulator for highscores, no VPinMAME/nvram folder could be resolved (" + emulators.size() + " VPX emulators found).");
    return null;
  }

  //-----------------------------------

  public TableDetails getTableDetails(int id) {
    FrontendConnector frontend = getFrontendConnector();
    TableDetails manifest = frontend.getTableDetails(id);
    if (manifest != null) {
      GameEmulator emu = emulators.get(manifest.getEmulatorId());
      manifest.setLauncherList(new ArrayList<>(emu.getAltExeNames()));
    }
    return manifest;

  }

  public void saveTableDetails(int id, TableDetails tableDetails) {
    getFrontendConnector().saveTableDetails(id, tableDetails);
  }

  public void updateTableFileUpdated(int id) {
    getFrontendConnector().updateTableFileUpdated(id);
  }

  //--------------------------
  private Game setGameEmulator(Game game) {
    if (game != null) {
      GameEmulator emulator = emulators.get(game.getEmulatorId());
      game.setEmulator(emulator);
    }
    return game;
  }

  private List<Game> setGameEmulator(List<Game> games) {
    for (Game game : games) {
      setGameEmulator(game);
    }
    return games;
  }

  public Game getGame(int id) {
    return setGameEmulator(getFrontendConnector().getGame(id));
  }

  public Game getGameByFilename(String filename) {
    return setGameEmulator(getFrontendConnector().getGameByFilename(filename));
  }

  public List<Game> getGamesByEmulator(int emulatorId) {
    return setGameEmulator(getFrontendConnector().getGamesByEmulator(emulatorId));
  }

  public List<Game> getGamesByFilename(String filename) {
    return setGameEmulator(getFrontendConnector().getGamesByFilename(filename));
  }

  public Game getGameByName(String gameName) {
    return setGameEmulator(getFrontendConnector().getGameByName(gameName));
  }

  public List<Game> getGames() {
    List<Game> results = setGameEmulator(getFrontendConnector().getGames());
    results.sort(Comparator.comparing(Game::getGameDisplayName));
    return results;
  }

  //--------------------------

  // no more used ?
  public int getVersion() {
    return getFrontendConnector().getVersion();
  }

  public JsonSettings getSettings() {
    return getFrontendConnector().getSettings();
  }

  public void saveSettings(@NonNull Map<String, Object> data) {
    getFrontendConnector().saveSettings(data);
  }

  public void setPupPackEnabled(Game game, boolean enable) {
    if (game != null) {
      getFrontendConnector().setPupPackEnabled(game, enable);
    }
  }

  public boolean isPupPackDisabled(Game game) {
    if (game != null) {
      return getFrontendConnector().isPupPackDisabled(game);
    }
    return false;
  }

  public int importGame(@NonNull File file, int emuId) {

    String baseName = FilenameUtils.getBaseName(file.getName());
    String formattedBaseName = baseName;//.replaceAll(" ", "-");
    Game gameByName = getGameByName(formattedBaseName);
    int count = 1;
    while (gameByName != null) {
      formattedBaseName = FilenameUtils.getBaseName(file.getName()) + count;
      LOG.info("Found existing gamename that exists while importing \"" + file.getName() + "\", trying again with \"" + formattedBaseName + "\"");
      gameByName = getGameByName(formattedBaseName);
    }

    GameEmulator gameEmulator = emulators.get(emuId);
    String gameFileName = gameEmulator.getGameFileName(file);
    String gameDisplayName = baseName.replaceAll("-", " ").replaceAll("_", " ");
    return getFrontendConnector().importGame(emuId, formattedBaseName, gameFileName, gameDisplayName, null, new Date(file.lastModified()));
  }

  public int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {
    return getFrontendConnector().importGame(emulatorId, gameName, gameFileName, gameDisplayName, launchCustomVar, dateFileUpdated);
  }

  public boolean deleteGame(String name) {
    Game gameByFilename = getGameByFilename(name);
    if (gameByFilename != null) {
      return deleteGame(gameByFilename.getId());
    }
    LOG.error("Failed to delete " + name + ": no game entry has been found for this name.");
    return false;
  }

  public boolean deleteGame(int id) {
    return getFrontendConnector().deleteGame(id);
  }

  public void deleteGames() {
    getFrontendConnector().deleteGames();
  }

  public int getGameCount() {
    int count = 0;
    for (GameEmulator value : this.emulators.values()) {
      count += getFrontendConnector().getGameCount(value.getId());
    }
    return count;
  }

  public List<Integer> getGameIds() {
    List<Integer> result = new ArrayList<>();
    for (GameEmulator value : this.emulators.values()) {
      result.addAll(getFrontendConnector().getGameIds(value.getId()));
    }
    return result;
  }

  //--------------------------

  @NonNull
  public Playlist getPlayList(int id) {
    return getFrontendConnector().getPlayList(id);
  }

  @NonNull
  public List<Playlist> getPlayLists(boolean excludeSqlLists) {
    return getFrontendConnector().getPlayLists(excludeSqlLists);
  }

  public void setPlaylistColor(int playlistId, long color) {
    getFrontendConnector().setPlaylistColor(playlistId, color);
  }

  public void addToPlaylist(int playlistId, int gameId, int favMode) {
    getFrontendConnector().addToPlaylist(playlistId, gameId, favMode);
  }

  public void updatePlaylistGame(int playlistId, int gameId, int favMode) {
    getFrontendConnector().updatePlaylistGame(playlistId, gameId, favMode);
  }

  public void deleteFromPlaylists(int gameId) {
    getFrontendConnector().deleteFromPlaylists(gameId);
  }

  public void deleteFromPlaylist(int playlistId, int gameId) {
    getFrontendConnector().deleteFromPlaylist(playlistId, gameId);
  }

  public Playlist getPlayListForGame(int gameId) {
    return getFrontendConnector().getPlayListForGame(gameId);
  }

  //--------------------------

  public java.util.Date getStartDate() {
    return getFrontendConnector().getStartDate();
  }

  @NonNull
  public List<TableAlxEntry> getAlxData() {
    return getFrontendConnector().getAlxData();
  }

  @NonNull
  public List<TableAlxEntry> getAlxData(int gameId) {
    return getFrontendConnector().getAlxData(gameId);
  }

  //--------------------------

  public FrontendControl getPinUPControlFor(VPinScreen screen) {
    switch (screen) {
      case Other2: {
        return getFrontendConnector().getFunction(FrontendControl.FUNCTION_SHOW_OTHER);
      }
      case GameHelp: {
        return getFrontendConnector().getFunction(FrontendControl.FUNCTION_SHOW_HELP);
      }
      case GameInfo: {
        return getFrontendConnector().getFunction(FrontendControl.FUNCTION_SHOW_FLYER);
      }
      default: {
      }
    }

    return new FrontendControl();
  }

  public FrontendControls getControls() {
    return getFrontendConnector().getControls();
  }


  @NonNull
  public List<Integer> getGameIdsFromPlaylists() {
    return getFrontendConnector().getGameIdsFromPlaylists();
  }

  //--------------------------

  public List<FrontendPlayerDisplay> getPupPlayerDisplays() {
    List<FrontendPlayerDisplay> result = new ArrayList<>();
    try {
      INIConfiguration iniConfiguration = new INIConfiguration();
      iniConfiguration.setCommentLeadingCharsUsedInInput(";");
      iniConfiguration.setSeparatorUsedInOutput("=");
      iniConfiguration.setSeparatorUsedInInput("=");

      File ini = new File(systemService.getPinUPSystemFolder(), "PinUpPlayer.ini");
      if (!ini.exists()) {
        LOG.error("Failed to find \"" + ini.getAbsolutePath() + "\", no display info found.");
        return result;
      }

      FileReader fileReader = new FileReader(ini);
      try {
        iniConfiguration.read(fileReader);
      } finally {
        fileReader.close();
      }

      Map<String, String> sectionMappings = new HashMap<>();
      sectionMappings.put("INFO", "Topper");
      sectionMappings.put("INFO1", "DMD");
      sectionMappings.put("INFO2", "BackGlass");
      sectionMappings.put("INFO3", "PlayField");
      sectionMappings.put("INFO4", "Music");
      sectionMappings.put("INFO5", "Apron/FullDMD");
      sectionMappings.put("INFO6", "GameSelect");
      sectionMappings.put("INFO7", "Loading");
      sectionMappings.put("INFO8", VPinScreen.Other2.name());
      sectionMappings.put("INFO9", VPinScreen.GameInfo.name());
      sectionMappings.put("INFO10", VPinScreen.GameHelp.name());

      Set<String> sections = iniConfiguration.getSections();
      for (String section : sections) {
        if (section.contains("INFO")) {
          try {
            FrontendPlayerDisplay display = new FrontendPlayerDisplay();
            SubnodeConfiguration sectionNode = iniConfiguration.getSection(section);
            String name = sectionMappings.get(section);
            if (name != null) {
              display.setName(name);
              display.setX(sectionNode.getInt("ScreenXPos"));
              display.setY(sectionNode.getInt("ScreenYPos"));
              display.setWidth(sectionNode.getInt("ScreenWidth"));
              display.setHeight(sectionNode.getInt("ScreenHeight"));
              display.setRotation(sectionNode.getInt("ScreenRotation"));
            }
            else {
              LOG.warn("Unsupported PinUP display for screen '" + name + "', display has been skipped.");
            }
            result.add(display);
          }
          catch (Exception e) {
            LOG.error("Failed to create PinUPPlayerDisplay: " + e.getMessage());
          }
        }
      }

      LOG.info("Loaded " + result.size() + " PinUPPlayer displays.");
    }
    catch (Exception e) {
      LOG.error("Failed to get player displays: " + e.getMessage(), e);
    }
    return result;
  }

  public boolean isValidVPXEmulator(Emulator emulator) {
    if (!emulator.isVisualPinball()) {
      return false;
    }

    if (!emulator.isVisible()) {
      LOG.warn("Ignoring " + emulator + ", because the emulator is not visible.");
      return false;
    }

    if (StringUtils.isEmpty(emulator.getDirGames())) {
      LOG.warn("Ignoring " + emulator + ", because \"Games Folder\" is not set.");
      return false;
    }

    // should not ignore as GameEmulator will set a folder by default
    //if (StringUtils.isEmpty(emulator.getDirRoms())) {
    //  LOG.warn("Ignoring " + emulator + ", because \"Roms Folder\" is not set.");
    //  return false;
    //}

    if (getFrontendConnector().getMediaAccessStrategy() != null && StringUtils.isEmpty(emulator.getDirMedia())) {
      LOG.warn("Ignoring " + emulator + ", because \"Media Dir\" is not set.");
      return false;
    }

    return true;
  }

  public void loadEmulators() {
    List<Emulator> ems = getFrontendConnector().getEmulators();
    this.emulators.clear();
    for (Emulator emulator : ems) {
      try {
        if (!emulator.isVisible()) {
          continue;
        }
        if (!emulator.isEnabled()) {
          continue;
        }

        if (emulator.isVisualPinball() && !isValidVPXEmulator(emulator)) {
          continue;
        }

        if (emulator.isVisualPinball() && emulator.getDirB2S() == null) {
          File b2sFolder = systemService.resolveBackglassServerFolder(new File(emulator.getDirGames()));
          if (b2sFolder == null) {
            // not installed, use B2SServer folder inside vpx folder
            b2sFolder = new File(emulator.getEmuLaunchDir(), "B2SServer");
          }
          emulator.setDirB2S(b2sFolder.getAbsolutePath());
        }

        GameEmulator gameEmulator = new GameEmulator(emulator, getFrontendConnector().getMediaAccessStrategy());
        emulators.put(emulator.getId(), gameEmulator);

        LOG.info("Loaded Emulator: " + gameEmulator);
      }
      catch (Exception e) {
        LOG.error("Emulator initialization failed: " + e.getMessage(), e);
      }
    }

    if (this.emulators.isEmpty()) {
      LOG.error("****************************************************************************************");
      LOG.error("No valid game emulators folder, fill all(!) emulator directory settings in PinUP Popper.");
      LOG.error("****************************************************************************************");
    }
  }

  //--------------------------

  @Override
  public void afterPropertiesSet() {

    FrontendConnector frontend = getFrontendConnector();
    if (frontend != null) {
      frontend.initializeConnector();
      tableAssetsService.registerAdapter(frontend.getTableAssetAdapter());
    }

    this.loadEmulators();

    getPupPlayerDisplays();

    GameEmulator defaultEmulator = getDefaultGameEmulator();
    if (defaultEmulator != null) {
      boolean b2sfolderSet = false;
      Map<String, Object> pathEntry = WinRegistry.getClassesValues(".res\\b2sserver.res\\ShellNew");
      if (!pathEntry.isEmpty()) {
        String path = String.valueOf(pathEntry.values().iterator().next());
        if (path.contains("\"")) {
          path = path.substring(1);
          path = path.substring(0, path.indexOf("\""));
          File exeFile = new File(path);
          File b2sFolder = exeFile.getParentFile();
          if (b2sFolder.exists()) {
            LOG.info("Resolved backglass server directory from WinRegistry: " + b2sFolder.getAbsolutePath());
            defaultEmulator.setBackglassServerDirectory(b2sFolder);
            b2sfolderSet = true;
          }
        }
      }
      // second try
      if (!b2sfolderSet || pathEntry.isEmpty()) {
        File backglassServerDirectory = defaultEmulator.getBackglassServerDirectory();
        File exeFile = new File(backglassServerDirectory, "B2SBackglassServerEXE.exe");
        File installDirectory = defaultEmulator.getInstallationFolder();
        if (!exeFile.exists() && installDirectory != null && installDirectory.exists()) {
          //search recursively for the server exe file
          Iterator<File> fileIterator = FileUtils.iterateFiles(installDirectory, new String[]{"exe"}, true);
          boolean found = false;
          while (fileIterator.hasNext()) {
            File next = fileIterator.next();
            if (next.getName().equals(exeFile.getName())) {
              defaultEmulator.setBackglassServerDirectory(next.getParentFile());
              LOG.info("Resolved backglass server directory from file search: " + defaultEmulator.getBackglassServerDirectory().getAbsolutePath());
              found = true;
              break;
            }
          }
          if (!found) {
            LOG.error("Failed to resolve backglass server directory, search returned no match. Sticking to default folder " + backglassServerDirectory.getAbsolutePath());
          }
        }
        else {
          LOG.info("Resolved backglass server directory " + backglassServerDirectory.getAbsolutePath());
        }
      }
    }
  }
}