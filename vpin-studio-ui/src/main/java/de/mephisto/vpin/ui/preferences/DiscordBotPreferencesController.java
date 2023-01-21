package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.discord.DiscordClient;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class DiscordBotPreferencesController implements Initializable {

  @FXML
  private TextField serverIdText;

  @FXML
  private TextField botTokenText;

  @FXML
  private TextField botChannelAllowList;

  @FXML
  private Button connectionTestBtn;

  @FXML
  private void onBotTutorial() {
    Dialogs.openBotTutorial();
  }

  @FXML
  private void onBotServerIdTutorial() {
    Dialogs.openBotServerIdTutorial();
  }

  @FXML
  private void onBotTokenTutorial() {
    Dialogs.openBotTokenTutorial();
  }

  @FXML
  private void onConnectionTest() {
    new Thread(() -> {
      try {
        String token = botTokenText.getText();
        String serverId = serverIdText.getText();
        Studio.stage.getScene().setCursor(Cursor.WAIT);
        DiscordClient client = new DiscordClient(token, serverId, null);
        client.refreshMembers(discordMembers -> {
          Platform.runLater(() -> {
            Studio.stage.getScene().setCursor(Cursor.DEFAULT);
            WidgetFactory.showInformation(Studio.stage, "Test Successful", "The connection test was successful.", discordMembers.size() + " members have been found.");
          });
        }, throwable -> {
          Platform.runLater(() -> {
            Studio.stage.getScene().setCursor(Cursor.DEFAULT);
            WidgetFactory.showAlert(Studio.stage, throwable.getMessage());
          });
        });
      } catch (Exception e) {
        Platform.runLater(() -> {
          Studio.stage.getScene().setCursor(Cursor.DEFAULT);
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        });
      }
    }).start();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BindingUtil.bindTextField(serverIdText, PreferenceNames.DISCORD_GUILD_ID, null);
    BindingUtil.bindTextField(botTokenText, PreferenceNames.DISCORD_BOT_TOKEN, null);
    BindingUtil.bindTextField(botChannelAllowList, PreferenceNames.DISCORD_BOT_ALLOW_LIST, null);

    serverIdText.textProperty().addListener((observableValue, s, t1) -> validateInput());
    botTokenText.textProperty().addListener((observableValue, s, t1) -> validateInput());

    this.validateInput();
  }

  private void validateInput() {
    String token = botTokenText.getText();
    String serverId = serverIdText.getText();
    this.connectionTestBtn.setDisable(StringUtils.isEmpty(token) || StringUtils.isEmpty(serverId));
  }
}
