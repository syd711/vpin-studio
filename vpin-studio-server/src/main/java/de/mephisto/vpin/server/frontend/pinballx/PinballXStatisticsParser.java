package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.frontend.GameEntry;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class PinballXStatisticsParser {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXStatisticsParser.class);

  private final FastDateFormat statisticsSdf = FastDateFormat.getInstance("dd/MM/yyyy HH:mm:ss");
  private final FastDateFormat statisticsSdfAlt = FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss");

  private PinballXConnector connector;

  public PinballXStatisticsParser(PinballXConnector connector) {
    this.connector = connector;
  }

  @NonNull
  private File getPinballXStatisticsIni() {
    File pinballXFolder = connector.getInstallationFolder();
    return new File(pinballXFolder, "/Databases/Statistics.ini");
  }

  public void getAlxData(Collection<GameEmulator> emus, List<TableAlxEntry> stats, Set<Integer> favs) {
    INIConfiguration iniConfiguration = new INIConfiguration();
    try (FileReader fileReader = new FileReader(getPinballXStatisticsIni(), Charset.forName("UTF-8"))) {
      iniConfiguration.read(fileReader);
      for (GameEmulator emu : emus) {
        for (GameEntry entry : connector.getGameEntries(emu.getId())) {
          TableDetails details = connector.getGameFromDb(emu.getId(), entry.getFilename());
          getAlxData(iniConfiguration, stats, favs, emu, entry.getId(), details);
        }
      }
    }
    catch (Exception e) {
      LOG.error("cannot parse Statistics.ini", e);
    }
  }

  private void getAlxData(INIConfiguration iniConfiguration, List<TableAlxEntry> stats, Set<Integer> favs, GameEmulator emu, int gameId, TableDetails game) throws ParseException {
    if (game == null) {
      return;
    }
    SubnodeConfiguration s = getGameSection(iniConfiguration, emu, game.getGameName());

    // collect favorites
    if (favs != null && s.containsKey("favorite") && BooleanUtils.toBoolean(s.getString("favorite"))) {
      favs.add(gameId);
    }

    // collect table stats
    if (stats != null && s.containsKey("lastplayed")) {
      TableAlxEntry e = new TableAlxEntry();
      /*
        [VisualPinball_24_Stern_2009]
        secondsplayed=70
        lastplayed=22/04/2024 17:52:12
        dateadded=21/08/2022 19:03:22
        description=24 (Stern 2009)
        rom
        timesplayed=2
       */
      
      e.setDisplayName(game.getGameDisplayName());
      e.setGameId(gameId);
      e.setUniqueId(gameId);

      try {
        e.setLastPlayed(statisticsSdf.parse(s.getString("lastplayed")));
      }
      catch (ParseException ex) {
        e.setLastPlayed(statisticsSdfAlt.parse(s.getString("lastplayed")));
      }

      if (s.containsKey("secondsplayed")) {
        e.setTimePlayedSecs(Integer.parseInt(s.getString("secondsplayed")));
      }
      if (s.containsKey("timesplayed")) {
        e.setNumberOfPlays(Integer.parseInt(s.getString("timesplayed")));
      }
      stats.add(e);
    }
  }

  protected SubnodeConfiguration getGameSection(INIConfiguration iniConfiguration, GameEmulator emu, String gameName) {
    String sectionName =  emu.getSafeName() + "_" + gameName.trim();
    sectionName = sectionName.replaceAll("[^A-Za-z0-9]", "_");
    int p = -1;
    while ((p = sectionName.indexOf("__")) >= 0) {
      sectionName = sectionName.substring(0, p) + sectionName.substring(p + 1);
    }
    sectionName = StringUtils.removeEnd(sectionName, "_");

    SubnodeConfiguration s = iniConfiguration.getSection(sectionName);
    return s;
  }

  public void writeFavorite(Game game, boolean favorite) {
    GameEmulator emu = connector.getEmulator(game.getEmulatorId());
    writeAlxData(emu, game.getGameName(), conf -> {
      if (favorite) {
        conf.setProperty("favorite", "true");
      } else {
        conf.clearProperty("favorite");
      }
    });
  };
  public void writeNumberOfPlayed(Game game, long nbPlayed) {
    GameEmulator emu = connector.getEmulator(game.getEmulatorId());
    writeAlxData(emu, game.getGameName(), conf -> {
      conf.setProperty("timesplayed", Long.toString(nbPlayed));
    });
  };
  public void writeSecondsPlayed(Game game, long secsPlayed) {
    GameEmulator emu = connector.getEmulator(game.getEmulatorId());
    writeAlxData(emu, game.getGameName(), conf -> {
      conf.setProperty("secondsplayed", Long.toString(secsPlayed));
    });
  };

  private void writeAlxData(GameEmulator emu, String gameName, Consumer<SubnodeConfiguration> c) {
    File statsIni = getPinballXStatisticsIni();
    INIConfiguration iniConfiguration = new INIConfiguration();
    try (FileReader fileReader = new FileReader(statsIni, Charset.forName("UTF-8"))) {
      iniConfiguration.read(fileReader);
    }
    catch (Exception e) {
      LOG.error("cannot parse Statistics.ini", e);
    }

    SubnodeConfiguration conf = getGameSection(iniConfiguration, emu, gameName);
    c.accept(conf);

    try (FileWriter fileWriter = new FileWriter(statsIni, Charset.forName("UTF-8"))) {
      iniConfiguration.write(fileWriter);
    }
    catch (Exception e) {
      LOG.error("cannot write Statistics.ini", e);
    }
  }

}
