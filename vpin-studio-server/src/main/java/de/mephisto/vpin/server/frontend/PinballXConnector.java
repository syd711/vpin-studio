package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.Playlist;
import de.mephisto.vpin.restclient.popper.PopperCustomOptions;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

@Service
@Qualifier("PinballX")
public class PinballXConnector implements FrontendConnector {

  private final static Logger LOG = LoggerFactory.getLogger(PinballXConnector.class);

  /** The list of parsed Games from XML database */
  private List<Game> games;

  private Map<Integer, TableDetails> tabledetails;

  @Override
  public void initialize(ServerSettings settings) {
  }
    
  @Override
  public List<Emulator> getEmulators() {
    List<Emulator> emulators = new ArrayList<>();
    File pinballXFolder = new File("C:/PinballX");
    File pinballXIni = new File(pinballXFolder, "/Config/PinballX.ini");
    
    if (!pinballXIni.exists()) {
      LOG.warn("Ini file not found "+ pinballXIni);
      return emulators;
    }
    
    INIConfiguration iniConfiguration = new INIConfiguration();
    //iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    //iniConfiguration.setSeparatorUsedInOutput("=");
    //iniConfiguration.setSeparatorUsedInInput("=");

    // mind pinballX.ini is encoded in UTF-16
    try (FileReader fileReader = new FileReader(pinballXIni, Charset.forName("UTF-16"))) {
      iniConfiguration.read(fileReader);
    } catch(Exception e) {
      LOG.error("cannot parse ini file " + pinballXIni, e);
    }
  
    // check standard emulators
    String[] emuNames = new String[] { 
      "Future Pinball", "Visual Pinball", "Zaccaria", "Pinball FX2", "Pinball FX3", "Pinball Arcade"
    };

    PinballXTableParser parser = new PinballXTableParser();

    this.games = new ArrayList<>();
    this.tabledetails = new HashMap<>();

    int emuId = 1;
    for (String emuName: emuNames) {
      String sectionName = emuName.replaceAll(" ", "");
      SubnodeConfiguration s = iniConfiguration.getSection(sectionName);
      if (!s.isEmpty()) {
        Emulator emu = createEmulator(s, pinballXFolder, emuId, emuName); 
        emulators.add(emu);
        File pinballXDb = new File(pinballXFolder, "/Databases/" + emuName + "/" + emuName + ".xml");
        if (pinballXDb.exists()) {
          int nbAdded = parser.addGames(pinballXDb, games, tabledetails, emu);
          LOG.info("Parsed games for emulator "+emuId + ", "+ emuName + ": " + nbAdded + " games");
        }
        emuId++;
      }
    }
    // Add specific ones
    for (int k = 1; k<20; k++) {
      SubnodeConfiguration s = iniConfiguration.getSection("System_"+k);
      if (!s.isEmpty()) {
        String emuname = s.getString("Name");
        emulators.add(createEmulator(s, pinballXFolder, emuId++, emuname));
      }
    }

    return emulators;
  }

  private String getEmulatorExtension(String emuName) {
    String emu = emuName.replaceAll(" ", "").toLowerCase();
    switch (emu) {
      case "futurepinball": return "";
      case "visualpinball": return "vpx";
      case "zaccaria": return "";
      case "pinballfx2": return "fp2";
      case "pinballfx3": return "fp3";
      case "pinballarcade": return "";
      default: return null;
    }
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
    String parameters = s.getString("Parameters");

    String gameext = null;
    if (s.containsKey("SystemType")) {
      int systemType = s.getInt("SystemType");
      switch (systemType) {
        case 1: gameext = "vpx"; break; // Visual Pinball
        case 2: gameext = "vpx"; break; // Future Pinball
        case 4: gameext = "exe"; break; // Custom Exe
      }
    } else {
      gameext = getEmulatorExtension(emuname);
    }
    
    String launchScript = executable + " " + StringUtils.defaultString(parameters);

    Emulator e = new Emulator();
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    File mediaDir = new File(installDir, "Media/" +emuname);
    if (mediaDir.exists() && mediaDir.isDirectory()) {
      e.setDirMedia(mediaDir.getAbsolutePath());
    }
    e.setDirGames(tablePath);
    if (StringUtils.equals(emuname, "Visual Pinball")) {
      e.setDirRoms(workingPath + "/VPinMAME/roms");
    }
    //e.setDescription(rs.getString("Description"));
    e.setEmuLaunchDir(workingPath);
    e.setLaunchScript(launchScript);
    e.setGamesExt(gameext);
    e.setVisible(enabled);

    return e;
  }

  @Override
  public TableDetails getTableDetails(int id) {
    return this.tabledetails.get(id);
  }

  @Override
  public void updateTableFileUpdated(int id) {
      //String stmt = "UPDATE Games SET DateFileUpdated=? WHERE GameID=?";
  }

  @Override
  public void saveTableDetails(int id, TableDetails tableDetails) {
    this.tabledetails.put(id, tableDetails);
  }

  @Override
  public List<Game> getGames() {
    return games;
  }

  @Override
  public Game getGame(int id) {
    return games.stream().filter(g -> g.getId()==id).findFirst().orElse(null);
  }

  @Override
  public Game getGameByFilename(String filename) {
    return games.stream().filter(g -> g.getGameFileName()==filename).findFirst().orElse(null);
  }

  @Override
  public List<Game> getGamesByEmulator(int emulatorId) {
    return games.stream().filter(g -> g.getEmulatorId()==emulatorId).collect(Collectors.toList());
  }

  @Override
  public List<Game> getGamesByFilename(String filename) {
    String gameName = filename.replaceAll("'", "''");
    return games.stream().filter(g -> StringUtils.containsIgnoreCase(g.getGameFileName(), gameName)).collect(Collectors.toList());
  }

  @Override
  public Game getGameByName(String gameName) {
    return games.stream().filter(g -> g.getGameName()==gameName).findFirst().orElse(null);
  }

  @Override
  public int getSqlVersion() {
    return -1;
  }
  @Override
  public boolean isPopper15() {
    return true;
  }

  //---------------------------

  @Override
  public PopperCustomOptions getCustomOptions() {
    PopperCustomOptions options = new PopperCustomOptions();
    return options;
  }

  @Override
  public void updateCustomOptions(@NonNull PopperCustomOptions options) {
  }

  public void updateRom(@NonNull Game game, String rom) {
    game.setRom(rom);
    // "UPDATE Games SET 'ROM'=? WHERE GameID=?");
  }

  public void updateGamesField(@NonNull Game game, String field, String value) {
    // "UPDATE Games SET '" + field + "'=? WHERE GameID=?");
  }


  @Nullable
  public String getGamesStringValue(@NonNull Game game, @NonNull String field) {
    return null;
  }

@Override
public int importGame(int emulatorId, String gameName, String gameFileName, String gameDisplayName,
      String launchCustomVar, java.util.Date dateFileUpdated) {
  LOG.info("Added game entry for '" + gameName + "', file name '" + gameFileName + "'");
    //updateGamesField(game, "Author", "");
    //updateGamesField(game, "TAGS", "");
    //updateGamesField(game, "Category", "");
    //updateGamesField(game, "MediaSearch", "");
    //updateGamesField(game, "IPDBNum", "");
    //updateGamesField(game, "AltRunMode", "");
    return -1;
  }

  @Override
  public boolean deleteGame(int id) {
    deleteFromPlaylists(id);
    // DELETE FROM Games where GameID = ?
    // DELETE FROM GamesStats where GameID = ?
    return true;
  }

  //----------------------------

  @Override
  public Playlist getPlayList(int id) {
    Playlist playlist = new Playlist();
    playlist.setId(id);
    playlist.setName("name");
    //playlist.setPlayListSQL("sql");
    //playlist.setMenuColor(rs.getInt("MenuColor"));
    //playlist.setSqlPlayList(sqlPlaylist);
    return playlist;
  }

  @Override
  public List<Playlist> getPlayLists(boolean excludeSqlLists) {
    List<Playlist> result = new ArrayList<>();
    return result;
  }

  @Override
  public void setPlaylistColor(int playlistId, long color) {
  }

  @Override
  public void addToPlaylist(int playlistId, int gameId, int favMode) {
  }

  @Override
  public void updatePlaylistGame(int playlistId, int gameId, int favMode) {
  }

  @Override
  public void deleteFromPlaylists(int gameId) {
  }

  @Override
  public void deleteFromPlaylist(int playlistId, int gameId) {
  }

  @Override
  public Playlist getPlayListForGame(int gameId) {
    Playlist result = null;
    return result;
  }

  //-------------------------
  
  @Override
  public java.util.Date getStartDate() {
    return new java.util.Date();
  }
  
  @Override
  public List<TableAlxEntry> getAlxData() {
    List<TableAlxEntry> result = new ArrayList<>();
        /*
        TableAlxEntry e = new TableAlxEntry();
        e.setDisplayName(rs.getString("GameDisplay"));
        e.setGameId(rs.getInt("GameId"));
        e.setUniqueId(rs.getInt("UniqueId"));
        e.setLastPlayed(rs.getDate("LastPlayed"));
        e.setTimePlayedSecs(rs.getInt("TimePlayedSecs"));
        e.setNumberOfPlays(rs.getInt("NumberPlays"));
        result.add(e);
        */
    return result;
  }

  @Override
  public List<TableAlxEntry> getAlxData(int gameId) {
    List<TableAlxEntry> result = new ArrayList<>();
        /*
        TableAlxEntry e = new TableAlxEntry();
        e.setDisplayName(rs.getString("GameDisplay"));
        e.setGameId(rs.getInt("GameId"));
        e.setUniqueId(rs.getInt("UniqueId"));
        e.setLastPlayed(rs.getDate("LastPlayed"));
        e.setTimePlayedSecs(rs.getInt("TimePlayedSecs"));
        e.setNumberOfPlays(rs.getInt("NumberPlays"));
        result.add(e);
        */
    return result;
  }

  //-------------------------

  @Override
  public PinUPControl getFunction(@NonNull String description) {
    PinUPControl f = null;
    return f;
  }

  @Override
  public PinUPControls getControls() {
    PinUPControls controls = new PinUPControls();
    return controls;
  }

  @Override
  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    return null;
  }

  @Override
  public int getGameCount(int emuId) {
    return games.stream().filter(g -> g.getEmulatorId()==emuId).collect(Collectors.counting()).intValue();
  }

  @Override
  public List<Integer> getGameIds(int emuId) {
    return games.stream().filter(g -> g.getEmulatorId()==emuId).map(g -> g.getId()).collect(Collectors.toList());
  }

  

  @Override
  public void deleteGames() {
  }

  @Override
  public List<Integer> getGameIdsFromPlaylists() {
    List<Integer> result = new ArrayList<>();
    return result;
  }

}
