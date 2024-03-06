package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.discord.DiscordWebhook;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class DiscordWebhookPreferencesController implements Initializable {

  @FXML
  private TextField webhookText;

  @FXML
  private Button testBtn;

  @FXML
  private void onTest() {
    Platform.runLater(() -> {
      String msg = WidgetFactory.showInputDialog(Studio.stage, "Webhook Test", "Test Message", "The given text will be passed to the configured Discord channel.", null, null);
      if (!StringUtils.isEmpty(msg)) {
        try {
          DiscordWebhook.call(this.webhookText.getText(), msg);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
//    BindingUtil.bindTextField(webhookText, PreferenceNames.DISCORD_WEBHOOK_URL, null);
    webhookText.textProperty().addListener((observableValue, s, t1) -> validateInput());
    this.validateInput();
  }

  private void validateInput() {
    String webhook = webhookText.getText();
    this.testBtn.setDisable(StringUtils.isEmpty(webhook));
  }
}
