package de.mephisto.vpin.server.altcolor;

import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class AltColorService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(AltColorService.class);

  @Autowired
  private MameService mameService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  public boolean setAltColorEnabled(@NonNull String rom, boolean b) {
    if (!StringUtils.isEmpty(rom)) {
      MameOptions options = mameService.getOptions(rom);
      options.setColorizeDmd(b);
      options.setUseExternalDmd(b);
      mameService.saveOptions(options);
      gameLifecycleService.notifyGameAssetsChanged(AssetType.ALT_COLOR, rom);
    }
    return b;
  }

  public AltColorTypes getAltColorType(@NonNull Game game) {
    AltColor altColor = getAltColor(game);
    if (altColor != null) {
      return altColor.getAltColorType();
    }
    return null;
  }

  public boolean delete(@NonNull Game game) {
    try {
      AltColor altColor = getAltColor(game);
      if (altColor != null) {
        File dir = new File(game.getEmulator().getAltColorFolder(), altColor.getName());
        if (dir.exists()) {
          FileUtils.deleteDirectory(dir);
          gameLifecycleService.notifyGameAssetsChanged(AssetType.ALT_COLOR, altColor.getName());
          return true;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to delete altcolor directory for " + game + ": " + e.getMessage(), e);
    }
    return false;
  }

  @Nullable
  public AltColor getAltColor(@NonNull Game game) {
    String rom = game.getRom();
    String tableName = game.getTableName();

    File altColorFolder = null;
    if (!StringUtils.isEmpty(game.getRomAlias())) {
      altColorFolder = new File(game.getEmulator().getAltColorFolder(), game.getRomAlias());
    }
    else if (!StringUtils.isEmpty(rom)) {
      altColorFolder = new File(game.getEmulator().getAltColorFolder(), rom);
    }

    if ((altColorFolder == null || !altColorFolder.exists()) && !StringUtils.isEmpty(tableName)) {
      altColorFolder = new File(game.getEmulator().getAltColorFolder(), tableName);
    }


    if (altColorFolder == null || !altColorFolder.exists()) {
      return null;
    }

    File[] altColorFiles = altColorFolder.listFiles((dir, name) -> new File(dir, name).isFile());
    if (altColorFiles != null && altColorFiles.length > 0) {
      AltColor altColor = new AltColor();
      altColor.setModificationDate(new Date(altColorFolder.lastModified()));
      altColor.setName(altColorFolder.getName());
      altColor.setFiles(Arrays.stream(altColorFiles).map(File::getName).collect(Collectors.toList()));

      AltColorTypes type = AltColorTypes.mame;
      Optional<File> pacFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.PAC_SUFFIX)).findFirst();
      Optional<File> palFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.PAL_SUFFIX)).findFirst();
      Optional<File> crzFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.SERUM_SUFFIX)).findFirst();

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

      File backupFolder = new File(altColorFolder, "backups/");
      if (backupFolder.exists()) {
        String[] list = backupFolder.list();
        if (list != null) {
          altColor.setBackedUpFiles(list.length);
        }
      }
      return altColor;
    }

    return null;
  }

  public void installAltColorFromArchive(@NonNull UploaderAnalysis analysis, Game game, File out) throws IOException {
    String assetFileName = analysis.getFileNameForAssetType(AssetType.PAC);
    if (assetFileName != null) {
      PackageUtil.unpackTargetFile(out, new File(game.getAltColorFolder(), "pin2dmd.pac"), assetFileName);
    }

    assetFileName = analysis.getFileNameForAssetType(AssetType.PAL);
    if (assetFileName != null) {
      PackageUtil.unpackTargetFile(out, new File(game.getAltColorFolder(), "pin2dmd.pal"), assetFileName);
    }

    assetFileName = analysis.getFileNameForAssetType(AssetType.VNI);
    if (assetFileName != null) {
      PackageUtil.unpackTargetFile(out, new File(game.getAltColorFolder(), "pin2dmd.vni"), assetFileName);
    }

    assetFileName = analysis.getFileNameForAssetType(AssetType.CRZ);
    if (assetFileName != null) {
      PackageUtil.unpackTargetFile(out, new File(game.getAltColorFolder(), game.getRom() + "." + UploaderAnalysis.SERUM_SUFFIX), assetFileName);
    }

    setAltColorEnabled(game.getRom(), true);
  }

  public JobDescriptor installAltColor(@NonNull Game game, File out) {
    File folder = game.getAltColorFolder();
    if (folder != null) {
      String name = out.getName();
      if (name.endsWith(UploaderAnalysis.PAC_SUFFIX)) {
        try {
          backupFolder(folder, UploaderAnalysis.PAC_SUFFIX);
          FileUtils.copyFile(out, new File(folder, "pin2dmd.pac"));
        }
        catch (IOException e) {
          LOG.error("Failed to copy pac file: " + e.getMessage(), e);
          return JobDescriptorFactory.error("Failed to copy pac file: " + e.getMessage());
        }
      }
      else if (name.endsWith(UploaderAnalysis.PAL_SUFFIX)) {
        try {
          backupFolder(folder, UploaderAnalysis.PAL_SUFFIX);
          FileUtils.copyFile(out, new File(folder, "pin2dmd.pal"));
        }
        catch (IOException e) {
          LOG.error("Failed to copy pal file: " + e.getMessage(), e);
          return JobDescriptorFactory.error("Failed to copy pal file: " + e.getMessage());
        }
      }
      else if (name.endsWith(UploaderAnalysis.VNI_SUFFIX)) {
        try {
          backupFolder(folder, UploaderAnalysis.VNI_SUFFIX);
          FileUtils.copyFile(out, new File(folder, "pin2dmd.vni"));
        }
        catch (IOException e) {
          LOG.error("Failed to copy vni file: " + e.getMessage(), e);
          return JobDescriptorFactory.error("Failed to copy vni file: " + e.getMessage());
        }
      }
      else if (name.endsWith(UploaderAnalysis.SERUM_SUFFIX)) {
        try {
          backupFolder(folder, UploaderAnalysis.SERUM_SUFFIX);
          FileUtils.copyFile(out, new File(folder, game.getRom() + "." + UploaderAnalysis.SERUM_SUFFIX));
        }
        catch (IOException e) {
          LOG.error("Failed to copy cRZ file: " + e.getMessage(), e);
          return JobDescriptorFactory.error("Failed to copy cRZ file: " + e.getMessage());
        }
      }
    }
    LOG.info("Successfully imported ALT color from temp file " + out.getAbsolutePath());
    setAltColorEnabled(game.getRom(), true);
    return JobDescriptorFactory.empty();
  }

  private void backupFolder(File folder, String targetSuffix) {
    File[] existingFiles = folder.listFiles((dir, name) -> new File(dir, name).isFile());
    File backupsFolder = new File(folder, "backups/");
    backupsFolder.mkdirs();
    if (existingFiles != null) {
      String format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
      for (File existingFile : existingFiles) {
        String existingSuffix = FilenameUtils.getExtension(existingFile.getName());
        if (targetSuffix.equalsIgnoreCase(UploaderAnalysis.PAL_SUFFIX) && existingSuffix.equalsIgnoreCase(UploaderAnalysis.VNI_SUFFIX)) {
          continue;
        }
        if (targetSuffix.equalsIgnoreCase(UploaderAnalysis.VNI_SUFFIX) && existingSuffix.equalsIgnoreCase(UploaderAnalysis.PAL_SUFFIX)) {
          continue;
        }

        try {
          String name = existingFile.getName();
          File backup = new File(backupsFolder, FilenameUtils.getBaseName(name) + "[" + format + "]." + FilenameUtils.getExtension(name));
          FileUtils.copyFile(existingFile, backup);
          LOG.info("Created backup ALTColor backup file \"" + backup.getAbsolutePath() + "\"");
          if (!existingFile.delete()) {
            LOG.error("Failed to delete existing ALTColor file \"" + existingFile.getAbsolutePath() + "\"");
          }
        }
        catch (IOException e) {
          LOG.error("Failed to backup ALTColor file \"" + existingFile.getAbsolutePath() + "\": " + e.getMessage(), e);
        }
      }
    }

  }

  @Override
  public void afterPropertiesSet() {
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
