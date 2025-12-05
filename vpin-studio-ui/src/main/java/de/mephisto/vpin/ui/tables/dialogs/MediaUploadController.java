package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.tables.models.MediaUploadArchiveItem;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FileSelectorDropEventHandler;
import de.mephisto.vpin.commons.utils.JFXFuture;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaUploadController extends BaseTableController<String, MediaUploadArchiveItem> implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private Node root;

  @FXML
  private Node tableInfo;

  @FXML
  TableColumn<MediaUploadArchiveItem, MediaUploadArchiveItem> columnSelection;

  @FXML
  TableColumn<MediaUploadArchiveItem, MediaUploadArchiveItem> columnFilename;

  @FXML
  TableColumn<MediaUploadArchiveItem, MediaUploadArchiveItem> columnPreview;

  @FXML
  TableColumn<MediaUploadArchiveItem, MediaUploadArchiveItem> columnAssetType;

  @FXML
  TableColumn<MediaUploadArchiveItem, MediaUploadArchiveItem> columnTarget;

  @FXML
  private CheckBox selectAllCheckbox;

  @FXML
  private StackPane loaderStack;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private CheckBox previewCheckbox;

  @FXML
  private Button fileBtn;

  @FXML
  private Label tableNameLabel;

  @FXML
  private Label tableFileLabel;

  @FXML
  private Label emulatorLabel;

  private File selection;
  private Stage stage;

  private boolean result = false;
  private GameEmulatorRepresentation emulator;
  private GameRepresentation game;
  private UploaderAnalysis uploaderAnalysis;

  private List<String> allData;
  private List<MediaUploadArchiveItem> filteredData;
  private AssetType filterMode;

  private List<String> excludedFiles = new ArrayList<>();
  private List<String> excludedFolders = new ArrayList<>();

  private final Map<String, Image> previewCache = new HashMap<>();
  private UISettings uiSettings;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null && selection.exists()) {
      result = true;

      List<String> excludedFiles = new ArrayList<>();
      List<String> excludedFolders = new ArrayList<>();
      this.tableView.getItems().forEach(m -> {
        if (!m.isSelected()) {
          if (m.isFolder()) {
            excludedFolders.add(m.getBean());
          }
          else {
            excludedFiles.add(m.getBean());
          }
        }
      });
      uploaderAnalysis.setExclusions(excludedFiles, excludedFolders);

      if (filterMode != null) {
        stage.close();
      }
      else {
        stage.close();
        Platform.runLater(() -> {
          Optional<UploadDescriptor> result = UniversalUploadUtil.upload(selection, game.getId(), UploadType.uploadAndImport, emulator.getId());
          if (result.isPresent()) {
            UploadDescriptor uploadDescriptor = result.get();
            uploadDescriptor.setSubfolderName(null);
            uploadDescriptor.setFolderBasedImport(false);
            uploadDescriptor.setAutoFill(true);

            uploadDescriptor.setExcludedFiles(uploaderAnalysis.getExcludedFiles());
            uploadDescriptor.setExcludedFolders(uploaderAnalysis.getExcludedFolders());

            GameMediaUploadPostProcessingProgressModel progressModel = new GameMediaUploadPostProcessingProgressModel("Importing Game Media", uploadDescriptor);
            result = UniversalUploadUtil.postProcess(progressModel);

            EventManager.getInstance().notifyTableChange(game.getId(), game.getRom(), game.getGameName());
          }
        });
      }
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {

      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Media Pack");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Media Pack", "*.zip", "*.rar", "*.7z"));
    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null) {
      this.uploaderAnalysis = null;
      refreshSelection();
    }
  }

  private void refreshSelection() {
    this.previewCache.clear();
    this.fileNameField.setText(this.selection.getAbsolutePath());

    if (uploaderAnalysis == null) {
      this.excludedFiles.clear();
      this.excludedFolders.clear();
      this.uploaderAnalysis = UploadAnalysisDispatcher.analyzeArchive(selection);
    }

    startReload("Generating Previews...");

    // run later to let the splash render properly
    JFXFuture.runAsync(() -> {
          allData = uploaderAnalysis.getFileNamesWithPath();
          LOG.info("Media Uploader is analyzing {} file entries.", uploaderAnalysis.getFileNamesWithPath().size());
          allData.addAll(uploaderAnalysis.getFoldersWithPath());
          LOG.info("Media Uploader is analyzing {} folder entries.", uploaderAnalysis.getFoldersWithPath().size());
          LOG.info("Media Uploader is analyzing {} archive entries.", allData.size());

          filteredData = allData.stream().map(d -> toModel(d)).filter(m -> m.getAssetType() != null).collect(Collectors.toList());

          if (uiSettings.isUploadMediaPreview()) {
            List<MediaUploadArchiveItem> images = filteredData.stream().filter(m -> m.isImage()).collect(Collectors.toList());
            for (int i = 0; i < images.size(); i++) {
              MediaUploadArchiveItem model = images.get(i);
              String message = "Generating Previews... (" + (i + 1) + "/" + images.size() + ")";
              Platform.runLater(() -> {
                loadingOverlay.setMessage(message);
              });
              previewCache.put(model.getName(), model.getPreview());
            }
          }
        })
        .thenLater(() -> {
          try {
            tableView.setItems(FXCollections.observableList(filteredData));

            this.fileNameField.setText(this.selection.getAbsolutePath());
            this.fileNameField.setDisable(false);
            this.fileBtn.setDisable(false);
            this.cancelBtn.setDisable(false);
            this.uploadBtn.setDisable(false);
            this.cancelBtn.setDisable(false);

            this.labelCount.setText(allData.size() + " entries");

            uploaderAnalysis.setExclusions(excludedFiles, excludedFolders);
            ObservableList<MediaUploadArchiveItem> items = tableView.getItems();
            for (MediaUploadArchiveItem item : items) {
              item.setSelected(!(excludedFiles.contains(item.getName()) || excludedFolders.contains(item.getName())));
            }

            tableView.refresh();
            refreshAfterSelection();
            endReload();
          }
          catch (Exception e) {
            LOG.error("Media refresh failed: {}", e.getMessage(), e);
          }
        });
  }

  private void refreshAfterSelection() {
    boolean b = tableView.getItems().stream().anyMatch(MediaUploadArchiveItem::isSelected);
    uploadBtn.setDisable(!b);
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }

  public void setData(@Nullable GameRepresentation game, UploaderAnalysis analysis, File file, Stage stage, @Nullable AssetType filterMode, int emulatorId) {
    this.filterMode = filterMode;
    this.game = game;
    this.selection = file;
    this.uploaderAnalysis = analysis;
    this.stage = stage;

    if (emulatorId != -1) {
      this.emulator = client.getEmulatorService().getGameEmulator(emulatorId);
      this.emulatorLabel.setText(this.emulator.getName());
    }

    if (game != null) {
      this.emulator = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
      this.emulatorLabel.setText(this.emulator.getName());
      this.tableNameLabel.setText(this.game.getGameDisplayName());
      this.tableFileLabel.setText(this.game.getGameFilePath());
    }

    if (filterMode != null) {
      this.uploadBtn.setText("Apply Selection");
      this.tableInfo.setVisible(false);
      this.stage.setTitle("Asset Selector");
    }

    if (selection != null) {
      excludedFiles = analysis.getExcludedFiles();
      excludedFolders = analysis.getExcludedFolders();
      analysis.resetExclusions();
      refreshSelection();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("media", "media", new MediaUploaderColumnSorter(this));

    uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    this.tableInfo.managedProperty().bindBidirectional(tableInfo.visibleProperty());

    this.result = false;
    this.selection = null;
    this.uploadBtn.setDisable(true);

    root.setOnDragOver(new FileSelectorDragEventHandler(root, PackageUtil.ARCHIVE_SUFFIXES));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      selection = file;
      refreshSelection();
    }));

    BaseLoadingColumn.configureColumn(columnSelection, (value, model) -> {
      CheckBox columnCheckbox = new CheckBox();
      columnCheckbox.setUserData(value);
      columnCheckbox.setSelected(model.isSelected());
      columnCheckbox.getStyleClass().add("default-text");
      columnCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          model.setSelected(newValue);
          tableView.refresh();
          refreshAfterSelection();
        }
      });

      //you can't de-select on selection mode
      if (filterMode != null) {
        if (filterMode.equals(AssetType.TABLE) && model.isTableAsset()) {
          columnCheckbox.setDisable(true);
        }
        else if (filterMode.equals(AssetType.DIF) && model.isPatch()) {
          columnCheckbox.setDisable(true);
        }
      }

      return columnCheckbox;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnFilename, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setTooltip(new Tooltip(model.getName()));
      if (model.isFolder()) {
        label.setGraphic(WidgetFactory.createIcon("mdi2f-folder-multiple-outline"));
      }
      else {
        label.setGraphic(WidgetFactory.createIcon("mdi2f-file-outline"));
      }
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnTarget, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setText(model.getTargetDisplayName());
      label.setTooltip(new Tooltip(model.getTargetDisplayName()));
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnAssetType, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setText(model.getAssetType().toString());

      if (model.getAssetType().equals(AssetType.ROM.toString())) {
        HBox hBox = new HBox(3);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(label);
        Tooltip tt = new Tooltip("Please check manually if this file is an actual ROM.");
        FontIcon icon = WidgetFactory.createIcon("mdi2h-help-circle-outline");
        icon.setIconSize(18);
        Label iconLabel = new Label();
        iconLabel.setGraphic(icon);
        iconLabel.setTooltip(tt);
        hBox.getChildren().add(iconLabel);
        return hBox;
      }
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnPreview, (value, model) -> {
      if (uiSettings.isUploadMediaPreview() && previewCache.containsKey(model.getName())) {
        ImageView imageView = new ImageView(previewCache.get(model.getName()));
        imageView.setFitWidth(250);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        return imageView;
      }

      Label label = new Label("-");
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    selectAllCheckbox.setSelected(true);
    selectAllCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        tableView.getItems().forEach(m -> m.setSelected(newValue));
        tableView.refresh();
      }
    });

    previewCheckbox.setSelected(uiSettings.isUploadMediaPreview());
    previewCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        uiSettings.setUploadMediaPreview(newValue);
        client.getPreferenceService().setJsonPreference(uiSettings);
        refreshSelection();
      }
    });
  }

  @Override
  protected MediaUploadArchiveItem toModel(String bean) {
    return new MediaUploadArchiveItem(bean, emulator, uploaderAnalysis, filterMode);
  }
}
