package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.discord.DiscordClient;
import de.mephisto.vpin.connectors.discord.GuildInfo;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import javafx.application.Platform;
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

    BindingUtil.bindTextField(botTokenText, PreferenceNames.DISCORD_BOT_TOKEN, null);
    BindingUtil.bindTextField(botChannelAllowList, PreferenceNames.DISCORD_BOT_ALLOW_LIST, null);

    botTokenText.textProperty().addListener((observableValue, s, t1) -> validateInput());
    this.validateInput();
  }

  private void validateInput() {
    String token = botTokenText.getText();
    this.connectionTestBtn.setDisable(StringUtils.isEmpty(token));

    if (!this.connectionTestBtn.isDisabled()) {
      serverCombo.setDisable(false);
      channelCombo.setDisable(false);

      List<DiscordServer> servers = client.getDiscordServers();
      ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
      serverCombo.getItems().addAll(discordServers);
      serverCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
        channelCombo.setItems(FXCollections.observableArrayList(client.getDiscordChannels(t1.getId())));
      });
    }
    else {
      serverCombo.setDisable(true);
      channelCombo.setDisable(true);
    }
  }
}
