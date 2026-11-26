package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mcp.McpSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Settings of the MCP endpoint of the server, which lets AI assistants like Claude query the tables of the cabinet.
 */
public class McpPreferencesController implements Initializable {

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private CheckBox readOnlyCheckbox;

  @FXML
  private Pane connectionPanel;

  @FXML
  private TextField urlText;

  @FXML
  private TextField commandText;

  private McpSettings settings;

  @FXML
  private void onCopyUrl() {
    copy(urlText.getText());
  }

  @FXML
  private void onCopyCommand() {
    copy(commandText.getText());
  }

  private void save() {
    client.getPreferenceService().setJsonPreference(settings);
    refresh();
  }

  private void refresh() {
    boolean enabled = settings.isEnabled();
    connectionPanel.setDisable(!enabled);

    String url = getMcpUrl();
    urlText.setText(url);
    commandText.setText("claude mcp add --transport http vpin-studio " + url);
  }

  private static String getMcpUrl() {
    String baseUrl = client.getRestClient().getBaseUrl();
    return StringUtils.appendIfMissing(baseUrl, "/") + "mcp";
  }

  private static void copy(String text) {
    ClipboardContent content = new ClipboardContent();
    content.putString(text);
    Clipboard.getSystemClipboard().setContent(content);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MCP_SETTINGS, McpSettings.class);
    if (settings == null) {
      //servers older than this client don't know the MCP settings
      settings = new McpSettings();
    }

    enabledCheckbox.setSelected(settings.isEnabled());
    enabledCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      settings.setEnabled(newValue);
      save();
    });

    readOnlyCheckbox.setSelected(settings.isReadOnly());
    readOnlyCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      settings.setReadOnly(newValue);
      save();
    });

    refresh();
  }
}
