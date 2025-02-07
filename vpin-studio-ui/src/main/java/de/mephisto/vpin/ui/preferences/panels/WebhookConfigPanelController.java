package de.mephisto.vpin.ui.preferences.panels;

import de.mephisto.vpin.restclient.webhooks.Webhook;
import de.mephisto.vpin.restclient.webhooks.WebhookEventType;
import de.mephisto.vpin.restclient.webhooks.WebhookType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WebhookConfigPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(WebhookConfigPanelController.class);


  @FXML
  private Label titleLabel;

  @FXML
  private TextField endpointText;
  @FXML
  private TextField parametersText;
  @FXML
  private CheckBox createCheckbox;
  @FXML
  private CheckBox updateCheckbox;
  @FXML
  private CheckBox deleteCheckbox;

  private Webhook webhook;

  public void setData(Webhook webhook, WebhookType webhookType) {
    this.webhook = webhook;
    endpointText.setText(webhook.getEndpoint());

    List<WebhookEventType> subscribe = webhook.getSubscribe();
    createCheckbox.setSelected(subscribe.contains(WebhookEventType.create));
    updateCheckbox.setSelected(subscribe.contains(WebhookEventType.update));
    deleteCheckbox.setSelected(subscribe.contains(WebhookEventType.delete));

    switch (webhookType) {
      case game:
        titleLabel.setText("Games Webhook");
        break;
      case score:
        titleLabel.setText("Highscores Webhook");
        deleteCheckbox.setVisible(false);
        break;
      case player:
        titleLabel.setText("Players Webhook");
        break;
    }
  }

  public void applyValues() {
    webhook.setEndpoint(endpointText.getText());
    webhook.setParameterValue(parametersText.getText());

    List<WebhookEventType> subscriptions = new ArrayList<>();
    if (createCheckbox.isSelected()) {
      subscriptions.add(WebhookEventType.create);
    }
    if (updateCheckbox.isSelected()) {
      subscriptions.add(WebhookEventType.update);
    }
    if (deleteCheckbox.isSelected()) {
      subscriptions.add(WebhookEventType.delete);
    }
    webhook.setSubscribe(subscriptions);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    deleteCheckbox.managedProperty().bindBidirectional(deleteCheckbox.visibleProperty());
  }
}
