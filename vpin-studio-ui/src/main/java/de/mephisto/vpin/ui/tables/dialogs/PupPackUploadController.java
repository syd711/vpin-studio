package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FileSelectorDropEventHandler;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class PupPackUploadController implements Initializable, DialogController {
  //private final static Logger LOG = LoggerFactory.getLogger(PupPackUploadController.class);

  @FXML
  private Node root;

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
  private Stage stage;

  private boolean result = false;

  private UploaderAnalysis<?> analysis;

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
      refreshSelection();
    }
  }

  private void refreshSelection() {
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

    root.setOnDragOver(new FileSelectorDragEventHandler(root, PackageUtil.ARCHIVE_SUFFIXES));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      selection = file;
      refreshSelection();
    }));
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }

  public void setFile(File file, UploaderAnalysis<?> uploaderAnalysis, Stage stage) {
    this.selection = file;
    this.stage = stage;
    if (selection != null) {
      refreshMatchingGame(uploaderAnalysis);
      this.fileNameField.setText(this.selection.getAbsolutePath());
      this.fileNameField.setDisable(false);
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
      this.uploadBtn.setDisable(false);
    }
  }

  private void refreshMatchingGame(UploaderAnalysis<?> uploaderAnalysis) {
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

    GameRepresentation gameRepresentation = Studio.client.getGameService().getFirstGameByRom(rom);
    if (gameRepresentation != null) {
      tableLabel.setText(gameRepresentation.getGameDisplayName());
    }
  }
}
