package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.VpxArchiveAnalyzer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.StudioFileChooser;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TableUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableUploadController.class);

  @FXML
  private TextField fileNameField;

  @FXML
  private RadioButton uploadAndImportRadio;

  @FXML
  private RadioButton uploadAndReplaceRadio;

  @FXML
  private RadioButton uploadAndCloneRadio;

  @FXML
  private CheckBox keepNamesCheckbox;

  @FXML
  private CheckBox keepDisplayNamesCheckbox;

  @FXML
  private CheckBox backupTableOnOverwriteCheckbox;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private Button cancelBtn;

  private File selection;
  private Optional<TableUploadResult> result = Optional.empty();

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
      try {
        TableUploadDescriptor descriptor = TableUploadDescriptor.uploadAndImport;
        if (uploadAndImportRadio.isSelected()) {
          descriptor = TableUploadDescriptor.uploadAndImport;
        }
        else if (uploadAndReplaceRadio.isSelected()) {
          descriptor = TableUploadDescriptor.uploadAndReplace;
        }
        else if (uploadAndCloneRadio.isSelected()) {
          descriptor = TableUploadDescriptor.uploadAndClone;
        }

        Platform.runLater(() -> {
          Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
          stage.close();
        });

        TableUploadProgressModel model = new TableUploadProgressModel("VPX Upload", selection, gameId, descriptor, emulatorRepresentation.getId());
        ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(model);

        List<Object> results = progressDialog.getResults();
        if (!results.isEmpty()) {
          result = Optional.of((TableUploadResult) results.get(0));
        }
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

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select VPX File");
    fileChooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("VPX File", "*.vpx", "*.zip", "*.rar"));

    this.selection = fileChooser.showOpenDialog(stage);
    uploadBtn.setDisable(true);
    if (this.selection != null) {
      String suffix = FilenameUtils.getExtension(this.selection.getName());

      if (suffix.equalsIgnoreCase("zip")) {
        this.fileBtn.setDisable(true);
        this.cancelBtn.setDisable(true);

        Platform.runLater(() -> {
          String analyze = VpxArchiveAnalyzer.analyze(selection);
          this.fileNameField.setText(this.selection.getAbsolutePath());
          this.fileNameField.setDisable(false);
          this.fileBtn.setDisable(false);
          this.cancelBtn.setDisable(false);

          if (analyze != null) {
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
    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    keepDisplayNamesCheckbox.setDisable(true);
    keepNamesCheckbox.setDisable(true);
    backupTableOnOverwriteCheckbox.setDisable(true);

    this.selection = null;
    this.uploadBtn.setDisable(true);
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getPinUPPopperService().getVpxGameEmulators();
    emulatorRepresentation = gameEmulators.get(0);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulatorRepresentation);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulatorRepresentation = t1;
      if (this.game != null) {
        boolean sameEmulator = t1.getId() == game.getEmulatorId();
        uploadAndImportRadio.setSelected(true);
        uploadAndReplaceRadio.setDisable(!sameEmulator);
      }
    });

    ToggleGroup toggleGroup = new ToggleGroup();
    uploadAndImportRadio.setToggleGroup(toggleGroup);
    uploadAndCloneRadio.setToggleGroup(toggleGroup);
    uploadAndReplaceRadio.setToggleGroup(toggleGroup);

    toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      @Override
      public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        keepDisplayNamesCheckbox.setDisable(!newValue.equals(uploadAndReplaceRadio));
        keepNamesCheckbox.setDisable(!newValue.equals(uploadAndReplaceRadio));
        backupTableOnOverwriteCheckbox.setDisable(!newValue.equals(uploadAndReplaceRadio));
      }
    });


    keepNamesCheckbox.setSelected(serverSettings.isVpxKeepFileNames());
    keepNamesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setVpxKeepFileNames(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    keepDisplayNamesCheckbox.setSelected(serverSettings.isVpxKeepDisplayNames());
    keepDisplayNamesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setVpxKeepDisplayNames(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    backupTableOnOverwriteCheckbox.setSelected(serverSettings.isBackupTableOnOverwrite());
    backupTableOnOverwriteCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setBackupTableOnOverwrite(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });
  }

  public void setGame(GameRepresentation game, TableUploadDescriptor descriptor) {
    this.game = game;

    if (game != null) {
      this.gameId = game.getId();
      this.uploadAndReplaceRadio.setText("Upload and Replace \"" + game.getGameDisplayName() + "\"");
      this.uploadAndCloneRadio.setText("Upload and Clone \"" + game.getGameDisplayName() + "\"");
      this.uploadAndCloneRadio.setDisable(game.getGameFileName().contains("\\"));

      GameEmulatorRepresentation gameEmulator = Studio.client.getPinUPPopperService().getGameEmulator(game.getEmulatorId());
      emulatorCombo.setValue(gameEmulator);
    }
    else {
      this.uploadAndReplaceRadio.setDisable(true);
      this.keepDisplayNamesCheckbox.setDisable(true);
      this.backupTableOnOverwriteCheckbox.setDisable(true);
      this.keepNamesCheckbox.setDisable(true);
      this.uploadAndCloneRadio.setDisable(true);
      this.gameId = -1;
    }

    switch (descriptor) {
      case uploadAndClone: {
        this.uploadAndCloneRadio.setSelected(true);
        break;
      }
      case uploadAndImport: {
        this.uploadAndImportRadio.setSelected(true);
        break;
      }
      case uploadAndReplace: {
        this.uploadAndReplaceRadio.setSelected(true);
        break;
      }
    }
  }

  @Override
  public void onDialogCancel() {

  }

  public Optional<TableUploadResult> uploadFinished() {
    return result;
  }
}
