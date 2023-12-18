package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountPreferencesController implements Initializable {

  @FXML
  private VBox accountPanel;

  @FXML
  private VBox registrationPanel;

  @FXML
  private TextField displayNameText;

  @FXML
  private TextField cabinetIdText;

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
    String text = displayNameText.getText();
    if (!StringUtils.isEmpty(text)) {
      String s = WidgetFactory.showInputDialog(Studio.stage, "Display Name", "Enter this display name used for friends and competitions.", null, null, text);
      if(!StringUtils.isEmpty(s)) {

      }
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    accountPanel.managedProperty().bindBidirectional(accountPanel.visibleProperty());
    registrationPanel.managedProperty().bindBidirectional(registrationPanel.visibleProperty());

    accountPanel.setVisible(true);
    registrationPanel.setVisible(false);
  }
}
