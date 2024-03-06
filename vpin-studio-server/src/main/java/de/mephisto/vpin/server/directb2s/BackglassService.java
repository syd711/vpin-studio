package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
      return extractor.extractData(game.getDirectB2SFile(), game.getEmulatorId(), game.getId());
    }
    return new DirectB2SData();
  }

  public boolean deleteBackglass(File b2sFile) {
    return b2sFile.exists() && b2sFile.delete();
  }

  public DirectB2SData getDirectB2SData(@NonNull DirectB2S directB2S) {
    String vpxName = directB2S.getName() + ".vpx";
    List<Game> gamesByFilename = pinUPConnector.getGamesByFilename(vpxName);
    for (Game game : gamesByFilename) {
      if (game.getEmulator().getId() == directB2S.getEmulatorId()) {
        return getDirectB2SData(game.getId());
      }
    }

    DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
    return extractor.extractData(new File(directB2S.getFileName()), directB2S.getEmulatorId(), -1);
  }

  public DirectB2STableSettings saveTableSettings(int gameId, DirectB2STableSettings settings) throws VPinStudioException {
    Game game = gameService.getGame(gameId);
    try {
      File settingsXml = game.getEmulator().getB2STableSettingsXml();
      B2STableSettingsSerializer tableSettingsSerializer = new B2STableSettingsSerializer(settingsXml);
      tableSettingsSerializer.serialize(settings);
      return settings;
    } catch (VPinStudioException e) {
      LOG.error("Failed to save table settings for \"" + game.getGameDisplayName() + "\": " + e.getMessage(), e);
      throw e;
    }
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
    if (!settingsXml.exists()) {
      emulator = pinUPConnector.getDefaultGameEmulator();
      settingsXml = emulator.getB2STableSettingsXml();
    }

    B2SServerSettingsParser serverSettingsParser = new B2SServerSettingsParser(settingsXml);
    return serverSettingsParser.getSettings();
  }

  public DirectB2ServerSettings saveServerSettings(int gameId, DirectB2ServerSettings settings) {
    Game game = gameService.getGame(gameId);
    GameEmulator emulator = game.getEmulator();
    File settingsXml = emulator.getB2STableSettingsXml();
    if (!settingsXml.exists()) {
      emulator = pinUPConnector.getDefaultGameEmulator();
      settingsXml = emulator.getB2STableSettingsXml();
    }

    B2SServerSettingsSerializer serverSettingsSerializer = new B2SServerSettingsSerializer(settingsXml);
    serverSettingsSerializer.serialize(settings);
    return getServerSettings(gameId);
  }

  public List<DirectB2S> getBackglasses() {
    List<DirectB2S> result = new ArrayList<>();
    List<GameEmulator> gameEmulators = pinUPConnector.getGameEmulators();
    for (GameEmulator gameEmulator : gameEmulators) {
      File tablesFolder = gameEmulator.getTablesFolder();
      Collection<File> files = org.apache.commons.io.FileUtils.listFiles(tablesFolder, new String[]{"directb2s"}, true);
      for (File file : files) {
        DirectB2S directB2SData = new DirectB2S();
        directB2SData.setEmulatorId(gameEmulator.getId());
        directB2SData.setName(FilenameUtils.getBaseName(file.getName()));
        directB2SData.setFileName(file.getAbsolutePath());

        String vpxFile = FilenameUtils.getBaseName(file.getName()) + ".vpx";
        directB2SData.setVpxAvailable(new File(file.getParentFile(), vpxFile).exists());
        result.add(directB2SData);
      }
    }
    return result;
  }

  public boolean rename(File b2sFile, String newName) {
    if (b2sFile.exists() && b2sFile.renameTo(new File(b2sFile.getParentFile(), newName))) {
      LOG.info("Renamed \"" + b2sFile.getName() + "\" to \"" + newName + "\"");
      return true;
    }
    return false;
  }

  public boolean duplicate(File b2sFile) throws IOException {
    try {
      File target = new File(b2sFile.getParentFile(), b2sFile.getName());
      target = FileUtils.uniqueFile(target);
      org.apache.commons.io.FileUtils.copyFile(b2sFile, target);
      LOG.info("Copied \"" + b2sFile.getName() + "\" to \"" + target.getAbsolutePath() + "\"");
      return true;
    } catch (IOException e) {
      LOG.error("Failed to duplicate backglass " + b2sFile.getAbsolutePath() + ": " + e.getMessage(), e);
      throw e;
    }
  }
}
