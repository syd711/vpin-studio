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
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.frontend.FrontendService;
import javafx.scene.image.Image;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;

@Service
public class BackglassService {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private DefaultPictureService defaultPictureService;

  /**
   * Cache between filename and data
   */
  private final Map<String, DirectB2SData> cacheDirectB2SData = new ConcurrentHashMap<>();

  private final Map<String, B2STableSettingsParser> cacheB2STableSettingsParser = new ConcurrentHashMap<>();

  public boolean clearCache() {
    cacheB2STableSettingsParser.clear();
    cacheDirectB2SData.clear();
    return true;
  }

  public DirectB2SData getDirectB2SData(int gameId) {
    Game game = gameService.getGame(gameId);
    return getDirectB2SData(game);
  }

  public DirectB2SData getDirectB2SData(@Nonnull DirectB2S directB2S) {
    String vpxName = directB2S.getName() + ".vpx";
    Game game = frontendService.getGameByFilename(directB2S.getEmulatorId(), vpxName);
    if (game != null) {
      return getDirectB2SData(game);
    }
    File b2sFile = getB2sFile(directB2S.getEmulatorId(), directB2S.getFileName());
    return getDirectB2SData(b2sFile, directB2S.getEmulatorId(), null, directB2S.getFileName());
  }

  public DirectB2SData getDirectB2SData(Game game) {
    if (game != null && game.getDirectB2SPath() != null) {
      File directB2SFile = game.getDirectB2SFile();
      Path relativeFilePath = game.getEmulator().getTablesFolder().toPath().relativize(directB2SFile.toPath());
      return getDirectB2SData(directB2SFile, game.getEmulatorId(), game, relativeFilePath.toString());
    }
    else {
      return new DirectB2SData();
    }
  }

  private DirectB2SData getDirectB2SData(File directB2SFile, int emulatorId, @Nullable Game game, String filename) {
    if (cacheDirectB2SData.containsKey(directB2SFile.getPath())) {
      return cacheDirectB2SData.get(directB2SFile.getPath());
    }
    DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
    DirectB2SData data = extractor.extractData(directB2SFile, emulatorId, filename, game != null ? game.getId() : -1);

    boolean forceBackglassExtraction = false;

    // now fill images dimension
    try {
      extractBackgroundData(data, game, forceBackglassExtraction);
    } catch (IOException ioe) {
      LOG.error("cannot extract background dimension", ioe);
    }
    try {
      exportDMDData(data, game, forceBackglassExtraction);
    } catch (IOException ioe) {
      LOG.error("cannot extract background dimension", ioe);
    }

    cacheDirectB2SData.put(directB2SFile.getPath(), data);
    return data;
  }

  private void extractBackgroundData(DirectB2SData data, @Nullable Game game, boolean forceBackglassExtraction) throws IOException {
    if (!forceBackglassExtraction && game != null) {
      File rawDefaultPicture = defaultPictureService.getRawDefaultPicture(game);
      if (!rawDefaultPicture.exists()) {
        defaultPictureService.extractDefaultPicture(game);
        rawDefaultPicture = defaultPictureService.getRawDefaultPicture(game);
      }

      if (rawDefaultPicture.exists()) {
        BufferedImage image = ImageUtil.loadImage(rawDefaultPicture);
        int backgroundWidth = (int) image.getWidth();
        int backgroundHeight = (int) image.getHeight();
        data.setBackgroundWidth(backgroundWidth);
        data.setBackgroundHeight(backgroundHeight);
      }
      else {
        data.setBackgroundWidth(0);
        data.setBackgroundHeight(0);
      }
    }
    else {
      String filename = FilenameUtils.getBaseName(data.getFilename());
      String backgroundBase64 = getBackgroundBase64(data.getEmulatorId(), filename);
      if (backgroundBase64 != null) {
        byte[] imageData = DatatypeConverter.parseBase64Binary(backgroundBase64);
        Image image = new Image(new ByteArrayInputStream(imageData));
        int backgroundWidth = (int) image.getWidth();
        int backgroundHeight = (int) image.getHeight();
        data.setBackgroundWidth(backgroundWidth);
        data.setBackgroundHeight(backgroundHeight);
      }
      else {
        data.setBackgroundWidth(0);
        data.setBackgroundHeight(0);
      }
    }
  }

  private void exportDMDData(DirectB2SData data, @Nullable Game game, boolean forceBackglassExtraction) throws IOException {
    if (!forceBackglassExtraction && game != null) {
      File picture = defaultPictureService.getDMDPicture(game);
      if (picture.exists()) {
        BufferedImage image = ImageUtil.loadImage(picture);
        int dmdWidth = (int) image.getWidth();
        int dmdHeight = (int) image.getHeight();
        data.setDmdWidth(dmdWidth);
        data.setDmdHeight(dmdHeight);
      }
      else {
        data.setDmdWidth(0);
        data.setDmdHeight(0);
      }
    }
    else if (data.isDmdImageAvailable()) {
      String filename = FilenameUtils.getBaseName(data.getFilename());

      String dmdBase64 = getDmdBase64(data.getEmulatorId(), filename);
      if (dmdBase64 != null) {
        byte[] dmdData = DatatypeConverter.parseBase64Binary(dmdBase64);
        Image dmdImage = new Image(new ByteArrayInputStream(dmdData));
        int dmdWidth = (int) dmdImage.getWidth();
        int dmdHeight = (int) dmdImage.getHeight();
        data.setDmdWidth(dmdWidth);
        data.setDmdHeight(dmdHeight);
      }
      else {
        data.setDmdWidth(0);
        data.setDmdHeight(0);
      }
    }
  }

  private File getB2sFile(int emuId, String filename) {
    GameEmulator emulator = frontendService.getGameEmulator(emuId);
    File b2sFile = new File(emulator.getTablesDirectory(), filename);
    return b2sFile;
  }

  public String getBackgroundBase64(int emuId, String filename) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile.exists()) {
      DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
      extractor.extractData(b2sFile, emuId, filename, -1);
      return extractor.getBackgroundBase64();
    }
    return null;
  }

  public String getDmdBase64(int emuId, String filename) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile.exists()) {
      DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
      extractor.extractData(b2sFile, emuId, filename, -1);
      return extractor.getDmdBase64();
    }
    return null;
  }

  public boolean setDmdImage(int emuId, String filename, String file, String dmdBase64) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile.exists()) {
      try {
        DirectB2SDataUpdater updater = new DirectB2SDataUpdater();
        updater.updateDmdImage(b2sFile, file, dmdBase64, false);
        // clean cache
        cacheDirectB2SData.remove(b2sFile.getPath());
        return true;
      }
      catch (Exception e) {
        LOG.error("Error while updating " + filename, e);
      }
    }
    return false;
  }

  public boolean deleteBackglass(int emuId, String filename) {
    File b2sFile = getB2sFile(emuId, filename);
    cacheDirectB2SData.remove(b2sFile.getPath());
    if (b2sFile.exists() && b2sFile.delete()) {
      LOG.info("Deleted " + b2sFile.getAbsolutePath());
      return true;
    }
    return false;
  }

  public DirectB2STableSettings saveTableSettings(int gameId, DirectB2STableSettings settings) throws VPinStudioException {
    Game game = gameService.getGame(gameId);
    try {
      File settingsXml = game.getEmulator().getB2STableSettingsXml();
      B2STableSettingsSerializer tableSettingsSerializer = new B2STableSettingsSerializer(settingsXml);
      tableSettingsSerializer.serialize(settings);
      // destroy cache, will be recreated on first access
      cacheB2STableSettingsParser.remove(settingsXml.getPath());
      return settings;
    }
    catch (VPinStudioException e) {
      LOG.error("Failed to save table settings for \"" + game.getGameDisplayName() + "\": " + e.getMessage(), e);
      throw e;
    }
  }

  @Nullable
  public DirectB2STableSettings getTableSettings(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game == null) {
      return null;
    }

    String rom = game.getRom();

    File settingsXml = game.getEmulator().getB2STableSettingsXml();
    B2STableSettingsParser tableSettingsParser = cacheB2STableSettingsParser.get(settingsXml.getPath());
    if (tableSettingsParser == null) {
      if (settingsXml.exists() && !StringUtils.isEmpty(rom)) {
        tableSettingsParser = new B2STableSettingsParser(settingsXml);
      }
      cacheB2STableSettingsParser.put(settingsXml.getPath(), tableSettingsParser);
    }

    if (tableSettingsParser != null) {
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
    GameEmulator emulator = frontendService.getGameEmulator(emuId);
    File settingsXml = emulator.getB2STableSettingsXml();
    if (!settingsXml.exists()) {
      emulator = frontendService.getDefaultGameEmulator();
      settingsXml = emulator.getB2STableSettingsXml();
    }

    B2SServerSettingsParser serverSettingsParser = new B2SServerSettingsParser(settingsXml);
    return serverSettingsParser.getSettings();
  }

  public DirectB2ServerSettings saveServerSettings(int emulatorId, DirectB2ServerSettings settings) {
    GameEmulator emulator = frontendService.getGameEmulator(emulatorId);
    File settingsXml = emulator.getB2STableSettingsXml();
    if (!settingsXml.exists()) {
      emulator = frontendService.getDefaultGameEmulator();
      settingsXml = emulator.getB2STableSettingsXml();
    }

    B2SServerSettingsSerializer serverSettingsSerializer = new B2SServerSettingsSerializer(settingsXml);
    serverSettingsSerializer.serialize(settings);
    return getServerSettings(emulatorId);
  }

  public List<DirectB2S> getBackglasses() {
    List<DirectB2S> result = new ArrayList<>();
    List<GameEmulator> gameEmulators = frontendService.getVpxGameEmulators();
    for (GameEmulator gameEmulator : gameEmulators) {
      File tablesFolder = gameEmulator.getTablesFolder();
      Path tablesPath = tablesFolder.toPath();

      IOFileFilter filter = new SuffixFileFilter(new String[]{"directb2s"}, IOCase.INSENSITIVE);
      Iterator<File> iter = org.apache.commons.io.FileUtils.iterateFiles(tablesFolder, filter, DirectoryFileFilter.DIRECTORY);
      while (iter.hasNext()) {
        File file = iter.next();
        DirectB2S directB2S = getDirectB2S(gameEmulator, tablesPath, file);
        result.add(directB2S);
      }
    }
    Collections.sort(result, Comparator.comparing(o -> o.getName().toLowerCase()));
    return result;
  }

  private DirectB2S getDirectB2S(GameEmulator gameEmulator, File file) {
    File tablesFolder = gameEmulator.getTablesFolder();
    Path tablesPath = tablesFolder.toPath();
    return getDirectB2S(gameEmulator, tablesPath, file);
  }
  private DirectB2S getDirectB2S(GameEmulator gameEmulator, Path tablesPath, File file) {
    DirectB2S directB2S = new DirectB2S();
    directB2S.setEmulatorId(gameEmulator.getId());
    directB2S.setFileName(tablesPath.relativize(file.toPath()).toString());

    String vpxFilename = FilenameUtils.getBaseName(file.getName()) + ".vpx";
    directB2S.setVpxAvailable(new File(file.getParentFile(), vpxFilename).exists());
    return directB2S;
  }

  public DirectB2S rename(int emuId, String filename, String newName) {
    GameEmulator emulator = frontendService.getGameEmulator(emuId);
    File b2sFile = new File(emulator.getTablesDirectory(), filename);
    File b2sNewFile = new File(b2sFile.getParentFile(), newName);
    if (b2sFile.exists() && b2sFile.renameTo(b2sNewFile)) {
      if (cacheDirectB2SData.containsKey(b2sFile.getPath())) {
        cacheDirectB2SData.remove(b2sFile.getPath());
      }
      LOG.info("Renamed \"" + filename + "\" to \"" + newName + "\"");
      
      return getDirectB2S(emulator, b2sNewFile);
    }
    return null;
  }

  public DirectB2S duplicate(int emuId, String filename) throws IOException {
    GameEmulator emulator = frontendService.getGameEmulator(emuId);
    File b2sFile = new File(emulator.getTablesDirectory(), filename);
    try {
      File target = new File(b2sFile.getParentFile(), b2sFile.getName());
      target = FileUtils.uniqueFile(target);
      org.apache.commons.io.FileUtils.copyFile(b2sFile, target);
      LOG.info("Copied \"" + filename + "\" to \"" + target.getAbsolutePath() + "\"");
      return getDirectB2S(emulator, target);
    }
    catch (IOException e) {
      LOG.error("Failed to duplicate backglass " + b2sFile.getAbsolutePath() + ": " + e.getMessage(), e);
      throw e;
    }
  }
}
