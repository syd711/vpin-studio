package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.ResetHighscoreDescriptor;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.ui.ProgressDialogController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.UpdateDialogController;
import de.mephisto.vpin.ui.competitions.CompetitionDiscordDialogController;
import de.mephisto.vpin.ui.competitions.CompetitionDiscordJoinDialogController;
import de.mephisto.vpin.ui.competitions.CompetitionOfflineDialogController;
import de.mephisto.vpin.ui.players.PlayerDialogController;
import de.mephisto.vpin.ui.tables.TableOverviewController;
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

  public static void openTableExportDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage(TableExportController.class, "dialog-table-export.fxml", "Table Export");
    TableExportController controller = (TableExportController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();
  }

  public static void openTablesExportDialog(List<GameRepresentation> games) {
    Stage stage = createStudioDialogStage(TableExportController.class, "dialog-tables-export.fxml", "Table Export");
    TablesExportController controller = (TablesExportController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
  }

  public static void openTableImportDialog(TableOverviewController tablesController) {
    Stage stage = createStudioDialogStage(TableImportController.class, "dialog-table-import.fxml", "Table Import");
    TableImportController controller = (TableImportController) stage.getUserData();
    controller.setTablesController(tablesController);
    stage.showAndWait();
  }

  public static void openVpaInstallationDialog(TablesController tablesController, List<VpaDescriptorRepresentation> vpaDescriptors) {
    Stage stage = createStudioDialogStage(VpaInstallationController.class, "dialog-vpa-install.fxml", "Install Tables");
    VpaInstallationController controller = (VpaInstallationController) stage.getUserData();
    controller.setData(tablesController, vpaDescriptors);
    stage.showAndWait();
  }

  public static VpaSourceRepresentation openVpaSourceFileDialog(VpaSourceRepresentation source) {
    Stage stage = createStudioDialogStage(VpaSourceFileDialogController.class, "dialog-vpa-source-file.fxml", "VPA Folder Repository");
    VpaSourceFileDialogController controller = (VpaSourceFileDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();

    return controller.getVpaSource();
  }

  public static VpaSourceRepresentation openVpaSourceHttpDialog(VpaSourceRepresentation source) {
    Stage stage = createStudioDialogStage(VpaSourceHttpDialogController.class, "dialog-vpa-source-http.fxml", "VPA HTTP Repository");
    VpaSourceHttpDialogController controller = (VpaSourceHttpDialogController) stage.getUserData();
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

  public static boolean openVpaUploadDialog() {
    Stage stage = createStudioDialogStage(VpaUploadController.class, "dialog-vpa-upload.fxml", "Visual Pinball Archive Upload");
    VpaUploadController controller = (VpaUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }



  public static void openVpaDownloadDialog(ObservableList<VpaDescriptorRepresentation> selectedItems) {
    Stage stage = createStudioDialogStage(VpaDownloadDialogController.class, "dialog-vpa-download.fxml", "Visual Pinball Archive Download");
    VpaDownloadDialogController controller = (VpaDownloadDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static boolean openRomUploadDialog() {
    Stage stage = createStudioDialogStage(ROMUploadController.class, "dialog-rom-upload.fxml", "Rom Upload");
    ROMUploadController controller = (ROMUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
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
