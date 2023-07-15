package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.SystemData;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.descriptors.ResetHighscoreDescriptor;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.ui.ProgressDialogController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.UpdateDialogController;
import de.mephisto.vpin.ui.archiving.dialogs.*;
import de.mephisto.vpin.ui.competitions.CompetitionDiscordDialogController;
import de.mephisto.vpin.ui.competitions.CompetitionDiscordJoinDialogController;
import de.mephisto.vpin.ui.competitions.CompetitionOfflineDialogController;
import de.mephisto.vpin.ui.launcher.InstallationController;
import de.mephisto.vpin.ui.players.PlayerDialogController;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.dialogs.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Dialogs {
  private final static Logger LOG = LoggerFactory.getLogger(Dialogs.class);

  public static PlayerRepresentation openPlayerDialog(PlayerRepresentation selection) {
    String title = "Add New Player";
    if (selection != null) {
      title = "Edit Player";
    }

    FXMLLoader fxmlLoader = new FXMLLoader(PlayerDialogController.class.getResource("dialog-player-edit.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    PlayerDialogController controller = (PlayerDialogController) stage.getUserData();
    controller.setPlayer(selection);
    stage.showAndWait();

    return controller.getPlayer();
  }

  public static CompetitionRepresentation openDiscordJoinCompetitionDialog() {
    String title = "Join Competition";
    FXMLLoader fxmlLoader = new FXMLLoader(CompetitionOfflineDialogController.class.getResource("dialog-discord-competition-join.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    CompetitionDiscordJoinDialogController controller = (CompetitionDiscordJoinDialogController) stage.getUserData();
    stage.showAndWait();

    return controller.getCompetition();
  }

  public static CompetitionRepresentation openDiscordCompetitionDialog(List<CompetitionRepresentation> all, @Nullable CompetitionRepresentation selection) {
    String title = "Edit Competition";
    if (selection == null) {
      title = "Add Competition";
    }
    else if (selection.getId() == null) {
      title = "Duplicate Competition";
    }

    FXMLLoader fxmlLoader = new FXMLLoader(CompetitionDiscordDialogController.class.getResource("dialog-discord-competition-edit.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    CompetitionDiscordDialogController controller = (CompetitionDiscordDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
  }


  public static CompetitionRepresentation openOfflineCompetitionDialog(List<CompetitionRepresentation> all, CompetitionRepresentation selection) {
    String title = "Edit Competition";
    if (selection == null) {
      title = "Add Competition";
    }
    else if (selection.getId() == null) {
      title = "Duplicate Competition";
    }


    FXMLLoader fxmlLoader = new FXMLLoader(CompetitionOfflineDialogController.class.getResource("dialog-offline-competition-edit.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    CompetitionOfflineDialogController controller = (CompetitionOfflineDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
  }

  public static boolean openMediaUploadDialog(Stage parentStage, TablesSidebarController tablesSidebarController, GameRepresentation game, PopperScreen screen) {
    Stage stage = createStudioDialogStage(parentStage, TableMediaUploadController.class, "dialog-media-upload.fxml", "Table Media Upload");
    TableMediaUploadController controller = (TableMediaUploadController) stage.getUserData();
    controller.setGame(game, screen);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();

    return controller.uploadFinished();
  }


  public static boolean openMediaAdminDialog(TablesSidebarController tablesSidebarController, GameRepresentation game, PopperScreen screen) {
    Stage stage = createStudioDialogStage(TableMediaAdminController.class, "dialog-media-admin.fxml", "Assets for Screen \"" + screen.name() + "\"");
    TableMediaAdminController controller = (TableMediaAdminController) stage.getUserData();
    controller.setGame(game, screen);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();

    return true;
  }

  public static boolean openAltSoundUploadDialog(TablesSidebarController tablesSidebarController, GameRepresentation game) {
    Stage stage = createStudioDialogStage(AltSoundUploadController.class, "dialog-altsound-upload.fxml", "ALT Sound Upload");
    AltSoundUploadController controller = (AltSoundUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openAltColorUploadDialog(TablesSidebarController tablesSidebarController, GameRepresentation game) {
    Stage stage = createStudioDialogStage(AltColorUploadController.class, "dialog-altcolor-upload.fxml", "ALT Color Upload");
    AltColorUploadController controller = (AltColorUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openPovUploadDialog(TablesSidebarController tablesSidebarController, GameRepresentation game) {
    Stage stage = createStudioDialogStage(PovUploadController.class, "dialog-pov-upload.fxml", "POV File Upload");
    PovUploadController controller = (PovUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openPupPackUploadDialog(TablesSidebarController tablesSidebarController, GameRepresentation game) {
    Stage stage = createStudioDialogStage(PupPackUploadController.class, "dialog-puppack-upload.fxml", "PUP Pack Upload");
    PupPackUploadController controller = (PupPackUploadController) stage.getUserData();
    controller.setGame(game);
    controller.setTableSidebarController(tablesSidebarController);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openDirectB2SUploadDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage(DirectB2SUploadController.class, "dialog-directb2s-upload.fxml", "DirectB2S File Upload");
    DirectB2SUploadController controller = (DirectB2SUploadController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openDefaultBackgroundUploadDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage(DefaultBackgroundUploadController.class, "dialog-background-picture-upload.fxml", "Default Background Upload");
    DefaultBackgroundUploadController controller = (DefaultBackgroundUploadController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openTableUploadDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage(TableUploadController.class, "dialog-table-upload.fxml", "VPX Table Upload");
    TableUploadController controller = (TableUploadController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openTableDeleteDialog(List<GameRepresentation> selectedGames, List<GameRepresentation> allGames) {
    Stage stage = createStudioDialogStage(TableDeleteController.class, "dialog-table-delete.fxml", "Delete");
    TableDeleteController controller = (TableDeleteController) stage.getUserData();
    controller.setGames(selectedGames, allGames);
    stage.showAndWait();

    return controller.tableDeleted();
  }

  public static void openTableDataDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage(TableDataController.class, "dialog-table-data.fxml", "Table Data");
    TableDataController controller = (TableDataController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();
  }

  public static void openTablesBackupDialog(List<GameRepresentation> games) {
    Stage stage = createStudioDialogStage(TablesBackupController.class, "dialog-tables-backup.fxml", "Table Backup");
    TablesBackupController controller = (TablesBackupController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
  }

  public static void openPopperScreensDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage(PopperScreensController.class, "dialog-popper-screens.fxml", "Keep Displays On");
    PopperScreensController controller = (PopperScreensController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();
  }

  public static void openTableImportDialog() {
    Stage stage = createStudioDialogStage(TableImportController.class, "dialog-table-import.fxml", "Table Import");
    TableImportController controller = (TableImportController) stage.getUserData();
    stage.showAndWait();
  }

  public static void openTableInstallationDialog(TablesController tablesController, List<ArchiveDescriptorRepresentation> descriptorRepresentations) {
    Stage stage = createStudioDialogStage(TableRestoreController.class, "dialog-table-restore.fxml", "Restore Tables");
    TableRestoreController controller = (TableRestoreController) stage.getUserData();
    controller.setData(tablesController, descriptorRepresentations);
    stage.showAndWait();
  }

  public static ArchiveSourceRepresentation openArchiveSourceFileDialog(ArchiveSourceRepresentation source) {
    Stage stage = createStudioDialogStage(ArchiveSourceFileDialogController.class, "dialog-archive-source-file.fxml", "Folder Repository");
    ArchiveSourceFileDialogController controller = (ArchiveSourceFileDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();
    return controller.getArchiveSource();
  }

  public static ArchiveSourceRepresentation openArchiveSourceHttpDialog(ArchiveSourceRepresentation source) {
    Stage stage = createStudioDialogStage(ArchiveSourceHttpDialogController.class, "dialog-archive-source-http.fxml", "HTTP Repository");
    ArchiveSourceHttpDialogController controller = (ArchiveSourceHttpDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();

    return controller.getArchiveSource();
  }

  public static ResetHighscoreDescriptor openHighscoreResetDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage(ResetHighscoreDialogController.class, "dialog-reset-highscore.fxml", "Highscore Reset");
    ResetHighscoreDialogController controller = (ResetHighscoreDialogController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.getDescriptor();
  }

  public static boolean openArchiveUploadDialog() {
    Stage stage = createStudioDialogStage(ArchiveUploadController.class, "dialog-archive-upload.fxml", "Upload");
    ArchiveUploadController controller = (ArchiveUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openArchiveDownloadDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = createStudioDialogStage(ArchiveDownloadDialogController.class, "dialog-archive-download.fxml", "Archive Download");
    ArchiveDownloadDialogController controller = (ArchiveDownloadDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static void openArchiveBundleDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = createStudioDialogStage(ArchiveBundleDialogController.class, "dialog-bundle-download.fxml", "Archive Bundle");
    ArchiveBundleDialogController controller = (ArchiveBundleDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static void openCopyArchiveToRepositoryDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = createStudioDialogStage(CopyArchiveToRepositoryDialogController.class, "dialog-copy-archive-to-repository.fxml", "Copy To Repository");
    CopyArchiveToRepositoryDialogController controller = (CopyArchiveToRepositoryDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static boolean openRomUploadDialog() {
    Stage stage = createStudioDialogStage(ROMUploadController.class, "dialog-rom-upload.fxml", "Rom Upload");
    ROMUploadController controller = (ROMUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openAliasMappingDialog(GameRepresentation game, String alias, String rom) {
    Stage stage = createStudioDialogStage(AliasMappingController.class, "dialog-alias-mapping.fxml", "Alias Mapping");
    AliasMappingController controller = (AliasMappingController) stage.getUserData();
    controller.setValues(game, alias, rom);
    stage.showAndWait();
    return true;
  }

  public static boolean openInstallerDialog() {
    Stage stage = createStudioDialogStage(InstallationController.class, "dialog-installer.fxml", "Visual Studio Server Installation");
    InstallationController controller = (InstallationController) stage.getUserData();
    controller.setStage(stage);
    stage.showAndWait();
    return controller.install();
  }

  public static boolean openAltSoundEditor(GameRepresentation game, AltSound altSound) {
    Stage stage = createStudioDialogStage(ROMUploadController.class, "dialog-alt-sound-editor.fxml", "ALT Sound Editor");
    AltSoundEditorController controller = (AltSoundEditorController) stage.getUserData();
    controller.setAltSound(game, altSound);
    stage.showAndWait();

    return true;
  }

  public static void openBotServerIdTutorial() {
    Stage stage = createStudioDialogStage("dialog-bot-server-id-tutorial.fxml", "Server ID Instructions");
    stage.showAndWait();
  }

  public static void openBotTokenTutorial() {
    Stage stage = createStudioDialogStage("dialog-bot-token-tutorial.fxml", "Bot Token Instructions");
    stage.showAndWait();
  }

  public static void openBotTutorial() {
    Stage stage = createStudioDialogStage("dialog-bot-tutorial.fxml", "Discord Bot Instructions");
    stage.showAndWait();
  }

  public static boolean openUpdateDialog() {
    Stage stage = createStudioDialogStage("dialog-update.fxml", "VPin Studio Updater");
    UpdateDialogController controller = (UpdateDialogController) stage.getUserData();
    stage.showAndWait();
    return true;
  }

  public static void openFile(@NonNull File file) {
    if (!Studio.client.getSystemService().isLocal()) {
      try {
        SystemData systemData = Studio.client.getSystemService().getSystemData(file.getAbsolutePath().replaceAll("\\\\", "/"));
        if (!StringUtils.isEmpty(systemData.getData())) {
          file = File.createTempFile(file.getName(), ".txt");
          file.deleteOnExit();
          Path path = Paths.get(file.toURI());
          Files.write(path, systemData.getData().getBytes());
        }
        else {
          WidgetFactory.showAlert(Studio.stage, "No Data", "The file \"" + file.getAbsolutePath() + "\" does not contain any data or wasn't found.");
        }
      } catch (IOException e) {
        LOG.error("Failed to create temporary file for text file: " + e.getMessage());
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to create temporary file for text file: " + e.getMessage());
        return;
      }
    }

    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        desktop.open(file);
      } catch (Exception e) {
        LOG.error("Failed to open discord link: " + e.getMessage(), e);
      }
    }
  }


  public static ProgressResultModel createProgressDialog(ProgressModel model) {
    Stage stage = createStudioDialogStage("dialog-progress.fxml", model.getTitle());
    stage.setAlwaysOnTop(true);
    ProgressDialogController controller = (ProgressDialogController) stage.getUserData();
    controller.setProgressModel(stage, model);
    stage.showAndWait();
    ProgressResultModel progressResult = controller.getProgressResult();
    stage.close();
    return progressResult;
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

    if (item.getScreen().equals(PopperScreen.PlayField.name()) || item.getScreen().equals(PopperScreen.Loading.name())) {
      mediaView.rotateProperty().set(90);
    }

    WidgetFactory.addMediaItemToBorderPane(client, item, mediaView);
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
    stage.showAndWait();
  }

  private static Stage createStudioDialogStage(String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(Studio.class.getResource(fxml));
    return WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
  }

  private static Stage createStudioDialogStage(Stage stage, Class clazz, String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    return WidgetFactory.createDialogStage(fxmlLoader, stage, title);
  }

  private static Stage createStudioDialogStage(Class clazz, String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    return WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
  }

  public static Optional<ButtonType> openPopperRunningWarning(Stage stage) {
    return WidgetFactory.showAlertOption(stage, "PinUP Popper is running.", "Close PinUP Popper", "Cancel",
        "PinUP Popper is running. To perform this operation, you have to close it.",
        "This will also KILL the the current emulator process!");
  }
}
