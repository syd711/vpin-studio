package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.webhooks.WebhookSet;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSound2DuckingProfile;
import de.mephisto.vpin.restclient.altsound.AltSound2SampleType;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.restclient.assets.AssetRequest;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.restclient.webhooks.WebhookSettings;
import de.mephisto.vpin.ui.MediaPreviewController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.archiving.dialogs.*;
import de.mephisto.vpin.ui.backglassmanager.BackglassManagerController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.preferences.dialogs.IScoredGameRoomDialogController;
import de.mephisto.vpin.ui.preferences.dialogs.WebhooksDialogController;
import de.mephisto.vpin.ui.tables.dialogs.*;
import de.mephisto.vpin.ui.tables.editors.dialogs.AltSound2ProfileDialogController;
import de.mephisto.vpin.ui.tables.editors.dialogs.AltSound2SampleTypeDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;

public class TableDialogs {
  private final static Logger LOG = LoggerFactory.getLogger(TableDialogs.class);

  public static void directAssetUpload(Stage stage, GameRepresentation game, VPinScreen screen) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Media");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Files", MediaTypesSelector.getFileSelection(screen)));

    List<File> files = fileChooser.showOpenMultipleDialog(stage);
    if (files != null && !files.isEmpty()) {
      Platform.runLater(() -> {

        FrontendMediaRepresentation medias = client.getGameMediaService().getGameMedia(game.getId());
        boolean append = false;
        if (medias.getMediaItems(screen).size() > 0) {
          Optional<ButtonType> buttonType = WidgetFactory.showConfirmationWithOption(Studio.stage, "Replace Media?",
              "A media asset already exists.",
              "Append new asset or overwrite existing asset?", "Overwrite", "Append");
          if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
          }
          else if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
            append = true;
          }
          else {
            return;
          }
        }

        FrontendMediaUploadProgressModel model = new FrontendMediaUploadProgressModel(game,
            "Media Upload", files, screen, append);
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  public static void directAssetUpload(Stage stage, PlaylistRepresentation playlist, VPinScreen screen) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Media");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Files", MediaTypesSelector.getFileSelection(screen)));

    List<File> files = fileChooser.showOpenMultipleDialog(stage);
    if (files != null && !files.isEmpty()) {
      Platform.runLater(() -> {

        FrontendMediaRepresentation medias = client.getPlaylistMediaService().getPlaylistMedia(playlist.getId());
        boolean append = false;
        if (medias.getMediaItems(screen).size() > 0) {
          Optional<ButtonType> buttonType = WidgetFactory.showConfirmationWithOption(Studio.stage, "Replace Media?",
              "A media asset already exists.",
              "Append new asset or overwrite existing asset?", "Overwrite", "Append");
          if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
          }
          else if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
            append = true;
          }
          else {
            return;
          }
        }

        FrontendMediaUploadProgressModel model = new FrontendMediaUploadProgressModel(playlist,
            "Media Upload", files, screen, append);
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  public static void openCfgUploads(File file, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(CfgUploadController.class, "dialog-cfg-upload.fxml", "Config File Upload");
    CfgUploadController controller = (CfgUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    stage.showAndWait();
  }

  public static void openBamCfgUploads(File file, GameRepresentation game, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(BAMCfgUploadController.class, "dialog-bam-cfg-upload.fxml", "BAM .cfg File Upload");
    BAMCfgUploadController controller = (BAMCfgUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    controller.setGame(game);
    stage.showAndWait();
  }

  public static void openDirectb2sUploads(GameRepresentation game, File file, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(Directb2sUploadController.class, "dialog-directb2s-upload.fxml", "Backglass Upload");
    Directb2sUploadController controller = (Directb2sUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    controller.setData(game);
    stage.showAndWait();
  }

  public static void openPinVolSettings(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(PinVolSettingsDialogController.class, "dialog-pinvol-settings.fxml", "PinVol Settings");
    PinVolSettingsDialogController controller = (PinVolSettingsDialogController) stage.getUserData();
    controller.setData(stage, games);
    stage.showAndWait();
  }

  public static void openMetadataDialog(AssetRequest request) {
    Stage stage = Dialogs.createStudioDialogStage(AssetMetadataController.class, "dialog-asset-metadata.fxml", "Metadata for\"" + request.getName() + "\"");
    AssetMetadataController controller = (AssetMetadataController) stage.getUserData();
    controller.setData(request);
    stage.showAndWait();
  }

  public static void openNvRamUploads(File file, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(NvRamUploadController.class, "dialog-nvram-upload.fxml", "NvRAM Upload");
    NvRamUploadController controller = (NvRamUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    stage.showAndWait();
  }

  public static void onRomUploads(File file, Runnable finalizer) {
    TableDialogs.openRomUploadDialog(file, () -> {
      EventManager.getInstance().notifyTablesChanged();
      Platform.runLater(() -> {
        if (finalizer != null) {
          finalizer.run();
        }
      });
    });
  }

  public static void onMusicUploads(File file, UploaderAnalysis analysis, Runnable finalizer) {
    TableDialogs.openMusicUploadDialog(file, analysis, finalizer);
  }


  public static boolean directUpload(Stage stage, AssetType assetType, GameRepresentation game, Runnable finalizer) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select " + assetType.toString());
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter(assetType.toString(), assetType.installableExtension()));

    File file = fileChooser.showOpenDialog(stage);
    if (file != null && file.exists()) {
      UploadAnalysisDispatcher.dispatchFile(file, game, assetType, finalizer);
      return true;
    }
    return false;
  }

  public static void openBackglassUpload(@Nullable TablesController tablesController, Stage stage, GameRepresentation game, File file, Runnable finalizer) {
    String directB2SPath = game.getDirectB2SPath();
    if (directB2SPath != null) {
      TableDialogs.openDirectb2sUploads(game, file, finalizer);
    }
    else {
      if (file == null) {
        boolean b = TableDialogs.directUpload(stage, AssetType.DIRECTB2S, game, null);
        if (b) {
          tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
        }
      }
      else {
        directBackglassUpload(stage, game, file, finalizer);
      }
    }
  }

  public static boolean directBackglassUpload(Stage stage, GameRepresentation game, File file, Runnable finalizer) {
    if (file != null && file.exists()) {
      String help2 = null;
      if (game.getDirectB2SPath() != null) {
        help2 = "The existing directb2 file of this table will be overwritten.";
      }
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Upload", "Upload backglass for \"" + game.getGameDisplayName() + "\"?", help2);
      if (result.get().equals(ButtonType.OK)) {
        DirectB2SUploadProgressModel model = new DirectB2SUploadProgressModel(game.getId(), "DirectB2S Upload", file, false, finalizer);
        ProgressDialog.createProgressDialog(model);
        return true;
      }
    }
    return false;
  }

  public static boolean directResUpload(Stage stage, GameRepresentation game, File file, Runnable finalizer) {
    if (file != null && file.exists()) {
      String help2 = null;
      if (game.getDirectB2SPath() != null) {
        help2 = "The existing .res file of this table will be overwritten.";
      }
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Upload", "Upload .res file for \"" + game.getGameDisplayName() + "\"?", help2);
      if (result.get().equals(ButtonType.OK)) {
        Platform.runLater(() -> {
          ResUploadProgressModel model = new ResUploadProgressModel(game.getId(), "Res File Upload", file, finalizer);
          ProgressDialog.createProgressDialog(model);
        });
        return true;
      }
    }
    return false;
  }

  public static boolean directIniUpload(Stage stage, GameRepresentation game, File file, Runnable finalizer) {
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        String help2 = null;
        if (game.getIniPath() != null) {
          help2 = "The existing .ini file of this table will be overwritten.";
        }
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Upload", "Upload .ini file for \"" + game.getGameDisplayName() + "\"?", help2);
        if (result.get().equals(ButtonType.OK)) {
          IniUploadProgressModel model = new IniUploadProgressModel(game.getId(), "Ini Upload", file, finalizer);
          ProgressDialog.createProgressDialog(model);
        }
      });
      return true;
    }
    return false;
  }

  public static boolean directBamCfgUpload(Stage stage, GameRepresentation game, File file, Runnable finalizer) {
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        String help2 = null;
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Upload", "Upload BAM .cfg file for \"" + game.getGameDisplayName() + "\"?", help2);
        if (result.get().equals(ButtonType.OK)) {
          BamCfgUploadProgressModel model = new BamCfgUploadProgressModel("BAM .cfg Upload", Arrays.asList(file), game.getId(), finalizer);
          ProgressDialog.createProgressDialog(model);
        }
      });
      return true;
    }
    return false;
  }

  public static boolean directPovUpload(Stage stage, GameRepresentation game, File file, Runnable finalizer) {
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        String help2 = null;
        if (game.getPovPath() != null) {
          help2 = "The existing .pov file of this table will be overwritten.";
        }
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Upload", "Upload .pov file for \"" + game.getGameDisplayName() + "\"?", help2);
        if (result.get().equals(ButtonType.OK)) {
          PovUploadProgressModel model = new PovUploadProgressModel(game.getId(), "POV Upload", file, finalizer);
          ProgressDialog.createProgressDialog(model);
        }
      });
      return true;
    }
    return false;
  }

  public static boolean openTableAssetsDialog(TableOverviewController overviewController, GameRepresentation game, VPinScreen screen) {
    if (TableAssetManagerDialogController.INSTANCE != null) {
      return true;
    }

    Stage stage = Dialogs.createStudioDialogStage(Studio.stage, TableAssetManagerDialogController.class, "dialog-table-asset-manager.fxml", "Asset Manager", TableAssetManagerDialogController.MODAL_STATE_ID);
    TableAssetManagerDialogController controller = (TableAssetManagerDialogController) stage.getUserData();
    controller.loadAllTables(game.getEmulatorId());
    controller.setGame(stage, overviewController, game, screen, false);

    FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
    stage.setUserData(fxResizeHelper);
    stage.setMinWidth(860);
    stage.setMinHeight(600);

    stage.showAndWait();
    return true;
  }

  public static boolean openTableAssetsDialog(TableOverviewController overviewController, GameRepresentation game, PlaylistRepresentation playlist, VPinScreen screen) {
    if (TableAssetManagerDialogController.INSTANCE != null) {
      return true;
    }

    Stage stage = Dialogs.createStudioDialogStage(Studio.stage, TableAssetManagerDialogController.class, "dialog-table-asset-manager.fxml", "Asset Manager", TableAssetManagerDialogController.MODAL_STATE_ID);
    TableAssetManagerDialogController controller = (TableAssetManagerDialogController) stage.getUserData();
    controller.loadAllTables(game != null ? game.getEmulatorId() : -1);
    controller.setStage(stage);
    controller.setPlaylistMode();
    controller.setPlaylist(stage, overviewController, playlist, screen);

    stage.showAndWait();
    return true;
  }

  public static boolean openHighscoresAdminDialog(TablesSidebarController tablesSidebarController, GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(TableHighscoresAdminController.class, "dialog-highscores-admin.fxml", "Archived Highscores \"" + game.getGameDisplayName() + "\"");
    TableHighscoresAdminController controller = (TableHighscoresAdminController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();
    return true;
  }

  public static boolean openAltColorAdminDialog(TablesSidebarController tablesSidebarController, GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(TableAltColorAdminController.class, "dialog-altcolor-admin.fxml", "ALT Colors for \"" + game.getGameDisplayName() + "\"");
    TableAltColorAdminController controller = (TableAltColorAdminController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();
    return true;
  }

  public static boolean openHighscoresResetDialog(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(HighscoreResetController.class, "dialog-highscore-reset.fxml", "Reset Highscores");
    HighscoreResetController controller = (HighscoreResetController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
    return true;
  }


  public static boolean openCommentDialog(TableOverviewController overviewController, GameRepresentation game) {
    openTableDataDialog(overviewController, game, 2);
    return true;
  }


  public static boolean openEventLogDialog(GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(EventLogController.class, "dialog-event-log.fxml", "Event Log", "eventLog");
    EventLogController controller = (EventLogController) stage.getUserData();
    controller.setGame(game);

    FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
    stage.setUserData(fxResizeHelper);
    stage.setMinWidth(800);
    stage.setMinHeight(600);

    stage.showAndWait();

    return true;
  }

  public static void openDMDPositionDialog(GameRepresentation game, @Nullable BackglassManagerController backglassMgrController) {
    Stage stage = Dialogs.createStudioDialogStage(DMDPositionController.class, "dialog-dmd-position.fxml", "DMD Position");
    DMDPositionController controller = (DMDPositionController) stage.getUserData();
    controller.setGame(game, backglassMgrController);
    stage.showAndWait();
  }

  public static void openAltSoundUploadDialog(GameRepresentation game, File file, UploaderAnalysis analysis, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(AltSoundUploadController.class, "dialog-altsound-upload.fxml", "ALT Sound Upload");
    AltSoundUploadController controller = (AltSoundUploadController) stage.getUserData();
    controller.setData(stage, file, game, analysis, finalizer);
    stage.showAndWait();
  }

  public static AltSound2DuckingProfile openAltSound2ProfileEditor(AltSound altSound, AltSound2DuckingProfile profile) {
    Stage stage = Dialogs.createStudioDialogStage(AltSound2ProfileDialogController.class, "dialog-altsound2-profile.fxml", "Edit Ducking Profile");
    AltSound2ProfileDialogController controller = (AltSound2ProfileDialogController) stage.getUserData();
    controller.setProfile(altSound, profile);
    stage.showAndWait();

    return controller.editingFinished();
  }

  public static void openAltSound2SampleTypeDialog(AltSound altSound, AltSound2SampleType sampleType) {
    Stage stage = Dialogs.createStudioDialogStage(AltSound2SampleTypeDialogController.class, "dialog-altsound2-sample-type.fxml", "Sample Type Settings");
    AltSound2SampleTypeDialogController controller = (AltSound2SampleTypeDialogController) stage.getUserData();
    controller.setProfile(altSound, sampleType);
    stage.showAndWait();
  }

  public static void openAltColorUploadDialog(GameRepresentation game, File file, UploaderAnalysis analysis, Runnable finalizer) {
    if (StringUtils.isEmpty(game.getRom())) {
      WidgetFactory.showAlert(Studio.stage, "No ROM", "Table \"" + game.getGameDisplayName() + "\" has no ROM name set.", "The ROM name is required for this upload type.");
    }

    Stage stage = Dialogs.createStudioDialogStage(AltColorUploadController.class, "dialog-altcolor-upload.fxml", "ALT Color Upload");
    AltColorUploadController controller = (AltColorUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setFile(stage, file, analysis, finalizer);
    stage.showAndWait();
  }

  public static void openPupPackUploadDialog(GameRepresentation game, File file, UploaderAnalysis analysis, Runnable finalizer) {
    if (StringUtils.isEmpty(game.getRom())) {
      WidgetFactory.showAlert(Studio.stage, "No ROM", "Table \"" + game.getGameDisplayName() + "\" has no ROM name set.", "The ROM name is required for this upload type.");
    }

    Stage stage = Dialogs.createStudioDialogStage(PupPackUploadController.class, "dialog-puppack-upload.fxml", "PUP Pack Upload");
    PupPackUploadController controller = (PupPackUploadController) stage.getUserData();
    controller.setFile(stage, file, analysis, finalizer);
    stage.showAndWait();
  }

  public static void openDMDUploadDialog(GameRepresentation game, File file, UploaderAnalysis analysis, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(DMDUploadController.class, "dialog-dmd-upload.fxml", "DMD Bundle Upload");
    DMDUploadController controller = (DMDUploadController) stage.getUserData();
    controller.setData(stage, file, game, analysis, finalizer);
    stage.showAndWait();
  }

  public static boolean openMediaUploadDialog(Stage parent, @Nullable GameRepresentation game, File file, @Nullable UploaderAnalysis analysis, @Nullable AssetType filterMode, int emulatorId) {
    String title = "Media Pack";
    if (game != null) {
      title = "Media for \"" + game.getGameDisplayName() + "\"";
    }
    if (filterMode != null) {
      title = "Media Selection";
    }
    Stage stage = Dialogs.createStudioDialogStage(parent, MediaUploadController.class, "dialog-media-upload.fxml", title, null);
    MediaUploadController controller = (MediaUploadController) stage.getUserData();
    controller.setData(game, analysis, file, stage, filterMode, emulatorId);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static Optional<UploadDescriptor> openTableUploadDialog(@Nullable GameRepresentation game, @Nullable EmulatorType emutype, @Nullable UploadType uploadType, UploaderAnalysis analysis) {
    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getEmulatorService().getGameEmulatorsByType(emutype);
    if (gameEmulators.isEmpty()) {
      WidgetFactory.showAlert(Studio.stage, "Error", "No game emulator found.");
      return Optional.empty();
    }

    Stage stage = Dialogs.createStudioDialogStage(TableUploadController.class, "dialog-table-upload.fxml", emutype.shortName() + " Table Upload");
    TableUploadController controller = (TableUploadController) stage.getUserData();
    controller.setGame(stage, game, uploadType, analysis);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openTableDeleteDialog(TableOverviewController tableOverviewController, List<GameRepresentation> selectedGames, List<GameRepresentation> allGames) {
    Stage stage = Dialogs.createStudioDialogStage(TableDeleteController.class, "dialog-table-delete.fxml", "Delete");
    TableDeleteController controller = (TableDeleteController) stage.getUserData();
    controller.setGames(tableOverviewController, selectedGames, allGames);
    stage.showAndWait();
  }

  public static void openConverterDialog(List<GameRepresentation> selectedGames) {
    Stage stage = Dialogs.createStudioDialogStage(MediaConverterDialogController.class, "dialog-media-converter.fxml", "Media Conversion");
    MediaConverterDialogController controller = (MediaConverterDialogController) stage.getUserData();
    controller.setGames(selectedGames);
    stage.showAndWait();
  }


  public static TableDetails openAutoFillSettingsDialog(Stage stage, List<GameRepresentation> games, TableDetails tableDetails) {
    return openAutoFillSettingsDialog(stage, games, tableDetails, null, null);
  }

  public static TableDetails openAutoFillSettingsDialog(Stage stage, List<GameRepresentation> games, TableDetails tableDetails, @Nullable String vpsTableId, @Nullable String vpsVersionId) {
    Stage dialogStage = Dialogs.createStudioDialogStage(stage, AutoFillSelectionController.class, "dialog-autofill-settings.fxml", "Auto-Fill Settings", null);
    AutoFillSelectionController controller = (AutoFillSelectionController) dialogStage.getUserData();
    controller.setData(games, tableDetails, vpsTableId, vpsVersionId);
    dialogStage.showAndWait();
    return controller.getTableDetails();
  }

  public static void openAutoMatchAll(List<GameRepresentation> games) {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, "Auto-Match table and version for all " + games.size() + " tables?",
            "Cancel", "Continue", "The table and display name is used to find the matching table.", "You may have to adept the result manually.", "Overwrite existing matchings", false);
        if (!result.isApplyClicked()) {
          ProgressDialog.createProgressDialog(new TableVpsDataAutoMatchProgressModel(games, result.isChecked(), false));
          EventManager.getInstance().notifyTablesChanged();
        }
      }
    }
    else {
      ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, "Auto-Match table and version for all " + games.size() + " tables?",
          "Cancel", "Continue", "The table and display name is used to find the matching table.", "You may have to adept the result manually.", "Overwrite existing matchings", false);
      if (!result.isApplyClicked()) {
        ProgressDialog.createProgressDialog(new TableVpsDataAutoMatchProgressModel(games, result.isChecked(), false));
        EventManager.getInstance().notifyTablesChanged();
      }
    }
  }

  public static boolean openAutoMatch(List<GameRepresentation> games) {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        return onOpenAutoMatch(games);
      }
    }
    else {
      return onOpenAutoMatch(games);
    }
    return false;
  }

  private static boolean onOpenAutoMatch(List<GameRepresentation> games) {
    String title = "Auto-Match table and version for " + games.size() + " tables?";
    if (games.size() == 1) {
      title = "Auto-Match table and version for \"" + games.get(0).getGameDisplayName() + "\"?";
    }

    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
        "This will overwrite the existing mapping.", "This action will overwrite the VPS table and version IDs fields.", "Auto-Match");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(new TableVpsDataAutoMatchProgressModel(games, true, false));
      return true;
    }
    return false;
  }

  public static boolean openScanAllDialog(List<GameRepresentation> games) {
    String title = "Re-scan all " + games.size() + " tables?";
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
        "Scanning will try to resolve ROM and highscore file names of the selected tables.", null, "Start Scan");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(ClearCacheProgressModel.getFullClearCacheModel());
      ProgressDialog.createProgressDialog(new TableScanProgressModel("Scanning Tables", games));
      return true;
    }
    return false;
  }

  public static void openTableDataDialog(@Nullable TableOverviewController overviewController, GameRepresentation game) {
    openTableDataDialog(overviewController, game, TableDataController.lastTab);
  }

  public static void openTableDataDialog(@Nullable TableOverviewController overviewController, GameRepresentation game, int tab) {
    try {
      Stage stage = Dialogs.createStudioDialogStage(TableDataController.class, "dialog-table-data.fxml", "Table Data Manager", "tableDataManager");
      TableDataController controller = (TableDataController) stage.getUserData();
      controller.setGame(stage, overviewController, game, tab);

      FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6, true);
      stage.setUserData(fxResizeHelper);
      stage.setMinWidth(812);
      stage.setMaxWidth(812);
      stage.setMaxHeight(1060);
      stage.setMinHeight(TableDataController.MIN_HEIGHT);

      stage.showAndWait();
    }
    catch (Exception e) {
      LOG.error("Failed to open table data manager: " + e.getMessage(), e);
    }
  }

  public static void openTablesBackupDialog(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(TablesBackupController.class, "dialog-tables-backup.fxml", "Table Backup");
    TablesBackupController controller = (TablesBackupController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
  }

  public static void openTableImportDialog(GameEmulatorRepresentation emulatorRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(TableImportController.class, "dialog-table-import.fxml", "Table Importer");
    TableImportController controller = (TableImportController) stage.getUserData();
    controller.setData(emulatorRepresentation);
    stage.showAndWait();
  }

  public static void openTableInstallationDialog(TablesController tablesController, List<ArchiveDescriptorRepresentation> descriptorRepresentations) {
    Stage stage = Dialogs.createStudioDialogStage(TableRestoreController.class, "dialog-table-restore.fxml", "Restore Tables");
    TableRestoreController controller = (TableRestoreController) stage.getUserData();
    controller.setData(tablesController, descriptorRepresentations);
    stage.showAndWait();
  }

  public static ArchiveSourceRepresentation openArchiveSourceFileDialog(ArchiveSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveSourceFileDialogController.class, "dialog-archive-source-file.fxml", "Folder Repository");
    ArchiveSourceFileDialogController controller = (ArchiveSourceFileDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();
    return controller.getArchiveSource();
  }

  public static void openWebhooksDialog(@NonNull WebhookSettings settings, @Nullable WebhookSet set) {
    Stage stage = Dialogs.createStudioDialogStage(WebhooksDialogController.class, "dialog-webhook-set.fxml", "Webhook Set");
    WebhooksDialogController controller = (WebhooksDialogController) stage.getUserData();
    controller.setData(settings, set);
    stage.showAndWait();
  }

  public static boolean openIScoredGameRoomDialog(@NonNull IScoredSettings settings, @Nullable IScoredGameRoom gameRoom) {
    Stage stage = Dialogs.createStudioDialogStage(IScoredGameRoomDialogController.class, "dialog-iscored-gameroom.fxml", "iScored Game Room");
    IScoredGameRoomDialogController controller = (IScoredGameRoomDialogController) stage.getUserData();
    controller.setData(settings, gameRoom);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ArchiveSourceRepresentation openArchiveSourceHttpDialog(ArchiveSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveSourceHttpDialogController.class, "dialog-archive-source-http.fxml", "HTTP Repository");
    ArchiveSourceHttpDialogController controller = (ArchiveSourceHttpDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();

    return controller.getArchiveSource();
  }

  public static void openVPSAssetsDialog(GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(VPSAssetsDialogController.class, "dialog-vps-assets.fxml", "Virtual Pinball Spreadsheet Assets");
    VPSAssetsDialogController controller = (VPSAssetsDialogController) stage.getUserData();
    controller.setGame(stage, game);
    stage.showAndWait();
  }

  public static boolean openArchiveUploadDialog() {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveUploadController.class, "dialog-archive-upload.fxml", "Upload");
    ArchiveUploadController controller = (ArchiveUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openArchiveDownloadDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveDownloadDialogController.class, "dialog-archive-download.fxml", "Archive Download");
    ArchiveDownloadDialogController controller = (ArchiveDownloadDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static void openVpbmArchiveBundleDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(VpbmArchiveBundleDialogController.class, "dialog-vpbm-bundle-download.fxml", "Archive Bundle");
    VpbmArchiveBundleDialogController controller = (VpbmArchiveBundleDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static void openVpaArchiveBundleDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(VpaArchiveBundleDialogController.class, "dialog-vpa-bundle-download.fxml", "Archive Bundle");
    VpaArchiveBundleDialogController controller = (VpaArchiveBundleDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static void openCopyArchiveToRepositoryDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(CopyArchiveToRepositoryDialogController.class, "dialog-copy-archive-to-repository.fxml", "Copy To Repository");
    CopyArchiveToRepositoryDialogController controller = (CopyArchiveToRepositoryDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static void openRomUploadDialog(File file, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(ROMUploadController.class, "dialog-rom-upload.fxml", "Rom Upload");
    ROMUploadController controller = (ROMUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    stage.showAndWait();
  }

  public static void openPatchUpload(GameRepresentation gameRepresentation, File file, UploaderAnalysis analysis, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(PatchUploadController.class, "dialog-patch-upload.fxml", "Patch Upload");
    PatchUploadController controller = (PatchUploadController) stage.getUserData();
    controller.setFile(stage, file, analysis, finalizer);
    controller.setData(gameRepresentation);
    stage.showAndWait();
  }

  public static void openMusicUploadDialog(File file, UploaderAnalysis analysis, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(MusicUploadController.class, "dialog-music-upload.fxml", "Music Upload");
    MusicUploadController controller = (MusicUploadController) stage.getUserData();
    controller.setFile(stage, file, analysis, finalizer);
    stage.showAndWait();
  }

  public static boolean openValidationDialog(List<GameRepresentation> selectedItems, boolean reload) {
    if (selectedItems.isEmpty()) {
      return false;
    }
    String title = "Re-validate " + selectedItems.size() + " tables?";
    if (selectedItems.size() == 1) {
      title = "Re-validate table \"" + selectedItems.get(0).getGameDisplayName() + "\"?";
    }

    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
        "This will reset the dismissed validations for this table too.", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      title = "Re-validating " + selectedItems.size() + " tables";
      if (selectedItems.size() == 1) {
        title = "Re-validating table \"" + selectedItems.get(0).getGameDisplayName() + "\"";
      }

      ProgressDialog.createProgressDialog(new TableValidateProgressModel(title, selectedItems, reload));
      return true;
    }
    return false;
  }

  public static void openDismissAllDialog(GameRepresentation gameRepresentation) {
    Stage stage = WidgetFactory.createDialogStage(DismissAllController.class, Studio.stage, "Dismiss Validation Errors", "dialog-dismiss-all.fxml");
    DismissAllController controller = (DismissAllController) stage.getUserData();
    controller.setGame(gameRepresentation);
    stage.showAndWait();
  }

  public static boolean openDefaultBackgroundUploadDialog(GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(DefaultBackgroundUploadController.class, "dialog-background-picture-upload.fxml", "Default Background Upload");
    DefaultBackgroundUploadController controller = (DefaultBackgroundUploadController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openMediaDialog(Stage parent, GameRepresentation game, FrontendMediaItemRepresentation item) {
    Stage stage = Dialogs.createStudioDialogStage(parent, MediaPreviewController.class, "dialog-media-preview.fxml", game.getGameDisplayName() + " - " + item.getScreen() + " Screen", "dialog-media-preview");
    MediaPreviewController controller = (MediaPreviewController) stage.getUserData();
    controller.setData(stage, game, item);

    FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
    stage.setUserData(fxResizeHelper);
    stage.setMinWidth(800);
    stage.setMinHeight(600);

    stage.showAndWait();
  }
}
