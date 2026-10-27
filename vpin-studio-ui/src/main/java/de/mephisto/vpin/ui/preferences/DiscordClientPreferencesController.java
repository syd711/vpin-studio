package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordClientSettings;
import de.mephisto.vpin.restclient.discord.DiscordClientStatus;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DiscordClientPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordClientPreferencesController.class);

  @FXML
  private CheckBox launchOnStartupCheckbox;

  @FXML
  private CheckBox statusUpdateCheckbox;

  @FXML
  private Pane installedInfo;

  @FXML
  private Label installedLabel;

  @FXML
  private Pane notInstalledInfo;

  @FXML
  private Button launchBtn;

  @FXML
  private void onLaunch() {
    launchBtn.setDisable(true);
    client.getDiscordService().launchDiscordClient();
  }

  private void refresh() {
    DiscordClientStatus status = client.getDiscordService().getDiscordClientStatus();
    boolean installed = status != null && status.isInstalled();

    installedInfo.setVisible(installed);
    installedInfo.setManaged(installed);
    notInstalledInfo.setVisible(!installed);
    notInstalledInfo.setManaged(!installed);
    if (installed) {
      installedLabel.setText(Messages.get("pref.discord_client.discord_is_installed", status.getInstallationFolder()));
    }

    launchOnStartupCheckbox.setDisable(!installed);
    statusUpdateCheckbox.setDisable(!installed);
    launchBtn.setDisable(!installed || status.isRunning());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    DiscordClientSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.DISCORD_CLIENT_SETTINGS, DiscordClientSettings.class);

    launchOnStartupCheckbox.setSelected(settings.isLaunchOnStartup());
    launchOnStartupCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      settings.setLaunchOnStartup(t1);
      client.getPreferenceService().setJsonPreference(settings);
    });

    statusUpdateCheckbox.setSelected(settings.isStatusUpdateEnabled());
    statusUpdateCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      settings.setStatusUpdateEnabled(t1);
      client.getPreferenceService().setJsonPreference(settings);
    });

    try {
      refresh();
    }
    catch (Exception e) {
      LOG.error("Failed to read Discord client status: {}", e.getMessage(), e);
    }
  }
}
