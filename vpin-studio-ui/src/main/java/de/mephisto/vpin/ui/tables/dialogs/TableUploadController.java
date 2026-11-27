package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.matcher.TableMatcher;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.textedit.TextEditorFile;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.*;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class TableUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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
  private HBox assetsView;

  @FXML
  private FlowPane assetsBox;

  @FXML
  private Label tableNameLabel;

  @FXML
  private Label vpsMatchLabel;

  @FXML
  private FontIcon vpsMatchIcon;

  @FXML
  private Label replaceTargetLabel;

  @FXML
  private Label cloneTargetLabel;

  @FXML
  private Label summaryLabel;

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
  private Runnable finalizer;

  /**
   * Incremented with every VPS lookup, so that a slow lookup does not overwrite the result of a newer selection.
   */
  private int vpsLookupId = 0;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onReadme(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String value = (String) ((Button) e.getSource()).getUserData();
    Dialogs.openTextEditor("readme", stage, new TextEditorFile(value), "README");
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


        GameMediaUploadPostProcessingProgressModel progressModel = new GameMediaUploadPostProcessingProgressModel(Messages.get("dialog.importing_game_media"), uploadDescriptor);
        result = UniversalUploadUtil.postProcess(progressModel);

        if (finalizer != null) {
          finalizer.run();
        }

        if (result.isPresent()) {
          // notify listeners of table import done
          EventManager.getInstance().notifyTableUploaded(result.get());
        }
      }
    }
  }

  @FXML
  private void onAssetFilter() {
    TableDialogs.openMediaUploadDialog(stage, this.game, selection, uploaderAnalysis, AssetType.TABLE, emulatorRepresentation.getId());
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
        Optional<ButtonType> result = WidgetFactory.showConfirmation(s, Messages.get("common.warning"),
            Messages.get("msg.the_selected_file_doesn_t_seem_to", selection.getName(), game.getGameDisplayName()), Messages.get("dialog.proceed_anyway"), Messages.get("dialog.yes_replace_table"));
        if (result.isEmpty() || result.get().equals(ButtonType.CANCEL)) {
          return false;
        }
      }
    }

    //suggest table match
    if (uploadType.equals(UploadType.uploadAndImport)) {
      try {
        ProgressResultModel checkResult = ProgressDialog.createProgressDialog(s,
            new WaitProgressModel<>(Messages.get("dialog.pre_checks"), Messages.get("dialog.running_pre_checks_before_upload"), () -> {
              return client.getGameService().findMatch(fileName);
            }));
        if (checkResult.isCancelled()) {
          return false;
        }
        GameRepresentation game = checkResult.getFirstTypedResult();
        if (game != null) {
          Optional<ButtonType> result = WidgetFactory.showConfirmation(s, Messages.get("dialog.potential_table_match_found"), Messages.get("msg.the_selected_file_seems_to_match_with", selection.getName(), game.getGameDisplayName()),
              Messages.get("dialog.would_you_like_to_proceed_adding_a"), Messages.get("dialog.yes_upload_as_new_table"));
          if (result.isEmpty() || result.get().equals(ButtonType.CANCEL)) {
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
    fileChooser.setTitle(Messages.get("dialog.select_table_file"));

    List<String> filters = Arrays.asList("*.vpx", "*.vpt", "*.zip", "*.rar", "*.7z");
    String description = Messages.get("dialog.filter_vpx_file");
    GameEmulatorRepresentation value = emulatorCombo.getValue();
    if (value != null && value.isFpEmulator()) {
      filters = Arrays.asList("*.fpt", "*.zip", "*.rar", "*.7z");
      description = Messages.get("dialog.filter_fp_file");
    }

    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter(description, filters));

    this.selection = fileChooser.showOpenDialog(stage);
    setSelection(true);
  }

  private void setSelection(boolean rescan) {
    this.readmeLabel.setText("–");
    this.readmeLabel.setVisible(true);
    this.readmeBtn.setVisible(false);
    this.tableNameLabel.setText("–");
    updateVpsMatch();
    updateSummary();

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
            String analyzeVpt = uploaderAnalysis.validateAssetTypeInArchive(AssetType.VPT);
            String _analyzeFpt = uploaderAnalysis.validateAssetTypeInArchive(AssetType.FPT);
            subfolderCheckbox.setDisable(analyzeVpx != null);

            uploadBtn.setDisable(false);
            // If the analysis failed.
            if (analyzeVpx != null && analyzeVpt != null && _analyzeFpt != null) {
              uploadBtn.setDisable(true);
              WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.no_table_file_found_in_this_archive"));
              this.selection = null;

              this.fileNameField.setText("");
              this.subfolderText.setText("");
              this.uploaderAnalysis.reset();
            }
            else {
              String readmeText = uploaderAnalysis.getReadMeText();
              this.readmeLabel.setText(Messages.get("tables.table_upload.no_readme_in_archive"));
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
            updateVpsMatch();
            updateSummary();
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
        this.uploaderAnalysis = new UploaderAnalysis(Features.PUPPACKS_ENABLED, this.selection);
        if (!selectMatchingEmulator()) {
          return;
        }
        EmulatorType emulatorType = this.uploaderAnalysis.getEmulatorType();
        this.subfolderCheckbox.setDisable(emulatorType == null || !emulatorType.isVpxEmulator());
        this.subfolderText.setDisable(emulatorType == null || !emulatorType.isVpxEmulator() || !subfolderCheckbox.isSelected());
        this.fileNameField.setText(this.selection.getAbsolutePath());
        this.tableNameLabel.setText(this.selection.getName());
        this.subfolderText.setText(FilenameUtils.getBaseName(this.selection.getName()));
        this.uploadBtn.setDisable(false);
        updateAnalysis();
        updateVpsMatch();
        updateSummary();
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
      WidgetFactory.showAlert(stage, Messages.get("dialog.invalid_file"), Messages.get("dialog.no_matching_emulator_found_for_the_selected"));
      return false;
    }


    if (emulatorType != null && emulatorType.isVpxEmulator()) {
      if (value != null && value.isVpxEmulator()) {
        return true;
      }

      List<GameEmulatorRepresentation> vpxEmulators = this.emulatorCombo.getItems().stream().filter(GameEmulatorRepresentation::isVpxEmulator).collect(Collectors.toList());
      Optional<GameEmulatorRepresentation> saved = vpxEmulators.stream().filter(e -> e.getId() == uiSettings.getDefaultUploadVpxEmulatorId()).findFirst();
      Optional<GameEmulatorRepresentation> selected = saved.isPresent() ? saved : vpxEmulators.stream().findFirst();
      if (selected.isPresent()) {
        emulatorCombo.setValue(selected.get());
        uiSettings.setDefaultUploadVpxEmulatorId(selected.get().getId());
        client.getPreferenceService().setJsonPreference(uiSettings);
        return true;
      }
      else {
        WidgetFactory.showAlert(stage, Messages.get("dialog.invalid_file"), Messages.get("dialog.no_matching_emulator_found"));
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
        WidgetFactory.showAlert(stage, Messages.get("dialog.invalid_file"), Messages.get("dialog.no_matching_future_pinball_emulator_found"));
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

    assetFilterBtn.setText(Messages.get("tables.table_upload.filter_selection"));
    assetFilterBtn.getStyleClass().remove("sm-danger-button");
    if (!uploaderAnalysis.getExclusions().isEmpty()) {
      assetFilterBtn.getStyleClass().add("sm-danger-button");
      if (uploaderAnalysis.getExclusions().size() == 1) {
        assetFilterBtn.setText(Messages.get("tables.table_upload.filter_selection_excluded_asset", uploaderAnalysis.getExclusions().size()));
      }
      else {
        assetFilterBtn.setText(Messages.get("tables.table_upload.filter_selection_excluded_assets", uploaderAnalysis.getExclusions().size()));
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


    assetAltSoundLabel.setText(pillText(Messages.get("tables.table_upload.alt_sound")));
    assetAltColorLabel.setText(pillText(Messages.get("tables.table_upload.alt_color")));
    assetMediaLabel.setText(pillText(Messages.get("tables.table_upload.media_pack_for_popper_screens")));
    assetMusicLabel.setText(pillText(Messages.get("tables.table_upload.music_pack")));
    assetBackglassLabel.setText(pillText(Messages.get("tables.table_upload.backglass")));
    assetPovLabel.setText(pillText(Messages.get("tables.table_upload.pov_file")));
    FrontendUtil.replaceName(assetMediaLabel, client.getFrontendService().getFrontendCached());

    assetCfgLabel.setText(pillText(Messages.get("tables.table_upload.cfg_file")));
    if (assetCfgLabel.isVisible()) {
      assetCfgLabel.setText(pillText(Messages.get("tables.table_upload.cfg_file_named", uploaderAnalysis.getFileNameForAssetType(AssetType.CFG))));
    }

    assetDmdLabel.setText(pillText(Messages.get("tables.table_upload.dmd_pack")));
    if (assetDmdLabel.isVisible()) {
      assetDmdLabel.setText(pillText(Messages.get("tables.table_upload.dmd_pack_named", uploaderAnalysis.getDMDPath())));
    }

    assetNvRamLabel.setText(pillText(Messages.get("tables.table_upload.nv_file")));
    if (assetNvRamLabel.isVisible()) {
      assetNvRamLabel.setText(pillText(Messages.get("tables.table_upload.nv_file_named", uploaderAnalysis.getFileNameForAssetType(AssetType.NV))));
    }

    assetPupPackLabel.setText(pillText(Messages.get("tables.table_upload.pup_pack")));
    if (assetPupPackLabel.isVisible()) {
      assetPupPackLabel.setText(pillText(Messages.get("tables.table_upload.pup_pack_named", uploaderAnalysis.getRomFromPupPack())));
    }

    assetIniLabel.setText(pillText(Messages.get("tables.table_upload.ini_file")));
    if (assetIniLabel.isVisible()) {
      assetIniLabel.setText(pillText(Messages.get("tables.table_upload.ini_file_named", uploaderAnalysis.getFileNameForAssetType(AssetType.INI))));
    }

    assetResLabel.setText(pillText(Messages.get("tables.table_upload.res_file")));
    if (assetResLabel.isVisible()) {
      assetResLabel.setText(pillText(Messages.get("tables.table_upload.res_file_named", uploaderAnalysis.getFileNameForAssetType(AssetType.RES))));
    }

    assetRomLabel.setText(pillText(Messages.get("tables.table_upload.rom")));
    if (assetRomLabel.isVisible()) {
      assetRomLabel.setText(pillText(Messages.get("tables.table_upload.rom_named", uploaderAnalysis.getRomFromArchive())));
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
    assetsView.managedProperty().bindBidirectional(assetsView.visibleProperty());
    assetsView.setVisible(false);
    readmeLabel.managedProperty().bindBidirectional(readmeLabel.visibleProperty());
    readmeBtn.managedProperty().bindBidirectional(readmeBtn.visibleProperty());

    readmeBtn.setVisible(false);

    uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    assetPupPackLabel.managedProperty().bindBidirectional(assetPupPackLabel.visibleProperty());
    assetMediaLabel.managedProperty().bindBidirectional(assetMediaLabel.visibleProperty());

    root.setOnDragOver(new FileSelectorDragEventHandler(root, "vpx", "vpt", PackageUtil.ARCHIVE_ZIP, PackageUtil.ARCHIVE_RAR, PackageUtil.ARCHIVE_7Z));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      selection = file;
      setSelection(true);
    }));

    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    Frontend frontend = client.getFrontendService().getFrontendCached();

    tableNameLabel.setText("–");
    readmeLabel.setText("–");

    this.selection = null;
    this.uploadBtn.setDisable(true);
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getEmulatorService().getValidatedGameEmulators().stream().filter(e -> e.isFpEmulator() || e.isVpxEmulator()).collect(Collectors.toList());
    emulatorRepresentation = gameEmulators.stream()
        .filter(e -> e.isVpxEmulator() && e.getId() == uiSettings.getDefaultUploadVpxEmulatorId())
        .findFirst()
        .orElse(gameEmulators.getFirst());
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulatorRepresentation);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulatorRepresentation = t1;
      subfolderCheckbox.setDisable(t1 == null || t1.isFpEmulator());
      if (this.game != null) {
        boolean sameEmulator = t1.getId() == game.getEmulatorId();
        uploadAndImportRadio.setSelected(true);
        setTableOptionsEnabled(sameEmulator);
      }
      updateVpsMatch();

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
      updateSummary();
    });

    autofillCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> updateVpsMatch());

    // the whole option card selects its radio button
    uploadImportBox.setOnMouseClicked(e -> selectOption(uploadAndImportRadio));
    uploadReplaceBox.setOnMouseClicked(e -> selectOption(uploadAndReplaceRadio));
    uploadCloneBox.setOnMouseClicked(e -> selectOption(uploadAndCloneRadio));

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
          uploadCloneBox.getStyleClass().add("sm-option-card-selected");
          uploadType = UploadType.uploadAndClone;
          uiSettings.setDefaultUploadMode(UploadType.uploadAndClone.name());
          updateSummary();
        }
        else {
          uploadCloneBox.getStyleClass().remove("sm-option-card-selected");
        }
      }
    });

    uploadAndReplaceRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          uploadReplaceBox.getStyleClass().add("sm-option-card-selected");
          uploadType = UploadType.uploadAndReplace;
          uiSettings.setDefaultUploadMode(UploadType.uploadAndReplace.name());
          updateSummary();
        }
        else {
          uploadReplaceBox.getStyleClass().remove("sm-option-card-selected");
        }
      }
    });

    uploadAndImportRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          uploadImportBox.getStyleClass().add("sm-option-card-selected");
          uploadType = UploadType.uploadAndImport;
          uiSettings.setDefaultUploadMode(UploadType.uploadAndImport.name());
          updateSummary();
        }
        else {
          uploadImportBox.getStyleClass().remove("sm-option-card-selected");
        }
      }
    });

    uploadImportBox.getStyleClass().add("sm-option-card-selected");

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

  public void setGame(@NonNull Stage stage, @Nullable GameRepresentation game, @Nullable UploadType uploadType, UploaderAnalysis analysis, @Nullable Runnable finalizer) {
    this.stage = stage;
    this.finalizer = finalizer;
    this.uploaderAnalysis = analysis;

    if (!StringUtils.isEmpty(uiSettings.getDefaultUploadMode()) && uploadType == null) {
      uploadType = UploadType.valueOf(uiSettings.getDefaultUploadMode());
    }

    if (game == null) {
      uploadType = UploadType.uploadAndImport;
    }

    this.game = game;

    if (game != null) {
      this.replaceTargetLabel.setText(game.getGameDisplayName());
      this.cloneTargetLabel.setText(game.getGameDisplayName());

      GameEmulatorRepresentation gameEmulator = Studio.client.getEmulatorService().getGameEmulator(game.getEmulatorId());
      emulatorCombo.setValue(gameEmulator);
    }
    else {
      this.replaceTargetLabel.setText(Messages.get("tables.table_upload.no_table_selected"));
      this.cloneTargetLabel.setText(Messages.get("tables.table_upload.no_table_selected"));
      setTableOptionsEnabled(false);
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
    updateSummary();
  }

  private void selectOption(RadioButton radio) {
    if (!radio.isDisabled()) {
      radio.setSelected(true);
    }
  }

  /**
   * "Replace" and "Clone" need a selected table of the same emulator, the whole card is disabled otherwise.
   */
  private void setTableOptionsEnabled(boolean enabled) {
    uploadReplaceBox.setDisable(!enabled);
    uploadCloneBox.setDisable(!enabled);
    uploadAndReplaceRadio.setDisable(!enabled);
    uploadAndCloneRadio.setDisable(!enabled);
  }

  /**
   * The asset texts are shared with list views ("- ROM"), the pills show them without the list dash.
   */
  private static String pillText(String listEntry) {
    return StringUtils.removeStart(listEntry, "- ");
  }

  @Nullable
  private String getUploadTableName() {
    if (selection == null) {
      return null;
    }
    String tableFile = uploaderAnalysis != null ? uploaderAnalysis.getTableFileName(selection.getName()) : selection.getName();
    return FilenameUtils.getBaseName(tableFile);
  }

  /**
   * Shows the VPS entry the uploaded table will most likely be matched with by the auto-fill.
   */
  private void updateVpsMatch() {
    int lookupId = ++vpsLookupId;
    vpsMatchLabel.setGraphic(null);
    vpsMatchLabel.getStyleClass().removeAll("sm-hint", "sm-text", "sm-version-ok");
    vpsMatchLabel.getStyleClass().add("sm-hint");

    String tableName = getUploadTableName();
    if (tableName == null) {
      vpsMatchLabel.setText("–");
      return;
    }
    if (!autofillCheckbox.isSelected()) {
      vpsMatchLabel.setText(Messages.get("tables.table_upload.vps_match_disabled"));
      return;
    }

    vpsMatchLabel.setText(Messages.get("tables.table_upload.vps_match_searching"));
    String[] formats = emulatorRepresentation != null ? emulatorRepresentation.getVpsEmulatorFeatures() : null;
    JFXFuture.supplyAsync(() -> {
          List<VpsTable> tables = client.getVpsService().getTables();
          return new TableMatcher(null).findClosest(formats, tableName, null, null, null, 0, tables);
        })
        .thenAcceptLater(vpsTable -> {
          if (lookupId != vpsLookupId) {
            return;
          }
          if (vpsTable == null) {
            vpsMatchLabel.setText(Messages.get("tables.table_upload.vps_match_none"));
            return;
          }
          vpsMatchLabel.setText(vpsTable.getDisplayName());
          vpsMatchLabel.getStyleClass().remove("sm-hint");
          vpsMatchLabel.getStyleClass().addAll("sm-text", "sm-version-ok");
          vpsMatchLabel.setGraphic(vpsMatchIcon);
        })
        .onErrorLater(e -> {
          LOG.warn("VPS lookup for upload failed: {}", e.getMessage(), e);
          if (lookupId == vpsLookupId) {
            vpsMatchLabel.setText(Messages.get("tables.table_upload.vps_match_none"));
          }
        });
  }

  /**
   * Upload button text and the one line summary in the footer, both follow the selected option.
   */
  private void updateSummary() {
    String tableName = getUploadTableName();
    String gameName = game != null ? game.getGameDisplayName() : "";
    switch (uploadType) {
      case uploadAndReplace: {
        uploadBtn.setText(Messages.get("tables.table_upload.upload_and_replace"));
        if (tableName != null) {
          String key = backupTableOnOverwriteCheckbox.isSelected() ? "tables.table_upload.summary_replace_backup" : "tables.table_upload.summary_replace";
          summaryLabel.setText(Messages.get(key, gameName, tableName));
        }
        break;
      }
      case uploadAndClone: {
        uploadBtn.setText(Messages.get("tables.table_upload.upload_and_clone_short"));
        if (tableName != null) {
          summaryLabel.setText(Messages.get("tables.table_upload.summary_clone", gameName, tableName));
        }
        break;
      }
      default: {
        uploadBtn.setText(Messages.get("tables.table_upload.upload_and_add"));
        if (tableName != null) {
          summaryLabel.setText(Messages.get("tables.table_upload.summary_add", tableName));
        }
      }
    }
    if (tableName == null) {
      summaryLabel.setText(Messages.get("tables.table_upload.select_or_drop_a_table_or_archive_file"));
    }
  }

  @Override
  public void onDialogCancel() {
  }

  public Optional<UploadDescriptor> uploadFinished() {
    return result;
  }
}
