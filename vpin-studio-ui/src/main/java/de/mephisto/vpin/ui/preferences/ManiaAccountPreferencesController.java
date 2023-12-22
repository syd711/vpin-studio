package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class ManiaAccountPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaAccountPreferencesController.class);

  @FXML
  private VBox accountPanel;

  @FXML
  private VBox registrationPanel;

  @FXML
  private TextField displayNameText;

  @FXML
  private TextField cabinetIdText;

  @FXML
  private TextField initialsText;

  private ManiaAccountRepresentation account;

  @FXML
  private void onCopy() {
    String text = cabinetIdText.getText();
    if (!StringUtils.isEmpty(text)) {
      final ClipboardContent content = new ClipboardContent();
      content.putString(text);
      Clipboard.getSystemClipboard().setContent(content);
    }
  }

  @FXML
  private void onNameEdit() {
    Dialogs.openManiaAccountDialog("VPin Mania Account Registration", this.account);
    refreshView();
  }

  @FXML
  private void onRegister() {
    Dialogs.openManiaAccountDialog("VPin Mania Account Registration", null);
    refreshView();
  }

  @FXML
  private void onDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete", "Delete your VPin-Mania account?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getManiaService().deleteAccount();
      } catch (Exception e) {
        WidgetFactory.showAlert(stage, "Error", "Error deleting account: " + e.getMessage());
      }
      refreshView();
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    accountPanel.managedProperty().bindBidirectional(accountPanel.visibleProperty());
    registrationPanel.managedProperty().bindBidirectional(registrationPanel.visibleProperty());

    refreshView();
  }

  private void refreshView() {
    account = Studio.client.getManiaService().getAccount();
    accountPanel.setVisible(account != null);
    registrationPanel.setVisible(account == null);

    if(account != null) {
      displayNameText.setText(account.getDisplayName());
      cabinetIdText.setText(account.getCabinetId());
      initialsText.setText(account.getInitials());
    }
  }
}
