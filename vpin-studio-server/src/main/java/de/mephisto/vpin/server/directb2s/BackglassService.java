package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.commons.utils.FileVersion;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
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
import org.springframework.util.StreamUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
public class BackglassService {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private DefaultPictureService defaultPictureService;

  /** 
   * The default filename ScreenRes.txt can be altered by setting the registry key
   * Software\B2S\B2SScreenResFileNameOverride to a different filename.
   * Read once
   */
  private String screenresTxt;

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
    Game game = frontendService.getGameByBaseFilename(directB2S.getEmulatorId(), 
        FilenameUtils.getBaseName(directB2S.getName()));
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
    }
    catch (IOException ioe) {
      LOG.error("cannot extract background dimension", ioe);
    }
    try {
      exportDMDData(data, game, forceBackglassExtraction);
    }
    catch (IOException ioe) {
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

        // also clean then re-extract the default picture
        Game game = frontendService.getGameByBaseFilename(emuId, FilenameUtils.getBaseName(filename));
        if (game != null) {
          defaultPictureService.deleteDefaultPictures(game);
          defaultPictureService.extractDefaultPicture(game);
        } 

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
  public DirectB2STableSettings getTableSettings(int gameId) {
    Game game = gameService.getGame(gameId);
    return game != null ? getTableSettings(game) : null;
  }

  @Nullable
  public DirectB2STableSettings getTableSettings(Game game) {
    String rom = game.getRom();

    File settingsXml = getB2STableSettingsXml();
    B2STableSettingsParser tableSettingsParser = cacheB2STableSettingsParser.get(settingsXml.getPath());
    if (tableSettingsParser == null) {
      if (settingsXml.exists() && !StringUtils.isEmpty(rom)) {
        tableSettingsParser = new B2STableSettingsParser(settingsXml);
        cacheB2STableSettingsParser.put(settingsXml.getPath(), tableSettingsParser);
      }
    }

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

    File b2sFolder = getBackglassServerFolder();
    if (!b2sFolder.exists()) {
      return null;
    }
    File settingsXml = getB2STableSettingsXml();
    B2SServerSettingsParser serverSettingsParser = new B2SServerSettingsParser(getBackglassServerFolder(), settingsXml);
    return serverSettingsParser.getSettings();
  }

  public DirectB2ServerSettings saveServerSettings(DirectB2ServerSettings settings) {
    File settingsXml = getB2STableSettingsXml();
    B2SServerSettingsSerializer serverSettingsSerializer = new B2SServerSettingsSerializer(settingsXml);
    serverSettingsSerializer.serialize(settings);
    return getServerSettings();
  }

  @NonNull
  public File getBackglassServerFolder() {
    File b2sFolder = systemService.getBackglassServerFolder();
    if (b2sFolder != null) {
      return b2sFolder;
    }
    // else search in VPX emulators folders
    for (GameEmulator emu : frontendService.getVpxGameEmulators()) {
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
    return new File("C:/vPinball/B2SServer");
  }

  @NonNull
  public File getB2STableSettingsXml() {
    File xml = new File(getBackglassServerFolder(), "B2STableSettings.xml");
    if (xml.exists()) {
      return xml;
    }

    // not found, check the legacy default : in tables folder of Vpx emulators
    for (GameEmulator emu : frontendService.getVpxGameEmulators()) {
      File tableXml = new File(emu.getTablesDirectory(), "B2STableSettings.xml");
      if (tableXml.exists()) {
        return tableXml;
      }
    }

    // else create a blank one
    try {
      Files.writeString(xml.toPath(), "<B2STableSettings></B2STableSettings>");
      return xml;
    }
    catch (IOException e) {
      LOG.error("Cannot find nor create file " + xml.getAbsolutePath(), e);
      throw new RuntimeException("Cannot find nor create file " + xml.getAbsolutePath() +", " + e.getMessage());
    }
  }

  //--------------------------

  public List<DirectB2S> getBackglasses() {
    List<DirectB2S> result = new ArrayList<>();
    List<GameEmulator> gameEmulators = frontendService.getBackglassGameEmulators();
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

  //------------------------------------

  public DirectB2sScreenRes getScreenRes(DirectB2S directb2s, boolean perTableOnly) {
    GameEmulator emulator = frontendService.getGameEmulator(directb2s.getEmulatorId());
    File b2sFile = new File(emulator.getTablesDirectory(), directb2s.getFileName());

    List<String> lines = readScreenRes(b2sFile, false, perTableOnly);
    if (lines == null) {
      return null;
    }
    DirectB2sScreenRes res = new DirectB2sScreenRes();
    res.setEmulatorId(directb2s.getEmulatorId());
    res.setFileName(directb2s.getFileName());
    res.setScreenresFilePath(lines.remove(0));
    res.setGlobal(StringUtils.containsIgnoreCase(res.getScreenresFilePath(), FilenameUtils.getBaseName(directb2s.getFileName())));

    // cf https://github.com/vpinball/b2s-backglass/blob/7842b3638b62741e21ebb511e2a886fa2091a40f/b2s_screenresidentifier/b2s_screenresidentifier/module.vb#L105
    res.setPlayfieldWidth(Integer.parseInt(lines.get(0)));
    res.setPlayfieldHeight(Integer.parseInt(lines.get(1)));

    res.setBackglassWidth(Integer.parseInt(lines.get(2)));
    res.setBackglassHeight(Integer.parseInt(lines.get(3)));

    res.setBackglassDisplay(lines.get(4));

    res.setBackglassX(Integer.parseInt(lines.get(5)));
    res.setBackglassY(Integer.parseInt(lines.get(6)));

    res.setDmdWidth(Integer.parseInt(lines.get(7)));
    res.setDmdHeight(Integer.parseInt(lines.get(8)));

    res.setDmdX(Integer.parseInt(lines.get(9)));
    res.setDmdY(Integer.parseInt(lines.get(10)));

    res.setDmYFlip("1".equals(lines.get(11)));

    if (lines.size() > 16) {
      res.setBackgroundX(Integer.parseInt(lines.get(12)));
      res.setBackgroundY(Integer.parseInt(lines.get(13)));

      res.setBackgroundWidth(Integer.parseInt(lines.get(14)));
      res.setBackgroundHeight(Integer.parseInt(lines.get(15)));
  
      res.setBackgroundFilePath(lines.get(16)); 

      if (lines.size() > 17) {
        res.setB2SWindowPunch(lines.get(17));
      }  
    }

    // Now add the associated game if any
    Game game = frontendService.getGameByBaseFilename(directb2s.getEmulatorId(), 
        FilenameUtils.getBaseName(directb2s.getFileName()));
    if (game != null) {
      //this will ensure that a scanned table is fetched and get the rom
      game = gameService.getGame(game.getId());
    }
    if (game != null) {
      res.setGameId(game.getId());
    }
    return res;
  }

  public File getScreenResFile(int emuId, String filename) {
    GameEmulator emulator = frontendService.getGameEmulator(emuId);
    File b2sFile = new File(emulator.getTablesDirectory(), filename);
    List<String> lines = readScreenRes(b2sFile, false, false);
    if (lines == null || lines.size() < 16) {
      return null;
    }
    lines.remove(0);
    File framePath = new File(lines.get(16));
    return framePath.exists() ? framePath : null;
  }

  /**
   * Load a screen.res file and returns the lines
   * @param b2sFile The associated directb2s file to get table filename
   * @param withComment Whether comment lines must be returned (true) or filtered (false)
   * @param perTableOnly Load only the file if it is table dedicated one, else null
   * @return the List of all lines in the file 
   */
  private List<String> readScreenRes(File b2sFile, boolean withComment, boolean perTableOnly) {
    if (screenresTxt == null) {
      // The default filename ScreenRes.txt can be altered by setting the registry key
      // Software\B2S\B2SScreenResFileNameOverride to a different filename.
      screenresTxt = StringUtils.defaultIfEmpty(
        systemService.readRegistryValue("HKEY_CURRENT_USER\\Software\\B2S", "B2SScreenResFileNameOverride"),
        "ScreenRes.txt");
    }

    // see https://github.com/vpinball/b2s-backglass/wiki/Screenres.txt
    // When the B2S Server starts, it tries to find the ScreenRes files in this order  (from backglassServer documentation):
    //  1) tablename.res next to the tablename.vpx
    File target = new File(b2sFile.getParentFile(), FilenameUtils.getBaseName(b2sFile.getName()) + ".res");
    if (!target.exists()) {
      //  2) Screenres.txt (or whatever set in the registry) in the same folder as tablename.vpx
      target = new File(b2sFile.getParentFile(), screenresTxt);
      if (perTableOnly || !target.exists()) {
        //  3) Screenres.txt (or whatever set in the registry) as tablename/Screenres.txt
        File tableFolder = new File(b2sFile.getParentFile(), FilenameUtils.getBaseName(b2sFile.getName()));
        target = new File(tableFolder, screenresTxt);
        if (!perTableOnly && !target.exists()) {
          //  4) Screenres.txt ( or whatever you set in the registry) in the folder where the B2SBackglassServerEXE.exe is located
          target = new File(getBackglassServerFolder(), screenresTxt);
        }
      }
    }

    if (!target.exists()) {
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

  public void saveScreenRes(DirectB2sScreenRes screenres) throws Exception {
    GameEmulator emulator = frontendService.getGameEmulator(screenres.getEmulatorId());
    File b2sFile = new File(emulator.getTablesDirectory(), screenres.getFileName());

    List<String> lines = readScreenRes(b2sFile, true, false);
    if (lines == null) {
      throw new IOException("Cannot find an existing table nor table .res");
    }
    String templateName = lines.remove(0);
    // if already a table file exists, replace it 
    File screenresFile = StringUtils.containsIgnoreCase(templateName, FilenameUtils.getBaseName(screenres.getFileName())) ?
      new File(templateName) : new File(emulator.getTablesDirectory(), FilenameUtils.getBaseName(screenres.getFileName()) + ".res");

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
        for (String line: lines) {
          if (line.startsWith("#")) {
            writer.write(line);
          }
          else {
            switch (currentLine++) {
              case 2: writer.write(Integer.toString(screenres.getBackglassWidth())); break;
              case 3: writer.write(Integer.toString(screenres.getBackglassHeight())); break;
              case 5: writer.write(Integer.toString(screenres.getBackglassX())); break;
              case 6: writer.write(Integer.toString(screenres.getBackglassY())); break;
              case 12: writer.write(Integer.toString(screenres.getBackgroundX())); break;
              case 13: writer.write(Integer.toString(screenres.getBackgroundY())); break;
              case 14: writer.write(Integer.toString(screenres.getBackgroundWidth())); break;
              case 15: writer.write(Integer.toString(screenres.getBackgroundHeight())); break;
              case 16: writer.write(StringUtils.defaultString(screenres.getBackgroundFilePath())); break;
              default : writer.write(line);
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
    // case when Gaùe 

    // get the Game with gameService, this  will ensure that a scanned table is fetched and get the rom
    Game game = screenres.getGameId() != -1 ? gameService.getGame(screenres.getGameId()) : null;
    if (game != null) {
      DirectB2STableSettings settings = getTableSettings(game);
      if (settings == null && StringUtils.isNotEmpty(game.getRom())) {
        settings = new DirectB2STableSettings();
        settings.setRom(game.getRom());
      }
      // If no settings found and no rom on game to associate the settings with, skip that phase
      if (settings != null) {
        boolean hasToBeSaved = false;

        boolean startAsExe = settings.getStartAsEXE() != null && settings.getStartAsEXE();
        if (!startAsExe && screenres.isTurnOnRunAsExe()) {
          hasToBeSaved = true;
          settings.setStartAsEXE(true);
        }
        // mind here 0=visible
        if (settings.getStartBackground()!=0 && screenres.isTurnOnBackground()) {
          hasToBeSaved = true;
          settings.setStartBackground(0);
        }

        if (hasToBeSaved) {
          saveTableSettings(game.getId(), settings);
        }
      }
    }
  }

  public String setScreenResFrame(int emulatorId, String filename, String screenName, InputStream is) throws IOException {
    GameEmulator emulator = frontendService.getGameEmulator(emulatorId);
    File frameFolder = new File(emulator.getTablesDirectory(), "_Frames");
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
      LOG.error("Cannot create _frames Folder " + emulator.getTablesDirectory());
      return null;
    }
  }
}
