package de.mephisto.vpin.server.frontend.standalone;

import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
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
  @Override
  public void initializeConnector() {
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
    frontend.setAssetSearchEnabled(false);
    return frontend;
  }

  //------------------------------------------------------
  @Override
  protected List<Emulator> loadEmulators() {
    List<Emulator> emulators = new ArrayList<>();

    // so far only VPX is supported in standalone mode
    File vpxInstallDir = getInstallationFolder();
    File vpxTableDir = new File(vpxInstallDir, "Tables");
    if (vpxTableDir.exists()) {
      LOG.info("VPX tables folder detected in " + vpxTableDir.getAbsolutePath());
      String emuName = vpxInstallDir.getName();
      Emulator vpxemu = createEmulator(vpxInstallDir, vpxTableDir, VPX_EMUID, emuName);
      vpxemu.setDescription("default");
      emulators.add(vpxemu);
    }
    else {
      LOG.error("No VPX installation found in folder \"" + vpxInstallDir.getAbsolutePath() + "\"");
    }
    return emulators;
  }

  private Emulator createEmulator(File installDir, File tablesDir, int emuId, String emuname) {
    EmulatorType type = EmulatorType.VisualPinball;
    Emulator e = new Emulator(type);
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    e.setDirGames(tablesDir.getAbsolutePath());

    File exe = getVPXExe();
    e.setEmuLaunchDir(exe.getParentFile().getAbsolutePath());
    e.setExeName(exe.getName());

    e.setGamesExt(type.getExtension());

    e.setVisible(true);

    return e;
  }

  /**
   * Initial loading of Games from filesystem discovery
   */
  @Override
  protected List<String> loadGames(Emulator emu) {
    List<String> filenames = new ArrayList<>();
    File vpxTableDir = new File(emu.getDirGames());
    Path pTables = Path.of(emu.getDirGames());
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
  protected void commitDb(Emulator emu) {
    // do nothing
  }

  //------------------------------------------------------------
  @Override
  public MediaAccessStrategy getMediaAccessStrategy() {
    // no associated medias
    return null;
  }

  @Override
  public TableAssetsAdapter getTableAssetAdapter() {
    return null;
  }

  //----------------------------------

  @Override
  protected String getFrontendExe() {
    return getVPXExe().getName();
  }
}
