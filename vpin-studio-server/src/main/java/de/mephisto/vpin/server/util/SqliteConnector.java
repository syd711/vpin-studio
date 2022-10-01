package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.GameInfo;
import de.mephisto.vpin.server.VPinService;
import de.mephisto.vpin.server.popper.PinUPControl;
import de.mephisto.vpin.server.roms.RomManager;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SqliteConnector {
  private final static Logger LOG = LoggerFactory.getLogger(SqliteConnector.class);

  public static final String POST_SCRIPT = "PostScript";
  public static final String LAUNCH_SCRIPT = "LaunchScript";
  public static final String ROM = "ROM";
  private final String dbFilePath;

  private Connection conn;
  private RomManager romManager;

  public SqliteConnector(RomManager romManager) {
    this(SystemInfo.getInstance().getPinUPDatabaseFile());
    this.romManager = romManager;
  }

  public SqliteConnector(File file) {
    dbFilePath = file.getAbsolutePath().replaceAll("\\\\", "/");
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
  public GameInfo getGame(@NonNull VPinService service, int id) {
    this.connect();
    GameInfo info = null;
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameID = " + id + ";");
      while (rs.next()) {
        info = createGameInfo(service, rs);
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
  public GameInfo getGameByFilename(@NonNull VPinService service, String filename) {
    this.connect();
    GameInfo info = null;
    try {
      String gameName = filename.replaceAll("'", "''");
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameFileName = '" + gameName + "';");
      while (rs.next()) {
        info = createGameInfo(service, rs);
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
  public GameInfo getGameByName(@NonNull VPinService service, String table) {
    this.connect();
    GameInfo info = null;
    try {
      String gameName = table.replaceAll("'", "''");
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games where GameDisplay = '" + gameName + "';");
      while (rs.next()) {
        info = createGameInfo(service, rs);
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
      while (rs.next()) {
        f = new PinUPControl();
        f.setActive(rs.getInt("Active") == 1);
        f.setDescription(rs.getString("Descript"));
        f.setCtrlKey(rs.getInt("CntrlCodes"));
        f.setId(rs.getInt("uniqueID"));
        break;
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
  public List<GameInfo> getGames(@NonNull VPinService service) {
    this.connect();
    List<GameInfo> results = new ArrayList<>();
    try {
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Games WHERE EMUID = 1;");
      while (rs.next()) {
        GameInfo info = createGameInfo(service, rs);
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

    results.sort(Comparator.comparing(GameInfo::getGameDisplayName));
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

  private void loadStats(@NonNull GameInfo game) {
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

  @Nullable
  private GameInfo createGameInfo(@NonNull VPinService service, @NonNull ResultSet rs) throws SQLException {
    GameInfo info = new GameInfo(service);

    int id = rs.getInt("GameID");
    info.setId(id);

    String gameFileName = rs.getString("GameFileName");
    info.setGameFileName(gameFileName);

    String gameDisplayName = rs.getString("GameDisplay");
    info.setGameDisplayName(gameDisplayName);

    File wheelIconFile = new File(SystemInfo.getInstance().getPinUPSystemFolder() + "/POPMedia/Visual Pinball X/Wheel/", FilenameUtils.getBaseName(gameFileName) + ".png");
    info.setWheelIconFile(wheelIconFile);

    File vpxFile = new File(SystemInfo.getInstance().getVPXTablesFolder(), gameFileName);
    if (!vpxFile.exists()) {
      LOG.warn("No vpx file " + vpxFile.getAbsolutePath() + " found, ignoring game.");
      return null;
    }
    info.setGameFile(vpxFile);


    String rom = romManager.getRomName(id);
    File romFile = null;
    File nvRamFile = null;
    File nvRamFolder = new File(SystemInfo.getInstance().getMameFolder(), "nvram");
    if (!StringUtils.isEmpty(rom)) {
      romFile = new File(SystemInfo.getInstance().getMameRomFolder(), rom + ".zip");
      nvRamFile = new File(nvRamFolder, rom + ".nv");
    }
    else if (!romManager.wasScanned(id)) {
      rom = romManager.scanRom(info);
    }

    info.setRom(rom);
    info.setNvRamFile(nvRamFile);
    info.setRomFile(romFile);

    loadStats(info);
    return info;
  }
}
