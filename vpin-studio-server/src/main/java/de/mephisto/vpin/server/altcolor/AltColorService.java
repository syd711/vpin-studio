package de.mephisto.vpin.server.altcolor;

import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.restclient.vpinmame.VPinMameOptions;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class AltColorService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private VPinMameService vPinMameService;

  @Autowired
  private DOFLinxService dofLinxService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private FolderLookupService folderLookupService;

  public void setAltColorEnabled(@NonNull Game game, boolean b) {
    String rom = game.getRom();
    if (game.isVpxGame() && !StringUtils.isEmpty(rom)) {
      VPinMameOptions options = vPinMameService.getOptions(rom);
      options.setColorizeDmd(b);
      options.setUseExternalDmd(b);
      vPinMameService.saveOptions(options);
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
        boolean folderFound = false;
        for (File dir : getAltColorFolders(game)) {
          if (!dir.exists()) {
            continue;
          }
          folderFound = true;
          File[] files = dir.listFiles();
          if (files != null) {
            for (File file : files) {
              if (file.isFile() && !SystemUtil.deleteFileOrFolder(file)) {
                LOG.error("Failed to delete ALT color file {}", file.getAbsolutePath());
              }
            }
          }
        }

        if (folderFound) {
          gameLifecycleService.notifyGameAssetsChanged(AssetType.ALT_COLOR, altColor.getName());
          gameLifecycleService.notifyGameUpdated(game.getId());
          return true;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to delete altcolor directory for {}: {}", game, e.getMessage(), e);
    }
    return false;
  }

  /**
   * The folder that shows the colorization of the game, the first existing one of {@link #getAltColorFolders(Game)}.
   */
  public File getAltColorFolder(@NonNull Game game) {
    List<File> folders = getAltColorFolders(game);
    return folders.stream().filter(File::exists).findFirst().orElse(folders.isEmpty() ? null : folders.get(0));
  }

  /**
   * All folders that may hold the colorization of the game. There is only one in the legacy layout, but
   * VPX 10.8.1 keeps the Serum files (serum/&lt;rom&gt;) apart from the VNI/PAL/PAC files (vni/&lt;rom&gt;).
   */
  @NonNull
  public List<File> getAltColorFolders(@NonNull Game game) {
    List<File> folders = Collections.emptyList();
    if (game.isZenGame()) {
      File altColorFolderRoot = vPinMameService.getAltColorFolder();
      folders = List.of(new File(altColorFolderRoot, dofLinxService.getGameNameForAltColor(game)));
    }

    if (game.getEmulator() != null) {
      for (String name : Arrays.asList(game.getRomAlias(), game.getRom(), game.getTableName())) {
        if (folders.stream().anyMatch(File::exists)) {
          break;
        }
        if (!StringUtils.isEmpty(name)) {
          folders = folderLookupService.getAltColorFolders(game, name);
        }
      }
    }
    return folders;
  }

  /**
   * The folder to write a file of the given type to. The legacy layout has one folder for all types.
   */
  private File getAltColorFolder(@NonNull Game game, @NonNull String suffix) {
    File folder = getAltColorFolder(game);
    if (folder == null || game.isZenGame()) {
      return folder;
    }

    boolean serum = UploaderAnalysis.SERUM_SUFFIX.equalsIgnoreCase(suffix) || UploaderAnalysis.CROMC_SUFFIX.equalsIgnoreCase(suffix);
    return serum ? folderLookupService.getSerumFolder(game, folder.getName()) : folderLookupService.getVniFolder(game, folder.getName());
  }

  public AltColor getAltColor(@NonNull Game game) {
    AltColor altColor = new AltColor();

    File altColorFolder = getAltColorFolder(game);
    if (altColorFolder == null || !altColorFolder.exists()) {
      return altColor;
    }

    List<File> existingFolders = getAltColorFolders(game).stream().filter(File::exists).collect(Collectors.toList());
    altColor.setFolder(altColorFolder.getAbsolutePath());
    File[] altColorFiles = existingFolders.stream()
        .map(folder -> folder.listFiles((dir, name) -> new File(dir, name).isFile()))
        .filter(Objects::nonNull)
        .flatMap(Arrays::stream)
        .toArray(File[]::new);
    if (altColorFiles.length > 0) {
      altColor.setModificationDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(altColorFolder.lastModified()), ZoneId.systemDefault()));
      altColor.setName(altColorFolder.getName());
      altColor.setAvailable(true);
      altColor.setFiles(Arrays.stream(altColorFiles).map(File::getName).collect(Collectors.toList()));

      AltColorTypes type = AltColorTypes.mame;
      Optional<File> pacFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.PAC_SUFFIX)).findFirst();
      Optional<File> palFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.PAL_SUFFIX)).findFirst();
      Optional<File> crzFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.SERUM_SUFFIX)).findFirst();
      Optional<File> cROMcFile = Arrays.stream(altColorFiles).filter(f -> f.getName().endsWith(UploaderAnalysis.CROMC_SUFFIX)).findFirst();

      if (pacFile.isPresent()) {
        altColor.setModificationDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(pacFile.get().lastModified()), ZoneId.systemDefault()));
        type = AltColorTypes.pac;
      }
      else if (palFile.isPresent()) {
        altColor.setModificationDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(palFile.get().lastModified()), ZoneId.systemDefault()));
        type = AltColorTypes.pal;
      }
      else if (crzFile.isPresent()) {
        altColor.setModificationDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(crzFile.get().lastModified()), ZoneId.systemDefault()));
        type = AltColorTypes.serum;
      }
      else if (cROMcFile.isPresent()) {
        altColor.setModificationDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(cROMcFile.get().lastModified()), ZoneId.systemDefault()));
        type = AltColorTypes.cROMc;
      }
      altColor.setAltColorType(type);
    }

    List<String> backedUpFiles = new ArrayList<>();
    for (File folder : existingFolders) {
      File backupFolder = new File(folder, "backups/");
      if (backupFolder.exists()) {
        String[] list = backupFolder.list((dir, name) -> name.contains("["));
        if (list != null) {
          backedUpFiles.addAll(Arrays.asList(list));
        }
      }
    }
    if (!backedUpFiles.isEmpty()) {
      altColor.setBackedUpFiles(backedUpFiles);
    }

    return altColor;
  }

  public void installAltColorFromArchive(@NonNull UploaderAnalysis analysis, Game game, File out) {
    installAltColorFromArchive(analysis, game, out, AssetType.PAC, "pin2dmd.pac");
    installAltColorFromArchive(analysis, game, out, AssetType.PAL, "pin2dmd.pal");
    installAltColorFromArchive(analysis, game, out, AssetType.VNI, "pin2dmd.vni");

    if (game.isZenGame()) {
      String name = dofLinxService.getGameNameForAltColor(game);
      installAltColorFromArchive(analysis, game, out, AssetType.CRZ, name + "." + UploaderAnalysis.SERUM_SUFFIX);
      installAltColorFromArchive(analysis, game, out, AssetType.CROMC, name + "." + UploaderAnalysis.CROMC_SUFFIX);
    }
    else {
      String romName = !StringUtils.isEmpty(game.getRomAlias()) ? game.getRomAlias() : game.getRom();
      installAltColorFromArchive(analysis, game, out, AssetType.CRZ, romName + "." + UploaderAnalysis.SERUM_SUFFIX);
      installAltColorFromArchive(analysis, game, out, AssetType.CROMC, romName + "." + UploaderAnalysis.CROMC_SUFFIX);
    }

    setAltColorEnabled(game, true);
  }

  private void installAltColorFromArchive(@NonNull UploaderAnalysis analysis, @NonNull Game game, @NonNull File out, @NonNull AssetType assetType, @NonNull String fileName) {
    List<String> assetFileNames = analysis.getFileNamesForAssetType(assetType);
    File gameAltColorFolder = getAltColorFolder(game, FilenameUtils.getExtension(fileName));
    if (assetFileNames.isEmpty() || gameAltColorFolder == null) {
      return;
    }

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
    if (getAltColorFolder(game) != null) {
      String name = out.getName();
      try {
        String altColorName = "pin2dmd";
        if (game.isZenGame()) {
          altColorName = dofLinxService.getGameNameForAltColor(game);
        }

        installAltColorFromFile(game, name, out, altColorName + ".pac");
        installAltColorFromFile(game, name, out, altColorName + ".vni");
        installAltColorFromFile(game, name, out, altColorName + ".pal");
        if (game.isZenGame()) {
          installAltColorFromFile(game, name, out, altColorName + "." + UploaderAnalysis.SERUM_SUFFIX);
          installAltColorFromFile(game, name, out, altColorName + "." + UploaderAnalysis.CROMC_SUFFIX);
        }
        else {
          String romName = !StringUtils.isEmpty(game.getRomAlias()) ? game.getRomAlias() : game.getRom();
          installAltColorFromFile(game, name, out, romName + "." + UploaderAnalysis.SERUM_SUFFIX);
          installAltColorFromFile(game, name, out, romName + "." + UploaderAnalysis.CROMC_SUFFIX);
        }
      }
      catch (IOException e) {
        LOG.error("Failed to copy alt color file: {}", e.getMessage(), e);
        return JobDescriptorFactory.error("Failed to copy alt color file: " + e.getMessage());
      }
    }
    LOG.info("Successfully imported ALT color from temp file {}", out.getAbsolutePath());
    setAltColorEnabled(game, true);
    return JobDescriptorFactory.empty();
  }

  private void installAltColorFromFile(@NonNull Game game, String name, File out, String fileName) throws IOException {
    String suffix = FilenameUtils.getExtension(fileName);
    if (name.endsWith(suffix)) {
      File folder = getAltColorFolder(game, suffix);
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
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
      String format = OffsetDateTime.now().format(formatter);
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
            format = OffsetDateTime.now().format(formatter);
            backup = new File(backupsFolder, FilenameUtils.getBaseName(name) + "[" + format + "]." + FilenameUtils.getExtension(name));
          }
          FileUtils.copyFile(existingFile, backup);
          LOG.info("Created backup ALTColor backup file \"{}\"", backup.getAbsolutePath());
          if (!existingFile.delete()) {
            LOG.error("Failed to delete existing ALTColor file \"{}\"", existingFile.getAbsolutePath());
          }
        }
        catch (Exception e) {
          LOG.error("Failed to backup ALTColor file \"{}\": {}", existingFile.getAbsolutePath(), e.getMessage(), e);
        }
      }
    }

  }

  public boolean restore(Game game, String filename) {
    String suffix = FilenameUtils.getExtension(filename);
    File folder = getAltColorFolder(game, suffix);
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
    File folder = getAltColorFolder(game, FilenameUtils.getExtension(filename));
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
