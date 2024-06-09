package de.mephisto.vpin.server.popper;

import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import de.mephisto.vpin.restclient.popper.Playlist;
import de.mephisto.vpin.restclient.popper.PopperCustomOptions;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.WinRegistry;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;


//TODO rename as FrontendService

@Service
public class PinUPConnector implements InitializingBean, PreferenceChangedListener  {
  
  private final static Logger LOG = LoggerFactory.getLogger(PinUPConnector.class);

  public static final String IS_FAV = "isFav";

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private Map<String, FrontendConnector> frontendsMap; // autowiring of Frontends

  private final Map<Integer, GameEmulator> emulators = new LinkedHashMap<>();

  private ServerSettings serverSettings;

  public PinUPConnector(Map<String, FrontendConnector> frontends) {
    this.frontendsMap = frontends;
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

  private FrontendConnector getFrontend() {
    return frontendsMap.get("pinUPConnectorImpl");
    //return frontendsMap.get("pinballXConnector");
  }
 

  public TableDetails getTableDetails(int id) {
    FrontendConnector frontend = getFrontend();
    TableDetails manifest = frontend.getTableDetails(id);

    List<String> altExeList = Collections.emptyList();//getAltExeList();
    GameEmulator emu = emulators.get(manifest.getEmulatorId());
    manifest.setLauncherList(new ArrayList<>(emu.getAltExeNames()));
    manifest.getLauncherList().addAll(altExeList);
    return manifest;

  }
  public void saveTableDetails(int id, TableDetails tableDetails) {
    getFrontend().saveTableDetails(id, tableDetails);
  }

  public void updateTableFileUpdated(int id) {
    getFrontend().updateTableFileUpdated(id);
  }

  //--------------------------
  private Game SetGameEmulator(Game game) {
    if (game != null) {
      GameEmulator emulator = emulators.get(game.getEmulatorId());
      game.setEmulator(emulator);
    }
    return game;
  }
  private List<Game> SetGameEmulator(List<Game> games) {
    for (Game game: games) {
      SetGameEmulator(game);
    }
    return games;
  }

  public Game getGame(int id) {
    return SetGameEmulator(getFrontend().getGame(id));
  }

  public Game getGameByFilename(String filename) {
    return SetGameEmulator(getFrontend().getGameByFilename(filename));
  }
  public List<Game> getGamesByEmulator(int emulatorId) {
    return SetGameEmulator(getFrontend().getGamesByEmulator(emulatorId));
  }
  public List<Game> getGamesByFilename(String filename) {
    return SetGameEmulator(getFrontend().getGamesByFilename(filename));
  }

  public Game getGameByName(String gameName) {
    return SetGameEmulator(getFrontend().getGameByName(gameName));
  }

  public List<Game> getGames() {
    List<Game>  results = SetGameEmulator(getFrontend().getGames());
    results.sort(Comparator.comparing(Game::getGameDisplayName));
    return results;
  }

  //--------------------------

  // TODO rename as getVersion()
  public int getSqlVersion() {
    return getFrontend().getSqlVersion();
  }
  public boolean isPopper15() {
    return getFrontend().isPopper15();
  }
  
  public PopperCustomOptions getCustomOptions() {
    return getFrontend().getCustomOptions();
  }
  public void updateCustomOptions(@NonNull PopperCustomOptions options) {
    getFrontend().updateCustomOptions(options);
  }

  public void updateRom(@NonNull Game game, String rom) {
    getFrontend().updateRom(game, rom);
  }

  public void updateGamesField(@NonNull Game game, String field, String value) {
    getFrontend().updateGamesField(game, field, value);
  }

  public String getGamesStringValue(@NonNull Game game, @NonNull String field) {
    return getFrontend().getGamesStringValue(game, field);
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
    return getFrontend().importGame(emuId, formattedBaseName, gameFileName, gameDisplayName, null, new Date(file.lastModified()));
  }

  public int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {
    return getFrontend().importGame(emulatorId, gameName, gameFileName, gameDisplayName, launchCustomVar, dateFileUpdated);
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
    return getFrontend().deleteGame(id);
  }
  public void deleteGames() {
    getFrontend().deleteGames();
  }

  public int getGameCount() {
    int count = 0;
    for (GameEmulator value : this.emulators.values()) {
      count += getFrontend().getGameCount(value.getId());
    }
    return count;
  }

  public List<Integer> getGameIds() {
    List<Integer> result = new ArrayList<>();
    for (GameEmulator value : this.emulators.values()) {
      result.addAll(getFrontend().getGameIds(value.getId()));
    }
    return result;
  }

  //--------------------------

  @NonNull
  public Playlist getPlayList(int id) {
    return getFrontend().getPlayList(id);
  }
  @NonNull
  public List<Playlist> getPlayLists(boolean excludeSqlLists) {
    return getFrontend().getPlayLists(excludeSqlLists);
  }
  public void setPlaylistColor(int playlistId, long color) {
    getFrontend().setPlaylistColor(playlistId, color);
  }
  public void addToPlaylist(int playlistId, int gameId, int favMode) {
    getFrontend().addToPlaylist(playlistId, gameId, favMode);
  }
  public void updatePlaylistGame(int playlistId, int gameId, int favMode) {
    getFrontend().updatePlaylistGame(playlistId, gameId, favMode);
  }
  public void deleteFromPlaylists(int gameId) {
    getFrontend().deleteFromPlaylists(gameId);
  }
  public void deleteFromPlaylist(int playlistId, int gameId) {
    getFrontend().deleteFromPlaylist(playlistId, gameId);
  }
  public Playlist getPlayListForGame(int gameId) {
    return getFrontend().getPlayListForGame(gameId);
  }

  //--------------------------

  public java.util.Date getStartDate() {
    return getFrontend().getStartDate();
  }

  @NonNull
  public List<TableAlxEntry> getAlxData() {
    return getFrontend().getAlxData();
  }
  @NonNull
  public List<TableAlxEntry> getAlxData(int gameId) {
    return getFrontend().getAlxData(gameId);
  }

  //--------------------------

  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    switch (screen) {
      case Other2: {
        return getFrontend().getFunction(PinUPControl.FUNCTION_SHOW_OTHER);
      }
      case GameHelp: {
        return getFrontend().getFunction(PinUPControl.FUNCTION_SHOW_HELP);
      }
      case GameInfo: {
        return getFrontend().getFunction(PinUPControl.FUNCTION_SHOW_FLYER);
      }
      default: {
      }
    }

    return new PinUPControl();
  }

  public PinUPControls getControls() {
    return getFrontend().getControls();
  }


  @NonNull
  public List<Integer> getGameIdsFromPlaylists() {
    return getFrontend().getGameIdsFromPlaylists();
  }

  //--------------------------

  public List<PinUPPlayerDisplay> getPupPlayerDisplays() {
    List<PinUPPlayerDisplay> result = new ArrayList<>();
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
      sectionMappings.put("INFO8", PopperScreen.Other2.name());
      sectionMappings.put("INFO9", PopperScreen.GameInfo.name());
      sectionMappings.put("INFO10", PopperScreen.GameHelp.name());

      Set<String> sections = iniConfiguration.getSections();
      for (String section : sections) {
        if (section.contains("INFO")) {
          try {
            PinUPPlayerDisplay display = new PinUPPlayerDisplay();
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

  public static boolean isValidVPXEmulator(Emulator emulator) {
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

    if (StringUtils.isEmpty(emulator.getDirRoms())) {
      LOG.warn("Ignoring " + emulator + ", because \"Roms Folder\" is not set.");
      return false;
    }

    if (StringUtils.isEmpty(emulator.getDirMedia())) {
      LOG.warn("Ignoring " + emulator + ", because \"Media Dir\" is not set.");
      return false;
    }

    return true;
  }

  public void loadEmulators() {
    List<Emulator> ems = getFrontend().getEmulators();
    this.emulators.clear();
    for (Emulator emulator : ems) { 
      try {
        if (!emulator.isVisible()) {
          continue;
        }

        if (emulator.isVisualPinball() && !isValidVPXEmulator(emulator)) {
          continue;
        }

        if (!emulator.isEnabled()) {
          continue;
        }

        GameEmulator gameEmulator = new GameEmulator(emulator, getFrontend().getMediaAccessStrategy());
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
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (propertyName.equals(PreferenceNames.SERVER_SETTINGS)) {
      this.serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() {

    this.serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    FrontendConnector frontend = getFrontend();
    if (frontend!=null) {
      frontend.initialize(this.serverSettings);
    }

    this.loadEmulators();

    getPupPlayerDisplays();

    GameEmulator defaultEmulator = getDefaultGameEmulator();
    if (defaultEmulator != null) {
      Map<String, Object> pathEntry = WinRegistry.getClassesValues(".res\\b2sserver.res\\ShellNew");
      if (pathEntry.isEmpty()) {
        File backglassServerDirectory = defaultEmulator.getBackglassServerDirectory();
        File exeFile = new File(defaultEmulator.getTablesFolder(), "B2SBackglassServerEXE.exe");
        if (!exeFile.exists()) {
          //search recursively for the server exe file
          Iterator<File> fileIterator = FileUtils.iterateFiles(backglassServerDirectory, new String[]{"exe"}, true);
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
      else {
        String path = String.valueOf(pathEntry.values().iterator().next());
        if (path.contains("\"")) {
          path = path.substring(1);
          path = path.substring(0, path.indexOf("\""));
          File exeFile = new File(path);
          File b2sFolder = exeFile.getParentFile();
          if (b2sFolder.exists()) {
            LOG.info("Resolved backglass server directory from WinRegistry: " + b2sFolder.getAbsolutePath());
            defaultEmulator.setBackglassServerDirectory(b2sFolder);
          }
        }
      }
    }
  }
}