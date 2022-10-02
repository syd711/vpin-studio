package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.jpa.GameDetails;
import de.mephisto.vpin.server.jpa.GameDetailsRepository;
import de.mephisto.vpin.server.popper.Emulators;
import de.mephisto.vpin.server.popper.PinUPControl;
import de.mephisto.vpin.server.roms.RomService;
import de.mephisto.vpin.server.system.SystemInfo;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PinUPConnector implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PinUPConnector.class);

  private final static String CURL_COMMAND_TABLE_START = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + SystemInfo.SERVER_PORT + "/service/gameLaunch";
  private final static String CURL_COMMAND_TABLE_EXIT = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + SystemInfo.SERVER_PORT + "/service/gameExit";

  public static final String POST_SCRIPT = "PostScript";
  public static final String LAUNCH_SCRIPT = "LaunchScript";
  private String dbFilePath;

  private Connection conn;

  @Autowired
  private SystemInfo systemInfo;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  @Override
  public void afterPropertiesSet() {
    File file = systemInfo.getPinUPDatabaseFile();
    dbFilePath = file.getAbsolutePath().replaceAll("\\\\", "/");
    runConfigCheck();
  }

  private void runConfigCheck() {
    Emulators[] values = Emulators.values();
    for (Emulators value : values) {
      String emulatorName = Emulators.getEmulatorName(value);
      String startupScript = this.getEmulatorStartupScript(emulatorName);
      if (startupScript != null && !startupScript.contains(CURL_COMMAND_TABLE_START)) {
        startupScript = startupScript + "\n\n" + CURL_COMMAND_TABLE_START;
        this.updateScript(emulatorName, "LaunchScript", startupScript);
      }
      String emulatorExitScript = this.getEmulatorExitScript(Emulators.getEmulatorName(value));
      if (emulatorExitScript != null && !emulatorExitScript.contains(CURL_COMMAND_TABLE_EXIT)) {
        emulatorExitScript = emulatorExitScript + "\n\n" + CURL_COMMAND_TABLE_EXIT;
        this.updateScript(emulatorName, "PostScript", emulatorExitScript);
      }
    }
    LOG.info("Finished Popper scripts configuration check.");
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
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameID = " + id + ";");
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

  @Nullable
  public PinUPControl getFunction(@NonNull String description) {
    PinUPControl f = null;
    this.connect();
    try {
      Statement statement = conn.createStatement();
      description = description.replaceAll("'", "''");
      ResultSet rs = statement.executeQuery("SELECT * FROM PinUPFunctions WHERE Descript = '" + description + "';");
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
    Game game = new Game(systemInfo);
    int id = rs.getInt("GameID");
    game.setId(id);

    String gameFileName = rs.getString("GameFileName");
    game.setGameFileName(gameFileName);

    String gameDisplayName = rs.getString("GameDisplay");
    game.setGameDisplayName(gameDisplayName);

    File wheelIconFile = new File(systemInfo.getPinUPSystemFolder() + "/POPMedia/Visual Pinball X/Wheel/", FilenameUtils.getBaseName(gameFileName) + ".png");
    game.setWheelIconFile(wheelIconFile);

    File vpxFile = new File(systemInfo.getVPXTablesFolder(), gameFileName);
    if (!vpxFile.exists()) {
      LOG.warn("No vpx file " + vpxFile.getAbsolutePath() + " found, ignoring game.");
      return null;
    }
    game.setGameFile(vpxFile);
    loadStats(game);
    loadDetails(game);
    return game;
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
      GameDetails details = gameDetailsRepository.findByPupId(game.getId());
      String rom = null;
      if (details == null) {
        RomService scanner = new RomService();
        rom = scanner.scanRom(game);
        GameDetails gameDetails = new GameDetails();
        gameDetails.setPupId(game.getId());
        gameDetails.setRomName(rom);
        gameDetails.setCreatedAt(new java.util.Date());
        gameDetails.setUpdatedAt(new java.util.Date());
        gameDetailsRepository.saveAndFlush(gameDetails);
      }
      else {
        rom = details.getRomName();
      }

      game.setRom(rom);
      if (!StringUtils.isEmpty(rom)) {
        File romFile = new File(systemInfo.getMameRomFolder(), rom + ".zip");
        game.setRomFile(romFile);

        File nvRamFolder = new File(systemInfo.getMameFolder(), "nvram");
        game.setNvRamFile(new File(nvRamFolder, rom + ".nv"));
      }
    } catch (Exception e) {
      LOG.error("Failed to parse ROM data of " + game + ": " + e.getMessage(), e);
    }
  }
}
