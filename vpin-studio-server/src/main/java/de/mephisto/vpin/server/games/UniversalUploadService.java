package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.vps.VpsService;
import de.mephisto.vpin.server.vpx.VPXService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
  private MameService mameService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private VpsService vpsService;

  public File writeTableFilenameBasedEntry(UploadDescriptor descriptor, String archiveFile) throws IOException {
    File tempFile = new File(descriptor.getTempFilename());
    String archiveSuffix = FilenameUtils.getExtension(tempFile.getName());
    if (PackageUtil.isSupportedArchive(archiveSuffix)) {
      File file = new File(archiveFile);
      String baseName = FilenameUtils.getBaseName(file.getName());
      String suffix = "." + FilenameUtils.getExtension(file.getName());
      File unpackedTempFile = File.createTempFile(baseName, suffix);
      PackageUtil.unpackTargetFile(tempFile, unpackedTempFile, archiveFile);
      descriptor.getTempFiles().add(unpackedTempFile);
      return unpackedTempFile;
    }
    return tempFile;
  }

  public void importFileBasedAssets(UploadDescriptor uploadDescriptor, AssetType assetType) throws Exception {
    importFileBasedAssets(uploadDescriptor, null, assetType);
  }

  public void importFileBasedAssets(UploadDescriptor uploadDescriptor, UploaderAnalysis analysis, AssetType assetType) throws Exception {

    // If the file is not a real file but a pointer to an external resource, it is time to get the real file...
    resolveLinks(uploadDescriptor);

    if (!uploadDescriptor.isImporting(assetType)) {
      LOG.info("Skipped file import of type " + assetType.name() + ", because it is not marked for import.");
      return;
    }

    LOG.info("---> Executing table asset archive import for type \"" + assetType.name() + "\" <---");
    File temporaryUploadDescriptorBundleFile = new File(uploadDescriptor.getTempFilename());
    try {
      Game game = gameService.getGame(uploadDescriptor.getGameId());
      if (game == null) {
        throw new Exception("No game found for id " + uploadDescriptor.getGameId());
      }

      if (PackageUtil.isSupportedArchive(FilenameUtils.getExtension(temporaryUploadDescriptorBundleFile.getName()))) {
        if (analysis == null) {
          analysis = new UploaderAnalysis<>(temporaryUploadDescriptorBundleFile);
          analysis.analyze();
        }

        String fileNameForAssetType = analysis.getFileNameForAssetType(assetType);
        if (fileNameForAssetType != null) {
          File temporaryAssetArchiveFile = writeTableFilenameBasedEntry(uploadDescriptor, fileNameForAssetType);
          uploadDescriptor.getTempFiles().add(temporaryAssetArchiveFile);
          copyGameFileAsset(temporaryAssetArchiveFile, game, assetType);
        }
      }
      else if (uploadDescriptor.isFileAsset(assetType)) {
        copyGameFileAsset(temporaryUploadDescriptorBundleFile, game, assetType);
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

    LOG.info("---> Executing asset archive import for type \"" + assetType.name() + "\" <---");
    File tempFile = new File(uploadDescriptor.getTempFilename());
    if (analysis == null) {
      analysis = new UploaderAnalysis(tempFile);
      analysis.analyze();
    }

    Game game = gameService.getGame(uploadDescriptor.getGameId());
    switch (assetType) {
      case ALT_SOUND: {
        JobDescriptor jobExecutionResult = altSoundService.installAltSound(uploadDescriptor.getEmulatorId(), analysis.getRomFromAltSoundPack(), tempFile);
        uploadDescriptor.setError(jobExecutionResult.getError());
        break;
      }
      case ALT_COLOR: {
        String suffix = FilenameUtils.getExtension(tempFile.getName());
        if (PackageUtil.isSupportedArchive(suffix)) {
          altColorService.installAltColorFromArchive(analysis, game, tempFile);
          break;
        }
        JobDescriptor jobExecutionResult = altColorService.installAltColor(game, tempFile);
        uploadDescriptor.setError(jobExecutionResult.getError());
        break;
      }
      case DMD_PACK: {
        dmdService.installDMDPackage(tempFile, analysis.getDMDPath(), uploadDescriptor.getEmulatorId());
        break;
      }
      case PUP_PACK: {
        pupPacksService.installPupPack(uploadDescriptor, analysis, uploadDescriptor.isAsync());
        break;
      }
      case POPPER_MEDIA: {
        gameMediaService.installMediaPack(uploadDescriptor, analysis);
        break;
      }
      case MUSIC: {
        String rom = null;
        if(game != null) {
          rom = game.getRom();
        }
        vpxService.installMusic(tempFile, analysis, rom, uploadDescriptor.isAcceptAllAudioAsMusic());
        break;
      }
      case ROM: {
        mameService.installRom(uploadDescriptor, tempFile, analysis);
        break;
      }
      case NV: {
        mameService.installNvRam(uploadDescriptor, tempFile, analysis);
        break;
      }
      case CFG: {
        mameService.installCfg(uploadDescriptor, tempFile, analysis);
        break;
      }
      default: {
        throw new UnsupportedOperationException("No matching archive handler found for " + assetType);
      }
    }
  }

  public void resolveLinks(UploadDescriptor uploadDescriptor) throws IOException {
    // detection that it is a file hosted on a remote source
    String originalFilename = uploadDescriptor.getOriginalUploadFileName();
    if (originalFilename.endsWith(VpsInstallLink.VPS_INSTALL_LINK_PREFIX)) {

      originalFilename = StringUtils.substring(originalFilename, 0, - VpsInstallLink.VPS_INSTALL_LINK_PREFIX.length());
      uploadDescriptor.setOriginalUploadFileName(originalFilename);

      File tempFile = new File(uploadDescriptor.getTempFilename());
      try (InputStream in = new FileInputStream(tempFile)) {
        String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        String link  = StringUtils.substringBeforeLast(content, "@");
        String order  = StringUtils.substringAfterLast(content, "@");

        String tempFilename = StringUtils.substring(uploadDescriptor.getTempFilename(), 0, - VpsInstallLink.VPS_INSTALL_LINK_PREFIX.length());
        uploadDescriptor.setTempFilename(tempFilename);
        try (FileOutputStream fout = new FileOutputStream(tempFilename)) {
          vpsService.downloadLink(fout, link, Integer.parseInt(order));
        }
      }
      tempFile.delete();
    }
  }

  private static void copyGameFileAsset(File temporaryUploadDescriptorBundleFile, Game game, AssetType assetType) throws IOException {
    String fileName = FilenameUtils.getBaseName(game.getGameFileName()) + "." + assetType.name().toLowerCase();
    File gameAssetFile = new File(game.getGameFile().getParentFile(), fileName);
    if (gameAssetFile.exists() && !gameAssetFile.delete()) {
      LOG.error("Failed to delete existing game asset file " + gameAssetFile.getAbsolutePath());
      throw new UnsupportedOperationException("Failed to delete existing game asset file " + gameAssetFile.getAbsolutePath());
    }

    org.apache.commons.io.FileUtils.copyFile(temporaryUploadDescriptorBundleFile, gameAssetFile);
    LOG.info("Copied \"" + temporaryUploadDescriptorBundleFile.getAbsolutePath() + "\" to \"" + gameAssetFile.getAbsolutePath() + "\"");
  }
}
