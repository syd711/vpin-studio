package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AutoMatchDialogController implements Initializable, DialogController {

  @FXML
  private Label titleLabel;

  @FXML
  private Label descriptionLabel;

  @FXML
  private Label hintLabel;

  @FXML
  private HBox overwriteMatchingsRow;

  @FXML
  private CheckBox overwriteMatchingsCheckbox;

  @FXML
  private CheckBox overwriteMetadataCheckbox;

  @FXML
  private Button confirmBtn;

  private boolean confirmed = false;

  @FXML
  private void onCancelClick(ActionEvent e) {
    confirmed = false;
    ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
  }

  @FXML
  private void onConfirmClick(ActionEvent e) {
    confirmed = true;
    ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
  }

  /**
   * @param showOverwriteMatchings if false, the existing matchings are always overwritten and the checkbox is hidden
   */
  public void setData(String title, String description, String hint, String confirmText, boolean showOverwriteMatchings) {
    titleLabel.setText(title);
    descriptionLabel.setText(description);
    hintLabel.setText(hint);
    hintLabel.setVisible(hint != null);
    hintLabel.setManaged(hint != null);
    confirmBtn.setText(confirmText);
    overwriteMatchingsRow.setVisible(showOverwriteMatchings);
    overwriteMatchingsRow.setManaged(showOverwriteMatchings);
    overwriteMatchingsCheckbox.setSelected(!showOverwriteMatchings);
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public boolean isOverwriteMatchings() {
    return overwriteMatchingsCheckbox.isSelected();
  }

  public boolean isOverwriteMetadata() {
    return overwriteMetadataCheckbox.isSelected();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  @Override
  public void onDialogCancel() {
  }
}
