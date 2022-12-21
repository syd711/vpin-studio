package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.DialogController;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.stage;

public class ROMUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(ROMUploadController.class);

  private static File lastFolderSelection;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  private File selection;

  private boolean result = false;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    if (selection != null && selection.exists()) {
      result = true;
      try {
        Studio.client.uploadRom(selection);
      } catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert("Uploading ROM failed, check log file for details:\n\n" + e.getMessage());
      } finally {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
      }
    }
  }

  @FXML
  private void onFileSelect() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select ROM File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("ROM", "*.zip"));

    if (ROMUploadController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(ROMUploadController.lastFolderSelection);
    }

    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null) {
      ROMUploadController.lastFolderSelection = this.selection.getParentFile();
      this.fileNameField.setText(this.selection.getAbsolutePath());
    }
    else {
      this.fileNameField.setText("");
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;

    this.uploadBtn.setDisable(true);
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }
}
