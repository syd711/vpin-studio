package de.mephisto.vpin.server.frontend.pinballx;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.server.games.Game;

public class PinballXStatisticsParser {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXStatisticsParser.class);

  private final FastDateFormat statisticsSdf = FastDateFormat.getInstance("dd/MM/yyyy HH:mm:ss");

  private PinballXConnector connector;

  public PinballXStatisticsParser(PinballXConnector connector) {
    this.connector = connector;
  }

  @NotNull
  private File getPinballXStatisticsIni() {
    File pinballXFolder = connector.getInstallationFolder();
    return new File(pinballXFolder, "/Databases/Statistics.ini");
  }

  public List<TableAlxEntry> getAlxData() {
    List<TableAlxEntry> result = new ArrayList<>();
    INIConfiguration iniConfiguration = new INIConfiguration();
    try (FileReader fileReader = new FileReader(getPinballXStatisticsIni(), Charset.forName("UTF-8"))) {
      iniConfiguration.read(fileReader);
      for (Game game : connector.getGames()) {
        getAlxData(iniConfiguration, result, game);
      }
    }
    catch (Exception e) {
      LOG.error("cannot parse Statistics.ini", e);
    }
    return result;
  }

  public List<TableAlxEntry> getAlxData(int gameId) {
    List<TableAlxEntry> result = new ArrayList<>();
    INIConfiguration iniConfiguration = new INIConfiguration();
    try (FileReader fileReader = new FileReader(getPinballXStatisticsIni(), Charset.forName("UTF-8"))) {
      iniConfiguration.read(fileReader);
      getAlxData(iniConfiguration, result, connector.getGame(gameId));
    }
    catch (Exception e) {
      LOG.error("cannot parse Statistics.ini for game " + gameId, e);
    }
    return result;
  }

  private void getAlxData(INIConfiguration iniConfiguration, List<TableAlxEntry> result, Game game) throws ParseException {
    if (game == null) {
      return;
    }
    Emulator emu = connector.getEmulator(game.getEmulatorId());
    String emuName = StringUtils.remove(emu.getName(), ' ');
    String sectionName =  emuName + "_" + game.getGameDisplayName().trim();
    sectionName = sectionName.replaceAll("[^A-Za-z0-9]", "_");
    int p = -1;
    while ((p = sectionName.indexOf("__")) >= 0) {
      sectionName = sectionName.substring(0, p) + sectionName.substring(p + 1);
    }
    sectionName = StringUtils.removeEnd(sectionName, "_");

    SubnodeConfiguration s = iniConfiguration.getSection(sectionName);
    if (s.containsKey("lastplayed")) {
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
      e.setGameId(game.getId());
      e.setUniqueId(game.getId());
      
      e.setLastPlayed(statisticsSdf.parse(s.getString("lastplayed")));
      if (s.containsKey("secondsplayed")) {
        e.setTimePlayedSecs(Integer.parseInt(s.getString("secondsplayed")));
      }
      if (s.containsKey("timesplayed")) {
        e.setNumberOfPlays(Integer.parseInt(s.getString("timesplayed")));
      }
      result.add(e);
    }
  }
}
