package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.system.SystemService;

@Service
@Qualifier("Standalone")
public class StandaloneConnector extends BaseConnector {

  private final static Logger LOG = LoggerFactory.getLogger(StandaloneConnector.class);

  @Autowired
  private SystemService systemService;


  @Override
  public void initialize(ServerSettings settings) {
  }
    
  @Override
  public List<Emulator> getEmulators() {
    
    this.games = new ArrayList<>();
    this.tabledetails = new HashMap<>();

    List<Emulator> emulators = new ArrayList<>();
    int emuId = 1;

    File vpxInstallDir = systemService.resolveVisualPinballInstallationFolder();
    if (vpxInstallDir==null) {
      // not found so default to standard Visual Piinball install directory
      vpxInstallDir = new File("c:/Visual Pinball");
    }
    String emuName = "Visual Pinball";
    File vpxTableDir = new File(vpxInstallDir, "tables");

    Emulator vpxemu = createEmulator(vpxInstallDir, vpxTableDir, emuId, emuName); 
    vpxemu.setDescription("default");
    vpxemu.setDirB2S(new File(vpxInstallDir, "B2SServer").getAbsolutePath());

    emulators.add(vpxemu);

    int nbAdded = 0; //addGames(vpxTableDir, games, tabledetails, vpxemu);
    LOG.info("Parsed games for emulator "+emuId + ", "+ emuName + ": " + nbAdded + " games");
    emuId++;

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



  @Override
  public MediaAccessStrategy getMediaAccessStrategy() {
    return null;
  }

}
