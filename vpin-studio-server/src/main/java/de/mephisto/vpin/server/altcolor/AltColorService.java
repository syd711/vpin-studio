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
import de.mephisto.vpin.server.vpx.FolderLookupService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

/**
 *
 */
@Service
public class AltColorService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private MameService mameService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private FolderLookupService folderLookupService;

  public void setAltColorEnabled(@NonNull Game game, boolean b) {
    String rom = game.getRom();
    if (game.isVpxGame() && !StringUtils.isEmpty(rom)) {
      MameOptions options = mameService.getOptions(rom);
      options.setColorizeDmd(b);
      options.setUseExternalDmd(b);
      mameService.saveOptions(options);
      gameLifecycleService.notifyGameAssetsChanged(AssetType.ALT_COLOR, rom);
    }
  }

  public AltColorTypes getAltColorType(@NonNull Game game) {
    AltColor altColor = getAltColor(game);
    if (altColor.isAvailable()) {
      return altColor.getAltColorType();
    }
    return null;
  }

  public boolean delete(@NonNull Game game) {
    try {
      AltColor altColor = getAltColor(game);
      if (altColor.isAvailable()) {
        File dir = getAltColorFolder(game);
        if (dir.exists()) {
          File[] files = dir.listFiles();
          if (files != null) {
            for (File file : files) {
              if (file.isFile() && !file.delete()) {
                LOG.error("Failed to delete ALT color file {}", file.getAbsolutePath());
              }
            }
          }
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

  private File getAltColorFolder(@NonNull Game game, String subfolder) {
    return folderLookupService.getAltColorFolder(game, subfolder);
  }

  public File getAltColorFolder(@NonNull Game game) {
    File altColorFolder = null;
    if (game.isZenGame()) {
      File altColorFolderRoot = mameService.getAltColorFolder();
      altColorFolder = new File(altColorFolderRoot, game.getGameName());
    }
    else if (!StringUtils.isEmpty(game.getRomAlias()) && game.getEmulator() != null) {
      altColorFolder = getAltColorFolder(game, game.getRomAlias());
    }
    else if (!StringUtils.isEmpty(game.getRom()) && game.getEmulator() != null) {
      altColorFolder = getAltColorFolder(game, game.getRom());
    }
    if ((altColorFolder == null || !altColorFolder.exists()) && !StringUtils.isEmpty(game.getTableName()) && game.getEmulator() != null) {
      altColorFolder = getAltColorFolder(game, game.getTableName());
    }
    return altColorFolder;
  }

  public AltColor getAltColor(@NonNull Game game) {
    AltColor altColor = new AltColor();

    File altColorFolder = getAltColorFolder(game);
    if (altColorFolder == null || !altColorFolder.exists()) {
      return altColor;
    }

    File[] altColorFiles = altColorFolder.listFiles((dir, name) -> new File(dir, name).isFile());
    if (altColorFiles != null && altColorFiles.length > 0) {
      altColor.setModificationDate(new Date(altColorFolder.lastModified()));
      altColor.setName(altColorFolder.getName());
      altColor.setAvailable(true);
      altColor.setFiles(Arrays.stream(altColorFiles).map(File::getName).collect(Collectors.toList()));

      AltColorTypes type = AltColorTypes.mame;
      Optional<File> pacFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.PAC_SUFFIX)).findFirst();
      Optional<File> palFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.PAL_SUFFIX)).findFirst();
      Optional<File> crzFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.SERUM_SUFFIX)).findFirst();
      Optional<File> cROMcFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.CROMC_SUFFIX)).findFirst();

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
      else if (cROMcFile.isPresent()) {
        altColor.setModificationDate(new Date(cROMcFile.get().lastModified()));
        type = AltColorTypes.cROMc;
      }
      altColor.setAltColorType(type);
    }

    File backupFolder = new File(altColorFolder, "backups/");
    if (backupFolder.exists()) {
      String[] list = backupFolder.list(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.contains("[");
        }
      });
      if (list != null) {
        altColor.setBackedUpFiles(Arrays.asList(list));
      }
    }

    return altColor;
  }

  public void installAltColorFromArchive(@NonNull UploaderAnalysis analysis, Game game, File out) {
    File gameAltColorFolder = getAltColorFolder(game);

    installAltColorFromArchive(analysis, gameAltColorFolder, out, AssetType.PAC, "pin2dmd.pac");
    installAltColorFromArchive(analysis, gameAltColorFolder, out, AssetType.PAL, "pin2dmd.pal");
    installAltColorFromArchive(analysis, gameAltColorFolder, out, AssetType.VNI, "pin2dmd.vni");

    if (game.isZenGame()) {
      installAltColorFromArchive(analysis, gameAltColorFolder, out, AssetType.CRZ, "pin2dmd." + UploaderAnalysis.SERUM_SUFFIX);
      installAltColorFromArchive(analysis, gameAltColorFolder, out, AssetType.CROMC, "pin2dmd." + UploaderAnalysis.CROMC_SUFFIX);
    }
    else {
      installAltColorFromArchive(analysis, gameAltColorFolder, out, AssetType.CRZ, game.getRom() + "." + UploaderAnalysis.SERUM_SUFFIX);
      installAltColorFromArchive(analysis, gameAltColorFolder, out, AssetType.CROMC, game.getRom() + "." + UploaderAnalysis.CROMC_SUFFIX);
    }

    setAltColorEnabled(game, true);
  }

  private void installAltColorFromArchive(@NonNull UploaderAnalysis analysis, @NonNull File gameAltColorFolder, @NonNull File out, @NonNull AssetType assetType, @NonNull String fileName) {
    List<String> assetFileNames = analysis.getFileNamesForAssetType(assetType);
    for (String assetFileName : assetFileNames) {
      //copy directly into the backups folder
      if (assetFileName.contains("[")) {
        File backupsFolder = new File(gameAltColorFolder, "backups/");
        PackageUtil.unpackTargetFile(out, new File(backupsFolder, assetFileName), assetFileName);
        continue;
      }

      backupFolder(gameAltColorFolder, FilenameUtils.getExtension(fileName));
      PackageUtil.unpackTargetFile(out, new File(gameAltColorFolder, fileName), assetFileName);
    }
  }

  public JobDescriptor installAltColorFromFile(@NonNull Game game, File out) {
    File folder = getAltColorFolder(game);
    if (folder != null) {
      String name = out.getName();
      try {
        installAltColorFromFile(name, folder, out, "pin2dmd.pac");
        installAltColorFromFile(name, folder, out, "pin2dmd.vni");
        installAltColorFromFile(name, folder, out, "pin2dmd.pal");
        if (game.isZenGame()) {
          installAltColorFromFile(name, folder, out, "pin2dmd." + UploaderAnalysis.SERUM_SUFFIX);
          installAltColorFromFile(name, folder, out, "pin2dmd." + UploaderAnalysis.CROMC_SUFFIX);
        }
        else {
          installAltColorFromFile(name, folder, out, game.getRom() + "." + UploaderAnalysis.SERUM_SUFFIX);
          installAltColorFromFile(name, folder, out, game.getRom() + "." + UploaderAnalysis.CROMC_SUFFIX);
        }
      }
      catch (IOException e) {
        LOG.error("Failed to copy alt color file: " + e.getMessage(), e);
        return JobDescriptorFactory.error("Failed to copy alt color file: " + e.getMessage());
      }
    }
    LOG.info("Successfully imported ALT color from temp file " + out.getAbsolutePath());
    setAltColorEnabled(game, true);
    return JobDescriptorFactory.empty();
  }

  private void installAltColorFromFile(String name, File folder, File out, String fileName) throws IOException {
    String suffix = FilenameUtils.getExtension(fileName);
    if (name.endsWith(suffix)) {
      backupFolder(folder, suffix);
      File f = new File(folder, fileName);
      FileUtils.copyFile(out, f);
      LOG.info("Written ALT color file {}", f.getAbsolutePath());
    }
  }

  private void backupFolder(File folder, String targetSuffix) {
    if (!folder.exists()) {
      return;
    }

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
          while (backup.exists()) {
            Thread.sleep(1000);
            format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
            backup = new File(backupsFolder, FilenameUtils.getBaseName(name) + "[" + format + "]." + FilenameUtils.getExtension(name));
          }
          FileUtils.copyFile(existingFile, backup);
          LOG.info("Created backup ALTColor backup file \"" + backup.getAbsolutePath() + "\"");
          if (!existingFile.delete()) {
            LOG.error("Failed to delete existing ALTColor file \"" + existingFile.getAbsolutePath() + "\"");
          }
        }
        catch (Exception e) {
          LOG.error("Failed to backup ALTColor file \"" + existingFile.getAbsolutePath() + "\": " + e.getMessage(), e);
        }
      }
    }

  }

  public boolean restore(Game game, String filename) {
    String suffix = FilenameUtils.getExtension(filename);
    File folder = getAltColorFolder(game);
    if (folder != null && folder.exists()) {
      try {
        switch (suffix) {
          case UploaderAnalysis.PAC_SUFFIX: {
            backupFolder(folder, UploaderAnalysis.PAC_SUFFIX);
            break;
          }
          case UploaderAnalysis.VNI_SUFFIX: {
            backupFolder(folder, UploaderAnalysis.VNI_SUFFIX);
            break;
          }
          case UploaderAnalysis.PAL_SUFFIX: {
            backupFolder(folder, UploaderAnalysis.PAL_SUFFIX);
            break;
          }
          case UploaderAnalysis.SERUM_SUFFIX: {
            backupFolder(folder, UploaderAnalysis.SERUM_SUFFIX);
            break;
          }
          case UploaderAnalysis.CROMC_SUFFIX: {
            backupFolder(folder, UploaderAnalysis.CROMC_SUFFIX);
            break;
          }
        }


        File backupFile = new File(folder, "backups/" + filename);
        if (backupFile.exists()) {
          String name = FilenameUtils.getBaseName(filename);
          String ext = FilenameUtils.getExtension(filename);
          name = name.substring(0, name.indexOf("["));
          String targetName = name + "." + ext;
          File target = new File(folder, targetName);
          FileUtils.copyFile(backupFile, target);
          LOG.info("Restored backup {} to {}", backupFile.getAbsolutePath(), target.getAbsolutePath());
        }
      }
      catch (Exception e) {
        LOG.error("ALT color backup creation failed: {}", e.getMessage(), e);
        return false;
      }
    }
    return true;
  }

  public boolean deleteBackup(Game game, String filename) {
    File folder = getAltColorFolder(game);
    folder = new File(folder, "backups/");
    if (folder.exists()) {
      File file = new File(folder, filename);
      if (file.exists() && file.delete()) {
        LOG.info("Deleted ALT color file {}", file.getAbsolutePath());
        gameLifecycleService.notifyGameAssetsChanged(AssetType.ALT_COLOR, folder.getName());
        return true;
      }
    }
    return false;
  }

  @Override
  public void afterPropertiesSet() {
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
