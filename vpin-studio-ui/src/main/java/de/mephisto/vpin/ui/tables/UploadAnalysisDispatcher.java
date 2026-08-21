package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.backups.BackupDialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class UploadAnalysisDispatcher {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static void dispatch(@NonNull File file, @Nullable GameRepresentation game, @Nullable Runnable finalizer) {
    String extension = FilenameUtils.getExtension(file.getName());
    EmulatorType emulatorType = null;
    if (game != null) {
      GameEmulatorRepresentation gameEmulator = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
      emulatorType = gameEmulator.getType();
    }
    AssetType assetType = AssetType.fromExtension(emulatorType, extension);
    if (assetType == null) {
      LOG.error("Unsupported upload type: " + assetType);
      Platform.runLater(() -> {
        WidgetFactory.showInformation(Studio.stage, Messages.get("dialog.the_given_file_type_is_not_supported"), null);
      });
      return;
    }

    if (PackageUtil.isSupportedArchive(extension)) {
      validateArchive(file, game, finalizer);
    }
    else {
      dispatchFile(file, game, assetType, finalizer);
    }
  }

  public static void dispatchFile(@NonNull File file, @Nullable GameRepresentation game, @NonNull AssetType assetType, @Nullable Runnable finalizer) {
    UploaderAnalysis analysis = new UploaderAnalysis(Features.PUPPACKS_ENABLED, file);
    dispatchBySuffix(file, game, assetType, analysis, finalizer);
  }

  private static void dispatchBySuffix(@NonNull File file, @Nullable GameRepresentation game, @NonNull AssetType assetType,
                                       @NonNull UploaderAnalysis analysis, @Nullable Runnable finalizer) {
    switch (assetType) {
      case ROM: {
        TableDialogs.onRomUploads(game != null ? game.getEmulatorId() : -1, file, finalizer);
        return;
      }
      case NV: {
        TableDialogs.openNvRamUploads(file, finalizer);
        return;
      }
      case CFG: {
        TableDialogs.openCfgUploads(file, finalizer);
        return;
      }
      case FPL: {
        TableDialogs.openFplUploads(file, finalizer);
        return;
      }
      case DIF: {
        TableDialogs.openPatchUpload(game, file, analysis, finalizer);
        return;
      }
      case DMD_PACK: {
        TableDialogs.openDMDUploadDialog(game, file, analysis, finalizer);
        return;
      }
      case VPT:
      case VPX: {
        TableDialogs.openTableUploadDialog(game, EmulatorType.VisualPinball, null, analysis, finalizer);
        return;
      }
      case FPT: {
        TableDialogs.openTableUploadDialog(game, EmulatorType.FuturePinball, null, analysis, finalizer);
        return;
      }
      default: {
      }
    }

    if (game == null) {
      WidgetFactory.showInformation(Studio.stage, Messages.get("dialog.the_given_file_can_not_be_uploaded"), null);
      return;
    }

    switch (assetType) {
      case ALT_SOUND: {
        TableDialogs.openAltSoundUploadDialog(game, file, analysis, finalizer);
        return;
      }
      case DIRECTB2S: {
        TableDialogs.openBackglassUpload(null, Studio.stage, game, file, finalizer);
        return;
      }
      case BAM_CFG: {
        TableDialogs.openBamCfgUploads(file, game, finalizer);
        return;
      }
      case RES: {
        TableDialogs.directResUpload(Studio.stage, game, file, finalizer);
        return;
      }
      case INI: {
        TableDialogs.directIniUpload(Studio.stage, game, file, finalizer);
        break;
      }
      case POV: {
        TableDialogs.directPovUpload(Studio.stage, game, file, finalizer);
        break;
      }
      case ALT_COLOR:
      case PAC:
      case PAL:
      case VNI:
      case CROMC:
      case CRZ: {
        TableDialogs.openAltColorUploadDialog(game, file, analysis, finalizer);
        break;
      }
      case MUSIC: {
        TableDialogs.openMusicUploadDialog(file, analysis, game.getId(), finalizer);
        break;
      }
      case PUP_PACK: {
        TableDialogs.openPupPackUploadDialog(game, file, analysis, finalizer);
        break;
      }
      case FRONTEND_MEDIA: {
        TableDialogs.openMediaUploadDialog(Studio.stage, game, file, analysis, null, -1);
        break;
      }
      case VPA: {
        BackupDialogs.openArchiveUploadDialog(file);
        break;
      }
      default: {
        showDefault(file);
      }
    }
  }

  private static void showDefault(@NonNull File file) {
    Platform.runLater(() -> {
      if (isArchive(file)) {
        WidgetFactory.showInformation(Studio.stage, Messages.get("dialog.no_matching_files_found_in_this_archive"), Messages.get("dialog.extract_the_archive_and_upload_the_files"));
      }
      else {
        WidgetFactory.showInformation(Studio.stage, Messages.get("dialog.the_given_file_type_is_not_supported"), null);
      }
    });
  }

  public static boolean isArchive(File file) {
    String extension = FilenameUtils.getExtension(file.getName());
    return PackageUtil.isSupportedArchive(extension);
  }

  public static UploaderAnalysis analyzeArchive(File file) {
    return analyzeArchive(null, file);
  }

  public static UploaderAnalysis analyzeArchive(Stage parentStage, File file) {
    try {
      ProgressModel<?> model = createProgressModel(file);
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(parentStage, model);
      List<Object> results = progressDialog.getResults();
      if (!results.isEmpty()) {
        return (UploaderAnalysis) results.getFirst();
      }
      else {
        WidgetFactory.showAlert(parentStage != null ? parentStage : Studio.stage, Messages.get("common.error"), Messages.get("dialog.error_opening_archive_upload_likely_cancelled"));
      }
    }
    catch (Exception e) {
      LOG.error("Error opening archive: {}", e.getMessage(), e);
      WidgetFactory.showAlert(parentStage != null ? parentStage : Studio.stage, Messages.get("common.error"), Messages.get("dialog.error_opening_archive") + e.getMessage());
    }
    return null;
  }


  public static String validateArchive(File file, AssetType assetType) {
    UploaderAnalysis analysis = analyzeArchive(file);
    return analysis != null ? analysis.validateAssetTypeInArchive(assetType) : null;
  }

  public static String validateArchive(@NonNull File file, @Nullable GameRepresentation game, @Nullable Runnable finalizer) {
    UploaderAnalysis analysis = analyzeArchive(file);
    if (analysis != null) {
      List<AssetType> assetTypes = analysis.getAssetTypes();
      if (!assetTypes.isEmpty()) {
        if (analysis.isVpxOrFpTable()) {
          TableDialogs.openTableUploadDialog(game, analysis.getEmulatorType(), null, analysis, finalizer);
        }
        else if (analysis.isPatch()) {
          if (game == null || !client.getEmulatorService().isVpxGame(game)) {
            WidgetFactory.showInformation(Studio.stage, Messages.get("dialog.can_not_apply_a_patch_without_a"), Messages.get("dialog.select_the_matching_table_for_the_patch"));
            return null;
          }
          TableDialogs.openPatchUpload(game, file, analysis, finalizer);
        }
        else if (assetTypes.size() == 1) {
          dispatchBySuffix(file, game, assetTypes.getFirst(), analysis, finalizer);
        }
        else {
          TableDialogs.openMediaUploadDialog(Studio.stage, game, file, analysis, null, -1);
        }
      }
      else {
        WidgetFactory.showInformation(Studio.stage, Messages.get("dialog.a_matching_asset_type_could_not_be"), null);
      }
    }
    return null;
  }

  private static ProgressModel<?> createProgressModel(File file) throws IOException {
    String suffix = FilenameUtils.getExtension(file.getName());
    if (suffix.equalsIgnoreCase("rar") || suffix.equalsIgnoreCase("7z")) {
      return new UploadDispatchAnalysisRarProgressModel(file);
    }
    if (suffix.equalsIgnoreCase("zip")) {
      return new UploadDispatchAnalysisZipProgressModel(file);
    }
    throw new UnsupportedOperationException("Unsupported file format '" + suffix + "'");
  }
}
