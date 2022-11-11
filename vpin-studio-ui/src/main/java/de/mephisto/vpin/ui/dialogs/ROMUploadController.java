package de.mephisto.vpin.ui.dialogs;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class ROMUploadController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ROMUploadController.class);

  private static File lastFolderSelection;
  private static boolean uploadTypeGeneratorSelectedLast;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  private File selection;

  private boolean result = false;
  private GameRepresentation game;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(){
    if (selection != null && selection.exists()) {
      boolean result = false;
      try {
//        if (uploadTypeGenerator.isSelected()) {
//          uploadTypeGeneratorSelectedLast = true;
//          result = client.uploadDirectB2SFile(selection, "generator", this.game.getId());
//        }
//        else {
//          uploadTypeGeneratorSelectedLast = false;
//          result = client.uploadDirectB2SFile(selection, null, -1);
//        }
      } catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert("Uploading directb2s failed, check log file for details:\n\n" + e.getMessage());
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

  public boolean uploadFinished() {
    return result;
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
  }
}
