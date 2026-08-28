package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSound2DuckingProfile;
import de.mephisto.vpin.restclient.altsound.AltSound2SampleType;
import de.mephisto.vpin.restclient.assets.AssetMetaData;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.restclient.webhooks.WebhookSet;
import de.mephisto.vpin.restclient.webhooks.WebhookSettings;
import de.mephisto.vpin.ui.MediaPreviewController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.preferences.dialogs.IScoredGameRoomDialogController;
import de.mephisto.vpin.ui.preferences.dialogs.WebhooksDialogController;
import de.mephisto.vpin.ui.tables.dialogs.*;
import de.mephisto.vpin.ui.tables.editors.dialogs.AltSound2ProfileDialogController;
import de.mephisto.vpin.ui.tables.editors.dialogs.AltSound2SampleTypeDialogController;
import de.mephisto.vpin.ui.tables.panels.BaseGameModel;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class TableDialogs {
  private final static Logger LOG = LoggerFactory.getLogger(TableDialogs.class);

  public static void directAssetUpload(Stage stage, GameRepresentation game, VPinScreen screen) {
    directAssetUpload(stage, game.getId(), false, screen);
  }

  public static void directAssetUpload(Stage stage, PlaylistRepresentation playlist, VPinScreen screen) {
    directAssetUpload(stage, playlist.getId(), true, screen);
  }

  public static void directAssetUpload(Stage stage, int id, boolean playlistMode, VPinScreen screen) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle(Messages.get("dialog.select_media"));
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Files", MediaTypesSelector.getFileSelection(screen)));

    List<File> files = fileChooser.showOpenMultipleDialog(stage);
    if (files != null && !files.isEmpty()) {
      Platform.runLater(() -> {
        VPinScreen loadingScreenId = null;
        FrontendMediaRepresentation medias = client.getGameMediaService().getMedia(id, playlistMode);
        boolean append = false;
        if (medias.getMediaItems(screen).size() > 0) {
          Optional<ButtonType> buttonType = WidgetFactory.showConfirmationWithOption(Studio.stage, Messages.get("dialog.replace_media"),
              Messages.get("dialog.a_media_asset_already_exists"),
              Messages.get("dialog.append_new_asset_or_overwrite_existing_asset"), Messages.get("dialog.overwrite"), Messages.get("dialog.append"));
          if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
          }
          else if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
            append = true;

            if (screen.equals(VPinScreen.Loading) && client.getFrontendService().getFrontendType().equals(FrontendType.Popper)) {
              VPinScreen vPinScreen = TableDialogs.openAssetScreenAssignmentDialog();
              if (vPinScreen != null) {
                loadingScreenId = vPinScreen;
              }
            }
          }
          else {
            return;
          }
        }

        FrontendMediaUploadProgressModel model = new FrontendMediaUploadProgressModel(id, playlistMode,
            Messages.get("dialog.media_upload"), files, screen, append, loadingScreenId);
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  public static VPinScreen openAssetScreenAssignmentDialog() {
    Stage stage = Dialogs.createStudioDialogStage(LoadingAsset2ScreenAssignmentController.class, "dialog-loading-asset-assignment.fxml", Messages.get("dialog.loading_screen_assignment"));
    LoadingAsset2ScreenAssignmentController controller = (LoadingAsset2ScreenAssignmentController) stage.getUserData();
    stage.showAndWait();

    return controller.getScreen();
  }

  public static void openCfgUploads(File file, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(CfgUploadController.class, "dialog-cfg-upload.fxml", Messages.get("dialog.config_file_upload"));
    CfgUploadController controller = (CfgUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    stage.showAndWait();
  }

  public static void openBamCfgUploads(File file, GameRepresentation game, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(BAMCfgUploadController.class, "dialog-bam-cfg-upload.fxml", Messages.get("dialog.bam_cfg_file_upload"));
    BAMCfgUploadController controller = (BAMCfgUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    controller.setGame(game);
    stage.showAndWait();
  }

  public static void openDirectb2sUploads(GameRepresentation game, File file, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(Directb2sUploadController.class, "dialog-directb2s-upload.fxml", Messages.get("dialog.backglass_upload"));
    Directb2sUploadController controller = (Directb2sUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    controller.setData(game);
    stage.showAndWait();
  }

  public static void openPinVolSettings(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(PinVolSettingsDialogController.class, "dialog-pinvol-settings.fxml", Messages.get("dialog.pinvol_settings"));
    PinVolSettingsDialogController controller = (PinVolSettingsDialogController) stage.getUserData();
    controller.setData(stage, games);
    stage.showAndWait();
  }

  public static void openMetadataDialog(AssetMetaData metadata, String filename) {
    Stage stage = Dialogs.createStudioDialogStage(AssetMetadataController.class, "dialog-asset-metadata.fxml", Messages.get("dialog.metadata_for") + filename + "\"");
    AssetMetadataController controller = (AssetMetadataController) stage.getUserData();
    controller.setData(metadata);
    stage.showAndWait();
  }

  public static void openNvRamUploads(File file, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(NvRamUploadController.class, "dialog-nvram-upload.fxml", Messages.get("dialog.nvram_upload"));
    NvRamUploadController controller = (NvRamUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    stage.showAndWait();
  }

  public static void openFplUploads(File file, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(NvRamUploadController.class, "dialog-fpl-upload.fxml", Messages.get("dialog.fpl_file_upload"));
    FplUploadController controller = (FplUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    stage.showAndWait();
  }

  public static void onRomUploads(int emulatorId, File file, Runnable finalizer) {
    GameEmulatorRepresentation gameEmulator = client.getEmulatorService().getGameEmulator(emulatorId);
    onRomUploads(gameEmulator, file, finalizer);
  }

  public static void onRomUploads(GameEmulatorRepresentation emulator, File file, Runnable finalizer) {
    TableDialogs.openRomUploadDialog(emulator, file, () -> {
      EventManager.getInstance().notifyTablesChanged();
      Platform.runLater(() -> {
        if (finalizer != null) {
          finalizer.run();
        }
      });
    });
  }

  public static void onMusicUploads(File file, UploaderAnalysis analysis, int gameId, Runnable finalizer) {
    TableDialogs.openMusicUploadDialog(file, analysis, gameId, finalizer);
  }


  public static boolean directUpload(Stage stage, AssetType assetType, GameRepresentation game, Runnable finalizer) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle(Messages.get("dialog.select") + assetType.toString());
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
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, Messages.get("dialog.upload"), Messages.get("dialog.upload_backglass_for") + game.getGameDisplayName() + "\"?", help2);
      if (result.get().equals(ButtonType.OK)) {
        DirectB2SUploadProgressModel model = new DirectB2SUploadProgressModel(game.getId(), Messages.get("dialog.directb2s_upload"), file, false);
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);
        if (/*resultModel.isSuccess() &&*/ finalizer != null) {
          finalizer.run();
        }
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
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, Messages.get("dialog.upload"), Messages.get("dialog.upload_res_file_for") + game.getGameDisplayName() + "\"?", help2);
      if (result.get().equals(ButtonType.OK)) {
        Platform.runLater(() -> {
          ResUploadProgressModel model = new ResUploadProgressModel(game.getId(), Messages.get("dialog.res_file_upload"), file);
          ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);
          if (/*resultModel.isSuccess() &&*/ finalizer != null) {
            finalizer.run();
          }
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
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, Messages.get("dialog.upload"), Messages.get("dialog.upload_ini_file_for") + game.getGameDisplayName() + "\"?", help2);
        if (result.get().equals(ButtonType.OK)) {
          IniUploadProgressModel model = new IniUploadProgressModel(game.getId(), Messages.get("dialog.ini_upload"), file);
          ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);
          if (/*resultModel.isSuccess() &&*/ finalizer != null) {
            finalizer.run();
          }
        }
      });
      return true;
    }
    return false;
  }

  public static boolean _directBamCfgUpload(Stage stage, GameRepresentation game, File file, Runnable finalizer) {
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        String help2 = null;
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, Messages.get("dialog.upload"), Messages.get("dialog.upload_bam_cfg_file_for") + game.getGameDisplayName() + "\"?", help2);
        if (result.get().equals(ButtonType.OK)) {
          BamCfgUploadProgressModel model = new BamCfgUploadProgressModel(Messages.get("dialog.bam_cfg_upload"), Arrays.asList(file), game.getId());
          ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);
          if (/*resultModel.isSuccess() &&*/ finalizer != null) {
            finalizer.run();
          }
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
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, Messages.get("dialog.upload"), Messages.get("dialog.upload_pov_file_for") + game.getGameDisplayName() + "\"?", help2);
        if (result.get().equals(ButtonType.OK)) {
          PovUploadProgressModel model = new PovUploadProgressModel(game.getId(), Messages.get("dialog.pov_upload"), file);
          ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);
          if (/*resultModel.isSuccess() &&*/ finalizer != null) {
            finalizer.run();
          }
        }
      });
      return true;
    }
    return false;
  }

  public static boolean download(Stage stage, String filename, InputStream in) {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle(Messages.get("dialog.select_target_folder"));
    File targetFolder = chooser.showOpenDialog(stage);

    if (targetFolder != null) {
      File targetFile = new File(targetFolder, filename);
      targetFile = FileUtils.uniqueFile(targetFile);
      try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
        IOUtils.copy(in, fileOutputStream);
        WidgetFactory.showInformation(stage, Messages.get("dialog.export_finished"), Messages.get("dialog.written") + targetFile.getName() + "\".");
        return true;
      }
      catch (IOException e) {
        LOG.error("Failed to download {} : {}", targetFile.getName(), e.getMessage(), e);
        WidgetFactory.showAlert(stage, Messages.get("common.error"), Messages.get("dialog.failed_to_download") + targetFile.getName() + ": " + e.getMessage());
      }
    }
    return false;
  }

  public static boolean openTableAssetsDialog(@Nullable TableOverviewController overviewController, GameRepresentation game, VPinScreen screen) {
    if (TableAssetManagerDialogController.INSTANCE != null) {
      return true;
    }

    Stage stage = Dialogs.createStudioDialogStage(Studio.stage, TableAssetManagerDialogController.class, "dialog-table-asset-manager.fxml", Messages.get("dialog.asset_manager"), TableAssetManagerDialogController.MODAL_STATE_ID);
    TableAssetManagerDialogController controller = (TableAssetManagerDialogController) stage.getUserData();
    controller.loadAllTables(game.getEmulatorId());
    controller.setGame(stage, overviewController, game, screen, false);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(860);
    stage.setMinHeight(600);

    stage.showAndWait();
    return true;
  }

  public static boolean openTableAssetsDialog(@Nullable TableOverviewController overviewController, GameRepresentation game, PlaylistRepresentation playlist, VPinScreen screen) {
    if (TableAssetManagerDialogController.INSTANCE != null) {
      return true;
    }

    Stage stage = Dialogs.createStudioDialogStage(Studio.stage, TableAssetManagerDialogController.class, "dialog-table-asset-manager.fxml", Messages.get("dialog.asset_manager"), TableAssetManagerDialogController.MODAL_STATE_ID);
    TableAssetManagerDialogController controller = (TableAssetManagerDialogController) stage.getUserData();
    controller.loadAllTables(game != null ? game.getEmulatorId() : -1);
    controller.setStage(stage);
    controller.setPlaylistMode();
    controller.setPlaylist(stage, overviewController, playlist, screen);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(860);
    stage.setMinHeight(600);

    stage.showAndWait();
    return true;
  }

  public static boolean openHighscoresAdminDialog(TablesSidebarController tablesSidebarController, GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(TableHighscoresAdminController.class, "dialog-highscores-admin.fxml", Messages.get("dialog.archived_highscores_for") + game.getGameDisplayName() + "\"");
    TableHighscoresAdminController controller = (TableHighscoresAdminController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();
    return true;
  }

  public static boolean openAltColorAdminDialog(TablesSidebarController tablesSidebarController, GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(TableAltColorAdminController.class, "dialog-altcolor-admin.fxml", Messages.get("dialog.alt_colors_for") + game.getGameDisplayName() + "\"");
    TableAltColorAdminController controller = (TableAltColorAdminController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();
    return true;
  }

  public static boolean openHighscoresResetDialog(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(HighscoreResetController.class, "dialog-highscore-reset.fxml", Messages.get("dialog.reset_highscores"));
    HighscoreResetController controller = (HighscoreResetController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
    return true;
  }


  public static boolean openCommentDialog(TableOverviewController overviewController, GameRepresentation game) {
    openTableDataDialog(overviewController, game, 2);
    return true;
  }


  public static boolean openTaggingDialog(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(TaggingDialogController.class, "dialog-tagging.fxml", Messages.get("dialog.bulk_tagging"));
    TaggingDialogController controller = (TaggingDialogController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
    return true;
  }


  public static void openTableMoveCloneDialog(TableOverviewController tableOverviewController, GameRepresentation game, boolean move) {
    openTableMoveCloneDialog(tableOverviewController, Collections.singletonList(game), move);
  }

  public static void openTableMoveCloneDialog(TableOverviewController tableOverviewController, List<GameRepresentation> games, boolean move) {
    Stage stage = Dialogs.createStudioDialogStage(TableMoveCloneController.class, "dialog-table-move-clone.fxml", move ? Messages.get("dialog.move_table") : Messages.get("dialog.clone_table"));
    TableMoveCloneController controller = (TableMoveCloneController) stage.getUserData();
    controller.setData(tableOverviewController, games, move);
    stage.showAndWait();
  }

  public static boolean openEventLogDialog(GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(EventLogController.class, "dialog-event-log.fxml", Messages.get("dialog.event_log"), "eventLog");
    EventLogController controller = (EventLogController) stage.getUserData();
    controller.setGame(game);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(800);
    stage.setMinHeight(600);

    stage.showAndWait();

    return true;
  }

  public static void openDMDPositionDialog(GameRepresentation game, @Nullable BaseTableController<?, ? extends BaseGameModel> baseTableController) {
    Stage stage = Dialogs.createStudioDialogStage(DMDPositionController.class, "dialog-dmd-position.fxml", Messages.get("dialog.dmd_position"));
    DMDPositionController controller = (DMDPositionController) stage.getUserData();
    controller.setGame(game, baseTableController);
    stage.showAndWait();
  }

  public static void openAltSoundUploadDialog(GameRepresentation game, File file, UploaderAnalysis analysis, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(AltSoundUploadController.class, "dialog-altsound-upload.fxml", Messages.get("dialog.alt_sound_upload"));
    AltSoundUploadController controller = (AltSoundUploadController) stage.getUserData();
    controller.setData(stage, file, game, analysis, finalizer);
    stage.showAndWait();
  }

  public static AltSound2DuckingProfile openAltSound2ProfileEditor(AltSound altSound, AltSound2DuckingProfile profile) {
    Stage stage = Dialogs.createStudioDialogStage(AltSound2ProfileDialogController.class, "dialog-altsound2-profile.fxml", Messages.get("dialog.edit_ducking_profile"));
    AltSound2ProfileDialogController controller = (AltSound2ProfileDialogController) stage.getUserData();
    controller.setProfile(altSound, profile);
    stage.showAndWait();

    return controller.editingFinished();
  }

  public static void openAltSound2SampleTypeDialog(AltSound altSound, AltSound2SampleType sampleType) {
    Stage stage = Dialogs.createStudioDialogStage(AltSound2SampleTypeDialogController.class, "dialog-altsound2-sample-type.fxml", Messages.get("dialog.sample_type_settings"));
    AltSound2SampleTypeDialogController controller = (AltSound2SampleTypeDialogController) stage.getUserData();
    controller.setProfile(altSound, sampleType);
    stage.showAndWait();
  }

  public static void openAltColorUploadDialog(GameRepresentation game, File file, UploaderAnalysis analysis, Runnable finalizer) {
    if (client.getEmulatorService().isVpxGame(game) && StringUtils.isEmpty(game.getRom())) {
      WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.no_rom"), Messages.get("dialog.table") + game.getGameDisplayName() + Messages.get("dialog.has_no_rom_name_set"), Messages.get("dialog.the_rom_name_is_required_for_this"));
    }

    Stage stage = Dialogs.createStudioDialogStage(AltColorUploadController.class, "dialog-altcolor-upload.fxml", Messages.get("dialog.alt_color_upload"));
    AltColorUploadController controller = (AltColorUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setFile(stage, file, analysis, finalizer);
    stage.showAndWait();
  }

  public static void openPupPackUploadDialog(GameRepresentation game, File file, UploaderAnalysis analysis, Runnable finalizer) {
    if (StringUtils.isEmpty(game.getRom())) {
      WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.no_rom"), Messages.get("dialog.table") + game.getGameDisplayName() + Messages.get("dialog.has_no_rom_name_set"), Messages.get("dialog.the_rom_name_is_required_for_this"));
    }

    Stage stage = Dialogs.createStudioDialogStage(PupPackUploadController.class, "dialog-puppack-upload.fxml", Messages.get("dialog.pup_pack_upload"));
    PupPackUploadController controller = (PupPackUploadController) stage.getUserData();
    controller.setFile(stage, file, analysis, finalizer);
    stage.showAndWait();
  }

  public static void openDMDUploadDialog(GameRepresentation game, File file, UploaderAnalysis analysis, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(DMDUploadController.class, "dialog-dmd-upload.fxml", Messages.get("dialog.dmd_bundle_upload"));
    DMDUploadController controller = (DMDUploadController) stage.getUserData();
    controller.setData(stage, file, game, analysis, finalizer);
    stage.showAndWait();
  }

  public static boolean openMediaUploadDialog(Stage parent, @Nullable GameRepresentation game, File file, @Nullable UploaderAnalysis analysis, @Nullable AssetType filterMode, int emulatorId) {
    String title = Messages.get("dialog.media_pack");
    if (game != null) {
      title = Messages.get("dialog.media_for") + game.getGameDisplayName() + "\"";
    }
    if (filterMode != null) {
      title = Messages.get("dialog.media_selection");
    }
    Stage stage = Dialogs.createStudioDialogStage(parent, MediaUploadController.class, "dialog-media-upload.fxml", title, null);
    MediaUploadController controller = (MediaUploadController) stage.getUserData();
    controller.setData(game, analysis, file, stage, filterMode, emulatorId);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static Optional<UploadDescriptor> openTableUploadDialog(@Nullable GameRepresentation game, @Nullable EmulatorType emutype, @Nullable UploadType uploadType, UploaderAnalysis analysis, @Nullable Runnable finalizer) {
    if (Studio.client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        return openTableUploadDialogUnchecked(game, emutype, uploadType, analysis, finalizer);
      }
      return Optional.empty();
    }
    return openTableUploadDialogUnchecked(game, emutype, uploadType, analysis, finalizer);
  }

  private static Optional<UploadDescriptor> openTableUploadDialogUnchecked(@Nullable GameRepresentation game, @Nullable EmulatorType emutype, @Nullable UploadType uploadType, UploaderAnalysis analysis, @Nullable Runnable finalizer) {
    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getEmulatorService().getGameEmulatorsByType(emutype);
    if (gameEmulators.isEmpty()) {
      WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.no_game_emulator_found"));
      return Optional.empty();
    }

    Stage stage = Dialogs.createStudioDialogStage(TableUploadController.class, "dialog-table-upload.fxml", Messages.get("dialog.table_upload"));
    TableUploadController controller = (TableUploadController) stage.getUserData();
    controller.setGame(stage, game, uploadType, analysis, finalizer);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openTableDeleteDialog(TableOverviewController tableOverviewController, List<GameRepresentation> selectedGames, List<GameRepresentation> allGames) {
    Stage stage = Dialogs.createStudioDialogStage(TableDeleteController.class, "dialog-table-delete.fxml", Messages.get("common.delete"));
    TableDeleteController controller = (TableDeleteController) stage.getUserData();
    controller.setGames(tableOverviewController, selectedGames, allGames);
    stage.showAndWait();
  }

  public static void openConverterDialog(List<GameRepresentation> selectedGames) {
    Stage stage = Dialogs.createStudioDialogStage(MediaConverterDialogController.class, "dialog-media-converter.fxml", Messages.get("dialog.media_conversion"));
    MediaConverterDialogController controller = (MediaConverterDialogController) stage.getUserData();
    controller.setGames(selectedGames);
    stage.showAndWait();
  }


  public static TableDetails openAutoFillSettingsDialog(Stage stage, List<GameRepresentation> games, TableDetails tableDetails) {
    return openAutoFillSettingsDialog(stage, games, tableDetails, null, null);
  }

  public static TableDetails openAutoFillSettingsDialog(Stage stage, List<GameRepresentation> games, TableDetails tableDetails, @Nullable String vpsTableId, @Nullable String vpsVersionId) {
    Stage dialogStage = Dialogs.createStudioDialogStage(stage, AutoFillSelectionController.class, "dialog-autofill-settings.fxml", Messages.get("dialog.auto_fill_settings"), null);
    AutoFillSelectionController controller = (AutoFillSelectionController) dialogStage.getUserData();
    controller.setData(games, tableDetails, vpsTableId, vpsVersionId);
    dialogStage.showAndWait();
    return controller.getTableDetails();
  }

  public static void openAutoMatchAll(List<GameRepresentation> games) {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, Messages.get("dialog.auto_match_table_and_version_for_all") + games.size() + Messages.get("dialog.tables"),
            Messages.get("common.cancel"), "Continue", Messages.get("dialog.the_table_and_display_name_is_used"), Messages.get("dialog.you_may_have_to_adept_the_result"), Messages.get("dialog.overwrite_existing_matchings"), false);
        if (!result.isApplyClicked()) {
          ProgressDialog.createProgressDialog(new TableVpsDataAutoMatchProgressModel(games, result.isChecked(), false));
          EventManager.getInstance().notifyTablesChanged();
        }
      }
    }
    else {
      ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, Messages.get("dialog.auto_match_table_and_version_for_all") + games.size() + Messages.get("dialog.tables"),
          Messages.get("common.cancel"), "Continue", Messages.get("dialog.the_table_and_display_name_is_used"), Messages.get("dialog.you_may_have_to_adept_the_result"), Messages.get("dialog.overwrite_existing_matchings"), false);
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
    String title = Messages.get("dialog.auto_match_table_and_version_for") + games.size() + Messages.get("dialog.tables");
    if (games.size() == 1) {
      title = Messages.get("dialog.auto_match_table_and_version_for") + "\"" + games.getFirst().getGameDisplayName() + "\"?";
    }

    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
        Messages.get("dialog.this_will_overwrite_the_existing_mapping"), Messages.get("dialog.this_action_will_overwrite_the_vps_table"), Messages.get("dialog.auto_match"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(new TableVpsDataAutoMatchProgressModel(games, true, false));
      return true;
    }
    return false;
  }

  public static boolean openScanAllDialog(List<GameRepresentation> games) {
    String title = Messages.get("dialog.re_scan_all") + games.size() + Messages.get("dialog.tables");
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
        Messages.get("dialog.scanning_will_try_to_resolve_rom_and"), null, Messages.get("dialog.start_scan"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(ClearCacheProgressModel.getFullClearCacheModel());
      ProgressDialog.createProgressDialog(new TableScanProgressModel(Messages.get("dialog.scanning_tables"), games));
      return true;
    }
    return false;
  }

  public static void openTableDataDialog(@Nullable TableOverviewController overviewController, GameRepresentation game) {
    openTableDataDialog(overviewController, game, -1);
  }

  public static void openTableDataDialog(@Nullable TableOverviewController overviewController, GameRepresentation game, int tab) {
    try {
      Stage stage = Dialogs.createStudioDialogStage(TableDataController.class, "dialog-table-data.fxml", Messages.get("dialog.table_data_manager"), "tableDataManager3");
      TableDataController controller = (TableDataController) stage.getUserData();
      controller.setGame(stage, overviewController, game, tab);

      FXResizeHelper.install(stage, 30, 6);
      stage.setMinWidth(1045);
      stage.setMaxHeight(1060);
      stage.setMinHeight(TableDataController.MIN_HEIGHT);

      stage.showAndWait();
    }
    catch (Exception e) {
      LOG.error("Failed to open table data manager: " + e.getMessage(), e);
    }
  }

  public static void openTableImportDialog(GameEmulatorRepresentation emulatorRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage("tableImport", Studio.stage, TableImportController.class, "dialog-table-import.fxml", Messages.get("dialog.table_importer"));
    TableImportController controller = (TableImportController) stage.getUserData();
    controller.setData(stage, emulatorRepresentation);
    stage.showAndWait();
  }

  public static void openWebhooksDialog(@NonNull WebhookSettings settings, @Nullable WebhookSet set) {
    Stage stage = Dialogs.createStudioDialogStage(WebhooksDialogController.class, "dialog-webhook-set.fxml", Messages.get("dialog.webhook_set"));
    WebhooksDialogController controller = (WebhooksDialogController) stage.getUserData();
    controller.setData(settings, set);
    stage.showAndWait();
  }

  public static boolean openIScoredGameRoomDialog(@NonNull IScoredSettings settings, @Nullable IScoredGameRoom gameRoom) {
    Stage stage = Dialogs.createStudioDialogStage(IScoredGameRoomDialogController.class, "dialog-iscored-gameroom.fxml", Messages.get("dialog.iscored_game_room"));
    IScoredGameRoomDialogController controller = (IScoredGameRoomDialogController) stage.getUserData();
    controller.setData(settings, gameRoom);
    stage.showAndWait();
    return controller.getResult();
  }

  public static void openVPSAssetsDialog(GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(VPSAssetsDialogController.class, "dialog-vps-assets.fxml", Messages.get("dialog.virtual_pinball_spreadsheet_assets"));
    VPSAssetsDialogController controller = (VPSAssetsDialogController) stage.getUserData();
    controller.setGame(stage, game);
    stage.showAndWait();
  }

  public static void openRomUploadDialog(GameEmulatorRepresentation emulator, File file, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(ROMUploadController.class, "dialog-rom-upload.fxml", Messages.get("dialog.rom_upload"));
    ROMUploadController controller = (ROMUploadController) stage.getUserData();
    controller.setFile(stage, file, null, finalizer);
    controller.setSelectedEmulator(emulator);
    stage.showAndWait();
  }

  public static void openPatchUpload(GameRepresentation gameRepresentation, File file, UploaderAnalysis analysis, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(PatchUploadController.class, "dialog-patch-upload.fxml", Messages.get("dialog.patch_upload"));
    PatchUploadController controller = (PatchUploadController) stage.getUserData();
    controller.setFile(stage, file, analysis, finalizer);
    controller.setData(gameRepresentation);
    stage.showAndWait();
  }

  public static void openMusicUploadDialog(File file, UploaderAnalysis analysis, int gameId, Runnable finalizer) {
    Stage stage = Dialogs.createStudioDialogStage(MusicUploadController.class, "dialog-music-upload.fxml", Messages.get("dialog.music_upload"));
    MusicUploadController controller = (MusicUploadController) stage.getUserData();
    controller.setFile(stage, file, analysis, finalizer);
    controller.setGameId(gameId);
    stage.showAndWait();
  }

  public static boolean openValidationDialog(List<GameRepresentation> selectedItems, boolean reload) {
    if (selectedItems.isEmpty()) {
      return false;
    }
    String title = Messages.get("dialog.re_validate") + selectedItems.size() + Messages.get("dialog.tables");
    if (selectedItems.size() == 1) {
      title = Messages.get("dialog.re_validate_table") + selectedItems.getFirst().getGameDisplayName() + "\"?";
    }

    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
        Messages.get("dialog.this_will_reset_the_dismissed_validations_for"), null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      title = Messages.get("dialog.re_validating") + selectedItems.size() + Messages.get("dialog.tables_2");
      if (selectedItems.size() == 1) {
        title = Messages.get("dialog.re_validating_table") + selectedItems.getFirst().getGameDisplayName() + "\"";
      }

      ProgressDialog.createProgressDialog(new TableValidateProgressModel(title, selectedItems, reload));
      return true;
    }
    return false;
  }

  public static void openDismissAllDialog(GameRepresentation gameRepresentation) {
    Stage stage = WidgetFactory.createDialogStage(DismissAllController.class, Studio.stage, Messages.get("dialog.dismiss_validation_errors"), "dialog-dismiss-all.fxml");
    DismissAllController controller = (DismissAllController) stage.getUserData();
    controller.setGame(gameRepresentation);
    stage.showAndWait();
  }

  public static boolean openDefaultBackgroundUploadDialog(GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(DefaultBackgroundUploadController.class, "dialog-background-picture-upload.fxml", Messages.get("dialog.default_background_upload"));
    DefaultBackgroundUploadController controller = (DefaultBackgroundUploadController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openMediaDialog(@NonNull Stage parent, @Nullable FrontendMediaItemRepresentation item) {
    if (item == null) {
      return;
    }

    Stage stage = Dialogs.createStudioDialogStage(parent, MediaPreviewController.class, "dialog-media-preview.fxml", item.getScreen() + Messages.get("dialog.screen"), "dialog-media-preview");
    MediaPreviewController controller = (MediaPreviewController) stage.getUserData();
    controller.setData(stage, item, false);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(800);
    stage.setMinHeight(600);

    stage.showAndWait();
  }

  public static void openMediaDialog(@NonNull Stage parent, @NonNull String title, @NonNull String url, @NonNull String mimeType) {
    Stage stage = Dialogs.createStudioDialogStage(parent, MediaPreviewController.class, "dialog-media-preview.fxml", title, "dialog-media-preview");
    MediaPreviewController controller = (MediaPreviewController) stage.getUserData();
    controller.setData(stage, url, mimeType);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(800);
    stage.setMinHeight(600);

    stage.showAndWait();
  }
}
