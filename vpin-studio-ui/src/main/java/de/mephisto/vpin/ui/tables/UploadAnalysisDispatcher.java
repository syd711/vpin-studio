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
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
        WidgetFactory.showInformation(Studio.stage, "The given file type is not supported for any upload.", null);
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
        TableDialogs.onRomUploads(file, finalizer);
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
      WidgetFactory.showInformation(Studio.stage, "The given file can not be uploaded without a table selection.", null);
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
        TableDialogs.openMusicUploadDialog(file, analysis, finalizer);
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
        WidgetFactory.showInformation(Studio.stage, "No matching files found in this archive.", "Extract the archive and upload the files separately.");
      }
      else {
        WidgetFactory.showInformation(Studio.stage, "The given file type is not supported for any upload.", null);
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
        return (UploaderAnalysis) results.get(0);
      }
      else {
        WidgetFactory.showAlert(parentStage != null ? parentStage : Studio.stage, "Error", "Error opening archive: Upload likely cancelled.");
      }
    }
    catch (Exception e) {
      LOG.error("Error opening archive: {}", e.getMessage(), e);
      WidgetFactory.showAlert(parentStage != null ? parentStage : Studio.stage, "Error", "Error opening archive: " + e.getMessage());
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
            WidgetFactory.showInformation(Studio.stage, "Can not apply a patch without a VPX table selected.", "Select the matching table for the patch file and try again.");
            return null;
          }
          TableDialogs.openPatchUpload(game, file, analysis, finalizer);
        }
        else if (assetTypes.size() == 1) {
          dispatchBySuffix(file, game, assetTypes.get(0), analysis, finalizer);
        }
        else {
          TableDialogs.openMediaUploadDialog(Studio.stage, game, file, analysis, null, -1);
        }
      }
      else {
        WidgetFactory.showInformation(Studio.stage, "A matching asset type could not be determined for this file.", null);
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
