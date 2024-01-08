package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.VpxArchiveAnalyzer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.stage;

public class TableUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableUploadController.class);

  private static File lastFolderSelection;

  @FXML
  private TextField fileNameField;

  @FXML
  private RadioButton uploadRadio;

  @FXML
  private RadioButton uploadAndImportRadio;

  @FXML
  private RadioButton uploadAndReplaceRadio;

  @FXML
  private RadioButton uploadAndCloneRadio;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private Button cancelBtn;

  private File selection;
  private boolean result = false;

  private GameRepresentation game;
  private int gameId;
  private GameEmulatorRepresentation emulatorRepresentation;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    if (selection != null) {
      uploadBtn.setDisable(true);
      result = true;
      try {
        TableUploadDescriptor descriptor = TableUploadDescriptor.upload;
        if(uploadAndImportRadio.isSelected()) {
          descriptor = TableUploadDescriptor.uploadAndImport;
        }
        else if (uploadAndReplaceRadio.isSelected()) {
          descriptor = TableUploadDescriptor.uploadAndReplace;
        }
        else if(uploadAndCloneRadio.isSelected()) {
          descriptor = TableUploadDescriptor.uploadAndClone;
        }

        Platform.runLater(()-> {
          Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
          stage.close();
        });

        TableUploadProgressModel model = new TableUploadProgressModel("VPX Upload", selection, gameId, descriptor, emulatorRepresentation.getId());
        ProgressDialog.createProgressDialog(model);
      } catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        stage.close();
        WidgetFactory.showAlert(stage, "Uploading VPX file failed.", "Please check the log file for details.", "Error: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select VPX File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("VPX File", "*.vpx", "*.zip"));

    if (TableUploadController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(TableUploadController.lastFolderSelection);
    }

    this.selection = fileChooser.showOpenDialog(stage);
    uploadBtn.setDisable(true);
    if (this.selection != null) {
      TableUploadController.lastFolderSelection = this.selection.getParentFile();

      String suffix = FilenameUtils.getExtension(this.selection.getName());
      if(suffix.equalsIgnoreCase("zip")) {

        this.fileBtn.setDisable(true);
        this.cancelBtn.setDisable(true);

        Platform.runLater(() -> {
          String analyze = VpxArchiveAnalyzer.analyze(selection);
          this.fileNameField.setText(this.selection.getAbsolutePath());
          this.fileNameField.setDisable(false);
          this.fileBtn.setDisable(false);
          this.cancelBtn.setDisable(false);

          if (analyze != null) {
            result = false;
            WidgetFactory.showAlert(Studio.stage, analyze);
            this.fileNameField.setText("");
          }
          this.uploadBtn.setDisable(analyze != null);
        });
      }
      else {
        this.uploadBtn.setDisable(false);
        this.fileNameField.setText(this.selection.getAbsolutePath());
      }
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

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getPinUPPopperService().getGameEmulators();
    emulatorRepresentation = gameEmulators.get(0);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulatorRepresentation);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulatorRepresentation = t1;
    });

    ToggleGroup toggleGroup = new ToggleGroup();
    uploadRadio.setToggleGroup(toggleGroup);
    uploadAndImportRadio.setToggleGroup(toggleGroup);
    uploadAndCloneRadio.setToggleGroup(toggleGroup);
    uploadAndReplaceRadio.setToggleGroup(toggleGroup);
  }

  public void setGame(GameRepresentation game) {
    this.game = game;

    if(game != null) {
      this.gameId = game.getId();
      this.uploadAndReplaceRadio.setText("Upload and Replace \"" + game.getGameDisplayName() + "\"");
      this.uploadAndCloneRadio.setText("Upload and Clone \"" + game.getGameDisplayName() + "\"");

      GameEmulatorRepresentation gameEmulator = Studio.client.getPinUPPopperService().getGameEmulator(game.getEmulatorId());
      emulatorCombo.setValue(gameEmulator);
    }
    else {
      this.gameId = -1;
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
