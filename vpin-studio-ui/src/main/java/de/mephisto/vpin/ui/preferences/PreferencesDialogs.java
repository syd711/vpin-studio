package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.util.ini.IniSettings;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class PreferencesDialogs {
  public static void openBotWhitelistDialog(DiscordBotPreferencesController preferencesController) {
    Stage stage = Dialogs.createStudioDialogStage(DiscordBotAllowListDialogController.class, "preference-bot-allowlist-dialog.fxml", "Bot Allow-List");
    DiscordBotAllowListDialogController controller = (DiscordBotAllowListDialogController) stage.getUserData();
    controller.setPreferencesController(preferencesController);
    stage.showAndWait();
  }

  public static void openBotServerIdTutorial() {
    Stage stage = Dialogs.createStudioDialogStage("dialog-bot-server-id-tutorial.fxml", "Server ID Instructions");
    stage.showAndWait();
  }

  public static void openBotTokenTutorial() {
    Stage stage = Dialogs.createStudioDialogStage("dialog-bot-token-tutorial.fxml", "Bot Token Instructions");
    stage.showAndWait();
  }

  public static void openBotTutorial() {
    Stage stage = Dialogs.createStudioDialogStage("dialog-bot-tutorial.fxml", "Discord Bot Instructions");
    stage.showAndWait();
  }

  public static void openPINemHiUIDialog(IniSettings settings) {
    Stage stage = Dialogs.createStudioDialogStage(PINemHiUIPreferenceController.class, "preference-pinemhi-ui.fxml", "PINemHi UI Settings");
    PINemHiUIPreferenceController controller = (PINemHiUIPreferenceController) stage.getUserData();
    controller.setSettings(settings);
    stage.showAndWait();
  }
}
