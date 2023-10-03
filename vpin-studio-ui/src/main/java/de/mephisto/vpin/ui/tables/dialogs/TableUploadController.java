package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.tables.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
  private Label replaceTitle;

  @FXML
  private Label cloneTitle;

  @FXML
  private Button uploadBtn;

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
        Dialogs.createProgressDialog(model);
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
        new FileChooser.ExtensionFilter("VPX File", "*.vpx"));

    if (TableUploadController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(TableUploadController.lastFolderSelection);
    }

    this.selection = fileChooser.showOpenDialog(stage);
    uploadBtn.setDisable(this.selection == null);

    if (this.selection != null) {
      TableUploadController.lastFolderSelection = this.selection.getParentFile();
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
    uploadRadio.selectedProperty().addListener((observableValue, aBoolean, t1) -> emulatorCombo.setDisable(false));

    uploadAndImportRadio.setToggleGroup(toggleGroup);
    uploadAndImportRadio.selectedProperty().addListener((observableValue, aBoolean, t1) -> emulatorCombo.setDisable(false));

    uploadAndCloneRadio.setToggleGroup(toggleGroup);
    uploadAndCloneRadio.selectedProperty().addListener((observableValue, aBoolean, t1) -> emulatorCombo.setDisable(true));

    uploadAndReplaceRadio.setToggleGroup(toggleGroup);
    uploadAndReplaceRadio.selectedProperty().addListener((observableValue, aBoolean, t1) -> emulatorCombo.setDisable(true));
  }

  public void setGame(GameRepresentation game) {
    this.game = game;

    if(game != null) {
      this.gameId = game.getId();
      this.replaceTitle.setText("Upload and Replace \"" + game.getGameDisplayName() + "\"");
      this.cloneTitle.setText("Upload and Clone \"" + game.getGameDisplayName() + "\"");

      GameEmulatorRepresentation gameEmulator = Studio.client.getPinUPPopperService().getGameEmulator(game.getEmulatorId());
      emulatorCombo.setValue(gameEmulator);
    }
    else {
      this.gameId = -1;
      this.uploadAndReplaceRadio.setDisable(true);
      this.uploadAndCloneRadio.setDisable(true);
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
