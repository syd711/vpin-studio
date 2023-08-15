package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class ROMUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(ROMUploadController.class);

  private static File lastFolderSelection;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  private List<File> selection;

  private boolean result = false;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    if (selection != null && !selection.isEmpty()) {
      result = true;
      try {
        Platform.runLater(()-> {
          Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
          stage.close();
        });
        RomUploadProgressModel model = new RomUploadProgressModel("ROM Upload", selection);
        Dialogs.createProgressDialog(model);
      } catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(stage, "Uploading ROM failed", "Please check the log file for details", "Error: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select ROM File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("ROM", "*.zip"));

    if (ROMUploadController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(ROMUploadController.lastFolderSelection);
    }

    this.selection = fileChooser.showOpenMultipleDialog(stage);
    if (this.selection != null && !this.selection.isEmpty()) {
      ROMUploadController.lastFolderSelection = this.selection.get(0).getParentFile();
      List<String> collect = this.selection.stream().map(f -> f.getName()).collect(Collectors.toList());
      this.fileNameField.setText(String.join(", ", collect));
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
