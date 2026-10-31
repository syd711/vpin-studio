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
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.BackglassNamingHelper;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.system.VidUtil;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

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
  private DOFLinxService dofLinxService;

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

  /**
   * Background and DMD base64 images of the most recently parsed backglasses, kept small as images are big
   */
  private static final int IMAGES_CACHE_SIZE = 2;
  private final Map<String, String[]> cacheImagesBase64 = Collections.synchronizedMap(new LinkedHashMap<>(IMAGES_CACHE_SIZE + 1, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, String[]> eldest) {
      return size() > IMAGES_CACHE_SIZE;
    }
  });

  private DirectB2ServerSettings serverSettings;

  private Map<String, DirectB2STableSettings> cacheB2STableSettings = new ConcurrentHashMap<>();


  public boolean clearCache() {
    reloadDirectB2SAndVersions();
    reloadTableSettings();
    cacheDirectB2SData.clear();
    cacheImagesBase64.clear();
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
  public DirectB2SData getDirectB2SData(@Nullable Game game) {
    if (game != null) {
      String directB2SFilename = BackglassNamingHelper.getBackglassFileName(game);
      String baseName = FilenameUtils.removeExtension(directB2SFilename).trim();
      DirectB2S b2s = cacheDirectB2SVersion.get(game.getEmulatorId() + "@" + baseName);
      if (b2s == null) {
        String reloadFileName = game.getGameFileName();
        if (!game.isZenGame()) {
          // the backglass may be named after the table's folder, so look for the name it really has
          String relativeFolder = new File(reloadFileName).getParent();
          reloadFileName = (relativeFolder == null ? "" : relativeFolder + File.separatorChar) + directB2SFilename;
        }
        b2s = reloadDirectB2SAndVersions(game.getEmulator(), reloadFileName);
      }
      if (b2s != null) {
        if (b2s.isEnabled()) {
          File directB2SFile = game.getDirectB2SFile();
          return getDirectB2SData(directB2SFile, game.getEmulatorId(), directB2SFilename);
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
    DirectB2SData cached = cacheDirectB2SData.get(emulatorId + "|" + directB2SFile.getPath());
    if (cached != null) {
      return cached;
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

    cacheAdd(emulatorId, directB2SFile, data);
    // keep the images as previews are usually requested right after the data
    imagesCacheAdd(directB2SFile, extractor);
    return data;
  }

  private void extractBackgroundData(DirectB2SData data, String backgroundBase64) throws IOException {
    if (backgroundBase64 != null) {
      byte[] imageData = Base64.getMimeDecoder().decode(sanitizeBase64(backgroundBase64));
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
        byte[] dmdData = Base64.getMimeDecoder().decode(sanitizeBase64(dmdBase64));
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

  private String sanitizeBase64(String base64) {
    if (base64 != null && base64.startsWith("data:image")) {
      return base64.substring(base64.indexOf(",") + 1);
    }
    return base64;
  }

  @Nullable
  private File getB2sFile(int emuId, String filename) {
    GameEmulator emulator = emulatorService.getGameEmulator(emuId);
    if (emulator.isZenEmulator()) {
      return new File(dofLinxService.getBackglassesFolder(emulator), filename);
    }

    if (emulator.getGamesDirectory() != null) {
      return new File(emulator.getGamesDirectory(), filename);
    }

    return null;
  }

  @Nullable
  public String getBackgroundBase64(int emuId, String filename) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile != null && b2sFile.exists()) {
      return getImagesBase64(b2sFile, emuId, filename)[0];
    }
    return null;
  }

  /**
   * Background of the backglass actually used by the game, whatever its name and type
   */
  @Nullable
  public String getBackgroundBase64(@NonNull Game game) {
    File b2sFile = game.getDirectB2SFile();
    if (b2sFile.exists()) {
      return getImagesBase64(b2sFile, game.getEmulatorId(), b2sFile.getName())[0];
    }
    return null;
  }

  @Nullable
  public String getDmdBase64(int emuId, String filename) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile != null && b2sFile.exists()) {
      return getImagesBase64(b2sFile, emuId, filename)[1];
    }
    return null;
  }

  /**
   * Returns the background and DMD base64 images, parsing the file only when not recently done.
   * Backglasses, especially .B2SPro ones, can be 30MB+ so a full parse per preview is expensive.
   */
  private String[] getImagesBase64(@NonNull File b2sFile, int emuId, String filename) {
    String[] images = cacheImagesBase64.get(imagesCacheKey(b2sFile));
    if (images == null) {
      DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
      extractor.extractData(b2sFile, emuId, filename);
      images = imagesCacheAdd(b2sFile, extractor);
    }
    return images;
  }

  public boolean setDmdImage(int emuId, String filename, String file, String dmdBase64) {
    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile != null && b2sFile.exists()) {
      try {
        DirectB2SDataUpdater updater = new DirectB2SDataUpdater();
        updater.updateDmdImage(b2sFile, file, dmdBase64, false);
        // clean cache
        cacheRemove(emuId, b2sFile);
        return true;
      }
      catch (Exception e) {
        LOG.error("Error while updating {}", filename, e);
      }
    }
    return false;
  }


  public void upddateScoresDisplayState(Game game, boolean state) {
    if (game != null && game.getDirectB2SPath() != null) {
      File directB2SFile = game.getDirectB2SFile();
      String directB2SFileName = BackglassNamingHelper.getBackglassFileName(game);
      DirectB2SData data = getDirectB2SData(directB2SFile, game.getEmulatorId(), directB2SFileName);
      if (data != null && data.getNbScores() > 0) {
        try {
          DirectB2SDataUpdater updater = new DirectB2SDataUpdater();
          updater.upddateScoresDisplayState(directB2SFile, state, false);
          // clean cache
          cacheRemove(game.getEmulatorId(), directB2SFile);
        }
        catch (Exception e) {
          LOG.error("Error while updating {}", directB2SFileName, e);
        }
      }
    }
  }

  //--------------------------------------

  public DirectB2SDetail getBackglassDetail(int emuId, String filename, Game game) {
    DirectB2SDetail detail = new DirectB2SDetail();
    detail.setEmulatorId(emuId);
    detail.setFilename(filename);

    File b2sFile = getB2sFile(emuId, filename);
    if (b2sFile == null) {
      return null;
    }

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

  //-------------------------------------- B2STableSettings ---

  private void reloadTableSettings() {
    if (Features.IS_STANDALONE) {
      // try from VPinballX.ini
      File ini = systemService.getStandaloneConfigFile();
      if (ini != null && ini.exists()) {
        B2STableSettingsParser parser = new B2STableSettingsParser();
        this.serverSettings = parser.parseServerSettingsIni(ini);
        this.cacheB2STableSettings.clear();
      }
    }
    else {
      File settingsXml = getB2STableSettingsXml();
      if (settingsXml != null && settingsXml.exists()) {
        B2STableSettingsParser parser = new B2STableSettingsParser();
        parser.parseSettingsXml(getBackglassServerFolder(), settingsXml);
        this.serverSettings = parser.getServerSettings();
        this.cacheB2STableSettings = parser.getTableSettings();
      }
    }

    if (this.serverSettings != null) {
      this.serverSettings.setExtendedSearch(false);
      Map<String, Object> currentUserValues = systemService.getCurrentUserValues("Software\\B2S");
      if (currentUserValues.containsKey("B2STableSettingsExtendedPath")) {
        Object value = currentUserValues.get("B2STableSettingsExtendedPath");
        try {
          if (Integer.parseInt(String.valueOf(value)) == 1) {
            this.serverSettings.setExtendedSearch(true);
          }
        }
        catch (NumberFormatException e) {
          //ignore
        }
      }
    }
  }

  public DirectB2STableSettings saveTableSettings(Game game, DirectB2STableSettings settings) throws VPinStudioException {
    try {
      if (Features.IS_STANDALONE) {
        File ini = game.getIniFile();
        if (ini != null && ini.exists()) {
          B2STableSettingsSerializer serverSettingsSerializer = new B2STableSettingsSerializer();
          serverSettingsSerializer.serializeIni(settings, serverSettings, ini);
          // add in cache
          tableSettingsCacheAdd(settings);
        }
      }
      else {
        File settingsXml = getB2STableSettingsXml();
        B2STableSettingsSerializer tableSettingsSerializer = new B2STableSettingsSerializer();
        tableSettingsSerializer.serializeXml(settings, settingsXml, true);
        // recreate tableSettings cache
        reloadTableSettings();
      }
      return settings;
    }
    catch (VPinStudioException e) {
      LOG.error("Failed to save table settings for \"{}\": {}", game.getId(), e.getMessage(), e);
      throw e;
    }
  }

  public boolean deleteB2STableSettings(Game game) {
    String rom = game.getRom();
    if (StringUtils.isNotEmpty(rom)) {
      rom = StringUtils.deleteWhitespace(rom);
      DirectB2STableSettings entry = tableSettingsCacheGet(rom, game.getTableName());

      if (entry != null) {
        try {
          File settingsXml = getB2STableSettingsXml();
          if (settingsXml.exists()) {
            B2STableSettingsSerializer serverSettingsSerializer = new B2STableSettingsSerializer();
            serverSettingsSerializer.serializeXml(entry, settingsXml, false);
            // recreate cache
            reloadTableSettings();
          }
        }
        catch (VPinStudioException e) {
          LOG.error("Failed to remote B2STableSettings.xml entry {}: {}", entry.getRom(), e.getMessage());
        }
      }
    }
    return true;
  }

  @Nullable
  public DirectB2STableSettings getTableSettings(Game game) {
    String rom = game.getRom();
    if (!StringUtils.isEmpty(game.getRomAlias())) {
      rom = game.getRomAlias();
    }

    if (StringUtils.isNotEmpty(rom)) {
      rom = StringUtils.deleteWhitespace(rom);
      DirectB2STableSettings entry = cacheB2STableSettings.get(rom);
      if (entry == null && !StringUtils.isEmpty(game.getTableName())) {
        entry = cacheB2STableSettings.get(game.getTableName());
      }

      // for Standalone, load and cache on the fly
      if (Features.IS_STANDALONE && entry == null) {
        File ini = game.getIniFile();
        if (ini != null && ini.exists()) {
          B2STableSettingsParser parser = new B2STableSettingsParser();
          entry = parser.parseTableSettingsIni(ini, rom);
          tableSettingsCacheAdd(entry);
        }
      }

      // still null, create an empty one
      if (entry == null) {
        entry = serverSettings != null ? new DirectB2STableSettings(serverSettings) : new DirectB2STableSettings();
        entry.setRom(rom);
      }
      return entry;
    }
    return null;
  }

  public DirectB2ServerSettings getServerSettings() {
    return this.serverSettings;
  }

  public DirectB2ServerSettings saveServerSettings(DirectB2ServerSettings settings) throws VPinStudioException {
    try {
      if (settings.isExtendedSearch()) {
        systemService.setUserValue("Software\\B2S\\", "B2STableSettingsExtendedPath", "1");
      }
      else {
        systemService.deleteUserValue("Software\\B2S\\", "B2STableSettingsExtendedPath");
      }

      if (Features.IS_STANDALONE) {
        File ini = systemService.getStandaloneConfigFile();
        if (ini != null && ini.exists()) {
          B2STableSettingsSerializer serverSettingsSerializer = new B2STableSettingsSerializer();
          serverSettingsSerializer.serializeIni(settings, new DirectB2ServerSettings(), ini);

          // cache
          this.serverSettings = settings;
        }
      }
      else {
        File settingsXml = getB2STableSettingsXml();
        B2STableSettingsSerializer serverSettingsSerializer = new B2STableSettingsSerializer();
        serverSettingsSerializer.serializeXml(settings, settingsXml, true);
        // recreate cache
        reloadTableSettings();
      }

      return getServerSettings();
    }
    catch (VPinStudioException e) {
      LOG.error("Failed to save server settings: {}", e.getMessage(), e);
      throw e;
    }
  }

//-------------------------------------- Key Files ---

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
        Optional<Path> found = walkStream.filter(p -> Strings.CI.equals(p.getFileName().toString(), "B2SBackglassServer.dll")).findFirst();
        if (found.isPresent()) {
          return found.get().toFile().getParentFile();
        }
      }
      catch (IOException ioe) {
        LOG.error("Cannot find backglass server in {}, skip this emulator for B2S server search", emu.getInstallationDirectory(), ioe);
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
      LOG.error("Cannot create file {}", xml.getAbsolutePath(), e);
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
    long start = System.currentTimeMillis();

    cacheDirectB2SVersion.clear();
    List<GameEmulator> gameEmulators = emulatorService.getBackglassGameEmulators();
    for (GameEmulator gameEmulator : gameEmulators) {
      if (!gameEmulator.isEnabled()) {
        continue;
      }

      if (gameEmulator.isVpxEmulator()) {
        if (StringUtils.isEmpty(gameEmulator.getGamesDirectory())) {
          LOG.info("Skipping backglass scan for: {}, no games directory found.", gameEmulator);
          continue;
        }

        File tablesFolder = gameEmulator.getGamesFolder();
        if (!tablesFolder.exists()) {
          LOG.info("Skipping backglass scan for: {}", tablesFolder.getAbsolutePath());
          continue;
        }

        LOG.info("Running emulator {} backglass scan for: {}", gameEmulator.getName(), tablesFolder.getAbsolutePath());
        loadBackglassForFolder(gameEmulator, tablesFolder);
      }

      if (gameEmulator.isZenEmulator() && dofLinxService.isValid()) {
        File b2sFolder = dofLinxService.getBackglassesFolder(gameEmulator);
        if (b2sFolder != null && b2sFolder.exists()) {
          LOG.info("Running emulator {} backglass scan for: {}", gameEmulator.getName(), b2sFolder.getAbsolutePath());
          loadBackglassForFolder(gameEmulator, b2sFolder);
        }
      }
    }

    long duration = System.currentTimeMillis() - start;
    LOG.info("Backglass scan finished, scanned {} files in {}ms", cacheDirectB2SVersion.size(), duration);
  }

  private void loadBackglassForFolder(GameEmulator gameEmulator, File tablesFolder) {
    Path tablesPath = tablesFolder.toPath();
    IOFileFilter filter = new SuffixFileFilter(BackglassFileType.extensions(), IOCase.INSENSITIVE);
    Iterator<File> iter = org.apache.commons.io.FileUtils.iterateFiles(tablesFolder, filter, DirectoryFileFilter.DIRECTORY);
    while (iter.hasNext()) {
      File file = iter.next();
      String fileName = tablesPath.relativize(file.toPath()).toString();
      String mainName = FileUtils.fromUniqueFile(fileName);

      // versions of a backglass are grouped by base name, so .directb2s and .B2SPro files end in the same entry
      String key = gameEmulator.getId() + "@" + FileUtils.baseUniqueFile(fileName).replaceAll("\\\\", "/");
      DirectB2S currentDirectB2S = cacheDirectB2SVersion.get(key);
      // new backglass detected
      if (currentDirectB2S == null) {
        currentDirectB2S = new DirectB2S();
        currentDirectB2S.setEmulatorId(gameEmulator.getId());
        addGameInfo(currentDirectB2S, gameEmulator, FilenameUtils.removeExtension(mainName));

        // add in cache
        setDirectB2SAndVersions(gameEmulator.getId(), mainName, currentDirectB2S);
      }
      currentDirectB2S.addVersion(fileName);
    }
  }

  private void addGameInfo(DirectB2S currentDirectB2S, GameEmulator gameEmulator, String mainBaseName) {
    String gameFilename = mainBaseName + "." + gameEmulator.getGameExt();
    if (gameEmulator.isZenEmulator()) {
      Game g = frontendService.getGameByDisplayName(gameEmulator.getId(), mainBaseName);
      currentDirectB2S.setGameId(g != null ? g.getId() : -1);
    }
    else {
      Game g = frontendService.getGameByFilename(gameEmulator.getId(), gameFilename);
      currentDirectB2S.setGameId(g != null ? g.getId() : -1);
    }
  }

  public DirectB2S getCacheDirectB2SAndVersions(Game game) {
    if (game != null) {
      String baseName = FileUtils.baseUniqueFile(game.getGameFileName());
      String key = game.getEmulatorId() + "@" + baseName.replaceAll("\\\\", "/");
      if (cacheDirectB2SVersion.containsKey(key)) {
        return cacheDirectB2SVersion.get(key);
      }

      key = game.getEmulatorId() + "@" + FilenameUtils.getBaseName(BackglassNamingHelper.getBackglassFileName(game));
      if (cacheDirectB2SVersion.containsKey(key)) {
        return cacheDirectB2SVersion.get(key);
      }
    }
    return null;
  }

  @Nullable
  public DirectB2S getDirectB2SAndVersions(Game game) {
    if (game != null) {
      String backglassFileName = BackglassNamingHelper.getBackglassFileName(game);
      return reloadDirectB2SAndVersions(game.getEmulator(), backglassFileName);
    }
    return null;
  }

  public DirectB2S getDirectB2SAndVersions(int emulatorId, String fileName) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    return reloadDirectB2SAndVersions(emulator, fileName);
  }

  public DirectB2S removeDirectB2SAndVersions(int emulatorId, String fileName) {
    String baseName = FileUtils.baseUniqueFile(fileName);
    return cacheDirectB2SVersion.remove(emulatorId + "@" + baseName.replaceAll("\\\\", "/"));
  }

  public void setDirectB2SAndVersions(int emulatorId, String fileName, DirectB2S b2s) {
    String baseName = FileUtils.baseUniqueFile(fileName);
    cacheDirectB2SVersion.put(emulatorId + "@" + baseName.replaceAll("\\\\", "/"), b2s);
  }

  @Nullable
  private DirectB2S reloadDirectB2SAndVersions(@NonNull GameEmulator emulator, String fileName) {
    //do not check this for emulators that do not support backglasses anyway
    if (emulator.isMameEmulator() || emulator.isOtherEmulator() || emulator.isZaccariaEmulator()) {
      return null;
    }

    if (StringUtils.isEmpty(emulator.getBackglassDirectory())) {
      LOG.info("Return DirectB2SAndVersions null, emulator {} has no backglass directory set.", emulator.getName());
      return null;
    }

    File gamesDirectory = new File(emulator.getBackglassDirectory());
    if (!gamesDirectory.exists()) {
      LOG.info("Return DirectB2SAndVersions null, emulator {} has an invalid backglass directory set.", emulator.getGamesDirectory());
      return null;
    }

    //It's ok to return an empty object as long as the game directory is there
    File file = new File(emulator.getBackglassDirectory(), fileName);
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
      if (BackglassFileType.isBackglassFile(n)) {
        String uniqueName = relativeFolder + FileUtils.baseUniqueFile(n);
        // version found
        if (uniqueName.equalsIgnoreCase(mainBaseName)) {
          b2s.addVersion(relativeFolder + n);
        }
      }
    }

    addGameInfo(b2s, emulator, mainBaseName);

    // update cache
    if (b2s.getVersions().size() > 0) {
      cacheDirectB2SVersion.put(emulator.getId() + "@" + mainBaseName.replaceAll("\\\\", "/"), b2s);
      return b2s;
    }
    else {
      cacheDirectB2SVersion.remove(emulator.getId() + "@" + mainBaseName.replaceAll("\\\\", "/"));
      return null;
    }
  }

//-------------------------------------------- OPERATIONS ---

  public DirectB2S rename(int emulatorId, String fileName, String newName) {
    String baseName = FileUtils.baseUniqueFile(fileName);
    DirectB2S b2s = cacheDirectB2SVersion.get(emulatorId + "@" + baseName.replaceAll("\\\\", "/"));
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
        String newVersion = Strings.CI.replace(version, baseName, newBaseName);
        File b2sFile = new File(emulator.getGamesDirectory(), version);
        File b2sNewFile = new File(emulator.getGamesDirectory(), newVersion);
        if (b2sFile.exists() && b2sFile.renameTo(b2sNewFile)) {
          cacheRemove(emulatorId, b2sFile);
          LOG.info("Renamed \"{}\" to \"{}\"", b2sFile, b2sNewFile);
          b2s.addVersion(newVersion);
        }
        else {
          LOG.error("Cannot rename \"{}\" to \"{}\"", b2sFile, b2sNewFile);
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
      LOG.info("Copied \"{}\" to \"{}\"", fileName, target.getAbsolutePath());
      return reloadDirectB2SAndVersions(emulator, fileName);
    }
    catch (IOException e) {
      LOG.error("Failed to duplicate backglass {}: {}", b2sFile.getAbsolutePath(), e.getMessage(), e);
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
          cacheRemove(emulatorId, b2sFile);
          b2sFile = tempFile;
        }
        else {
          LOG.error("Cannot rename " + mainFile + " to {}, operation ignored", b2sFile.getAbsolutePath());
        }
      }
      if (b2sFile.renameTo(mainFile)) {
        cacheRemove(emulatorId, b2sFile);
      }
      else {
        throw new RuntimeException("Cannot rename " + b2sFile + ", operation ignored");
      }
    }

    // a main backglass of the other type (.directb2s vs .B2SPro) would remain active, so make it a version
    File mainFile = new File(emulator.getGamesDirectory(), mainFileName);
    for (File sibling : BackglassNamingHelper.getSiblingBackglassFiles(mainFile)) {
      disableMainFile(emulatorId, sibling);
    }
    return reloadDirectB2SAndVersions(emulator, mainFileName);
  }

  public DirectB2S disable(int emulatorId, String fileName) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    String mainFileName = FileUtils.fromUniqueFile(fileName);
    File mainFile = new File(emulator.getGamesDirectory(), mainFileName);
    // disable the main files of all backglass types
    List<File> mainFiles = new ArrayList<>(BackglassNamingHelper.getSiblingBackglassFiles(mainFile));
    mainFiles.add(mainFile);
    for (File file : mainFiles) {
      disableMainFile(emulatorId, file);
    }
    return reloadDirectB2SAndVersions(emulator, fileName);
  }

  private void disableMainFile(int emulatorId, File mainFile) {
    if (mainFile.exists()) {
      File target = FileUtils.uniqueFile(mainFile);
      if (!mainFile.renameTo(target)) {
        throw new RuntimeException("Cannot rename " + mainFile + " to " + target);
      }
      cacheRemove(emulatorId, mainFile);
    }
  }

  public boolean deleteBackglass(int emulatorId, String filename) {
    DirectB2S b2s = removeDirectB2SAndVersions(emulatorId, filename);
    if (b2s != null) {
      GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
      for (String version : b2s.getVersions()) {
        File b2sFile = new File(emulator.getBackglassDirectory(), version);
        if (b2sFile.exists() && b2sFile.delete()) {
          cacheRemove(emulatorId, b2sFile);
          LOG.info("Deleted {}", b2sFile.getAbsolutePath());
          return true;
        }
        else {
          LOG.info("Cannot delete {}", b2sFile.getAbsolutePath());
        }
      }
    }
    return false;
  }

  public DirectB2S deleteVersion(int emulatorId, String filename) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    File b2sFile = new File(emulator.getGamesDirectory(), filename);
    if (b2sFile.exists() && b2sFile.delete()) {
      cacheRemove(emulatorId, b2sFile);
      LOG.info("Deleted {}", b2sFile.getAbsolutePath());
    }
    return reloadDirectB2SAndVersions(emulator, filename);
  }

//-------------------------------------------- SCREENRES ---

  public DirectB2sScreenRes getScreenRes(Game game, boolean perTableOnly) {
    if (game != null) {
      File b2sFile = game.getDirectB2SFile();
      String b2sFilename = BackglassNamingHelper.getBackglassFileName(game);
      DirectB2sScreenRes res = getScreenRes(b2sFile, perTableOnly);
      if (res != null) {
        res.setEmulatorId(game.getEmulatorId());
        res.setB2SFileName(b2sFilename);
        res.setGameId(game.getId());
      }
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

  public @Nullable DirectB2sScreenRes getGlobalScreenRes() {
    return getScreenRes((File) null, false);
  }

  private @Nullable DirectB2sScreenRes getScreenRes(@Nullable File b2sFile, boolean perTableOnly) {
    List<String> lines = readScreenRes(b2sFile, false, perTableOnly);
    if (lines == null) {
      return null;
    }
    DirectB2sScreenRes res = new DirectB2sScreenRes();
    res.setScreenresFilePath(lines.removeFirst());
    res.setGlobal(b2sFile == null || Strings.CI.contains(res.getScreenresFilePath(), b2sFile.getName()));

    res.setVersion(lines.removeFirst());

    // cf https://github.com/vpinball/b2s-backglass/blob/7842b3638b62741e21ebb511e2a886fa2091a40f/b2s_screenresidentifier/b2s_screenresidentifier/module.vb#L105
    res.setPlayfieldWidth(parseIntSafe(lines.getFirst()));
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

    if (lines.size() >= 12) {
      res.setDmYFlip("1".equals(lines.get(11)));
    }
    else {
      res.setDmYFlip(false);
    }

    if (lines.size() > 16) {
      res.setBackgroundX(parseIntSafe(lines.get(12)));
      res.setBackgroundY(parseIntSafe(lines.get(13)));

      res.setBackgroundWidth(parseIntSafe(lines.get(14)));
      res.setBackgroundHeight(parseIntSafe(lines.get(15)));

      File file = new File(lines.get(16));
      if (file.exists()) {
        res.setBackgroundFilePath(lines.get(16));
      }
      else {
        LOG.warn("Frame file referenced in .res does not exist, ignoring: {}", lines.get(16));
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
  private @Nullable List<String> readScreenRes(@Nullable File b2sFile, boolean withComment, boolean perTableOnly) {
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
    if (target == null || !target.exists()) {
      // do not load global screenres if perTableOnly is requested !
      if (perTableOnly) {
        return null;
      }
      //  4) Screenres.txt ( or whatever you set in the registry) in the folder where the B2SBackglassServerEXE.exe is located
      target = new File(getBackglassServerFolder(), screenresTxt);

      // fall back to tables folder of the first emulator
      if (!target.exists()) {
        LOG.info("Failed to read Screenres.txt via standard patterns, trying first VPX emulator instead.");
        List<GameEmulator> vpxGameEmulators = emulatorService.getVpxGameEmulators();
        if (!vpxGameEmulators.isEmpty()) {
          GameEmulator emulator = vpxGameEmulators.getFirst();
          target = new File(emulator.getGamesDirectory(), "ScreenRes.txt");
        }

        if (!target.exists()) {
          LOG.error("Unable to read {}, please check the following :", screenresTxt);
          LOG.error("- your backglass server folder is registered: {}", getBackglassServerFolder());
          LOG.error("- A table-dedicated <tablename.res> or {} file is present in the same folder as your table", screenresTxt);
          LOG.error("- a {} file is present in the tables folder or in the backglass server folder", screenresTxt);
          return null;
        }
      }
    }

    LOG.info("Reading screen res file: {}", target.getAbsolutePath());
    try (BufferedReader reader = new BufferedReader(new FileReader(target))) {
      String version = null;

      List<String> lines = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        // detection of version
        // see https://github.com/vpinball/b2s-backglass/blob/7adc7d10b026863529ac3399b6e7235134cb80d0/b2s_screenresidentifier/b2s_screenresidentifier/module.vb#L93
        if (line.replace(" ", "").startsWith("#V2")) {
          version = line;
          // remove the line from the file, as we will reinsert proper version on top
          continue;
        }
        if (withComment || !line.startsWith("#")) {
          lines.add(line.trim());
        }
      }
      if (version == null) {
        version = "# V1";
      }
      // insert additional information
      lines.addFirst(version);
      lines.addFirst(target.getAbsolutePath());
      return lines;
    }
    catch (Exception e) {
      LOG.error("Cannor open screen res file {}", target.getAbsolutePath(), e);
      return null;
    }
  }


  public byte[] generateFrame(int emulatorId, String filename, @Nullable Game game, DirectB2SFrameType frameType) {
    File b2sFile = getB2sFile(emulatorId, filename);
    if (b2sFile != null && b2sFile.exists()) {
      try {
        DirectB2SData tableData = getDirectB2SData(b2sFile, emulatorId, filename);
        BufferedImage image = getPreviewBackgroundImage(tableData, game, false);

        // The background image should fit the full screen
        DirectB2sScreenRes screenres = getScreenRes(b2sFile, false);
        if (screenres != null) {
          int targetW = screenres.getFullBackglassWidth();
          int targetH = screenres.getFullBackglassHeight();
          return DirectB2SFrameTypeGenerator.generateAsByte(image, targetW, targetH, frameType, false);
        }
        else {
          LOG.error("Cannot read screenres and determine image dimensions for {} and emulator {}", filename, emulatorId);
        }
      }
      catch (IOException ioe) {
        LOG.error("Error in frame generation for {} and emulator {}", filename, emulatorId, ioe);
      }
    }
    return null;
  }

  protected void generateAndSaveFrame(DirectB2sScreenRes screenres, @Nullable Game game) {
    if (screenres.isBackglassCentered() && !DirectB2SFrameType.USE_FRAME.equals(screenres.getFrameType())) {
      byte[] frame = generateFrame(screenres.getEmulatorId(), screenres.getB2SFileName(), game, screenres.getFrameType());
      File b2sFile = getB2sFile(screenres.getEmulatorId(), screenres.getB2SFileName());
      if (frame != null && b2sFile != null && b2sFile.exists()) {
        File frameFolder = new File(b2sFile.getParent(), "_Frames");
        if (frameFolder.exists() || frameFolder.mkdir()) {
          String name = FilenameUtils.getBaseName(screenres.getB2SFileName());
          File frameFile = new File(frameFolder, name + " " + screenres.getFrameType() + ".png");
          try {
            Files.deleteIfExists(frameFile.toPath());
            ByteArrayInputStream is = new ByteArrayInputStream(frame);
            try (FileOutputStream out = new FileOutputStream(frameFile)) {
              StreamUtils.copy(is, out);
              screenres.setBackgroundFilePath(frameFile.getAbsolutePath());
            }
          }
          catch (IOException ioe) {
            LOG.error("Error saving frame", ioe);
          }
        }
      }
    }
    else {
      LOG.warn("Cannot create generate or save frame");
    }
  }


  public void saveScreenRes(DirectB2sScreenRes screenres, @Nullable Game game) throws Exception {
    // possibly generate frame and modify screenres
    generateAndSaveFrame(screenres, game);

    // then save screenres
    GameEmulator emulator = emulatorService.getGameEmulator(screenres.getEmulatorId());
    File b2sFile = new File(emulator.getGamesDirectory(), screenres.getB2SFileName());

    List<String> lines = readScreenRes(b2sFile, true, false);
    if (lines == null) {
      throw new IOException("Cannot find an existing table nor table .res");
    }
    String templateName = lines.removeFirst();

    // if already a table file exists, replace it
    File screenresFile = Strings.CI.contains(templateName, FilenameUtils.getBaseName(screenres.getB2SFileName())) ?
        new File(templateName) : new File(emulator.getGamesDirectory(), FilenameUtils.removeExtension(screenres.getB2SFileName()) + ".res");

    if (!screenresFile.exists() || screenresFile.delete()) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(screenresFile))) {

        // check version number in case it is not in the original file
        String version = lines.removeFirst();
        if (!version.replace(" ", "").startsWith("#V2")) {
          // fetch the b2s version from the file itself
          File b2sServerExe = new File(getBackglassServerFolder(), "B2SBackglassServerEXE.exe");
          String b2sVersion = FileVersion.fetch(b2sServerExe);
          version = "# V" + b2sVersion;
        }
        writer.write(version);
        writer.write(System.lineSeparator());

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
          saveTableSettings(game, settings);
        }
      }
    }
  }

  /**
   * Converts the given screen of all table-dedicated .res files of the emulator to a new resolution.
   * <ul>
   *   <li>PLAYFIELD: the playfield size is set to the new resolution.</li>
   *   <li>BACKGLASS: the backglass (or the framed background, if a frame is used) is scaled from its current
   *   resolution to the new one, including the DMD, which is positioned relative to the backglass.</li>
   *   <li>DMD: the DMD size and position are scaled from the current DMD size to the new one.</li>
   * </ul>
   * Files that already match the target resolution are left untouched, modified files are backed up as .res.bak.
   *
   * @param dryRun if true, nothing is written and only the affected files are counted
   * @return the number of affected .res files
   */
  public int bulkResizeScreenRes(int emulatorId, ScreenResTarget target, int newWidth, int newHeight, boolean dryRun) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    if (emulator == null || target == null || newWidth <= 0 || newHeight <= 0) {
      return 0;
    }

    int affected = 0;
    File gamesDirectory = new File(emulator.getGamesDirectory());
    Collection<File> resFiles = org.apache.commons.io.FileUtils.listFiles(gamesDirectory, new String[]{"res"}, true);
    for (File resFile : resFiles) {
      if (BackglassNamingHelper.findExistingBackglassFileName(resFile.getParentFile(), FilenameUtils.getBaseName(resFile.getName())) == null) {
        continue;
      }

      try {
        List<String> lines = Files.readAllLines(resFile.toPath());
        // indexes of the non-comment lines, they hold the values
        List<Integer> valueLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
          if (!lines.get(i).trim().startsWith("#")) {
            valueLines.add(i);
          }
        }
        if (valueLines.size() < 11) {
          continue;
        }

        int currentWidth;
        int currentHeight;
        switch (target) {
          case PLAYFIELD:
            currentWidth = parseIntSafe(lines.get(valueLines.get(0)).trim());
            currentHeight = parseIntSafe(lines.get(valueLines.get(1)).trim());
            break;
          case DMD:
            currentWidth = parseIntSafe(lines.get(valueLines.get(7)).trim());
            currentHeight = parseIntSafe(lines.get(valueLines.get(8)).trim());
            break;
          default:
            currentWidth = parseIntSafe(lines.get(valueLines.get(2)).trim());
            currentHeight = parseIntSafe(lines.get(valueLines.get(3)).trim());
            if (valueLines.size() > 16) {
              int bgWidth = parseIntSafe(lines.get(valueLines.get(14)).trim());
              int bgHeight = parseIntSafe(lines.get(valueLines.get(15)).trim());
              if (bgWidth > 0 && bgHeight > 0) {
                currentWidth = bgWidth;
                currentHeight = bgHeight;
              }
            }
        }

        if (currentWidth == newWidth && currentHeight == newHeight) {
          continue;
        }
        // the playfield is set, the other screens are scaled and need a valid current size
        if (!ScreenResTarget.PLAYFIELD.equals(target) && (currentWidth <= 0 || currentHeight <= 0)) {
          continue;
        }

        affected++;
        if (dryRun) {
          continue;
        }

        if (ScreenResTarget.PLAYFIELD.equals(target)) {
          lines.set(valueLines.get(0), Integer.toString(newWidth));
          lines.set(valueLines.get(1), Integer.toString(newHeight));
        }
        else {
          double scaleX = (double) newWidth / currentWidth;
          double scaleY = (double) newHeight / currentHeight;

          // value index -> true for x/width values, false for y/height values
          Map<Integer, Boolean> scaled = new LinkedHashMap<>();
          if (ScreenResTarget.BACKGLASS.equals(target)) {
            scaled.put(2, true);
            scaled.put(3, false);
            scaled.put(5, true);
            scaled.put(6, false);
            scaled.put(12, true);
            scaled.put(13, false);
            scaled.put(14, true);
            scaled.put(15, false);
          }
          // the DMD is positioned relative to the backglass and scales with it
          scaled.put(7, true);
          scaled.put(8, false);
          scaled.put(9, true);
          scaled.put(10, false);

          for (Map.Entry<Integer, Boolean> entry : scaled.entrySet()) {
            if (entry.getKey() >= valueLines.size()) {
              continue;
            }
            int lineIndex = valueLines.get(entry.getKey());
            String raw = lines.get(lineIndex).trim();
            if (raw.isEmpty()) {
              continue;
            }
            long value = Math.round(parseIntSafe(raw) * (entry.getValue() ? scaleX : scaleY));
            lines.set(lineIndex, Long.toString(value));
          }
        }

        Files.copy(resFile.toPath(), new File(resFile.getParentFile(), resFile.getName() + ".bak").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.write(resFile.toPath(), lines);
        LOG.info("Converted {} of {} from {}x{} to {}x{}", target, resFile.getAbsolutePath(), currentWidth, currentHeight, newWidth, newHeight);
      }
      catch (Exception e) {
        LOG.error("Failed to scale screen res file {}: {}", resFile.getAbsolutePath(), e.getMessage(), e);
      }
    }
    return affected;
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
      LOG.error("Cannot create _frames Folder {}", emulator.getGamesDirectory());
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
        String base64 = Base64.getEncoder().encodeToString(img);
        setDmdImage(game.getEmulatorId(), BackglassNamingHelper.getBackglassFileName(game), item.getFile().getName(), base64);
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
          LOG.error("Failed to copy resource file as background: {}", e.getMessage(), e);
        }
      }
      else if ("video".equals(baseType)) {
        return VidUtil.grab(item.getFile());
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
    try {
      BufferedImage preview = getPreviewBackgroundImage(tableData, game, includeFrame);
      if (preview != null) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(preview, "png", baos);
        return baos.toByteArray();
      }
    }
    catch (IOException ioe) {
      LOG.error("Cannot generate preview image for {]}", tableData.getFilename(), ioe);
    }
    return null;
  }

  private BufferedImage getPreviewBackgroundImage(DirectB2SData tableData, @Nullable Game game, boolean includeFrame) throws IOException {
    if (tableData != null && tableData.isBackgroundAvailable()) {
      String base64 = getBackgroundBase64(tableData.getEmulatorId(), tableData.getFilename());
      if (base64 != null) {
        byte[] bytes = Base64.getMimeDecoder().decode(sanitizeBase64(base64));
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
                LOG.info("Rendering frame for {}: {}", tableData.getFilename(), frameFile.getAbsolutePath());
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
              else {
                LOG.warn("Frame file does not exist during rendering, skipping for {}: {}", tableData.getFilename(), frameFile.getAbsolutePath());
              }
            }
          }
          return preview;
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
    if (tableData != null && tableData.isDmdImageAvailable()) {
      String base64 = getDmdBase64(tableData.getEmulatorId(), tableData.getFilename());
      if (base64 != null) {
        return Base64.getMimeDecoder().decode(sanitizeBase64(base64));
      }
    }
    // not found or error
    return null;
  }

  private void cacheAdd(int emulatorId, File b2sFile, DirectB2SData data) {
    cacheDirectB2SData.put(emulatorId + "|" + b2sFile.getPath(), data);
  }

  private void cacheRemove(int emulatorId, File b2sFile) {
    cacheDirectB2SData.remove(emulatorId + "|" + b2sFile.getPath());
  }

  private String[] imagesCacheAdd(File b2sFile, DirectB2SDataExtractor extractor) {
    String[] images = new String[]{extractor.getBackgroundBase64(), extractor.getDmdBase64()};
    cacheImagesBase64.put(imagesCacheKey(b2sFile), images);
    return images;
  }

  /**
   * size and date are part of the key, so any rewrite of the file, by studio or not, invalidates the entry
   */
  private String imagesCacheKey(File b2sFile) {
    return b2sFile.getAbsolutePath() + "|" + b2sFile.length() + "|" + b2sFile.lastModified();
  }


  private void tableSettingsCacheAdd(DirectB2STableSettings settings) {
    if (StringUtils.isNotEmpty(settings.getRom())) {
      cacheB2STableSettings.put(StringUtils.deleteWhitespace(settings.getRom()), settings);
    }
  }

  private DirectB2STableSettings tableSettingsCacheGet(String rom, String tableName) {
    DirectB2STableSettings entry = StringUtils.isNotEmpty(rom) ? cacheB2STableSettings.get(rom) : null;
    if (entry == null && StringUtils.isNotEmpty(tableName)) {
      entry = cacheB2STableSettings.get(tableName);
    }
    return entry;
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
    long start = System.currentTimeMillis();
    // force initialisation of table cache
    clearCache();
    LOG.info("{} initialization finished, took {}ms", this.getClass().getSimpleName(), (System.currentTimeMillis() - start));
  }
}
