package de.mephisto.vpin.ui.archiving;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.VPBMPreferencesController;
import de.mephisto.vpin.ui.tables.TableDialogs;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

public class RepositoryController implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(RepositoryController.class);
  public static final String TAB_NAME = "Table Backups";

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

  @FXML
  private Button clearBtn;

  private Parent loadingOverlay;


  private ObservableList<ArchiveDescriptorRepresentation> data;
  private List<ArchiveDescriptorRepresentation> archives;
  private TablesController tablesController;
  private ChangeListener<ArchiveSourceRepresentation> sourceComboChangeListener;
  private SystemSummary systemSummary;

  // Add a public no-args constructor
  public RepositoryController() {
  }

  @FXML
  private void onClear() {
    searchTextField.setText("");
  }

  @FXML
  private void onVPBM() {
    Platform.runLater(() -> {
      vpbmBtbn.setDisable(true);
    });

    VPBMPreferencesController.openVPBM();


    Platform.runLater(() -> {
      try {
        Thread.sleep(2000);
      }
      catch (InterruptedException e) {
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
          WidgetFactory.showAlert(stage, "Table Exists", "Delete the existing table \"" + first.get().getGameDisplayName() + "\" before restoring it.");
          return;
        }
      }

      if (client.getFrontendService().isFrontendRunning()) {
        if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
          TableDialogs.openTableInstallationDialog(tablesController, selectedItems);
        }
      }
      else {
        TableDialogs.openTableInstallationDialog(tablesController, selectedItems);
      }
    }
  }

  @FXML
  private void onArchiveAdd() {
    boolean uploaded = TableDialogs.openArchiveUploadDialog();
    if (uploaded) {
      doReload();
    }
  }

  @FXML
  private void onDownload() {
    ObservableList<ArchiveDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      TableDialogs.openArchiveDownloadDialog(selectedItems);
    }
  }

  @FXML
  private void onBundle() {
    ObservableList<ArchiveDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      if (systemSummary.getArchiveType().equals(ArchiveType.VPA)) {
        TableDialogs.openVpaArchiveBundleDialog(selectedItems);
      }
      else {
        TableDialogs.openVpbmArchiveBundleDialog(selectedItems);
      }
    }
  }

  @FXML
  private void onToRepositoryCopy() {
    ObservableList<ArchiveDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      TableDialogs.openCopyArchiveToRepositoryDialog(selectedItems);
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
    if (sourceCombo.getValue() != null) {
      disable = sourceCombo.getValue().getId() != -1;
    }

    deleteBtn.setDisable(disable);
    restoreBtn.setDisable(disable);

    tableView.setVisible(false);

    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }


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
          deleteBtn.setDisable(sourceCombo.getValue() != null && sourceCombo.getValue().getId() != -1);
          restoreBtn.setDisable(sourceCombo.getValue() != null && sourceCombo.getValue().getId() != -1);
        }

        this.searchTextField.setDisable(false);

        tableStack.getChildren().remove(loadingOverlay);
        tableView.setVisible(true);

      });
    }).start();
  }

  @FXML
  private void onDelete() {
    List<ArchiveDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      String title = "Delete the " + selectedItems.size() + " selected archives?";
      if (selectedItems.size() == 1) {
        title = "Delete Archive \"" + selectedItems.get(0).getFilename() + "\"?";
      }
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title, null, null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          for (ArchiveDescriptorRepresentation selectedItem : selectedItems) {
            client.getArchiveService().deleteArchive(selectedItem.getSource().getId(), selectedItem.getFilename());
          }
        }
        catch (Exception e) {
          WidgetFactory.showAlert(stage, "Error", "Error deleting archives: " + e.getMessage());
        }
        tableView.getSelectionModel().clearSelection();
        doReload();
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    clearBtn.setVisible(false);
    sourceCombo.managedProperty().bindBidirectional(sourceCombo.visibleProperty());
    sourceCombo.setVisible(false);
    copyToRepositoryBtn.managedProperty().bindBidirectional(copyToRepositoryBtn.visibleProperty());
    copyToRepositoryBtn.setVisible(false);
    tableView.setPlaceholder(new Label("The list of archived tables is shown here."));

    vpbmBtbn.managedProperty().bindBidirectional(vpbmBtbn.visibleProperty());

    systemSummary = client.getSystemService().getSystemSummary();
    vpbmBtbn.setVisible(systemSummary.getArchiveType().equals(ArchiveType.VPBM));


    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Archives...");
    }
    catch (IOException e) {
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

      deleteBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.File.name()) || newSelection == null);
      addArchiveBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.File.name()));
      restoreBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.File.name()) || newSelection == null);
      bundleBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.File.name()) || newSelection == null);
      copyToRepositoryBtn.setDisable(!archiveSource.getType().equals(ArchiveSourceType.Http.name()) || newSelection == null);


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
      clearBtn.setVisible(filterValue != null && filterValue.length() > 0);
      tableView.getSelectionModel().clearSelection();

      List<ArchiveDescriptorRepresentation> filtered = filterArchives(this.archives);
      tableView.setItems(FXCollections.observableList(filtered));
    });

    sourceComboChangeListener = (observable, oldValue, newValue) -> {
      addArchiveBtn.setDisable(!newValue.getType().equals(ArchiveSourceType.File.name()));
      restoreBtn.setDisable(!newValue.getType().equals(ArchiveSourceType.File.name()));
      bundleBtn.setDisable(!newValue.getType().equals(ArchiveSourceType.File.name()));

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

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME));
    ArchiveDescriptorRepresentation archiveDescriptor = tableView.getSelectionModel().getSelectedItem();
    if (archiveDescriptor != null) {
      NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME, archiveDescriptor.getFilename()));
    }
  }

  private void updateSelection(Optional<ArchiveDescriptorRepresentation> newSelection) {
    NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME));
    if (newSelection.isPresent()) {
      ArchiveDescriptorRepresentation descriptorRepresentation = newSelection.get();
      NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME, descriptorRepresentation.getFilename()));
    }
    tablesController.getRepositorySideBarController().setArchiveDescriptor(newSelection);
  }

  private List<ArchiveDescriptorRepresentation> filterArchives(List<ArchiveDescriptorRepresentation> archives) {
    List<ArchiveDescriptorRepresentation> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    for (ArchiveDescriptorRepresentation archive : archives) {
      if (archive.getFilename() != null) {
        String filename = archive.getFilename().toLowerCase();
        if (systemSummary.getArchiveType().equals(ArchiveType.VPBM) && !filename.endsWith(".vpinzip")) {
          continue;
        }

        if (systemSummary.getArchiveType().equals(ArchiveType.VPA) && !filename.endsWith(".vpa")) {
          continue;
        }

        if (filename.contains(filterValue.toLowerCase())) {
          filtered.add(archive);
        }
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

    if (jobType.equals(JobType.TABLE_BACKUP)) {
      EventManager.getInstance().notifyTableChange(event.getGameId(), null);
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

  @Override
  public void onKeyEvent(KeyEvent event) {
    if (event.getCode() == KeyCode.F && event.isControlDown()) {
      searchTextField.requestFocus();
      searchTextField.selectAll();
      event.consume();
    }
    else if (event.getCode() == KeyCode.ESCAPE) {
      if (searchTextField.isFocused()) {
        searchTextField.setText("");
      }
      event.consume();
    }
  }
}