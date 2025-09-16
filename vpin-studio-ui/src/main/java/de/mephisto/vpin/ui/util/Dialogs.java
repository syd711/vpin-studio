package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.system.SystemData;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.launcher.InstallationDialogController;
import de.mephisto.vpin.ui.players.dialogs.PlayerDialogController;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;

public class Dialogs {
  private final static Logger LOG = LoggerFactory.getLogger(Dialogs.class);

  public static void editFile(File file) {
    if (file != null) {
      try {
        if (file.exists()) {
          Studio.edit(file);
        }
        else {
          WidgetFactory.showAlert(Studio.stage, "File Not Found", "The file \"" + file.getAbsolutePath() + "\" does not exist.");
        }
      }
      catch (Exception e) {
        LOG.error("Failed to open Explorer: " + e.getMessage(), e);
      }
    }
  }

  public static void openUpdateInfoDialog(String version, boolean force) {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    if (force || !uiSettings.isHideUpdateInfo()) {
      FXMLLoader fxmlLoader = new FXMLLoader(UpdateInfoDialogController.class.getResource("dialog-update-info.fxml"));
      Stage stage = WidgetFactory.createDialogStage("update-info", fxmlLoader, Studio.stage, "Release Notes for " + version);
      stage.showAndWait();

      uiSettings.setHideUpdateInfo(true);
      client.getPreferenceService().setJsonPreference(uiSettings);

      if (!force) {
        ClientUpdatePostProcessing.executePostProcessing();
      }
    }
  }

  public static void openNextUpdateDialog(String version) {
    FXMLLoader fxmlLoader = new FXMLLoader(UpdateInfoDialogController.class.getResource("dialog-update-info.fxml"));
    Stage stage = WidgetFactory.createDialogStage("update-info", fxmlLoader, Studio.stage, "Release Notes for " + version);
    UpdateInfoDialogController controller = (UpdateInfoDialogController) stage.getUserData();
    controller.setForUpdate(version);
    stage.showAndWait();
  }

  public static boolean openUpdateDialog() {
    return openUpdateDialog(Studio.client);
  }

  public static boolean openUpdateDialog(VPinStudioClient client) {
    Stage stage = createStudioDialogStage("dialog-update.fxml", "VPin Studio Updater");
    UpdateDialogController controller = (UpdateDialogController) stage.getUserData();
    controller.setClient(client);
    stage.showAndWait();
    return true;
  }

  public static boolean openTextEditor(MonitoredTextFile file, String title) throws Exception {
    return openTextEditor("text-editor", Studio.stage, file, title);
  }

  public static boolean openTextEditor(String stateId, Stage s, MonitoredTextFile file, String title) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(TextEditorController.class.getResource("text-editor.fxml"));
      Stage stage = WidgetFactory.createDialogStage(stateId, fxmlLoader, s, title, TextEditorController.class.getSimpleName());
      stage.setMinWidth(800);
      stage.setMinHeight(600);
      TextEditorController controller = (TextEditorController) stage.getUserData();
      controller.load(file);

      FXResizeHelper.install(stage, 30, 6);

      stage.showAndWait();
      return controller.isSaved();
    }
    catch (Exception e) {
      LOG.error("Failed to open file: {}", e.getMessage(), e);
      WidgetFactory.showAlert(s, "Error", "Failed to open file: " + e.getMessage());
    }
    return false;
  }

  public static PlayerRepresentation openPlayerDialog(PlayerRepresentation selection, List<PlayerRepresentation> players) {
    String title = "Add New Player";
    if (selection != null) {
      title = "Edit Player";
    }

    FXMLLoader fxmlLoader = new FXMLLoader(PlayerDialogController.class.getResource("dialog-player-edit.fxml"));
    Stage stage = WidgetFactory.createDialogStage("player-edit", fxmlLoader, Studio.stage, title);
    PlayerDialogController controller = (PlayerDialogController) stage.getUserData();
    controller.setPlayer(selection, players);
    stage.showAndWait();

    return controller.getPlayer();
  }


  public static boolean openInstallerDialog() {
    Stage stage = createStudioDialogStage(null, Studio.stage, InstallationDialogController.class, "dialog-installer.fxml", "Visual Studio Server Installation");
    InstallationDialogController controller = (InstallationDialogController) stage.getUserData();
    controller.setStage(stage);
    stage.showAndWait();
    return controller.install();
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
      }
      catch (IOException e) {
        LOG.error("Failed to create temporary file for text file: " + e.getMessage());
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to create temporary file for text file: " + e.getMessage());
        return;
      }
    }
    Studio.open(file);
  }

  public static Stage createStudioDialogStage(String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(Studio.class.getResource(fxml));
    String stateId = FilenameUtils.getBaseName(fxml);
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, Studio.stage, title);
  }

  public static Stage createStudioDialogStage(Stage stage, Class<?> clazz, String fxml, String title, String modalStateId) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    String stateId = FilenameUtils.getBaseName(fxml);
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, stage, title, modalStateId);
  }


  public static Stage createStudioDialogStage(Class<?> clazz, String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    String stateId = FilenameUtils.getBaseName(fxml);
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, Studio.stage, title, null);
  }

  public static Stage createStudioDialogStage(Class<?> clazz, String fxml, String title, String modalStateId) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    String stateId = FilenameUtils.getBaseName(fxml);
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, Studio.stage, title, modalStateId);
  }

  public static Stage createStudioDialogStage(String stateId, Stage stage, Class<?> clazz, String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, stage, title);
  }

  public static boolean openFrontendRunningWarning(Stage stage) {
    boolean local = client.getSystemService().isLocal();
    Frontend frontend = Studio.client.getFrontendService().getFrontendCached();

    if (!local) {
      ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithCheckbox(stage,
          FrontendUtil.replaceName("[Frontend] is running.", frontend),
          "Kill Processes", "Cancel",
          FrontendUtil.replaceName("[Frontend] is running. To perform this operation, you have to close it.", frontend),
          null, "Switch cabinet to maintenance mode");
      if (confirmationResult.isApplyClicked()) {
        client.getFrontendService().terminateFrontend();
        if (confirmationResult.isChecked()) {
          EventManager.getInstance().notifyMaintenanceMode(true);
        }
        return true;
      }
      return false;
    }
    else {
      Optional<ButtonType> buttonType = WidgetFactory.showAlertOption(stage,
          FrontendUtil.replaceName("[Frontend] is running.", frontend),
          "Kill Processes", "Cancel",
          FrontendUtil.replaceName("[Frontend] is running. To perform this operation, you have to close it.", frontend),
          null);
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
        client.getFrontendService().terminateFrontend();
        return true;
      }
      return false;
    }
  }
}
