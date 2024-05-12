package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class UploadAnalysisDispatcher {
  private final static Logger LOG = LoggerFactory.getLogger(UploadAnalysisDispatcher.class);

  public static void dispatch(@NonNull TablesSidebarController tablesController, @NonNull File file, @Nullable GameRepresentation game) {
    String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
    AssetType assetType = AssetType.valueOf(extension.toUpperCase());
    if (assetType == null) {
      WidgetFactory.showInformation(Studio.stage, "The given file type is not supported for any upload.", null);
      return;
    }

    if (extension.equals("zip")) {
      validateArchive(tablesController, file, game);
    }
    else {
      UploaderAnalysis analysis = new UploaderAnalysis(game, file);
      dispatchBySuffix(tablesController, file, game, assetType, analysis);
    }
  }

  private static void dispatchBySuffix(@NonNull TablesSidebarController tablesController, @NonNull File file, @Nullable GameRepresentation game, AssetType assetType, UploaderAnalysis analysis) {
    switch (assetType) {
      case ROM: {
        TableDialogs.onRomUploads(tablesController, file);
        return;
      }
      case VPX: {
        TableDialogs.openTableUploadDialog(tablesController.getTablesController(), game, TableUploadType.uploadAndImport, analysis);
        return;
      }
    }

    if (game == null) {
      WidgetFactory.showInformation(Studio.stage, "The given file can not be uploaded without a table selection.", null);
      return;
    }

    switch (assetType) {
      case ALT_SOUND: {
        TableDialogs.openAltSoundUploadDialog(tablesController, game, file);
        return;
      }
      case DIRECTB2S: {
        TableDialogs.directBackglassUpload(Studio.stage, game, file);
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
        TableDialogs.openAltColorUploadDialog(tablesController, game, file);
        break;
      }
      case MUSIC: {
        TableDialogs.openMusicUploadDialog(file);
        break;
      }
      case PUP_PACK: {
        TableDialogs.openPupPackUploadDialog(tablesController, game, file);
        break;
      }
      default: {
        showDefault(file);
      }
    }

  }

  private static void showDefault(@NonNull File file) {
    if (isArchive(file)) {
      WidgetFactory.showInformation(Studio.stage, "No matching files found in this archive.", "Extract the archive and upload the files separately.");
    }
    else {
      WidgetFactory.showInformation(Studio.stage, "The given file type is not supported for any upload.", null);
    }
  }

  private static boolean isArchive(File file) {
    String extension = FilenameUtils.getExtension(file.getName());
    return extension.equalsIgnoreCase("zip") || extension.equalsIgnoreCase("7z");
  }

  public static UploaderAnalysis analyzeArchive(File file, GameRepresentation game) {
    try {
      UploadDispatchAnalysisZipProgressModel model = new UploadDispatchAnalysisZipProgressModel(game, file);
      ProgressDialog.createProgressDialog(model);
      return model.getAnalysis();
    }
    catch (Exception e) {
      LOG.error("Error opening archive: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Error opening archive: " + e.getMessage());
    }
    return null;
  }


  public static String validateArchive(File file, GameRepresentation game, AssetType assetType) {
    try {
      UploadDispatchAnalysisZipProgressModel model = new UploadDispatchAnalysisZipProgressModel(game, file);
      ProgressDialog.createProgressDialog(model);
      return model.getAnalysis().validateAssetType(assetType);
    }
    catch (Exception e) {
      LOG.error("Error opening archive: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Error opening archive: " + e.getMessage());
    }
    return null;
  }

  public static String validateArchive(@Nullable TablesSidebarController tablesSidebarController, File file, GameRepresentation game) {
    try {
      UploadDispatchAnalysisZipProgressModel model = new UploadDispatchAnalysisZipProgressModel(game, file);
      ProgressDialog.createProgressDialog(model);
      UploaderAnalysis analysis = model.getAnalysis();
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
}
