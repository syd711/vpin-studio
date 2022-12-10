package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.connectors.discord.DiscordClient;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.WidgetFactory;
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
  private Button connectionTestBtn;

  @FXML
  private void onBotTutorial() {
    Dialogs.openBotTutorial();
  }

  @FXML
  private void onConnectionTest() {
    new Thread(() -> {
      try {
        String token = botTokenText.getText();
        String serverId = serverIdText.getText();
        Studio.stage.getScene().setCursor(Cursor.WAIT);
        DiscordClient client = new DiscordClient(token, serverId);
        client.refreshMembers(discordMembers -> {
          Platform.runLater(() -> {
            Studio.stage.getScene().setCursor(Cursor.DEFAULT);
            WidgetFactory.showInformation("Read " + discordMembers.size() + " members.", "Test Successful");
          });
        }, throwable -> {
          Platform.runLater(() -> {
            Studio.stage.getScene().setCursor(Cursor.DEFAULT);
            WidgetFactory.showAlert(throwable.getMessage());
          });
        });
      } catch (Exception e) {
        Platform.runLater(() -> {
          Studio.stage.getScene().setCursor(Cursor.DEFAULT);
          WidgetFactory.showAlert(e.getMessage());
        });
      }
    }).start();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BindingUtil.bindTextField(serverIdText, PreferenceNames.DISCORD_GUILD_ID, null);
    BindingUtil.bindTextField(botTokenText, PreferenceNames.DISCORD_BOT_TOKEN, null);

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
