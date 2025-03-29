package de.mephisto.vpin.server.fp;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class FPService {
  private final static Logger LOG = LoggerFactory.getLogger(FPService.class);

  @Autowired
  private FPCommandLineService fpCommandLineService;

  @Autowired
  private EmulatorService emulatorService;

  public boolean play(@Nullable Game game, @Nullable String altExe) {
    if (game != null) {
      return fpCommandLineService.execute(game, altExe);
    }

    return fpCommandLineService.launch();
  }

  public void installBAMCfg(@NonNull UploadDescriptor uploadDescriptor, @Nullable Game game, @NonNull File tempFile, @NonNull Frontend frontend, UploaderAnalysis analysis) throws IOException {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(game.getEmulatorId());
    File bamFolder = new File(gameEmulator.getInstallationFolder(), "BAM/cfg/");
    installBAMFile(uploadDescriptor, game, tempFile, analysis, AssetType.BAM_CFG, frontend, bamFolder);
  }

  public void installBAMFile(@NonNull UploadDescriptor uploadDescriptor, @Nullable Game game, File tempFile, UploaderAnalysis analysis, AssetType assetType, Frontend frontend, File folder) throws IOException {
    if (analysis == null) {
      analysis = new UploaderAnalysis(frontend, tempFile);
      analysis.analyze();
    }

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
      ZipUtil.unzipTargetFile(tempFile, out, bamCfgFile);
      LOG.info("Installed " + assetType.name() + ": " + out.getAbsolutePath());
    }
    else {
      if (game != null) {
        String fileName = FilenameUtils.getBaseName(game.getGameFileName()) + ".cfg";
        File out = new File(folder, fileName);
        if (out.exists() && !out.delete()) {
          throw new IOException("Failed to delete existing " + assetType.name() + " file " + out.getAbsolutePath());
        }
        org.apache.commons.io.FileUtils.copyFile(tempFile, out);
        LOG.info("Installed " + assetType.name() + ": " + out.getAbsolutePath());
      }
      else {
        LOG.error("Failed to install BAM cfg file, no game found for id {}", uploadDescriptor.getGameId());
      }
    }
  }
}
