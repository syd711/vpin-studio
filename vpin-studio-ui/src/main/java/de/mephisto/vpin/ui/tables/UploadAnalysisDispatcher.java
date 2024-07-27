package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class UploadAnalysisDispatcher {
  private final static Logger LOG = LoggerFactory.getLogger(UploadAnalysisDispatcher.class);

  public static void dispatch(@NonNull TablesSidebarController tablesController, @NonNull File file, @Nullable GameRepresentation game) {
    String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
    AssetType assetType = null;
    try {
      assetType = AssetType.valueOf(extension.toUpperCase());
    }
    catch (IllegalArgumentException e) {
      LOG.error("Unsupported upload type: " + assetType);
      Platform.runLater(() -> {
        WidgetFactory.showInformation(Studio.stage, "The given file type is not supported for any upload.", null);
      });
      return;
    }


    if (PackageUtil.isSupportedArchive(extension)) {
      validateArchive(tablesController, file, game);
    }
    else {
      UploaderAnalysis analysis = new UploaderAnalysis(file);
      dispatchBySuffix(tablesController, file, game, assetType, analysis);
    }
  }

  private static void dispatchBySuffix(@NonNull TablesSidebarController tablesController, @NonNull File file, @Nullable GameRepresentation game, AssetType assetType, UploaderAnalysis analysis) {
    switch (assetType) {
      case ROM: {
        TableDialogs.onRomUploads(file);
        return;
      }
      case NV: {
        TableDialogs.openNvRamUploads(file);
        return;
      }
      case CFG: {
        TableDialogs.openCfgUploads(file);
        return;
      }
      case DMD_PACK: {
        TableDialogs.openDMDUploadDialog(game, file, analysis);
        return;
      }
      case ALT_SOUND: {
        TableDialogs.openAltSoundUploadDialog(file, analysis, game != null ? game.getId() : -1);
        return;
      }
      case VPX: {
        TableDialogs.openTableUploadDialog(tablesController.getTableOverviewController(), game, TableUploadType.uploadAndImport, analysis);
        return;
      }
    }

    if (game == null) {
      WidgetFactory.showInformation(Studio.stage, "The given file can not be uploaded without a table selection.", null);
      return;
    }

    switch (assetType) {
      case DIRECTB2S: {
        TableDialogs.directBackglassUpload(Studio.stage, game, file);
        return;
      }
      case RES: {
        TableDialogs.directResUpload(Studio.stage, game, file);
        return;
      }
      case INI: {
        TableDialogs.directIniUpload(Studio.stage, game, file);
        break;
      }
      case POV: {
        TableDialogs.directPovUpload(Studio.stage, game, file);
        break;
      }
      case ALT_COLOR:
      case PAC:
      case PAL:
      case VNI:
      case CRZ: {
        TableDialogs.openAltColorUploadDialog(game, file);
        break;
      }
      case MUSIC: {
        TableDialogs.openMusicUploadDialog(file, analysis);
        break;
      }
      case PUP_PACK: {
        TableDialogs.openPupPackUploadDialog(game, file, analysis);
        break;
      }
      case POPPER_MEDIA: {
        TableDialogs.openMediaUploadDialog(game, file, analysis);
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

  private static boolean isArchive(File file) {
    String extension = FilenameUtils.getExtension(file.getName());
    return PackageUtil.isSupportedArchive(extension);
  }

  public static UploaderAnalysis analyzeArchive(File file) {
    try {
      ProgressModel model = createProgressModel(file);
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(model);
      return (UploaderAnalysis) progressDialog.getResults().get(0);
    }
    catch (Exception e) {
      LOG.error("Error opening archive: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Error opening archive: " + e.getMessage());
    }
    return null;
  }


  public static String validateArchive(File file, AssetType assetType) {
    try {
      ProgressModel model = createProgressModel(file);
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(model);
      UploaderAnalysis analysis = (UploaderAnalysis) progressDialog.getResults().get(0);
      return analysis.validateAssetType(assetType);
    }
    catch (Exception e) {
      LOG.error("Error opening archive: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Error opening archive: " + e.getMessage());
    }
    return null;
  }

  public static String validateArchive(@Nullable TablesSidebarController tablesSidebarController, File file, GameRepresentation game) {
    try {
      ProgressModel model = createProgressModel(file);
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(model);
      UploaderAnalysis analysis = (UploaderAnalysis) progressDialog.getResults().get(0);
      AssetType singleAssetType = analysis.getSingleAssetType();
      if (singleAssetType != null) {
        String s = analysis.validateAssetType(singleAssetType);
        if (s == null) {
          dispatchBySuffix(tablesSidebarController, file, game, singleAssetType, analysis);
        }
        else {
          WidgetFactory.showAlert(Studio.stage, "Invalid", "The selected file is not valid.", s);
        }
      }
      else {
        WidgetFactory.showInformation(Studio.stage, "A matching asset type could not be determined for this file.", null);
      }
    }
    catch (Exception e) {
      LOG.error("Error creating UploadDispatchAnalysisZipProgressModel: " + e.getMessage(), e);
    }
    return null;
  }

  private static ProgressModel createProgressModel(File file) throws IOException {
    String suffix = FilenameUtils.getExtension(file.getName());
    if (suffix.equalsIgnoreCase("rar")) {
      return new UploadDispatchAnalysisRarProgressModel(file);
    }
    if (suffix.equalsIgnoreCase("zip")) {
      return new UploadDispatchAnalysisZipProgressModel(file);
    }
    throw new UnsupportedOperationException("Unsupported file format '" + suffix + "'");
  }
}
