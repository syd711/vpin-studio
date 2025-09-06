package de.mephisto.vpin.ui.backups.dialogs;

import de.mephisto.vpin.commons.BackupSourceType;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.backups.BackupSourceRepresentation;
import de.mephisto.vpin.restclient.system.FolderRepresentation;
import de.mephisto.vpin.ui.util.FolderChooserDialog;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class BackupSourceFolderDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(BackupSourceFolderDialogController.class);

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField folderField;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private Button folderBtn;

  private BackupSourceRepresentation source;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.source = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    this.source.setType(BackupSourceType.Folder.name());
    this.source.setName(nameField.getText().trim());
    this.source.setLocation(folderField.getText().trim());
    this.source.setEnabled(enabledCheckbox.isSelected());

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onFileSelect() {
    FolderRepresentation folderRepresentation = FolderChooserDialog.open(null);
    if (folderRepresentation != null) {
      folderField.setText(folderRepresentation.getPath());
      if (StringUtils.isEmpty(nameField.getText())) {
        nameField.setText(folderRepresentation.getName());
      }

      validateInput();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    folderBtn.managedProperty().bindBidirectional(folderBtn.visibleProperty());

    source = new BackupSourceRepresentation();
//    folderBtn.setVisible(client.getSystemService().isLocal());

    nameField.textProperty().addListener((observableValue, s, t1) -> {
      source.setName(t1);
      validateInput();
    });

    folderField.textProperty().addListener((observableValue, s, t1) -> {
      source.setLocation(t1);
      validateInput();
    });
    this.validateInput();

    this.nameField.requestFocus();
  }

  private void validateInput() {
    String name = nameField.getText();
    String folder = folderField.getText();

    saveBtn.setDisable(StringUtils.isEmpty(name) || StringUtils.isEmpty(folder));
  }

  @Override
  public void onDialogCancel() {
    this.source = null;
  }

  public BackupSourceRepresentation getArchiveSource() {
    return source;
  }

  public void setSource(BackupSourceRepresentation source) {
    if (source != null) {
      this.source = source;
      nameField.setText(source.getName());
      folderField.setText(source.getLocation());
      enabledCheckbox.setSelected(source.isEnabled());
    }
    validateInput();
  }
}
