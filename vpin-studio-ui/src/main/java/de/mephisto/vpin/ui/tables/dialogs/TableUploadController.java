package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
  private CheckBox autofillCheckbox;

  @FXML
  private CheckBox subfolderCheckbox;

  @FXML
  private CheckBox backupTableOnOverwriteCheckbox;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private TextField subfolderText;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private VBox uploadImportBox;
  @FXML
  private VBox uploadReplaceBox;
  @FXML
  private VBox uploadCloneBox;

  @FXML
  private Label noAssetsLabel;

  @FXML
  private CheckBox assetPupPackCheckbox;
  @FXML
  private CheckBox assetAltSoundCheckbox;
  @FXML
  private CheckBox assetMediaCheckbox;
  @FXML
  private CheckBox assetBackglassCheckbox;
  @FXML
  private CheckBox assetRomCheckbox;
  @FXML
  private CheckBox assetDmdCheckbox;

  @FXML
  private VBox assetsBox;

  private File selection;
  private Optional<TableUploadDescriptor> result = Optional.empty();

  private GameRepresentation game;
  private GameEmulatorRepresentation emulatorRepresentation;

  private TableUploadDescriptor tableUploadDescriptor = new TableUploadDescriptor();
  private TableOverviewController tableOverviewController;
  private UploaderAnalysis uploaderAnalysis;

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
        String subFolder= this.subfolderText.getText();
        boolean useSubFolder = this.subfolderCheckbox.isSelected();
        boolean autoFill = this.autofillCheckbox.isSelected();

        Platform.runLater(() -> {
          Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
          stage.close();
        });

        TableUploadProgressModel model = new TableUploadProgressModel("VPX Upload", selection, game.getId(), tableUploadDescriptor.getUploadType(), emulatorRepresentation.getId());
        ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(model);

        List<Object> results = progressDialog.getResults();
        if (!results.isEmpty()) {
          TableUploadDescriptor uploadDescriptor = (TableUploadDescriptor) results.get(0);

          uploadDescriptor.setSubfolderName(subFolder);
          uploadDescriptor.setFolderBasedImport(useSubFolder);
          uploadDescriptor.setAutoFill(autoFill);

          TableUploadProcessingProgressModel progressModel = new TableUploadProcessingProgressModel("Importing Table and Assets", uploadDescriptor);
          ProgressResultModel progressDialogResult = ProgressDialog.createProgressDialog(progressModel);
          if (!progressDialogResult.getResults().isEmpty()) {
            uploadDescriptor = (TableUploadDescriptor) progressDialogResult.getResults().get(0);
            result = Optional.of(uploadDescriptor);
            tableOverviewController.refreshUploadResult(result);
          }
        }
      }
      catch (Exception e) {
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
    setSelection(true);
  }

  private void setSelection(boolean rescan) {
    uploadBtn.setDisable(true);
    if (this.selection != null) {
      String suffix = FilenameUtils.getExtension(this.selection.getName());

      if (suffix.equalsIgnoreCase("zip")) {
        this.fileBtn.setDisable(true);
        this.cancelBtn.setDisable(true);

        Platform.runLater(() -> {
          if(rescan) {
            uploaderAnalysis = UploadAnalysisDispatcher.analyzeArchive(selection, game);
          }
          String analyze = uploaderAnalysis.validateAssetType(AssetType.VPX);

          this.fileNameField.setText(this.selection.getAbsolutePath());
          this.subfolderText.setText(FilenameUtils.getBaseName(uploaderAnalysis.getVpxFileName()));
          this.fileNameField.setDisable(false);
          this.fileBtn.setDisable(false);
          this.cancelBtn.setDisable(false);

          if (analyze != null) {
            WidgetFactory.showAlert(Studio.stage, analyze);
            this.fileNameField.setText("");
            this.subfolderText.setText("");
          }
          else {
            updateAnalysis();
          }
          this.uploadBtn.setDisable(analyze != null);
        });
      }
      else {
        this.uploadBtn.setDisable(false);
        this.fileNameField.setText(this.selection.getAbsolutePath());
        this.subfolderText.setText(FilenameUtils.getBaseName(this.selection.getName()));
      }
    }
    else {
      this.fileNameField.setText("");
    }
  }

  private void updateAnalysis() {
    assetPupPackCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.PUP_PACK) == null);
    assetPupPackCheckbox.setVisible(assetPupPackCheckbox.isSelected());

    assetAltSoundCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.ALT_SOUND) == null);
    assetAltSoundCheckbox.setVisible(assetAltSoundCheckbox.isSelected());

    assetMediaCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.POPPER_MEDIA) == null);
    assetMediaCheckbox.setVisible(assetMediaCheckbox.isSelected());

    assetBackglassCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.DIRECTB2S) == null);
    assetBackglassCheckbox.setVisible(assetBackglassCheckbox.isSelected());

    assetDmdCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.DMD_PACK) == null);
    assetDmdCheckbox.setVisible(assetDmdCheckbox.isSelected());

    assetRomCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.ROM) == null);
    assetRomCheckbox.setVisible(assetRomCheckbox.isSelected());

    assetsBox.setVisible(assetBackglassCheckbox.isSelected() || assetAltSoundCheckbox.isSelected() || assetMediaCheckbox.isSelected() || assetBackglassCheckbox.isSelected() || assetRomCheckbox.isSelected());
    noAssetsLabel.setVisible(!assetsBox.isVisible());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

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

    uploadAndCloneRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          uploadCloneBox.getStyleClass().add("selection-panel-selected");
          tableUploadDescriptor.setUploadType(TableUploadType.uploadAndClone);
        }
        else {
          uploadCloneBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    uploadAndReplaceRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          uploadReplaceBox.getStyleClass().add("selection-panel-selected");
          tableUploadDescriptor.setUploadType(TableUploadType.uploadAndReplace);
        }
        else {
          uploadReplaceBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    uploadAndImportRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          uploadImportBox.getStyleClass().add("selection-panel-selected");
          tableUploadDescriptor.setUploadType(TableUploadType.uploadAndImport);
        }
        else {
          uploadImportBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    autofillCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        tableUploadDescriptor.setAutoFill(newValue);
      }
    });

    subfolderCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        subfolderText.setDisable(!newValue);
      }
    });

    uploadImportBox.getStyleClass().add("selection-panel-selected");

    noAssetsLabel.setVisible(true);
    noAssetsLabel.managedProperty().bindBidirectional(noAssetsLabel.visibleProperty());
    assetsBox.setVisible(false);
    assetsBox.managedProperty().bindBidirectional(assetsBox.visibleProperty());

    assetPupPackCheckbox.managedProperty().bindBidirectional(assetPupPackCheckbox.visibleProperty());
    assetMediaCheckbox.managedProperty().bindBidirectional(assetMediaCheckbox.visibleProperty());
    assetAltSoundCheckbox.managedProperty().bindBidirectional(assetAltSoundCheckbox.visibleProperty());
    assetBackglassCheckbox.managedProperty().bindBidirectional(assetBackglassCheckbox.visibleProperty());
    assetRomCheckbox.managedProperty().bindBidirectional(assetRomCheckbox.visibleProperty());
    assetDmdCheckbox.managedProperty().bindBidirectional(assetDmdCheckbox.visibleProperty());

    assetPupPackCheckbox.setVisible(false);
    assetMediaCheckbox.setVisible(false);
    assetAltSoundCheckbox.setVisible(false);
    assetBackglassCheckbox.setVisible(false);
    assetRomCheckbox.setVisible(false);
    assetDmdCheckbox.setVisible(false);
  }

  public void setGame(@NonNull TableOverviewController tableOverviewController, @Nullable GameRepresentation game, TableUploadType uploadType, UploaderAnalysis analysis) {
    this.tableOverviewController = tableOverviewController;
    this.uploaderAnalysis = analysis;

    tableUploadDescriptor.setUploadType(uploadType);
    tableUploadDescriptor.setEmulatorId(this.emulatorCombo.getValue().getId());
    tableUploadDescriptor.setAutoFill(this.autofillCheckbox.isSelected());
    this.tableUploadDescriptor.setUploadType(uploadType);

    this.game = game;

    if (game != null) {
      tableUploadDescriptor.setGameId(game.getId());
      this.uploadAndReplaceRadio.setText("Upload and Replace \"" + game.getGameDisplayName() + "\"");
      this.uploadAndCloneRadio.setText("Upload and Clone \"" + game.getGameDisplayName() + "\"");

      GameEmulatorRepresentation gameEmulator = Studio.client.getPinUPPopperService().getGameEmulator(game.getEmulatorId());
      emulatorCombo.setValue(gameEmulator);
    }
    else {
      this.uploadAndReplaceRadio.setDisable(true);
      this.keepDisplayNamesCheckbox.setDisable(true);
      this.backupTableOnOverwriteCheckbox.setDisable(true);
      this.keepNamesCheckbox.setDisable(true);
      this.uploadAndCloneRadio.setDisable(true);
    }

    switch (uploadType) {
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

    if(this.uploaderAnalysis != null) {
      this.selection = uploaderAnalysis.getFile();
      setSelection(false);
    }
  }

  @Override
  public void onDialogCancel() {

  }

  public Optional<TableUploadDescriptor> uploadFinished() {
    return result;
  }
}
