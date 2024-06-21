package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.server.frontend.BaseConnector;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.server.preferences.PreferencesService;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("PinballX")
public class PinballXConnector extends BaseConnector {
  public final static String PINBALL_X = FrontendType.PinballX.name();

  private final static Logger LOG = LoggerFactory.getLogger(PinballXConnector.class);

  @Autowired
  private PinballXAssetsAdapter assetsAdapter;

  @Autowired
  private PreferencesService preferencesService;
  
  private Map<String, TableDetails> mapTableDetails;

  @Override
  public void initializeConnector() {
    PinballXSettings ps = getSettings();
    if (ps!=null) {
      assetsAdapter.configureCredentials(ps.getGameExMail(), ps.getGameExPassword());
    }
  }

  @NotNull
  @Override
  public File getInstallationFolder() {
    return new File("C:/PinballX"); //TODO
  }

  public Frontend getFrontend() {
    Frontend frontend = new Frontend();
    frontend.setInstallationDirectory(getInstallationFolder().getAbsolutePath());
    frontend.setFrontendType(FrontendType.PinballX);

    frontend.setFrontendExe("PinballX.exe");
    frontend.setAdminExe("Game Manager.exe");
    frontend.setIconName("pinballx.png");
    List<VPinScreen> screens = new ArrayList<>(Arrays.asList(VPinScreen.values()));
    screens.remove(VPinScreen.Other2);
    frontend.setSupportedScreens(screens);
    frontend.setIgnoredValidations(Arrays.asList(GameValidationCode.CODE_NO_OTHER2,
        GameValidationCode.CODE_PUP_PACK_FILE_MISSING,
        GameValidationCode.CODE_OUTDATED_RECORDING
    ));

    PinballXSettings ps = getSettings();
    frontend.setAssetSearchEnabled(ps!=null && ps.isGameExEnabled());
    frontend.setAssetSearchLabel("GameEx Assets Search for PinballX");
    frontend.setPlayfieldMediaInverted(true);
    return frontend;
  }

  @Override
  public PinballXSettings getSettings() {
    try {
      PinballXSettings settings = preferencesService.getJsonPreference(PreferenceNames.PINBALLX_SETTINGS, PinballXSettings.class);
      return settings;
    } catch (Exception e) {
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
    } catch (Exception e) {
      LOG.error("Saving pinballX settings failed: " + e.getMessage(), e);
    }
  }

  @Override
  protected List<Emulator> loadEmulators() {
    List<Emulator> emulators = new ArrayList<>();
    File pinballXFolder = getInstallationFolder();
    File pinballXIni = new File(pinballXFolder, "/Config/PinballX.ini");
    
    if (!pinballXIni.exists()) {
      LOG.warn("Ini file not found "+ pinballXIni);
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
    } catch(Exception e) {
      LOG.error("cannot parse ini file " + pinballXIni, e);
    }
  
    // check standard emulators
    String[] emuNames = new String[] { 
      "Future Pinball", "Visual Pinball", "Zaccaria", "Pinball FX2", "Pinball FX3", "Pinball Arcade"
    };

    int emuId = 1;
    for (String emuName: emuNames) {
      String sectionName = emuName.replaceAll(" ", "");
      SubnodeConfiguration s = iniConfiguration.getSection(sectionName);
      if (!s.isEmpty()) {
        Emulator emu = createEmulator(s, pinballXFolder, emuId, emuName); 
        emulators.add(emu);
        emuId++;
      }
    }
    // Add specific ones
    for (int k = 1; k<20; k++) {
      SubnodeConfiguration s = iniConfiguration.getSection("System_"+k);
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
    //String parameters = s.getString("Parameters");

    String gameext = null;
    if (s.containsKey("SystemType")) {
      int systemType = s.getInt("SystemType");
      switch (systemType) {
        case 1: gameext = "vpx"; break; // Visual Pinball
        case 2: gameext = "vpx"; break; // Future Pinball
        case 4: gameext = "exe"; break; // Custom Exe
      }
    } else {
      gameext = getEmulatorExtension(emuname);
    }
    
    Emulator e = new Emulator();
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    File mediaDir = new File(installDir, "Media/" +emuname);
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

}
