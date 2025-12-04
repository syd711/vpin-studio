package de.mephisto.vpin.server.fp;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class FuturePinballService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private FPCommandLineService fpCommandLineService;

  public boolean play(@Nullable Game game, @Nullable String altExe) {
    if (game != null) {
      return fpCommandLineService.execute(game, altExe);
    }

    return fpCommandLineService.launch();
  }

  public void installBAMCfg(@NonNull UploadDescriptor uploadDescriptor, @Nullable Game game, @NonNull GameEmulator gameEmulator, @NonNull File tempFile, @NonNull UploaderAnalysis analysis) throws IOException {
    File bamFolder = new File(gameEmulator.getInstallationFolder(), "BAM/cfg/");
    installBAMFile(uploadDescriptor, game, tempFile, analysis, AssetType.BAM_CFG, bamFolder);
  }

  private void installBAMFile(@NonNull UploadDescriptor uploadDescriptor, @Nullable Game game, File tempFile, @NonNull UploaderAnalysis analysis, AssetType assetType, File folder) throws IOException {
    String bamCfgFile = analysis.getFileNameWithPathForExtension("cfg");
    if (bamCfgFile != null) {
      String filename = bamCfgFile;
      if (filename.contains("/")) {
        filename = filename.substring(filename.lastIndexOf("/") + 1);
      }
      File out = new File(folder, filename);
      if (out.exists() && !out.delete()) {
        throw new IOException("Failed to delete existing " + assetType.name() + " file " + out.getAbsolutePath());
      }
      PackageUtil.unpackTargetFile(tempFile, out, bamCfgFile);
      LOG.info("Installed .bam file {}: {}", assetType.name(), out.getAbsolutePath());
    }
    else {
      if (game != null) {
        String fileName = FilenameUtils.getBaseName(game.getGameFileName()) + ".cfg";
        File out = new File(folder, fileName);
        if (out.exists() && !out.delete()) {
          throw new IOException("Failed to delete existing " + assetType.name() + " file " + out.getAbsolutePath());
        }
        org.apache.commons.io.FileUtils.copyFile(tempFile, out);
        LOG.info("Installed {}: {}", assetType.name(), out.getAbsolutePath());
      }
      else {
        LOG.error("Failed to install BAM cfg file, no game found for id {}", uploadDescriptor.getGameId());
      }
    }
  }

  public void installLibrary(UploadDescriptor uploadDescriptor, GameEmulator gameEmulator, File tempFile, UploaderAnalysis analysis) throws IOException {
    File installationFolder = gameEmulator != null ? gameEmulator.getInstallationFolder() : null;
    if (installationFolder != null && installationFolder.exists()) {
      List<String> fileNamesForAssetType = analysis.getFileNamesForAssetType(AssetType.FPL);
      File libFolder = new File(gameEmulator.getInstallationFolder(), "Libraries/");

      if (libFolder.exists()) {
        for (String fplFileName : fileNamesForAssetType) {
          if (fplFileName.contains("/")) {
            fplFileName = fplFileName.substring(fplFileName.lastIndexOf("/") + 1);
          }
          File out = new File(installationFolder, fplFileName);
          if (out.exists() && !out.delete()) {
            throw new IOException("Failed to delete existing " + fplFileName + " file " + out.getAbsolutePath());
          }
          org.apache.commons.io.FileUtils.copyFile(tempFile, out);
          LOG.info("Installed fpl file {}: {}", tempFile.getName(), out.getAbsolutePath());
        }

        if (FilenameUtils.getExtension(tempFile.getName()).equalsIgnoreCase(AssetType.FPL.name())) {
          File out = new File(libFolder, uploadDescriptor.getOriginalUploadFileName());
          if (out.exists() && !out.delete()) {
            throw new IOException("Failed to delete existing " + uploadDescriptor.getOriginalUploadFileName() + " file " + out.getAbsolutePath());
          }
          org.apache.commons.io.FileUtils.copyFile(tempFile, out);
          LOG.info("Installed fpl file {}: {}", tempFile.getName(), out.getAbsolutePath());
        }
      }
    }
    else {
      LOG.error("Failed to install fpl file, no game found for id {}", uploadDescriptor.getGameId());
    }
  }
}
