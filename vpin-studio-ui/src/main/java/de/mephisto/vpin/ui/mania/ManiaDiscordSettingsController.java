package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.User;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.ui.HeaderResizeableController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.util.ManiaUrlFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

/**
 * Settings of the VPin Mania Discord Community Bot.
 * They require a VPin Mania user account linked to Discord, which is done on the VPin Mania website.
 */
public class ManiaDiscordSettingsController implements Initializable, ManiaSettingsPage {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaDiscordSettingsController.class);

  @FXML
  private CheckBox shareDiscordActivityCheckbox;

  @FXML
  private CheckBox participateInDiscordChallengesCheckbox;

  @FXML
  private CheckBox challengeBadgeCheckbox;

  @FXML
  private CheckBox challengeResetHighscoresCheckbox;

  @FXML
  private VBox discordSettingsBox;

  @FXML
  private VBox discordNotLinkedInfo;

  private ManiaSettings settings;

  @FXML
  private void onOpenManiaSettings() {
    Studio.browse(ManiaUrlFactory.createSettingsUrl());
  }

  @FXML
  private void onAddDiscordBot() {
    Studio.browse(ManiaUrlFactory.createDiscordBotInviteUrl());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  @Override
  public void setData(ManiaSettingsData data) {
    if (!data.isRegistered()) {
      HeaderResizeableController.toggleManiaView();
      return;
    }

    settings = data.getSettings();
    User user = data.getUser();

    boolean linked = user != null && user.isDiscordLinked();
    discordSettingsBox.setDisable(!linked);
    discordNotLinkedInfo.setVisible(!linked);
    discordNotLinkedInfo.setManaged(!linked);

    shareDiscordActivityCheckbox.setSelected(settings.isShareDiscordActivity());
    shareDiscordActivityCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        settings.setShareDiscordActivity(newValue);
        client.getPreferenceService().setJsonPreference(settings);
      }
      catch (Exception e) {
        LOG.error("Failed to save mania settings: " + e.getMessage(), e);
      }
    });

    participateInDiscordChallengesCheckbox.setSelected(user == null || user.isParticipatingInDiscordChallenges());
    participateInDiscordChallengesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateChallengeParticipation(newValue));

    // resetting highscores is pointless if the user does not participate in any challenge
    challengeResetHighscoresCheckbox.disableProperty().bind(participateInDiscordChallengesCheckbox.selectedProperty().not());
    challengeResetHighscoresCheckbox.setSelected(settings.isChallengeResetHighscores());
    challengeResetHighscoresCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        settings.setChallengeResetHighscores(newValue);
        client.getPreferenceService().setJsonPreference(settings);
      }
      catch (Exception e) {
        LOG.error("Failed to save mania settings: " + e.getMessage(), e);
      }
    });

    challengeBadgeCheckbox.setSelected(settings.isChallengeBadgeEnabled());
    challengeBadgeCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        settings.setChallengeBadgeEnabled(newValue);
        client.getPreferenceService().setJsonPreference(settings);
      }
      catch (Exception e) {
        LOG.error("Failed to save mania settings: " + e.getMessage(), e);
      }
    });
  }

  private void updateChallengeParticipation(boolean participate) {
    participateInDiscordChallengesCheckbox.setDisable(true);
    JFXFuture.runAsync(() -> {
          try {
            maniaClient.getUserClient().setParticipateInDiscordChallenges(participate);
          }
          catch (Exception e) {
            throw new JFXFuture.JFXRuntimeException(e.getMessage(), e);
          }
        })
        .thenLater(() -> {
          participateInDiscordChallengesCheckbox.setDisable(false);
          // the participation is stored on VPin Mania, so the listeners (e.g. the challenges view) are notified manually
          client.getPreferenceService().notifyPreferenceChange(PreferenceNames.MANIA_SETTINGS, settings);
        })
        .onErrorLater(e -> {
          LOG.error("Failed to update the Discord challenge participation: {}", e.getMessage(), e);
          participateInDiscordChallengesCheckbox.setDisable(false);
          WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("mania.mania_discord_settings.participation_update_failed"), e.getMessage());
        });
  }
}
