package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.popper.PopperMediaService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.vpx.VPXService;
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
public class UniversalUploadService {
  private final static Logger LOG = LoggerFactory.getLogger(UniversalUploadService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private DMDService dmdService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private PopperMediaService popperMediaService;

  @Autowired
  private PupPacksService pupPacksService;

  public File writeTableFilenameBasedEntry(UploadDescriptor descriptor, String suffix) throws IOException {
    File tempFile = new File(descriptor.getTempFilename());
    String archiveSuffix = FilenameUtils.getExtension(tempFile.getName());
    if (PackageUtil.isSupportedArchive(archiveSuffix)) {
      String archiveMatch = PackageUtil.contains(tempFile, suffix);
      File unpackedTempFile = File.createTempFile(FilenameUtils.getBaseName(archiveMatch), suffix);
      PackageUtil.unpackTargetFile(tempFile, unpackedTempFile, archiveMatch);
      descriptor.getTempFiles().add(unpackedTempFile);
      return unpackedTempFile;
    }
    return tempFile;
  }


  public void importFileBasedAssets(UploadDescriptor uploadDescriptor, AssetType assetType) throws Exception {
    File temporaryAssetFile = new File(uploadDescriptor.getTempFilename());
    try {
      Game game = gameService.getGame(uploadDescriptor.getGameId());
      if (game == null) {
        throw new Exception("No game found for id " + uploadDescriptor.getGameId());
      }

      if (uploadDescriptor.isImporting(assetType)) {
        if (PackageUtil.isSupportedArchive(FilenameUtils.getExtension(temporaryAssetFile.getName()))) {
          File temporaryUploadFile = new File(uploadDescriptor.getTempFilename());
          LOG.info("Analyzing temporary upload file " + temporaryUploadFile.getAbsolutePath());
          UploaderAnalysis analysis = new UploaderAnalysis(temporaryUploadFile);
          analysis.analyze();

          if (analysis.containsAssetType(assetType)) {
            File temporaryAssetArchiveFile = writeTableFilenameBasedEntry(uploadDescriptor, "." + assetType.name());
            uploadDescriptor.getTempFiles().add(temporaryAssetArchiveFile);
            copyGameFileAsset(temporaryAssetFile, game, assetType);
          }
        }
        else if (uploadDescriptor.isFileAsset(assetType)) {
          copyGameFileAsset(temporaryAssetFile, game, assetType);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to import " + assetType.name() + " file:" + e.getMessage(), e);
      throw e;
    }
  }

  public void importArchiveBasedAssets(@NonNull UploadDescriptor uploadDescriptor, @Nullable UploaderAnalysis analysis, @NonNull AssetType assetType) throws Exception {
    if (!uploadDescriptor.isImporting(assetType)) {
      LOG.info("Skipped bundle import of type " + assetType.name() + ", because it is not marked for import.");
      return;
    }

    File tempFile = new File(uploadDescriptor.getTempFilename());
    Game game = gameService.getGame(uploadDescriptor.getGameId());
    switch (assetType) {
      case ALT_SOUND: {
        altSoundService.installAltSound(game, tempFile);
        break;
      }
      case ALT_COLOR: {
        altColorService.installAltColor(game, tempFile);
        break;
      }
      case DMD_PACK: {
        dmdService.installDMDPackage(tempFile);
        break;
      }
      case PUP_PACK: {
        pupPacksService.installPupPack(uploadDescriptor, analysis);
        break;
      }
      case POPPER_MEDIA: {
        popperMediaService.installMediaPack(uploadDescriptor, analysis);
        break;
      }
      case MUSIC: {
        vpxService.installMusic(tempFile);
        break;
      }
      case ROM: {
        gameService.installRom(uploadDescriptor, tempFile, analysis);
        break;
      }
      default: {
        throw new UnsupportedOperationException("No matching archive handler found for " + assetType);
      }
    }
  }

  private static void copyGameFileAsset(File temporaryAssetFile, Game game, AssetType assetType) throws IOException {
    String fileName = FilenameUtils.getBaseName(game.getGameFileName()) + "." + assetType.name().toLowerCase();
    File gameAssetFile = new File(game.getGameFile().getParentFile(), fileName);
    if (gameAssetFile.exists() && !gameAssetFile.delete()) {
      LOG.error("Failed to delete existing game asset file " + gameAssetFile.getAbsolutePath());
      throw new UnsupportedOperationException("Failed to delete existing game asset file " + gameAssetFile.getAbsolutePath());
    }

    org.apache.commons.io.FileUtils.copyFile(temporaryAssetFile, gameAssetFile);
    LOG.info("Copied \"" + temporaryAssetFile.getAbsolutePath() + "\" to \"" + gameAssetFile.getAbsolutePath() + "\"");
  }
}
