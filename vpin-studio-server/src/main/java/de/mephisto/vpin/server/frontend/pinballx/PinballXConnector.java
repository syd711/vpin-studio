package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service("PinballX")
public class PinballXConnector extends BaseConnector {
  public final static String PINBALL_X = FrontendType.PinballX.name();

  @Autowired
  private SystemService systemService;

  private final static Logger LOG = LoggerFactory.getLogger(PinballXConnector.class);

  private Map<String, TableDetails> mapTableDetails;

  @Override
  public void initializeConnector(ServerSettings settings) {
  }

  @NotNull
  @Override
  public File getInstallationFolder() {
    return systemService.getFrontendInstallationFolder();
  }

  @Override
  public JsonSettings getSettings() {
    return new PinballXSettings();//TODO
  }

  @Override
  public void saveSettings(@NotNull Map<String, Object> data) {
    super.saveSettings(data);//TODO
  }

  @Override
  protected List<Emulator> loadEmulators() {
    List<Emulator> emulators = new ArrayList<>();
    File pinballXFolder = getInstallationFolder();
    File pinballXIni = new File(pinballXFolder, "/Config/PinballX.ini");

    if (!pinballXIni.exists()) {
      LOG.warn("Ini file not found " + pinballXIni);
      return emulators;
    }

    mapTableDetails = new HashMap<>();

    INIConfiguration iniConfiguration = new INIConfiguration();
    //iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    //iniConfiguration.setSeparatorUsedInOutput("=");
    //iniConfiguration.setSeparatorUsedInInput("=");

    // mind pinballX.ini is encoded in UTF-16
    try (FileReader fileReader = new FileReader(pinballXIni, Charset.forName("UTF-16"))) {
      iniConfiguration.read(fileReader);
    }
    catch (Exception e) {
      LOG.error("cannot parse ini file " + pinballXIni, e);
    }

    // check standard emulators
    String[] emuNames = new String[]{
        "Future Pinball", "Visual Pinball", "Zaccaria", "Pinball FX2", "Pinball FX3", "Pinball Arcade"
    };

    int emuId = 1;
    for (String emuName : emuNames) {
      String sectionName = emuName.replaceAll(" ", "");
      SubnodeConfiguration s = iniConfiguration.getSection(sectionName);
      if (!s.isEmpty()) {
        Emulator emu = createEmulator(s, pinballXFolder, emuId, emuName);
        emulators.add(emu);
        emuId++;
      }
    }
    // Add specific ones
    for (int k = 1; k < 20; k++) {
      SubnodeConfiguration s = iniConfiguration.getSection("System_" + k);
      if (!s.isEmpty()) {
        String emuname = s.getString("Name");
        emulators.add(createEmulator(s, pinballXFolder, emuId++, emuname));
      }
    }

    return emulators;
  }

  /*
  [System_1]
  Name=System1 - other VPX
  Enabled=True
  SystemType=1
  WorkingPath=C:\Visual Pinball 10.7
  TablePath=C:\Visual Pinball\tables
  Executable=VPinballX.exe
  Parameters=-light
  */
  private Emulator createEmulator(SubnodeConfiguration s, File installDir, int emuId, String emuname) {

    boolean enabled = s.getBoolean("Enabled", false);
    String tablePath = s.getString("TablePath");
    String workingPath = s.getString("WorkingPath");
    String executable = s.getString("Executable");
    String parameters = s.getString("Parameters");

    String gameext = null;
    if (s.containsKey("SystemType")) {
      int systemType = s.getInt("SystemType");
      switch (systemType) {
        case 1:
          gameext = "vpx";
          break; // Visual Pinball
        case 2:
          gameext = "vpx";
          break; // Future Pinball
        case 4:
          gameext = "exe";
          break; // Custom Exe
      }
    }
    else {
      gameext = getEmulatorExtension(emuname);
    }

    String launchScript = executable + " " + StringUtils.defaultString(parameters);

    Emulator e = new Emulator();
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    File mediaDir = new File(installDir, "Media/" + emuname);
    if (mediaDir.exists() && mediaDir.isDirectory()) {
      e.setDirMedia(mediaDir.getAbsolutePath());
    }
    e.setDirGames(tablePath);
    if (StringUtils.equals(emuname, "Visual Pinball")) {
      e.setDirRoms(workingPath + "/VPinMAME/roms");
    }
    //e.setDescription(rs.getString("Description"));
    e.setEmuLaunchDir(workingPath);
    e.setLaunchScript(launchScript);
    e.setGamesExt(gameext);
    e.setVisible(enabled);

    return e;
  }

  @Override
  protected List<String> loadGames(Emulator emu) {
    File pinballXFolder = getInstallationFolder();
    List<String> games = new ArrayList<>();

    File pinballXDb = new File(pinballXFolder, "/Databases/" + emu.getName() + "/" + emu.getName() + ".xml");
    if (pinballXDb.exists()) {
      PinballXTableParser parser = new PinballXTableParser();
      parser.addGames(pinballXDb, games, mapTableDetails, emu);
    }

    return games;
  }


  //---------------------------------------------------

  @Override
  protected TableDetails getGameFromDb(String game) {
    return mapTableDetails.get(game);
  }

  @Override
  protected void updateGameInDb(String game, TableDetails details) {
    mapTableDetails.put(game, details);
  }

  @Override
  protected void dropGameFromDb(String game) {
    mapTableDetails.remove(game);
  }

  @Override
  protected void commitDb() {
    //TODO implement persitence in XML
  }

  //------------------------------------------------------------

  @Override
  public MediaAccessStrategy getMediaAccessStrategy() {
    return new PinballXMediaAccessStrategy();
  }

  //----------------------------------
  // UI Management


  @Override
  public boolean killFrontend() {
    List<ProcessHandle> pinUpProcesses = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() &&
            (
                p.info().command().get().contains("PinballX") ||
                    p.info().command().get().contains("VPXStarter") ||
                    p.info().command().get().contains("VPinballX") ||
                    p.info().command().get().startsWith("VPinball") ||
                    p.info().command().get().contains("B2SBackglassServerEXE") ||
                    p.info().command().get().contains("DOF")))
        .collect(Collectors.toList());

    if (pinUpProcesses.isEmpty()) {
      LOG.info("No PinballX processes found, termination canceled.");
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
        if (cmdName.contains("PinballX") && cmdName.contains("Setup")) {
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
      List<String> params = Arrays.asList("cmd", "/c", "start", "PinballX.exe");
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
