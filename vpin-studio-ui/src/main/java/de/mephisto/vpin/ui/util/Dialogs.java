package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.descriptors.ResetHighscoreDescriptor;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.ui.ProgressDialogController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.UpdateDialogController;
import de.mephisto.vpin.ui.competitions.CompetitionDiscordDialogController;
import de.mephisto.vpin.ui.competitions.CompetitionDiscordJoinDialogController;
import de.mephisto.vpin.ui.competitions.CompetitionOfflineDialogController;
import de.mephisto.vpin.ui.launcher.InstallationController;
import de.mephisto.vpin.ui.players.PlayerDialogController;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tables.dialogs.*;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Dialogs {

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
    else if(selection.getId() == null) {
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
    else if(selection.getId() == null) {
      title = "Duplicate Competition";
    }


    FXMLLoader fxmlLoader = new FXMLLoader(CompetitionOfflineDialogController.class.getResource("dialog-offline-competition-edit.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    CompetitionOfflineDialogController controller = (CompetitionOfflineDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
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

  public static boolean openTableDeleteDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage(TableDeleteController.class, "dialog-table-delete.fxml", "Delete");
    TableDeleteController controller = (TableDeleteController) stage.getUserData();
    controller.setGame(game);
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

  public static void openVpaInstallationDialog(TablesController tablesController, List<ArchiveDescriptorRepresentation> vpaDescriptors) {
    Stage stage = createStudioDialogStage(TableInstallFromBackController.class, "dialog-table-install.fxml", "Install Tables");
    TableInstallFromBackController controller = (TableInstallFromBackController) stage.getUserData();
    controller.setData(tablesController, vpaDescriptors);
    stage.showAndWait();
  }

  public static ArchiveSourceRepresentation openVpaSourceFileDialog(ArchiveSourceRepresentation source) {
    Stage stage = createStudioDialogStage(ArchiveSourceFileDialogController.class, "dialog-vpa-source-file.fxml", "VPA Folder Repository");
    ArchiveSourceFileDialogController controller = (ArchiveSourceFileDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();

    return controller.getVpaSource();
  }

  public static ArchiveSourceRepresentation openVpaSourceHttpDialog(ArchiveSourceRepresentation source) {
    Stage stage = createStudioDialogStage(ArchiveSourceHttpDialogController.class, "dialog-vpa-source-http.fxml", "VPA HTTP Repository");
    ArchiveSourceHttpDialogController controller = (ArchiveSourceHttpDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();

    return controller.getVpaSource();
  }

  public static ResetHighscoreDescriptor openHighscoreResetDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage(ResetHighscoreDialogController.class, "dialog-reset-highscore.fxml", "Highscore Reset");
    ResetHighscoreDialogController controller = (ResetHighscoreDialogController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.getDescriptor();
  }

  public static boolean openArchiveUploadDialog() {
    Stage stage = createStudioDialogStage(ArchiveUploadController.class, "dialog-archive-upload.fxml", "Archive Upload");
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

  public static ProgressResultModel createProgressDialog(ProgressModel model) {
    Stage stage = createStudioDialogStage("dialog-progress.fxml", model.getTitle());
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
