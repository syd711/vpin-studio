package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DirectB2SUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SUploadController.class);

  private static boolean uploadTypeGeneratorSelectedLast;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Label titleLabel;

  private File selection;

  private boolean result = false;
  private GameRepresentation game;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    if (selection != null && selection.exists()) {
      Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
      result = true;
      try {
        uploadTypeGeneratorSelectedLast = false;
        DirectB2SUploadProgressModel model = new DirectB2SUploadProgressModel(this.game.getId(), "DirectB2S Upload", selection, "table");
        ProgressDialog.createProgressDialog(model);
      } catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Uploading directb2s failed.", "Please check the log file for details.", "Error: " + e.getMessage());
      } finally {
        stage.close();
      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select DirectB2S File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Direct B2S", "*.directb2s"));

    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null) {
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

  public void setData(GameRepresentation game, File file) {
    this.game = game;
    this.titleLabel.setText("Select directb2s file for \"" + game.getGameDisplayName() + "\":");

    if (file != null) {
      this.selection = file;
      this.fileNameField.setText(this.selection.getAbsolutePath());
    }
  }
}
