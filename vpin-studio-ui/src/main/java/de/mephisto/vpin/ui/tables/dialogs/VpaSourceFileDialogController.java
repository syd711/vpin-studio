package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.VpaSourceType;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.representations.VpaSourceRepresentation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.stage;

public class VpaSourceFileDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(VpaSourceFileDialogController.class);
  private static File lastFolderSelection;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField folderField;

  private VpaSourceRepresentation source;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.source = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    this.source.setName(nameField.getText());
    this.source.setType(VpaSourceType.File.name());
    this.source.setLocation(folderField.getText());

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onFileSelect() {
    DirectoryChooser chooser = new DirectoryChooser();
    if (VpaSourceFileDialogController.lastFolderSelection != null) {
      chooser.setInitialDirectory(VpaSourceFileDialogController.lastFolderSelection);
    }
    chooser.setTitle("Select Source Folder");
    File targetFolder = chooser.showDialog(stage);
    if (targetFolder != null) {
      folderField.setText(targetFolder.getAbsolutePath());
      VpaSourceFileDialogController.lastFolderSelection = targetFolder;

      if(StringUtils.isEmpty(nameField.getText())) {
        nameField.setText(StringUtils.capitalize(targetFolder.getName()));
      }
    }
    validateInput();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    source = new VpaSourceRepresentation();

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

  public VpaSourceRepresentation getVpaSource() {
    return source;
  }

  public void setSource(VpaSourceRepresentation source) {
    if (source != null) {
      this.source = source;
      nameField.setText(source.getName());
      folderField.setText(source.getLocation());
    }
    validateInput();
  }
}
