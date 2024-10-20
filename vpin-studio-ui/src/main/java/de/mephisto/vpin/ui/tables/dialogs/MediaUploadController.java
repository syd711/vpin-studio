package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaUploadController extends BaseTableController<String, MediaUploadController.ArchiveItem> implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MediaUploadController.class);

  @FXML
  private Node root;

  @FXML
  private Node tableInfo;

  @FXML
  TableColumn<ArchiveItem, ArchiveItem> columnSelection;

  @FXML
  TableColumn<ArchiveItem, ArchiveItem> columnFilename;

  @FXML
  TableColumn<ArchiveItem, ArchiveItem> columnPreview;

  @FXML
  TableColumn<ArchiveItem, ArchiveItem> columnAssetType;

  @FXML
  TableColumn<ArchiveItem, ArchiveItem> columnTarget;

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

  private List<String> archiveItemSelection = new ArrayList<>();
  private Map<String, Image> previewCache = new HashMap<>();
  private List<String> data;
  private List<String> filteredData;

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
      stage.close();

      Platform.runLater(() -> {
        MediaPackUploadProgressModel model = new MediaPackUploadProgressModel(this.game.getId(), "Media Pack Upload", selection);
        ProgressDialog.createProgressDialog(model);
      });
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
    archiveItemSelection.clear();
    previewCache.clear();

    this.fileNameField.setText(this.selection.getAbsolutePath());

    if (uploaderAnalysis == null) {
      this.uploaderAnalysis = UploadAnalysisDispatcher.analyzeArchive(selection);
    }

    startReload("Generating Previews...");

    // run later to let the splash render properly
    JFXFuture.runAsync(() -> {
          data = uploaderAnalysis.getFileNamesWithPath();
          data.addAll(uploaderAnalysis.getDirectories());
          filteredData = data.stream().filter(d -> toModel(d).getAssetType() != null).collect(Collectors.toList());
          setItems(filteredData);

          archiveItemSelection.addAll(filteredData);
        })
        .thenLater(() -> {
          this.emulatorLabel.setText(this.emulator.getName());
          this.fileNameField.setText(this.selection.getAbsolutePath());
          this.fileNameField.setDisable(false);
          this.fileBtn.setDisable(false);
          this.cancelBtn.setDisable(false);
          this.uploadBtn.setDisable(false);
          this.cancelBtn.setDisable(false);

          this.labelCount.setText(data.size() + " entries");

          endReload();
        });
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }

  public void setData(GameRepresentation game, UploaderAnalysis analysis, File file, Stage stage, boolean filterMode) {
    this.emulator = client.getFrontendService().getDefaultGameEmulator();
    this.game = game;
    this.selection = file;
    this.uploaderAnalysis = analysis;
    this.stage = stage;

    if (game != null) {
      this.emulator = client.getFrontendService().getGameEmulator(game.getEmulatorId());
      this.tableNameLabel.setText(this.game.getGameDisplayName());
      this.tableFileLabel.setText(this.game.getGameFilePath());
    }

    if (filterMode) {
      this.uploadBtn.setText("Apply Selection");
      this.tableInfo.setVisible(false);
      this.stage.setTitle("Asset Selector");
    }

    if (selection != null) {
      refreshSelection();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("media", "media", new MediaUploaderColumnSorter(this));

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
      columnCheckbox.setSelected(archiveItemSelection.contains(value));
      columnCheckbox.getStyleClass().add("default-text");
      columnCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (!newValue) {
            archiveItemSelection.remove(value);
          }
          else if (!archiveItemSelection.contains(value)) {
            archiveItemSelection.add(value);
          }
        }
      });
      return columnCheckbox;
    }, true);

    BaseLoadingColumn.configureColumn(columnFilename, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setTooltip(new Tooltip(model.getName()));
      if (model.folder) {
        label.setGraphic(WidgetFactory.createIcon("mdi2f-folder-multiple-outline"));
      }
      else {
        label.setGraphic(WidgetFactory.createIcon("mdi2f-file-outline"));
      }
      return label;
    }, true);

    BaseLoadingColumn.configureColumn(columnTarget, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setText(model.getTarget());
      return label;
    }, true);

    BaseLoadingColumn.configureColumn(columnAssetType, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setText(model.getAssetType().toString());
      return label;
    }, true);

    BaseLoadingColumn.configureColumn(columnPreview, (value, model) -> {
      Image preview = model.getPreview();
      if (preview != null) {
        ImageView imageView = new ImageView(preview);
        imageView.setFitWidth(250);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        return imageView;
      }

      Label label = new Label("-");
      label.getStyleClass().add("default-text");
      return label;
    }, true);

    selectAllCheckbox.setSelected(true);
    selectAllCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (!newValue) {
          archiveItemSelection.clear();
        }
        else {
          archiveItemSelection.clear();
          ObservableList<ArchiveItem> items = tableView.getItems();
          archiveItemSelection.addAll(items.stream().map(ArchiveItem::getName).collect(Collectors.toList()));
        }
        tableView.refresh();
      }
    });
  }

  @Override
  protected ArchiveItem toModel(String bean) {
    return new ArchiveItem(bean);
  }

  public class ArchiveItem extends BaseLoadingModel<String, ArchiveItem> {
    private AssetType assetType;
    private String target;
    private String name;
    private boolean folder;

    public ArchiveItem(String bean) {
      super(bean);
      this.name = bean;
      this.folder = uploaderAnalysis.getDirectories().contains(name);
      load();
    }

    @Override
    public boolean sameBean(String object) {
      return false;
    }

    @Override
    public void load() {
      String name = getName();
      LOG.info("Loading " + name);

      String pupPackDir = uploaderAnalysis.getPUPPackFolder();
      if (pupPackDir != null) {
        //check if we have the PUP pack folder here
        if (pupPackDir.equals(this.getName())) {
          assetType = AssetType.PUP_PACK;
          target = client.getFrontendService().getFrontendCached().getInstallationDirectory() + "...";
          LOG.info(name + ": " + assetType.name());
        }

        //ignore children of PUP packs
        if (name.startsWith(pupPackDir)) {
          return;
        }
      }

      String dmdDir = uploaderAnalysis.getDMDPath();
      if (dmdDir != null) {
        //check if we have the DMD bundle here
        if (dmdDir.equals(this.getName())) {
          assetType = AssetType.DMD_PACK;
          target = emulator.getTablesDirectory() + "...";
          LOG.info(name + ": " + assetType.name());
        }

        if (name.startsWith(dmdDir)) {
          return;
        }
      }

      String musicFolder = uploaderAnalysis.getMusicFolder();
      if (musicFolder != null) {
        //check if we have the musicFolder bundle here
        if (musicFolder.equals(this.getName())) {
          assetType = AssetType.MUSIC_BUNDLE;
          target = emulator.getTablesDirectory() + "...";
          LOG.info(name + ": " + assetType.name());
        }

        if (name.startsWith(musicFolder)) {
          return;
        }
      }

      //ignore all folders from here
      if (folder) {
        return;
      }

      VPinScreen[] screens = VPinScreen.values();
      for (VPinScreen screen : screens) {
        List<String> popperMediaFiles = uploaderAnalysis.getPopperMediaFiles(screen);
        if (popperMediaFiles.contains(name)) {
          assetType = AssetType.POPPER_MEDIA;
          target = "Screen \"" + screen.name() + "\"";
          LOG.info(name + ": " + assetType.name());
          return;
        }
      }

      if (resolveTableFileAssets(Arrays.asList(AssetType.INI, AssetType.POV, AssetType.RES, AssetType.RES, AssetType.DIRECTB2S, AssetType.VPX))) {
        LOG.info(name + ": " + assetType.name());
        return;
      }

      String extension = FilenameUtils.getExtension(name);
      AssetType asset = AssetType.fromExtension(extension);
      if (asset != null) {
        if (asset.equals(AssetType.RAR) || asset.equals(AssetType.SEVENZIP)) {
          return;
        }

        if (asset.equals(AssetType.NV)) {
          this.assetType = asset;
          this.target = client.getFrontendService().getDefaultGameEmulator().getNvramDirectory();
          LOG.info(name + ": " + assetType.name());
        }
        else if (asset.equals(AssetType.ROM)) {
          this.assetType = asset;
          this.target = client.getFrontendService().getDefaultGameEmulator().getRomDirectory();
          LOG.info(name + ": " + assetType.name());
        }
        else if (asset.equals(AssetType.PAL) || asset.equals(AssetType.PAC) || asset.equals(AssetType.CRZ) || asset.equals(AssetType.VNI)) {
          this.assetType = asset;
          this.target = client.getFrontendService().getDefaultGameEmulator().getAltColorDirectory();
          LOG.info(name + ": " + assetType.name());
        }
        else if (asset.equals(AssetType.ZIP)) {
          this.assetType = AssetType.ROM;
          this.target = client.getFrontendService().getDefaultGameEmulator().getRomDirectory();
          LOG.info(name + ": " + assetType.name());
        }
      }

      LOG.info("Unsupported asset type for \"" + name + "\"");
    }

    @Override
    public String getName() {
      return bean;
    }

    public String getTarget() {
      return target;
    }

    public String getAssetType() {
      if (assetType != null) {
        getPreview();
        return assetType.toString();
      }
      return null;
    }

    public Image getPreview() {
      String name = getName();
      if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".gif")) {
        if (!previewCache.containsKey(name)) {
          try {
            LOG.info("Reading previewable archive file " + name);
            InputStream in = new ByteArrayInputStream(uploaderAnalysis.readFile(name));
            Image image = new Image(in);
            previewCache.put(name, image);
          }
          catch (Exception e) {
            LOG.error("Failed to create preview: {}", e.getMessage(), e);
          }
        }

        return previewCache.get(name);
      }
      return null;
    }

    private boolean resolveTableFileAssets(List<AssetType> tableAssetTypes) {
      String extension = FilenameUtils.getExtension(getName());
      for (AssetType tableAssetType : tableAssetTypes) {
        if (extension.equalsIgnoreCase(tableAssetType.name())) {
          target = emulator.getTablesDirectory();
          assetType = tableAssetType;
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) return true;
      if (object == null || getClass() != object.getClass()) return false;
      if (!super.equals(object)) return false;
      ArchiveItem that = (ArchiveItem) object;
      return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), name);
    }
  }
}
