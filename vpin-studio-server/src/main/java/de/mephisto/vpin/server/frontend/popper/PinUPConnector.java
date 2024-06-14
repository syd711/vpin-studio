package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("Popper")
public class PinUPConnector implements FrontendConnector {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendService.class);

  private final static String CURL_COMMAND_POPPER_START = "curl -X POST --data-urlencode \"system=\" http://localhost:" + SystemService.SERVER_PORT + "/service/popperLaunch";
  private final static String CURL_COMMAND_TABLE_START = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + SystemService.SERVER_PORT + "/service/gameLaunch";
  private final static String CURL_COMMAND_TABLE_EXIT = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + SystemService.SERVER_PORT + "/service/gameExit";

  public static final String POST_SCRIPT = "PostScript";
  public static final String LAUNCH_SCRIPT = "LaunchScript";
  public static final int DB_VERSION = 64;
  public static final String IS_FAV = "isFav";
  public static final String POPPER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  private String dbFilePath;

  @Autowired
  private SystemService systemService;

  private int sqlVersion = DB_VERSION;
  
  private ServerSettings serverSettings;

  private void initVisualPinballXScripts(Emulator emulator) {
    String emulatorName = emulator.getName();
    String emulatorStartupScript = this.getEmulatorStartupScript(emulatorName);
    if (!emulatorStartupScript.contains(CURL_COMMAND_TABLE_START)) {
      emulatorStartupScript = emulatorStartupScript + "\n\n" + CURL_COMMAND_TABLE_START;
      this.updateScript(emulatorName, "LaunchScript", emulatorStartupScript);
    }
    String emulatorExitScript = this.getEmulatorExitScript(emulatorName);
    if (!emulatorExitScript.contains(CURL_COMMAND_TABLE_EXIT)) {
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
    }
    catch (SQLException e) {
      LOG.error("Failed to connect to sqlite: " + e.getMessage(), e);
    }
    return null;
  }

  private void disconnect(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      }
      catch (SQLException e) {
        LOG.error("Error disconnecting from sqlite: " + e.getMessage());
      }
    }
  }

  @Nullable
  @Override
  public Game getGame(int id) {
    Connection connect = connect();
    Game info = null;
    try {
      PreparedStatement statement = connect.prepareStatement(
        "SELECT g.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
        + " left join Emulators e on g.EMUID=e.EMUID where g.GameID = ?");

      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        info = createGame(rs);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get game for id '" + id + "': " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return info;
  }

  @NonNull
  public List<String> getAltExeList() {
    Connection connect = connect();
    try {
      PreparedStatement statement = connect.prepareStatement("SELECT Altexe FROM PupLookups");
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        String altExe = rs.getString("Altexe");
        if (!StringUtils.isEmpty(altExe)) {
          String[] split = altExe.split("\\r\\n");
          return Arrays.asList(split);
        }
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to load altexe list: " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }

    return Collections.emptyList();
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
        manifest.setSqlVersion(sqlVersion);

        manifest.setEmulatorId(rs.getInt("EMUID"));
        manifest.setEmulatorType(rs.getString("GameType"));
        manifest.setGameName(rs.getString("GameName"));
        manifest.setGameFileName(rs.getString("GameFileName"));
        manifest.setGameDisplayName(rs.getString("GameDisplay"));
        manifest.setGameVersion(rs.getString("GAMEVER"));
        manifest.setDateAdded(rs.getTimestamp("DateAdded"));
        manifest.setNotes(rs.getString("Notes"));
        manifest.setGameYear(rs.getInt("GameYear"));
        if (rs.wasNull()) {
          manifest.setGameYear(null);
        }
        String gameType = rs.getString("GameType");
        if (gameType != null && (gameType.equals(GameType.SS.name()) || gameType.equals(GameType.EM.name()) || gameType.equals(GameType.Original.name()))) {
          manifest.setGameType(GameType.valueOf(gameType));
        }

        manifest.setRomName(rs.getString("ROM"));
        manifest.setManufacturer(rs.getString("Manufact"));
        manifest.setNumberOfPlayers(rs.getInt("NumPlayers"));
        if (rs.wasNull()) {
          manifest.setNumberOfPlayers(null);
        }
        manifest.setStatus(rs.getInt("Visible"));
        if (rs.wasNull()) {
          manifest.setStatus(0);
        }
        manifest.setTags(rs.getString("TAGS"));
        manifest.setVolume(rs.getString("sysVolume"));
        manifest.setCategory(rs.getString("Category"));
        manifest.setAuthor(rs.getString("Author"));
        manifest.setLaunchCustomVar(rs.getString("LaunchCustomVar"));
        manifest.setCustom2(rs.getString("CUSTOM2"));
        manifest.setCustom3(rs.getString("CUSTOM3"));
        manifest.setKeepDisplays(rs.getString("GKeepDisplays"));
        manifest.setGameTheme(rs.getString("GameTheme"));
        manifest.setGameRating(rs.getInt("GameRating"));
        if (rs.wasNull()) {
          manifest.setGameRating(null);
        }
        manifest.setDof(rs.getString("DOFStuff"));
        manifest.setIPDBNum(rs.getString("IPDBNum"));
        manifest.setAltRunMode(rs.getString("AltRunMode"));
        manifest.setUrl(rs.getString("WebLinkURL"));
        manifest.setDesignedBy(rs.getString("DesignedBy"));
        manifest.setMediaSearch(rs.getString("MediaSearch"));
        manifest.setSpecial(rs.getString("Special"));

        manifest.setAltLaunchExe(rs.getString("ALTEXE"));

        //check for popper DB update 1.5
        if (sqlVersion >= DB_VERSION) {
          manifest.setWebGameId(rs.getString("WEBGameID"));
          manifest.setRomAlt(rs.getString("ROMALT"));
          manifest.setMod(rs.getInt("ISMOD") == 1);
          manifest.setWebLink2Url(rs.getString("WebLink2URL"));
          manifest.setTourneyId(rs.getString("TourneyID"));
          manifest.setCustom4(rs.getString("CUSTOM4"));
          manifest.setCustom5(rs.getString("CUSTOM5"));
        }
      }
      rs.close();
      statement.close();

      if (manifest != null) {
        loadStats(connect, manifest, id);

        if (manifest.isPopper15()) {
          loadGameExtras(connect, manifest, id);
        }
      }
    }
    catch (SQLException e) {
      LOG.error("Failed to get game for id '" + id + "': " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return manifest;
  }

  public void updateTableFileUpdated(int id) {
    Connection connect = this.connect();
    try {
      String stmt = "UPDATE Games SET DateFileUpdated=? WHERE GameID=?";
      PreparedStatement preparedStatement = connect.prepareStatement(stmt);

      SimpleDateFormat sdf = new SimpleDateFormat(POPPER_DATE_FORMAT);
      Timestamp timestamp = new Timestamp(System.currentTimeMillis());
      String ts = sdf.format(timestamp);
      preparedStatement.setObject(1, ts);
      preparedStatement.setInt(2, id);

      preparedStatement.executeUpdate();
      preparedStatement.close();
    }
    catch (Exception e) {
      LOG.error("Failed to save table details: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public void saveTableDetails(int id, TableDetails tableDetails) {
    Connection connect = this.connect();
    try {
      StringBuilder stmtBuilder = new StringBuilder("UPDATE Games SET ");
      List<Object> params = new ArrayList<>();

      stmtBuilder.append("'EMUID' = ?, ");
      params.add(tableDetails.getEmulatorId());
      stmtBuilder.append("'GameName' = ?, ");
      params.add(tableDetails.getGameName());
      stmtBuilder.append("'GameDisplay' = ?, ");
      params.add(tableDetails.getGameDisplayName());
      stmtBuilder.append("'GameFileName' = ?, ");
      params.add(tableDetails.getGameFileName());
      stmtBuilder.append("'GameTheme' = ?, ");
      params.add(tableDetails.getGameTheme());
      stmtBuilder.append("'Notes' = ?, ");
      params.add(tableDetails.getNotes());
      stmtBuilder.append("'GameYear' = ?, ");
      params.add(tableDetails.getGameYear());
      stmtBuilder.append("'ROM' = ?, ");
      params.add(tableDetails.getRomName());
      stmtBuilder.append("'Manufact' = ?, ");
      params.add(tableDetails.getManufacturer());
      stmtBuilder.append("'NumPlayers' = ?, ");
      params.add(tableDetails.getNumberOfPlayers());
      stmtBuilder.append("'TAGS' = ?, ");
      params.add(tableDetails.getTags() != null ? tableDetails.getTags() : "");
      stmtBuilder.append("'Category' = ?, ");
      params.add(tableDetails.getCategory() != null ? tableDetails.getCategory() : "");
      stmtBuilder.append("'Author' = ?, ");
      params.add(tableDetails.getAuthor() != null ? tableDetails.getAuthor() : "");
      stmtBuilder.append("'sysVolume' = ?, ");
      params.add(tableDetails.getVolume());
      stmtBuilder.append("'LaunchCustomVar' = ?, ");
      params.add(tableDetails.getLaunchCustomVar());
      stmtBuilder.append("'GKeepDisplays' = ?, ");
      params.add(tableDetails.getKeepDisplays());
      stmtBuilder.append("'GameRating' = ?, ");
      params.add(tableDetails.getGameRating());
      stmtBuilder.append("'ALTEXE' = ?, ");
      params.add(tableDetails.getAltLaunchExe());
      stmtBuilder.append("'GameType' = ?, ");
      params.add(tableDetails.getGameType() != null ? tableDetails.getGameType().name() : null);
      stmtBuilder.append("'GAMEVER' = ?, ");
      params.add(tableDetails.getGameVersion());
      stmtBuilder.append("'DOFStuff' = ?, ");
      params.add(tableDetails.getDof());
      stmtBuilder.append("'IPDBNum' = ?, ");
      params.add(tableDetails.getIPDBNum() != null ? tableDetails.getIPDBNum() : "");
      stmtBuilder.append("'AltRunMode' = ?, ");
      params.add(tableDetails.getAltRunMode() != null ? tableDetails.getAltRunMode() : "");
      stmtBuilder.append("'WebLinkURL' = ?, ");
      params.add(tableDetails.getUrl());
      stmtBuilder.append("'DesignedBy' = ?, ");
      params.add(tableDetails.getDesignedBy());
      stmtBuilder.append("'Visible' = ?, ");
      params.add(tableDetails.getStatus());
      stmtBuilder.append("'CUSTOM2' = ?, ");
      params.add(tableDetails.getCustom2());
      stmtBuilder.append("'CUSTOM3' = ?, ");
      params.add(tableDetails.getCustom3());
      stmtBuilder.append("'MediaSearch' = ?, ");
      params.add(tableDetails.getMediaSearch() != null ? tableDetails.getMediaSearch() : "");
      stmtBuilder.append("'Special' = ?, ");
      params.add(tableDetails.getSpecial());

      //check for popper DB update 1.5
      if (sqlVersion >= DB_VERSION) {
        stmtBuilder.append("'WEBGameID' = ?, ");
        params.add(tableDetails.getWebGameId());
        stmtBuilder.append("'ROMALT' = ?, ");
        params.add(tableDetails.getRomAlt());
        stmtBuilder.append("'ISMOD' = ?, ");
        params.add(tableDetails.isMod());
        stmtBuilder.append("'WebLink2URL' = ?, ");
        params.add(tableDetails.getWebLink2Url());
        stmtBuilder.append("'TourneyID' = ?, ");
        params.add(tableDetails.getTourneyId());
        stmtBuilder.append("'CUSTOM4' = ?, ");
        params.add(tableDetails.getCustom4());
        stmtBuilder.append("'CUSTOM5' = ?, ");
        params.add(tableDetails.getCustom5());

        importGameExtraValues(id, tableDetails.getgLog(), tableDetails.getgNotes(), tableDetails.getgPlayLog(), tableDetails.getgDetails());
      }

      stmtBuilder.append("DateUpdated=? WHERE GameID=?");

      String stmt = stmtBuilder.toString();
      PreparedStatement preparedStatement = connect.prepareStatement(stmt);
      int index = 1;
      for (Object param : params) {
        preparedStatement.setObject(index, param);
        index++;
      }

      SimpleDateFormat sdf = new SimpleDateFormat(POPPER_DATE_FORMAT);
      Timestamp timestamp = new Timestamp(System.currentTimeMillis());
      String ts = sdf.format(timestamp);

      preparedStatement.setObject(index, ts);
      index++;

      preparedStatement.setInt(index, id);
      preparedStatement.executeUpdate();
      preparedStatement.close();
    }
    catch (Exception e) {
      LOG.error("Failed to save table details: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  @Nullable
  public Game getGameByFilename(String filename) {
    Connection connect = this.connect();
    Game info = null;
    try {
      String gameName = filename.replaceAll("'", "''");
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery(
        "SELECT g.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
        + " left join Emulators e on g.EMUID=e.EMUID "
        + " where g.GameFileName = '" + gameName + "' OR g.GameFileName LIKE '%\\" + gameName + "';");
      while (rs.next()) {
        info = createGame(rs);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game by filename '" + filename + "': " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return info;
  }

  @NonNull
  public List<Game> getGamesByEmulator(int emulatorId) {
    Connection connect = this.connect();
    List<Game> result = new ArrayList<>();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery(
        "SELECT g.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
        + " left join Emulators e on g.EMUID=e.EMUID"
        + " where g.EMUID = " + emulatorId);
      while (rs.next()) {
        result.add(createGame(rs));
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game by emulatorId '" + emulatorId + "': " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return result;
  }

  @NonNull
  public List<Game> getGamesByFilename(String filename) {
    Connection connect = this.connect();
    List<Game> result = new ArrayList<>();
    try {
      String gameName = filename.replaceAll("'", "''");
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery(
        "SELECT g.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
        + " left join Emulators e on g.EMUID=e.EMUID"
        + " where GameFileName LIKE '%" + gameName + "%';");
      while (rs.next()) {
        Game game = createGame(rs);
        result.add(game);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game by filename '" + filename + "': " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return result;
  }

  @Nullable
  public Game getGameByName(String gameName) {
    Connection connect = this.connect();
    Game info = null;
    try {
      gameName = gameName.replaceAll("'", "''");
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery(
        "SELECT g.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
        + " left join Emulators e on g.EMUID=e.EMUID"
        + " where GameName = '" + gameName + "';");
      while (rs.next()) {
        info = createGame(rs);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
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
    }
    catch (SQLException e) {
      LOG.error("Failed to read startup script: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }

    if (script == null) {
      script = "";
    }
    return script;
  }

  @NonNull
  public int getVersion() {
    int version = -1;
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GlobalSettings;");
      rs.next();
      version = rs.getInt("SQLVersion");
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.warn("Failed to PinUP Popper Database version: " + e.getMessage() + ", using legacy database schema.", e);
    } finally {
      this.disconnect(connect);
    }
    return version;
  }
  @Override
  public boolean isPopper15() {
    return getVersion() >= PinUPConnector.DB_VERSION;
  }

  public void updateStartupScript(@NonNull String content) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("UPDATE GlobalSettings SET 'StartupBatch'=?");
      preparedStatement.setString(1, content);
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Update of startup script successful.");
    }
    catch (Exception e) {
      LOG.error("Failed to update startup script script:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public FrontendCustomOptions getCustomOptions() {
    Connection connect = connect();
    FrontendCustomOptions options = null;
    try {
      PreparedStatement statement = connect.prepareStatement("SELECT * FROM GlobalSettings");
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        String optionString = rs.getString("GlobalOptions");
        options = new FrontendCustomOptions();
        options.setScriptData(optionString);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed get custom options: " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return options;
  }

  public void updateCustomOptions(@NonNull FrontendCustomOptions options) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("UPDATE GlobalSettings SET 'GlobalOptions'=?");
      preparedStatement.setString(1, options.toString());
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Updated of custom options");
    }
    catch (Exception e) {
      LOG.error("Failed to update custom options:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  @Override
  public void setPupPackEnabled(@NonNull Game game, boolean enable) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement;
      if (enable) {
        preparedStatement = connect.prepareStatement("UPDATE Games SET 'ROM'=?, 'LaunchCustomVar'='' WHERE GameID=?");
        preparedStatement.setString(1, game.getRom());
        preparedStatement.setInt(2, game.getId());
      } else {
        preparedStatement = connect.prepareStatement("UPDATE Games SET 'LaunchCustomVar'='HIDEPUP' WHERE GameID=?");
        preparedStatement.setInt(1, game.getId());
      }
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Updated of ROM of \"" + game + "\" to " + game.getRom());
      LOG.info("Updated of LaunchCustomVar of \"" + game + "\" to \"" + (enable? "": "HIDEPUP") + "\"");
    }
    catch (Exception e) {
      LOG.error("Failed to update \"LaunchCustomVar\" " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  @Nullable
  public boolean isPupPackDisabled(@NonNull Game game) {
    String effectiveRom = game.getRom();
    if (StringUtils.isEmpty(effectiveRom)) {
      return false;
    }

    Connection connect = this.connect();
    try {
      PreparedStatement statement = connect.prepareStatement("SELECT * FROM Games where GameID = ?");
      statement.setInt(1, game.getId());
      ResultSet rs = statement.executeQuery();
      String rom = null;
      String custom = null;
    if (rs.next()) {
        rom = rs.getString("ROM");
        custom = rs.getString("LaunchCustomVar");
        return rom != null && !StringUtils.isEmpty(custom) && custom.equals("HIDEPUP");
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read \"LaunchCustomVar\": " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return false;
  }

  /**
   * Creates a new entry in the PinUP Popper database.
   * Returns the id of the new entry.
   *
   * @return the generated game id.
   */
  public int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO Games (EMUID, GameName, GameFileName, GameDisplay, Visible, LaunchCustomVar, DateAdded, DateFileUpdated, " +
          "Author, TAGS, Category, MediaSearch, IPDBNum, AltRunMode) " +
          "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setInt(1, emulatorId);
      preparedStatement.setString(2, gameName);
      preparedStatement.setString(3, gameFileName);
      preparedStatement.setString(4, gameDisplayName);
      preparedStatement.setInt(5, 1);
      preparedStatement.setString(6, launchCustomVar != null ? launchCustomVar : "");

      SimpleDateFormat sdf = new SimpleDateFormat(POPPER_DATE_FORMAT);
      Timestamp timestamp = new Timestamp(System.currentTimeMillis());
      String ts = sdf.format(timestamp);
      preparedStatement.setString(7, ts);
      preparedStatement.setString(8, sdf.format(dateFileUpdated));

      preparedStatement.setString(9, "");
      preparedStatement.setString(10, "");
      preparedStatement.setString(11, "");
      preparedStatement.setString(12, "");
      preparedStatement.setString(13, "");
      preparedStatement.setString(14, "");

      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Added game entry for '" + gameName + "', file name '" + gameFileName + "'");
      try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
        if (keys.next()) {
          return keys.getInt(1);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to update game table:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return -1;
  }

  // no more used
  public boolean deleteGame(String name) {
    Game gameByFilename = getGameByFilename(name);
    if (gameByFilename != null) {
      return deleteGame(gameByFilename.getId());
    }
    LOG.error("Failed to delete " + name + ": no game entry has been found for this name.");
    return false;
  }

  @Override
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
    }
    catch (Exception e) {
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
    }
    catch (Exception e) {
      LOG.error("Failed to update game stats table:" + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }


  @NonNull
  public Playlist getPlayList(int id) {
    Playlist playlist = new Playlist();
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Playlists WHERE Visible = 1 AND PlayListID = " + id + ";");
      while (rs.next()) {
        String sql = rs.getString("PlayListSQL");
        String name = rs.getString("PlayName");
        boolean sqlPlaylist = rs.getInt("PlayListType") == 1;
        playlist.setId(rs.getInt("PlayListID"));
        playlist.setName(name);
        playlist.setPlayListSQL(sql);

        playlist.setMenuColor(rs.getInt("MenuColor"));
        if (rs.wasNull()) {
          playlist.setMenuColor(null);
        }
        playlist.setSqlPlayList(sqlPlaylist);

        if (sqlPlaylist) {
          playlist.setGames(getGameIdsFromSqlPlaylist(sql));
        }
        else {
          playlist.setGames(getGameIdsFromPlaylist(playlist.getId()));
        }
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get playlist: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return playlist;
  }

  @NonNull
  public List<Playlist> getPlayLists(boolean excludeSqlLists) {
    Connection connect = this.connect();
    List<Playlist> result = new ArrayList<>();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Playlists WHERE Visible = 1;");
      while (rs.next()) {
        String sql = rs.getString("PlayListSQL");
        String name = rs.getString("PlayName");
        boolean sqlPlaylist = rs.getInt("PlayListType") == 1;

        if (excludeSqlLists && sqlPlaylist) {
          continue;
        }

        Playlist playlist = new Playlist();
        playlist.setId(rs.getInt("PlayListID"));
        playlist.setName(name);
        playlist.setPlayListSQL(sql);

        playlist.setMenuColor(rs.getInt("MenuColor"));
        if (rs.wasNull()) {
          playlist.setMenuColor(null);
        }
        playlist.setSqlPlayList(sqlPlaylist);

        if (sqlPlaylist) {
          playlist.setGames(getGameIdsFromSqlPlaylist(sql));
        }
        else {
          playlist.setGames(getGameIdsFromPlaylist(playlist.getId()));
        }

        result.add(playlist);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get playlist: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return result;
  }

  public void setPlaylistColor(int playlistId, long color) {
    Connection connect = this.connect();
    String sql = "UPDATE PlayLists SET 'MenuColor'=" + color + " WHERE PlayListID = " + playlistId + ";";
    try {
      Statement stmt = connect.createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
    }
    catch (Exception e) {
      LOG.error("Failed to update PlayList: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public void addToPlaylist(int playlistId, int gameId, int favMode) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO PlayListDetails (PlayListID, GameID, Visible, DisplayOrder, NumPlayed, " + IS_FAV + ") VALUES (?,?,?,?,?,?)");
      preparedStatement.setInt(1, playlistId);
      preparedStatement.setInt(2, gameId);
      preparedStatement.setInt(3, 1);
      preparedStatement.setInt(4, 0);
      preparedStatement.setInt(5, 0);
      preparedStatement.setInt(6, favMode);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Added game " + gameId + " to playlist " + playlistId);
    }
    catch (SQLException e) {
      LOG.error("Failed to update playlist details: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public void updatePlaylistGame(int playlistId, int gameId, int favMode) {
    Connection connect = this.connect();
    String sql = "UPDATE PlayListDetails SET " + IS_FAV + " = " + favMode + " WHERE GameID=" + gameId + " AND PlayListID=" + playlistId + ";";
    try {
      Statement stmt = connect.createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
    }
    catch (Exception e) {
      LOG.error("Failed to update playlist [" + sql + "]: " + e.getMessage(), e);
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
    }
    catch (SQLException e) {
      LOG.error("Failed to update playlist details: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  public void deleteFromPlaylist(int playlistId, int gameId) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("DELETE FROM PlayListDetails WHERE GameID = ? AND PlayListID = ?");
      preparedStatement.setInt(1, gameId);
      preparedStatement.setInt(2, playlistId);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Removed game " + gameId + " from playlist " + playlistId);
    }
    catch (SQLException e) {
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
    }
    catch (SQLException e) {
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
        e.setDisplayName(rs.getString("EmuDisplay"));
        e.setDirMedia(rs.getString("DirMedia"));
        e.setDirGames(rs.getString("DirGames"));
        e.setDirRoms(rs.getString("DirRoms"));
        e.setDescription(rs.getString("Description"));
        e.setEmuLaunchDir(rs.getString("EmuLaunchDir"));
        e.setLaunchScript(rs.getString("LaunchScript"));
        e.setGamesExt(rs.getString("GamesExt"));
        e.setVisible(rs.getInt("Visible") == 1);

        result.add(e);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get function: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }

    // init scripts
    for (Emulator emu : result) {
      if (emu.isVisualPinball()) {
        initVisualPinballXScripts(emu);
      }
    }

    return result;
  }

  @NonNull
  public List<TableAlxEntry> getAlxData() {
    Connection connect = this.connect();
    List<TableAlxEntry> result = new ArrayList<>();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("select * from GamesStats JOIN GAMES ON GAMES.GameID = GamesStats.GameID;");
      while (rs.next()) {
        TableAlxEntry e = new TableAlxEntry();
        e.setDisplayName(rs.getString("GameDisplay"));
        e.setGameId(rs.getInt("GameId"));
        e.setUniqueId(rs.getInt("UniqueId"));
        e.setLastPlayed(rs.getDate("LastPlayed"));
        e.setTimePlayedSecs(rs.getInt("TimePlayedSecs"));
        e.setNumberOfPlays(rs.getInt("NumberPlays"));
        result.add(e);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get alx data: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return result;
  }

  @NonNull
  public List<TableAlxEntry> getAlxData(int gameId) {
    Connection connect = this.connect();
    List<TableAlxEntry> result = new ArrayList<>();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("select * from GamesStats JOIN GAMES ON GAMES.GameID = GamesStats.GameID where GamesStats.GameID = " + gameId + ";");
      while (rs.next()) {
        TableAlxEntry e = new TableAlxEntry();
        e.setDisplayName(rs.getString("GameDisplay"));
        e.setGameId(rs.getInt("GameId"));
        e.setUniqueId(rs.getInt("UniqueId"));
        e.setLastPlayed(rs.getDate("LastPlayed"));
        e.setTimePlayedSecs(rs.getInt("TimePlayedSecs"));
        e.setNumberOfPlays(rs.getInt("NumberPlays"));
        result.add(e);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get alx data: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return result;
  }

//  public void enablePCGameEmulator() {
//    List<Emulator> ems = this.getEmulators();
//    for (Emulator em : ems) {
//      if (em.getName().equalsIgnoreCase(EmulatorType.PC_GAMES) && em.isVisible()) {
//        return;
//      }
//    }
//
//    Connection connect = this.connect();
//    String sql = "UPDATE Emulators SET 'VISIBLE'=1 WHERE EmuName = '" + EmulatorType.PC_GAMES + "';";
//    try {
//      Statement stmt = connect.createStatement();
//      stmt.executeUpdate(sql);
//      stmt.close();
//      LOG.info("Enabled PC Games emulator for popper.");
//    } catch (Exception e) {
//      LOG.error("Failed to update script script [" + sql + "]: " + e.getMessage(), e);
//    } finally {
//      this.disconnect(connect);
//    }
//  }

  //TODO rename getControl
  @Nullable
  public FrontendControl getFunction(@NonNull String description) {
    FrontendControl f = null;
    Connection connect = this.connect();
    try {
      PreparedStatement statement = connect.prepareStatement("SELECT * FROM PinUPFunctions WHERE Descript = ?");
      statement.setString(1, description);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        f = new FrontendControl();
        f.setActive(rs.getInt("Active") == 1);
        f.setDescription(rs.getString("Descript"));
        f.setCtrlKey(rs.getInt("CntrlCodes"));
        f.setId(rs.getInt("uniqueID"));
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get function: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return f;
  }


  public FrontendControl getPinUPControlFor(VPinScreen screen) {
    switch (screen) {
      case Other2: {
        return getFunction(FrontendControl.FUNCTION_SHOW_OTHER);
      }
      case GameHelp: {
        return getFunction(FrontendControl.FUNCTION_SHOW_HELP);
      }
      case GameInfo: {
        return getFunction(FrontendControl.FUNCTION_SHOW_FLYER);
      }
      default: {

      }
    }

    return new FrontendControl();
  }

  @NonNull
  public FrontendControls getControls() {
    FrontendControls controls = new FrontendControls();
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PinUPFunctions;");
      while (rs.next()) {
        FrontendControl f = new FrontendControl();
        f.setActive(rs.getInt("Active") == 1);
        f.setDescription(rs.getString("Descript"));
        f.setCtrlKey(rs.getInt("CntrlCodes"));
        f.setId(rs.getInt("uniqueID"));
        controls.addControl(f);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to functions: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return controls;
  }

  @Override
  public int getGameCount(int emuId) {
    int count = 0;
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT count(*) as count FROM Games WHERE EMUID = " + emuId + ";");
      while (rs.next()) {
        count = count + rs.getInt("count");
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game count for emulator " + emuId + ": " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return count;
  }

  @Override
  public List<Integer> getGameIds(int emuId) {
    List<Integer> result = new ArrayList<>();
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT GameID FROM Games WHERE EMUID = " + emuId + ";");
      while (rs.next()) {
        result.add(rs.getInt("GameID"));
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game count: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return result;
  }

  @NonNull
  @Override
  public List<Game> getGames() {
    Connection connect = this.connect();
    List<Game> results = new ArrayList<>();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery(
        "SELECT g.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
        + " left join Emulators e on g.EMUID=e.EMUID");
      while (rs.next()) {
        Game info = createGame(rs);
        if (info == null) {
          continue;
        }

        results.add(info);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get games: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return results;
  }

  @NonNull
  @Override
  public java.util.Date getStartDate() {
    Connection connect = this.connect();
    Date date = null;
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT DateAdded from Games asc limit 1;");
      while (rs.next()) {
        date = rs.getDate(1);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get start time: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }

    return date;
  }

  @Override
  public void deleteGames() {
    Connection connect = this.connect();
    try {
      Statement statement = connect.createStatement();
      statement.execute("DELETE FROM Games WHERE EMUID = 1;");
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to delete games: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  @NonNull
  @Override
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
    }
    catch (SQLException e) {
      LOG.error("Failed to read playlists: " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return result;
  }

  @NonNull
  private List<PlaylistGame> getGameIdsFromPlaylist(int id) {
    List<PlaylistGame> result = new ArrayList<>();
    Connection connect = connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PlayListDetails WHERE PlayListID = " + id);

      while (rs.next()) {
        PlaylistGame game = new PlaylistGame();
        int gameId = rs.getInt("GameID");
        game.setId(gameId);

        int favMode = rs.getInt(IS_FAV);
        game.setFav(favMode == 1);
        game.setGlobalFav(favMode == 2);

        result.add(game);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read playlists: " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return result;
  }


  private List<PlaylistGame> getGameIdsFromSqlPlaylist(String sql) {
    List<PlaylistGame> result = new ArrayList<>();
    Connection connect = connect();
    try {
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery(sql);

      while (rs.next()) {
        PlaylistGame game = new PlaylistGame();
        int gameId = rs.getInt("GameID");
        game.setId(gameId);
        result.add(game);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read playlists: " + e.getMessage(), e);
    } finally {
      disconnect(connect);
    }
    return result;
  }

  @NonNull
  public String getEmulatorStartupScript(@NonNull String emuName) {
    String script = "";
    Connection connect = this.connect();
    try {
      emuName = emuName.replaceAll("'", "''");
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators where EmuName = '" + emuName + "';");
      rs.next();
      script = rs.getString(LAUNCH_SCRIPT);
      if (script == null) {
        script = "";
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read startup script or " + emuName + ": " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
    return script;
  }

  @NonNull
  public String getEmulatorExitScript(@NonNull String emuName) {
    String script = "";
    Connection connect = this.connect();
    try {
      emuName = emuName.replaceAll("'", "''");
      Statement statement = connect.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators where EmuName = '" + emuName + "';");
      rs.next();
      script = rs.getString(POST_SCRIPT);
      if (script == null) {
        script = "";
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
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
    }
    catch (Exception e) {
      LOG.error("Failed to update script script " + scriptName + " [" + sql + "]: " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

  /*
    ResultSet rs = statement.executeQuery("SELECT * FROM Emulators;");

    e.setId(rs.getInt("EMUID"));
      e.setName(rs.getString("EmuName"));
      e.setDisplayName(rs.getString("EmuDisplay"));
      e.setDirMedia(rs.getString("DirMedia"));
      e.setDirGames(rs.getString("DirGames"));
      e.setDirRoms(rs.getString("DirRoms"));
      e.setDescription(rs.getString("Description"));
      e.setEmuLaunchDir(rs.getString("EmuLaunchDir"));
      e.setLaunchScript(rs.getString("LaunchScript"));
      e.setGamesExt(rs.getString("GamesExt"));
      e.setVisible(rs.getInt("Visible") == 1);

   */


  @Nullable
  private Game createGame(@NonNull ResultSet rs) throws SQLException {

    int emuId = rs.getInt("EMUID");
    boolean emuVisible = rs.getInt("EmuVisible") == 1;

    //GameEmulator emulator = emulators.get(emuId);
    //if (emulator == null || !emulator.isVpx()) {
    //  return null;
    //}

    Game game = new Game();
    game.setEmulatorId(emuId);

    int id = rs.getInt("GameID");
    game.setId(id);

    game.setDisabled(rs.getInt("Visible") == 0 || !emuVisible);

    String gameFileName = rs.getString("GameFileName");
    game.setGameFileName(gameFileName);

    String rom = rs.getString("ROM");
    game.setRom(rom);
    String tableName = rs.getString("ROMALT");
    game.setTableName(tableName);

    //TODO add VPS ids here
    if (!StringUtils.isEmpty(serverSettings.getMappingHsFileName())) {
      String highscoreFilename = rs.getString(serverSettings.getMappingHsFileName());
      game.setHsFileName(highscoreFilename);
    }

    String gameDisplayName = rs.getString("GameDisplay");
    game.setGameDisplayName(gameDisplayName);

    String gameName = rs.getString("GameName");
    game.setGameName(gameName);
    game.setDateAdded(rs.getDate("DateAdded"));
    game.setDateUpdated(rs.getDate("DateUpdated"));

    game.setVersion(rs.getString("GAMEVER"));

    String dirGame = rs.getString("DirGames");
    File vpxFile = new File(dirGame, gameFileName);
    game.setGameFile(vpxFile);

    game.setExtTableId(rs.getString(serverSettings.getMappingVpsTableId()));
    game.setExtTableVersionId(rs.getString(serverSettings.getMappingVpsTableVersionId()));
    game.setHsFileName(rs.getString(serverSettings.getMappingHsFileName()));

    return game;
  }

  private void loadStats(@NonNull Connection connection, @NonNull TableDetails manifest, int gameId) {
    try {
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GamesStats where GameID = " + gameId + ";");
      while (rs.next()) {
        int numberPlays = rs.getInt("NumberPlays");
        Date lastPlayed = rs.getDate("LastPlayed");

        manifest.setLastPlayed(lastPlayed);
        manifest.setNumberPlays(numberPlays);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read table stats info: " + e.getMessage(), e);
    }
  }

  private void loadGameExtras(@NonNull Connection connection, @NonNull TableDetails manifest, int gameId) {
    try {
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GamesExtra where GameID = " + gameId + ";");
      while (rs.next()) {
        String gLog = rs.getString("gLOG");
        String gNotes = rs.getString("gNotes");
        String gPlayLog = rs.getString("gPlayLog");
        String gDetails = rs.getString("gDetails");

        manifest.setgLog(gLog);
        manifest.setgNotes(gNotes);
        manifest.setgPlayLog(gPlayLog);
        manifest.setgDetails(gDetails);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read table stats info: " + e.getMessage(), e);
    }
  }

  private void importGameExtraValues(int gameId, String gLog, String gNotes, String gPlayLog, String gDetails) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = connect.prepareStatement("insert or replace into GamesExtra (GameID, gLOG, gNotes, gPlayLog, gDetails) values (?,?,?,?,?)");
      preparedStatement.setInt(1, gameId);
      preparedStatement.setString(2, gLog);
      preparedStatement.setString(3, gNotes);
      preparedStatement.setString(4, gPlayLog);
      preparedStatement.setString(5, gDetails);
      preparedStatement.executeUpdate();
      preparedStatement.close();
    }
    catch (Exception e) {
      LOG.error("Failed to update game extra for " + gameId + ": " + e.getMessage(), e);
    } finally {
      this.disconnect(connect);
    }
  }

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

  @Override
  public MediaAccessStrategy getMediaAccessStrategy() {
    return ((mediaDirectory, tableName, screen) -> new File(mediaDirectory, screen.name()));
  }

  public void initializeConnector(ServerSettings settings) {
    this.serverSettings = settings;

    File file = systemService.getPinUPDatabaseFile();
    dbFilePath = file.getAbsolutePath().replaceAll("\\\\", "/");

    sqlVersion = this.getVersion();

  }

}
