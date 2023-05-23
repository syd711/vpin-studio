package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.discord.DiscordClient;
import de.mephisto.vpin.connectors.discord.GuildInfo;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DiscordBotPreferencesController implements Initializable {
  @FXML
  private Label botTokenLabel;

  @FXML
  private TextField botChannelAllowList;

  @FXML
  private Button resetBtn;

  @FXML
  private ComboBox<DiscordServer> serverCombo;

  @FXML
  private ComboBox<DiscordChannel> channelCombo;

  @FXML
  private void onReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Token Reset", "Reset this Discord bot token?",
        "If you haven't stored it elsewhere, you have to re-generate a new one using the Discord developer portal.",
        "Yes, delete token");
    if (result.get().equals(ButtonType.OK)) {
      client.getPreferenceService().setPreference(PreferenceNames.DISCORD_BOT_TOKEN, "");
      botTokenLabel.setText("-");
      this.serverCombo.setDisable(true);
      this.serverCombo.setValue(null);
      this.channelCombo.setDisable(true);
      this.channelCombo.setValue(null);
    }
  }

  @FXML
  private void onTokenEdit() {
    String value = this.botTokenLabel.getText();
    if (value.length() == 1) {
      value = "";
    }

    String token = WidgetFactory.showInputDialog(Studio.stage, "Discord Bot Token", "Discord Bot Token",
        "Paste the bot token copied from the Discord developer portal here.", null, value);
    if (StringUtils.isEmpty(token)) {
      return;
    }

    Studio.stage.getScene().setCursor(Cursor.WAIT);
    new Thread(() -> {
      DiscordClient discordClient = null;
      try {
        discordClient = new DiscordClient(token.trim(), null);
        List<GuildInfo> guilds = discordClient.getGuilds();
        discordClient.shutdown();

        Platform.runLater(() -> {
          client.clearDiscordCache();
          Studio.stage.getScene().setCursor(Cursor.DEFAULT);
          client.getPreferenceService().setPreference(PreferenceNames.DISCORD_BOT_TOKEN, token.trim());
          botTokenLabel.setText(token.trim());
          serverCombo.setDisable(false);
          channelCombo.setDisable(false);
          resetBtn.setDisable(false);
          validateDefaultChannel();
        });
      } catch (Exception e) {
        if (discordClient != null) {
          discordClient.shutdown();
        }

        Platform.runLater(() -> {
          Studio.stage.getScene().setCursor(Cursor.DEFAULT);
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        });
      }
    }).start();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    serverCombo.setDisable(true);
    channelCombo.setDisable(true);
    resetBtn.setDisable(true);

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.DISCORD_BOT_TOKEN);
    String token = !StringUtils.isEmpty(preference.getValue()) ? preference.getValue() : "-";
    botTokenLabel.setText(token);
    if (!StringUtils.isEmpty(token)) {
      serverCombo.setDisable(false);
      channelCombo.setDisable(false);
      resetBtn.setDisable(false);
    }

    BindingUtil.bindTextField(botChannelAllowList, PreferenceNames.DISCORD_BOT_ALLOW_LIST, "");

    validateDefaultChannel();

    serverCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_GUILD_ID, newValue.getId());
        validateDefaultChannel();
      }
      else {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_GUILD_ID, "");
      }
    });

    channelCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_CHANNEL_ID, newValue.getId());
      }
      else {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_CHANNEL_ID, "");
      }
    });
  }

  private void validateDefaultChannel() {
    client.clearDiscordCache();

    List<DiscordServer> servers = client.getDiscordService().getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serverCombo.setItems(FXCollections.observableList(discordServers));

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.DISCORD_GUILD_ID);
    PreferenceEntryRepresentation channelPreference = client.getPreference(PreferenceNames.DISCORD_CHANNEL_ID);

    long serverId = preference.getLongValue();
    if (serverId > 0) {
      DiscordServer discordServer = client.getDiscordServer(serverId);
      if (discordServer != null) {
        serverCombo.setValue(discordServer);
        List<DiscordChannel> discordChannels = client.getDiscordService().getDiscordChannels(discordServer.getId());
        channelCombo.setItems(FXCollections.observableArrayList(discordChannels));

        long channelId = channelPreference.getLongValue();
        if (channelId > 0) {
          Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() == channelId).findFirst();
          first.ifPresent(discordChannel -> channelCombo.setValue(discordChannel));
        }
      }
    }
  }
}
