package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.StringSimilarity;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
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
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TableUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableUploadController.class);
  public static final int MATCHING_PERCENTAGE = 90;

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
  private Button assetFilterBtn;

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
  private Label assetPupPackLabel;
  @FXML
  private Label assetAltSoundLabel;
  @FXML
  private Label assetAltColorLabel;
  @FXML
  private Label assetMediaLabel;
  @FXML
  private Label assetMusicLabel;
  @FXML
  private Label assetBackglassLabel;
  @FXML
  private Label assetRomLabel;
  @FXML
  private Label assetPovLabel;
  @FXML
  private Label assetIniLabel;
  @FXML
  private Label assetResLabel;
  @FXML
  private Label assetCfgLabel;
  @FXML
  private Label assetNvRamLabel;
  @FXML
  private Label assetDmdLabel;

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
  private UploaderAnalysis<?> uploaderAnalysis;
  private Stage stage;
  private UISettings uiSettings;

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

      String subFolder = this.subfolderText.getText();
      boolean useSubFolder = this.subfolderCheckbox.isSelected();
      boolean autoFill = this.autofillCheckbox.isSelected();
      int gameId = getGameId();

      uploadBtn.setDisable(true);
      Platform.runLater(() -> {
        onCancelClick(event);
      });

      result = UniversalUploader.upload(selection, gameId, tableUploadDescriptor.getUploadType(), emulatorRepresentation);
      if (result.isPresent()) {
        UploadDescriptor uploadDescriptor = result.get();
        uploadDescriptor.setSubfolderName(subFolder);
        uploadDescriptor.setFolderBasedImport(useSubFolder);
        uploadDescriptor.setAutoFill(autoFill);

        uploadDescriptor.setExcludedFiles(uploaderAnalysis.getExcludedFiles());
        uploadDescriptor.setExcludedFolders(uploaderAnalysis.getExcludedFolders());
        result = UniversalUploader.postProcess(uploadDescriptor);
        if (result.isPresent()) {
          // notify listeners of table import done
          EventManager.getInstance().notifyTableUploaded(result.get());
        }
      }
    }
  }

  @FXML
  private void onAssetFilter() {
    TableDialogs.openMediaUploadDialog(this.game, selection, uploaderAnalysis, true);
    updateAnalysis();
  }

  private boolean runPreChecks(Stage s) {
    //check accidental overwrite
    String fileName = FilenameUtils.getBaseName(selection.getName());
    if (game != null && tableUploadDescriptor.getUploadType().equals(TableUploadType.uploadAndReplace)) {
      boolean similarAtLeastToPercent = StringSimilarity.isSimilarAtLeastToPercent(fileName.replaceAll("_", " "), game.getGameDisplayName(), MATCHING_PERCENTAGE);
      if (!similarAtLeastToPercent) {
        similarAtLeastToPercent = StringSimilarity.isSimilarAtLeastToPercent(fileName, FilenameUtils.getBaseName(game.getGameFileName()), MATCHING_PERCENTAGE);
      }
      if (!similarAtLeastToPercent) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(s, "Warning",
            "The selected file \"" + selection.getName() + "\" doesn't seem to match with table \"" + game.getGameDisplayName() + "\".", "Proceed anyway?", "Yes, replace table");
        if (!result.isPresent() || result.get().equals(ButtonType.CANCEL)) {
          return false;
        }
      }
    }

    //suggest table match
    if (tableUploadDescriptor.getUploadType().equals(TableUploadType.uploadAndImport)) {
      try {
        ProgressResultModel checkResult = ProgressDialog.createProgressDialog(s,
            new WaitProgressModel<>("Pre-Checks", "Running pre-checks before upload...", () -> {
              return client.getGameService().findMatch(fileName);
            }));
        if (checkResult.isCancelled()) {
          return false;
        }
        GameRepresentation game = checkResult.getFirstTypedResult();
        if (game != null) {
          Optional<ButtonType> result = WidgetFactory.showConfirmation(s, "Potential Table Match Found", "The selected file \"" + selection.getName() + "\" seems to match with the table \"" + game.getGameDisplayName() + "\".",
              "Would you like to proceed adding a new table?", "Yes, upload as new table");
          if (!result.isPresent() || result.get().equals(ButtonType.CANCEL)) {
            return false;
          }
        }
      }
      catch (Exception e) {
        LOG.warn("Failed to find matching table: " + e.getMessage(), e);
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
        new FileChooser.ExtensionFilter("VPX File", "*.vpx", "*.zip", "*.rar", "*.7z"));

    this.selection = fileChooser.showOpenDialog(stage);
    setSelection(true);
  }

  private void setSelection(boolean rescan) {
    assetFilterBtn.setVisible(false);
    if (this.selection != null) {
      String extension = FilenameUtils.getExtension(selection.getName());
      if (PackageUtil.isSupportedArchive(extension)) {
        assetFilterBtn.setVisible(true);
      }
    }

    tableNameLabel.setVisible(false);
    tableTitleLabel.setVisible(false);
    this.readmeTextField.setText("");

    subfolderCheckbox.setDisable(true);
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
            if (!selectMatchingEmulator()) {
              return;
            }
          }

          // If null the analysis was not successful.
          if (this.uploaderAnalysis != null) {
            String analyzeVpx = uploaderAnalysis.validateAssetType(AssetType.VPX);
            String analyzeFpt = uploaderAnalysis.validateAssetType(AssetType.FPT);
            subfolderCheckbox.setDisable(analyzeVpx != null);

            uploadBtn.setDisable(false);
            // If the analysis failed.
            if (analyzeVpx != null && analyzeFpt != null) {
              uploadBtn.setDisable(true);
              WidgetFactory.showAlert(Studio.stage, "No table file found in this archive.");
              this.selection = null;

              this.assetFilterBtn.setVisible(false);
              this.fileNameField.setText("");
              this.subfolderText.setText("");
              this.uploaderAnalysis.reset();
            }
            else {
              String readmeText = uploaderAnalysis.getReadMeText();
              this.readmeTextField.setText(readmeText);

              tableTitleLabel.setVisible(true);
              tableNameLabel.setVisible(true);
              tableNameLabel.setText(uploaderAnalysis.getTableFileName(null));

              this.fileNameField.setText(this.selection.getAbsolutePath());
              this.subfolderText.setText(FilenameUtils.getBaseName(uploaderAnalysis.getTableFileName(this.selection.getName())));
            }

            updateAnalysis();
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
        this.uploadBtn.setDisable(false);
      }
    }
    else {
      this.fileNameField.setText("");
    }
  }

  private boolean selectMatchingEmulator() {
    boolean vpx = this.uploaderAnalysis.validateAssetType(AssetType.VPX) == null;
    boolean fp = this.uploaderAnalysis.validateAssetType(AssetType.FPT) == null;
    GameEmulatorRepresentation value = emulatorCombo.getValue();

    if (vpx) {
      if (value != null && value.isVpxEmulator()) {
        return true;
      }

      Optional<GameEmulatorRepresentation> first = this.emulatorCombo.getItems().stream().filter(GameEmulatorRepresentation::isVpxEmulator).findFirst();
      if (first.isPresent()) {
        emulatorCombo.setValue(first.get());
        return true;
      }
      else {
        WidgetFactory.showAlert(stage, "Invalid File", "No matching Future Pinball emulator found.");
        this.selection = null;
        setSelection(false);
      }
    }
    else if (fp) {
      if (value != null && value.isFpEmulator()) {
        return true;
      }

      Optional<GameEmulatorRepresentation> first = this.emulatorCombo.getItems().stream().filter(GameEmulatorRepresentation::isFpEmulator).findFirst();
      if (first.isPresent()) {
        emulatorCombo.setValue(first.get());
        return true;
      }
      else {
        WidgetFactory.showAlert(stage, "Invalid File", "No matching Future Pinball emulator found.");
        this.selection = null;
        setSelection(false);
      }
    }
    return false;
  }

  private void updateAnalysis() {
    if (uploaderAnalysis == null) {
      return;
    }

    assetFilterBtn.setVisible(selection != null);
    assetFilterBtn.setText("Filter Selection");
    if (!uploaderAnalysis.getExclusions().isEmpty()) {
      if (uploaderAnalysis.getExclusions().size() == 1) {
        assetFilterBtn.setText("Filter Selection (" + uploaderAnalysis.getExclusions().size() + " excluded asset)");
      }
      else {
        assetFilterBtn.setText("Filter Selection (" + uploaderAnalysis.getExclusions().size() + " excluded assets)");
      }
    }

    assetPupPackLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.PUP_PACK) == null);
    assetAltSoundLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.ALT_SOUND) == null);
    assetAltColorLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.ALT_COLOR) == null);
    assetMediaLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.FRONTEND_MEDIA) == null);
    assetMusicLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.MUSIC) == null);
    assetBackglassLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.DIRECTB2S) == null);
    assetIniLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.INI) == null);
    assetPovLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.POV) == null);
    assetResLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.RES) == null);
    assetDmdLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.DMD_PACK) == null);
    assetRomLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.ROM) == null);
    assetCfgLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.CFG) == null);
    assetNvRamLabel.setVisible(uploaderAnalysis.validateAssetType(AssetType.NV) == null);


    assetCfgLabel.setText("- .cfg File");
    if (assetCfgLabel.isVisible()) {
      assetCfgLabel.setText("- .cfg File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.CFG) + ")");
    }

    assetDmdLabel.setText("- DMD Pack");
    if (assetDmdLabel.isVisible()) {
      assetDmdLabel.setText("- DMD Pack (" + uploaderAnalysis.getDMDPath() + ")");
    }

    assetNvRamLabel.setText("- .nv File");
    if (assetNvRamLabel.isVisible()) {
      assetNvRamLabel.setText("- .nv File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.NV) + ")");
    }

    assetPupPackLabel.setText("- PUP Pack");
    if (assetPupPackLabel.isVisible()) {
      assetPupPackLabel.setText("- PUP Pack (" + uploaderAnalysis.getRomFromPupPack() + ")");
    }

    assetIniLabel.setText("- .ini File");
    if (assetIniLabel.isVisible()) {
      assetIniLabel.setText("- .ini File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.INI) + ")");
    }

    assetResLabel.setText("- .res File");
    if (assetResLabel.isVisible()) {
      assetResLabel.setText("- .res File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.RES) + ")");
    }

    assetRomLabel.setText("- ROM");
    if (assetRomLabel.isVisible()) {
      assetRomLabel.setText("- ROM (" + uploaderAnalysis.getRomFromArchive() + ")");
    }

    assetAltSoundLabel.setText("- ALT Sound");
    if (assetAltSoundLabel.isVisible()) {
      assetAltSoundLabel.setText("- ALT Sound (" + uploaderAnalysis.getRomFromAltSoundPack() + ")");
    }

    assetsBox.setVisible(assetBackglassLabel.isVisible()
        || assetAltSoundLabel.isVisible()
        || assetAltColorLabel.isVisible()
        || assetPovLabel.isVisible()
        || assetIniLabel.isVisible()
        || assetResLabel.isVisible()
        || assetCfgLabel.isVisible()
        || assetNvRamLabel.isVisible()
        || assetMusicLabel.isVisible()
        || assetMediaLabel.isVisible()
        || assetBackglassLabel.isVisible()
        || assetPupPackLabel.isVisible()
        || assetRomLabel.isVisible());
    noAssetsLabel.setVisible(!assetsBox.isVisible());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    assetFilterBtn.setVisible(false);

    uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    assetPupPackLabel.managedProperty().bindBidirectional(assetPupPackLabel.visibleProperty());
    assetMediaLabel.managedProperty().bindBidirectional(assetMediaLabel.visibleProperty());

    root.setOnDragOver(new FileSelectorDragEventHandler(root, "vpx", PackageUtil.ARCHIVE_ZIP, PackageUtil.ARCHIVE_RAR, PackageUtil.ARCHIVE_7Z));
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

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getFrontendService().getGameEmulators().stream().filter(e -> e.isFpEmulator() || e.isVpxEmulator()).collect(Collectors.toList());
    emulatorRepresentation = gameEmulators.get(0);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulatorRepresentation);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulatorRepresentation = t1;
      subfolderCheckbox.setDisable(t1 == null || t1.isFpEmulator());
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
          uiSettings.setDefaultUploadMode(TableUploadType.uploadAndClone.name());
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
          uiSettings.setDefaultUploadMode(TableUploadType.uploadAndReplace.name());
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
          uiSettings.setDefaultUploadMode(TableUploadType.uploadAndImport.name());
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

    assetPupPackLabel.managedProperty().bindBidirectional(assetPupPackLabel.visibleProperty());
    assetMusicLabel.managedProperty().bindBidirectional(assetMusicLabel.visibleProperty());
    assetMediaLabel.managedProperty().bindBidirectional(assetMediaLabel.visibleProperty());
    assetAltColorLabel.managedProperty().bindBidirectional(assetAltColorLabel.visibleProperty());
    assetAltSoundLabel.managedProperty().bindBidirectional(assetAltSoundLabel.visibleProperty());
    assetBackglassLabel.managedProperty().bindBidirectional(assetBackglassLabel.visibleProperty());
    assetRomLabel.managedProperty().bindBidirectional(assetRomLabel.visibleProperty());
    assetDmdLabel.managedProperty().bindBidirectional(assetDmdLabel.visibleProperty());
    assetResLabel.managedProperty().bindBidirectional(assetResLabel.visibleProperty());
    assetPovLabel.managedProperty().bindBidirectional(assetPovLabel.visibleProperty());
    assetIniLabel.managedProperty().bindBidirectional(assetIniLabel.visibleProperty());
    assetCfgLabel.managedProperty().bindBidirectional(assetCfgLabel.visibleProperty());
    assetNvRamLabel.managedProperty().bindBidirectional(assetNvRamLabel.visibleProperty());

    assetPupPackLabel.setVisible(false);
    assetMediaLabel.setVisible(false);
    assetMusicLabel.setVisible(false);
    assetAltSoundLabel.setVisible(false);
    assetAltColorLabel.setVisible(false);
    assetBackglassLabel.setVisible(false);
    assetResLabel.setVisible(false);
    assetRomLabel.setVisible(false);
    assetDmdLabel.setVisible(false);
    assetIniLabel.setVisible(false);
    assetPovLabel.setVisible(false);
    assetNvRamLabel.setVisible(false);
    assetCfgLabel.setVisible(false);
  }

  public void setGame(@NonNull Stage stage, @Nullable GameRepresentation game, @Nullable TableUploadType uploadType, UploaderAnalysis analysis) {
    this.stage = stage;
    this.uploaderAnalysis = analysis;

    if (!StringUtils.isEmpty(uiSettings.getDefaultUploadMode()) && uploadType == null) {
      uploadType = TableUploadType.valueOf(uiSettings.getDefaultUploadMode());
    }

    if (game == null) {
      uploadType = TableUploadType.uploadAndImport;
    }

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
