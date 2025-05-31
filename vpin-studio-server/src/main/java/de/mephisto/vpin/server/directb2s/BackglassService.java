package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.commons.utils.FileVersion;
import de.mephisto.vpin.restclient.directb2s.*;
import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.ValidationStateFactory;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.system.JCodec;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
public class BackglassService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private EmulatorService emulatorService;
  @Autowired
  private FrontendService frontendService;

  @Autowired
  private BackglassValidationService backglassValidationService;

  /**
   * The default filename ScreenRes.txt can be altered by setting the registry key
   * Software\B2S\B2SScreenResFileNameOverride to a different filename.
   * Read once
   */
  private String screenresTxt;

  /**
   * Cache between filename and data
   */
  private final Map<String, DirectB2S> cacheDirectB2SVersion = new ConcurrentHashMap<>();

  private final Map<String, DirectB2SData> cacheDirectB2SData = new ConcurrentHashMap<>();

  private final Map<String, B2STableSettingsParser> cacheB2STableSettingsParser = new ConcurrentHashMap<>();


  public boolean clearCache() {
    reloadDirectB2SAndVersions();
    cacheB2STableSettingsParser.clear();
    cacheDirectB2SData.clear();
    return true;
  }

  public DirectB2SData getDirectB2SData(int emuId, String filename) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile == null) {
      return null;
    }
    return getDirectB2SData(b2sFile, emuId, filename);
  }

  /**
   * Manage case where no B2SFile is active for a game, then take first one
   */
  public DirectB2SData getDirectB2SData(@NonNull Game game) {
    if (game != null) {
      String baseName = FilenameUtils.removeExtension(game.getGameFileName()).trim();
      DirectB2S b2s = cacheDirectB2SVersion.get(game.getEmulatorId() + "@" + baseName);
      if (b2s == null) {
        b2s = reloadDirectB2SAndVersions(game.getEmulator(), game.getGameFileName());
      }
      if (b2s != null) {
        if (b2s.isEnabled()) {
          File directB2SFile = game.getDirectB2SFile();
          String directB2SFileName = game.getDirectB2SFilename();
          return getDirectB2SData(directB2SFile, game.getEmulatorId(), directB2SFileName);
        }
        else {
          String directB2SFileName = b2s.getVersion(0);
          File directB2SFile = new File(game.getGameFile().getParentFile(), directB2SFileName);
          return getDirectB2SData(directB2SFile, game.getEmulatorId(), directB2SFileName);
        }
      }
    }
    //else
    return new DirectB2SData();
  }

  private DirectB2SData getDirectB2SData(@NonNull File directB2SFile, int emulatorId, String filename) {
    if (cacheDirectB2SData.containsKey(directB2SFile.getPath())) {
      return cacheDirectB2SData.get(directB2SFile.getPath());
    }
    DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
    DirectB2SData data = extractor.extractData(directB2SFile, emulatorId, filename);

    // now fill images dimension
    try {
      extractBackgroundData(data, extractor.getBackgroundBase64());
    }
    catch (IOException ioe) {
      LOG.error("cannot extract background dimension", ioe);
    }
    try {
      exportDMDData(data, extractor.getDmdBase64());
    }
    catch (IOException ioe) {
      LOG.error("cannot extract background dimension", ioe);
    }

    cacheDirectB2SData.put(directB2SFile.getPath(), data);
    return data;
  }

  private void extractBackgroundData(DirectB2SData data, String backgroundBase64) throws IOException {
    if (backgroundBase64 != null) {
      byte[] imageData = DatatypeConverter.parseBase64Binary(backgroundBase64);
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
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

  private void exportDMDData(DirectB2SData data, String dmdBase64) throws IOException {
    if (data.isDmdImageAvailable()) {
      if (dmdBase64 != null) {
        byte[] dmdData = DatatypeConverter.parseBase64Binary(dmdBase64);
        BufferedImage dmdImage = ImageIO.read(new ByteArrayInputStream(dmdData));
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

  @Nullable
  private File getB2sFile(int emuId, String filename) {
    GameEmulator emulator = emulatorService.getGameEmulator(emuId);
    if (emulator.getGamesDirectory() != null) {
      return new File(emulator.getGamesDirectory(), filename);
    }
    return null;
  }

  public String getBackgroundBase64(int emuId, String filename) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile != null && b2sFile.exists()) {
      DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
      extractor.extractData(b2sFile, emuId, filename);
      return extractor.getBackgroundBase64();
    }
    return null;
  }

  public String getDmdBase64(int emuId, String filename) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile.exists()) {
      DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
      extractor.extractData(b2sFile, emuId, filename);
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


  public void upddateScoresDisplayState(Game game, boolean state) {
    if (game != null && game.getDirectB2SPath() != null) {
      File directB2SFile = game.getDirectB2SFile();
      String directB2SFileName = game.getDirectB2SFilename();
      DirectB2SData data = getDirectB2SData(directB2SFile, game.getEmulatorId(), directB2SFileName);
      if (data !=  null && data.getNbScores() > 0) {
        try {
          DirectB2SDataUpdater updater = new DirectB2SDataUpdater();
          updater.upddateScoresDisplayState(directB2SFile, state, false);
        }
        catch (Exception e) {
          LOG.error("Error while updating " + directB2SFileName, e);
        }
      }
    }
  }

  //--------------------------------------

  public DirectB2SDetail getBackglassDetail(int emuId, String filename, Game game) {
    DirectB2SDetail detail = new DirectB2SDetail();
    detail.setEmulatorId(emuId);
    detail.setFilename(filename);

    GameEmulator emulator = emulatorService.getGameEmulator(emuId);
    File b2sFile = new File(emulator.getGamesDirectory(), filename);
    DirectB2SData data = getDirectB2SData(b2sFile, emuId, filename);

    if (data != null) {
      detail.setDmdImageAvailable(data.isDmdImageAvailable());
      detail.setFullDmd(DirectB2SData.isFullDmd(data.getDmdWidth(), data.getDmdHeight()));
      detail.setDmdWidth(data.getDmdWidth());
      detail.setDmdHeight(data.getDmdHeight());
      detail.setGrillHeight(data.getGrillHeight());
      detail.setNbScores(data.getNbScores());

      DirectB2sScreenRes screenres = getScreenRes(b2sFile, true);
      if (screenres != null) {
        detail.setResPath(screenres.getScreenresFilePath());
        detail.setFramePath(screenres.getBackgroundFilePath());
      }
    }
    DirectB2STableSettings tableSettings = null;
    if (game != null) {
      detail.setGameId(game.getId());
      tableSettings = getTableSettings(game);
      if (tableSettings != null) {
        detail.setHideGrill(tableSettings.getHideGrill());
        detail.setHideB2SDMD(tableSettings.isHideB2SDMD());
        detail.setHideBackglass(tableSettings.isHideB2SBackglass());
        detail.setHideDMD(tableSettings.getHideDMD());
      }
    }

    DirectB2ServerSettings serverSettings = getServerSettings();

    //run validations at the end!!!
    List<ValidationState> validate = backglassValidationService.validate(detail, game, tableSettings, serverSettings, true);
    if (validate.isEmpty()) {
      validate.add(ValidationStateFactory.empty());
    }
    detail.setValidations(validate);

    return detail;
  }

  //--------------------------------------

  private B2STableSettingsParser getTableSettingsParser() {
    File settingsXml = getB2STableSettingsXml();
    B2STableSettingsParser tableSettingsParser = cacheB2STableSettingsParser.get(settingsXml.getPath());
    if (tableSettingsParser == null) {
      if (settingsXml.exists()) {
        tableSettingsParser = new B2STableSettingsParser(getBackglassServerFolder(), settingsXml);
        cacheB2STableSettingsParser.put(settingsXml.getPath(), tableSettingsParser);
      }
    }
    return tableSettingsParser;
  }

  public DirectB2STableSettings saveTableSettings(int gameId, DirectB2STableSettings settings) throws VPinStudioException {
    try {
      File settingsXml = getB2STableSettingsXml();
      B2STableSettingsSerializer tableSettingsSerializer = new B2STableSettingsSerializer(settingsXml);
      tableSettingsSerializer.serialize(settings);
      // destroy cache, will be recreated on first access
      cacheB2STableSettingsParser.remove(settingsXml.getPath());
      return settings;
    }
    catch (VPinStudioException e) {
      LOG.error("Failed to save table settings for \"" + gameId + "\": " + e.getMessage(), e);
      throw e;
    }
  }

  @Nullable
  public DirectB2STableSettings getTableSettings(Game game) {
    String rom = game.getRom();

    B2STableSettingsParser tableSettingsParser = getTableSettingsParser();
    if (tableSettingsParser != null && !StringUtils.isEmpty(rom)) {
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

  //-----------------------------

  public DirectB2ServerSettings getServerSettings() {
    B2STableSettingsParser parser = getTableSettingsParser();
    return parser != null ? parser.getSettings() : null;
  }

  public DirectB2ServerSettings saveServerSettings(DirectB2ServerSettings settings) throws VPinStudioException {
    try {
      File settingsXml = getB2STableSettingsXml();
      B2STableSettingsSerializer serverSettingsSerializer = new B2STableSettingsSerializer(settingsXml);
      serverSettingsSerializer.serialize(settings);
      // destroy cache, will be recreated on first access
      cacheB2STableSettingsParser.remove(settingsXml.getPath());

      return getServerSettings();
    }
    catch (VPinStudioException e) {
      LOG.error("Failed to save server settings: " + e.getMessage(), e);
      throw e;
    }
  }

  @NonNull
  public File getBackglassServerFolder() {
    File b2sFolder = systemService.getBackglassServerFolder();
    if (b2sFolder != null) {
      return b2sFolder;
    }
    // else search in VPX emulators folders
    for (GameEmulator emu : emulatorService.getVpxGameEmulators()) {
      // check in installation
      try (Stream<Path> walkStream = Files.walk(emu.getInstallationFolder().toPath())) {
        Optional<Path> found = walkStream.filter(p -> StringUtils.equalsIgnoreCase(p.getFileName().toString(), "B2SBackglassServer.dll")).findFirst();
        if (found.isPresent()) {
          return found.get().toFile().getParentFile();
        }
      }
      catch (IOException ioe) {
        LOG.error("Cannot find backglass server in " + emu.getInstallationDirectory() + ", skip this emulator for B2S server search", ioe);
      }
    }
    // not installed, use default folder
    return new File("C:/B2SServer");
  }

  @NonNull
  public File getB2STableSettingsXml() {
    File xml = new File(getBackglassServerFolder(), "B2STableSettings.xml");
    if (xml.exists()) {
      return xml;
    }

    // not found, check the legacy default : in tables folder of Vpx emulators
    for (GameEmulator emu : emulatorService.getVpxGameEmulators()) {
      File tableXml = new File(emu.getGamesDirectory(), "B2STableSettings.xml");
      if (tableXml.exists()) {
        return tableXml;
      }
    }

    // else create a blank one
    try {
      File folder = xml.getParentFile();
      if (folder.exists() || folder.mkdirs()) {
        Files.writeString(xml.toPath(), "<B2STableSettings></B2STableSettings>");
        return xml;
      }
    }
    catch (IOException e) {
      LOG.error("Cannot create file " + xml.getAbsolutePath(), e);
    }
    throw new RuntimeException("Cannot find nor create file " + xml.getAbsolutePath());
  }

  //-------------------------------------------- DirectB2SAndVersions ---

  public List<DirectB2S> getBackglasses() {
    ArrayList<DirectB2S> result = new ArrayList<>(cacheDirectB2SVersion.values());
    Collections.sort(result, Comparator.comparing(o -> o.getName().toLowerCase()));
    return result;
  }

  private void reloadDirectB2SAndVersions() {
    cacheDirectB2SVersion.clear();

    List<DirectB2S> result = new ArrayList<>();

    long start = System.currentTimeMillis();
    List<GameEmulator> gameEmulators = emulatorService.getBackglassGameEmulators();
    for (GameEmulator gameEmulator : gameEmulators) {
      if (!gameEmulator.isEnabled()) {
        continue;
      }

      if (StringUtils.isEmpty(gameEmulator.getGamesDirectory())) {
        LOG.info("Skipping backglass scan for: {}, no games directory found.", gameEmulator);
        continue;
      }

      File tablesFolder = gameEmulator.getGamesFolder();
      if (!tablesFolder.exists()) {
        LOG.info("Skipping backglass scan for: {}", tablesFolder.getAbsolutePath());
        continue;
      }

      Path tablesPath = tablesFolder.toPath();

      LOG.info("Running backglass scan for: {}", tablesFolder.getAbsolutePath());
      IOFileFilter filter = new SuffixFileFilter(new String[]{"directb2s"}, IOCase.INSENSITIVE);
      Iterator<File> iter = org.apache.commons.io.FileUtils.iterateFiles(tablesFolder, filter, DirectoryFileFilter.DIRECTORY);
      DirectB2S currentDirectB2S = null;
      while (iter.hasNext()) {
        File file = iter.next();
        String fileName = tablesPath.relativize(file.toPath()).toString();
        String mainName = FileUtils.fromUniqueFile(fileName);

        // new backglass detected
        if (currentDirectB2S == null || !currentDirectB2S.getFileName().equals(mainName)) {
          currentDirectB2S = new DirectB2S();
          currentDirectB2S.setEmulatorId(gameEmulator.getId());
          addGameInfo(currentDirectB2S, gameEmulator, FilenameUtils.removeExtension(mainName));

          result.add(currentDirectB2S);
          // add in cache
          setDirectB2SAndVersions(gameEmulator.getId(), mainName, currentDirectB2S);
        }
        currentDirectB2S.addVersion(tablesPath.relativize(file.toPath()).toString());
      }
    }

    long duration = System.currentTimeMillis() - start;
    LOG.info("Backglass scan finished, scanned {} files in {}ms", cacheDirectB2SVersion.size(), duration);
  }

  private void addGameInfo(DirectB2S currentDirectB2S, GameEmulator gameEmulator, String mainBaseName) {
    String gameFilename = mainBaseName + "." + gameEmulator.getGameExt();
    Game g = frontendService.getGameByFilename(gameEmulator.getId(), gameFilename);
    currentDirectB2S.setGameId(g != null ? g.getId() : -1);
  }

  public DirectB2S getDirectB2SAndVersions(Game game) {
    if (game != null) {
      return reloadDirectB2SAndVersions(game.getEmulator(), game.getGameFileName());
    }
    return null;
  }

  public DirectB2S getDirectB2SAndVersions(int emulatorId, String fileName) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    return reloadDirectB2SAndVersions(emulator, fileName);
  }

  public DirectB2S removeDirectB2SAndVersions(int emulatorId, String fileName) {
    String baseName = FileUtils.baseUniqueFile(fileName);
    return cacheDirectB2SVersion.remove(emulatorId + "@" + baseName);
  }

  public void setDirectB2SAndVersions(int emulatorId, String fileName, DirectB2S b2s) {
    String baseName = FileUtils.baseUniqueFile(fileName);
    cacheDirectB2SVersion.put(emulatorId + "@" + baseName, b2s);
  }

  @Nullable
  private DirectB2S reloadDirectB2SAndVersions(@NonNull GameEmulator emulator, String fileName) {
    //do not check this for emulators that do not support backglasses anyway
    if (emulator.isMameEmulator() || emulator.isOtherEmulator()) {
      return null;
    }

    if (StringUtils.isEmpty(emulator.getGamesDirectory())) {
      LOG.info("Return DirectB2SAndVersions null, emulator {} has no game directory set.", emulator.getName());
      return null;
    }

    File gamesDirectory = new File(emulator.getGamesDirectory());
    if (!gamesDirectory.exists()) {
      LOG.info("Return DirectB2SAndVersions null, emulator {} has an invalid game directory set.", emulator.getGamesDirectory());
      return null;
    }

    //It's ok to return an empty object as long as the game directory is there
    File file = new File(emulator.getGamesDirectory(), fileName);
    if (file.getParentFile() == null || !file.getParentFile().exists()) {
      return null;
    }

    String[] fileNames = file.getParentFile().list();
    if (fileNames == null) {
      LOG.info("Return DirectB2SAndVersions null as there is no file to process in folder {}", gamesDirectory.getAbsolutePath());
      return null;
    }

    // First get the baseName without unicity marker of all directb2s to be renamed
    // mind that it contains the folder of fileName when fileName is in a subfolder
    String mainBaseName = FileUtils.baseUniqueFile(fileName);
    // The optional subfolder
    String relativeFolder = new File(fileName).getParent();
    relativeFolder = relativeFolder == null ? "" : relativeFolder + File.separatorChar;

    DirectB2S b2s = new DirectB2S();
    b2s.setEmulatorId(emulator.getId());

    // now iterate over files
    for (String n : fileNames) {
      // skip non backglass files
      if (n.toLowerCase().endsWith(".directb2s")) {
        String uniqueName = relativeFolder + FileUtils.baseUniqueFile(n);
        // version found
        if (uniqueName.equalsIgnoreCase(mainBaseName)) {
          b2s.addVersion(relativeFolder + n);
        }
      }
    }

    addGameInfo(b2s, emulator, mainBaseName);

    // update cache
    if (b2s.getNbVersions() > 0) {
      cacheDirectB2SVersion.put(emulator.getId() + "@" + mainBaseName, b2s);
      return b2s;
    }
    else {
      cacheDirectB2SVersion.remove(emulator.getId() + "@" + mainBaseName);
      return null;
    }
  }

  //-------------------------------------------- OPERATIONS ---

  public DirectB2S rename(int emulatorId, String fileName, String newName) {
    String baseName = FileUtils.baseUniqueFile(fileName);
    DirectB2S b2s = cacheDirectB2SVersion.get(emulatorId + "@" + baseName);
    if (b2s != null) {
      GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
      //File parentFile = new File(emulator.getGamesDirectory(), fileName).getParentFile();

      // First get the baseNames without unicity marker and extension of the target file
      String relativeFolder = new File(fileName).getParent();
      relativeFolder = relativeFolder == null ? "" : relativeFolder + File.separatorChar;
      String newBaseName = relativeFolder + FileUtils.baseUniqueFile(newName);

      // make a copy of all versions then clear
      List<String> versions = new ArrayList<>(b2s.getVersions());
      b2s.clearVersions();
      for (String version : versions) {
        // rename the file
        String newVersion = StringUtils.replaceIgnoreCase(version, baseName, newBaseName);
        File b2sFile = new File(emulator.getGamesDirectory(), version);
        File b2sNewFile = new File(emulator.getGamesDirectory(), newVersion);
        if (b2sFile.exists() && b2sFile.renameTo(b2sNewFile)) {
          cacheDirectB2SData.remove(b2sFile.getPath());
          LOG.info("Renamed \"" + b2sFile + "\" to \"" + b2sNewFile + "\"");
          b2s.addVersion(newVersion);
        }
        else {
          LOG.error("Cannot rename \"" + b2sFile + "\" to \"" + b2sNewFile + "\"");
        }
      }

      addGameInfo(b2s, emulator, FilenameUtils.removeExtension(b2s.getFileName()));
    }
    return b2s;
  }

  /**
   * kept as used in tests
   */
  public DirectB2S duplicate(int emulatorId, String fileName) throws IOException {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    File b2sFile = new File(emulator.getGamesDirectory(), fileName);
    try {
      File target = FileUtils.uniqueFile(b2sFile);
      org.apache.commons.io.FileUtils.copyFile(b2sFile, target);
      LOG.info("Copied \"" + fileName + "\" to \"" + target.getAbsolutePath() + "\"");
      return reloadDirectB2SAndVersions(emulator, fileName);
    }
    catch (IOException e) {
      LOG.error("Failed to duplicate backglass " + b2sFile.getAbsolutePath() + ": " + e.getMessage(), e);
      throw e;
    }
  }

  public DirectB2S setAsDefault(int emulatorId, String fileName) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    File b2sFile = new File(emulator.getGamesDirectory(), fileName);

    String mainFileName = FileUtils.fromUniqueFile(fileName);
    if (!fileName.equals(mainFileName)) {
      File mainFile = new File(emulator.getGamesDirectory(), mainFileName);
      if (mainFile.exists()) {
        File tempFile = new File(b2sFile.getParentFile(), b2sFile.getName() + ".tmp");
        if (b2sFile.renameTo(tempFile) && mainFile.renameTo(b2sFile)) {
          cacheDirectB2SData.remove(b2sFile.getPath());
          b2sFile = tempFile;
        }
        else {
          throw new RuntimeException("Cannot rename " + mainFile + ", operation ignored");
        }
      }
      if (b2sFile.renameTo(mainFile)) {
        cacheDirectB2SData.remove(mainFile.getPath());
      }
      else {
        throw new RuntimeException("Cannot rename " + b2sFile + ", operation ignored");
      }
    }
    return reloadDirectB2SAndVersions(emulator, fileName);
  }

  public DirectB2S disable(int emulatorId, String fileName) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    String mainFileName = FileUtils.fromUniqueFile(fileName);
    File mainFile = new File(emulator.getGamesDirectory(), mainFileName);
    if (mainFile.exists()) {
      File target = FileUtils.uniqueFile(mainFile);
      if (!mainFile.renameTo(target)) {
        throw new RuntimeException("Cannot rename " + mainFile + " to " + target);
      }
      cacheDirectB2SData.remove(mainFile.getPath());
    }
    return reloadDirectB2SAndVersions(emulator, fileName);
  }

  public boolean deleteBackglass(int emulatorId, String filename) {
    DirectB2S b2s = removeDirectB2SAndVersions(emulatorId, filename);
    boolean success = true;
    if (b2s != null) {
      GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
      for (String version : b2s.getVersions()) {
        File b2sFile = new File(emulator.getGamesDirectory(), version);
        if (b2sFile.exists() && b2sFile.delete()) {
          cacheDirectB2SData.remove(b2sFile.getPath());
          LOG.info("Deleted " + b2sFile.getAbsolutePath());
        }
        else {
          LOG.info("Cannot delete " + b2sFile.getAbsolutePath());
          success = false;
        }
      }
    }
    return success;
  }

  public DirectB2S deleteVersion(int emulatorId, String filename) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    File b2sFile = new File(emulator.getGamesDirectory(), filename);
    if (b2sFile.exists() && b2sFile.delete()) {
      cacheDirectB2SData.remove(b2sFile.getPath());
      LOG.info("Deleted " + b2sFile.getAbsolutePath());
    }
    return reloadDirectB2SAndVersions(emulator, filename);
  }

  //-------------------------------------------- SCREENRES ---

  public DirectB2sScreenRes getScreenRes(Game game, boolean perTableOnly) {
    if (game != null) {
      GameEmulator emulator = game.getEmulator();
      String b2sFilename = game.getDirectB2SFilename();
      File b2sFile = new File(emulator.getGamesDirectory(), b2sFilename);
      DirectB2sScreenRes res = getScreenRes(b2sFile, perTableOnly);
      res.setB2SFileName(b2sFilename);
      res.setEmulatorId(emulator.getId());
      res.setGameId(game.getId());
      return res;
    }
    return null;
  }

  public DirectB2sScreenRes getScreenRes(int emulatorId, String fileName, @Nullable Game game, boolean perTableOnly) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    if (emulator != null) {
      File b2sFile = new File(emulator.getGamesDirectory(), fileName);
      DirectB2sScreenRes res = getScreenRes(b2sFile, perTableOnly);
      if (res != null) {
        res.setEmulatorId(emulatorId);
        res.setB2SFileName(fileName);
        res.setGameId(game != null ? game.getId() : -1);
        return res;
      }
    }
    return null;
  }

  public DirectB2sScreenRes getGlobalScreenRes() {
    return getScreenRes((File) null, false);
  }

  private DirectB2sScreenRes getScreenRes(@Nullable File b2sFile, boolean perTableOnly) {
    List<String> lines = readScreenRes(b2sFile, false, perTableOnly);
    if (lines == null) {
      return null;
    }
    DirectB2sScreenRes res = new DirectB2sScreenRes();
    res.setScreenresFilePath(lines.remove(0));
    res.setGlobal(b2sFile == null || StringUtils.containsIgnoreCase(res.getScreenresFilePath(), b2sFile.getName()));

    // cf https://github.com/vpinball/b2s-backglass/blob/7842b3638b62741e21ebb511e2a886fa2091a40f/b2s_screenresidentifier/b2s_screenresidentifier/module.vb#L105
    res.setPlayfieldWidth(parseIntSafe(lines.get(0)));
    res.setPlayfieldHeight(parseIntSafe(lines.get(1)));

    res.setBackglassWidth(parseIntSafe(lines.get(2)));
    res.setBackglassHeight(parseIntSafe(lines.get(3)));

    res.setBackglassDisplay(lines.get(4));

    res.setBackglassX(parseIntSafe(lines.get(5)));
    res.setBackglassY(parseIntSafe(lines.get(6)));

    res.setDmdWidth(parseIntSafe(lines.get(7)));
    res.setDmdHeight(parseIntSafe(lines.get(8)));

    res.setDmdX(parseIntSafe(lines.get(9)));
    res.setDmdY(parseIntSafe(lines.get(10)));

    res.setDmYFlip("1".equals(lines.get(11)));

    if (lines.size() > 16) {
      res.setBackgroundX(parseIntSafe(lines.get(12)));
      res.setBackgroundY(parseIntSafe(lines.get(13)));

      res.setBackgroundWidth(parseIntSafe(lines.get(14)));
      res.setBackgroundHeight(parseIntSafe(lines.get(15)));

      File file = new File(lines.get(16));
      if (file.exists()) {
        res.setBackgroundFilePath(lines.get(16));
      }

      if (lines.size() > 17) {
        res.setB2SWindowPunch(lines.get(17));
      }
    }
    return res;
  }

  /**
   * Load a screen.res file and returns the lines
   *
   * @param b2sFile      The associated directb2s file to get table filename
   * @param withComment  Whether comment lines must be returned (true) or filtered (false)
   * @param perTableOnly Load only the file if it is table dedicated one, else null
   * @return the List of all lines in the file
   */
  private List<String> readScreenRes(@Nullable File b2sFile, boolean withComment, boolean perTableOnly) {
    if (screenresTxt == null) {
      // The default filename ScreenRes.txt can be altered by setting the registry key
      // Software\B2S\B2SScreenResFileNameOverride to a different filename.
      screenresTxt = StringUtils.defaultIfEmpty(
          systemService.readUserValue("Software\\B2S", "B2SScreenResFileNameOverride"),
          "ScreenRes.txt");
    }

    File target = null;
    if (b2sFile != null) {
      // see https://github.com/vpinball/b2s-backglass/wiki/Screenres.txt
      // When the B2S Server starts, it tries to find the ScreenRes files in this order  (from backglassServer documentation):
      //  1) tablename.res next to the tablename.vpx
      target = new File(b2sFile.getParentFile(), FilenameUtils.getBaseName(b2sFile.getName()) + ".res");
      if (!target.exists()) {
        //  2) Screenres.txt (or whatever set in the registry) in the same folder as tablename.vpx
        target = new File(b2sFile.getParentFile(), screenresTxt);
        if (perTableOnly || !target.exists()) {
          //  3) Screenres.txt (or whatever set in the registry) as tablename/Screenres.txt
          File tableFolder = new File(b2sFile.getParentFile(), FilenameUtils.getBaseName(b2sFile.getName()));
          target = new File(tableFolder, screenresTxt);
        }
      }
    }

    // load global screen res file
    if (!perTableOnly && (target == null || !target.exists())) {
      //  4) Screenres.txt ( or whatever you set in the registry) in the folder where the B2SBackglassServerEXE.exe is located
      target = new File(getBackglassServerFolder(), screenresTxt);
    }

    if (target == null || !target.exists()) {
      return null;
    }

    List<String> lines = new ArrayList<>();
    lines.add(target.getAbsolutePath());
    try (BufferedReader reader = new BufferedReader(new FileReader(target))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (withComment || !line.startsWith("#")) {
          lines.add(line.trim());
        }
      }
      return lines;
    }
    catch (Exception e) {
      LOG.error("Cannor open screen res file " + target.getAbsolutePath(), e);
      return null;
    }
  }

  public void saveScreenRes(DirectB2sScreenRes screenres, @Nullable Game game) throws Exception {
    GameEmulator emulator = emulatorService.getGameEmulator(screenres.getEmulatorId());
    File b2sFile = new File(emulator.getGamesDirectory(), screenres.getB2SFileName());

    List<String> lines = readScreenRes(b2sFile, true, false);
    if (lines == null) {
      throw new IOException("Cannot find an existing table nor table .res");
    }
    String templateName = lines.remove(0);
    // if already a table file exists, replace it 
    File screenresFile = StringUtils.containsIgnoreCase(templateName, FilenameUtils.getBaseName(screenres.getB2SFileName())) ?
        new File(templateName) : new File(emulator.getGamesDirectory(), StringUtils.replaceIgnoreCase(screenres.getB2SFileName(), ".directb2s", ".res"));

    if (!screenresFile.exists() || screenresFile.delete()) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(screenresFile))) {

        // generate version number in case it is not in th eoriginal file
        if (!lines.get(0).replace(" ", "").toLowerCase().startsWith("#v2")) {
          // fetch the b2s version from the file itself
          File b2sServerExe = new File(getBackglassServerFolder(), "B2SBackglassServerEXE.exe");
          String b2sVersion = FileVersion.fetch(b2sServerExe);
          writer.write("# V" + b2sVersion);
          writer.write(System.lineSeparator());
        }
        int currentLine = 0;
        while (lines.size() <= 16) {
          lines.add("");
        }
        for (String line : lines) {
          if (line.startsWith("#")) {
            writer.write(line);
          }
          else {
            switch (currentLine++) {
              case 0:
                writer.write(Integer.toString(screenres.getPlayfieldWidth()));
                break;
              case 1:
                writer.write(Integer.toString(screenres.getPlayfieldHeight()));
                break;
              case 2:
                writer.write(Integer.toString(screenres.getBackglassWidth()));
                break;
              case 3:
                writer.write(Integer.toString(screenres.getBackglassHeight()));
                break;
              case 4:
                writer.write(screenres.getBackglassDisplay());
                break;
              case 5:
                writer.write(Integer.toString(screenres.getBackglassX()));
                break;
              case 6:
                writer.write(Integer.toString(screenres.getBackglassY()));
                break;
              case 7:
                writer.write(Integer.toString(screenres.getDmdWidth()));
                break;
              case 8:
                writer.write(Integer.toString(screenres.getDmdHeight()));
                break;
              case 9:
                writer.write(Integer.toString(screenres.getDmdX()));
                break;
              case 10:
                writer.write(Integer.toString(screenres.getDmdY()));
                break;
              case 11:
                writer.write(screenres.getDmdYFlip() ? "1" : "0");
                break;
              case 12:
                writer.write(Integer.toString(screenres.getBackgroundX()));
                break;
              case 13:
                writer.write(Integer.toString(screenres.getBackgroundY()));
                break;
              case 14:
                writer.write(Integer.toString(screenres.getBackgroundWidth()));
                break;
              case 15:
                writer.write(Integer.toString(screenres.getBackgroundHeight()));
                break;
              case 16:
                writer.write(StringUtils.defaultString(screenres.getBackgroundFilePath()));
                break;
              default:
                // write all other lines unchanged 
                writer.write(line);
            }
          }
          writer.write(System.lineSeparator());
        }
        // generate the last line if not done
        if (currentLine == 16 && StringUtils.isNotEmpty(screenres.getBackgroundFilePath())) {
          writer.write(screenres.getBackgroundFilePath());
          writer.write(System.lineSeparator());
        }
      }
    }
    else {
      throw new IOException("Cannot overwrite existing table screen res file " + screenresFile.getAbsolutePath());
    }

    // now everything is saved, automatically turns settings in B2STableSettings

    // the Game must have been got with gameService, this  will ensure that a scanned table is fetched and get the rom
    if (game != null) {
      DirectB2STableSettings settings = getTableSettings(game);
      if (settings == null && StringUtils.isNotEmpty(game.getRom())) {
        settings = new DirectB2STableSettings();
        settings.setRom(game.getRom());
      }
      // If no settings found and no rom on game to associate the settings with, skip that phase
      if (settings != null) {
        boolean hasToBeSaved = false;

        if (settings.getStartAsEXE() != 1 && screenres.isTurnOnRunAsExe()) {
          hasToBeSaved = true;
          settings.setStartAsEXE(1);
        }
        // mind here 0=visible
        if (settings.getStartBackground() != 0 && screenres.isTurnOnBackground()) {
          hasToBeSaved = true;
          settings.setStartBackground(0);
        }

        if (hasToBeSaved) {
          saveTableSettings(game.getId(), settings);
        }
      }
    }
  }

  public String setScreenResFrame(int emulatorId, String b2sFilename, String screenName, InputStream is) throws IOException {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    File frameFolder = new File(emulator.getGamesDirectory(), "_Frames");
    if (frameFolder.exists() || is != null && frameFolder.mkdir()) {
      File frameFile = new File(frameFolder, screenName);
      Files.deleteIfExists(frameFile.toPath());
      if (is != null) {
        try (FileOutputStream out = new FileOutputStream(frameFile)) {
          StreamUtils.copy(is, out);
        }
      }
      // return the filename created or deleted in case of success
      return frameFile.getAbsolutePath();
    }
    else {
      LOG.error("Cannot create _frames Folder " + emulator.getGamesDirectory());
      return null;
    }
  }

  //----------------------------------- FrontendFullMedia manipulation ---

  public void useFrontendFullDMDMedia(int gameId) {
    TableDetails tableDetails = frontendService.getTableDetails(gameId);
    if (tableDetails != null) {
      String keepDisplays = VPinScreen.keepDisplaysAddScreen(tableDetails.getKeepDisplays(), VPinScreen.Menu);
      tableDetails.setKeepDisplays(keepDisplays);
      frontendService.saveTableDetails(gameId, tableDetails);
    }
  }

  public void grabFrontendFullDMDMedia(Game game) {
    FrontendMedia frontendMedia = frontendService.getGameMedia(game.getId());
    FrontendMediaItem item = frontendMedia.getDefaultMediaItem(VPinScreen.Menu);
    try {
      byte[] img = grabFromFrontendMedia(item);
      if (img != null) {
        String base64 = DatatypeConverter.printBase64Binary(img);
        setDmdImage(game.getEmulatorId(), game.getDirectB2SFilename(), item.getFile().getName(), base64);
      }
    }
    catch (Exception ioe) {
      LOG.error("Error while grabbing image from frontendmedia", ioe);
    }
  }

  public byte[] grabFromFrontendMedia(FrontendMediaItem item) {
    if (item != null && item.getFile().exists()) {
      String baseType = MimeTypeUtil.determineBaseType(item.getMimeType());
      if ("image".equals(baseType)) {
        try {
          return Files.readAllBytes(item.getFile().toPath());
        }
        catch (IOException e) {
          LOG.error("Failed to copy resource file as background: " + e.getMessage(), e);
        }
      }
      else if ("video".equals(baseType)) {
        return JCodec.grab(item.getFile());
      }
    }
    return null;
  }

  //-------------------------------------------- PREVIEWS in BYTES[] ---

  // need an enriched game with rom
  public byte[] getPreviewBackground(Game game, boolean includeFrame) {
    DirectB2SData tableData = getDirectB2SData(game);
    return getPreviewBackground(tableData, game, includeFrame);
  }

  // need an enriched game with rom
  public byte[] getPreviewBackground(int emuId, String filename, @Nullable Game game, boolean includeFrame) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile != null) {
      DirectB2SData tableData = getDirectB2SData(b2sFile, emuId, filename);
      return getPreviewBackground(tableData, game, includeFrame);
    }
    return new byte[]{};
  }

  public byte[] getPreviewBackground(DirectB2SData tableData, @Nullable Game game, boolean includeFrame) {
    if (tableData != null && tableData.isBackgroundAvailable()) {
      String base64 = getBackgroundBase64(tableData.getEmulatorId(), tableData.getFilename());
      if (base64 != null) {
        byte[] bytes = DatatypeConverter.parseBase64Binary(base64);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
          BufferedImage preview = ImageIO.read(bais);
          if (tableData.getGrillHeight() > 0) {
            DirectB2STableSettings tableSettings = game != null ? getTableSettings(game) : null;
            boolean hideGrill = false;
            if (tableSettings != null && tableSettings.getHideGrill() == 2) {
              DirectB2ServerSettings serverSettings = getServerSettings();
              hideGrill = serverSettings != null && serverSettings.isHideGrill();
            }
            else {
              hideGrill = (tableSettings != null && tableSettings.getHideGrill() == 1);
            }
            if (hideGrill) {
              preview = preview.getSubimage(0, 0, preview.getWidth(), preview.getHeight() - tableData.getGrillHeight());
            }
          }

          if (includeFrame) {
            GameEmulator emulator = game != null ? game.getEmulator() : emulatorService.getGameEmulator(tableData.getEmulatorId());
            File b2sFile = new File(emulator.getGamesDirectory(), tableData.getFilename());
            DirectB2sScreenRes screenres = getScreenRes(b2sFile, true);
            if (screenres != null && screenres.hasFrame()) {
              File frameFile = new File(screenres.getBackgroundFilePath());
              if (frameFile.exists()) {
                BufferedImage combined = new BufferedImage(screenres.getBackgroundWidth(), screenres.getBackgroundHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = combined.getGraphics();

                BufferedImage frameImage = ImageIO.read(frameFile);
                g.drawImage(frameImage, 0, 0,
                    screenres.getBackgroundWidth(),
                    screenres.getBackgroundHeight(),
                    null);

                g.drawImage(preview,
                    screenres.getBackglassX() - screenres.getBackgroundX(),
                    screenres.getBackglassY() - screenres.getBackgroundY(),
                    screenres.getBackglassWidth(),
                    screenres.getBackglassHeight(),
                    null);
                g.dispose();
                preview = combined;
              }
            }
          }

          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ImageIO.write(preview, "png", baos);
          return baos.toByteArray();
        }
        catch (IOException ioe) {
          LOG.error("Cannot generate preview image for {]}", tableData.getFilename(), ioe);
        }
      }
    }
    // not found or error
    return null;
  }

  public byte[] getPreviewDmd(Game game) {
    DirectB2SData tableData = getDirectB2SData(game);
    return getPreviewDmd(tableData, game);
  }

  public byte[] getPreviewDmd(DirectB2SData tableData, @Nullable Game game) {
    if (tableData != null && tableData.isBackgroundAvailable()) {
      String base64 = getDmdBase64(tableData.getEmulatorId(), tableData.getFilename());
      if (base64 != null) {
        return DatatypeConverter.parseBase64Binary(base64);
      }
    }
    // not found or error
    return null;
  }

  //------------------------------

  public static int parseIntSafe(String value) {
    return parseIntSafe(value, 0);
  }

  public static int parseIntSafe(String value, int defaultValue) {
    try {
      if (!StringUtils.isEmpty(value)) {
        value = value.replaceAll("@", "").trim();
        return Integer.parseInt(value);
      }
    }
    catch (NumberFormatException e) {
      //ignore
    }
    return defaultValue;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // force initialisation of table cache
    reloadDirectB2SAndVersions();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

}
