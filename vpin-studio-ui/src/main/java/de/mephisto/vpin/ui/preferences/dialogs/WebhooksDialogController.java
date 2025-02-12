package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.webhooks.Webhook;
import de.mephisto.vpin.restclient.webhooks.WebhookSet;
import de.mephisto.vpin.restclient.webhooks.WebhookSettings;
import de.mephisto.vpin.restclient.webhooks.WebhookType;
import de.mephisto.vpin.ui.preferences.panels.WebhookConfigPanelController;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class WebhooksDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(WebhooksDialogController.class);

  @FXML
  private Button saveBtn;

  @FXML
  private VBox hookList;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private TextField nameField;

  private WebhookSettings webhookSettings;
  private WebhookSet webhookSet;

  private List<WebhookConfigPanelController> webhookPanelsControllers = new ArrayList<>();

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    for (WebhookConfigPanelController webhookPanelsController : webhookPanelsControllers) {
      webhookPanelsController.applyValues();
    }

    List<WebhookSet> collect = new ArrayList<>(webhookSettings.getSets().stream().filter(s -> !s.getUuid().equals(webhookSet.getUuid())).collect(Collectors.toList()));
    collect.add(webhookSet);
    webhookSettings.setSets(collect);
    client.getPreferenceService().setJsonPreference(webhookSettings);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(@NonNull WebhookSettings webhookSettings, @Nullable WebhookSet set) {
    this.webhookSettings = webhookSettings;
    webhookSet = set;
    if (webhookSet == null) {
      webhookSet = new WebhookSet();
      webhookSet.setUuid(UUID.randomUUID().toString());
      webhookSet.setName("My Webhooks");
    }

    nameField.setText(webhookSet.getName());
    enabledCheckbox.setSelected(webhookSet.isEnabled());

    addWebhookPanel(webhookSet.getGames(), WebhookType.game);
    addWebhookPanel(webhookSet.getScores(), WebhookType.score);
    addWebhookPanel(webhookSet.getPlayers(), WebhookType.player);

    saveBtn.setDisable(StringUtils.isEmpty(webhookSet.getName()));
  }

  private void addWebhookPanel(Webhook webhook, WebhookType webhookType) {
    try {
      FXMLLoader loader = new FXMLLoader(WebhookConfigPanelController.class.getResource("webhook-config-panel.fxml"));
      Parent builtInRoot = loader.load();
      WebhookConfigPanelController controller = loader.getController();
      webhookPanelsControllers.add(controller);
      controller.setData(webhook, webhookType);
      hookList.getChildren().add(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load webhook panel: " + e.getMessage(), e);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    saveBtn.setDisable(true);
    nameField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        webhookSet.setName(newValue);
        saveBtn.setDisable(StringUtils.isEmpty(webhookSet.getName()));
      }
    });

    enabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        webhookSet.setEnabled(newValue);
      }
    });
  }
}
