package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.achievements.AchievementsSettings;
import de.mephisto.vpin.restclient.achievements.VpxPluginStatus;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.achievements.AchievementsMigrationDialog;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.achievements.AchievementsUI;
import de.mephisto.vpin.ui.util.SystemUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * VPin Studio preferences: achievements (ported VPX Achievement Watcher) settings, tracked players and the
 * migration of Achievement Watcher data.
 */
public class AchievementsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(AchievementsPreferencesController.class);

  @FXML
  private CheckBox enabledCheckbox;
  @FXML
  private CheckBox cloudCheckbox;
  @FXML
  private VBox playersBox;
  @FXML
  private CheckBox liveCheckbox;
  @FXML
  private Button liveInstallBtn;
  @FXML
  private Button liveOpenFolderBtn;
  @FXML
  private Button liveUninstallBtn;
  @FXML
  private Button liveUpdateVpxBtn;
  @FXML
  private Label liveStatusLabel;

  private AchievementsSettings settings;
  private File pluginsFolder;

  @FXML
  private void onLiveInstall() {
    installPlugins();
  }

  @FXML
  private void onLiveOpenFolder() {
    if (pluginsFolder != null) {
      SystemUtil.openFolder(pluginsFolder);
    }
  }

  /**
   * The installed VPX is too old for the plugins: opens the Visual Pinball updates of the System Manager.
   */
  @FXML
  private void onLiveUpdateVpx() {
    if (PreferencesController.instance != null) {
      PreferencesController.instance.closePreferences();
    }
    NavigationController.navigateTo(NavigationItem.SystemManager, new NavigationOptions(ComponentType.vpinball));
  }

  @FXML
  private void onLiveUninstall() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("pref.achievements.live_uninstall_title"),
        Messages.get("pref.achievements.live_uninstall_text"), null, Messages.get("pref.achievements.live_uninstall"));
    if (result.isEmpty() || !result.get().equals(ButtonType.OK)) {
      return;
    }
    // switched off first, otherwise the server installs the plugins again on the next start
    liveCheckbox.setSelected(false);
    liveInstallBtn.setDisable(true);
    liveUninstallBtn.setDisable(true);
    liveStatusLabel.setStyle("");
    liveStatusLabel.setText(Messages.get("pref.achievements.live_uninstalling"));
    JFXFuture.supplyAsync(() -> client.getAchievementsService().uninstallVpxPlugins())
        .thenAcceptLater(status -> {
          showLiveStatus(status);
          // the live tracking is off now, so the state alone would hide why nothing was removed
          if (status != null && status.detail != null
              && (VpxPluginStatus.BUSY.equals(status.state) || VpxPluginStatus.FAILED.equals(status.state))) {
            liveStatusLabel.setText(status.detail);
            liveStatusLabel.setStyle("-fx-text-fill: #FF9800;");
          }
        })
        .onErrorLater(e -> {
          liveUninstallBtn.setDisable(false);
          liveStatusLabel.setText(e.getMessage());
        });
  }

  @FXML
  private void onMigrate() {
    AchievementsMigrationDialog.open();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getPreferenceService().getJsonPreference(PreferenceNames.ACHIEVEMENTS_SETTINGS, AchievementsSettings.class);
    if (settings == null) {
      settings = new AchievementsSettings();
    }
    enabledCheckbox.setSelected(settings.isEnabled());
    cloudCheckbox.setSelected(settings.isCloudSyncEnabled());
    enabledCheckbox.selectedProperty().addListener((obs, o, n) -> {
      settings.setEnabled(n);
      save();
    });
    cloudCheckbox.selectedProperty().addListener((obs, o, n) -> {
      settings.setCloudSyncEnabled(n);
      save();
    });
    liveCheckbox.setSelected(settings.isLiveTrackingEnabled());
    liveCheckbox.selectedProperty().addListener((obs, o, n) -> {
      settings.setLiveTrackingEnabled(n);
      save();
      if (n) {
        installPlugins();
      }
      else {
        // the server restores the official PinMAME plugin in the background
        liveInstallBtn.setDisable(true);
        liveStatusLabel.setText(Messages.get("pref.achievements.live_state.disabled"));
        liveUpdateVpxBtn.setVisible(false);
      }
    });
    liveInstallBtn.setDisable(!settings.isLiveTrackingEnabled());
    liveOpenFolderBtn.managedProperty().bind(liveOpenFolderBtn.visibleProperty());
    liveOpenFolderBtn.setVisible(false);
    liveUninstallBtn.managedProperty().bind(liveUninstallBtn.visibleProperty());
    liveUninstallBtn.setVisible(false);
    liveUpdateVpxBtn.managedProperty().bind(liveUpdateVpxBtn.visibleProperty());
    liveUpdateVpxBtn.setVisible(false);
    refreshLiveStatus();
    refreshPlayers();
  }

  private void installPlugins() {
    liveInstallBtn.setDisable(true);
    liveStatusLabel.setText(Messages.get("pref.achievements.live_installing"));
    JFXFuture.supplyAsync(() -> client.getAchievementsService().installVpxPlugins())
        .thenAcceptLater(this::showLiveStatus)
        .onErrorLater(e -> {
          liveInstallBtn.setDisable(!settings.isLiveTrackingEnabled());
          liveStatusLabel.setText(e.getMessage());
        });
  }

  private void refreshLiveStatus() {
    JFXFuture.supplyAsync(() -> client.getAchievementsService().getVpxPluginStatus())
        .thenAcceptLater(this::showLiveStatus)
        .onErrorLater(e -> liveStatusLabel.setText(""));
  }

  private void showLiveStatus(VpxPluginStatus status) {
    liveInstallBtn.setDisable(!settings.isLiveTrackingEnabled());
    updateOpenFolderButton(status);
    liveUninstallBtn.setDisable(false);
    liveUninstallBtn.setVisible(status != null && status.installed);
    if (status == null) {
      liveStatusLabel.setText("");
      return;
    }
    String state = settings.isLiveTrackingEnabled() ? status.state : VpxPluginStatus.DISABLED;
    liveUpdateVpxBtn.setVisible(VpxPluginStatus.VPX_UPDATE_NEEDED.equals(state) || VpxPluginStatus.VPX_OLD.equals(state));
    String text = Messages.get("pref.achievements.live_state." + state,
        status.installedTag != null ? status.installedTag : "", status.vpxVersion != null ? status.vpxVersion : "",
        status.requiredVpxVersion != null ? status.requiredVpxVersion : "");
    if (status.detail != null && (VpxPluginStatus.FAILED.equals(state) || VpxPluginStatus.REBUILD_NEEDED.equals(state))) {
      text += System.lineSeparator() + status.detail;
    }
    liveStatusLabel.setText(text);
    liveStatusLabel.setStyle("-fx-text-fill: " + (VpxPluginStatus.READY.equals(state) ? "#5CC46A"
        : VpxPluginStatus.DISABLED.equals(state) ? "#BDBDBD" : "#FF9800") + ";");
  }

  /**
   * The plugins folder lives on the cabinet, so the button is only offered for a local server once the plugins are
   * installed.
   */
  private void updateOpenFolderButton(VpxPluginStatus status) {
    pluginsFolder = null;
    if (status != null && status.installedTag != null && status.vpxFolder != null && client.getSystemService().isLocal()) {
      File folder = new File(status.vpxFolder, "plugins");
      if (folder.isDirectory()) {
        pluginsFolder = folder;
      }
    }
    liveOpenFolderBtn.setVisible(pluginsFolder != null);
  }

  private void save() {
    client.getPreferenceService().setJsonPreference(settings);
  }

  private void refreshPlayers() {
    JFXFuture.supplyAsync(() -> new PlayersData(client.getPlayerService().getPlayers(), AchievementsUI.getManiaLinkedPlayerIds()))
        .thenAcceptLater(data -> {
          List<PlayerRepresentation> players = data.players();
          playersBox.getChildren().clear();
          if (players.isEmpty()) {
            playersBox.getChildren().add(new Label(Messages.get("achievements.no_players")));
          }
          for (PlayerRepresentation p : players) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            CheckBox tracked = new CheckBox(p.getName() + (p.getInitials() != null ? " (" + p.getInitials() + ")" : ""));
            tracked.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            tracked.setMinWidth(260);
            tracked.setSelected(p.isAchievementsEnabled());
            tracked.selectedProperty().addListener((obs, o, n) -> {
              p.setAchievementsEnabled(n);
              try {
                client.getPlayerService().savePlayer(p);
              }
              catch (Exception e) {
                LOG.error("Failed to save player: {}", e.getMessage(), e);
              }
            });
            boolean linked = data.linkedPlayerIds().contains(p.getId());
            Label mania = new Label(linked ? Messages.get("pref.achievements.linked") : Messages.get("pref.achievements.not_linked"));
            mania.setStyle("-fx-text-fill: " + (linked ? "#5CC46A" : "#BDBDBD") + "; -fx-font-size: 13px;");
            row.getChildren().addAll(tracked, mania);
            playersBox.getChildren().add(row);
          }
        });
  }

  private record PlayersData(List<PlayerRepresentation> players, Set<Long> linkedPlayerIds) {
  }

}
