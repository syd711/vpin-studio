package de.mephisto.vpin.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.sql.*;

/**
 * Reads a PinUP Popper SQLite database and exports all games for a given
 * emulator to a JSON file matching the pupgames format (e.g. pinball_fx3.json).
 *
 * Usage:
 *   PUPDatabaseExporter.export("C:\\Users\\matth\\Downloads\\PUPDatabase.db", 3, new File("output.json"));
 */
public class PUPDatabaseExporter {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT);

  /**
   * Exports all games for the given emulator ID from the PUP database to a JSON file.
   *
   * @param dbPath   path to the PUPDatabase.db SQLite file
   * @param emuId    the emulator ID to export games for
   * @param output   the output JSON file
   */
  public static void export(String dbPath, int emuId, File output) throws Exception {
    String url = "jdbc:sqlite:" + dbPath;

    try (Connection conn = DriverManager.getConnection(url)) {
      EmuInfo emu = readEmuInfo(conn, emuId);
      ArrayNode games = readGames(conn, emuId, emu);

      ObjectNode root = MAPPER.createObjectNode();
      root.set("GameExport", games);

      MAPPER.writeValue(output, root);
      System.out.println("Exported " + games.size() + " games to " + output.getAbsolutePath());
    }
  }

  private static EmuInfo readEmuInfo(Connection conn, int emuId) throws SQLException {
    EmuInfo info = new EmuInfo();
    String sql = "SELECT EmuDisplay, DirGames, DirGamesShare, DirMedia, DirMediaShare " +
                 "FROM Emulators WHERE EMUID = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, emuId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          info.emuDisplay    = nullToEmpty(rs.getString("EmuDisplay"));
          info.dirGames      = nullToEmpty(rs.getString("DirGames"));
          info.dirGamesShare = nullToEmpty(rs.getString("DirGamesShare"));
          info.dirMedia      = nullToEmpty(rs.getString("DirMedia"));
          info.dirMediaShare = nullToEmpty(rs.getString("DirMediaShare"));
        }
      }
    }
    return info;
  }

  private static ArrayNode readGames(Connection conn, int emuId, EmuInfo emu) throws SQLException {
    ArrayNode games = MAPPER.createArrayNode();

    String sql = "SELECT GameID, EMUID, GameName, GameFileName, GameDisplay, " +
                 "UseEmuDefaults, Visible, Notes, DateAdded, GameYear, ROM, Manufact, " +
                 "NumPlayers, ResolutionX, ResolutionY, OutputScreen, ThemeColor, GameType, " +
                 "TAGS, Category, Author, LaunchCustomVar, GKeepDisplays, GameTheme, " +
                 "GameRating, Special, sysVolume, DOFStuff, MediaSearch, AudioChannels, " +
                 "CUSTOM2, CUSTOM3, GAMEVER, ALTEXE, IPDBNum, DateUpdated, DateFileUpdated, " +
                 "AutoRecFlag, AltRunMode, WebLinkURL, DesignedBy " +
                 "FROM Games WHERE EMUID = ? ORDER BY GameDisplay";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, emuId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          ObjectNode game = MAPPER.createObjectNode();

          game.put("GameID",           nullToEmpty(rs.getString("GameID")));
          game.put("EMUID",            nullToEmpty(rs.getString("EMUID")));
          game.put("GameName",         nullToEmpty(rs.getString("GameName")));
          game.put("GameFileName",     nullToEmpty(rs.getString("GameFileName")));
          game.put("GameDisplay",      nullToEmpty(rs.getString("GameDisplay")));
          game.put("UseEmuDefaults",   nullToEmpty(rs.getString("UseEmuDefaults")));
          game.put("Visible",          nullToEmpty(rs.getString("Visible")));
          game.put("Notes",            nullToEmpty(rs.getString("Notes")));
          game.put("DateAdded",        nullToEmpty(rs.getString("DateAdded")));
          game.put("GameYear",         nullToEmpty(rs.getString("GameYear")));
          game.put("ROM",              nullToEmpty(rs.getString("ROM")));
          game.put("Manufact",         nullToEmpty(rs.getString("Manufact")));
          game.put("NumPlayers",       nullToEmpty(rs.getString("NumPlayers")));
          game.put("ResolutionX",      nullToEmpty(rs.getString("ResolutionX")));
          game.put("ResolutionY",      nullToEmpty(rs.getString("ResolutionY")));
          game.put("OutputScreen",     nullToEmpty(rs.getString("OutputScreen")));
          game.put("ThemeColor",       nullToEmpty(rs.getString("ThemeColor")));
          game.put("GameType",         nullToEmpty(rs.getString("GameType")));
          game.put("TAGS",             nullToEmpty(rs.getString("TAGS")));
          game.put("Category",         nullToEmpty(rs.getString("Category")));
          game.put("Author",           nullToEmpty(rs.getString("Author")));
          game.put("LaunchCustomVar",  nullToEmpty(rs.getString("LaunchCustomVar")));
          game.put("GKeepDisplays",    nullToEmpty(rs.getString("GKeepDisplays")));
          game.put("GameTheme",        nullToEmpty(rs.getString("GameTheme")));
          game.put("GameRating",       nullToEmpty(rs.getString("GameRating")));
          game.put("Special",          nullToEmpty(rs.getString("Special")));
          game.put("sysVolume",        nullToEmpty(rs.getString("sysVolume")));
          game.put("DOFStuff",         nullToEmpty(rs.getString("DOFStuff")));
          game.put("MediaSearch",      nullToEmpty(rs.getString("MediaSearch")));
          game.put("AudioChannels",    nullToEmpty(rs.getString("AudioChannels")));
          game.put("CUSTOM2",          nullToEmpty(rs.getString("CUSTOM2")));
          game.put("CUSTOM3",          nullToEmpty(rs.getString("CUSTOM3")));
          game.put("GAMEVER",          nullToEmpty(rs.getString("GAMEVER")));
          game.put("ALTEXE",           nullToEmpty(rs.getString("ALTEXE")));
          game.put("IPDBNum",          nullToEmpty(rs.getString("IPDBNum")));
          game.put("DateUpdated",      nullToEmpty(rs.getString("DateUpdated")));
          game.put("DateFileUpdated",  nullToEmpty(rs.getString("DateFileUpdated")));
          game.put("AutoRecFlag",      nullToEmpty(rs.getString("AutoRecFlag")));
          game.put("AltRunMode",       nullToEmpty(rs.getString("AltRunMode")));
          game.put("WebLinkURL",       nullToEmpty(rs.getString("WebLinkURL")));
          game.put("DesignedBy",       nullToEmpty(rs.getString("DesignedBy")));

          // Emulator-level fields (denormalized into each game entry, matching pupgames format)
          game.put("EMUID_1",          String.valueOf(emuId));
          game.put("EmuDisplay",       emu.emuDisplay);
          game.put("DirGames",         emu.dirGames);
          game.put("DirGamesShare",    emu.dirGamesShare);
          game.put("DirMedia",         emu.dirMedia);
          game.put("DirMediaShare",    emu.dirMediaShare);

          games.add(game);
        }
      }
    }
    return games;
  }

  private static String nullToEmpty(String value) {
    return value != null ? value : "";
  }

  private static class EmuInfo {
    String emuDisplay    = "";
    String dirGames      = "";
    String dirGamesShare = "";
    String dirMedia      = "";
    String dirMediaShare = "";
  }

  public static void main(String[] args) throws Exception {
//    if (args.length < 3) {
//      System.err.println("Usage: PUPDatabaseExporter <db-path> <emulator-id> <output-json>");
//      System.err.println("Example: PUPDatabaseExporter \"C:\\Users\\matth\\Downloads\\PUPDatabase.db\" 3 pinball_fx3.json");
//      System.exit(1);
//    }

    String dbPath = "C:\\Users\\matth\\Downloads\\PUPDatabase.db";
    int emuId = 3;
    File output = new File("./deleteme.json");

    export(dbPath, emuId, output);
  }
}
