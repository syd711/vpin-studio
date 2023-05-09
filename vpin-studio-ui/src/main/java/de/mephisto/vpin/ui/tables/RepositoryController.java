package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.VpaSourceType;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.ImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.restclient.representations.VpaSourceRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.events.VpaImportedEvent;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class RepositoryController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(RepositoryController.class);

  @FXML
  private Button deleteBtn;

  @FXML
  private Button installBtn;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button downloadBtn;

  @FXML
  private TextField searchTextField;

  @FXML
  private ComboBox<VpaSourceRepresentation> sourceCombo;

  @FXML
  private TableView<VpaDescriptorRepresentation> tableView;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> iconColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> nameColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> directB2SColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> pupPackColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> romColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> popperColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> povColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> altSoundColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> sizeColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> createdAtColumn;

  @FXML
  private StackPane tableStack;

  private Parent loadingOverlay;


  private ObservableList<VpaDescriptorRepresentation> data;
  private List<VpaDescriptorRepresentation> archives;
  private TablesController tablesController;
  private ChangeListener<VpaSourceRepresentation> sourceComboChangeListener;

  // Add a public no-args constructor
  public RepositoryController() {
  }

  @FXML
  private void onInstall() {
    ObservableList<VpaDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      if (client.isPinUPPopperRunning()) {
        Optional<ButtonType> buttonType = Dialogs.openPopperRunningWarning(Studio.stage);
        if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
          Studio.client.terminatePopper();
          Dialogs.openVpaInstallationDialog(tablesController, selectedItems);
        }
      }
      else {
        Dialogs.openVpaInstallationDialog(tablesController, selectedItems);
      }
    }
  }

  @FXML
  private void onUpload() {
    boolean uploaded = Dialogs.openVpaUploadDialog();
    if (uploaded) {
      doReload();
    }
  }

  @FXML
  private void onDownload() {
    ObservableList<VpaDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      Dialogs.openVpaDownloadDialog(selectedItems);
    }
  }

  @FXML
  public void onReload() {
    VpaSourceRepresentation selectedItem = sourceCombo.getSelectionModel().getSelectedItem();
    if(selectedItem != null) {
      client.invalidateVpaCache(selectedItem.getId());
    }
    doReload();
  }

  public void doReload() {
    this.searchTextField.setDisable(true);

    VpaDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    tableView.getSelectionModel().clearSelection();
    boolean disable = selection == null;
    deleteBtn.setDisable(disable);
    installBtn.setDisable(disable);

    tableView.setVisible(false);
    tableStack.getChildren().add(loadingOverlay);

    new Thread(() -> {
      archives = client.getVpaDescriptors();

      Platform.runLater(() -> {
        data = FXCollections.observableList(filterArchives(archives));
        tableView.setItems(data);
        tableView.refresh();
        if (data.contains(selection)) {
          tableView.getSelectionModel().select(selection);
          deleteBtn.setDisable(false);
          installBtn.setDisable(false);
        }
        else if (!data.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }

        this.searchTextField.setDisable(false);

        tableStack.getChildren().remove(loadingOverlay);
        tableView.setVisible(true);

      });
    }).start();
  }

  @FXML
  private void onDelete() {
    VpaDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      //TODO
//      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Archive '" + selection.getFilename() + "'?");
//      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
//        try {
//          client.deleteVpaDescriptor(selection.getSource().getId(), selection.getManifest().getUuid());
//        } catch (Exception e) {
//          WidgetFactory.showAlert(stage, "Error", "Error deleting \"" + selection.getFilename() + "\": " + e.getMessage());
//        }
//        tableView.getSelectionModel().clearSelection();
//        doReload();
//      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Table Repository"));
    tableView.setPlaceholder(new Label("The list of archived tables is shown here."));


    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Archives...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
//TODO
//    iconColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      String thumbnail = value.getManifest().getThumbnail();
//      if (thumbnail == null) {
//        Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
//        ImageView view = new ImageView(wheel);
//        view.setPreserveRatio(true);
//        view.setFitWidth(70);
//        view.setFitHeight(70);
//        return new SimpleObjectProperty(view);
//      }
//
//      byte[] decode = Base64.getDecoder().decode(thumbnail);
//      Image wheel = new Image(new ByteArrayInputStream(decode));
//      ImageView view = new ImageView(wheel);
//      view.setPreserveRatio(true);
//      view.setFitWidth(80);
//      view.setFitHeight(80);
//      ImageUtil.setClippedImage(view, (int) (wheel.getWidth() / 2));
//      return new SimpleObjectProperty(view);
//    });
//
//    nameColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      return new SimpleStringProperty(value.getManifest().getGameDisplayName());
//    });
//
//    directB2SColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      boolean directb2s = value.getManifest().getPackageInfo().isDirectb2s();
//      if (directb2s) {
//        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
//      }
//      return new SimpleStringProperty("");
//    });
//
//    pupPackColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      boolean packaged = value.getManifest().getPackageInfo().isPupPack();
//      if (packaged) {
//        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
//      }
//      return new SimpleStringProperty("");
//    });
//
//    popperColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      boolean packaged = value.getManifest().getPackageInfo().isPopperMedia();
//      if (packaged) {
//        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
//      }
//      return new SimpleStringProperty("");
//    });
//
//    povColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      boolean pov = value.getManifest().getPackageInfo().isPov();
//      if (pov) {
//        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
//      }
//      return new SimpleStringProperty("");
//    });
//
//    romColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      boolean packaged = value.getManifest().getPackageInfo().isPopperMedia();
//      if (packaged) {
//        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
//      }
//      return new SimpleStringProperty("");
//    });
//
//    altSoundColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      boolean enabled = value.getManifest().getPackageInfo().isAltSound();
//      if (enabled) {
//        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
//      }
//      return new SimpleStringProperty("");
//    });
//
//    sizeColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      if(value.getSize() == 0 ) {
//        return new SimpleStringProperty("-");
//      }
//      return new SimpleStringProperty(FileUtils.readableFileSize(value.getSize()));
//    });
//
//    createdAtColumn.setCellValueFactory(cellData -> {
//      VpaDescriptorRepresentation value = cellData.getValue();
//      return new SimpleStringProperty(DateFormat.getInstance().format(value.getCreatedAt()));
//    });
//
//    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
//      boolean fileRepo = newSelection != null && newSelection.getSource().getType().equals(VpaSourceType.File.name());
//      deleteBtn.setDisable(!fileRepo);
//      installBtn.setDisable(newSelection == null);
//      downloadBtn.setDisable(newSelection == null);
//
//      if (oldSelection == null || !oldSelection.equals(newSelection)) {
//        updateSelection(Optional.ofNullable(newSelection));
//      }
//    });
//
//    tableView.setRowFactory(tv -> {
//      TableRow<VpaDescriptorRepresentation> row = new TableRow<>();
//      row.setOnMouseClicked(event -> {
//        if (event.getClickCount() == 2 && (!row.isEmpty())) {
//
//        }
//      });
//      return row;
//    });
//
//    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
//      tableView.getSelectionModel().clearSelection();
//
//      List<VpaDescriptorRepresentation> filtered = filterArchives(this.archives);
//      tableView.setItems(FXCollections.observableList(filtered));
//    });

    sourceComboChangeListener = (observable, oldValue, newValue) -> doReload();
    refreshRepositoryCombo();

    deleteBtn.setDisable(true);
    installBtn.setDisable(true);
    downloadBtn.setDisable(true);

    EventManager.getInstance().addListener(this);
    this.doReload();
  }

  private void updateSelection(Optional<VpaDescriptorRepresentation> newSelection) {
    NavigationController.setBreadCrumb(Arrays.asList("Table Repository"));
    if (newSelection.isPresent()) {
      VpaDescriptorRepresentation descriptorRepresentation = newSelection.get();
      NavigationController.setBreadCrumb(Arrays.asList("Table Repository", descriptorRepresentation.getFilename()));
    }
    tablesController.getRepositorySideBarController().setVpaDescriptor(newSelection);
  }

  private List<VpaDescriptorRepresentation> filterArchives(List<VpaDescriptorRepresentation> archives) {
    List<VpaDescriptorRepresentation> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    VpaSourceRepresentation selectedItem = sourceCombo.getSelectionModel().getSelectedItem();
    for (VpaDescriptorRepresentation archive : archives) {
      if (selectedItem != null && archive.getSource().getId() != selectedItem.getId()) {
        continue;
      }

      if (archive.getFilename() != null && archive.getFilename().toLowerCase().contains(filterValue.toLowerCase())) {
        filtered.add(archive);
      }
    }
    return filtered;
  }

  public Optional<VpaDescriptorRepresentation> getSelection() {
    VpaDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }

  @Override
  public void onVpaSourceUpdate() {
    Platform.runLater(() -> {
      refreshRepositoryCombo();
      doReload();
    });
  }

  @Override
  public void onVpaDownload() {
    Platform.runLater(() -> {
      onReload();
    });
  }

  @Override
  public void onVpaImport(@NonNull VpaImportedEvent event) {
    Platform.runLater(() -> {
      client.invalidateVpaCache(-1);
      onReload();
    });
  }

  private void refreshRepositoryCombo() {
    sourceCombo.valueProperty().removeListener(sourceComboChangeListener);
    List<VpaSourceRepresentation> repositories = new ArrayList<>(client.getVpaSources());
    sourceCombo.setItems(FXCollections.observableList(repositories));
    sourceCombo.getSelectionModel().select(0);
    sourceCombo.valueProperty().addListener(sourceComboChangeListener);
  }

  public int getCount() {
    return this.archives != null ? this.archives.size() : 0;
  }

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  public void initSelection() {
    VpaDescriptorRepresentation vpa = tableView.getSelectionModel().getSelectedItem();
    if(vpa != null) {
      NavigationController.setBreadCrumb(Arrays.asList("Tables", vpa.getManifest().getGameDisplayName()));
    }
  }
}