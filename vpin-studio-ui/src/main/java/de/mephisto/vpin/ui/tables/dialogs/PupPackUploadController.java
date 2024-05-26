package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PupPackUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackUploadController.class);

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private Label tableLabel;

  @FXML
  private Label romLabel;

  private File selection;

  private boolean result = false;

  private UploaderAnalysis analysis;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null && selection.exists()) {
      result = false;
      stage.close();

      Platform.runLater(() -> {
        PupPackUploadProgressModel model = new PupPackUploadProgressModel(null, "PUP Pack Upload", selection);
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select PUP Pack");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("PUP Pack", "*.zip", "*.rar"));

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


    analysis = UploadAnalysisDispatcher.analyzeArchive(this.selection);
    refreshMatchingGame(analysis);
    String validation = analysis.validateAssetType(AssetType.PUP_PACK);

    if (validation != null) {
      result = false;
      WidgetFactory.showAlert(stage, "Invalid Pup Pack", validation);
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

  public void setFile(File file, UploaderAnalysis uploaderAnalysis, Stage stage) {
    this.selection = file;
    if (selection != null) {
      refreshMatchingGame(uploaderAnalysis);
      this.fileNameField.setText(this.selection.getAbsolutePath());
      this.fileNameField.setDisable(false);
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
      this.uploadBtn.setDisable(false);
    }
  }

  private void refreshMatchingGame(UploaderAnalysis uploaderAnalysis) {
    tableLabel.setText("-");
    romLabel.setText("-");
    this.uploadBtn.setDisable(true);
    if (uploaderAnalysis == null) {
      return;
    }

    String rom = uploaderAnalysis.getRomFromPupPack();
    if (rom == null) {
      return;
    }
    romLabel.setText(rom);
    this.uploadBtn.setDisable(false);

    List<GameRepresentation> gamesCached = Studio.client.getGameService().getGamesCached();
    for (GameRepresentation gameRepresentation : gamesCached) {
      String gameRom = gameRepresentation.getRom();
      if (!StringUtils.isEmpty(gameRom) && gameRom.equalsIgnoreCase(rom)) {
        tableLabel.setText(gameRepresentation.getGameDisplayName());
        break;
      }
    }
  }
}
