package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.server.preferences.PreferencesService;
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

@Service("PinballX")
public class PinballXConnector extends BaseConnector {
  public final static String PINBALL_X = FrontendType.PinballX.name();

  @Autowired
  private SystemService systemService;

  private final static Logger LOG = LoggerFactory.getLogger(PinballXConnector.class);

  @Autowired
  private PinballXAssetsAdapter assetsAdapter;

  @Autowired
  private PreferencesService preferencesService;

  private Map<String, TableDetails> mapTableDetails = new HashMap<>();

  @Override
  public void initializeConnector() {
    PinballXSettings ps = getSettings();
    if (ps != null) {
      assetsAdapter.configureCredentials(ps.getGameExMail(), ps.getGameExPassword());
    }
  }

  @NotNull
  @Override
  public File getInstallationFolder() {
    return systemService.getFrontendInstallationFolder();
  }

  public Frontend getFrontend() {
    Frontend frontend = new Frontend();
    frontend.setName("PinballX");
    frontend.setInstallationDirectory(getInstallationFolder().getAbsolutePath());
    frontend.setFrontendType(FrontendType.PinballX);

    frontend.setFrontendExe("PinballX.exe");
    frontend.setAdminExe("Settings.exe");
    frontend.setIconName("pinballx.png");
    List<VPinScreen> screens = new ArrayList<>(Arrays.asList(VPinScreen.values()));
    screens.remove(VPinScreen.Other2);
    frontend.setSupportedScreens(screens);
    frontend.setIgnoredValidations(Arrays.asList(GameValidationCode.CODE_NO_OTHER2,
        GameValidationCode.CODE_PUP_PACK_FILE_MISSING,
        GameValidationCode.CODE_OUTDATED_RECORDING
    ));

    PinballXSettings ps = getSettings();
    frontend.setAssetSearchEnabled(ps != null && ps.isGameExEnabled());
    frontend.setAssetSearchLabel("GameEx Assets Search for PinballX");
    frontend.setPlayfieldMediaInverted(true);
    return frontend;
  }

  @Override
  public PinballXSettings getSettings() {
    try {
      PinballXSettings settings = preferencesService.getJsonPreference(PreferenceNames.PINBALLX_SETTINGS, PinballXSettings.class);
      return settings;
    }
    catch (Exception e) {
      LOG.error("Getting pinballX settings failed: " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void saveSettings(@NotNull Map<String, Object> data) {
    try {
      PinballXSettings settings = JsonSettings.objectMapper.convertValue(data, PinballXSettings.class);
      preferencesService.savePreference(PreferenceNames.PINBALLX_SETTINGS, settings);
      // reinitialize the connector with updated settings
      initializeConnector();
    }
    catch (Exception e) {
      LOG.error("Saving pinballX settings failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void clearCache() {
    this.mapTableDetails.clear();
    super.clearCache();
  }

  @Override
  protected List<Emulator> loadEmulators() {
    List<Emulator> emulators = new ArrayList<>();
    File pinballXFolder = getInstallationFolder();
    File pinballXIni = getPinballXIni();

    if (!pinballXIni.exists()) {
      LOG.warn("Ini file not found " + pinballXIni);
      return emulators;
    }

    mapTableDetails = new HashMap<>();

    INIConfiguration iniConfiguration = new INIConfiguration();
    //iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput("=");
//    iniConfiguration.setSeparatorUsedInInput("=");

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

    //check the launch and exist scripts
    for (Emulator emulator : emulators) {
      initVisualPinballXScripts(emulator, iniConfiguration);
    }

    return emulators;
  }

  @NotNull
  private File getPinballXIni() {
    File pinballXFolder = getInstallationFolder();
    return new File(pinballXFolder, "/Config/PinballX.ini");
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
    //String parameters = s.getString("Parameters");

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

    Emulator e = new Emulator();
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    File mediaDir = new File(installDir, "Media/" + emuname);
    if (mediaDir.exists() && mediaDir.isDirectory()) {
      e.setDirMedia(mediaDir.getAbsolutePath());
    }
    e.setDirGames(tablePath);
    e.setEmuLaunchDir(workingPath);
    e.setExeName(executable);

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

  @Override
  public TableAssetsAdapter getTableAssetAdapter() {
    return assetsAdapter;
  }

  @Override
  public List<FrontendPlayerDisplay> getFrontendPlayerDisplays() {
    File pinballXIni = getPinballXIni();

    INIConfiguration iniConfiguration = new INIConfiguration();
    //iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput("=");
//    iniConfiguration.setSeparatorUsedInInput("=");

    // mind pinballX.ini is encoded in UTF-16
    try (FileReader fileReader = new FileReader(pinballXIni, Charset.forName("UTF-16"))) {
      iniConfiguration.read(fileReader);
    }
    catch (Exception e) {
      LOG.error("cannot parse ini file " + pinballXIni, e);
    }

    List<FrontendPlayerDisplay> displayList = new ArrayList<>();
    SubnodeConfiguration display = iniConfiguration.getSection("Display");
    displayList.add(createDisplay(display, VPinScreen.PlayField));

    SubnodeConfiguration topper = iniConfiguration.getSection("Topper");
    displayList.add(createDisplay(topper, VPinScreen.Topper));

    SubnodeConfiguration dmd = iniConfiguration.getSection("DMD");
    displayList.add(createDisplay(dmd, VPinScreen.DMD));

    SubnodeConfiguration backGlass = iniConfiguration.getSection("BackGlass");
    displayList.add(createDisplay(backGlass, VPinScreen.BackGlass));

    SubnodeConfiguration apron = iniConfiguration.getSection("Apron");
    displayList.add(createDisplay(apron, VPinScreen.Menu));

    return displayList;
  }


  //----------------------------------
  // UI Management


  @Override
  public boolean killFrontend() {
    List<ProcessHandle> vpinProcesses = ProcessHandle
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

    if (vpinProcesses.isEmpty()) {
      LOG.info("No PinballX processes found, termination canceled.");
      return false;
    }

    for (ProcessHandle pinUpProcess : vpinProcesses) {
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
        if (cmdName.contains("PinballX")) {
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

  //---------------- Utilities -----------------------------------------------------------------------------------------


  private void initVisualPinballXScripts(Emulator emulator, INIConfiguration iniConfiguration) {
    if (emulator.isVisualPinball()) {
      //VPX scripts
      SubnodeConfiguration visualPinball = iniConfiguration.getSection("VisualPinball");
      visualPinball.setProperty("LaunchBeforeEnabled", "True");
      visualPinball.setProperty("LaunchBeforeExecutable", "emulator-launch.bat");
      visualPinball.setProperty("LaunchBeforeParameters", "\"[TABLEPATH]\\[TABLEFILE]\"");
      visualPinball.setProperty("LaunchBeforeWorkingPath", new File(RESOURCES + "/scripts").getAbsolutePath());

      visualPinball.setProperty("LaunchAfterEnabled", "True");
      visualPinball.setProperty("LaunchAfterExecutable", "emulator-exit.bat");
      visualPinball.setProperty("LaunchAfterParameters", "\"[TABLEPATH]\\[TABLEFILE]\"");
      visualPinball.setProperty("LaunchAfterWorkingPath", new File(RESOURCES + "/scripts").getAbsolutePath());

      //frontend launch script
      SubnodeConfiguration startup = iniConfiguration.getSection("StartupProgram");
      startup.setProperty("Enabled", " True");
      startup.setProperty("Executable", "frontend-launch.bat");
      startup.setProperty("WorkingPath", new File(RESOURCES + "/scripts").getAbsolutePath());

      saveIni(iniConfiguration);
    }
  }

  private void saveIni(INIConfiguration iniConfiguration) {
    if (isAdministrationRunning()) {
      killAdministration();
    }

    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(getPinballXIni(), Charset.forName("UTF-16"));
      iniConfiguration.write(fileWriter);
    }
    catch (Exception e) {
      LOG.error("Failed to write PinballX.ini: " + e.getMessage(), e);
    }
    finally {
      try {
        if (fileWriter != null) {
          fileWriter.close();
        }
      }
      catch (IOException e) {
        //ignore
      }
    }
  }

  private boolean isAdministrationRunning() {
    List<ProcessHandle> allProcesses = systemService.getProcesses();
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        if (cmdName.contains("Settings.exe")) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean killAdministration() {
    List<ProcessHandle> processes = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() && p.info().command().get().contains("Settings.exe"))
        .collect(Collectors.toList());

    if (processes.isEmpty()) {
      LOG.info("No PinballX processes found, termination canceled.");
      return false;
    }

    for (ProcessHandle p : processes) {
      String cmd = p.info().command().get();
      boolean b = p.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
    }
    return true;
  }

  private FrontendPlayerDisplay createDisplay(SubnodeConfiguration display, VPinScreen screen) {
    FrontendPlayerDisplay player = new FrontendPlayerDisplay();
    player.setName(screen.name());
    player.setMonitor(Integer.parseInt(display.getString("monitor", "0")));
    player.setX(Integer.parseInt(display.getString("x", "0")));
    player.setY(Integer.parseInt(display.getString("y", "0")));
    player.setWidth(Integer.parseInt(display.getString("width", "0")));
    player.setHeight(Integer.parseInt(display.getString("height", "0")));
    player.setRotation(Integer.parseInt(display.getString("rotate", "0")));

    LOG.info("Created PinballX player display \"" + screen.name() + "\"");
    return player;
  }

}
