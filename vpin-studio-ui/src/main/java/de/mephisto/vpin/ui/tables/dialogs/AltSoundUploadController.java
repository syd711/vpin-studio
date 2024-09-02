package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AltSoundUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundUploadController.class);

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

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private Stage stage;
  private AltSoundUploadProgressModel model;
  private boolean result = false;

  private File selection;
  private UploaderAnalysis uploaderAnalysis;
  private GameEmulatorRepresentation emulatorRepresentation;
  private int gameId;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    result = true;

    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null && selection.exists()) {
      stage.close();
      model = new AltSoundUploadProgressModel(gameId, "ALT Sound Upload", selection, this.emulatorCombo.getValue().getId(), uploaderAnalysis.getRomFromAltSoundPack());

      Platform.runLater(() -> {
        ProgressResultModel progressResult = ProgressDialog.createProgressDialog(model);

        // Cancelling the upload progress doesn't actually cancel the HTTP request, however we still do not want to continue to the next step.
        if (progressResult.isCancelled()) {
          result = false;
        }
      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select ALT Sound");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("ALT Sound Package", "*.zip", "*.rar"));

    this.selection = fileChooser.showOpenDialog(stage);
    this.uploadBtn.setDisable(selection == null);
    if (this.selection != null) {
      refreshSelection();
    }
    else {
      this.fileNameField.setText("");
    }
  }

  private void refreshSelection() {
    this.uploadBtn.setDisable(selection == null);

    this.fileNameField.setText("Analyzing \"" + selection.getName() + "\"...");
    this.fileNameField.setDisable(true);
    this.fileBtn.setDisable(true);
    this.cancelBtn.setDisable(true);


    Platform.runLater(() -> {
      uploaderAnalysis = UploadAnalysisDispatcher.analyzeArchive(this.selection);
      refreshMatchingGame(uploaderAnalysis);
      String validation = uploaderAnalysis.validateAssetType(AssetType.ALT_SOUND);

      if (validation != null) {
        WidgetFactory.showAlert(stage, "Invalid ALT Sound Pack", validation);
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
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;
    
    this.uploadBtn.setDisable(true);

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getFrontendService().getVpxGameEmulators();
    emulatorRepresentation = gameEmulators.get(0);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulatorRepresentation);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulatorRepresentation = t1;
    });

    root.setOnDragOver(new FileSelectorDragEventHandler(root, PackageUtil.ARCHIVE_SUFFIXES));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      selection = file;
      refreshSelection();
    }));
  }

  public void setData(Stage stage, File file, UploaderAnalysis uploaderAnalysis, int gameId) {
    this.stage = stage;
    this.selection = file;
    this.uploaderAnalysis = uploaderAnalysis;
    this.gameId = gameId;

    if (this.selection != null) {
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

    String rom = uploaderAnalysis.getRomFromAltSoundPack();
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

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }
}
