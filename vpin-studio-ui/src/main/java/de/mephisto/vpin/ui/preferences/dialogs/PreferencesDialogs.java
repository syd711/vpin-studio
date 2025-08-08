package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.restclient.util.ini.IniSettings;
import de.mephisto.vpin.ui.preferences.DiscordBotPreferencesController;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.stage.Stage;

public class PreferencesDialogs {
  public static void openMediaSourceFolderDialog(@NonNull TableAssetSource source) {
    Stage stage = Dialogs.createStudioDialogStage(TableAssetSourceFolderDialogController.class, "dialog-asset-source-folder.fxml", "Media Source");
    TableAssetSourceFolderDialogController controller = (TableAssetSourceFolderDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();
  }

  public static void openBotWhitelistDialog(DiscordBotPreferencesController preferencesController) {
    Stage stage = Dialogs.createStudioDialogStage(DiscordBotAllowListDialogController.class, "preference-bot-allowlist-dialog.fxml", "Bot Allow-List");
    DiscordBotAllowListDialogController controller = (DiscordBotAllowListDialogController) stage.getUserData();
    controller.setPreferencesController(preferencesController);
    stage.showAndWait();
  }

  public static void openBotServerIdTutorial() {
    Stage stage = Dialogs.createStudioDialogStage(PreferencesDialogs.class, "dialog-bot-server-id-tutorial.fxml", "Server ID Instructions");
    stage.showAndWait();
  }

  public static void openBotTokenTutorial() {
    Stage stage = Dialogs.createStudioDialogStage(PreferencesDialogs.class, "dialog-bot-token-tutorial.fxml", "Bot Token Instructions");
    stage.showAndWait();
  }

  public static void openButtonRecorder() {
    Stage stage = Dialogs.createStudioDialogStage(BtnRecorderDialogController.class, "preference-table-pause-btn-recorder-dialog.fxml", "Controller Bindings");
    BtnRecorderDialogController controller = (BtnRecorderDialogController) stage.getUserData();
    stage.showAndWait();
  }

  public static void openBotTutorial() {
    Stage stage = Dialogs.createStudioDialogStage(PreferencesDialogs.class, "dialog-bot-tutorial.fxml", "Discord Bot Instructions");
    stage.showAndWait();
  }

  public static void openPINemHiUIDialog(IniSettings settings) {
    Stage stage = Dialogs.createStudioDialogStage(PINemHiUIPreferenceController.class, "preference-pinemhi-ui.fxml", "PINemHi UI Settings");
    PINemHiUIPreferenceController controller = (PINemHiUIPreferenceController) stage.getUserData();
    controller.setSettings(settings);
    stage.showAndWait();
  }

  public static void openPauseMenuTestDialog() {
    Stage stage = Dialogs.createStudioDialogStage(TablePauseTestDialogController.class, "preference-table-pause-test-dialog.fxml", "Pause Menu Test");
    stage.showAndWait();
  }

  public static void openRestoreBackupDialog() {
    Stage stage = Dialogs.createStudioDialogStage(RestoreBackupDialogController.class, "dialog-restore-backup.fxml", "Restore Backup");
    RestoreBackupDialogController controller = (RestoreBackupDialogController) stage.getUserData();
    controller.setStage(stage);
    stage.showAndWait();

  }
}
