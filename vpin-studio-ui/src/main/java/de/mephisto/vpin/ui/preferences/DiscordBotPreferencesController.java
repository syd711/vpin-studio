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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DiscordBotPreferencesController implements Initializable {
  @FXML
  private TextField botTokenText;

  @FXML
  private TextField botChannelAllowList;

  @FXML
  private Button pasteButton;

  @FXML
  private Button resetButton;

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
      client.setPreference(PreferenceNames.DISCORD_BOT_TOKEN, "");
      botTokenText.setText("");
      this.serverCombo.setDisable(true);
      this.serverCombo.setValue(null);
      this.channelCombo.setDisable(true);
      this.channelCombo.setValue(null);
    }
  }

  @FXML
  private void onTokenPaste() {
    String token = Clipboard.getSystemClipboard().getString();
    if(StringUtils.isEmpty(token)) {
      WidgetFactory.showAlert(Studio.stage, "No Token", "The system clipboard contains no token value.", "Please copy the token value from the Discord developer portal and paste it here.");
      return;
    }

    Studio.stage.getScene().setCursor(Cursor.WAIT);
    new Thread(() -> {
      DiscordClient discordClient = null;
      try {
        discordClient = DiscordClient.create(token.trim(), null);
        List<GuildInfo> guilds = discordClient.getGuilds();
        Platform.runLater(() -> {
          Studio.stage.getScene().setCursor(Cursor.DEFAULT);
          client.setPreference(PreferenceNames.DISCORD_BOT_TOKEN, token.trim());
          botTokenText.setText(token.trim());
          serverCombo.setDisable(false);
          channelCombo.setDisable(false);
          validateDefaultChannel();
        });
        discordClient.shutdown();
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

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.DISCORD_BOT_TOKEN);
    String token = preference.getValue();
    botTokenText.setText(token);
    if(!StringUtils.isEmpty(token)) {
      serverCombo.setDisable(false);
      channelCombo.setDisable(false);
    }

    BindingUtil.bindTextField(botChannelAllowList, PreferenceNames.DISCORD_BOT_ALLOW_LIST, "");
    validateDefaultChannel();
  }

  private void validateDefaultChannel() {
    List<DiscordServer> servers = client.getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serverCombo.setItems(FXCollections.observableList(discordServers));

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.DISCORD_GUILD_ID);
    long longValue = preference.getLongValue();
    if (longValue > 0) {
      DiscordServer discordServer = client.getDiscordServer(longValue);
      if(discordServer != null) {
        serverCombo.setValue(discordServer);
        List<DiscordChannel> discordChannels = client.getDiscordChannels(discordServer.getId());
        channelCombo.setItems(FXCollections.observableArrayList(discordChannels));

        PreferenceEntryRepresentation channelPreference = client.getPreference(PreferenceNames.DISCORD_CHANNEL_ID);
        long channelId = channelPreference.getLongValue();
        if (channelId > 0) {
          Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() == channelId).findFirst();
          first.ifPresent(discordChannel -> channelCombo.setValue(discordChannel));
        }
      }
    }

    serverCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue != null) {
        client.setPreference(PreferenceNames.DISCORD_GUILD_ID, newValue.getId());
      }
      else {
        client.setPreference(PreferenceNames.DISCORD_GUILD_ID, "");
      }
    });

    channelCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue != null) {
        client.setPreference(PreferenceNames.DISCORD_CHANNEL_ID, newValue.getId());
      }
      else {
        client.setPreference(PreferenceNames.DISCORD_CHANNEL_ID, "");
      }
    });
  }
}
