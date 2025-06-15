package de.mephisto.vpin.server.frontend.standalone;

import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * USes a table per structure folder :
 *
   - Table folder
    - <table>.vpx
    - <table>.vbs (if necessary)
    - <table>.ini (if desired)
    - <table>.directb2s (if desired)
    - music (if table has music)
       - *
    - pupvideos (if table has pup)
       - *
    - pinmame
       - roms
          - <rom>.zip
       - altcolor
          - <rom>
             - <rom>.cRZ (if you want)
       - altsound
          - <rom>
             - * (if you want)
       - nvram
          - <rom>.nv (if you want)
       - ini
          - <rom>.ini (if you want)

    also suport
     https://github.com/superhac/vpinfe
     https://github.com/superhac/vpinmediadb
 */
@Service("Standalone")
public class StandaloneConnector extends BaseConnector {
  public final static String STANDALONE = FrontendType.Standalone.name();

  private static final Logger LOG = LoggerFactory.getLogger(StandaloneConnector.class);

  /**
   * The default emulator id for VPX
   */
  private static final int VPX_EMUID = 1;

  private static final String VPX_EMUNAME = "Visual Pinball";

  @Autowired
  private SystemService systemService;

  /** The resolved execution file, cannot be null but can be dummy ! */
  private File vpxExe;

  @Override
  public void initializeConnector() {
    vpxExe = resolveExe();
  }

  private File resolveExe() {
    File installFolder = getInstallationFolder();
    String[] exes = {"VPinballX64.exe", "VPinballX.exe", "VPinball995.exe" };
    for (String exe : exes) {
      File fexe = new File(installFolder, exe);
      if (fexe.exists()) {
        return fexe;
      }
    }
    LOG.error("Cannot find a valid standard executable in " + installFolder + ", tables cannot be properly launched !");
    // cannot be null
    return new File(installFolder, "vpx_not_found.exe");
  }

  @Override
  public List<FrontendPlayerDisplay> getFrontendPlayerDisplays() {
    return List.of();
  }

  @NonNull
  @Override
  public File getInstallationFolder() {
    return systemService.getStandaloneInstallationFolder();
  }

  @NonNull
  public File getTablesFolder() {
    File tablesFolder = systemService.getStandaloneTablesFolder();
    if (tablesFolder == null) {
      tablesFolder = new File(getInstallationFolder(), "Tables");
    }
    return tablesFolder;
  }

  public Frontend getFrontend() {
    Frontend frontend = new Frontend();
    //frontend.setInstallationDirectory(getInstallationFolder().getAbsolutePath());
    frontend.setFrontendType(FrontendType.Standalone);
    frontend.setName(VPX_EMUNAME);

    frontend.setIgnoredValidations(Arrays.asList(
        GameValidationCode.CODE_NO_AUDIO,
        GameValidationCode.CODE_NO_AUDIO_LAUNCH,
        GameValidationCode.CODE_NO_APRON,
        GameValidationCode.CODE_NO_INFO,
        GameValidationCode.CODE_NO_HELP,
        GameValidationCode.CODE_NO_TOPPER,
        GameValidationCode.CODE_NO_BACKGLASS,
        GameValidationCode.CODE_NO_DMD,
        GameValidationCode.CODE_NO_PLAYFIELD,
        GameValidationCode.CODE_NO_LOADING,
        GameValidationCode.CODE_NO_OTHER2,
        GameValidationCode.CODE_NO_WHEEL_IMAGE,
        GameValidationCode.CODE_PUP_PACK_FILE_MISSING
    ));

    return frontend;
  }

  //------------------------------------------------------
  @Override
  protected List<GameEmulator> loadEmulators() {
    List<GameEmulator> emulators = new ArrayList<>();
    // so far only VPX is supported in standalone mode
    File vpxInstallDir = getInstallationFolder();
    File vpxTableDir = getTablesFolder();
    if (vpxInstallDir != null && vpxInstallDir.exists() && vpxTableDir != null && vpxTableDir.exists()) {
      LOG.info("VPX tables folder detected in " + vpxTableDir.getAbsolutePath());
      String emuName = vpxInstallDir.getName();
      GameEmulator vpxemu = createEmulator(vpxInstallDir, vpxTableDir, VPX_EMUID, emuName);
      vpxemu.setDescription("default");
      emulators.add(vpxemu);
    }
    else {
      LOG.error("No VPX installation found in folder \"" + vpxInstallDir + "\"");
    }
    return emulators;
  }

  private GameEmulator createEmulator(File installDir, File tablesDir, int emuId, String emuname) {
    EmulatorType type = EmulatorType.VisualPinball;
    GameEmulator e = new GameEmulator();
    e.setType(type);
    e.setId(emuId);
    e.setSafeName(emuname);
    e.setName(emuname);
    e.setGamesDirectory(tablesDir.getAbsolutePath());
    e.setInstallationDirectory(vpxExe.getParentFile().getAbsolutePath());
    e.setExeName(vpxExe.getName());
    e.setGameExt(type.getExtension());
    e.setEnabled(true);
    return e;
  }

  /**
   * Initial loading of Games from filesystem discovery
   */
  @Override
  protected List<String> loadGames(GameEmulator emu) {
    List<String> filenames = new ArrayList<>();
    File vpxTableDir = new File(emu.getGamesDirectory());
    Path pTables = Path.of(emu.getGamesDirectory());
    if (vpxTableDir.exists()) {
      Iterator<File> tablesIterator = FileUtils.iterateFiles(vpxTableDir, new String[]{"vpx"}, true);
      while (tablesIterator.hasNext()) {
        File tableFile = tablesIterator.next();

        Path pfile = tableFile.toPath();
        Path pRelative = pTables.relativize(pfile);
        String gameFileName = pRelative.toString();
        filenames.add(gameFileName);
      }
    }
    return filenames;
  }

  //------------------------------------------------------------

  @Override
  protected TableDetails getGameFromDb(int emuId, String filename) {
    TableDetails details = new TableDetails();
    details.setEmulatorId(emuId);
    details.setStatus(1);
    details.setGameFileName(filename);
    String basename = FilenameUtils.getBaseName(filename);
    details.setGameDisplayName(basename);
    details.setGameName(basename);
    return details;
  }

  @Override
  protected void updateGameInDb(int emuId, String filename, TableDetails details) {
    // do nothing
  }

  @Override
  protected void dropGameFromDb(int emuId, String filename) {
    // do nothing
  }

  @Override
  protected void commitDb(GameEmulator emu) {
    // do nothing
  }

  @Override
  public boolean deleteEmulator(int emulatorId) {
    return false;
  }

  @Override
  public GameEmulator saveEmulator(GameEmulator emulator) {
    return null;
  }

  //----------------------------------

  @Override
  protected String getFrontendExe() {
    return vpxExe.getName();
  }
}
