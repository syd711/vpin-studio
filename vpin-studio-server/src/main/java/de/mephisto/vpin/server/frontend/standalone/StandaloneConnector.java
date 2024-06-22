package de.mephisto.vpin.server.frontend.standalone;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.system.SystemService;

@Service("Standalone")
public class StandaloneConnector extends BaseConnector {
  public final static String STANDALONE = FrontendType.Standalone.name();

  private static final Logger LOG = LoggerFactory.getLogger(StandaloneConnector.class);

  /** The default emulator id for VPX */
  private static final int VPX_EMUID = 1;

  @Autowired
  private SystemService systemService;
  
  @Override
  public void initializeConnector(ServerSettings settings) {
  }

  @NotNull
  @Override
  public File getInstallationFolder() {
    return systemService.getFrontendInstallationFolder();
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

  //----------------------------------

  // UI Management
  private String getVPXExe() { //TODO configurable default exe
    return "VPinballX64.exe";
  }

  @Override
  public boolean killFrontend() {
    List<ProcessHandle> pinUpProcesses = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() &&
            (
                p.info().command().get().contains(getVPXExe()) ||
                    p.info().command().get().contains("VPXStarter") ||
                    p.info().command().get().contains("VPinballX") ||
                    p.info().command().get().startsWith("VPinball")))
        .collect(Collectors.toList());

    if (pinUpProcesses.isEmpty()) {
      LOG.info("No VPX processes found, termination canceled.");
      return false;
    }

    for (ProcessHandle pinUpProcess : pinUpProcesses) {
      String cmd = pinUpProcess.info().command().get();
      boolean b = pinUpProcess.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
    }
    return true;
  }

  @Override
  public boolean isFrontendRunning() {
    List<ProcessHandle> allProcesses = systemService.getProcesses();
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        if (cmdName.contains("Pinball Player")) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean restartFrontend() {
    killFrontend();

    try {
      List<String> params = Arrays.asList("cmd", "/c", "start", getVPXExe());
      SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
      executor.setDir(systemService.getFrontendInstallationFolder());
      executor.executeCommandAsync();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("PinballX restart failed: {}", standardErrorFromCommand);
        return false;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to start PinballX again: " + e.getMessage(), e);
      return false;
    }
    return true;
  }
}
