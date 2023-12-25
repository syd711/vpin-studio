package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.system.SystemData;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.UpdateInfoDialog;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.launcher.InstallationDialogController;
import de.mephisto.vpin.ui.players.PlayerDialogController;
import de.mephisto.vpin.ui.preferences.ManiaAccountDialogController;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
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

import static de.mephisto.vpin.ui.Studio.client;

public class Dialogs {
  private final static Logger LOG = LoggerFactory.getLogger(Dialogs.class);

  public static void openUpdateInfoDialog(String version, boolean force) {
    PreferenceEntryRepresentation doNotShowAgainPref = client.getPreferenceService().getPreference(PreferenceNames.UI_DO_NOT_SHOW_AGAINS);
    List<String> csvValue = doNotShowAgainPref.getCSVValue();
    if (force || !csvValue.contains(PreferenceNames.UI_DO_NOT_SHOW_AGAIN_UPDATE_INFO)) {
      FXMLLoader fxmlLoader = new FXMLLoader(UpdateInfoDialog.class.getResource("dialog-update-info.fxml"));
      Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, "Release Notes for " + version);
      stage.showAndWait();
    }

    if (!csvValue.contains(PreferenceNames.UI_DO_NOT_SHOW_AGAIN_UPDATE_INFO)) {
      csvValue.add(PreferenceNames.UI_DO_NOT_SHOW_AGAIN_UPDATE_INFO);
      client.getPreferenceService().setPreference(PreferenceNames.UI_DO_NOT_SHOW_AGAINS, String.join(",", csvValue));
    }
  }

  public static boolean openUpdateDialog() {
    Stage stage = createStudioDialogStage("dialog-update.fxml", "VPin Studio Updater");
    stage.showAndWait();
    return true;
  }

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


  public static boolean openInstallerDialog() {
    Stage stage = createStudioDialogStage(InstallationDialogController.class, "dialog-installer.fxml", "Visual Studio Server Installation");
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

  public static Stage createStudioDialogStage(String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(Studio.class.getResource(fxml));
    return WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
  }

  public static Stage createStudioDialogStage(Stage stage, Class clazz, String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    return WidgetFactory.createDialogStage(fxmlLoader, stage, title);
  }

  public static Stage createStudioDialogStage(Class clazz, String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    return WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
  }

  public static boolean openPopperRunningWarning(Stage stage) {
    boolean local = client.getSystemService().isLocal();
    if (!local) {
      ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithCheckbox(stage, "PinUP Popper/VPinballX is running.", "Kill Processes", "Cancel",
        "PinUP Popper and/or VPinballX is running. To perform this operation, you have to close it.", null, "Switch cabinet to maintenance mode");
      if (confirmationResult.isApplied()) {
        client.getPinUPPopperService().terminatePopper();
        if (confirmationResult.isChecked()) {
          EventManager.getInstance().notifyMaintenanceMode(true);
        }
        return true;
      }
      return false;
    }
    else {
      Optional<ButtonType> buttonType = WidgetFactory.showAlertOption(stage, "PinUP Popper/VPinballX is running.", "Kill Processes", "Cancel",
        "PinUP Popper and/or VPinballX is running. To perform this operation, you have to close it.",
        null);
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
        client.getPinUPPopperService().terminatePopper();
        return true;
      }
      return false;
    }
  }
}
