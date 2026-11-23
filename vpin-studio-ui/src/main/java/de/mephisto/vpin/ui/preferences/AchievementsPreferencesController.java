package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.achievements.AchievementsSettings;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.achievements.AchievementsMigrationDialog;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

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

  private AchievementsSettings settings;

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
    refreshPlayers();
  }

  private void save() {
    client.getPreferenceService().setJsonPreference(settings);
  }

  private void refreshPlayers() {
    JFXFuture.supplyAsync(() -> client.getPlayerService().getPlayers())
        .thenAcceptLater(players -> {
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
            Label mania = new Label(p.getManiaAccountUuid() != null ? Messages.get("pref.achievements.linked") : Messages.get("pref.achievements.not_linked"));
            mania.setStyle("-fx-text-fill: " + (p.getManiaAccountUuid() != null ? "#5CC46A" : "#BDBDBD") + "; -fx-font-size: 13px;");
            row.getChildren().addAll(tracked, mania);
            playersBox.getChildren().add(row);
          }
        });
  }

}
