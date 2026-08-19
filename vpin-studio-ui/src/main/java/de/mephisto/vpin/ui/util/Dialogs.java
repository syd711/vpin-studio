package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.system.SystemData;
import de.mephisto.vpin.restclient.textedit.TextEditorFile;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.launcher.InstallationDialogController;
import de.mephisto.vpin.ui.players.dialogs.PlayerDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class Dialogs {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static void editFile(File file) {
    if (file != null) {
      try {
        if (file.exists()) {
          Studio.edit(file);
        }
        else {
          WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.file_not_found"), Messages.get("dialog.the_file") + file.getAbsolutePath() + Messages.get("dialog.does_not_exist"));
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
      fxmlLoader.setResources(Messages.getBundle());
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
    fxmlLoader.setResources(Messages.getBundle());
    Stage stage = WidgetFactory.createDialogStage("update-info", fxmlLoader, Studio.stage, "Release Notes for " + version);
    UpdateInfoDialogController controller = (UpdateInfoDialogController) stage.getUserData();
    controller.setData(stage, version);
    stage.showAndWait();
  }

  public static boolean openUpdateDialog() {
    return openUpdateDialog(Studio.client);
  }

  public static boolean openUpdateDialog(VPinStudioClient client) {
    Stage stage = createStudioDialogStage("dialog-updater.fxml", "VPin Studio Updater");
    UpdateDialogController controller = (UpdateDialogController) stage.getUserData();
    controller.setData(stage, client);
    stage.showAndWait();
    return true;
  }

  public static boolean openTextEditor(TextEditorFile file, String title) throws Exception {
    return openTextEditor("text-editor", Studio.stage, file, title);
  }

  public static boolean openTextEditor(String stateId, Stage s, TextEditorFile file, String title) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(TextEditorController.class.getResource("text-editor.fxml"));
      fxmlLoader.setResources(Messages.getBundle());
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
      WidgetFactory.showAlert(s, Messages.get("common.error"), Messages.get("dialog.failed_to_open_file") + e.getMessage());
    }
    return false;
  }

  public static PlayerRepresentation openPlayerDialog(PlayerRepresentation selection, List<PlayerRepresentation> players) {
    String title = "Add New Player";
    if (selection != null) {
      title = "Edit Player";
    }

    FXMLLoader fxmlLoader = new FXMLLoader(PlayerDialogController.class.getResource("dialog-player-edit.fxml"));
    fxmlLoader.setResources(Messages.getBundle());
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
          WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.no_data"), Messages.get("dialog.the_file") + file.getAbsolutePath() + Messages.get("dialog.does_not_contain_any_data_or_wasn"));
        }
      }
      catch (IOException e) {
        LOG.error(Messages.get("dialog.failed_to_create_temporary_file_for_text") + e.getMessage());
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.failed_to_create_temporary_file_for_text") + e.getMessage());
        return;
      }
    }
    Studio.open(file);
  }

  public static Stage createStudioDialogStage(String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(Studio.class.getResource(fxml));
    fxmlLoader.setResources(Messages.getBundle());
    String stateId = FilenameUtils.getBaseName(fxml);
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, Studio.stage, title);
  }

  public static Stage createStudioDialogStage(Stage stage, Class<?> clazz, String fxml, String title, String modalStateId) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    fxmlLoader.setResources(Messages.getBundle());
    String stateId = FilenameUtils.getBaseName(fxml);
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, stage, title, modalStateId);
  }


  public static Stage createStudioDialogStage(Class<?> clazz, String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    fxmlLoader.setResources(Messages.getBundle());
    String stateId = FilenameUtils.getBaseName(fxml);
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, Studio.stage, title, null);
  }

  public static Stage createStudioDialogStage(Class<?> clazz, String fxml, String title, String modalStateId) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    fxmlLoader.setResources(Messages.getBundle());
    String stateId = FilenameUtils.getBaseName(fxml);
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, Studio.stage, title, modalStateId);
  }

  public static Stage createStudioDialogStage(String stateId, Stage stage, Class<?> clazz, String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    fxmlLoader.setResources(Messages.getBundle());
    return WidgetFactory.createDialogStage(stateId, fxmlLoader, stage, title);
  }

  public static boolean openFrontendRunningWarning(Stage stage) {
    boolean local = client.getSystemService().isLocal();
    Frontend frontend = Studio.client.getFrontendService().getFrontendCached();

    if (!local) {
      ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithCheckbox(stage,
          FrontendUtil.replaceName(Messages.get("dialog.frontend_is_running"), frontend),
          Messages.get("dialog.kill_processes"), Messages.get("common.cancel"),
          FrontendUtil.replaceName(Messages.get("dialog.frontend_is_running_to_perform_this_operation"), frontend),
          null, Messages.get("dialog.switch_cabinet_to_maintenance_mode_2"));
      if (confirmationResult.isApplyClicked()) {
        client.getFrontendService().terminateFrontend();
        if (confirmationResult.isChecked()) {
          EventManager.getInstance().notifyMaintenanceMode(true);
        }
        return true;
      }
      return false;
    }
    return Dialogs.killFrontend();
  }

  public static boolean killFrontend() {
    Frontend frontend = client.getFrontendService().getFrontendCached();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        FrontendUtil.replaceNames(Messages.get("dialog.stop_all_emulators_and_frontend_processes"), frontend, null));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      JFXFuture.supplyAsync(() -> {
        return client.getFrontendService().terminateFrontend();
      }).thenAcceptLater((requestResult) -> {
        LOG.info("Kill frontend request finished.");
      });
      return true;
    }
    return false;
  }
}
