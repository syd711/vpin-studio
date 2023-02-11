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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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
  private Button connectionTestBtn;

  @FXML
  private ComboBox<DiscordServer> serverCombo;

  @FXML
  private ComboBox<DiscordChannel> channelCombo;

  @FXML
  private void onConnectionTest() {
    new Thread(() -> {
      DiscordClient client = null;
      try {
        String token = botTokenText.getText();
        Studio.stage.getScene().setCursor(Cursor.WAIT);
        client = DiscordClient.create(token, null);
        List<GuildInfo> guilds = client.getGuilds();
        Platform.runLater(() -> {
          Studio.stage.getScene().setCursor(Cursor.DEFAULT);
          WidgetFactory.showInformation(Studio.stage, "Test Successful", "The connection test was successful.", "The bot is member of " + guilds.size() + " server(s).");
          validateInput();
        });
        client.shutdown();
      } catch (Exception e) {
        if (client != null) {
          client.shutdown();
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

    BindingUtil.bindTextField(botTokenText, PreferenceNames.DISCORD_BOT_TOKEN, "");
    BindingUtil.bindTextField(botChannelAllowList, PreferenceNames.DISCORD_BOT_ALLOW_LIST, "");


    botTokenText.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
       validateInput();
      }
    });
    this.validateInput();
  }

  private void validateInput() {
    boolean empty = StringUtils.isEmpty(botTokenText.getText());
    connectionTestBtn.setDisable(empty);
    if(empty) {
      connectionTestBtn.setDisable(true);
      serverCombo.setDisable(true);
      channelCombo.setDisable(true);
      return;
    }
    validateDefaultChannel();
  }

  private void validateDefaultChannel() {
    serverCombo.setDisable(true);
    channelCombo.setDisable(true);
    List<DiscordServer> servers = client.getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serverCombo.setItems(FXCollections.observableList(discordServers));


    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.DISCORD_GUILD_ID);
    long longValue = preference.getLongValue();
    if (longValue > 0) {
      DiscordServer discordServer = client.getDiscordServer(longValue);
      if(discordServer != null) {
        serverCombo.setDisable(false);
        channelCombo.setDisable(false);

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
