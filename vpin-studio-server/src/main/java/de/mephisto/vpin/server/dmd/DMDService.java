package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.mame.MameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
  private MameService mameService;

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
    }
    catch (Exception e) {
      LOG.error("Failed to delete DMD directory for " + game + ": " + e.getMessage(), e);
    }
    return false;
  }

  @Nullable
  public DMDPackage getDMDPackage(@NonNull Game game) {

    if (StringUtils.isNotEmpty(game.getDMDType())) {
      DMDPackageTypes packageTypes = DMDPackageTypes.Unknown;
      if (StringUtils.equalsIgnoreCase(game.getDMDType(), DMDPackageTypes.FlexDMD.name())) {
        packageTypes = DMDPackageTypes.FlexDMD;
      }
      else if (StringUtils.equalsIgnoreCase(game.getDMDType(), DMDPackageTypes.UltraDMD.name())) {
        packageTypes = DMDPackageTypes.UltraDMD;
      }

      DMDPackage dmdPackage = new DMDPackage();
      dmdPackage.setDmdPackageTypes(packageTypes);

      if (StringUtils.isNotEmpty(game.getDMDProjectFolder())) {
        dmdPackage.setName(game.getDMDProjectFolder());

        File dmdFolder = new File(game.getEmulator().getGamesFolder(), game.getDMDProjectFolder());
        if (dmdFolder.exists()) {
          dmdPackage.setModificationDate(new Date(dmdFolder.lastModified()));
          File[] dmdFiles = dmdFolder.listFiles((dir, name) -> new File(dir, name).isFile());
          if (dmdFiles != null && dmdFiles.length > 0) {
            dmdPackage.setFiles(Arrays.stream(dmdFiles).map(File::getName).collect(Collectors.toList()));
            Arrays.stream(dmdFiles).forEach(f -> dmdPackage.setSize(dmdPackage.getSize() + f.length()));
          }
        }
        else {
          ValidationState st = new ValidationState();
          st.setCode(GameValidationCode.CODE_NO_DMDFOLDER);
          dmdPackage.getValidationStates().add(st);
        }
      }
      else {
        dmdPackage.setName("Not Used");
      }
      return dmdPackage;
    }
    return null;
  }

  public void installDMDPackage(@NonNull File archive, @Nullable String dmdPath, @NonNull File gameFile) {
    if (dmdPath != null) {
      File tablesFolder = gameFile.getParentFile();
      String extension = FilenameUtils.getExtension(archive.getName()).toLowerCase();
      if (extension.equals(PackageUtil.ARCHIVE_ZIP)) {
        DMDInstallationUtil.unzip(archive, tablesFolder, dmdPath);
      }
      else if (extension.equals(PackageUtil.ARCHIVE_7Z) || extension.equals(PackageUtil.ARCHIVE_RAR)) {
        DMDInstallationUtil.unrar(archive, tablesFolder, dmdPath);
      }
      else {
        throw new UnsupportedOperationException("Unsupported archive format for DMD pack " + archive.getName());
      }
    }
    else {
      LOG.info("Skipped DMD extraction, no DMD path found in archive.");
    }
  }

  public ComponentSummary getFreezySummary() {
    File mameFolder = mameService.getMameFolder();
    return FreezySummarizer.summarizeFreezy(mameFolder);
  }

  public boolean clearCache() {
    return true;
  }

  @Override
  public void afterPropertiesSet() {
  }
}
