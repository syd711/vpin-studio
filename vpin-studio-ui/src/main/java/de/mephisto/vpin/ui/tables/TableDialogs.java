package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.DirectB2SArchiveAnalyzer;
import de.mephisto.vpin.commons.utils.IniArchiveAnalyzer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.commons.utils.media.VideoMediaPlayer;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSound2DuckingProfile;
import de.mephisto.vpin.restclient.altsound.AltSound2SampleType;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.archiving.dialogs.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.dialogs.*;
import de.mephisto.vpin.ui.tables.editors.dialogs.AltSound2ProfileDialogController;
import de.mephisto.vpin.ui.tables.editors.dialogs.AltSound2SampleTypeDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;

public class TableDialogs {

  public static void directAssetUpload(Stage stage, GameRepresentation game, PopperScreen screen) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Media");
    fileChooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("Files", PopperMediaTypesSelector.getFileSelection(screen)));

    List<File> files = fileChooser.showOpenMultipleDialog(stage);
    if (files != null && !files.isEmpty()) {
      Platform.runLater(() -> {
        TableMediaUploadProgressModel model = new TableMediaUploadProgressModel(game.getId(),
          "Popper Media Upload", files, "popperMedia", screen);
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  public static void onRomUploads(TablesSidebarController tablesSidebarController) {
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        boolean uploaded = TableDialogs.openRomUploadDialog();
        if (uploaded) {
          tablesSidebarController.getTablesController().onReload();
        }
      }
    }
    else {
      boolean uploaded = TableDialogs.openRomUploadDialog();
      if (uploaded) {
        tablesSidebarController.getTablesController().onReload();
      }
    }
  }

  public static void onMusicUploads(TablesSidebarController tablesSidebarController) {
    boolean uploaded = TableDialogs.openMusicUploadDialog();
    if (uploaded) {
      tablesSidebarController.getTablesController().onReload();
    }
  }


  public static boolean directBackglassUpload(Stage stage, GameRepresentation game) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select DirectB2S File");
    fileChooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("Direct B2S", "*.directb2s", "*.zip", "*.rar"));

    File file = fileChooser.showOpenDialog(stage);
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        String analyze = DirectB2SArchiveAnalyzer.analyze(file);
        if (!StringUtils.isEmpty(analyze)) {
          WidgetFactory.showAlert(Studio.stage, "Error", analyze);
        }
        else {
          DirectB2SUploadProgressModel model = new DirectB2SUploadProgressModel(game.getId(), "DirectB2S Upload", file, "table");
          ProgressDialog.createProgressDialog(model);
        }
      });
      return true;
    }
    return false;
  }

  public static boolean directBackglassUpload(Stage stage, GameRepresentation game, File file) {
    if (file != null && file.exists()) {
      String help2 = null;
      if (game.isDirectB2SAvailable()) {
        help2 = "The existing directb2 file of this table will be overwritten.";
      }
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Upload", "Upload backglass for \"" + game.getGameDisplayName() + "\"?", help2);
      if (result.get().equals(ButtonType.OK)) {
        Platform.runLater(() -> {
          String analyze = DirectB2SArchiveAnalyzer.analyze(file);
          if (!StringUtils.isEmpty(analyze)) {
            WidgetFactory.showAlert(Studio.stage, "Error", analyze);
          }
          else {
            DirectB2SUploadProgressModel model = new DirectB2SUploadProgressModel(game.getId(), "DirectB2S Upload", file, "table");
            ProgressDialog.createProgressDialog(model);
          }
        });
        return true;
      }
    }
    return false;
  }

  public static boolean directIniUpload(Stage stage, GameRepresentation game) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select .ini File");
    fileChooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter(".ini Files", "*.ini", "*.zip", "*.rar"));

    File file = fileChooser.showOpenDialog(stage);
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        String analyze = IniArchiveAnalyzer.analyze(file);
        if (!StringUtils.isEmpty(analyze)) {
          WidgetFactory.showAlert(Studio.stage, "Error", analyze);
        }
        else {

          IniUploadProgressModel model = new IniUploadProgressModel(game.getId(), "Ini Upload", file);
          ProgressDialog.createProgressDialog(model);
        }
      });
      return true;
    }
    return false;
  }

  public static boolean directIniUpload(Stage stage, GameRepresentation game, File file) {
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        String help2 = null;
        if (game.isIniAvailable()) {
          help2 = "The existing .ini file of this table will be overwritten.";
        }
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Upload", "Upload .ini file for \"" + game.getGameDisplayName() + "\"?", help2);
        if (result.get().equals(ButtonType.OK)) {
          IniUploadProgressModel model = new IniUploadProgressModel(game.getId(), "Ini Upload", file);
          ProgressDialog.createProgressDialog(model);
        }
      });
      return true;
    }
    return false;
  }

  public static boolean directPovUpload(Stage stage, GameRepresentation game, File file) {
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        String help2 = null;
        if (game.isPovAvailable()) {
          help2 = "The existing .pov file of this table will be overwritten.";
        }
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Upload", "Upload .ini file for \"" + game.getGameDisplayName() + "\"?", help2);
        if (result.get().equals(ButtonType.OK)) {
          PovUploadProgressModel model = new PovUploadProgressModel(game.getId(), "POV Upload", file);
          ProgressDialog.createProgressDialog(model);
        }
      });
      return true;
    }
    return false;
  }

  public static boolean openTableAssetsDialog(TableOverviewController overviewController, GameRepresentation game, PopperScreen screen) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice defaultScreenDevice = ge.getDefaultScreenDevice();
    GraphicsConfiguration defaultConfiguration = defaultScreenDevice.getDefaultConfiguration();
    boolean hd = defaultConfiguration.getBounds().getHeight() <= 1080;

    String fxml = "dialog-table-asset-manager.fxml";
    if (hd) {
      fxml = "dialog-table-asset-manager-hd.fxml";
    }
    Stage stage = Dialogs.createStudioDialogStage(TableAssetManagerDialogController.class, fxml, "Asset Manager", null);
    TableAssetManagerDialogController controller = (TableAssetManagerDialogController) stage.getUserData();
    controller.setGame(overviewController, game, screen);

//    FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
//    stage.setUserData(fxResizeHelper);

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

  public static boolean openDirectB2sManagerDialog(TablesSidebarController tablesSidebarController) {
    Stage stage = Dialogs.createStudioDialogStage(BackglassManagerDialogController.class, "dialog-directb2s-admin.fxml", "Backglass Manager");
    BackglassManagerDialogController controller = (BackglassManagerDialogController) stage.getUserData();
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();

    return true;
  }

  public static boolean openNotesDialog(GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(TableNotesController.class, "dialog-table-notes.fxml", "Comments");
    TableNotesController controller = (TableNotesController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return true;
  }

  public static boolean openAltSoundUploadDialog(TablesSidebarController tablesSidebarController, GameRepresentation game, File file) {
    Stage stage = Dialogs.createStudioDialogStage(AltSoundUploadController.class, "dialog-altsound-upload.fxml", "ALT Sound Upload");
    AltSoundUploadController controller = (AltSoundUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    controller.setFile(file);
    stage.showAndWait();

    return controller.uploadFinished();
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

  public static boolean openAltColorUploadDialog(TablesSidebarController tablesSidebarController, GameRepresentation game, File file) {
    Stage stage = Dialogs.createStudioDialogStage(AltColorUploadController.class, "dialog-altcolor-upload.fxml", "ALT Color Upload");
    AltColorUploadController controller = (AltColorUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    controller.setFile(file);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openPovUploadDialog(TablesSidebarController tablesSidebarController, GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(PovUploadController.class, "dialog-pov-upload.fxml", "POV File Upload");
    PovUploadController controller = (PovUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openPupPackUploadDialog(TablesSidebarController tablesSidebarController, GameRepresentation game, File file) {
    Stage stage = Dialogs.createStudioDialogStage(PupPackUploadController.class, "dialog-puppack-upload.fxml", "PUP Pack Upload");
    PupPackUploadController controller = (PupPackUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    controller.setFile(file, stage);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openDMDUploadDialog(TablesSidebarController tablesSidebarController, GameRepresentation game, File file) {
    Stage stage = Dialogs.createStudioDialogStage(DMDUploadController.class, "dialog-dmd-upload.fxml", "DMD Bundle Upload");
    DMDUploadController controller = (DMDUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    controller.setFile(file, stage);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static Optional<TableUploadResult> openTableUploadDialog(GameRepresentation game, TableUploadDescriptor descriptor) {
    Stage stage = Dialogs.createStudioDialogStage(TableUploadController.class, "dialog-table-upload.fxml", "VPX Table Upload");
    TableUploadController controller = (TableUploadController) stage.getUserData();
    controller.setGame(game, descriptor);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openTableDeleteDialog(List<GameRepresentation> selectedGames, List<GameRepresentation> allGames) {
    Stage stage = Dialogs.createStudioDialogStage(TableDeleteController.class, "dialog-table-delete.fxml", "Delete");
    TableDeleteController controller = (TableDeleteController) stage.getUserData();
    controller.setGames(selectedGames, allGames);
    stage.showAndWait();

    return controller.tableDeleted();
  }

  public static void openAutoFillAll() {
    ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, "Auto-fill table meta data for all " + client.getGameService().getVpxGamesCached().size() + " tables?",
      "Cancel", "Continue", "The VPX script meta data and VPS table information will be used to fill Popper the popper database fields.",
      "You can choose to overwrite existing data or to fill only empty values.", "Overwrite existing data", false);
    if (!result.isApplyClicked()) {
      ProgressDialog.createProgressDialog(new TableDataAutoFillProgressModel(client.getGameService().getVpxGamesCached(), result.isChecked()));
      EventManager.getInstance().notifyTablesChanged();
    }
  }

  public static TableDetails openAutoFill(GameRepresentation game) {
    ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, "Auto-fill table meta data for \"" + game.getGameDisplayName() + "\"?",
      "Cancel", "Continue", "The VPX script meta data and VPS table information will be used to fill Popper the popper database fields.",
      "You can choose to overwrite existing data or to fill only empty values.", "Overwrite existing data", false);
    if (!result.isApplyClicked()) {
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new TableDataAutoFillProgressModel(Arrays.asList(game), result.isChecked()));
      if (!progressDialog.getResults().isEmpty()) {
        return (TableDetails) progressDialog.getResults().get(0);
      }
    }
    return null;
  }

  public static void openAutoMatchAll() {
    ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, "Auto-Match table and version for all " + client.getGameService().getVpxGamesCached().size() + " tables?",
      "Cancel", "Continue", "The table and display name is used to find the matching table.", "You may have to adept the result manually.", "Overwrite existing matchings", false);
    if (!result.isApplyClicked()) {
      ProgressDialog.createProgressDialog(new TableVpsDataAutoMatchProgressModel(client.getGameService().getVpxGamesCached(), result.isChecked()));
      EventManager.getInstance().notifyTablesChanged();
    }
  }

  public static void openAutoMatch(GameRepresentation game) {
    if (Studio.client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        onOpenAutoMatch(game);
      }
      return;
    }
    onOpenAutoMatch(game);
  }

  private static void onOpenAutoMatch(GameRepresentation game) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Auto-Match table and version for \"" + game.getGameDisplayName() + "\"?",
      "This will overwrite the existing mapping.", "This action will overwrite the Popper fields configured for the VPS table and version IDs.", "Auto-Match");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(new TableVpsDataAutoMatchProgressModel(Arrays.asList(game), true));
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

  public static boolean openScanAllDialog(List<GameRepresentation> games) {
    String title = "Re-scan all " + games.size() + " tables?";
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
      "Scanning will try to resolve ROM and highscore file names of the selected tables.", null, "Start Scan");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.clearCache();
      ProgressDialog.createProgressDialog(new TableScanProgressModel("Scanning Tables", games));
      return true;
    }
    return false;
  }

  public static void openTableDataDialog(TableOverviewController overviewController, GameRepresentation game) {
    openTableDataDialog(overviewController, game, TableDataController.lastTab);
  }

  public static void openTableDataDialog(TableOverviewController overviewController, GameRepresentation game, int tab) {
    Stage stage = Dialogs.createStudioDialogStage(TableDataController.class, "dialog-table-data.fxml", "Table Data Manager");
    TableDataController controller = (TableDataController) stage.getUserData();
    controller.setGame(stage, overviewController, game, tab);
    stage.showAndWait();
  }

  public static void openTablesBackupDialog(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(TablesBackupController.class, "dialog-tables-backup.fxml", "Table Backup");
    TablesBackupController controller = (TablesBackupController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
  }

  public static void openTableImportDialog() {
    Stage stage = Dialogs.createStudioDialogStage(TableImportController.class, "dialog-table-import.fxml", "Table Import");
    TableImportController controller = (TableImportController) stage.getUserData();
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
    controller.setGame(game);
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

  public static boolean openRomUploadDialog() {
    Stage stage = Dialogs.createStudioDialogStage(ROMUploadController.class, "dialog-rom-upload.fxml", "Rom Upload");
    ROMUploadController controller = (ROMUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openMusicUploadDialog() {
    Stage stage = Dialogs.createStudioDialogStage(MusicUploadController.class, "dialog-music-upload.fxml", "Music Upload");
    MusicUploadController controller = (MusicUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
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
    FXMLLoader fxmlLoader = new FXMLLoader(DismissAllController.class.getResource("dialog-dismiss-all.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, "Dismiss Validation Errors");
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

  public static void openMediaDialog(VPinStudioClient client, GameRepresentation game, GameMediaItemRepresentation item) {
    Parent root = null;
    try {
      root = FXMLLoader.load(Studio.class.getResource("dialog-media.fxml"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    Stage owner = Studio.stage;
    BorderPane mediaView = (BorderPane) root.lookup("#mediaView");

    AssetMediaPlayer assetMediaPlayer = WidgetFactory.addMediaItemToBorderPane(client, item, mediaView);
    final Stage stage = WidgetFactory.createStage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle(game.getGameDisplayName() + " - " + item.getScreen() + " Screen");

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });


    if (assetMediaPlayer instanceof VideoMediaPlayer) {
      VideoMediaPlayer player = (VideoMediaPlayer) assetMediaPlayer;
      player.scaleForDialog(item.getScreen());
    }

    stage.showAndWait();
  }
}
