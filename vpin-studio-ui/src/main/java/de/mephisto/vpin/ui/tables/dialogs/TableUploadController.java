package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.matcher.TableMatcher;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.restclient.vps.VpsInstallLink;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
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
  private VBox assetsView;

  @FXML
  private VBox assetsBox;

  @FXML
  private Label tableNameLabel;

  @FXML
  private Label tableTitleLabel;

  @FXML
  private Label readmeLabel;

  @FXML
  private Button readmeBtn;

  private File selection;
  private Optional<UploadDescriptor> result = Optional.empty();

  private GameRepresentation game;
  private GameEmulatorRepresentation emulatorRepresentation;

  private UploadType uploadType = UploadType.uploadAndImport;
  private UploaderAnalysis uploaderAnalysis;
  private Stage stage;
  private UISettings uiSettings;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onReadme(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String value = (String) ((Button) e.getSource()).getUserData();
    Dialogs.openTextEditor("readme", stage, new MonitoredTextFile(value), "README");
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

      result = UniversalUploadUtil.upload(selection, gameId, uploadType, emulatorRepresentation.getId());
      if (result.isPresent()) {
        UploadDescriptor uploadDescriptor = result.get();
        uploadDescriptor.setSubfolderName(subFolder);
        uploadDescriptor.setFolderBasedImport(useSubFolder);
        uploadDescriptor.setAutoFill(autoFill);

        uploadDescriptor.setExcludedFiles(uploaderAnalysis.getExcludedFiles());
        uploadDescriptor.setExcludedFolders(uploaderAnalysis.getExcludedFolders());


        GameMediaUploadPostProcessingProgressModel progressModel = new GameMediaUploadPostProcessingProgressModel("Importing Game Media", uploadDescriptor);
        result = UniversalUploadUtil.postProcess(progressModel);
        if (result.isPresent()) {
          // notify listeners of table import done
          EventManager.getInstance().notifyTableUploaded(result.get());
        }
      }
    }
  }

  @FXML
  private void onAssetFilter() {
    TableDialogs.openMediaUploadDialog(stage, this.game, selection, uploaderAnalysis, AssetType.TABLE, -1);
    updateAnalysis();
  }

  private boolean runPreChecks(Stage s) {
    //check accidental overwrite
    String fileName = FilenameUtils.getBaseName(selection.getName());
    if (game != null && uploadType.equals(UploadType.uploadAndReplace)) {
      TableMatcher matcher = new TableMatcher(null);
      boolean similar = false;
      if (StringUtils.isNotEmpty(game.getExtTableId())) {
        VpsTable vpsTable = client.getVpsService().getTableById(game.getExtTableId());
        similar = matcher.isClose(vpsTable, fileName);
      }
      else {
        similar = matcher.isClose(game.getGameDisplayName(), fileName);
      }

      if (!similar) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(s, "Warning",
            "The selected file \"" + selection.getName() + "\" doesn't seem to match with table \"" + game.getGameDisplayName() + "\".", "Proceed anyway?", "Yes, replace table");
        if (!result.isPresent() || result.get().equals(ButtonType.CANCEL)) {
          return false;
        }
      }
    }

    //suggest table match
    if (uploadType.equals(UploadType.uploadAndImport)) {
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

    List<String> filters = Arrays.asList("*.vpx", "*.zip", "*.rar", "*.7z");
    String description = "FP file";
    GameEmulatorRepresentation value = emulatorCombo.getValue();
    if (value != null && value.isFpEmulator()) {
      filters = Arrays.asList("*.fpt", "*.zip", "*.rar", "*.7z");
      description = "VPX file";
    }

    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter(description, filters));

    this.selection = fileChooser.showOpenDialog(stage);
    setSelection(true);
  }

  private void setSelection(boolean rescan) {
    this.readmeLabel.setVisible(true);
    this.readmeBtn.setVisible(false);

    subfolderCheckbox.setDisable(true);
    uploadBtn.setDisable(true);
    this.assetsView.setVisible(false);

    if (this.selection != null) {
      String suffix = FilenameUtils.getExtension(this.selection.getName());
      if (PackageUtil.isSupportedArchive(suffix)) {
        this.fileBtn.setDisable(true);
        this.cancelBtn.setDisable(true);

        Platform.runLater(() -> {
          if (rescan) {
            this.uploaderAnalysis = UploadAnalysisDispatcher.analyzeArchive(selection);
          }
          this.fileBtn.setDisable(false);
          this.cancelBtn.setDisable(false);

          // If null the analysis was not successful.
          if (this.uploaderAnalysis != null) {
            if (!selectMatchingEmulator()) {
              return;
            }

            String analyzeVpx = uploaderAnalysis.validateAssetTypeInArchive(AssetType.VPX);
            String analyzeFpt = uploaderAnalysis.validateAssetTypeInArchive(AssetType.FPT);
            subfolderCheckbox.setDisable(analyzeVpx != null);

            uploadBtn.setDisable(false);
            // If the analysis failed.
            if (analyzeVpx != null && analyzeFpt != null) {
              uploadBtn.setDisable(true);
              WidgetFactory.showAlert(Studio.stage, "No table file found in this archive.");
              this.selection = null;

              this.fileNameField.setText("");
              this.subfolderText.setText("");
              this.uploaderAnalysis.reset();
            }
            else {
              String readmeText = uploaderAnalysis.getReadMeText();
              if (!StringUtils.isEmpty(readmeText)) {
                this.readmeBtn.setUserData(readmeText);
                this.readmeBtn.setVisible(true);
                this.readmeLabel.setVisible(false);
              }

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
      else if (VpsInstallLink.isLinkFilename(selection.getName())) {
        this.fileNameField.setText("");
        this.fileNameField.setDisable(true);
        this.fileBtn.setDisable(true);
        this.uploadBtn.setDisable(false);
      }
      else {
        this.uploaderAnalysis = new UploaderAnalysis(Features.PUPPACKS_ENABLED, this.selection);
        if (!selectMatchingEmulator()) {
          return;
        }
        EmulatorType emulatorType = this.uploaderAnalysis.getEmulatorType();
        this.subfolderCheckbox.setDisable(emulatorType == null || !emulatorType.isVpxEmulator());
        this.subfolderText.setDisable(emulatorType == null || !emulatorType.isVpxEmulator() || !subfolderCheckbox.isSelected());
        this.fileNameField.setText(this.selection.getAbsolutePath());
        this.subfolderText.setText(FilenameUtils.getBaseName(this.selection.getName()));
        this.uploadBtn.setDisable(false);
        updateAnalysis();
      }
    }
    else {
      this.fileNameField.setText("");
    }
  }

  private boolean selectMatchingEmulator() {
    EmulatorType emulatorType = this.uploaderAnalysis.getEmulatorType();
    GameEmulatorRepresentation value = emulatorCombo.getValue();

    if(emulatorType == null) {
      WidgetFactory.showAlert(stage, "Invalid File", "No matching emulator found for the selected file.");
      return false;
    }


    if (emulatorType != null && emulatorType.isVpxEmulator()) {
      if (value != null && value.isVpxEmulator()) {
        return true;
      }

      Optional<GameEmulatorRepresentation> first = this.emulatorCombo.getItems().stream().filter(GameEmulatorRepresentation::isVpxEmulator).findFirst();
      if (first.isPresent()) {
        emulatorCombo.setValue(first.get());
        return true;
      }
      else {
        WidgetFactory.showAlert(stage, "Invalid File", "No matching emulator found.");
        this.selection = null;
        setSelection(false);
      }
    }
    else if (emulatorType != null && emulatorType.isFpEmulator()) {
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
      assetsView.setVisible(false);
      return;
    }

    assetFilterBtn.setText("Filter Selection");
    if (!uploaderAnalysis.getExclusions().isEmpty()) {
      if (uploaderAnalysis.getExclusions().size() == 1) {
        assetFilterBtn.setText("Filter Selection (" + uploaderAnalysis.getExclusions().size() + " excluded asset)");
      }
      else {
        assetFilterBtn.setText("Filter Selection (" + uploaderAnalysis.getExclusions().size() + " excluded assets)");
      }
    }

    assetPupPackLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.PUP_PACK) == null);
    assetAltSoundLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.ALT_SOUND) == null);
    assetAltColorLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.ALT_COLOR) == null);
    assetMediaLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.FRONTEND_MEDIA) == null);
    assetMusicLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.MUSIC) == null);
    assetBackglassLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.DIRECTB2S) == null);
    assetIniLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.INI) == null);
    assetPovLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.POV) == null);
    assetResLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.RES) == null);
    assetDmdLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.DMD_PACK) == null);
    assetRomLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.ROM) == null);
    assetCfgLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.CFG) == null);
    assetNvRamLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.NV) == null);


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
      assetAltSoundLabel.setText("- ALT Sound");
    }

    assetsView.setVisible(assetBackglassLabel.isVisible()
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
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    assetsView.setVisible(false);
    readmeLabel.managedProperty().bindBidirectional(readmeLabel.visibleProperty());
    readmeBtn.managedProperty().bindBidirectional(readmeBtn.visibleProperty());

    readmeBtn.setVisible(false);

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

    tableNameLabel.setText("-");

    this.selection = null;
    this.uploadBtn.setDisable(true);
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getEmulatorService().getValidatedGameEmulators().stream().filter(e -> e.isFpEmulator() || e.isVpxEmulator()).collect(Collectors.toList());
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
        uploadAndCloneRadio.setDisable(!sameEmulator);
        keepDisplayNamesCheckbox.setDisable(!sameEmulator);
        keepNamesCheckbox.setDisable(!sameEmulator);
        backupTableOnOverwriteCheckbox.setDisable(!sameEmulator);
      }

      EmulatorType emulatorType = emulatorRepresentation.getType();
      if (this.uploaderAnalysis != null) {
        this.uploadBtn.setDisable(!this.uploaderAnalysis.getEmulatorType().equals(emulatorType));
      }
      else if (selection != null) {
        UploaderAnalysis analysis = new UploaderAnalysis(Features.PUPPACKS_ENABLED, selection);
        this.uploadBtn.setDisable(!analysis.getEmulatorType().equals(emulatorType));
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
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    keepDisplayNamesCheckbox.setSelected(serverSettings.isVpxKeepDisplayNames());
    keepDisplayNamesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uploadAndReplaceRadio.setSelected(true);
      serverSettings.setVpxKeepDisplayNames(t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    backupTableOnOverwriteCheckbox.setSelected(serverSettings.isBackupTableOnOverwrite());
    backupTableOnOverwriteCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uploadAndReplaceRadio.setSelected(true);
      serverSettings.setBackupTableOnOverwrite(t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    subfolderCheckbox.setSelected(serverSettings.isUseSubfolders());
    subfolderText.setDisable(!subfolderCheckbox.isSelected());
    subfolderCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setUseSubfolders(t1);
      subfolderText.setDisable(!t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    uploadAndCloneRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          uploadCloneBox.getStyleClass().add("selection-panel-selected");
          uploadType = UploadType.uploadAndClone;
          uiSettings.setDefaultUploadMode(UploadType.uploadAndClone.name());
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
          uploadType = UploadType.uploadAndReplace;
          uiSettings.setDefaultUploadMode(UploadType.uploadAndReplace.name());
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
          uploadType = UploadType.uploadAndImport;
          uiSettings.setDefaultUploadMode(UploadType.uploadAndImport.name());
        }
        else {
          uploadImportBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    uploadImportBox.getStyleClass().add("selection-panel-selected");

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

  public void setGame(@NonNull Stage stage, @Nullable GameRepresentation game, @Nullable UploadType uploadType, UploaderAnalysis analysis) {
    this.stage = stage;
    this.uploaderAnalysis = analysis;

    if (!StringUtils.isEmpty(uiSettings.getDefaultUploadMode()) && uploadType == null) {
      uploadType = UploadType.valueOf(uiSettings.getDefaultUploadMode());
    }

    if (game == null) {
      uploadType = UploadType.uploadAndImport;
    }

    this.game = game;

    if (game != null) {
      this.uploadAndReplaceRadio.setText("Upload and Replace \"" + game.getGameDisplayName() + "\"");
      this.uploadAndCloneRadio.setText("Upload and Clone \"" + game.getGameDisplayName() + "\"");

      GameEmulatorRepresentation gameEmulator = Studio.client.getEmulatorService().getGameEmulator(game.getEmulatorId());
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
