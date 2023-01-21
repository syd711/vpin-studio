package de.mephisto.vpin.ui.preferences;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ServiceInfoPreferencesController implements Initializable {

  @FXML
  private Label startupTimeLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private TextArea logTextArea;

  @FXML
  private void onReload() {
    String logs = client.logs();
    logTextArea.setText(logs);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Date startupTime = client.getStartupTime();
    startupTimeLabel.setText(DateFormat.getDateTimeInstance().format(startupTime));
    versionLabel.setText(client.version());

    onReload();
  }
}
