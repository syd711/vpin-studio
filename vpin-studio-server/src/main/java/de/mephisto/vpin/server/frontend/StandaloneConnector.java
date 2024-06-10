package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;

@Service
@Qualifier("Standalone")
public class StandaloneConnector extends BaseConnector {

  private final static Logger LOG = LoggerFactory.getLogger(StandaloneConnector.class);

  @Autowired
  private SystemService systemService;
  
  protected Map<Integer, TableDetails> tabledetails;

  private static final int VPX_EMUID = 1;

  @Override
  public void initialize(ServerSettings settings) {
  }
  
  //------------------------------------------------------
  @Override
  public List<Emulator> getEmulators() {
    
    List<Emulator> emulators = new ArrayList<>();

    File vpxInstallDir = systemService.resolveVisualPinballInstallationFolder();
    if (vpxInstallDir==null) {
      // not found so default to standard Visual Piinball install directory
      vpxInstallDir = new File("c:/Visual Pinball");
    }
    String emuName = "Visual Pinball";
    File vpxTableDir = new File(vpxInstallDir, "tables");

    Emulator vpxemu = createEmulator(vpxInstallDir, vpxTableDir, VPX_EMUID, emuName); 
    vpxemu.setDescription("default");
    vpxemu.setDirB2S(new File(vpxInstallDir, "B2SServer").getAbsolutePath());

    emulators.add(vpxemu);

    int nbAdded = 0; //addGames(vpxTableDir, games, tabledetails, vpxemu);
    LOG.info("Parsed games for emulator " + VPX_EMUID + ", " + emuName + ": " + nbAdded + " games");

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

  //-----------------------------------------------------------
  @Override
  public List<Game> getGames() {
    File vpxInstallDir = systemService.resolveVisualPinballInstallationFolder();
    if (vpxInstallDir==null) {
      // not found so default to standard Visual Piinball install directory
      vpxInstallDir = new File("c:/Visual Pinball");
    }

    this.tabledetails = new HashMap<>();
    List<Game> games = new ArrayList<>();
    if (vpxInstallDir.exists()) {
      Iterator<File> tablesIterator = FileUtils.iterateFiles(new File(vpxInstallDir, "tables"), new String[]{"vpx"}, true);
      int cnt = 1;
      while (tablesIterator.hasNext()) {
        Game g = createGame(tablesIterator.next(), cnt++);
        games.add(g);
      }
    }
    return games;
  }

  private Game createGame(File tableFile, int gameid) {
    String tableName = StringUtils.removeEndIgnoreCase(tableFile.getName(), ".vpx");

    Game game = new Game();
    game.setId(gameid);
    game.setGameName(tableName);
    game.setGameFileName(tableName);
    game.setGameDisplayName(tableName);
    game.setGameFile(tableFile);

    game.setEmulatorId(VPX_EMUID);

    TableDetails detail = new TableDetails();
    detail.setGameName(tableName);
    detail.setGameFileName(tableName);
    detail.setGameDisplayName(tableName);
    detail.setEmulatorId(VPX_EMUID);

    tabledetails.put(gameid, detail);
    return game;
  }

  @Override
  public TableDetails getTableDetails(int id) {
    return this.tabledetails.get(id);
  }

  @Override
  public void saveTableDetails(int id, TableDetails tableDetails) {
    this.tabledetails.put(id, tableDetails);
  }

  //------------------------------------------------------------
  @Override
  public MediaAccessStrategy getMediaAccessStrategy() {
    return null;
  }

}
