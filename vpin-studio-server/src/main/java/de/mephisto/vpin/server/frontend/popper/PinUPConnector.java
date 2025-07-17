package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.frontend.popper.PopperSettings;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.competitions.CompetitionIdFactory;
import de.mephisto.vpin.server.frontend.CacheTableAssetsAdapter;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.recorder.EmulatorRecorderJob;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.WindowsUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service("Popper")
public class PinUPConnector implements FrontendConnector, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PinUPConnector.class);

  private final static String CURL_COMMAND_POPPER_START = "curl -X POST --data-urlencode \"system=\" http://localhost:" + SystemUtil.getPort() + "/service/popperLaunch";
  private final static String CURL_COMMAND_TABLE_START_LEGACY = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + SystemUtil.getPort() + "/service/gameLaunch";
  private final static String CURL_COMMAND_TABLE_START = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" --data-urlencode \"emu=[DIREMU]\" http://localhost:" + SystemUtil.getPort() + "/service/gameLaunch";
  private final static String CURL_COMMAND_TABLE_EXIT_LEGACY = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + SystemUtil.getPort() + "/service/gameExit";
  private final static String CURL_COMMAND_TABLE_EXIT = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" --data-urlencode \"emu=[DIREMU]\" http://localhost:" + SystemUtil.getPort() + "/service/gameExit";

  public static final String POST_SCRIPT = "PostScript";
  public static final String LAUNCH_SCRIPT = "LaunchScript";
  public static final int DB_VERSION = 64;
  public static final String IS_FAV = "isFav";
  public static final String POPPER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final String PIN_UP_MENU = "PinUpMenu";
  public String dbFilePath;

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  private PupLauncher pupLauncher;

  private PinUPMediaAccessStrategy pinUPMediaAccessStrategy;

  private int sqlVersion = DB_VERSION;

  private TableAssetsAdapter assetsAdapter;
  private PupEventEmitter pupEventEmitter;

  @Override
  public void reloadCache() {
    //not used yet
  }

  @NonNull
  @Override
  public File getInstallationFolder() {
    return systemService.getPinupInstallationFolder();
  }

  private void initEmulatorScripts(GameEmulator emulator) {
    String emulatorName = emulator.getName();

    String emulatorStartupScript = this.getEmulatorStartupScript(emulatorName);
    checkScript("LaunchScript", emulator, emulatorStartupScript, CURL_COMMAND_TABLE_START, CURL_COMMAND_TABLE_START_LEGACY);

    String emulatorExitScript = this.getEmulatorExitScript(emulatorName);
    checkScript("PostScript", emulator, emulatorExitScript, CURL_COMMAND_TABLE_EXIT, CURL_COMMAND_TABLE_EXIT_LEGACY);

    String startupScript = this.getStartupScript();
    if (!startupScript.contains(CURL_COMMAND_POPPER_START)) {
      startupScript = startupScript + "\n" + CURL_COMMAND_POPPER_START + "\n";
      this.updateStartupScript(startupScript);
    }
  }

  private void checkScript(String dbFieldName, GameEmulator emulator, String script, String call, String legacyCall) {
    if (script.contains(legacyCall)) {
      script = script.replace(legacyCall, "").trim();
    }

    if (!script.contains(call)) {
      script = script.trim() + "\n\n" + call;
      this.updateScript(emulator.getName(), dbFieldName, script);
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
      LOG.error("Failed to connect to sqlite: {}", e.getMessage(), e);
    }
    return null;
  }

  private void disconnect(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      }
      catch (SQLException e) {
        LOG.error("Error disconnecting from sqlite: {}", e.getMessage());
      }
    }
  }

  @Nullable
  @Override
  public Game getGame(int id) {
    Connection connect = connect();
    Game info = null;
    try {
      PreparedStatement statement = Objects.requireNonNull(connect).prepareStatement(
          "SELECT g.*, s.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
              + " left join Emulators e on g.EMUID=e.EMUID left join GamesStats s on s.GameID = g.GameID where g.GameID = ?");

      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        info = createGame(rs);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get game for id '{}': {}", id, e.getMessage(), e);
    }
    finally {
      disconnect(connect);
    }
    return info;
  }

  @Nullable
  public TableDetails getTableDetails(int id) {
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    Connection connect = connect();
    TableDetails manifest = null;
    try {
      PreparedStatement statement = Objects.requireNonNull(connect).prepareStatement("SELECT * FROM Games where GameID = ?");
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        manifest = new TableDetails();
        manifest.setSqlVersion(sqlVersion);

        manifest.setEmulatorId(rs.getInt("EMUID"));
        manifest.setGameName(rs.getString("GameName"));
        manifest.setGameFileName(rs.getString("GameFileName"));
        manifest.setGameDisplayName(rs.getString("GameDisplay"));
        manifest.setGameVersion(rs.getString("GAMEVER"));
        manifest.setDateAdded(getDateAsTimestamp(rs, "DateAdded"));
        manifest.setDateModified(getDateAsTimestamp(rs, "DateUpdated"));
        manifest.setNotes(rs.getString("Notes"));
        manifest.setGameYear(rs.getInt("GameYear"));
        if (rs.wasNull()) {
          manifest.setGameYear(null);
        }
        String gameType = rs.getString("GameType");
        manifest.setGameType(gameType);

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

        // add mapped field
        if (!StringUtils.isEmpty(serverSettings.getMappingHsFileName())) {
          manifest.setHsFilename(rs.getString(serverSettings.getMappingHsFileName()));
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
      LOG.error("Failed to get game for id '{}': {}", id, e.getMessage(), e);
    }
    finally {
      disconnect(connect);
    }
    return manifest;
  }

  public void updateTableFileUpdated(int id) {
    Connection connect = this.connect();
    try {
      String stmt = "UPDATE Games SET DateFileUpdated=? WHERE GameID=?";
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement(stmt);

      SimpleDateFormat sdf = new SimpleDateFormat(POPPER_DATE_FORMAT);
      Timestamp timestamp = new Timestamp(System.currentTimeMillis());
      String ts = sdf.format(timestamp);
      preparedStatement.setObject(1, ts);
      preparedStatement.setInt(2, id);

      preparedStatement.executeUpdate();
      preparedStatement.close();
    }
    catch (Exception e) {
      LOG.error("Failed to save table details: {}", e.getMessage(), e);
    }
    finally {
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
      params.add(tableDetails.getGameType() != null ? tableDetails.getGameType() : "");
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

        importGameExtraValues(connect, id, tableDetails.getgLog(), tableDetails.getgNotes(), tableDetails.getgPlayLog(), tableDetails.getgDetails());
      }

      stmtBuilder.append("DateUpdated=? WHERE GameID=?");

      String stmt = stmtBuilder.toString();
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement(stmt);
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
    }
    finally {
      this.disconnect(connect);
    }
  }

  @Override
  public void vpsLink(int gameId, String extTableId, String extTableVersionId) {
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("UPDATE Games SET "
          + "'" + serverSettings.getMappingVpsTableId() + "'=?, "
          + "'" + serverSettings.getMappingVpsTableVersionId() + "'=?, "
          + "DateUpdated=? WHERE GameID=?");
      int index = 1;
      preparedStatement.setString(index++, extTableId);
      preparedStatement.setString(index++, extTableVersionId);

      SimpleDateFormat sdf = new SimpleDateFormat(POPPER_DATE_FORMAT);
      Timestamp timestamp = new Timestamp(System.currentTimeMillis());
      String ts = sdf.format(timestamp);
      preparedStatement.setObject(index++, ts);

      preparedStatement.setInt(index++, gameId);

      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Update Game with VPS link information.");
    }
    catch (Exception e) {
      LOG.error("Failed to Game with VPS Link:" + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }

  @Nullable
  @Override
  public Game getGameByFilename(int emulatorId, String filename) {
    Connection connect = this.connect();
    Game info = null;
    try {
      String gameName = filename.replaceAll("'", "''");
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery(
          "SELECT g.*, s.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
              + " left join Emulators e on g.EMUID=e.EMUID left join GamesStats s on s.GameID = g.GameID "
              + " where g.EMUID = " + emulatorId
              + " and (g.GameFileName = '" + gameName + "' OR g.GameFileName LIKE '%\\" + gameName + "');");
      while (rs.next()) {
        info = createGame(rs);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game by filename '" + filename + "': " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return info;
  }

  @NonNull
  public List<Game> getGamesByEmulator(int emulatorId) {
    long start = System.currentTimeMillis();
    Connection connect = this.connect();
    List<Game> result = new ArrayList<>();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery(
          "SELECT g.*, s.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
              + " left join Emulators e on g.EMUID=e.EMUID left join GamesStats s on s.GameID = g.GameID"
              + " where g.EMUID = " + emulatorId);
      while (rs.next()) {
        result.add(createGame(rs));
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game by emulatorId '" + emulatorId + "': " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
      LOG.info("Game fetch for emulatorId '" + emulatorId + "' took " + (System.currentTimeMillis() - start) + "ms.");
    }
    return result;
  }

  @NonNull
  @Override
  public List<Game> getGamesByFilename(String filename) {
    Connection connect = this.connect();
    List<Game> result = new ArrayList<>();
    try {
      String gameName = filename.replaceAll("'", "''");
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery(
          "SELECT g.*, s.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
              + " left join Emulators e on g.EMUID=e.EMUID left join GamesStats s on s.GameID = g.GameID"
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
    }
    finally {
      this.disconnect(connect);
    }
    return result;
  }

  @Nullable
  @Override
  public Game getGameByName(int emulatorId, String gameName) {
    Connection connect = this.connect();
    Game info = null;
    try {
      gameName = gameName.replaceAll("'", "''");
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery(
          "SELECT g.*, s.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
              + " left join Emulators e on g.EMUID=e.EMUID left join GamesStats s on s.GameID = g.GameID"
              + " where g.EMUID = " + emulatorId
              + " and GameName = '" + gameName + "';");
      while (rs.next()) {
        info = createGame(rs);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game by gameName '" + gameName + "': " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return info;
  }

  @NonNull
  public String getStartupScript() {
    String script = null;
    Connection connect = this.connect();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GlobalSettings;");
      rs.next();
      script = rs.getString("StartupBatch");
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read startup script: " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }

    if (script == null) {
      script = "";
    }
    return script;
  }

  public boolean isSystemVolumeControlled() {
    boolean systemVolumeControlled = false;
    Connection connect = this.connect();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GlobalSettings;");
      rs.next();
      int on = rs.getInt("SYSVOLUME");
      systemVolumeControlled = (on != -1);
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to sysvolume setting: " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return systemVolumeControlled;
  }

  public int getVersion() {
    int version = -1;
    Connection connect = this.connect();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM GlobalSettings;");
      rs.next();
      version = rs.getInt("SQLVersion");
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.warn("Failed to PinUP Popper Database version: " + e.getMessage() + ", using legacy database schema.", e);
    }
    finally {
      this.disconnect(connect);
    }
    return version;
  }

  public void updateStartupScript(@NonNull String content) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("UPDATE GlobalSettings SET 'StartupBatch'=?");
      preparedStatement.setString(1, content);
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Update of startup script successful.");
    }
    catch (Exception e) {
      LOG.error("Failed to update startup script script:" + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }

  @Override
  public PopperSettings getSettings() {
    Connection connect = connect();
    PopperSettings settings = null;
    try {
      PreparedStatement statement = Objects.requireNonNull(connect).prepareStatement("SELECT * FROM GlobalSettings");
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        String optionString = rs.getString("GlobalOptions");
        settings = new PopperSettings();
        settings.setScriptData(optionString);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed get custom settings: {}", e.getMessage(), e);
    }
    finally {
      disconnect(connect);
    }
    return settings;
  }

  public void saveSettings(@NonNull Map<String, Object> data) {
    Connection connect = this.connect();
    try {
      PopperSettings options = JsonSettings.objectMapper.convertValue(data, PopperSettings.class);
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("UPDATE GlobalSettings SET 'GlobalOptions'=?");
      preparedStatement.setString(1, options.toString());
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Updated of custom options");
    }
    catch (Exception e) {
      LOG.error("Failed to update custom options:" + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }

  @Override
  public void setPupPackEnabled(@NonNull Game game, boolean enable) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement;
      if (enable) {
        preparedStatement = Objects.requireNonNull(connect).prepareStatement("UPDATE Games SET 'ROM'=?, 'LaunchCustomVar'='' WHERE GameID=?");
        preparedStatement.setString(1, game.getRom());
        preparedStatement.setInt(2, game.getId());
      }
      else {
        preparedStatement = Objects.requireNonNull(connect).prepareStatement("UPDATE Games SET 'LaunchCustomVar'='HIDEPUP' WHERE GameID=?");
        preparedStatement.setInt(1, game.getId());
      }
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Updated of LaunchCustomVar of \"" + game + "\" to \"" + (enable ? "" : "HIDEPUP") + "\"");
    }
    catch (Exception e) {
      LOG.error("Failed to update \"LaunchCustomVar\" " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }

//  @Nullable
//  public boolean isPupPackDisabled(@NonNull Game game) {
//    String effectiveRom = game.getRom();
//    if (StringUtils.isEmpty(effectiveRom)) {
//      return false;
//    }
//
//    Connection connect = this.connect();
//    try {
//      PreparedStatement statement = Objects.requireNonNull(connect).prepareStatement("SELECT * FROM Games where GameID = ?");
//      statement.setInt(1, game.getId());
//      ResultSet rs = statement.executeQuery();
//      String rom = null;
//      String custom = null;
//      if (rs.next()) {
//        rom = rs.getString("ROM");
//        custom = rs.getString("LaunchCustomVar");
//        return rom != null && !StringUtils.isEmpty(custom) && custom.equals("HIDEPUP");
//      }
//      rs.close();
//      statement.close();
//    }
//    catch (SQLException e) {
//      LOG.error("Failed to read \"LaunchCustomVar\": " + e.getMessage(), e);
//    }
//    finally {
//      this.disconnect(connect);
//    }
//    return false;
//  }

  @Override
  public List<FrontendPlayerDisplay> getFrontendPlayerDisplays() {
    List<FrontendPlayerDisplay> result = new ArrayList<>();
    try {
      INIConfiguration iniConfiguration = new INIConfiguration();
      iniConfiguration.setCommentLeadingCharsUsedInInput(";");
      iniConfiguration.setSeparatorUsedInOutput("=");
      iniConfiguration.setSeparatorUsedInInput("=");

      File ini = new File(getInstallationFolder(), "PinUpPlayer.ini");
      if (!ini.exists()) {
        LOG.error("Failed to find \"" + ini.getAbsolutePath() + "\", no display info found.");
        return result;
      }

      FileReader fileReader = new FileReader(ini);
      try {
        iniConfiguration.read(fileReader);
      }
      finally {
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
              display.setTechnicalName(section);
              display.setName(name);
              display.setScreen(VPinScreen.valueOfScreen(name));
              display.setX(sectionNode.getInt("ScreenXPos"));
              display.setY(sectionNode.getInt("ScreenYPos"));
              display.setWidth(sectionNode.getInt("ScreenWidth"));
              display.setHeight(sectionNode.getInt("ScreenHeight"));
              display.setRotation(sectionNode.getInt("ScreenRotation"));
            }
            else {
              LOG.warn("Unsupported PinUP display for screen '{}', display has been skipped.", name);
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

  /**
   * Creates a new entry in the PinUP Popper database.
   * Returns the id of the new entry.
   *
   * @return the generated game id.
   */
  public int importGame(int emulatorId, @NonNull String gameName, @NonNull String gameFileName, @NonNull String gameDisplayName, @Nullable String launchCustomVar, @NonNull java.util.Date dateFileUpdated) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("INSERT INTO Games (EMUID, GameName, GameFileName, GameDisplay, Visible, LaunchCustomVar, DateAdded, DateFileUpdated, " +
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
    }
    finally {
      this.disconnect(connect);
    }
    return -1;
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
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("DELETE FROM Games where GameID = ?");
      preparedStatement.setInt(1, gameId);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Deleted game entry with id " + gameId);
    }
    catch (Exception e) {
      LOG.error("Failed to update game table:" + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }

  private void deleteStats(int gameId) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("DELETE FROM GamesStats where GameID = ?");
      preparedStatement.setInt(1, gameId);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Deleted game stats entry with id " + gameId);
    }
    catch (Exception e) {
      LOG.error("Failed to update game stats table:" + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }


  public PlaylistGame getPlaylistGame(int playlistId, int gameId) {
    Connection connect = this.connect();
    String sql = "SELECT * FROM PlayListDetails WHERE GameID=" + gameId + " AND PlayListID=" + playlistId + ";";
    PlaylistGame game = null;
    try {
      Statement stmt = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        game = new PlaylistGame();
        game.setId(gameId);

        int favMode = rs.getInt(IS_FAV);
        game.setFav(favMode == 1);
        game.setGlobalFav(favMode == 2);
        break;
      }
      rs.close();
      stmt.close();
    }
    catch (Exception e) {
      LOG.error("Failed to read playlist game [" + sql + "]: " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return game;
  }

  @NonNull
  public Playlist clearPlaylist(int id) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("DELETE FROM PlayListDetails WHERE PlayListID = ?");
      preparedStatement.setInt(1, id);
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Cleared playlist {}", id);
    }
    catch (SQLException e) {
      LOG.error("Failed to update playlist details: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return getPlaylist(id);
  }

  @NonNull
  public Playlist getPlaylist(int id) {
    Playlist playlist = new Playlist();
    Connection connect = this.connect();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Playlists WHERE PlayListID = " + id + ";");
      while (rs.next()) {
        playlist = createPlaylist(rs, null, null);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get playlist: " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return playlist;
  }

  @NonNull
  public Playlist getPlaylistTree() {
    List<Playlist> result = new ArrayList<>();
    List<Playlist> playLists = getPlaylists().stream().filter(p -> p.getId() >= 0).collect(Collectors.toList());
    Playlist root = playLists.stream().filter(p -> p.getParentId() == -1).findFirst().get();
    result.add(root);
    buildPlaylistTree(root);
    return root;
  }

  private void buildPlaylistTree(Playlist parent) {
    List<Playlist> children = getPlaylistChildren(parent.getId());
    for (Playlist playList : children) {
      parent.getChildren().add(playList);
      buildPlaylistTree(playList);
    }
  }

  @NonNull
  private List<Playlist> getPlaylistChildren(long parentId) {
    Connection connect = this.connect();
    List<Playlist> result = new ArrayList<>();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Playlists WHERE PlayListParent = " + parentId);
      while (rs.next()) {
        Playlist playlist = createPlaylist(rs, null, null);
        result.add(playlist);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get playlist: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    Collections.sort(result, Comparator.comparingInt(Playlist::getDisplayOrder));
    return result;
  }

  @NonNull
  public List<Playlist> getPlaylists() {
    Connection connect = this.connect();
    List<Playlist> result = new ArrayList<>();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Playlists;");

      Playlist favsPlaylist = new Playlist();
      favsPlaylist.setId(PlaylistRepresentation.PLAYLIST_FAVORITE_ID);
      favsPlaylist.setName("Playlist Favorites");

      Playlist globalFavsPlaylist = new Playlist();
      globalFavsPlaylist.setId(PlaylistRepresentation.PLAYLIST_GLOBALFAV_ID);
      globalFavsPlaylist.setName("Global Favorites");

      result.add(favsPlaylist);
      result.add(globalFavsPlaylist);

      while (rs.next()) {
        Playlist playlist = createPlaylist(rs, globalFavsPlaylist, favsPlaylist);
        result.add(playlist);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get playlist: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }

    Collections.sort(result, Comparator.comparingInt(Playlist::getDisplayOrder));
    return result;
  }


  private Map<Integer, PlaylistGame> updateSQLPlaylist(Playlist playlist, String sql, Map<Integer, PlaylistGame> playlistGameMap) {
    if (StringUtils.isEmpty(sql)) {
      return playlistGameMap;
    }

    //fetch the ids of tables applicable for this playlist
    List<Integer> sqlPlaylistIds = getGameIdsFromSqlPlaylist(playlist, sql);
    Map<Integer, PlaylistGame> updated = new HashMap<>();
    for (Integer gameId : sqlPlaylistIds) {
      if (playlistGameMap.containsKey(gameId)) {
        playlistGameMap.get(gameId).setPlayed(true);
        updated.put(gameId, playlistGameMap.get(gameId));
      }
      else {
        PlaylistGame playlistGame = new PlaylistGame();
        playlistGame.setId(gameId);
        playlistGame.setPlayed(false);
        playlistGame.setFav(false);
        playlistGame.setGlobalFav(false);
        updated.put(gameId, playlistGame);
      }
    }
    return updated;
  }

  public void addToPlaylist(int playlistId, int gameId, int favMode) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("INSERT INTO PlayListDetails (PlayListID, GameID, Visible, DisplayOrder, NumPlayed, " + IS_FAV + ") VALUES (?,?,?,?,?,?)");
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
    }
    finally {
      this.disconnect(connect);
    }
  }

  public void updatePlaylistGame(int playlistId, int gameId, int favMode) {
    Connection connect = this.connect();
    String sql = "UPDATE PlayListDetails SET " + IS_FAV + " = " + favMode + " WHERE GameID=" + gameId + " AND PlayListID=" + playlistId + ";";
    try {
      Statement stmt = Objects.requireNonNull(connect).createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
    }
    catch (Exception e) {
      LOG.error("Failed to update playlist [" + sql + "]: " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }

  public void deleteFromPlaylists(int gameId) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("DELETE FROM PlayListDetails WHERE GameID = ?");
      preparedStatement.setInt(1, gameId);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Removed game " + gameId + " from all playlists");
    }
    catch (SQLException e) {
      LOG.error("Failed to update playlist details: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }


  @Override
  public void savePlaylistOrder(PlaylistOrder playlistOrder) {
    Map<Integer, Integer> playlistToOrderId = playlistOrder.getPlaylistToOrderId();
    Set<Map.Entry<Integer, Integer>> entries = playlistToOrderId.entrySet();
    for (Map.Entry<Integer, Integer> entry : entries) {
      int id = entry.getKey();
      Playlist playlist = getPlaylist(id);
      playlist.setDisplayOrder(entry.getValue());
      savePlaylist(playlist);
      LOG.info("Updated {} to displayOrder {}", playlist, entry.getValue());
    }
  }

  @Override
  public Playlist savePlaylist(Playlist playlist) {
    Connection connect = this.connect();
    try {
      int index = 1;

      PreparedStatement preparedStatement = null;
      if (playlist.getId() < 0) {
        preparedStatement = Objects.requireNonNull(connect).prepareStatement("INSERT OR REPLACE INTO PlayLists (" +
                "PlayName, Visible, DisplayOrder, Logo, PlayListParent, PlayDisplay, Notes, PlayListType, PlayListSQL, MenuColor, passcode, UglyList, HideSysLists, ThemeFolder, useDefaults, DOFStuff) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
            , Statement.RETURN_GENERATED_KEYS);
      }
      else {
        preparedStatement = Objects.requireNonNull(connect).prepareStatement("INSERT OR REPLACE INTO PlayLists (" +
                "PlayListID, PlayName, Visible, DisplayOrder, Logo, PlayListParent, PlayDisplay, Notes, PlayListType, PlayListSQL, MenuColor, passcode, UglyList, HideSysLists, ThemeFolder, useDefaults, DOFStuff) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
            , Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(index++, playlist.getId());
      }

      preparedStatement.setString(index++, playlist.getName());
      preparedStatement.setInt(index++, playlist.isVisible() ? 1 : 0);
      preparedStatement.setInt(index++, playlist.getDisplayOrder()); // display order - not used yet
      preparedStatement.setString(index++, playlist.getMediaName());
      preparedStatement.setInt(index++, playlist.getParentId());
      preparedStatement.setString(index++, playlist.getName()); //looks the "PlayDisplay" is always the name
      preparedStatement.setString(index++, ""); //no field for notes
      preparedStatement.setInt(index++, playlist.isSqlPlayList() ? 1 : 0);
      preparedStatement.setString(index++, playlist.getPlayListSQL());
      preparedStatement.setInt(index++, playlist.getMenuColor() != null ? playlist.getMenuColor() :  Integer.valueOf("FFFFFF", 16));
      preparedStatement.setInt(index++, playlist.getPassCode());
      preparedStatement.setInt(index++, playlist.isUglyList() ? 1 : 0);
      preparedStatement.setInt(index++, playlist.isHideSysLists() ? 1 : 0);
      preparedStatement.setString(index++, null); //theme folder not used yet
      preparedStatement.setInt(index++, playlist.isUseDefaults() ? 1 : 0);
      preparedStatement.setString(index++, StringUtils.isEmpty(playlist.getDofCommand()) ? null : playlist.getDofCommand());
      preparedStatement.executeUpdate();
      preparedStatement.close();

      try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
        if (keys.next()) {
          int id = keys.getInt(1);
          return getPlaylist(id);
        }
      }

      return getPlaylist(playlist.getId());
    }
    catch (Exception e) {
      LOG.error("Failed to update playlist: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return null;
  }

  public boolean deletePlaylist(int playlistId) {
    List<Integer> ids = new ArrayList<>();
    ids.add(playlistId);
    Connection connect = this.connect();
    try {
      //maybe recursive deletion in the future?
      for (Integer id : ids) {
        PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("DELETE FROM PlayLists WHERE PlayListID = ?");
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
        preparedStatement.close();

        preparedStatement = Objects.requireNonNull(connect).prepareStatement("DELETE FROM PlayListDetails WHERE PlayListID = ?");
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        LOG.info("Deleted playlist {}", playlistId);
      }
      return true;
    }
    catch (SQLException e) {
      LOG.error("Failed to update playlist details: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return false;
  }

  public void deleteFromPlaylist(int playlistId, int gameId) {
    Connection connect = this.connect();
    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("DELETE FROM PlayListDetails WHERE GameID = ? AND PlayListID = ?");
      preparedStatement.setInt(1, gameId);
      preparedStatement.setInt(2, playlistId);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Removed game " + gameId + " from playlist " + playlistId);
    }
    catch (SQLException e) {
      LOG.error("Failed to update playlist details: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }

  // no more used
  private Playlist getPlayListForGame(int gameId) {
    Playlist result = null;
    Connection connect = connect();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
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
      LOG.error("Failed to read playlist for gameId: {}", e.getMessage(), e);
    }
    finally {
      disconnect(connect);
    }
    return result;
  }

  @Override
  @NonNull
  public List<GameEmulator> getEmulators() {
    Connection connect = this.connect();
    List<GameEmulator> result = new ArrayList<>();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators;");
      while (rs.next()) {
        GameEmulator e = createEmulatorFromResultSet(rs);
        result.add(e);
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get function: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }

    //this can not be executed within a fetch!!!
    for (GameEmulator emulator : result) {
      if (emulator.getType().isVpxEmulator() || emulator.getType().isFpEmulator()) {
        initEmulatorScripts(emulator);
      }
    }

    return result;
  }

  @Override
  public boolean deleteEmulator(int emulatorId) {
    Connection connect = this.connect();
    try {
      //maybe recursive deletion in the future?
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("DELETE FROM Emulators WHERE EMUID = ?");
      preparedStatement.setInt(1, emulatorId);
      preparedStatement.executeUpdate();
      preparedStatement.close();
      LOG.info("Deleted emulator {}", emulatorId);


      PreparedStatement preparedStatementGames = Objects.requireNonNull(connect).prepareStatement("DELETE FROM Games WHERE EMUID = ?");
      preparedStatementGames.setInt(1, emulatorId);
      preparedStatementGames.executeUpdate();
      preparedStatementGames.close();
      LOG.info("Deleted games from emulator {}", emulatorId);

      return true;
    }
    catch (SQLException e) {
      LOG.error("Failed to delete emulator: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return false;
  }

  /**
   * "EMUID"	INTEGER NOT NULL,
   * "EmuName"	VARCHAR(100) NOT NULL COLLATE NOCASE,
   * "Description"	VARCHAR(200),
   * "DirGames"	VARCHAR(250),
   * "DirMedia"	VARCHAR(255),
   * "EmuDisplay"	VARCHAR(200) COLLATE NOCASE,
   * "Visible"	INTEGER DEFAULT (1),
   * "DirRoms"	VARCHAR(250),
   * "EmuLaunchDir"	VARCHAR(250),
   * "HideScreens"	INTEGER,
   * "GamesExt"	VARCHAR(200),
   * "ImageExt"	VARCHAR(25),
   * "VideoExt"	VARCHAR(25),
   * "EscapeKeyCode"	INTEGER,
   * "LaunchScript"	TEXT,
   * "PostScript"	TEXT,
   * "KeepDisplays"	VARCHAR(20),
   * "ProcessName"	VARCHAR(50),
   * "WinTitle"	VARCHAR(50),
   * "SkipScan"	INTEGER,
   * "emuVolume"	INTEGER DEFAULT (-1),
   * "DirGamesShare"	VARCHAR(250),
   * "DirRomsShare"	VARCHAR(250),
   * "DirMediaShare"	VARCHAR(250),
   * "CanPause"	INTEGER DEFAULT 0,
   * "CoreFile"	VARCHAR(250),
   * "HelpScript"	TEXT,
   * "AutoScanStartup"	INTEGER DEFAULT 0,
   * "IgnoreFileScan"	TEXT,
   * "SafeLaunch"	INTEGER DEFAULT 0,
   * "SafeReturn"	INTEGER DEFAULT 0,
   *
   * @param emulator
   * @return
   */
  @Override
  public GameEmulator saveEmulator(GameEmulator emulator) {
    Connection connect = this.connect();
    try {
      int index = 1;

      PreparedStatement preparedStatement = null;
      if (emulator.getId() < 0) {
        preparedStatement = Objects.requireNonNull(connect).prepareStatement("INSERT INTO Emulators (" +
                "EmuName, Description, DirGames, DirMedia, EmuDisplay, Visible, DirRoms, EmuLaunchDir, GamesExt, LaunchScript, PostScript) values (?,?,?,?,?,?,?,?,?,?,?)"
            , Statement.RETURN_GENERATED_KEYS);
      }
      else {
        preparedStatement = Objects.requireNonNull(connect).prepareStatement("INSERT OR REPLACE INTO Emulators (" +
                "EMUID, EmuName, Description, DirGames, DirMedia, EmuDisplay, Visible, DirRoms, EmuLaunchDir, GamesExt, LaunchScript, PostScript) values (?,?,?,?,?,?,?,?,?,?,?,?)"
            , Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(index++, emulator.getId());
      }

      preparedStatement.setString(index++, emulator.getSafeName());
      preparedStatement.setString(index++, emulator.getDescription());
      preparedStatement.setString(index++, emulator.getGamesDirectory());
      preparedStatement.setString(index++, emulator.getMediaDirectory());
      preparedStatement.setString(index++, emulator.getName());
      preparedStatement.setInt(index++, emulator.isEnabled() ? 1 : 0);
      preparedStatement.setString(index++, emulator.getRomDirectory());
      preparedStatement.setString(index++, emulator.getInstallationDirectory());
      preparedStatement.setString(index++, emulator.getGameExt());
      preparedStatement.setString(index++, emulator.getLaunchScript() != null ? emulator.getLaunchScript().getScript() : null);
      preparedStatement.setString(index++, emulator.getExitScript() != null ? emulator.getExitScript().getScript() : null);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Saved " + emulator);
      try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
        if (keys.next()) {
          int id = keys.getInt(1);
          return getEmulator(id);
        }
      }

      return getEmulator(emulator.getId());
    }
    catch (Exception e) {
      LOG.error("Failed to update emulator: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return null;
  }

  @Override
  @NonNull
  public GameEmulator getEmulator(int emuId) {
    Connection connect = this.connect();
    GameEmulator emulator = null;
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM Emulators WHERE EMUID = " + emuId + ";");
      if (rs.next()) {
        emulator = createEmulatorFromResultSet(rs);
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to getEmulator: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    if (emulator != null && (emulator.getType().isVpxEmulator() || emulator.getType().isFpEmulator())) {
      initEmulatorScripts(emulator);
    }
    return emulator;
  }

  private GameEmulator createEmulatorFromResultSet(ResultSet rs) throws SQLException {
    String emuName = rs.getString("EmuName");
    String dirGames = rs.getString("DirGames");
    String extension = rs.getString("GamesExt");

    EmulatorType type = getEmulatorType(emuName, dirGames, extension);

    GameEmulator e = new GameEmulator();
    e.setType(type);
    e.setId(rs.getInt("EMUID"));
    e.setSafeName(emuName);
    e.setGamesDirectory(dirGames);
    e.setGameExt(extension);
    e.setName(rs.getString("EmuDisplay"));
    e.setMediaDirectory(rs.getString("DirMedia"));
    e.setRomDirectory(rs.getString("DirRoms"));
    e.setDescription(rs.getString("Description"));
    e.setInstallationDirectory(rs.getString("EmuLaunchDir"));
    e.getLaunchScript().setScript(rs.getString("LaunchScript"));
    e.getExitScript().setScript(rs.getString("PostScript"));
    e.setEnabled(rs.getInt("Visible") == 1);

    // specific initialization
    setEmulatorExe(e, rs);
    return e;
  }

  private void setEmulatorExe(GameEmulator e, ResultSet rs) throws SQLException {
    if (e.getType().equals(EmulatorType.VisualPinball)) {
      String exeName = "VPinballX64.exe";

      //parsing of the specific popper script
      String launchScript = rs.getString("LaunchScript");
      if (StringUtils.isNotEmpty(launchScript)) {
        Pattern pattern = Pattern.compile("\\b(\\w+)=(\\w+)\\b");
        Matcher m = pattern.matcher(launchScript);
        while (m.find()) {
          String key = m.group(1);
          String value = m.group(2);
          if (key != null && key.equals("VPXEXE") && value != null) {
            exeName = value.trim() + ".exe";
          }
        }
      }
      e.setExeName(exeName);
    }
    else if (e.getType().equals(EmulatorType.FuturePinball)) {
      e.setExeName("Future Pinball.exe");
    }
  }

  @NonNull
  private EmulatorType getEmulatorType(String emuName, String dirGames, String extension) {
    EmulatorType type = EmulatorType.fromExtension(extension);
    if (type != null) {
      return type;
    }

    type = EmulatorType.fromName(emuName);
    if (type != null) {
      return type;
    }

    for (EmulatorType t : EmulatorType.values()) {
      if (t.getExtension() != null && dirGames != null) {
        String ext = t.getExtension().toLowerCase();
        final Path[] fileFound = {null};
        try {
          Files.walkFileTree(Paths.get(dirGames), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
              if (StringUtils.endsWithIgnoreCase(file.toString(), ext)) {
                fileFound[0] = file;
                return FileVisitResult.TERMINATE;
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
              return FileVisitResult.CONTINUE;
            }
          });
        }
        catch (IOException ioe) {
          LOG.error("Encountered exception while traversing {}", dirGames, ioe);
        }
        if (fileFound[0] != null) {
          return t;
        }
      }
    }
    return EmulatorType.OTHER;
  }

  @NonNull
  public List<TableAlxEntry> getAlxData() {
    Connection connect = this.connect();
    List<TableAlxEntry> result = new ArrayList<>();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
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
      LOG.error("Failed to get alx data: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return result;
  }

  @NonNull
  public List<TableAlxEntry> getAlxData(int gameId) {
    Connection connect = this.connect();
    List<TableAlxEntry> result = new ArrayList<>();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
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
      LOG.error("Failed to get alx data: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return result;
  }

  @Override
  public boolean updateNumberOfPlaysForGame(int gameId, long value) {
    Connection connect = this.connect();
    String sql = "UPDATE GamesStats SET 'NumberPlays'=" + value + " WHERE GameID = " + gameId;
    try {
      Statement stmt = Objects.requireNonNull(connect).createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
      LOG.info("Update of NumberPlays for '{}' successful.", gameId);
    }
    catch (Exception e) {
      LOG.error("Failed to update NumberPlays for " + gameId + " [" + sql + "]: " + e.getMessage(), e);
      return false;
    }
    finally {
      this.disconnect(connect);
    }
    return true;
  }

  @Override
  public boolean updateSecondsPlayedForGame(int gameId, long seconds) {
    Connection connect = this.connect();
    String sql = "UPDATE GamesStats SET 'TimePlayedSecs'=" + seconds + " WHERE GameID = " + gameId;
    try {
      Statement stmt = Objects.requireNonNull(connect).createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
      LOG.info("Update of TimePlayedSecs to {} for {} successful.", seconds, gameId);
    }
    catch (Exception e) {
      LOG.error("Failed to update TimePlayedSecs for " + gameId + " [" + sql + "]: " + e.getMessage(), e);
      return false;
    }
    finally {
      this.disconnect(connect);
    }
    return true;
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
//      Statement stmt = Objects.requireNonNull(connect).createStatement();
//      stmt.executeUpdate(sql);
//      stmt.close();
//      LOG.info("Enabled PC Games emulator for popper.");
//    } catch (Exception e) {
//      LOG.error("Failed to update script script [" + sql + "]: " + e.getMessage(), e);
//    } finally {
//      this.disconnect(connect);
//    }
//  }

  @Nullable
  public FrontendControl getFrontendControl(@NonNull String description) {
    FrontendControl f = null;
    Connection connect = this.connect();
    try {
      PreparedStatement statement = Objects.requireNonNull(connect).prepareStatement("SELECT * FROM PinUPFunctions WHERE Descript = ?");
      statement.setString(1, description);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        f = new FrontendControl();
        f.setActive(rs.getInt("Active") == 1);
        f.setDescription(rs.getString("Descript"));
        f.setCtrlKey(rs.getInt("CntrlCodes"));
        f.setJoyCode(rs.getInt("JoyCodes"));
        f.setId(rs.getInt("uniqueID"));
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get function: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return f;
  }


  public FrontendControl getFrontendControlFor(VPinScreen screen) {
    switch (screen) {
      case Other2: {
        return getFrontendControl(FrontendControl.FUNCTION_SHOW_OTHER);
      }
      case GameHelp: {
        return getFrontendControl(FrontendControl.FUNCTION_SHOW_HELP);
      }
      case GameInfo: {
        return getFrontendControl(FrontendControl.FUNCTION_SHOW_FLYER);
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
      Statement statement = Objects.requireNonNull(connect).createStatement();
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
      LOG.error("Failed to functions: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return controls;
  }

  @Override
  public int getGameCount(int emuId) {
    int count = 0;
    Connection connect = this.connect();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT count(*) as count FROM Games WHERE EMUID = " + emuId + ";");
      while (rs.next()) {
        count = count + rs.getInt("count");
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game count for emulator {}: {}", emuId, e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return count;
  }

  @Override
  public List<Integer> getGameIds(int emuId) {
    List<Integer> result = new ArrayList<>();
    Connection connect = this.connect();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT GameID FROM Games WHERE EMUID = " + emuId + ";");
      while (rs.next()) {
        result.add(rs.getInt("GameID"));
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read game count: {}", e.getMessage(), e);
    }
    finally {
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
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery(
          "SELECT g.*, s.*, e.Visible as EmuVisible, e.DirGames FROM Games g"
              + " left join Emulators e on g.EMUID=e.EMUID left join GamesStats s on s.GameID = g.GameID ");
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
      LOG.error("Failed to get games: {}", e.getMessage(), e);
    }
    finally {
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
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT DateAdded from Games asc limit 1;");
      while (rs.next()) {
        date = getDate(rs, "DateAdded");
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to get start time: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }

    return date;
  }

  @Override
  public void deleteGames(int emuId) {
    Connection connect = this.connect();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      statement.execute("DELETE FROM Games WHERE EMUID = " + emuId);
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to delete games: {}", e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
  }

  @NonNull
  private Map<Integer, PlaylistGame> getGamesFromPlaylist(int id) {
    Map<Integer, PlaylistGame> result = new LinkedHashMap<>();
    Connection connect = connect();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PlayListDetails WHERE PlayListID = " + id);

      while (rs.next()) {
        try {
          PlaylistGame game = new PlaylistGame();
          int gameId = rs.getInt("GameID");
          game.setId(gameId);

          Integer favMode = rs.getInt(IS_FAV);
          if (rs.wasNull()) {
            favMode = 0;
          }
          game.setFav(favMode == 1);
          game.setGlobalFav(favMode == 2);
          game.setPlayed(true);

          result.put(gameId, game);
        }
        catch (SQLException e) {
          LOG.error("Failed to read playlist {}: {}", id, e.getMessage(), e);
        }
      }

      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read playlists: {}", e.getMessage(), e);
    }
    finally {
      disconnect(connect);
    }
    return result;
  }

  private List<Integer> getGameIdsFromSqlPlaylist(Playlist playlist, String sql) {
    if (StringUtils.isEmpty(sql)) {
      return Collections.emptyList();
    }
    List<Integer> result = new ArrayList<>();
    Connection connect = connect();
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = Objects.requireNonNull(connect).createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        int gameId = rs.getInt("GameID");
        result.add(gameId);
      }
      rs.close();
      statement.close();
    }
    catch (Exception e) {
      try {
        if (rs != null) {
          rs.close();
        }
      }
      catch (SQLException ex) {
        //ignore
      }
      try {
        if (statement != null) {
          statement.close();
        }

      }
      catch (SQLException ex) {
        //ignore
      }
      LOG.warn("Failed to read playlists: {}", e.getMessage());
      playlist.setSqlError(e.getMessage());
    }
    finally {
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
      Statement statement = Objects.requireNonNull(connect).createStatement();
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
    }
    finally {
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
      Statement statement = Objects.requireNonNull(connect).createStatement();
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
    }
    finally {
      this.disconnect(connect);
    }
    return script;
  }

  public void updateScript(@NonNull String emuName, @NonNull String scriptName, @NonNull String content) {
    Connection connect = this.connect();
    String sql = "UPDATE Emulators SET " + scriptName + "=";

    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement(sql + "? WHERE EmuName=?");
      preparedStatement.setString(1, content);
      preparedStatement.setString(2, emuName);
      preparedStatement.executeUpdate();
      preparedStatement.close();

      LOG.info("Update of " + scriptName + " for '" + emuName + "' successful.");
    }
    catch (Exception e) {
      LOG.error("Failed to update script " + scriptName + " [" + sql + "]: " + e.getMessage(), e);
    }
    finally {
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

  @NonNull
  private Playlist createPlaylist(ResultSet rs, Playlist globalFavsPlaylist, Playlist favsPlaylist) throws SQLException {
    Playlist playlist = new Playlist();
    String sql = rs.getString("PlayListSQL");
    String name = rs.getString("PlayName");
    boolean sqlPlaylist = rs.getInt("PlayListType") == 1;
    playlist.setId(rs.getInt("PlayListID"));
    playlist.setParentId(rs.getInt("PlayListParent"));
    playlist.setMediaName(rs.getString("Logo"));
    playlist.setVisible(rs.getInt("Visible") == 1);
    playlist.setPassCode(rs.getInt("passcode"));
    playlist.setUglyList(rs.getInt("UglyList") == 1);
    Integer displayOrder = rs.getInt("DisplayOrder");
    playlist.setDisplayOrder(displayOrder);
    playlist.setHideSysLists(rs.getInt("HideSysLists") == 1);
    playlist.setUseDefaults(rs.getInt("useDefaults") == 1);
    playlist.setDofCommand(rs.getString("DOFStuff"));
    playlist.setName(name);
    playlist.setPlayListSQL(sql);
    playlist.setMenuColor(rs.getInt("MenuColor"));
    if (rs.wasNull()) {
      playlist.setMenuColor(null);
    }
    playlist.setSqlPlayList(sqlPlaylist);

    Map<Integer, PlaylistGame> playlistGameMap = getGamesFromPlaylist(playlist.getId());
    if (sqlPlaylist) {
      playlistGameMap = updateSQLPlaylist(playlist, sql, playlistGameMap);
    }
    playlist.setGames(new ArrayList<>(playlistGameMap.values()));

    // used for playlist management, moved from TablesSidebarPlaylistsController as it is pinup specific
    playlist.setAddFavCheckboxes(sqlPlaylist && sql != null && sql.contains("EMUID"));

    if (playlist.isSqlPlayList() && StringUtils.isEmpty(sql)) {
      playlist.setSqlError("Missing SQL query");
    }

    if (globalFavsPlaylist != null && favsPlaylist != null) {
      List<PlaylistGame> games = playlist.getGames();
      for (PlaylistGame game : games) {
        if (game.isFav()) {
          favsPlaylist.getGames().add(game);
        }
        if (game.isGlobalFav()) {
          globalFavsPlaylist.getGames().add(game);
        }
      }
    }

    return playlist;
  }

  @NonNull
  private Game createGame(@NonNull ResultSet rs) throws SQLException {
    int emuId = rs.getInt("EMUID");
    boolean emuVisible = rs.getInt("EmuVisible") == 1;

    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    Game game = new Game();
    //game.setMediaStrategy(pinUPMediaAccessStrategy);
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

    String custom = rs.getString("LaunchCustomVar");
    game.setPupPackDisabled(!StringUtils.isEmpty(custom) && custom.equals("HIDEPUP"));

    String gameDisplayName = rs.getString("GameDisplay");
    game.setGameDisplayName(gameDisplayName);

    String gameName = rs.getString("GameName");
    game.setGameName(gameName);
    game.setDateAdded(getDate(rs, "DateAdded"));
    game.setDateUpdated(getDate(rs, "DateUpdated"));

    game.setVersion(rs.getString("GAMEVER"));
    game.setAltLauncherExe(rs.getString("ALTEXE"));
    game.setNumberPlayed(rs.getInt("NumberPlays"));

    game.setGameStatus(rs.getInt("Visible"));
    if (rs.wasNull()) {
      game.setGameStatus(-1);
    }

    game.setRating(rs.getInt("GameRating"));
    if (rs.wasNull()) {
      game.setRating(0);
    }

    String dirGame = rs.getString("DirGames");
    File vpxFile = new File(dirGame, gameFileName);
    game.setGameFile(vpxFile);

    if (!StringUtils.isEmpty(serverSettings.getMappingVpsTableId())) {
      game.setExtTableId(rs.getString(serverSettings.getMappingVpsTableId()));
    }
    if (!StringUtils.isEmpty(serverSettings.getMappingVpsTableVersionId())) {
      game.setExtTableVersionId(rs.getString(serverSettings.getMappingVpsTableVersionId()));
    }
    if (!StringUtils.isEmpty(serverSettings.getMappingHsFileName())) {
      game.setHsFileName(rs.getString(serverSettings.getMappingHsFileName()));
    }
    if (!StringUtils.isEmpty(serverSettings.getMappingPatchVersion())) {
      game.setPatchVersion(rs.getString(serverSettings.getMappingPatchVersion()));
    }

    if (sqlVersion >= DB_VERSION) {
      String tourneyId = rs.getString("TourneyID");
      game.setCompetitionTypes(CompetitionIdFactory.getCompetitionTypes(tourneyId));
    }

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

  private Map<String, String> getLookups() {
    Connection connect = this.connect();
    Map<String, String> results = new HashMap<>();
    try {
      Statement statement = Objects.requireNonNull(connect).createStatement();
      ResultSet rs = statement.executeQuery("SELECT * FROM PupLookups;");
      if (rs.next()) {
        results.put("GameType", rs.getString("GameType"));
        results.put("GameTheme", rs.getString("GameTheme"));
        results.put("Manufact", rs.getString("Manufact"));
        results.put("Category", rs.getString("Category"));
        results.put("Custom1", rs.getString("Custom1"));
        results.put("Custom2", rs.getString("Custom2"));
        results.put("Custom3", rs.getString("Custom3"));
        results.put("AltExe", rs.getString("Altexe"));
      }
      rs.close();
      statement.close();
    }
    catch (SQLException e) {
      LOG.error("Failed to read lookups: " + e.getMessage(), e);
    }
    finally {
      this.disconnect(connect);
    }
    return results;
  }

  private void importGameExtraValues(Connection connect, int gameId, String gLog, String gNotes, String gPlayLog, String gDetails) {
    try {
      PreparedStatement preparedStatement = Objects.requireNonNull(connect).prepareStatement("insert or replace into GamesExtra (GameID, gLOG, gNotes, gPlayLog, gDetails) values (?,?,?,?,?)");
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
    }
  }

  public List<FrontendPlayerDisplay> getPupPlayerDisplays() {
    List<FrontendPlayerDisplay> result = new ArrayList<>();
    try {
      INIConfiguration iniConfiguration = new INIConfiguration();
      iniConfiguration.setCommentLeadingCharsUsedInInput(";");
      iniConfiguration.setSeparatorUsedInOutput("=");
      iniConfiguration.setSeparatorUsedInInput("=");

      File ini = new File(getInstallationFolder(), "PinUpPlayer.ini");
      if (!ini.exists()) {
        LOG.error("Failed to find \"" + ini.getAbsolutePath() + "\", no display info found.");
        return result;
      }

      FileReader fileReader = new FileReader(ini);
      try {
        iniConfiguration.read(fileReader);
      }
      finally {
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
              display.setScreen(VPinScreen.valueOfScreen(name));
              display.setX(sectionNode.getInt("ScreenXPos"));
              display.setY(sectionNode.getInt("ScreenYPos"));
              display.setWidth(sectionNode.getInt("ScreenWidth"));
              display.setHeight(sectionNode.getInt("ScreenHeight"));
              display.setRotation(sectionNode.getInt("ScreenRotation"));
            }
            else {
              LOG.warn("Unsupported PinUP display for screen '{}', display has been skipped.", name);
            }
            result.add(display);
          }
          catch (Exception e) {
            LOG.error("Failed to create PinUPPlayerDisplay: {}", e.getMessage());
          }
        }
      }

      LOG.info("Loaded " + result.size() + " PinUPPlayer displays.");
    }
    catch (Exception e) {
      LOG.error("Failed to get player displays: {}", e.getMessage(), e);
    }
    return result;
  }

  @Override
  public MediaAccessStrategy getMediaAccessStrategy() {
    if (this.pinUPMediaAccessStrategy == null) {
      this.pinUPMediaAccessStrategy = new PinUPMediaAccessStrategy(getInstallationFolder());
    }
    return pinUPMediaAccessStrategy;
  }

  @Override
  public TableAssetsAdapter getTableAssetAdapter() {
    return assetsAdapter;
  }

  public void initializeConnector() {
    File file = getDatabaseFile();
    dbFilePath = file.getAbsolutePath().replaceAll("\\\\", "/");

    sqlVersion = this.getVersion();

    try {
      Class<?> aClass = Class.forName("de.mephisto.vpin.popper.PopperAssetAdapter");
      TableAssetsAdapter assetAdapter = (TableAssetsAdapter) aClass.getDeclaredConstructor().newInstance();
      // add cache to Popper search adapter
      this.assetsAdapter = new CacheTableAssetsAdapter(assetAdapter);
    }
    catch (Exception e) {
      LOG.error("Unable to find PopperAssetAdapter: " + e.getMessage());
    }
  }

  public File getDatabaseFile() {
    return new File(getInstallationFolder(), "PUPDatabase.db");
  }

  public Frontend getFrontend() {
    Frontend frontend = new Frontend();
    frontend.setName("PinUP Popper");
    frontend.setInstallationDirectory(getInstallationFolder().getAbsolutePath());
    frontend.setFrontendType(FrontendType.Popper);

    frontend.setFrontendExe("PinUpMenu.exe");
    frontend.setAdminExe("PinUpMenuSetup.exe");
    frontend.setIconName("popper.png");
    frontend.setSupportedScreens(Arrays.asList(VPinScreen.values()));
    frontend.setSystemVolumeControlEnabled(isSystemVolumeControlled());

    Map<String, String> lookups = getLookups();
    frontend.getFieldLookups().getGameType().addAll(toList(lookups, "GameType"));
    frontend.getFieldLookups().getGameTheme().addAll(toList(lookups, "GameTheme"));
    frontend.getFieldLookups().getManufacturer().addAll(toList(lookups, "Manufact"));
    frontend.getFieldLookups().getCategory().addAll(toList(lookups, "Category"));
    frontend.getFieldLookups().getCustom1().addAll(toList(lookups, "Custom1"));
    frontend.getFieldLookups().getCustom2().addAll(toList(lookups, "Custom2"));
    frontend.getFieldLookups().getCustom3().addAll(toList(lookups, "Custom3"));
    frontend.getFieldLookups().getAltExe().addAll(toList(lookups, "AltExe"));

    return frontend;
  }

  private List<String> toList(Map<String, String> lookups, String key) {
    List<String> result = new ArrayList<>();
    if (lookups.containsKey(key)) {
      String value = lookups.get(key);
      if (value != null) {
        List<String> values = Arrays.stream(value.split("\n")).map(StringUtils::trim).filter(v -> !StringUtils.isEmpty(v)).collect(Collectors.toList());
        result.addAll(values);
      }
    }
    return result;
  }

  @Override
  public boolean killFrontend() {
    NirCmd.setTaskBarVisible(true);
    pupEventEmitter.sendPupEvent(11, 2);
    List<ProcessHandle> pinUpProcesses = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() &&
            (
                p.info().command().get().contains("PinUpMenu") ||
                    p.info().command().get().contains("PinUpDisplay") ||
                    p.info().command().get().contains("PinUpPlayer") ||
                    p.info().command().get().contains("VPXStarter") ||
//                    p.info().command().get().contains(PupServer.EXE_NAME) ||
                    p.info().command().get().contains("PinUpPackEditor") ||
                    p.info().command().get().contains("VPinballX") ||
                    p.info().command().get().startsWith("VPinball") ||
                    p.info().command().get().contains("Future Pinball") ||
                    p.info().command().get().contains("B2SBackglassServerEXE"))).collect(Collectors.toList());

    if (pinUpProcesses.isEmpty()) {
      LOG.info("No remaining PinUP processes found, termination finished.");
      return true;
    }

    for (ProcessHandle pinUpProcess : pinUpProcesses) {
      String cmd = pinUpProcess.info().command().get();
      boolean b = pinUpProcess.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
    }

    //actually redundant, but who knows what else is in there
    File showTaskbarExe = new File(getInstallationFolder(), "showtaskbar.exe");
    if (showTaskbarExe.exists()) {
      SystemCommandExecutor exec = new SystemCommandExecutor(Arrays.asList("showtaskbar.exe"));
      exec.setDir(getInstallationFolder());
      exec.executeCommandAsync();
    }
    else {
      LOG.info("Popper '" + showTaskbarExe.getAbsolutePath() + "' not found, nircmd.exe has been used before.");
    }

    return true;
  }

  @Override
  public boolean isFrontendRunning() {
    List<ProcessHandle> allProcesses = systemService.getProcesses();
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        if (cmdName.contains(PIN_UP_MENU)) {
          return true;
        }
      }
    }
    return false;
  }

  //-------------------------------
  // Recording

  @Override
  public boolean startFrontendRecording() {
    return restartFrontend(true);
  }

  @Override
  public boolean startGameRecording(Game game) {
    return launchGame(game, true);
  }

  @Override
  public void endGameRecording(Game game) {

  }

  @Override
  public void endFrontendRecording() {
    killFrontend();
  }

  //-------------------------------

  @Override
  public boolean restartFrontend() {
    killFrontend();

    try {
      List<String> params = Arrays.asList("cmd", "/c", "start", "PinUpMenu.exe");
      SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
      executor.setDir(getInstallationFolder());
      executor.executeCommandAsync();

      systemService.waitForProcess("PinUpMenu.exe", 5, 3000);
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Popper restart failed: {}", standardErrorFromCommand);
        return false;
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to start PinUP Popper again: " + e.getMessage(), e);
    }
    return false;
  }

  private boolean restartFrontend(boolean wait) {
    restartFrontend();
    if (wait) {
      if (!systemService.waitForProcess(PIN_UP_MENU, 10)) {
        LOG.warn("Failed to launch frontend, timeout.");
        return false;
      }
    }
    return true;
  }

  /**
   * Alternative
   * <p>
   * <p>
   * Thread.sleep(4000);
   * WinDef.HWND pinUPMenuPlayer = User32.INSTANCE.FindWindow(null, "PinUP Menu Player");
   * LOG.info("Found PopUP Menu player, sending window command");
   * DesktopWindow desktopWindow = player.get();
   * WinDef.HWND hwnd = desktopWindow.getHWND();
   * final int msg = 0x400 + 42;
   * User32.INSTANCE.PostMessage(hwnd, msg, new WinDef.WPARAM(6), new WinDef.LPARAM(game.getId()));
   *
   * @param game
   * @return
   */
  @Override
  public boolean launchGame(Game game) {
    restartFrontend(true);
    return launchGame(game, false);
  }

  private boolean launchGame(Game game, boolean wait) {
    pupLauncher.launch(game.getId());
    if (!wait) {
      return true;
    }

    try {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<Boolean> submit = executor.submit(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          while (!WindowsUtil.isProcessRunning("Future Pinball", "Visual Pinball Player")) {
            Thread.sleep(1000);
          }
          return true;
        }
      });

      return submit.get(EmulatorRecorderJob.EMULATOR_WAITING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
    catch (Exception e) {
      LOG.error("Waiting for frontend launch failed: {}", e.getMessage(), e);
    }
    return false;
  }

  private Date getDate(ResultSet set, String field) {
    try {
      return set.getDate(field);
    }
    catch (Exception e) {
      LOG.warn("Failed to parse date from database: {}", e.getMessage());
    }
    return new Date(new java.util.Date().getTime());
  }

  private java.util.Date getDateAsTimestamp(ResultSet set, String field) {
    try {
      return set.getTimestamp(field);
    }
    catch (Exception e) {
      LOG.warn("Failed to parse timestapm from database: {}", e.getMessage());
      try {
        return set.getDate(field);
      }
      catch (Exception ex) {
        LOG.warn("Failed to parse date from database: {}", e.getMessage());
      }
    }
    return new Date(new java.util.Date().getTime());
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      this.pupLauncher = new PupLauncher();
      this.pupEventEmitter = new PupEventEmitter(this.getInstallationFolder());
    }
    catch (Exception e) {
      LOG.error("Failed to initialize PinUPConnector: {}", e.getMessage(), e);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
