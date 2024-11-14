package de.mephisto.vpin.server.frontend.pinbally;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.frontend.PlaylistGame;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.playlists.Playlist;

public class PinballYStatisticsParser {
  private final static Logger LOG = LoggerFactory.getLogger(PinballYStatisticsParser.class);

  private final FastDateFormat statisticsSdf = FastDateFormat.getInstance("yyyyMMddHHmmss");

  private FrontendConnector connector;

  private String[] headers = StringUtils.split("Game,Last Played,Play Count,Play Time,Is Favorite,Rating,Audio Volume,Categories,Is Hidden,Date Added,High Score Style,Marked For Capture,Show When Running", ",");

  public PinballYStatisticsParser(FrontendConnector connector) {
    this.connector = connector;
  }

  @NonNull
  private File getGameStatsFile() {
    File pinballXFolder = connector.getInstallationFolder();
    return new File(pinballXFolder, "/GameStats.csv");
  }

  public void getAlxData(Collection<Emulator> emus, List<TableAlxEntry> stats, Set<Integer> favs, List<Playlist> playlists) {
    getGameData(emus, (record, g) -> getAlxData(record, stats, favs, playlists, g));
  }

  private void getAlxData(CSVRecord record, List<TableAlxEntry> stats, Set<Integer> favs, List<Playlist> playlists, Game game) {
    if (game == null) {
      return;
    }

    // collect favorites
    String fav = safeGet(record, "Is Favorite");
    boolean isFav = fav != null && BooleanUtils.toBoolean(fav);
    if (favs != null && isFav) {
      favs.add(game.getId());
    }

    // collect table stats
    String lastPlayed = safeGet(record, "Last Played");
    if (stats != null && lastPlayed != null) {
      TableAlxEntry e = new TableAlxEntry();
      
      e.setDisplayName(game.getGameDisplayName());
      e.setGameId(game.getId());
      e.setUniqueId(game.getId());

      try {
        e.setLastPlayed(statisticsSdf.parse(lastPlayed));
      }
      catch (ParseException pe) {
        LOG.error("Cannot parse date " + lastPlayed + "," + pe.getMessage());
      }

      String playTime = safeGet(record, "Play Time");
      if (StringUtils.isNotEmpty(playTime)) {
        e.setTimePlayedSecs(Integer.parseInt(playTime));
      }
      String playCount = safeGet(record, "Play Count");
      if (StringUtils.isNotEmpty(playCount)) {
        e.setNumberOfPlays(Integer.parseInt(playCount));
      }
      stats.add(e);
    }

    // collect playlists
    String categories = safeGet(record, "Categories");
    if (playlists != null && categories != null) {
      String[] cats = StringUtils.split(categories, ",");
      for (String cat: cats) {
        Playlist p = getPlaylist(playlists, cat);
        if (p == null) {
          p = new Playlist();
          p.setId(playlists.size());
          p.setEmulatorId(game.getEmulatorId());
          p.setName(cat);
          // don't set mediaName, studio will use the name
          playlists.add(p);
        }

        PlaylistGame pg = new PlaylistGame();
        pg.setId(game.getId());
        pg.setPlayed(lastPlayed != null);
        pg.setFav(isFav);
        pg.setGlobalFav(false);
    
        p.addGame(pg);
      }
    }
  }

  private String safeGet(CSVRecord record, String name) {
    return record.isSet(name) ? record.get(name) : null;
  }

  private Playlist getPlaylist(List<Playlist> playlists, String cat) {
    return playlists.stream().filter(pl -> StringUtils.equalsIgnoreCase(pl.getName(), cat)).findFirst().orElse(null);
  }

  //------------------------------------

  private void getGameData(Collection<Emulator> emus, RecordVisitor visitor) {
    if (getGameStatsFile().exists()) {
      if (!getGameData(emus, visitor, "UTF-8")) {
        getGameData(emus, visitor, "UTF-16");
      }
    }
  }
  private boolean getGameData(Collection<Emulator> emus, RecordVisitor visitor, String charset) {
    try (FileReader fileReader = new FileReader(getGameStatsFile(), Charset.forName(charset))) {

      CSVFormat format = CSVFormat.RFC4180.builder()
        .setHeader(new String[0])
        .setIgnoreEmptyLines(true)
        .setQuoteMode(QuoteMode.NON_NUMERIC)
        .setQuote('"')
        .setTrim(true)
        .build();

      CSVParser parser = format.parse(fileReader);

      // check parsing and presence of columns
      if (!parser.getHeaderMap().containsKey("Game")) {
        return false;
      }

      Iterator<CSVRecord> iterator = parser.iterator();
      while (iterator.hasNext()) {
        CSVRecord record = iterator.next();
        String game = safeGet(record, "Game");
        for (Emulator emu : emus) {
          if (StringUtils.endsWith(game, "." + emu.getName())) {
            String gameName = StringUtils.substringBefore(game, "." + emu.getName());
            Game g = connector.getGameByName(emu.getId(), gameName);
            try {
              visitor.accept(record, g);
            }
            catch (Exception ge) {
              LOG.error("Ignored error while processing record for game " + game + ", " + ge.getMessage());
            } 
            break;
          }
        }
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("cannot read GameStats.csv", e);
      return false;
    }
  }

  //------------------------------------

  public void writeFavorite(Game game, boolean favorite) {
    writeGameData(game, (values, g) -> {
      if (favorite) {
        values.put("Is Favorite", "Yes");
      } else {
        values.put("Is Favorite", "");
      }
    });
  };

  public void writeNumberOfPlayed(Game game, long nbPlayed) {
    writeGameData(game, (values, g) -> {
      values.put("Play Count", Long.toString(nbPlayed));
    });
  };

  public void writeSecondsPlayed(Game game, long secsPlayed) {
    writeGameData(game, (values, g) -> {
      values.put("Play Time", Long.toString(secsPlayed));
    });
  };

  public void writePlaylistGame(Game game, Playlist pl) {
    writeGameData(game, (record, g) -> {
      String categories = record.get("Categories");
      List<String> cats= categories == null ? new ArrayList<>() :
        Stream.of(categories.split(",")).map(String::trim).collect(Collectors.toList());
      
      // if the game is in the Playlist, make sure the playlist is in the categories of the game
      if (pl.containsGame(g.getId())) {
        if (!cats.contains(pl.getName())) {
          cats.add(pl.getName());
        }
      }
      else {
        if (cats.contains(pl.getName())) {
          cats.remove(pl.getName());
        }
      }

      record.put("Categories", String.join(",", cats));
    });
  }

  public void writeStat(Game game, TableAlxEntry stat) {
    writeGameData(game, (values, g) -> {
      values.put("Play Time", Long.toString(stat.getTimePlayedSecs()));
      values.put("Play Count", Long.toString(stat.getNumberOfPlays()));
      values.put("Last Played", statisticsSdf.format(stat.getLastPlayed()));
    });
  }

  private void writeGameData(Game g, RecordBuilder builder) {

    File gamestats = getGameStatsFile();
    File gamestatsW = new File(gamestats.getParentFile(), gamestats.getName() + ".temp");
  
    CSVFormat format = CSVFormat.RFC4180.builder()
      .setHeader(headers)
      .setIgnoreEmptyLines(true)
      .setQuoteMode(QuoteMode.MINIMAL)
      .setQuote('"')
      .setTrim(true)
      .build();

    Emulator emu = connector.getEmulator(g.getEmulatorId());

    try (CSVPrinter printer = new CSVPrinter(new FileWriter(gamestatsW, Charset.forName("UTF-16")), format)) {
      boolean[] found = { false };
      getGameData(Arrays.asList(emu), (record, game) -> {
        Map<String, String> values = record.toMap();
        if (g.getId() == game.getId()) {
          builder.collectValues(values, game);
          found[0] = true;
        }
        printRecord(printer, values);
      });
      if (!found[0]) {
        Map<String, String> values = new HashMap<>();
        values.put("Game", g.getGameName() + "." + emu.getName());
        builder.collectValues(values, g);
        printRecord(printer, values);
      }
    }
    catch (Exception ioe) {
      LOG.error("Cannot write data", ioe);
    }

    // now this is done, switch files
    try {
      if (gamestats.exists()) {
        Files.delete(gamestats.toPath());
      }
      gamestatsW.renameTo(gamestats);
    }
    catch (IOException ioe) {
      LOG.error("Cannot delete file " + gamestats + ", statistics not saved !", ioe);
    }
  }

  private void printRecord(CSVPrinter printer, Map<String, String> values) throws IOException {
    for (String header : headers) {
      printer.print(values.get(header));
    }
    printer.println();
  }
  
  //------------------------------------


  @FunctionalInterface
  private static interface RecordVisitor {
    void accept(CSVRecord record, Game game) throws Exception;
  }

  @FunctionalInterface
  private static interface RecordBuilder {
    void collectValues(Map<String, String> values, Game game) throws Exception;
  }
}
