package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetSourceType;
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

public class TableAssetSourceFolderDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetSourceFolderDialogController.class);
  private static File lastFolderSelection;

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

  private TableAssetSource source;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.source = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    this.source.setType(TableAssetSourceType.FileSystem);
    this.source.setName(nameField.getText().trim());
    this.source.setLocation(folderField.getText().trim());
    this.source.setEnabled(enabledCheckbox.isSelected());

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onFileSelect() {
    DirectoryChooser chooser = new DirectoryChooser();
    if (TableAssetSourceFolderDialogController.lastFolderSelection != null) {
      chooser.setInitialDirectory(TableAssetSourceFolderDialogController.lastFolderSelection);
    }
    chooser.setTitle("Select Folder");
    File targetFolder = chooser.showDialog(stage);
    if (targetFolder != null) {
      folderField.setText(targetFolder.getAbsolutePath());
      TableAssetSourceFolderDialogController.lastFolderSelection = targetFolder;

      if(StringUtils.isEmpty(nameField.getText())) {
        nameField.setText(targetFolder.getAbsolutePath());
      }
    }
    validateInput();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    source = new TableAssetSource();
    folderBtn.setVisible(client.getSystemService().isLocal());

    nameField.textProperty().addListener((observableValue, s, t1) -> {
      source.setName(t1);
      validateInput();
    });
    this.validateInput();

    this.nameField.requestFocus();
  }

  private void validateInput() {
    String name = nameField.getText();
    String initials = folderField.getText();

    saveBtn.setDisable(StringUtils.isEmpty(name) || StringUtils.isEmpty(initials));
  }

  @Override
  public void onDialogCancel() {
    this.source = null;
  }

  public TableAssetSource getTableAssetSource() {
    return source;
  }

  public void setSource(TableAssetSource source) {
    if (source != null) {
      this.source = source;
      nameField.setText(source.getName());
      folderField.setText(source.getLocation());
      enabledCheckbox.setSelected(source.isEnabled());
    }
    validateInput();
  }
}
