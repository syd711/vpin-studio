package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.ui.HeaderResizeableController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaAccountSettingsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaAccountSettingsController.class);

  @FXML
  private VBox preferencesPanel;

  @FXML
  private Label idLabel;


  @FXML
  private void onIdCopy() {
    Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
    if (cabinet != null) {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      content.putString(cabinet.getUuid());
      clipboard.setContent(content);
    }
  }

  @FXML
  private void onAccountDelete() {
    boolean deregistered = ManiaHelper.deregister();
    if (deregistered) {

    }

    HeaderResizeableController.toggleManiaView();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    preferencesPanel.managedProperty().bindBidirectional(preferencesPanel.visibleProperty());

    if (ManiaHelper.isRegistered()) {
      Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
      if (cabinet != null) {
        idLabel.setText(cabinet.getUuid());
      }

      preferencesPanel.setVisible(cabinet != null);
    }
    else {
      HeaderResizeableController.toggleManiaView();
    }
  }
}
