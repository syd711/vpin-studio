package de.mephisto.vpin.server.altcolor;

import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.AltColorAnalyzer.*;

/**
 *
 */
@Service
public class AltColorService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(AltColorService.class);

  public boolean isAltColorAvailable(@NonNull Game game) {
    AltColor altColor = getAltColor(game);
    return altColor != null && !altColor.getFiles().isEmpty();
  }

  public boolean delete(@NonNull Game game) {
    try {
      AltColor altColor = getAltColor(game);
      if (altColor != null) {
        File dir = new File(game.getEmulator().getAltColorFolder(), altColor.getName());
        if (dir.exists()) {
          FileUtils.deleteDirectory(dir);
          return true;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to delete altcolor directory for " + game + ": " + e.getMessage(), e);
    }
    return false;
  }

  @Nullable
  public AltColor getAltColor(@NonNull Game game) {
    String rom = game.getRom();
    String tableName = game.getTableName();

    File altColorFolder = null;
    if (!StringUtils.isEmpty(rom)) {
      altColorFolder = new File(game.getEmulator().getAltColorFolder(), rom);
    }

    if (altColorFolder == null || (!altColorFolder.exists() && !StringUtils.isEmpty(tableName))) {
      altColorFolder = new File(game.getEmulator().getAltColorFolder(), tableName);
    }


    if (!altColorFolder.exists()) {
      return null;
    }

    File[] altColorFiles = altColorFolder.listFiles((dir, name) -> new File(dir, name).isFile());
    if (altColorFiles != null && altColorFiles.length > 0) {
      AltColor altColor = new AltColor();
      altColor.setModificationDate(new Date(altColorFolder.lastModified()));
      altColor.setName(altColorFolder.getName());
      altColor.setFiles(Arrays.stream(altColorFiles).map(File::getName).collect(Collectors.toList()));

      AltColorTypes type = AltColorTypes.mame;
      Optional<File> pacFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(PAC_SUFFIX)).findFirst();
      Optional<File> palFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(PAL_SUFFIX)).findFirst();
      Optional<File> crzFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(SERUM_SUFFIX)).findFirst();

      if (pacFile.isPresent()) {
        altColor.setModificationDate(new Date(pacFile.get().lastModified()));
        type = AltColorTypes.pac;
      }
      else if (palFile.isPresent()) {
        altColor.setModificationDate(new Date(palFile.get().lastModified()));
        type = AltColorTypes.pal;
      }
      else if (crzFile.isPresent()) {
        altColor.setModificationDate(new Date(crzFile.get().lastModified()));
        type = AltColorTypes.serum;
      }

      altColor.setAltColorType(type);
      return altColor;
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

  @Override
  public void afterPropertiesSet() {
  }
}
