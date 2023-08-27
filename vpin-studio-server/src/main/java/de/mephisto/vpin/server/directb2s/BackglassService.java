package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.DirectB2SData;
import de.mephisto.vpin.restclient.DirectB2STableSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class BackglassService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private SystemService systemService;

  private B2STableSettingsParser parser;

  public DirectB2SData getDirectB2SData(int id) {
    Game game = gameService.getGame(id);
    if (game != null && game.isDirectB2SAvailable()) {
      DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
      return extractor.extractData(game.getDirectB2SFile());
    }
    return new DirectB2SData();
  }

  public DirectB2STableSettings saveTableSettings(int id, DirectB2STableSettings settings) {
    return null;
  }

  public DirectB2STableSettings getTableSettings(int id) {
    Game game = gameService.getGame(id);

    String rom = game.getRom();
    if (!StringUtils.isEmpty(game.getRomAlias())) {
      rom = game.getRomAlias();
    }
    DirectB2STableSettings entry = parser.getEntry(rom);

    if (entry == null) {
      entry = new DirectB2STableSettings();
      entry.setRom(rom);
    }
    return entry;
  }

  @Override
  public void afterPropertiesSet() {
    File settingsXml = systemService.getB2STableSettingsXml();
    if (settingsXml.exists()) {
      this.parser = new B2STableSettingsParser(systemService.getB2STableSettingsXml());
    }
    else {
      LOG.error(settingsXml.getAbsolutePath() + " not found.");
    }
  }
}
