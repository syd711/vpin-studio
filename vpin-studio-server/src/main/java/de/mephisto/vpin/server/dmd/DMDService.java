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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
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
    String rom = String.valueOf(game.getRom());
    String tableName = String.valueOf(game.getTableName());

    List<String> folderNames = Arrays.asList(rom.toLowerCase() + "." + DMDPackageTypes.FlexDMD.name().toLowerCase(),
      rom.toLowerCase() + "." + DMDPackageTypes.UltraDMD.name().toLowerCase(),
      tableName.toLowerCase() + "." + DMDPackageTypes.FlexDMD.name().toLowerCase(),
      tableName.toLowerCase() + "." + DMDPackageTypes.UltraDMD.name().toLowerCase());

    File dmdFolder = null;
    File[] subFolders = game.getEmulator().getTablesFolder().listFiles(File::isDirectory);
    for (File folder : subFolders) {
      if (folderNames.contains(folder.getName().toLowerCase())) {
        dmdFolder = folder;
        break;
      }
      String name = folder.getName().toLowerCase();
      if (!StringUtils.isEmpty(game.getRom()) && name.contains(game.getRom().toLowerCase()) && name.contains("dmd")) {
        dmdFolder = folder;
        break;
      }

      if (!StringUtils.isEmpty(game.getTableName()) && name.contains(game.getTableName().toLowerCase()) && name.contains("dmd")) {
        dmdFolder = folder;
        break;
      }
    }

    if (dmdFolder == null) {
      return null;
    }

    String folderName = dmdFolder.getName().toLowerCase();
    DMDPackageTypes packageTypes = DMDPackageTypes.Unknown;
    if (folderName.contains(DMDPackageTypes.FlexDMD.name().toLowerCase())) {
      packageTypes = DMDPackageTypes.FlexDMD;
    }
    else if (folderName.contains(DMDPackageTypes.UltraDMD.name().toLowerCase())) {
      packageTypes = DMDPackageTypes.UltraDMD;
    }

    DMDPackage dmdPackage = new DMDPackage();
    File[] dmdFiles = dmdFolder.listFiles((dir, name) -> new File(dir, name).isFile());
    if (dmdFiles != null && dmdFiles.length > 0) {
      dmdPackage.setModificationDate(new Date(dmdFolder.lastModified()));
      dmdPackage.setDmdPackageTypes(packageTypes);
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
