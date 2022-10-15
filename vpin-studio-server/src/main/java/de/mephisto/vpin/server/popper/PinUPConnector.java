package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.jpa.GameDetails;
import de.mephisto.vpin.server.jpa.GameDetailsRepository;
import de.mephisto.vpin.server.roms.RomService;
import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.*;
import java.sql.Date;
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

  private Connection conn;

  @Autowired
  private SystemService systemService;

  @Autowired
  private RomService romService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  @Override
  public void afterPropertiesSet() {
    File file = systemService.getPinUPDatabaseFile();
    dbFilePath = file.getAbsolutePath().replaceAll("\\\\", "/");
    runConfigCheck();
  }

  private void runConfigCheck() {
    List<Emulator> ems = this.getEmulators();
    for (Emulator emulator : ems) {
      if(emulator.getName().equals(Emulator.VISUAL_PINBALL_X)) {
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
  private void connect() {
    try {
      String url = "jdbc:sqlite:" + dbFilePath;
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      LOG.error("Failed to connect to sqlite: " + e.getMessage(), e);
    }
  }

  private void disconnect() {
    if (this.conn != null) {
      try {
        this.conn.close();
      } catch (SQLException e) {
        LOG.error("Error disconnecting from sqlite: " + e.getMessage());
      }
    }
  }

  @Nullable
  public Game getGame(int id) {
    this.connect();
    Game info = null;
    try {
      PreparedStatement statement = conn.prepareStatement("SELECT * FROM Games where GameID = ?");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        info = createGame(rs);
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get game for id '" + id + "': " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return info;
  }

  @Nullable
  public Game getGameByFilename(String filename) {
    this.connect();
    Game info = null;
    try {
      String gameName = filename.replaceAll("'", "''");
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameFileName = '" + gameName + "';");
      while (rs.next()) {
        info = createGame(rs);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game by filename '" + filename + "': " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return info;
  }

  @Nullable
  public Game getGameByName(String table) {
    this.connect();
    Game info = null;
    try {
      String gameName = table.replaceAll("'", "''");
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameDisplay = '" + gameName + "';");
      while (rs.next()) {
        info = createGame(rs);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get game by name '" + table + "': " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return info;
  }

  @NonNull
  public String getStartupScript() {
    String script = null;
    this.connect();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GlobalSettings;");
      rs.next();
      script = rs.getString("StartupBatch");
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read startup script: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }

    if(script == null) {
      script = "";
    }
    return script;
  }

  public void updateStartupScript(@NonNull String content) {
    this.connect();
    try {
      PreparedStatement preparedStatement = conn.prepareStatement("UPDATE GlobalSettings SET 'StartupBatch'=?");
      preparedStatement.setString(1, content);
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Update of startup script successful.");
    } catch (Exception e) {
      LOG.error("Failed to update startup script script:" + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
  }

  @NonNull
  public List<Emulator> getEmulators() {
    this.connect();
    List<Emulator> result = new ArrayList<>();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators;");
      while (rs.next()) {
        Emulator e = new Emulator(null);
        e.setId(rs.getInt("EMUID"));
        e.setName(rs.getString("EmuName"));
        e.setMediaDir(rs.getString("DirMedia"));
        result.add(e);
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get function: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return result;
  }

  @Nullable
  public PinUPControl getFunction(@NonNull String description) {
    PinUPControl f = null;
    this.connect();
    try {
      PreparedStatement statement = conn.prepareStatement("SELECT * FROM PinUPFunctions WHERE Descript = ?");
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
      this.disconnect();
    }
    return f;
  }

  @NonNull
  public List<PinUPControl> getControls() {
    this.connect();
    List<PinUPControl> results = new ArrayList<>();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PinUPFunctions;");
      while (rs.next()) {
        PinUPControl f = new PinUPControl();
        f.setActive(rs.getInt("Active") == 1);
        f.setDescription(rs.getString("Descript"));
        f.setCtrlKey(rs.getInt("CntrlCodes"));
        f.setId(rs.getInt("uniqueID"));
        results.add(f);
      }

      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to functions: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return results;
  }

  public int getGameCount() {
    int count = 0;
    this.connect();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT count(*) as count FROM Games WHERE EMUID = 1;");
      while (rs.next()) {
        count = rs.getInt("count");
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read game count: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return count;
  }

  @NonNull
  public List<Game> getGames() {
    this.connect();
    List<Game> results = new ArrayList<>();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games WHERE EMUID = 1;");
      while (rs.next()) {
        Game info = createGame(rs);
        if (info != null) {
          results.add(info);
        }
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to get games: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }

    results.sort(Comparator.comparing(Game::getGameDisplayName));
    return results;
  }

  @NonNull
  public List<Integer> getGameIdsFromPlaylists() {
    List<Integer> result = new ArrayList<>();
    connect();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PlayListDetails;");

      while (rs.next()) {
        int gameId = rs.getInt("GameID");
        result.add(gameId);
      }
    } catch (SQLException e) {
      LOG.error("Failed to read playlists: " + e.getMessage(), e);
    } finally {
      disconnect();
    }
    return result;
  }

  @Nullable
  public String getEmulatorStartupScript(@NonNull String emuName) {
    String script = null;
    this.connect();
    try {
      emuName = emuName.replaceAll("'", "''");
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators where EmuName = '" + emuName + "';");
      rs.next();
      script = rs.getString(LAUNCH_SCRIPT);
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read startup script or " + emuName + ": " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return script;
  }

  @Nullable
  public String getEmulatorExitScript(@NonNull String emuName) {
    String script = null;
    this.connect();
    try {
      emuName = emuName.replaceAll("'", "''");
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators where EmuName = '" + emuName + "';");
      rs.next();
      script = rs.getString(POST_SCRIPT);
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read exit script or " + emuName + ": " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
    return script;
  }

  public void updateScript(@NonNull String emuName, @NonNull String scriptName, @NonNull String content) {
    this.connect();
    String sql = "UPDATE Emulators SET '" + scriptName + "'='" + content + "' WHERE EmuName = '" + emuName + "';";
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
      LOG.info("Update of " + scriptName + " successful.");
    } catch (Exception e) {
      LOG.error("Failed to update script script " + scriptName + " [" + sql + "]: " + e.getMessage(), e);
    } finally {
      this.disconnect();
    }
  }

  @Nullable
  private Game createGame(@NonNull ResultSet rs) throws SQLException {
    Game game = new Game(systemService);
    int id = rs.getInt("GameID");
    game.setId(id);

    String gameFileName = rs.getString("GameFileName");
    game.setGameFileName(gameFileName);

    String gameDisplayName = rs.getString("GameDisplay");
    game.setGameDisplayName(gameDisplayName);

    int emuId = rs.getInt("EMUID");

    File vpxFile = new File(systemService.getVPXTablesFolder(), gameFileName);
    if (!vpxFile.exists()) {
      LOG.warn("No vpx file " + vpxFile.getAbsolutePath() + " found, ignoring game.");
      return null;
    }
    game.setGameFile(vpxFile);
    loadStats(game);
    loadDetails(game);
    loadEmulator(game, emuId);
    return game;
  }

  private void loadEmulator(@NonNull Game game, int emuId) {
    try {
      PreparedStatement statement = conn.prepareStatement("SELECT * FROM Emulators WHERE EMUID = ?");
      statement.setInt(1, emuId);
      ResultSet rs = statement.executeQuery();
      Emulator emulator = new Emulator(game);
      game.setEmulator(emulator);

      if (rs.next()) {
        emulator.setId(rs.getInt("EMUID"));
        emulator.setName(rs.getString("EmuName"));
        emulator.setMediaDir(rs.getString("DirMedia"));
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      LOG.error("Failed to read emulator info: " + e.getMessage(), e);
    }
  }

  private void loadStats(@NonNull Game game) {
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GamesStats where GameID = " + game.getId() + ";");
      while (rs.next()) {
        int numberPlays = rs.getInt("NumberPlays");
        Date lastPlayed = rs.getDate("LastPlayed");

        game.setLastPlayed(lastPlayed);
        game.setNumberPlays(numberPlays);
      }
    } catch (SQLException e) {
      LOG.error("Failed to read game info: " + e.getMessage(), e);
    }
  }

  private void loadDetails(@NonNull Game game) {
    try {
      GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
      if (gameDetails == null) {
        ScanResult scanResult = romService.scanGameFile(game);

        gameDetails = new GameDetails();
        gameDetails.setPupId(game.getId());
        gameDetails.setRomName(scanResult.getRom());
        gameDetails.setNvOffset(scanResult.getNvOffset());
        gameDetails.setCreatedAt(new java.util.Date());
        gameDetails.setUpdatedAt(new java.util.Date());
        gameDetailsRepository.saveAndFlush(gameDetails);
      }

      game.setRom(gameDetails.getRomName());
      game.setNvOffset(gameDetails.getNvOffset());
    } catch (Exception e) {
      LOG.error("Failed to load details for " + game + ": " + e.getMessage(), e);
    }
  }
}
