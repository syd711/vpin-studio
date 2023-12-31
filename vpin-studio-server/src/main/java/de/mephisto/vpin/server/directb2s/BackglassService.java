package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class BackglassService {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private PinUPConnector pinUPConnector;

  public DirectB2SData getDirectB2SData(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null && game.isDirectB2SAvailable()) {
      DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
      return extractor.extractData(game.getDirectB2SFile());
    }
    return new DirectB2SData();
  }

  public DirectB2STableSettings saveTableSettings(int gameId, DirectB2STableSettings settings) throws VPinStudioException {
    Game game = gameService.getGame(gameId);
    File settingsXml = game.getEmulator().getB2STableSettingsXml();
    B2STableSettingsSerializer tableSettingsSerializer = new B2STableSettingsSerializer(settingsXml);
    tableSettingsSerializer.serialize(settings);
    return settings;
  }

  public DirectB2STableSettings getTableSettings(int gameId) {
    Game game = gameService.getGame(gameId);
    String rom = game.getRom();

    File settingsXml = game.getEmulator().getB2STableSettingsXml();
    if (settingsXml.exists() && !StringUtils.isEmpty(rom)) {
      B2STableSettingsParser tableSettingsParser = new B2STableSettingsParser(settingsXml);
      DirectB2STableSettings entry = tableSettingsParser.getEntry(rom);
      if (entry == null && !StringUtils.isEmpty(game.getRomAlias())) {
        entry = tableSettingsParser.getEntry(game.getRomAlias());
      }

      if (entry == null && !StringUtils.isEmpty(game.getTableName())) {
        entry = tableSettingsParser.getEntry(game.getTableName());
      }

      if (entry == null) {
        entry = new DirectB2STableSettings();
        entry.setRom(rom);
      }
      return entry;
    }

    return null;
  }

  public DirectB2ServerSettings getServerSettings(int emuId) {
    GameEmulator emulator = pinUPConnector.getGameEmulator(emuId);
    File settingsXml = emulator.getB2STableSettingsXml();
    B2SServerSettingsParser serverSettingsParser = new B2SServerSettingsParser(settingsXml);
    return serverSettingsParser.getSettings();
  }

  public DirectB2ServerSettings saveServerSettings(int gameId, DirectB2ServerSettings settings) {
    Game game = gameService.getGame(gameId);
    GameEmulator emulator = game.getEmulator();
    File settingsXml = emulator.getB2STableSettingsXml();

    B2SServerSettingsSerializer serverSettingsSerializer = new B2SServerSettingsSerializer(settingsXml);
    serverSettingsSerializer.serialize(settings);
    return getServerSettings(gameId);
  }
}
