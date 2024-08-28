package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.commons.utils.StringSimilarity;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.*;
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
import javafx.scene.Node;
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

public class TableUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableUploadController.class);

  @FXML
  private Node root;

  @FXML
  private TextField fileNameField;

  @FXML
  private RadioButton uploadAndImportRadio;

  @FXML
  private Label uploadAndImportDescription;

  @FXML
  private RadioButton uploadAndReplaceRadio;

  @FXML
  private Label uploadAndReplaceDescription;

  @FXML
  private RadioButton uploadAndCloneRadio;

  @FXML
  private Label uploadAndCloneDescription;

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
  private CheckBox assetAltColorCheckbox;
  @FXML
  private CheckBox assetMediaCheckbox;
  @FXML
  private CheckBox assetMusicCheckbox;
  @FXML
  private CheckBox assetBackglassCheckbox;
  @FXML
  private CheckBox assetRomCheckbox;
  @FXML
  private CheckBox assetPovCheckbox;
  @FXML
  private CheckBox assetIniCheckbox;
  @FXML
  private CheckBox assetResCheckbox;
  @FXML
  private CheckBox assetCfgCheckbox;
  @FXML
  private CheckBox assetNvRamCheckbox;
  @FXML
  private CheckBox assetDmdCheckbox;

  @FXML
  private VBox assetsBox;

  @FXML
  private Label tableNameLabel;

  @FXML
  private Label tableTitleLabel;

  @FXML
  private TextArea readmeTextField;

  @FXML
  private VBox readmeBox;

  private File selection;
  private Optional<UploadDescriptor> result = Optional.empty();

  private GameRepresentation game;
  private GameEmulatorRepresentation emulatorRepresentation;

  private UploadDescriptor tableUploadDescriptor = UploadDescriptorFactory.create();
  private UploaderAnalysis uploaderAnalysis;
  private Stage stage;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    Stage s = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null) {
      if (!runPreChecks(s)) {
        return;
      }

      uploadBtn.setDisable(true);


      try {
        String subFolder = this.subfolderText.getText();
        boolean useSubFolder = this.subfolderCheckbox.isSelected();
        boolean autoFill = this.autofillCheckbox.isSelected();

        boolean assetPupPack = assetPupPackCheckbox.isSelected();
        boolean assetAltSound = assetAltSoundCheckbox.isSelected();
        boolean assetAltColor = assetAltColorCheckbox.isSelected();
        boolean assetMusic = assetMusicCheckbox.isSelected();
        boolean assetMedia = assetMediaCheckbox.isSelected();
        boolean assetBackglass = assetBackglassCheckbox.isSelected();
        boolean assetRom = assetRomCheckbox.isSelected();
        boolean assetDmd = assetDmdCheckbox.isSelected();
        boolean assetIni = assetIniCheckbox.isSelected();
        boolean assetRes = assetResCheckbox.isSelected();
        boolean assetPov = assetPovCheckbox.isSelected();
        boolean assetNvRam = assetNvRamCheckbox.isSelected();
        boolean assetCfg = assetCfgCheckbox.isSelected();

        int gameId = getGameId();

        Platform.runLater(() -> {
          onCancelClick(event);
        });

        //Platform.runLater(() -> {
        TableUploadProgressModel model = new TableUploadProgressModel("Table Upload", selection, gameId, tableUploadDescriptor.getUploadType(), emulatorRepresentation.getId());
        ProgressResultModel uploadResultModel = ProgressDialog.createProgressDialog(model);

        List<Object> results = uploadResultModel.getResults();
        if (!results.isEmpty()) {
          final UploadDescriptor uploadDescriptor = (UploadDescriptor) results.get(0);
          if (!StringUtils.isEmpty(uploadDescriptor.getError())) {
            Platform.runLater(() -> {
              WidgetFactory.showAlert(stage, "Error", "Upload Failed: " + uploadDescriptor.getError());
            });
            return;
          }


          uploadDescriptor.setSubfolderName(subFolder);
          uploadDescriptor.setFolderBasedImport(useSubFolder);
          uploadDescriptor.setAutoFill(autoFill);

          if (assetAltSound) {
            uploadDescriptor.getAssetsToImport().add(AssetType.ALT_SOUND);
          }
          if (assetAltSound) {
            uploadDescriptor.getAssetsToImport().add(AssetType.ALT_SOUND);
          }
          if (assetAltColor) {
            uploadDescriptor.getAssetsToImport().add(AssetType.ALT_COLOR);
          }
          if (assetBackglass) {
            uploadDescriptor.getAssetsToImport().add(AssetType.DIRECTB2S);
          }
          if (assetDmd) {
            uploadDescriptor.getAssetsToImport().add(AssetType.DMD_PACK);
          }
          if (assetMusic) {
            uploadDescriptor.getAssetsToImport().add(AssetType.MUSIC);
          }
          if (assetMedia) {
            uploadDescriptor.getAssetsToImport().add(AssetType.POPPER_MEDIA);
          }
          if (assetPupPack) {
            uploadDescriptor.getAssetsToImport().add(AssetType.PUP_PACK);
          }
          if (assetRom) {
            uploadDescriptor.getAssetsToImport().add(AssetType.ROM);
          }
          if (assetIni) {
            uploadDescriptor.getAssetsToImport().add(AssetType.INI);
          }
          if (assetPov) {
            uploadDescriptor.getAssetsToImport().add(AssetType.POV);
          }
          if (assetRes) {
            uploadDescriptor.getAssetsToImport().add(AssetType.RES);
          }
          if (assetCfg) {
            uploadDescriptor.getAssetsToImport().add(AssetType.CFG);
          }
          if (assetNvRam) {
            uploadDescriptor.getAssetsToImport().add(AssetType.NV);
          }

          TableUploadProcessingProgressModel progressModel = new TableUploadProcessingProgressModel("Importing Table and Assets", uploadDescriptor);
          ProgressResultModel progressDialogResult = ProgressDialog.createProgressDialog(progressModel);
          if (!progressDialogResult.getResults().isEmpty()) {
            UploadDescriptor uploadedAndImportedDescriptor = (UploadDescriptor) progressDialogResult.getResults().get(0);
            if (!StringUtils.isEmpty(uploadedAndImportedDescriptor.getError())) {
              Platform.runLater(() -> {
                WidgetFactory.showAlert(stage, "Error", "Error during import: " + uploadedAndImportedDescriptor.getError());
              });
              return;
            }

            result = Optional.of(uploadedAndImportedDescriptor);
            // notify listeners of table import done
            EventManager.getInstance().notifyTableUploaded(uploadedAndImportedDescriptor);
          }
        }
        //});
      }
      catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        stage.close();
        WidgetFactory.showAlert(stage, "Uploading VPX file failed.", "Please check the log file for details.", "Error: " + e.getMessage());
      }
    }
  }

  private boolean runPreChecks(Stage s) {
    //check accidental overwrite
    String fileName = selection.getName();
    if (game != null && tableUploadDescriptor.getUploadType().equals(TableUploadType.uploadAndReplace)) {
      boolean similarAtLeastToPercent = StringSimilarity.isSimilarAtLeastToPercent(fileName, game.getGameDisplayName(), 50);
      if (!similarAtLeastToPercent) {
        similarAtLeastToPercent = StringSimilarity.isSimilarAtLeastToPercent(fileName, game.getGameFileName(), 50);
      }
      if (!similarAtLeastToPercent) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(s, "Warning", "The selected file \"" + selection.getName() + "\" doesn't seem to match with table \"" + game.getGameDisplayName() + "\".", "Proceed anyway?", "Yes, replace table");
        if (!result.isPresent() || result.get().equals(ButtonType.CANCEL)) {
          return false;
        }
      }
    }

    //suggest table match
    if (tableUploadDescriptor.getUploadType().equals(TableUploadType.uploadAndImport)) {
      try {
        GameRepresentation game = client.getGameService().findMatch(fileName);
      }
      catch (Exception e) {

      }
    }
    return true;
  }

  private int getGameId() {
    int gameId = -1;
    if (game != null) {
      gameId = game.getId();
    }
    return gameId;
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select VPX File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("VPX File", "*.vpx", "*.zip", ".rar"));

    this.selection = fileChooser.showOpenDialog(stage);
    setSelection(true);
  }

  private void setSelection(boolean rescan) {
    tableNameLabel.setVisible(false);
    tableTitleLabel.setVisible(false);
    this.readmeTextField.setText("");

    uploadBtn.setDisable(true);

    if (this.selection != null) {
      String suffix = FilenameUtils.getExtension(this.selection.getName());
      readmeBox.setVisible(PackageUtil.isSupportedArchive(suffix));

      if (PackageUtil.isSupportedArchive(suffix)) {
        this.fileBtn.setDisable(true);
        this.cancelBtn.setDisable(true);

        Platform.runLater(() -> {
          if (rescan) {
            this.uploaderAnalysis = UploadAnalysisDispatcher.analyzeArchive(selection);
          }

          // If null the analysis was not successful.
          if (this.uploaderAnalysis != null) {
            String analyze = uploaderAnalysis.validateAssetType(AssetType.VPX);

            // If the analysis failed.
            if (analyze != null) {
              WidgetFactory.showAlert(Studio.stage, analyze);

              this.fileNameField.setText("");
              this.subfolderText.setText("");
              this.uploaderAnalysis.reset();
            }
            else {
              String readmeText = uploaderAnalysis.getReadMeText();
              this.readmeTextField.setText(readmeText);

              tableTitleLabel.setVisible(true);
              tableNameLabel.setVisible(true);
              tableNameLabel.setText(uploaderAnalysis.getVpxFileName(null));

              this.fileNameField.setText(this.selection.getAbsolutePath());
              this.subfolderText.setText(FilenameUtils.getBaseName(uploaderAnalysis.getVpxFileName(this.selection.getName())));
            }

            updateAnalysis();

            this.uploadBtn.setDisable(analyze != null);
            this.fileBtn.setDisable(false);
            this.cancelBtn.setDisable(false);

            return;
          }

          this.fileNameField.setText("");
          this.subfolderText.setText("");

          this.fileNameField.setDisable(false);
          this.fileBtn.setDisable(false);
          this.cancelBtn.setDisable(false);
          this.uploadBtn.setDisable(true);
        });
      }
      else {
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

    assetAltColorCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.ALT_COLOR) == null);
    assetAltColorCheckbox.setVisible(assetAltColorCheckbox.isSelected());

    assetMediaCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.POPPER_MEDIA) == null);
    assetMediaCheckbox.setVisible(assetMediaCheckbox.isSelected());

    assetMusicCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.MUSIC) == null);
    assetMusicCheckbox.setVisible(assetMusicCheckbox.isSelected());

    assetBackglassCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.DIRECTB2S) == null);
    assetBackglassCheckbox.setVisible(assetBackglassCheckbox.isSelected());

    assetIniCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.INI) == null);
    assetIniCheckbox.setVisible(assetIniCheckbox.isSelected());

    assetPovCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.POV) == null);
    assetPovCheckbox.setVisible(assetPovCheckbox.isSelected());

    assetResCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.RES) == null);
    assetResCheckbox.setVisible(assetResCheckbox.isSelected());

    assetDmdCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.DMD_PACK) == null);
    assetDmdCheckbox.setVisible(assetDmdCheckbox.isSelected());

    assetRomCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.ROM) == null);
    assetRomCheckbox.setVisible(assetRomCheckbox.isSelected());

    assetCfgCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.CFG) == null);
    assetCfgCheckbox.setVisible(assetCfgCheckbox.isSelected());

    assetNvRamCheckbox.setSelected(uploaderAnalysis.validateAssetType(AssetType.NV) == null);
    assetNvRamCheckbox.setVisible(assetNvRamCheckbox.isSelected());


    assetCfgCheckbox.setText(".cfg File");
    if (assetCfgCheckbox.isSelected()) {
      assetCfgCheckbox.setText(".cfg File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.CFG) + ")");
    }

    assetDmdCheckbox.setText("DMD Pack");
    if (assetDmdCheckbox.isSelected()) {
      assetDmdCheckbox.setText("DMD Pack (" + uploaderAnalysis.getDMDPath() + ")");
    }

    assetNvRamCheckbox.setText(".nv File");
    if (assetNvRamCheckbox.isSelected()) {
      assetNvRamCheckbox.setText(".nv File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.NV) + ")");
    }

    assetPupPackCheckbox.setText("PUP Pack");
    if (assetPupPackCheckbox.isSelected()) {
      assetPupPackCheckbox.setText("PUP Pack (" + uploaderAnalysis.getRomFromPupPack() + ")");
    }

    assetIniCheckbox.setText(".ini File");
    if (assetIniCheckbox.isSelected()) {
      assetIniCheckbox.setText(".ini File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.INI) + ")");
    }

    assetResCheckbox.setText(".res File");
    if (assetResCheckbox.isSelected()) {
      assetResCheckbox.setText(".res File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.RES) + ")");
    }

    assetRomCheckbox.setText("ROM");
    if (assetRomCheckbox.isSelected()) {
      assetRomCheckbox.setText("ROM (" + uploaderAnalysis.getRomFromArchive() + ")");
    }

    assetAltSoundCheckbox.setText("ALT Sound");
    if (assetAltSoundCheckbox.isSelected()) {
      assetAltSoundCheckbox.setText("ALT Sound (" + uploaderAnalysis.getRomFromAltSoundPack() + ")");
    }

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    if (!frontendType.supportPupPacks()) {
      assetPupPackCheckbox.setSelected(false);
      assetPupPackCheckbox.setVisible(false);

      assetMediaCheckbox.setSelected(false);
      assetMediaCheckbox.setVisible(false);
    }


    assetsBox.setVisible(assetBackglassCheckbox.isSelected()
        || assetAltSoundCheckbox.isSelected()
        || assetAltColorCheckbox.isSelected()
        || assetPovCheckbox.isSelected()
        || assetIniCheckbox.isSelected()
        || assetResCheckbox.isSelected()
        || assetCfgCheckbox.isSelected()
        || assetNvRamCheckbox.isSelected()
        || assetMusicCheckbox.isSelected()
        || assetMediaCheckbox.isSelected()
        || assetBackglassCheckbox.isSelected()
        || assetPupPackCheckbox.isSelected()
        || assetRomCheckbox.isSelected());
    noAssetsLabel.setVisible(!assetsBox.isVisible());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    assetPupPackCheckbox.managedProperty().bindBidirectional(assetPupPackCheckbox.visibleProperty());
    assetMediaCheckbox.managedProperty().bindBidirectional(assetMediaCheckbox.visibleProperty());

    root.setOnDragOver(new FileSelectorDragEventHandler(root, "vpx", "zip", "rar"));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      selection = file;
      setSelection(true);
    }));

    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    Frontend frontend = client.getFrontendService().getFrontendCached();

    tableNameLabel.setVisible(false);
    tableTitleLabel.setVisible(false);

    this.selection = null;
    this.uploadBtn.setDisable(true);
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getFrontendService().getVpxGameEmulators();
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

    FrontendUtil.replaceName(uploadAndImportRadio, frontend);
    FrontendUtil.replaceName(uploadAndImportDescription, frontend);
    FrontendUtil.replaceName(uploadAndReplaceDescription, frontend);
    FrontendUtil.replaceName(uploadAndCloneDescription, frontend);

    keepNamesCheckbox.setSelected(serverSettings.isVpxKeepFileNames());
    keepNamesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uploadAndReplaceRadio.setSelected(true);
      serverSettings.setVpxKeepFileNames(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    keepDisplayNamesCheckbox.setSelected(serverSettings.isVpxKeepDisplayNames());
    keepDisplayNamesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uploadAndReplaceRadio.setSelected(true);
      serverSettings.setVpxKeepDisplayNames(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    backupTableOnOverwriteCheckbox.setSelected(serverSettings.isBackupTableOnOverwrite());
    backupTableOnOverwriteCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uploadAndReplaceRadio.setSelected(true);
      serverSettings.setBackupTableOnOverwrite(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    subfolderCheckbox.setSelected(serverSettings.isUseSubfolders());
    subfolderText.setDisable(!subfolderCheckbox.isSelected());
    subfolderCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setUseSubfolders(t1);
      subfolderText.setDisable(!t1);
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

    uploadImportBox.getStyleClass().add("selection-panel-selected");

    noAssetsLabel.setVisible(true);
    noAssetsLabel.managedProperty().bindBidirectional(noAssetsLabel.visibleProperty());
    assetsBox.setVisible(false);
    assetsBox.managedProperty().bindBidirectional(assetsBox.visibleProperty());

    assetPupPackCheckbox.managedProperty().bindBidirectional(assetPupPackCheckbox.visibleProperty());
    assetMusicCheckbox.managedProperty().bindBidirectional(assetMusicCheckbox.visibleProperty());
    assetMediaCheckbox.managedProperty().bindBidirectional(assetMediaCheckbox.visibleProperty());
    assetAltColorCheckbox.managedProperty().bindBidirectional(assetAltColorCheckbox.visibleProperty());
    assetAltSoundCheckbox.managedProperty().bindBidirectional(assetAltSoundCheckbox.visibleProperty());
    assetBackglassCheckbox.managedProperty().bindBidirectional(assetBackglassCheckbox.visibleProperty());
    assetRomCheckbox.managedProperty().bindBidirectional(assetRomCheckbox.visibleProperty());
    assetDmdCheckbox.managedProperty().bindBidirectional(assetDmdCheckbox.visibleProperty());
    assetResCheckbox.managedProperty().bindBidirectional(assetResCheckbox.visibleProperty());
    assetPovCheckbox.managedProperty().bindBidirectional(assetPovCheckbox.visibleProperty());
    assetIniCheckbox.managedProperty().bindBidirectional(assetIniCheckbox.visibleProperty());
    assetCfgCheckbox.managedProperty().bindBidirectional(assetCfgCheckbox.visibleProperty());
    assetNvRamCheckbox.managedProperty().bindBidirectional(assetNvRamCheckbox.visibleProperty());

    assetPupPackCheckbox.setVisible(false);
    assetMediaCheckbox.setVisible(false);
    assetMusicCheckbox.setVisible(false);
    assetAltSoundCheckbox.setVisible(false);
    assetAltColorCheckbox.setVisible(false);
    assetBackglassCheckbox.setVisible(false);
    assetResCheckbox.setVisible(false);
    assetRomCheckbox.setVisible(false);
    assetDmdCheckbox.setVisible(false);
    assetIniCheckbox.setVisible(false);
    assetPovCheckbox.setVisible(false);
    assetNvRamCheckbox.setVisible(false);
    assetCfgCheckbox.setVisible(false);
  }

  public void setGame(@NonNull Stage stage, @Nullable GameRepresentation game, TableUploadType uploadType, UploaderAnalysis analysis) {
    this.stage = stage;
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

      GameEmulatorRepresentation gameEmulator = Studio.client.getFrontendService().getGameEmulator(game.getEmulatorId());
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

    if (this.uploaderAnalysis != null) {
      this.selection = uploaderAnalysis.getFile();
      setSelection(false);
    }
  }

  @Override
  public void onDialogCancel() {
  }

  public Optional<UploadDescriptor> uploadFinished() {
    return result;
  }
}
