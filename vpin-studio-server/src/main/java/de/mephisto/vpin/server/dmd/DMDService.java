package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import de.mephisto.vpin.restclient.dmd.FreezySummary;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.popper.PinUPConnector;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class DMDService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(DMDService.class);

  @Autowired
  private PinUPConnector pinUPConnector;

  private Map<Integer, FreezySummary> cache = new HashMap<>();

  public boolean isDMDPackageAvailable(@NonNull Game game) {
    DMDPackage dmdPackage = getDMDPackage(game);
    return dmdPackage != null && !dmdPackage.getFiles().isEmpty();
  }

  public boolean delete(@NonNull Game game) {
    try {
      DMDPackage dmdPackage = getDMDPackage(game);
      if (dmdPackage != null) {
        File dir = new File(game.getEmulator().getAltColorFolder(), dmdPackage.getName());
        if (dir.exists()) {
          FileUtils.deleteDirectory(dir);
          return true;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to delete DMD directory for " + game + ": " + e.getMessage(), e);
    }
    return false;
  }

  @Nullable
  public DMDPackage getDMDPackage(@NonNull Game game) {
    String rom = game.getRom();
    String tableName = game.getTableName();

    List<String> folderNames = Arrays.asList(rom + "." + DMDPackageTypes.FlexDMD.name(),
      rom + "." + DMDPackageTypes.UltraDMD.name(),
      tableName + "." + DMDPackageTypes.FlexDMD.name(),
      tableName + "." + DMDPackageTypes.UltraDMD.name());

    File dmdFolder = null;
    for (String folderName : folderNames) {
      dmdFolder = new File(game.getEmulator().getTablesFolder(), folderName);
      if (dmdFolder.exists()) {

        break;
      }
    }

    if (!dmdFolder.exists()) {
      return null;
    }

    DMDPackage dmdPackage = new DMDPackage();
    File[] dmdFiles = dmdFolder.listFiles((dir, name) -> new File(dir, name).isFile());
    if (dmdFiles != null && dmdFiles.length > 0) {
      dmdPackage.setModificationDate(new Date(dmdFolder.lastModified()));
      dmdPackage.setDmdPackageTypes(dmdFolder.getName().contains(DMDPackageTypes.FlexDMD.name()) ? DMDPackageTypes.FlexDMD : DMDPackageTypes.UltraDMD);
      dmdPackage.setName(dmdFolder.getName());
      dmdPackage.setFiles(Arrays.stream(dmdFiles).map(File::getName).collect(Collectors.toList()));
      Arrays.stream(dmdFiles).forEach(f -> dmdPackage.setSize(dmdPackage.getSize() + f.length()));
      return dmdPackage;
    }

    return null;
  }

  public JobExecutionResult installDMDPackage(Game game, File archive) {
    DMDInstallationUtil.unzip(archive, game.getEmulator().getTablesFolder());
    return JobExecutionResultFactory.empty();
  }

  public FreezySummary getFreezySummary(int emulatorId) {
    if (!cache.containsKey(emulatorId)) {
      GameEmulator defaultGameEmulator = pinUPConnector.getGameEmulator(emulatorId);
      cache.put(emulatorId, readFreezyDetails(defaultGameEmulator));
    }
    return cache.get(emulatorId);
  }

  private FreezySummary readFreezyDetails(@NonNull GameEmulator emulator) {
    FreezySummary summary = new FreezySummary();
    File iniFile = new File(emulator.getMameFolder(), "DmdDevice.ini");
    try {
      if (!iniFile.exists()) {
        throw new Exception(iniFile.getAbsolutePath() + " does not exist.");
      }

      INIConfiguration iniConfiguration = new INIConfiguration();
      iniConfiguration.setCommentLeadingCharsUsedInInput(";");
      iniConfiguration.setSeparatorUsedInOutput("=");
      iniConfiguration.setSeparatorUsedInInput("=");

      FileReader fileReader = new FileReader(iniFile);
      iniConfiguration.read(fileReader);
      SubnodeConfiguration s = iniConfiguration.getSection("global");
      if (s == null) {
        throw new Exception("'global' section in " + iniFile.getAbsolutePath() + " does ont exist");
      }

      if (s.containsKey("vni.key")) {
        throw new Exception("'vni.key' not found in " + iniFile.getAbsolutePath());
      }

      String vniKey = s.getString("vni.key");
      if (StringUtils.isEmpty(vniKey)) {
        throw new Exception("No value for 'vni.key' in " + iniFile.getAbsolutePath());
      }
      summary.setVniKey(vniKey);

      int pluginIndex = 0;
      String key = "plugin." + pluginIndex + ".path";
      while (s.containsKey(key)) {
        summary.getPlugins().add(s.getString(key));
        pluginIndex++;
        key = "plugin." + pluginIndex + ".path";
      }
    } catch (Exception e) {
      LOG.error("Failed to load " + iniFile.getAbsolutePath() + ": " + e.getMessage());
      summary.setStatus(e.getMessage());
    }
    return summary;
  }

  @Override
  public void afterPropertiesSet() {
  }

  public boolean clearCache() {
    return false;
  }
}
