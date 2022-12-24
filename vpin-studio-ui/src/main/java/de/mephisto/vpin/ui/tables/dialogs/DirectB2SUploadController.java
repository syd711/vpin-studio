package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.fx.DialogController;
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

public class DirectB2SUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SUploadController.class);

  private static File lastFolderSelection;
  private static boolean uploadTypeGeneratorSelectedLast;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private RadioButton uploadTypeGenerator;

  @FXML
  private RadioButton uploadTypeTable;

  private File selection;

  private boolean result = false;
  private GameRepresentation game;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event){
    if (selection != null && selection.exists()) {
      result = true;
      try {
        if (uploadTypeGenerator.isSelected()) {
          uploadTypeGeneratorSelectedLast = true;
          client.uploadDirectB2SFile(selection, "generator", this.game.getId());
        }
        else {
          uploadTypeGeneratorSelectedLast = false;
          client.uploadDirectB2SFile(selection, "table", this.game.getId());
        }
      } catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert("Uploading directb2s failed, check log file for details:\n\n" + e.getMessage());
      }
      finally {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
      }
    }
  }

  @FXML
  private void onFileSelect() throws IOException {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select DirectB2S File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Direct B2S", "*.directb2s"));

    if (DirectB2SUploadController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(DirectB2SUploadController.lastFolderSelection);
    }

    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null) {
      DirectB2SUploadController.lastFolderSelection = this.selection.getParentFile();
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

    this.uploadTypeGenerator.setSelected(DirectB2SUploadController.uploadTypeGeneratorSelectedLast);
    this.uploadTypeTable.setSelected(!DirectB2SUploadController.uploadTypeGeneratorSelectedLast);
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
  }
}
