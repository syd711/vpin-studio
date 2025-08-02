package de.mephisto.vpin.server.games;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.archiving.ArchiveMameData;
import de.mephisto.vpin.restclient.archiving.VpaArchiveUtil;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.archiving.adapters.vpa.VpaService;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.fp.FPService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

@Service
public class UniversalUploadService {
  private final static Logger LOG = LoggerFactory.getLogger(UniversalUploadService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private DMDService dmdService;

  @Autowired
  private MameService mameService;

  @Autowired
  private FPService fpService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private MusicService musicService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private MameRomAliasService mameRomAliasService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @Autowired
  private VpaService vpaService;

  public UploadDescriptor process(@RequestBody UploadDescriptor uploadDescriptor) {
    Thread.currentThread().setName("Universal Upload Thread");
    long start = System.currentTimeMillis();
    LOG.info("*********** Importing " + uploadDescriptor.getTempFilename() + " ************************");
    try {
      // If the file is not a real file but a pointer to an external resource, it is time to get the real file...
      resolveLinks(uploadDescriptor);

      File tempFile = new File(uploadDescriptor.getTempFilename());
      UploaderAnalysis analysis = new UploaderAnalysis(Features.PUPPACKS_ENABLED, tempFile);
      analysis.analyze();
      analysis.setExclusions(uploadDescriptor.getExcludedFiles(), uploadDescriptor.getExcludedFiles());

      if (analysis.isVpxOrFpTable()) {
        LOG.info("Importing table bundle, not media bundle.");

        String tableFileName = analysis.getTableFileName(uploadDescriptor.getOriginalUploadFileName());
        File temporaryGameFile = writeTableFilenameBasedEntry(uploadDescriptor, tableFileName);
        importGame(temporaryGameFile, uploadDescriptor, analysis);
      }
      else {
        LOG.info("Skipped importing table, no game file found.");
      }

      processGameAssets(uploadDescriptor, analysis);
    }
    catch (Exception e) {
      LOG.error("Processing \"" + uploadDescriptor.getTempFilename() + "\" failed: " + e.getMessage(), e);
      uploadDescriptor.setError("Processing failed: " + e.getMessage());
    }
    finally {
      uploadDescriptor.finalizeUpload();
      LOG.info("Import finished, took " + (System.currentTimeMillis() - start) + " ms.");
    }
    LOG.info("****************************** /Import Finished *************************************");
    return uploadDescriptor;
  }

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

    // For backup imports, check if the AssetType was enabled
    if (!isImportEnabled(uploadDescriptor, assetType)) {
      LOG.info("Skipped import of asset type \"{}\", disabled for backup imports.", assetType.name());
      return;
    }

    LOG.info("---> Executing table asset archive import for type \"" + assetType.name() + "\" <---");
    File temporaryUploadDescriptorBundleFile = new File(uploadDescriptor.getTempFilename());
    Game game = gameService.getGame(uploadDescriptor.getGameId());
    if (game == null) {
      throw new Exception("No game found for id " + uploadDescriptor.getGameId());
    }
    try {
      if (PackageUtil.isSupportedArchive(FilenameUtils.getExtension(temporaryUploadDescriptorBundleFile.getName()))) {
        if (analysis == null) {
          analysis = new UploaderAnalysis(Features.PUPPACKS_ENABLED, temporaryUploadDescriptorBundleFile);
          analysis.analyze();
        }

        String fileNameForAssetType = analysis.getFileNameForAssetType(assetType);
        if (fileNameForAssetType != null) {
          File temporaryAssetArchiveFile = writeTableFilenameBasedEntry(uploadDescriptor, fileNameForAssetType);
          uploadDescriptor.getTempFiles().add(temporaryAssetArchiveFile);
          copyGameFileAsset(temporaryAssetArchiveFile, game, assetType, uploadDescriptor.getUploadType());
        }
      }
      else if (uploadDescriptor.isFileAsset(assetType)) {
        copyGameFileAsset(temporaryUploadDescriptorBundleFile, game, assetType, uploadDescriptor.getUploadType());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to import " + assetType.name() + " file:" + e.getMessage(), e);
      throw e;
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(game.getId(), assetType, null);
    }
  }

  private boolean isImportEnabled(UploadDescriptor uploadDescriptor, @NonNull AssetType assetType) {
    if (uploadDescriptor.isBackupRestoreMode()) {
      BackupSettings backupSettings = preferencesService.getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class);
      return backupSettings.isAssetEnabled(assetType);
    }
    return true;
  }

  public void importArchiveBasedAssets(@NonNull UploadDescriptor uploadDescriptor, @Nullable UploaderAnalysis analysis, @NonNull AssetType assetType) throws Exception {
    importArchiveBasedAssets(uploadDescriptor, analysis, assetType, false);
  }

  public void importArchiveBasedAssets(@NonNull UploadDescriptor uploadDescriptor, @Nullable UploaderAnalysis analysis, @NonNull AssetType assetType, boolean validateAssetType) throws Exception {
    LOG.info("---> Executing asset archive import for type \"" + assetType.name() + "\" <---");
    // For backup imports, check if the AssetType was enabled
    if (!isImportEnabled(uploadDescriptor, assetType)) {
      LOG.info("Skipped import of asset type \"{}\", disabled for backup imports.", assetType.name());
      return;
    }

    File tempFile = new File(uploadDescriptor.getTempFilename());
    if (analysis == null) {
      analysis = new UploaderAnalysis(Features.PUPPACKS_ENABLED, tempFile);
      analysis.analyze();
    }

    Game game = gameService.getGame(uploadDescriptor.getGameId());
    GameEmulator gameEmulator = emulatorService.getGameEmulator(uploadDescriptor.getEmulatorId());
    String updatedAssetName = uploadDescriptor.getRom();

    switch (assetType) {
      case ALT_SOUND: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.ALT_SOUND) == null) {
          String rom = analysis.getRomFromAltSoundPack();
          if (game != null && StringUtils.isEmpty(rom)) {
            rom = game.getRom();
          }
          if (game != null && StringUtils.isEmpty(rom)) {
            rom = game.getScannedRom();
          }
          if (StringUtils.isEmpty(rom)) {
            rom = FilenameUtils.getBaseName(uploadDescriptor.getOriginalUploadFileName());
          }
          if (!StringUtils.isEmpty(rom)) {
            JobDescriptor jobExecutionResult = altSoundService.installAltSound(uploadDescriptor.getEmulatorId(), rom, tempFile);
            uploadDescriptor.setError(jobExecutionResult.getError());
            gameLifecycleService.notifyGameAssetsChanged(assetType, rom);
          }
          else {
            LOG.error("Failed to install ALT sound bundle, no ROM name could be detected.");
          }
        }
        break;
      }
      case ALT_COLOR: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.ALT_COLOR) == null) {
          String suffix = FilenameUtils.getExtension(tempFile.getName());
          if (PackageUtil.isSupportedArchive(suffix)) {
            altColorService.installAltColorFromArchive(analysis, game, tempFile);
            if (game != null) {
              gameLifecycleService.notifyGameAssetsChanged(game.getId(), assetType, updatedAssetName);
            }
            break;
          }
          if (game != null) {
            JobDescriptor jobExecutionResult = altColorService.installAltColorFromFile(game, tempFile);
            uploadDescriptor.setError(jobExecutionResult.getError());
            gameLifecycleService.notifyGameAssetsChanged(game.getId(), assetType, updatedAssetName);
          }
        }
        break;
      }
      case DMD_PACK: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.DMD_PACK) == null) {
          if (game != null) {
            dmdService.installDMDPackage(tempFile, analysis, game);
            gameLifecycleService.notifyGameAssetsChanged(game.getId(), assetType, updatedAssetName);
          }
        }
        break;
      }
      case PUP_PACK: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.PUP_PACK) == null) {
          pupPacksService.installPupPack(uploadDescriptor, analysis, uploadDescriptor.isAsync());
          gameLifecycleService.notifyGameAssetsChanged(assetType, updatedAssetName);
        }
        break;
      }
      case FRONTEND_MEDIA: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.FRONTEND_MEDIA) == null) {
          gameMediaService.installMediaPack(uploadDescriptor, analysis);
          if (game != null) {
            gameLifecycleService.notifyGameAssetsChanged(game.getId(), assetType, updatedAssetName);
          }
        }
        break;
      }
      case MUSIC: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.MUSIC) == null) {
          String rom = null;
          if (game != null) {
            rom = game.getRom();
          }
          musicService.installMusic(tempFile, uploadDescriptor.getEmulatorId(), analysis, rom, uploadDescriptor.isAcceptAllAudioAsMusic());
          if (game != null) {
            gameLifecycleService.notifyGameAssetsChanged(game.getId(), assetType, updatedAssetName);
          }
        }
        break;
      }
      case ROM: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.ROM) == null) {
          mameService.installRom(uploadDescriptor, gameEmulator, tempFile, analysis);
          gameLifecycleService.notifyGameAssetsChanged(assetType, updatedAssetName);
        }
        break;
      }
      case NV: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.NV) == null) {
          mameService.installNvRam(uploadDescriptor, gameEmulator, tempFile, analysis);
          gameLifecycleService.notifyGameAssetsChanged(assetType, updatedAssetName);
        }
        break;
      }
      case CFG: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.CFG) == null) {
          mameService.installCfg(uploadDescriptor, gameEmulator, tempFile, analysis);
          gameLifecycleService.notifyGameAssetsChanged(assetType, updatedAssetName);
        }
        break;
      }
      case BAM_CFG: {
        if (!validateAssetType || analysis.validateAssetTypeInArchive(AssetType.BAM_CFG) == null) {
          fpService.installBAMCfg(uploadDescriptor, game, gameEmulator, tempFile, analysis);
          if (game != null) {
            gameLifecycleService.notifyGameAssetsChanged(game.getId(), assetType, updatedAssetName);
          }
        }
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
    if (VpsInstallLink.isLinkFilename(originalFilename)) {

      originalFilename = VpsInstallLink.getOriginalFilename(originalFilename);
      uploadDescriptor.setOriginalUploadFileName(originalFilename);

      File tempFile = new File(uploadDescriptor.getTempFilename());
      try (InputStream in = new FileInputStream(tempFile)) {
        String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        String link = StringUtils.substringBeforeLast(content, "@");
        String order = StringUtils.substringAfterLast(content, "@");

        String tempFilename = VpsInstallLink.getOriginalFilename(uploadDescriptor.getTempFilename());
        uploadDescriptor.setTempFilename(tempFilename);
        try (FileOutputStream fout = new FileOutputStream(tempFilename)) {
          vpsService.downloadLink(fout, link, Integer.parseInt(order));
        }
      }
      tempFile.delete();
    }
  }

  private static void copyGameFileAsset(File temporaryUploadDescriptorBundleFile, Game game, AssetType assetType, @Nullable UploadType uploadType) throws IOException {
    String fileName = FilenameUtils.getBaseName(game.getGameFileName()) + "." + assetType.name().toLowerCase();
    File gameAssetFile = new File(game.getGameFile().getParentFile(), fileName);

    if (UploadType.uploadAndAppend.equals(uploadType)) {
      gameAssetFile = FileUtils.uniqueFile(gameAssetFile);
      LOG.info("Creating unique game asset file {}", gameAssetFile.getAbsolutePath());
    }

    boolean replaced = false;
    if (gameAssetFile.exists()) {
      if (!gameAssetFile.delete()) {
        LOG.error("Failed to delete existing game asset file " + gameAssetFile.getAbsolutePath());
        throw new UnsupportedOperationException("Failed to delete existing game asset file " + gameAssetFile.getAbsolutePath());
      }
      replaced = true;
    }

    org.apache.commons.io.FileUtils.copyFile(temporaryUploadDescriptorBundleFile, gameAssetFile);
    if (replaced) {
      LOG.info("Replaced \"" + gameAssetFile.getAbsolutePath() + "\" with \"" + temporaryUploadDescriptorBundleFile.getAbsolutePath() + "\"");
    }
    else {
      LOG.info("Copied \"" + temporaryUploadDescriptorBundleFile.getAbsolutePath() + "\" to \"" + gameAssetFile.getAbsolutePath() + "\"");
    }
  }

  public void processGameAssets(UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    if (uploadDescriptor.getGameId() > 0) {
      importFileBasedAssets(uploadDescriptor, analysis, AssetType.DIRECTB2S);
      importFileBasedAssets(uploadDescriptor, analysis, AssetType.POV);
      importFileBasedAssets(uploadDescriptor, analysis, AssetType.INI);
      importFileBasedAssets(uploadDescriptor, analysis, AssetType.RES);
      importFileBasedAssets(uploadDescriptor, analysis, AssetType.VBS);
    }
    else {
      LOG.info("Skipped table based assets since no gameId was set for the upload.");
    }

    importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.DMD_PACK, true);
    importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.PUP_PACK, true);
    importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.FRONTEND_MEDIA, true);
    importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.ALT_SOUND, true);
    importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.ALT_COLOR, true);
    importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.MUSIC, true);
    importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.ROM, true);
    importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.NV, true);

    if (analysis.isVpxTable()) {
      importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.CFG, true);
    }
    if (analysis.isFpTable()) {
      importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.BAM_CFG, true);
    }

    if (uploadDescriptor.isBackupRestoreMode()) {
      File tempFile = new File(uploadDescriptor.getTempFilename());
      ZipFile zipFile = vpaService.createProtectedArchive(tempFile);
      ArchiveMameData mameData = VpaArchiveUtil.readMameData(zipFile);
      GameEmulator gameEmulator = emulatorService.getGameEmulator(uploadDescriptor.getEmulatorId());

      if (mameData != null) {
        mameService.saveRegistryData(mameData);
        mameRomAliasService.writeAlias(gameEmulator, mameData.getRom(), mameData.getAlias());
      }

      String highscoreBackupZipEntry = analysis.getFileNameWithPathForExtension(HighscoreBackupService.FILE_SUFFIX);
      if (!StringUtils.isEmpty(highscoreBackupZipEntry)) {
        File highscoreBackupTempFile = File.createTempFile("highscore-backup", "." + HighscoreBackupService.FILE_SUFFIX);
        ZipUtil.writeZippedFile(analysis.getFile(), highscoreBackupZipEntry, highscoreBackupTempFile);
        highscoreBackupService.restoreBackupFile(gameEmulator, highscoreBackupTempFile);
      }
    }

    if (analysis.isVpxOrFpTable()) {
      notifyUpdates(uploadDescriptor);
    }
  }

  /**
   * Responsible for emitting updates about the newly installed table.
   * Right now this is kept simply, some event bus might be used in the future here.
   *
   * @param uploadDescriptor
   */
  public void notifyUpdates(UploadDescriptor uploadDescriptor) {
    Long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    Long channelId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_UPDATES_CHANNEL_ID, -1);
    if (channelId > 0 && serverId > 0) {
      Game game = gameService.scanGame(uploadDescriptor.getGameId());
      if (game != null) {
        EmbedBuilder embed = new EmbedBuilder();
        String url = null;
        String imgUrl = null;
        if (!StringUtils.isEmpty(game.getExtTableId())) {
          url = VPS.getVpsTableUrl(game.getExtTableId());
          VpsTable table = vpsService.getTableById(game.getExtTableId());
          if (table != null) {
            Optional<VpsTableVersion> first = table.getTableFiles().stream().filter(t -> t.getImgUrl() != null).findFirst();
            if (first.isPresent()) {
              imgUrl = first.get().getImgUrl();
            }
          }
        }

        embed.setTitle(game.getGameDisplayName(), url);
        if (imgUrl != null) {
          embed.setImage(imgUrl);
        }
        if (uploadDescriptor.getUploadType().equals(UploadType.uploadAndImport)) {
          embed.addField("New Table Installed", "", false);
        }
        else {
          embed.addField("Table Updated", "", false);
        }
        embed.setColor(Color.GREEN);
        MessageEmbed build = embed.build();

        discordService.sendMessage(serverId, channelId, build);
      }
    }
  }


  public void importGame(File temporaryGameFile, UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    UploadType uploadType = uploadDescriptor.getUploadType();
    switch (uploadType) {
      case uploadAndImport: {
        gameMediaService.uploadAndImport(temporaryGameFile, uploadDescriptor, analysis);
        break;
      }
      case uploadAndReplace: {
        gameMediaService.uploadAndReplace(temporaryGameFile, uploadDescriptor, analysis);
        break;
      }
      case uploadAndClone: {
        gameMediaService.uploadAndClone(temporaryGameFile, uploadDescriptor, analysis);
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unmapped upload type " + uploadType);
      }
    }
  }

  //-------------------------------

  public UploadDescriptor create() {
    UploadDescriptor uploadDescriptor = new UploadDescriptor();
    return uploadDescriptor;
  }

  public UploadDescriptor error(String message) {
    UploadDescriptor descriptor = create();
    descriptor.setError(message);
    return descriptor;
  }

  public UploadDescriptor create(MultipartFile file, int gameId) {
    UploadDescriptor descriptor = create();
    descriptor.setGameId(gameId);
    descriptor.setFile(file);
    descriptor.setOriginalUploadFileName(file.getOriginalFilename());
    return descriptor;
  }

  public UploadDescriptor create(MultipartFile file) {
    return create(file, 0);
  }
}
