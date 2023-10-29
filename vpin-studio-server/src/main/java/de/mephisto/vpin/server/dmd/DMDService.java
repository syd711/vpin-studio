package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.altcolor.AltColorUtil;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.AltColorAnalyzer.*;

/**
 *
 */
@Service
public class DMDService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(DMDService.class);

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

  public JobExecutionResult installAltColor(Game game, File out) {
    File folder = game.getAltColorFolder();
    if (folder != null) {
      LOG.info("Extracting archive to " + folder.getAbsolutePath());
      if (!folder.exists()) {
        if (!folder.mkdirs()) {
          return JobExecutionResultFactory.error("Failed to create ALT color directory " + folder.getAbsolutePath());
        }
      }

      String name = out.getName();
      if (name.endsWith(".zip")) {
        AltColorUtil.unzip(out, folder);
      }
      else if (name.endsWith(PAC_SUFFIX)) {
        try {
          FileUtils.copyFile(out, new File(game.getAltColorFolder(), "pin2dmd.pac"));
        } catch (IOException e) {
          LOG.error("Failed to copy pac file: " + e.getMessage(), e);
          return JobExecutionResultFactory.error("Failed to copy pac file: " + e.getMessage());
        }
      }
      else if (name.endsWith(PAL_SUFFIX)) {
        try {
          FileUtils.copyFile(out, new File(game.getAltColorFolder(), "pin2dmd.pal"));
        } catch (IOException e) {
          LOG.error("Failed to copy pal file: " + e.getMessage(), e);
          return JobExecutionResultFactory.error("Failed to copy pal file: " + e.getMessage());
        }
      }
      else if (name.endsWith(VNI_SUFFIX)) {
        try {
          FileUtils.copyFile(out, new File(game.getAltColorFolder(), "pin2dmd.vni"));
        } catch (IOException e) {
          LOG.error("Failed to copy vni file: " + e.getMessage(), e);
          return JobExecutionResultFactory.error("Failed to copy vni file: " + e.getMessage());
        }
      }
      else if (name.endsWith(SERUM_SUFFIX)) {
        try {
          FileUtils.copyFile(out, new File(game.getAltColorFolder(), game.getRom() + SERUM_SUFFIX));
        } catch (IOException e) {
          LOG.error("Failed to copy cRZ file: " + e.getMessage(), e);
          return JobExecutionResultFactory.error("Failed to copy cRZ file: " + e.getMessage());
        }
      }

      if (!out.delete()) {
        return JobExecutionResultFactory.error("Failed to delete temporary file.");
      }
    }
    return JobExecutionResultFactory.empty();
  }

  public JobExecutionResult installDMDPackage(Game game, File archive) {
    DMDInstallationUtil.unzip(archive, game.getEmulator().getTablesFolder());
    return JobExecutionResultFactory.empty();
  }

  @Override
  public void afterPropertiesSet() {
  }
}
