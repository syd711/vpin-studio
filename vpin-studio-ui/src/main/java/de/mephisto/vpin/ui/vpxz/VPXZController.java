package de.mephisto.vpin.ui.vpxz;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.vpxz.*;
import de.mephisto.vpin.restclient.vpxz.models.Table;
import de.mephisto.vpin.restclient.vpxz.models.Tables;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.SystemUtil;
import de.mephisto.vpin.ui.vpxz.dialogs.VPXZInstallationProgressModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class VPXZController extends BaseTableController<VPXZDescriptorRepresentation, VPXZModel> implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZController.class);
  public static final String TAB_NAME = "VPXZ Files";

  @FXML
  private Button deleteBtn;

  @FXML
  private Button openFolderButton;

  @FXML
  private Button addVpxzButton;

  @FXML
  private Button downloadBtn;

  @FXML
  private Button installBtn;

  @FXML
  private Label labelCount;

  @FXML
  private Label connectionStatusLabel;

  @FXML
  private Label connectionVersionLabel;

  @FXML
  private ComboBox<VPXZSourceRepresentation> sourceCombo;

  @FXML
  TableColumn<VPXZModel, VPXZModel> nameColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> romColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> installedColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> povColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> iniColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> vbsColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> resColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> sizeColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> createdAtColumn;

  @FXML
  private Separator endSeparator;

  @FXML
  private ToolBar toolbar;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private List<VPXZModel> filteredData;

  private TablesController tablesController;
  private ChangeListener<VPXZSourceRepresentation> sourceComboChangeListener;
  private SystemSummary systemSummary;
  private List<VPXZDescriptorRepresentation> data;
  private MobileDeviceCheckRunnable mobileDeviceCheckRunnable;
  private Tables installedTables;

  // Add a public no-args constructor
  public VPXZController() {
  }

  @FXML
  public final void onFolder() {
    ObservableList<VPXZModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      VPXZDescriptorRepresentation descriptor = selectedItems.get(0).getBean();
      VPXZSourceRepresentation source = sourceCombo.getValue();

      File file = new File(source.getLocation(), descriptor.getFilename());
      if (file.exists()) {
        SystemUtil.openFile(file);
      }
    }
  }

  @FXML
  private void onVPXZUpload() {
    boolean uploaded = VPXZDialogs.openVpxzUploadDialog();
    if (uploaded) {
      doReload(Optional.empty());
    }
  }

  @FXML
  private void onInstall() {
    VPXZModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      return;
    }

    VPXZDescriptorRepresentation descriptor = selectedItem.getBean();
    ProgressDialog.createProgressDialog(new VPXZInstallationProgressModel("Upload & Install", descriptor));
  }

  @FXML
  private void onConnectionSettings() {
    PreferencesController.open("vpxz");
  }

  @FXML
  private void onDownload() {
    ObservableList<VPXZModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      List<VPXZDescriptorRepresentation> vpxzFiles = selectedItems.stream().map(s -> s.getBean()).collect(Collectors.toList());
      VPXZDialogs.openVpxzDownloadDialog(vpxzFiles);
    }
  }

  @FXML
  public void onReload() {
    doReload(true, Optional.empty());
  }

  public void doReload(Optional<VPXZDescriptorRepresentation> selection) {
    if (selection.isEmpty()) {
      VPXZModel selectedItem = this.tableView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        selection = Optional.of(selectedItem.getBean());
      }
    }

    this.tableView.getSelectionModel().clearSelection();
    doReload(false, selection);
  }

  public void doReload(boolean invalidate, Optional<VPXZDescriptorRepresentation> value) {
    if (value.isEmpty()) {
      VPXZModel selectedItem = this.tableView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        value = Optional.of(selectedItem.getBean());
      }
    }

    this.searchTextField.setDisable(true);

    VPXZSourceRepresentation selectedItem = sourceCombo.getSelectionModel().getSelectedItem();
    final VPXZDescriptorRepresentation selectedVpxz = value.orElse(null);
    tableView.getSelectionModel().clearSelection();
    boolean disable = value.isEmpty();
    deleteBtn.setDisable(disable);

    tableView.setVisible(false);
    labelCount.setText("-");

    startReload("Loading .vpxz Files...");
    JFXFuture.supplyAsync(() -> {
      if (selectedItem != null && invalidate) {
        client.getVpxzService().invalidateVPXZCache();
      }

      VPXZSourceRepresentation vpxMobileSource = sourceCombo.getValue();
      data = client.getVpxzService().getVPXZForSource(vpxMobileSource.getId());
      List<VPXZDescriptorRepresentation> filteredVpxz = filterFiles(data);
      return filteredVpxz;
    }).thenAcceptLater((filtered) -> {
      setItems(filtered);
      tableView.refresh();
      if (tableView.getItems().contains(toModel(selectedVpxz))) {
        deleteBtn.setDisable(false);
        tableView.getSelectionModel().select(toModel(selectedVpxz));
      }

      labelCount.setText(tableView.getItems().size() + " .vpxz files");
      labelCount.setVisible(!tableView.getItems().isEmpty());
      this.searchTextField.setDisable(false);
      tableView.setVisible(true);
      endReload();
    });
  }

  @FXML
  private void onVpxzDelete() {
    List<VPXZModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      String title = "Delete the " + selectedItems.size() + " selected files?";
      if (selectedItems.size() == 1) {
        title = "Delete File \"" + selectedItems.get(0).getName() + "\"?";
      }
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title, null, null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        List<VPXZDescriptorRepresentation> vpxzDescriptors = selectedItems.stream().map(s -> s.getBean()).collect(Collectors.toList());
        ProgressDialog.createProgressDialog(new VPXZDeleteProgressModel(vpxzDescriptors));
        tableView.getSelectionModel().clearSelection();
        doReload(Optional.empty());
        tablesController.getVpxzSidebarController().setVPXZDescriptor(Optional.empty());
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("vpxz", "vpxz", new VPXZColumnSorter(this));

    labelCount.managedProperty().bindBidirectional(labelCount.visibleProperty());

    openFolderButton.setDisable(true);
    clearBtn.setVisible(false);
    endSeparator.managedProperty().bindBidirectional(endSeparator.visibleProperty());
    sourceCombo.managedProperty().bindBidirectional(sourceCombo.visibleProperty());
    openFolderButton.managedProperty().bindBidirectional(openFolderButton.visibleProperty());
    downloadBtn.managedProperty().bindBidirectional(downloadBtn.visibleProperty());
    tableView.setPlaceholder(new Label("This VPXZ source does not contain any files."));

    systemSummary = client.getSystemService().getSystemSummary();
    openFolderButton.setVisible(client.getSystemService().isLocal());
    endSeparator.setVisible(client.getSystemService().isLocal());
    downloadBtn.setVisible(!client.getSystemService().isLocal());

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      clearSelection();
      List<VPXZDescriptorRepresentation> filteredVpxz = filterFiles(data);
      setItems(filteredVpxz);
      clearBtn.setVisible(filterValue != null && !filterValue.isEmpty());
    });
    searchTextField.setOnKeyPressed(event -> {
      if (event.getCode().toString().equalsIgnoreCase("ESCAPE")) {
        searchTextField.setText("");
        tableView.requestFocus();
        setItems(data);
        event.consume();
      }
    });

    BaseLoadingColumn.configureColumn(nameColumn, (value, model) -> {
      VBox vBox = new VBox(3);

      TableDetails tableDetails = value.getTableDetails();
      if (tableDetails != null) {
        Label name = new Label(tableDetails.getGameDisplayName());
        name.getStyleClass().add("default-text");
        vBox.getChildren().add(name);

        Label fileName = new Label(value.getFilename());
        fileName.getStyleClass().add("default-text");
        fileName.setStyle("-fx-font-size: 12px;");
        vBox.getChildren().add(fileName);
      }
      else {
        Label name = new Label(FilenameUtils.getBaseName(value.getFilename()));
        name.getStyleClass().add("default-text");
        vBox.getChildren().add(name);
      }

      Label size = new Label(FileUtils.readableFileSize(value.getSize()));
      size.getStyleClass().add("default-text");
      size.setStyle("-fx-font-size: 12px;");
      vBox.getChildren().add(size);

      Label created = new Label(new SimpleDateFormat().format(value.getCreatedAt()));
      created.getStyleClass().add("default-text");
      created.setStyle("-fx-font-size: 12px;");
      vBox.getChildren().add(created);
      return vBox;
    }, this, true);

    BaseLoadingColumn.configureColumn(installedColumn, (value, model) -> {
      String baseName = FilenameUtils.getBaseName(value.getFilename());
      if (this.installedTables != null) {
        Optional<Table> installedTable = this.installedTables.getTables().stream().filter(t -> t.getName().equalsIgnoreCase(baseName)).findFirst();
        if (installedTable.isPresent()) {
          return WidgetFactory.createCheckIcon();
        }
        else {
          return WidgetFactory.createUnsupportedIcon();
        }
      }

      FontIcon icon = WidgetFactory.createIcon("mdi2c-cloud-question");
      icon.setIconSize(22);
      Label label = new Label();
      label.setTooltip(new Tooltip("No connection to validate installation"));
      label.setGraphic(icon);
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(povColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo pov = value.getPackageInfo().getPov();
        if (pov != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", pov.toString());
        }
      }
      return new Label("");
    }, this, true);


    BaseLoadingColumn.configureColumn(iniColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo ini = value.getPackageInfo().getIni();
        if (ini != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", ini.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(vbsColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo vbs = value.getPackageInfo().getVbs();
        if (vbs != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", vbs.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(resColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo res = value.getPackageInfo().getRes();
        if (res != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", res.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(romColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo rom = value.getPackageInfo().getRom();
        if (rom != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", rom.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(sizeColumn, (value, model) -> {
      if (value.getSize() == 0) {
        return new Label("-");
      }
      Label label = new Label(FileUtils.readableFileSize(value.getSize()));
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(createdAtColumn, (value, model) -> {
      Label label = new Label(DateFormat.getInstance().format(value.getCreatedAt()));
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);


    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      VPXZSourceRepresentation vpxzSource = sourceCombo.getValue();

      installBtn.setDisable(newSelection == null);
      deleteBtn.setDisable(!vpxzSource.getType().equals(VPXZSourceType.Folder.name()) || newSelection == null);
      addVpxzButton.setDisable(!vpxzSource.getType().equals(VPXZSourceType.Folder.name()));
      downloadBtn.setDisable(!vpxzSource.getType().equals(VPXZSourceType.Folder.name()) || tableView.getSelectionModel().getSelectedItems().size() == 0);
      openFolderButton.setDisable(newSelection == null);


      if (oldSelection == null || !oldSelection.equals(newSelection)) {
        updateSelection(newSelection != null ? Optional.of(newSelection.getBean()) : Optional.empty());
      }
    });

    tableView.setRowFactory(tv -> {
      TableRow<VPXZModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
//          onRestore();
        }
      });
      return row;
    });

    sourceComboChangeListener = (observable, oldValue, newValue) -> {
      addVpxzButton.setDisable(!newValue.getType().equals(VPXZSourceType.Folder.name()));
      downloadBtn.setDisable(!newValue.getType().equals(VPXZSourceType.Folder.name()));

      VPXZModel selectedItem = tableView.getSelectionModel().getSelectedItem();
      tableView.getSelectionModel().clearSelection();
      doReload(selectedItem != null ? Optional.of(selectedItem.getBean()) : Optional.empty());
    };
    refreshRepositoryCombo();

    deleteBtn.setDisable(true);
    downloadBtn.setDisable(true);

    mobileDeviceCheckRunnable = new MobileDeviceCheckRunnable(this);
    mobileDeviceCheckRunnable.setPaused(true);
    scheduler.scheduleAtFixedRate(mobileDeviceCheckRunnable, 1, 5, TimeUnit.SECONDS);
  }

  public void refreshConnection() {
    JFXFuture.supplyAsync(() -> {
      return client.getVpxzService().ping();
    }).thenAcceptLater(version -> {
      if (version == null) {
        connectionVersionLabel.setText("-");
        connectionStatusLabel.setText("-");
      }
      else {
        connectionVersionLabel.setText(version.getVersion());
        connectionStatusLabel.setText("Connected");
      }

      if (version != null) {
        Tables tables = client.getVpxzService().getTables();
        boolean refresh = (this.installedTables == null && tables != null) || (tables == null && this.installedTables != null);
        this.installedTables = tables;

        if (refresh) {
          tableView.refresh();
        }
      }
    });
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME));
    if (options == null) {
      refreshView(Optional.empty());
    }
    else {
      refreshView(Optional.of((VPXZDescriptorRepresentation) options.getModel()));
    }
    EventManager.getInstance().removeListener(this);
    EventManager.getInstance().addListener(this);

    mobileDeviceCheckRunnable.setPaused(false);

    onReload();
  }

  @Override
  public void onViewDeactivated() {
    EventManager.getInstance().removeListener(this);
    mobileDeviceCheckRunnable.setPaused(true);
  }

  public void refreshView(Optional<VPXZDescriptorRepresentation> selection) {
    Platform.runLater(() -> {
      toolbar.getItems().stream().forEach(i -> i.setDisable(false));
      this.doReload(selection);
    });
  }

  private void updateSelection(Optional<VPXZDescriptorRepresentation> newSelection) {
    NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME));
    if (newSelection.isPresent()) {
      VPXZDescriptorRepresentation descriptorRepresentation = newSelection.get();
      NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME, descriptorRepresentation.getFilename()));
    }
    tablesController.getVpxzSidebarController().setVPXZDescriptor(newSelection);
  }

  private List<VPXZDescriptorRepresentation> filterFiles(List<VPXZDescriptorRepresentation> descriptors) {
    List<VPXZDescriptorRepresentation> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    for (VPXZDescriptorRepresentation descriptor : descriptors) {
      if (descriptor.getFilename() != null) {
        String filename = descriptor.getFilename().toLowerCase();
        if (!filename.endsWith("." + VPXZType.VPXZ.name().toLowerCase())) {
          continue;
        }

        if (filename.contains(filterValue.toLowerCase())) {
          filtered.add(descriptor);
        }
      }
    }
    return filtered;
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    JobType jobType = event.getJobType();
    if (jobType.equals(JobType.VPXZ_EXPORT)) {
      Platform.runLater(() -> {
        onReload();
      });
    }
  }

  public void repositoryUpdated() {
    Platform.runLater(() -> {
      refreshRepositoryCombo();
      doReload(Optional.empty());
    });
  }

  private void refreshRepositoryCombo() {
    sourceCombo.valueProperty().removeListener(sourceComboChangeListener);
    List<VPXZSourceRepresentation> repositories = new ArrayList<>(client.getVpxzService().getVPXZSources());
    sourceCombo.setItems(FXCollections.observableList(repositories));
    sourceCombo.getSelectionModel().select(0);
    sourceCombo.valueProperty().addListener(sourceComboChangeListener);
  }

  public int getCount() {
    return this.filteredData != null ? this.filteredData.size() : 0;
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

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (preferenceType.equals(PreferenceType.vpxz)) {
      Platform.runLater(() -> {
        onReload();
      });
    }
  }

  @Override
  protected VPXZModel toModel(VPXZDescriptorRepresentation VPXZDescriptorRepresentation) {
    return new VPXZModel(VPXZDescriptorRepresentation);
  }
}