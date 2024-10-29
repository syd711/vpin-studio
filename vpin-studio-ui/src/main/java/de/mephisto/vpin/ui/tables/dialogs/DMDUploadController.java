package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DMDUploadController extends BaseUploadController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDUploadController.class);

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;


  private GameRepresentation game;

  public DMDUploadController() {
    super("DMD Bundle", false, true, "zip", "rar", "7z");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new DMDUploadProgressModel("DMD Bundle Upload", getSelection(), getSelectedEmulatorId(), game, finalizer);
  }

  private void refreshSelection(Stage stage) {
    File selection = getSelection();

    this.fileNameField.setText("Analyzing \"" + selection.getName() + "\", please wait...");
    this.fileNameField.setDisable(true);
    this.fileBtn.setDisable(true);
    this.cancelBtn.setDisable(true);


    String analyze = UploadAnalysisDispatcher.validateArchive(selection, AssetType.DMD_PACK);

    if (analyze != null) {
      WidgetFactory.showAlert(stage, analyze);
      this.fileNameField.setText("");
      this.fileBtn.setDisable(false);
      this.fileNameField.setDisable(false);
      this.cancelBtn.setDisable(false);
    }
    else {
      this.fileNameField.setText(selection.getAbsolutePath());
      this.fileNameField.setDisable(false);
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
      this.uploadBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
    }
  }

  public void setData(Stage stage, File file, GameRepresentation game, UploaderAnalysis<?> analysis, Runnable finalizer) {
    super.setFile(stage, file, finalizer);
    this.game = game;
    if (file != null) {
      if (analysis != null) {
        this.fileNameField.setText(file.getAbsolutePath());
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
