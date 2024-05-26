package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MediaUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MediaUploadController.class);

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

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
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null && selection.exists()) {
      result = true;
      stage.close();

      Platform.runLater(() -> {
        MediaPackUploadProgressModel model = new MediaPackUploadProgressModel(this.game.getId(), "Media Pack Upload", selection);
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Media Pack");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Media Pack", "*.zip"));
    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null) {
      refreshSelection(stage);
    }
  }

  private void refreshSelection(Stage stage) {
    this.fileNameField.setText("Analyzing \"" + selection.getName() + "\", please wait...");
    this.fileNameField.setDisable(true);
    this.fileBtn.setDisable(true);
    this.cancelBtn.setDisable(true);


    String analyze = UploadAnalysisDispatcher.validateArchive(selection, AssetType.POPPER_MEDIA);

    if (analyze != null) {
      result = false;
      WidgetFactory.showAlert(stage, analyze);
      this.fileNameField.setText("");
      this.fileBtn.setDisable(false);
      this.fileNameField.setDisable(false);
      this.cancelBtn.setDisable(false);
    }
    else {
      this.fileNameField.setText(this.selection.getAbsolutePath());
      this.fileNameField.setDisable(false);
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
      this.uploadBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;
    this.uploadBtn.setDisable(true);
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }

  public void setData(GameRepresentation game, UploaderAnalysis analysis, File file, Stage stage) {
    this.game = game;
    this.selection = file;
    if (selection != null) {
      if (analysis != null) {
        this.fileNameField.setText(this.selection.getAbsolutePath());
        this.fileNameField.setDisable(false);
        this.fileBtn.setDisable(false);
        this.cancelBtn.setDisable(false);
        this.uploadBtn.setDisable(false);
        this.cancelBtn.setDisable(false);
      }
      else {
        refreshSelection(stage);
      }
    }
  }
}
