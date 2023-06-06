package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.restclient.PinUPControls;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.server.backup.ArchiveUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Date;
import java.sql.*;
import java.util.*;

@Service
public class PinUPConnector implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PinUPConnector.class);

  private final static String CURL_COMMAND_POPPER_START = "curl -X POST --data-urlencode \"system=\" http://localhost:" + SystemService.SERVER_PORT + "/service/popperLaunch";
  private final static String CURL_COMMAND_TABLE_START = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + SystemService.SERVER_PORT + "/service/gameLaunch";
  private final static String CURL_COMMAND_TABLE_EXIT = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + SystemService.SERVER_PORT + "/service/gameExit";

  public static final String POST_SCRIPT = "PostScript";
  public static final String LAUNCH_SCRIPT = "LaunchScript";
  private String dbFilePath;

  @Autowired
  private SystemService systemService;

  private final Map<Integer, Emulator> emulators = new HashMap<>();

  @Override
  public void afterPropertiesSet() {
    File file = systemService.getPinUPDatabaseFile();
    dbFilePath = file.getAbsolutePath().replaceAll("\\\\", "/");
    runConfigCheck();
  }

  private void runConfigCheck() {
    List<Emulator> ems = this.getEmulators();
    for (Emulator emulator : ems) {
      emulators.put(emulator.getId(), emulator);

      String name = emulator.getName();
      if (name.equals(Emulator.VISUAL_PINBALL) || name.equals(Emulator.VISUAL_PINBALL_X) || name.equals(Emulator.FUTURE_PINBALL) || name.equals(Emulator.PINBALL_FX3)) {
        initVisualPinballXScripts(emulator);
      }
    }
    LOG.info("Finished Popper scripts configuration check.");
  }

  private void initVisualPinballXScripts(Emulator emulator) {
    String emulatorName = emulator.getName();
    String emulatorStartupScript = this.getEmulatorStartupScript(emulatorName);
    if (emulatorStartupScript != null && !emulatorStartupScript.contains(CURL_COMMAND_TABLE_START)) {
      emulatorStartupScript = emulatorStartupScript + "\n\n" + CURL_COMMAND_TABLE_START;
      this.updateScript(emulatorName, "LaunchScript", emulatorStartupScript);
    }
    String emulatorExitScript = this.getEmulatorExitScript(emulatorName);
    if (emulatorExitScript != null && !emulatorExitScript.contains(CURL_COMMAND_TABLE_EXIT)) {
      emulatorExitScript = emulatorExitScript + "\n\n" + CURL_COMMAND_TABLE_EXIT;
      this.updateScript(emulatorName, "PostScript", emulatorExitScript);
    }

    String startupScript = this.getStartupScript();
    if (!startupScript.contains(CURL_COMMAND_POPPER_START)) {
      startupScript = startupScript + "\n" + CURL_COMMAND_POPPER_START + "\n";
      this.updateStartupScript(startupScript);
    }
  }

  /**
   * Connect to a database
   */
  private Connection connect() {
    try {
      String url = "jdbc:sqlite:" + dbFilePath;
      return DriverManager.getConnection(url);
    } catch (SQLException e) {
      LOG.error("Failed to connect to sqlite: " + e.getMessage(), e);
    }
    return null;
  }

  private void disconnect(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        LOG.error("Error disconnecting from sqlite: " + e.getMessage());
      }
    }
  }

  @Nullable
  public Game getGame(int id) {
    Connection connect = connect();
    Game info = null;
    try {
      PreparedStatement statement = connect.prepareStatement("SELECT * FROM Games where GameID = ?");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        info = createGame(connect, rs);
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get game for id '" + id + "': " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return info;
  }

  @Nullable
  public TableDetails getTableDetails(int id) {
    Connection connect = connect();
    TableDetails manifest = null;
    try {
      PreparedStatement statement = connect.prepareStatement("SELECT * FROM Games where GameID = ?");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        manifest = new TableDetails();
        manifest.setEmulatorType(rs.getString("GameType"));
        manifest.setGameName(rs.getString("GameName"));
        manifest.setGameFileName(rs.getString("GameFileName"));
        manifest.setGameDisplayName(rs.getString("GameDisplay"));
        manifest.setNotes(rs.getString("Notes"));
        manifest.setGameYear(rs.getInt("GameYear"));
        manifest.setRomName(rs.getString("ROM"));
        manifest.setManufacturer(rs.getString("Manufact"));
        manifest.setNumberOfPlayers(rs.getInt("NumPlayers"));
        manifest.setTags(rs.getString("TAGS"));
        manifest.setVolume(rs.getString("sysVolume"));
        manifest.setCategory(rs.getString("Category"));
        manifest.setAuthor(rs.getString("Author"));
        manifest.setLaunchCustomVar(rs.getString("LaunchCustomVar"));
        manifest.setKeepDisplays(rs.getString("GKeepDisplays"));
        manifest.setGameTheme(rs.getString("GameTheme"));
        manifest.setGameRating(rs.getInt("GameRating"));
        manifest.setDof(rs.getString("DOFStuff"));
        manifest.setIPDBNum(rs.getString("IPDBNum"));
        manifest.setAltRunMode(rs.getString("AltRunMode"));
        manifest.setUrl(rs.getString("WebLinkURL"));
        manifest.setDesignedBy(rs.getString("DesignedBy"));

      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get game for id '" + id + "': " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return manifest;
  }

  @Nullable
  public Game getGameByFilename(String filename) {
    Connection connect = this.connect();
    Game info = null;
    try {
      String gameName = filename.replaceAll("'", "''");
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameFileName = '" + gameName + "';");
      while (rs.next()) {
        info = createGame(connect, rs);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game by filename '" + filename + "': " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return info;
  }

  @Nullable
  public Game getGameByName(String gameName) {
    Connection connect = this.connect();
    Game info = null;
    try {
      gameName = gameName.replaceAll("'", "''");
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameName = '" + gameName + "';");
      while (rs.next()) {
        info = createGame(connect, rs);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game by gameName '" + gameName + "': " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return info;
  }

  @NonNull
  public String getStartupScript() {
    String script = null;
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GlobalSettings;");
      rs.next();
      script = rs.getString("StartupBatch");
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read startup script: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }

    if (script == null) {
      script = "";
    }
    return script;
  }

  public void updateStartupScript(@NonNull String content) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("UPDATE GlobalSettings SET 'StartupBatch'=?");
      preparedStatement.setString(1, content);
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Update of startup script successful.");
    } catch (Exception e) {
      LOG.error("Failed to update startup script script:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public void updateRom(@NonNull Game game, String rom) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("UPDATE Games SET 'ROM'=? WHERE GameID=?");
      preparedStatement.setString(1, rom);
      preparedStatement.setInt(2, game.getId());
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Updated of ROM of " + game + " to " + rom);
    } catch (Exception e) {
      LOG.error("Failed to update ROM:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public void updateVolume(@NonNull Game game, int volume) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("UPDATE Games SET 'sysVolume'=? WHERE GameID=?");
      preparedStatement.setInt(1, volume);
      preparedStatement.setInt(2, game.getId());
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Updated of volume of " + game + " to " + volume);
    } catch (Exception e) {
      LOG.error("Failed to update volume:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public int importGame(@NonNull File file) {
    String emulator = ArchiveUtil.getEmulatorType(file);
    String name = FilenameUtils.getBaseName(file.getName());
    String gameFileName = file.getName();
    String gameDisplayName = name.replaceAll("-", " ").replaceAll("_", " ");
    return importGame(emulator, name, gameFileName, gameDisplayName, null);
  }

  /**
   * Creates a new entry in the PinUP Popper database.
   * Returns the id of the new entry.
   *
   * @return the generated game id.
   */
  public int importGame(@NonNull String emulator, @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName, @Nullable String launchCustomVar) {
    Connection connect = this.connect();
    int emulatorId = getEmulatorId(emulator);
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO Games (EMUID, GameName, GameFileName, GameDisplay, Visible, LaunchCustomVar) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setInt(1, emulatorId);
      preparedStatement.setString(2, gameName);
      preparedStatement.setString(3, gameFileName);
      preparedStatement.setString(4, gameDisplayName);
      preparedStatement.setInt(5, 1);
      preparedStatement.setString(6, launchCustomVar);
//      preparedStatement.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
      int affectedRows = preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Added game entry for '" + gameFileName + "'");
      try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
        if (keys.next()) {
          return keys.getInt(1);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to update game table:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return -1;
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
    deleteFromPlaylists(id);
    deleteFromGames(id);
    deleteStats(id);
    return true;
  }

  private void deleteFromGames(int gameId) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("DELETE FROM Games where GameID = ?");
      preparedStatement.setInt(1, gameId);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Deleted game entry with id " + gameId);
    } catch (Exception e) {
      LOG.error("Failed to update game table:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  private void deleteStats(int gameId) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("DELETE FROM GamesStats where GameID = ?");
      preparedStatement.setInt(1, gameId);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Deleted game stats entry with id " + gameId);
    } catch (Exception e) {
      LOG.error("Failed to update game stats table:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  @NonNull
  public List<Playlist> getPlayLists() {
    Connection connect = this.connect();
    List<Playlist> result = new ArrayList<>();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Playlists WHERE Visible = 1;");
      while (rs.next()) {
        String sql = rs.getString("PlayListSQL");
        if (!StringUtils.isEmpty(sql)) {
          continue;
        }

        Playlist playlist = new Playlist();
        playlist.setId(rs.getInt("PlayListID"));
        playlist.setName(rs.getString("PlayName"));
        result.add(playlist);
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get playlist: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return result;
  }

  public void addToPlaylist(int gameId, int playlistId) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO PlayListDetails (PlayListID, GameID, Visible, DisplayOrder, NumPlayed) VALUES (?,?,?,?,?)");
      preparedStatement.setInt(1, playlistId);
      preparedStatement.setInt(2, gameId);
      preparedStatement.setInt(3, 1);
      preparedStatement.setInt(4, 0);
      preparedStatement.setInt(5, 0);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Added game " + gameId + " to playlist " + playlistId);
    } catch (SQLException e) {
      LOG.error("Failed to update playlist details: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public void deleteFromPlaylists(int gameId) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("DELETE FROM PlayListDetails WHERE GameID = ?");
      preparedStatement.setInt(1, gameId);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Removed game " + gameId + " from all playlists");
    } catch (SQLException e) {
      LOG.error("Failed to update playlist details: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public Playlist getPlayListForGame(int gameId) {
    Playlist result = null;
    Connection connect = connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PlayListDetails WHERE GameID = " + gameId);
      while (rs.next()) {
        Playlist playlist = new Playlist();
        playlist.setId(rs.getInt("PlayListID"));
        result = playlist;
        break;
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read playlist for gameId: " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return result;
  }

  @NonNull
  public List<Emulator> getEmulators() {
    Connection connect = this.connect();
    List<Emulator> result = new ArrayList<>();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators;");
      while (rs.next()) {
        Emulator e = new Emulator();
        e.setId(rs.getInt("EMUID"));
        e.setName(rs.getString("EmuName"));
        e.setMediaDir(rs.getString("DirMedia"));
        e.setVisible(rs.getInt("Visible") == 1);
        result.add(e);
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get function: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return result;
  }


  public void enablePCGameEmulator() {
    List<Emulator> ems = this.getEmulators();
    for (Emulator em : ems) {
      if (em.getName().equals(EmulatorType.PC_GAMES) && em.isVisible()) {
        return;
      }
    }

    Connection connect = this.connect();
    String sql = "UPDATE Emulators SET 'VISIBLE'=1 WHERE EmuName = '" + Emulator.PC_GAMES + "';";
    try {
      Statement stmt = connect.createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
      LOG.info("Enabled PC Games emulator for popper.");
    } catch (Exception e) {
      LOG.error("Failed to update script script [" + sql + "]: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  @Nullable
  private PinUPControl getFunction(@NonNull String description) {
    PinUPControl f = null;
    Connection connect = this.connect();
    try {
      PreparedStatement statement = connect.prepareStatement("SELECT * FROM PinUPFunctions WHERE Descript = ?");
      statement.setString(1, description);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        f = new PinUPControl();
        f.setActive(rs.getInt("Active") == 1);
        f.setDescription(rs.getString("Descript"));
        f.setCtrlKey(rs.getInt("CntrlCodes"));
        f.setId(rs.getInt("uniqueID"));
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get function: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return f;
  }


  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    PinUPControl fn = null;
    switch (screen) {
      case Other2: {
        return getFunction(PinUPControl.FUNCTION_SHOW_OTHER);
      }
      case GameHelp: {
        return getFunction(PinUPControl.FUNCTION_SHOW_HELP);
      }
      case GameInfo: {
        return getFunction(PinUPControl.FUNCTION_SHOW_FLYER);
      }
      default: {

      }
    }

    return new PinUPControl();
  }

  @NonNull
  public PinUPControls getControls() {
    PinUPControls controls = new PinUPControls();
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PinUPFunctions;");
      while (rs.next()) {
        PinUPControl f = new PinUPControl();
        f.setActive(rs.getInt("Active") == 1);
        f.setDescription(rs.getString("Descript"));
        f.setCtrlKey(rs.getInt("CntrlCodes"));
        f.setId(rs.getInt("uniqueID"));
        controls.addControl(f);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to functions: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return controls;
  }

  public int getGameCount() {
    int count = 0;
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT count(*) as count FROM Games WHERE EMUID = 1;");
      while (rs.next()) {
        count = rs.getInt("count");
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game count: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return count;
  }

  public List<Integer> getGameIds() {
    List<Integer> result = new ArrayList<>();
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT GameID FROM Games WHERE EMUID = 1;");
      while (rs.next()) {
        result.add(rs.getInt("GameID"));
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game count: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return result;
  }

  @NonNull
  public List<Game> getGames() {
    Connection connect = this.connect();
    List<Game> results = new ArrayList<>();
    try {
      Statement statement = connect.createStatement();
      //ResultSet rs = statement.executeQuery("SELECT * from GAMES g LEFT JOIN GamesStats stats ON stats.GameID = g.GameID  WHERE g.EMUID = 1;");
      ResultSet rs = statement.executeQuery("SELECT * FROM Games WHERE EMUID = 1;");
      while (rs.next()) {
        Game info = createGame(connect, rs);
        if (info != null) {
          results.add(info);
        }
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get games: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }

    results.sort(Comparator.comparing(Game::getGameDisplayName));
    return results;
  }

  @NonNull
  public List<Integer> getGameIdsFromPlaylists() {
    List<Integer> result = new ArrayList<>();
    Connection connect = connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PlayListDetails;");

      while (rs.next()) {
        int gameId = rs.getInt("GameID");
        result.add(gameId);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read playlists: " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return result;
  }

  @Nullable
  public String getEmulatorStartupScript(@NonNull String emuName) {
    String script = null;
    Connection connect = this.connect();
    try {
      emuName = emuName.replaceAll("'", "''");
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators where EmuName = '" + emuName + "';");
      rs.next();
      script = rs.getString(LAUNCH_SCRIPT);
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read startup script or " + emuName + ": " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return script;
  }

  @Nullable
  public String getEmulatorExitScript(@NonNull String emuName) {
    String script = null;
    Connection connect = this.connect();
    try {
      emuName = emuName.replaceAll("'", "''");
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators where EmuName = '" + emuName + "';");
      rs.next();
      script = rs.getString(POST_SCRIPT);
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read exit script or " + emuName + ": " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return script;
  }

  public void updateScript(@NonNull String emuName, @NonNull String scriptName, @NonNull String content) {
    Connection connect = this.connect();
    String sql = "UPDATE Emulators SET '" + scriptName + "'='" + content + "' WHERE EmuName = '" + emuName + "';";
    try {
      Statement stmt = connect.createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
      LOG.info("Update of " + scriptName + " for '" + emuName + "' successful.");
    } catch (Exception e) {
      LOG.error("Failed to update script script " + scriptName + " [" + sql + "]: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  @Nullable
  private Game createGame(@NonNull Connection connection, @NonNull ResultSet rs) throws SQLException {
    Game game = new Game(systemService);
    int id = rs.getInt("GameID");
    game.setId(id);

    String gameFileName = rs.getString("GameFileName");
    game.setGameFileName(gameFileName);

    String rom = rs.getString("ROM");
    game.setRom(rom);

    String gameDisplayName = rs.getString("GameDisplay");
    game.setGameDisplayName(gameDisplayName);

    game.setCustom2(rs.getString("CUSTOM2"));
    game.setCustom3(rs.getString("CUSTOM3"));

    int emuId = rs.getInt("EMUID");
    Emulator emulator = emulators.get(emuId);
    game.setEmulator(emulator);

    if (emulator.getName().equalsIgnoreCase(Emulator.VISUAL_PINBALL_X) || emulator.getName().equalsIgnoreCase(Emulator.VISUAL_PINBALL)) {
      File vpxFile = new File(systemService.getVPXTablesFolder(), gameFileName);
      game.setGameFile(vpxFile);
    }
    else if (emulator.getName().equalsIgnoreCase(Emulator.FUTURE_PINBALL)) {
      File fpFile = new File(systemService.getFuturePinballTablesFolder(), gameFileName);
      if (!fpFile.exists()) {
        return null;
      }
      game.setGameFile(fpFile);
    }

    loadStats(connection, game);
    return game;
  }

  private void loadStats(@NonNull Connection connection, @NonNull Game game) {
    try {
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GamesStats where GameID = " + game.getId() + ";");
      while (rs.next()) {
        int numberPlays = rs.getInt("NumberPlays");
        Date lastPlayed = rs.getDate("LastPlayed");

        game.setLastPlayed(lastPlayed);
        game.setNumberPlays(numberPlays);
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game info: " + e.getMessage(), e);
    }
  }

  private int getEmulatorId(@NonNull String name) {
    Set<Map.Entry<Integer, Emulator>> entries = this.emulators.entrySet();
    for (Map.Entry<Integer, Emulator> entry : entries) {
      if (entry.getValue().getName().equals(name)) {
        return entry.getKey();
      }
    }
    throw new UnsupportedOperationException("Failed to determine emulator id for '" + name + "'");
  }


  public void saveTableDetails(Game game, TableDetails manifest) {
    int id = game.getId();
    importManifestValue(id, "GameName", manifest.getGameName());
    importManifestValue(id, "GameDisplay", manifest.getGameDisplayName());
    importManifestValue(id, "GameFileName", manifest.getGameFileName());
    importManifestValue(id, "GameTheme", manifest.getGameTheme());
    importManifestValue(id, "Notes", manifest.getNotes());
    importManifestValue(id, "GameYear", manifest.getGameYear());
    importManifestValue(id, "ROM", manifest.getRomName());
    importManifestValue(id, "Manufact", manifest.getManufacturer());
    importManifestValue(id, "NumPlayers", manifest.getNumberOfPlayers());
    importManifestValue(id, "TAGS", manifest.getTags());
    importManifestValue(id, "Category", manifest.getCategory());
    importManifestValue(id, "Author", manifest.getAuthor());
    importManifestValue(id, "sysVolume", manifest.getVolume());
    importManifestValue(id, "LaunchCustomVar", manifest.getLaunchCustomVar());
    importManifestValue(id, "GKeepDisplays", manifest.getKeepDisplays());
    importManifestValue(id, "GameRating", manifest.getGameRating());
    importManifestValue(id, "DOFStuff", manifest.getDof());
    importManifestValue(id, "IPDBNum", manifest.getIPDBNum());
    importManifestValue(id, "AltRunMode", manifest.getAltRunMode());
    importManifestValue(id, "WebLinkURL", manifest.getUrl());
    importManifestValue(id, "DesignedBy", manifest.getDesignedBy());
  }

  private void importManifestValue(int gameId, String field, Object value) {
    if (value == null) {
      return;
    }

    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("UPDATE Games SET '" + field + "'=? WHERE GameID=?");
      preparedStatement.setObject(1, value);
      preparedStatement.setInt(2, gameId);
      preparedStatement.executeUpdate();
      preparedStatement.close();
    } catch (Exception e) {
      LOG.error("Failed to update game manifest value'" + field + "' (" + value + "):" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }
}
