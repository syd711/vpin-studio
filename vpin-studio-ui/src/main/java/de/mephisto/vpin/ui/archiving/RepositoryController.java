package de.mephisto.vpin.ui.archiving;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.representations.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.representations.ArchiveSourceRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TablesController;
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
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
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
  private Button restoreBtn;

  @FXML
  private Button addArchiveBtn;

  @FXML
  private Button copyToRepositoryBtn;

  @FXML
  private Button bundleBtn;

  @FXML
  private Button vpbmBtbn;

  @FXML
  private TextField searchTextField;

  @FXML
  private ComboBox<ArchiveSourceRepresentation> sourceCombo;

  @FXML
  private TableView<ArchiveDescriptorRepresentation> tableView;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> iconColumn;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> nameColumn;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> directB2SColumn;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> pupPackColumn;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> romColumn;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> popperColumn;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> povColumn;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> altSoundColumn;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> sizeColumn;

  @FXML
  private TableColumn<ArchiveDescriptorRepresentation, String> createdAtColumn;

  @FXML
  private StackPane tableStack;

  private Parent loadingOverlay;


  private ObservableList<ArchiveDescriptorRepresentation> data;
  private List<ArchiveDescriptorRepresentation> archives;
  private TablesController tablesController;
  private ChangeListener<ArchiveSourceRepresentation> sourceComboChangeListener;

  // Add a public no-args constructor
  public RepositoryController() {
  }

  @FXML
  private void onVPBM() {
    Platform.runLater(() -> {
      vpbmBtbn.setDisable(true);
    });

    new Thread(() -> {
      List<String> commands = Arrays.asList("vPinBackupManager.exe");
      LOG.info("Executing vpbm: " + String.join(" ", commands));
      File dir = new File("./resources/", "vpbm");
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(dir);
      executor.executeCommandAsync();
    }).start();


    Platform.runLater(() -> {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        //ignore
      }
      vpbmBtbn.setDisable(false);
    });
  }

  @FXML
  private void onRestore() {
    ObservableList<ArchiveDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      List<GameRepresentation> games = tablesController.getTableOverviewController().getGames();
      for (ArchiveDescriptorRepresentation selectedItem : selectedItems) {
        String archiveBaseName = FilenameUtils.getBaseName(selectedItem.getFilename());
        Optional<GameRepresentation> first = games.stream().filter(g -> FilenameUtils.getBaseName(g.getGameFileName()).equals(archiveBaseName)).findFirst();
        if (first.isPresent()) {
          WidgetFactory.showAlert(stage, "Table Exists", "Delete the existing table before restoring it.");
          return;
        }
      }


      if (client.getPinUPPopperService().isPinUPPopperRunning()) {
        if (Dialogs.openPopperRunningWarning(Studio.stage)) {
          Dialogs.openTableInstallationDialog(tablesController, selectedItems);
        }
      }
      else {
        Dialogs.openTableInstallationDialog(tablesController, selectedItems);
      }
    }
  }

  @FXML
  private void onArchiveAdd() {
    boolean uploaded = Dialogs.openArchiveUploadDialog();
    if (uploaded) {
      doReload();
    }
  }

//  @FXML
//  private void onDownload() {
//    ObservableList<ArchiveDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
//    if (!selectedItems.isEmpty()) {
//      Dialogs.openArchiveDownloadDialog(selectedItems);
//    }
//  }

  @FXML
  private void onBundle() {
    ObservableList<ArchiveDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      Dialogs.openArchiveBundleDialog(selectedItems);
    }
  }

  @FXML
  private void onToRepositoryCopy() {
    ObservableList<ArchiveDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      Dialogs.openCopyArchiveToRepositoryDialog(selectedItems);
    }
  }

  @FXML
  public void onReload() {
    doReload(true);
  }

  public void doReload() {
    this.tableView.getSelectionModel().clearSelection();
    doReload(false);
  }

  public void doReload(boolean invalidate) {
    this.searchTextField.setDisable(true);

    ArchiveSourceRepresentation selectedItem = sourceCombo.getSelectionModel().getSelectedItem();
    final ArchiveDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    tableView.getSelectionModel().clearSelection();
    boolean disable = selection == null;
    deleteBtn.setDisable(disable);
    restoreBtn.setDisable(disable);

    tableView.setVisible(false);
    tableStack.getChildren().add(loadingOverlay);

    new Thread(() -> {
      if (selectedItem != null && invalidate) {
        client.getArchiveService().invalidateArchiveCache();
      }

      ArchiveSourceRepresentation value = sourceCombo.getValue();
      archives = client.getArchiveService().getArchiveDescriptors(value.getId());

      Platform.runLater(() -> {
        data = FXCollections.observableList(filterArchives(archives));
        tableView.setItems(data);
        tableView.refresh();
        if (data.contains(selection)) {
          tableView.getSelectionModel().select(selection);
          deleteBtn.setDisable(false);
          restoreBtn.setDisable(false);
        }

        this.searchTextField.setDisable(false);

        tableStack.getChildren().remove(loadingOverlay);
        tableView.setVisible(true);

      });
    }).start();
  }

  @FXML
  private void onDelete() {
    ArchiveDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Archive '" + selection.getFilename() + "'?", null, null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getArchiveService().deleteArchive(selection.getSource().getId(), selection.getFilename());
        } catch (Exception e) {
          WidgetFactory.showAlert(stage, "Error", "Error deleting \"" + selection.getFilename() + "\": " + e.getMessage());
        }
        tableView.getSelectionModel().clearSelection();
        doReload();
      }
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

    iconColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        String thumbnail = value.getPackageInfo().getThumbnail();
        if (thumbnail != null) {
          byte[] decode = Base64.getDecoder().decode(thumbnail);
          Image wheel = new Image(new ByteArrayInputStream(decode));
          ImageView view = new ImageView(wheel);
          view.setPreserveRatio(true);
          view.setFitWidth(80);
          view.setFitHeight(80);
          CommonImageUtil.setClippedImage(view, (int) (wheel.getWidth() / 2));
          return new SimpleObjectProperty(view);
        }
      }

      Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
      ImageView view = new ImageView(wheel);
      view.setPreserveRatio(true);
      view.setFitWidth(70);
      view.setFitHeight(70);
      return new SimpleObjectProperty(view);
    });

    nameColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      String gameDisplayName = value.getTableDetails().getGameDisplayName();
      return new SimpleStringProperty(gameDisplayName);
    });

    directB2SColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        boolean directb2s = value.getPackageInfo().isDirectb2s();
        if (directb2s) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
        }
      }
      return new SimpleStringProperty("");
    });

    pupPackColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        boolean packaged = value.getPackageInfo().isPupPack();
        if (packaged) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
        }
      }

      return new SimpleStringProperty("");
    });

    popperColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        boolean packaged = value.getPackageInfo().isPopperMedia();
        if (packaged) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
        }
      }

      return new SimpleStringProperty("");
    });

    povColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        boolean pov = value.getPackageInfo().isPov();
        if (pov) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
        }
      }
      return new SimpleStringProperty("");
    });

    romColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        boolean packaged = value.getPackageInfo().isPopperMedia();
        if (packaged) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
        }
      }
      return new SimpleStringProperty("");
    });

    altSoundColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        boolean enabled = value.getPackageInfo().isAltSound();
        if (enabled) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
        }
      }

      return new SimpleStringProperty("");
    });

    sizeColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      if (value.getSize() == 0) {
        return new SimpleStringProperty("-");
      }
      return new SimpleStringProperty(FileUtils.readableFileSize(value.getSize()));
    });

    createdAtColumn.setCellValueFactory(cellData -> {
      ArchiveDescriptorRepresentation value = cellData.getValue();
      return new SimpleStringProperty(DateFormat.getInstance().format(value.getCreatedAt()));
    });

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      ArchiveSourceRepresentation archiveSource = sourceCombo.getValue();

      deleteBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.File.name()));
      addArchiveBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.File.name()));
      restoreBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.File.name()));
      bundleBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.File.name()));
      copyToRepositoryBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.Http.name()));


      if (oldSelection == null || !oldSelection.equals(newSelection)) {
        updateSelection(Optional.ofNullable(newSelection));
      }
    });

    tableView.setRowFactory(tv -> {
      TableRow<ArchiveDescriptorRepresentation> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {

        }
      });
      return row;
    });

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();

      List<ArchiveDescriptorRepresentation> filtered = filterArchives(this.archives);
      tableView.setItems(FXCollections.observableList(filtered));
    });

    sourceComboChangeListener = (observable, oldValue, newValue) -> {
      addArchiveBtn.setDisable(!newValue.getType().equals(ArchiveSourceType.File.name()));
      restoreBtn.setDisable(!newValue.getType().equals(ArchiveSourceType.File.name()));
      bundleBtn.setDisable(!newValue.getType().equals(ArchiveSourceType.File.name()));
      copyToRepositoryBtn.setDisable(!newValue.getType().equals(ArchiveSourceType.Http.name()));

      tableView.getSelectionModel().clearSelection();
      doReload();
    };
    refreshRepositoryCombo();

    deleteBtn.setDisable(true);
    restoreBtn.setDisable(true);
    bundleBtn.setDisable(true);
    copyToRepositoryBtn.setDisable(true);

    vpbmBtbn.setDisable(sourceCombo.getValue().getId() == -1 || (
        !Studio.client.getSystemService().isLocal() && new File("resources", "vpbm").exists()));

    EventManager.getInstance().addListener(this);
    this.doReload();
  }

  private void updateSelection(Optional<ArchiveDescriptorRepresentation> newSelection) {
    NavigationController.setBreadCrumb(Arrays.asList("Table Repository"));
    if (newSelection.isPresent()) {
      ArchiveDescriptorRepresentation descriptorRepresentation = newSelection.get();
      NavigationController.setBreadCrumb(Arrays.asList("Table Repository", descriptorRepresentation.getFilename()));
    }
    tablesController.getRepositorySideBarController().setArchiveDescriptor(newSelection);
  }

  private List<ArchiveDescriptorRepresentation> filterArchives(List<ArchiveDescriptorRepresentation> archives) {
    List<ArchiveDescriptorRepresentation> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    ArchiveSourceRepresentation selectedItem = sourceCombo.getSelectionModel().getSelectedItem();
    for (ArchiveDescriptorRepresentation archive : archives) {
      if (archive.getFilename() != null && archive.getFilename().toLowerCase().contains(filterValue.toLowerCase())) {
        filtered.add(archive);
      }
    }
    return filtered;
  }

  public Optional<ArchiveDescriptorRepresentation> getSelection() {
    ArchiveDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    JobType jobType = event.getJobType();
    if (jobType.equals(JobType.ARCHIVE_INSTALL)
        || jobType.equals(JobType.ARCHIVE_DOWNLOAD_TO_REPOSITORY)
        || jobType.equals(JobType.ARCHIVE_DOWNLOAD_TO_FILESYSTEM)
    ) {
      Platform.runLater(() -> {
        onReload();
      });
    }
  }

  public void repositoryUpdated() {
    Platform.runLater(() -> {
      refreshRepositoryCombo();
      doReload();
    });
  }

  private void refreshRepositoryCombo() {
    sourceCombo.valueProperty().removeListener(sourceComboChangeListener);
    List<ArchiveSourceRepresentation> repositories = new ArrayList<>(client.getArchiveService().getArchiveSources());
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
    ArchiveDescriptorRepresentation archiveDescriptor = tableView.getSelectionModel().getSelectedItem();
    if (archiveDescriptor != null) {
      NavigationController.setBreadCrumb(Arrays.asList("Tables", archiveDescriptor.getFilename()));
    }
  }
}