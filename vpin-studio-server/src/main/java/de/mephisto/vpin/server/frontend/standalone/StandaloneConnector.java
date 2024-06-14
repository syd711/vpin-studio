package de.mephisto.vpin.server.frontend.standalone;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.system.SystemService;

@Service(FrontendType.STANDALONE)
public class StandaloneConnector extends BaseConnector {

  private static final Logger LOG = LoggerFactory.getLogger(StandaloneConnector.class);

  /** The default emulator id for VPX */
  private static final int VPX_EMUID = 1;

  @Autowired
  private SystemService systemService;
  
  @Override
  public void initializeConnector(ServerSettings settings) {
  }
  
  //------------------------------------------------------
  @Override
  protected List<Emulator> loadEmulators() {

    List<Emulator> emulators = new ArrayList<>();

    // so far only VPX is supported in standalone mode
    String emuName = "Visual Pinball";
    File vpxInstallDir = systemService.resolveVisualPinballInstallationFolder();
    File vpxTableDir = vpxInstallDir!=null? new File(vpxInstallDir, "tables"): null;
    if (vpxTableDir!=null && vpxTableDir.exists()) {
      LOG.info("Visual Pinball tables folder detected in " + vpxTableDir.getAbsolutePath());
      Emulator vpxemu = createEmulator(vpxInstallDir, vpxTableDir, VPX_EMUID, emuName); 
      vpxemu.setDescription("default");
      emulators.add(vpxemu);
    }
    return emulators;
  }

  private Emulator createEmulator(File installDir, File tablesDir, int emuId, String emuname) {
    Emulator e = new Emulator();
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    e.setDirGames(tablesDir.getAbsolutePath());

    if (StringUtils.equals(emuname, "Visual Pinball")) {
      e.setDirRoms(new File(installDir, "/VPinMAME/roms").getAbsolutePath());
    }
    
    //e.setDescription(rs.getString("Description"));
    e.setEmuLaunchDir(installDir.getAbsolutePath());

    String launchScript = null;
    e.setLaunchScript(launchScript);

    String gameext = getEmulatorExtension(emuname);
    e.setGamesExt(gameext);

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
  protected TableDetails getGameFromDb(String filename) {
    return null;
  }

  @Override
  protected void updateGameInDb(String filename, TableDetails details) {
    // do nothing
  }

  @Override
  protected void dropGameFromDb(String filename) {
    // do nothing
  }

  @Override
  protected void commitDb() {
    // do nothing
  }

  //------------------------------------------------------------
  @Override
  public MediaAccessStrategy getMediaAccessStrategy() {
    // no associated medias
    return null;
  }

}
