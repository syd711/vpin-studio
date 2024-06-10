package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.games.Game;

@Service
@Qualifier("PinballX")
public class PinballXConnector extends BaseConnector {

  private final static Logger LOG = LoggerFactory.getLogger(PinballXConnector.class);

 /** The list of parsed Games from XML database */
  protected List<Game> games;

  protected Map<Integer, TableDetails> tabledetails;

  @Override
  public void initialize(ServerSettings settings) {
  }
    
  @Override
  public List<Emulator> getEmulators() {
    List<Emulator> emulators = new ArrayList<>();
    File pinballXFolder = new File("C:/PinballX");
    File pinballXIni = new File(pinballXFolder, "/Config/PinballX.ini");
    
    if (!pinballXIni.exists()) {
      LOG.warn("Ini file not found "+ pinballXIni);
      return emulators;
    }
    
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

    PinballXTableParser parser = new PinballXTableParser();

    this.games = new ArrayList<>();
    this.tabledetails = new HashMap<>();

    int emuId = 1;
    for (String emuName: emuNames) {
      String sectionName = emuName.replaceAll(" ", "");
      SubnodeConfiguration s = iniConfiguration.getSection(sectionName);
      if (!s.isEmpty()) {
        Emulator emu = createEmulator(s, pinballXFolder, emuId, emuName); 
        emulators.add(emu);
        File pinballXDb = new File(pinballXFolder, "/Databases/" + emuName + "/" + emuName + ".xml");
        if (pinballXDb.exists()) {
          int nbAdded = parser.addGames(pinballXDb, games, tabledetails, emu);
          LOG.info("Parsed games for emulator "+emuId + ", "+ emuName + ": " + nbAdded + " games");
        }
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
    String parameters = s.getString("Parameters");

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
    
    String launchScript = executable + " " + StringUtils.defaultString(parameters);

    Emulator e = new Emulator();
    e.setId(emuId);
    e.setName(emuname);
    e.setDisplayName(emuname);

    File mediaDir = new File(installDir, "Media/" +emuname);
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

   //-----------------------------------------------------------
   @Override
   public List<Game> getGames() {
     return games;
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
    return new PinballXMediaAccessStrategy();
  }

}
