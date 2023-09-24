package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
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

  private B2STableSettingsParser tableSettingsParser;
  private B2STableSettingsSerializer tableSettingsSerializer;
  private B2SServerSettingsParser serverSettingsParser;
  private B2SServerSettingsSerializer serverSettingsSerializer;

  public DirectB2SData getDirectB2SData(int id) {
    Game game = gameService.getGame(id);
    if (game != null && game.isDirectB2SAvailable()) {
      DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
      return extractor.extractData(game.getDirectB2SFile());
    }
    return new DirectB2SData();
  }

  public DirectB2STableSettings saveTableSettings(DirectB2STableSettings settings) {
    if (tableSettingsSerializer == null) {
      throw new UnsupportedOperationException("No B2STableSettings.xml found");
    }

    tableSettingsSerializer.serialize(settings);
    return settings;
  }

  public DirectB2STableSettings getTableSettings(int id) {
    if (tableSettingsParser == null) {
      throw new UnsupportedOperationException("No B2STableSettings.xml found");
    }

    Game game = gameService.getGame(id);
    String rom = game.getRom();
    if (!StringUtils.isEmpty(game.getRomAlias())) {
      rom = game.getRomAlias();
    }

    if (StringUtils.isEmpty(rom)) {
      rom = game.getTableName();
    }

    DirectB2STableSettings entry = tableSettingsParser.getEntry(rom);

    if (entry == null) {
      entry = new DirectB2STableSettings();
      entry.setRom(rom);
    }
    return entry;
  }

  public DirectB2ServerSettings getServerSettings() {
    return serverSettingsParser.getSettings();
  }

  public DirectB2ServerSettings saveServerSettings(DirectB2ServerSettings settings) {
    serverSettingsSerializer.serialize(settings);
    return getServerSettings();
  }

  public boolean deleteImages(Game game) {

    return false;
  }

  @Override
  public void afterPropertiesSet() {
    File settingsXml = systemService.getB2STableSettingsXml();
    if (settingsXml.exists()) {
      this.tableSettingsParser = new B2STableSettingsParser(systemService.getB2STableSettingsXml());
      this.tableSettingsSerializer = new B2STableSettingsSerializer(systemService.getB2STableSettingsXml());

      this.serverSettingsParser = new B2SServerSettingsParser(systemService.getB2STableSettingsXml());
      this.serverSettingsSerializer = new B2SServerSettingsSerializer(systemService.getB2STableSettingsXml());
    }
    else {
      LOG.error(settingsXml.getAbsolutePath() + " not found.");
    }
  }
}
