package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.webhooks.WebhookSet;
import de.mephisto.vpin.restclient.webhooks.WebhookSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class WebhooksPreferencesController implements Initializable {

  @FXML
  private TableView<WebhookSet> tableView;

  @FXML
  private TableColumn<WebhookSet, String> nameColumn;

  @FXML
  private TableColumn<WebhookSet, String> enabledColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button editBtn;
  private WebhookSettings webhookSettings;


  @FXML
  private void onLinkClick(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Studio.browse(linkText);
  }

  @FXML
  private void onReload() {
    reload();
  }

  @FXML
  private void onEdit() {
    WebhookSet selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      TableDialogs.openWebhooksDialog(webhookSettings, selectedItem);
      reload();
    }
  }

  @FXML
  private void onAdd() {
    TableDialogs.openWebhooksDialog(webhookSettings, null);
    reload();
  }

  @FXML
  private void onDelete() {
    WebhookSet selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Webhook Set \"" + selectedItem.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          WebhookSettings webhookSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WEBHOOK_SETTINGS, WebhookSettings.class);
          webhookSettings.remove(selectedItem);
          client.getPreferenceService().setJsonPreference(webhookSettings);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Error deleting \"" + selectedItem.getName() + "\": " + e.getMessage());
        } finally {
          reload();
        }
      }
    }
  }

  private void reload() {
    client.getPreferenceService().clearCache(PreferenceNames.WEBHOOK_SETTINGS);
    webhookSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WEBHOOK_SETTINGS, WebhookSettings.class);
    List<WebhookSet> sets = webhookSettings.getSets();
    tableView.setItems(FXCollections.observableList(sets));
    tableView.refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    webhookSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WEBHOOK_SETTINGS, WebhookSettings.class);

    tableView.setPlaceholder(new Label("              No webhook sets found.\nAdd a webhook set to connect with other systems."));
    deleteBtn.setDisable(true);
    editBtn.setDisable(true);

    nameColumn.setCellValueFactory(cellData -> {
      WebhookSet value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });

    enabledColumn.setCellValueFactory(cellData -> {
      WebhookSet value = cellData.getValue();
      if (value.isEnabled()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
    });

    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      deleteBtn.setDisable(disable);
      editBtn.setDisable(disable);
    });

    tableView.setRowFactory(tv -> {
      TableRow<WebhookSet> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onEdit();
        }
      });
      return row;
    });

    reload();
  }
}
