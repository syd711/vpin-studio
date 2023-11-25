package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
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

  private Map<Integer, ComponentSummary> cache = new HashMap<>();

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

  public ComponentSummary getFreezySummary(int emulatorId) {
    if (!cache.containsKey(emulatorId)) {
      GameEmulator defaultGameEmulator = pinUPConnector.getGameEmulator(emulatorId);
      cache.put(emulatorId, FreezySummarizer.summarizeFreezy(defaultGameEmulator));
    }
    return cache.get(emulatorId);
  }

  public boolean clearCache() {
    this.cache.clear();
    return true;
  }

  @Override
  public void afterPropertiesSet() {
  }
}
