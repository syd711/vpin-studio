package de.mephisto.vpin.ui.vpxz;

import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.vpxz.*;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class VPXZController extends BaseTableController<VPXZDescriptorRepresentation, VPXZModel> implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZController.class);
  public static final String TAB_NAME = "VPXZ Files";

  @FXML
  private Button deleteBtn;

  @FXML
  private Button openFolderButton;

  @FXML
  private Button addArchiveBtn;

  @FXML
  private Button downloadBtn;

  @FXML
  private Label labelCount;

  @FXML
  private ComboBox<VPXZSourceRepresentation> sourceCombo;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> iconColumn;

  @FXML
  TableColumn<VPXZModel, VPXZModel> nameColumn;

  @FXML
  TableColumn<VPXZModel, VPXZModel> directB2SColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> romColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> povColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> iniColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> vbsColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> resColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> musicColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> highscoreColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> dmdColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> registryColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> altSoundColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> altColorColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> sizeColumn;

  @FXML
  private TableColumn<VPXZModel, VPXZModel> createdAtColumn;

  @FXML
  private Separator endSeparator;

  @FXML
  private ToolBar toolbar;

  private List<VPXZModel> filteredData;

  private TablesController tablesController;
  private ChangeListener<VPXZSourceRepresentation> sourceComboChangeListener;
  private SystemSummary systemSummary;
  private List<VPXZDescriptorRepresentation> data;

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
  private void onArchiveAdd() {
    boolean uploaded = VPXZDialogs.openArchiveUploadDialog();
    if (uploaded) {
      doReload(Optional.empty());
    }
  }

  @FXML
  private void onDownload() {
    ObservableList<VPXZModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      List<VPXZDescriptorRepresentation> backups = selectedItems.stream().map(s -> s.getBean()).collect(Collectors.toList());
      VPXZDialogs.openArchiveDownloadDialog(backups);
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
    final VPXZDescriptorRepresentation selectedBackup = value.orElse(null);
    tableView.getSelectionModel().clearSelection();
    boolean disable = value.isEmpty();
    deleteBtn.setDisable(disable);

    tableView.setVisible(false);
    labelCount.setText("-");

    startReload("Loading Backups...");
    JFXFuture.supplyAsync(() -> {
      if (selectedItem != null && invalidate) {
        client.getVPXMobileService().invalidateVPXZCache();
      }

      VPXZSourceRepresentation vpxMobileSource = sourceCombo.getValue();
      data = client.getVPXMobileService().getVPXZForSource(vpxMobileSource.getId());
      List<VPXZDescriptorRepresentation> filteredBackups = filterArchives(data);
      return filteredBackups;
    }).thenAcceptLater((filteredBackups) -> {
      setItems(filteredBackups);
      tableView.refresh();
      if (tableView.getItems().contains(toModel(selectedBackup))) {
        deleteBtn.setDisable(false);
        tableView.getSelectionModel().select(toModel(selectedBackup));
      }

      labelCount.setText(tableView.getItems().size() + " backups");
      this.searchTextField.setDisable(false);
      tableView.setVisible(true);
      endReload();
    });
  }

  @FXML
  private void onBackupDelete() {
    List<VPXZModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      String title = "Delete the " + selectedItems.size() + " selected archives?";
      if (selectedItems.size() == 1) {
        title = "Delete Archive \"" + selectedItems.get(0).getName() + "\"?";
      }
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title, null, null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        List<VPXZDescriptorRepresentation> backups = selectedItems.stream().map(s -> s.getBean()).collect(Collectors.toList());
        ProgressDialog.createProgressDialog(new VPXZDeleteProgressModel(backups));
        tableView.getSelectionModel().clearSelection();
        doReload(Optional.empty());
        tablesController.getRepositorySideBarController().setArchiveDescriptor(Optional.empty());
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("backup", "backups", new VPXZColumnSorter(this));

    openFolderButton.setDisable(true);
    clearBtn.setVisible(false);
    endSeparator.managedProperty().bindBidirectional(endSeparator.visibleProperty());
    sourceCombo.managedProperty().bindBidirectional(sourceCombo.visibleProperty());
    openFolderButton.managedProperty().bindBidirectional(openFolderButton.visibleProperty());
    downloadBtn.managedProperty().bindBidirectional(downloadBtn.visibleProperty());
    tableView.setPlaceholder(new Label("This backup source does contains any files."));

    systemSummary = client.getSystemService().getSystemSummary();
    openFolderButton.setVisible(client.getSystemService().isLocal());
    endSeparator.setVisible(client.getSystemService().isLocal());
    downloadBtn.setVisible(!client.getSystemService().isLocal());

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      clearSelection();
      List<VPXZDescriptorRepresentation> filteredBackups = filterArchives(data);
      setItems(filteredBackups);
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

    BaseLoadingColumn.configureColumn(iconColumn, (value, model) -> {
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
          return view;
        }
      }

      Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
      ImageView view = new ImageView(wheel);
      view.setPreserveRatio(true);
      view.setFitWidth(70);
      view.setFitHeight(70);
      return view;
    }, this, true);

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

    BaseLoadingColumn.configureColumn(directB2SColumn, (value, model) -> {
      Label label = new Label("");
      if (value.getPackageInfo() != null) {
        VPXZFileInfo directb2s = value.getPackageInfo().getDirectb2s();
        if (directb2s != null) {
          Label iconLabel = WidgetFactory.createCheckboxIcon("#FFFFFF", directb2s.toString());
          int nbVersions = directb2s.getFiles();
          FontIcon icon = null;
          if (nbVersions > 9) {
            icon = WidgetFactory.createIcon("mdi2n-numeric-9-plus-box-multiple-outline", "#FFFFFF");
            iconLabel.setGraphic(icon);
          }
          else if (nbVersions > 1) {
            icon = WidgetFactory.createIcon("mdi2n-numeric-" + nbVersions + "-box-multiple-outline", "#FFFFFF");
            iconLabel.setGraphic(icon);
          }
          return iconLabel;
        }
      }
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

    BaseLoadingColumn.configureColumn(musicColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo mus = value.getPackageInfo().getMusic();
        if (mus != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", mus.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(highscoreColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo highscore = value.getPackageInfo().getHighscore();
        if (highscore != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", highscore.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(dmdColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo dmd = value.getPackageInfo().getDmd();
        if (dmd != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", dmd.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(registryColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo reg = value.getPackageInfo().getMameData();
        if (reg != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", reg.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(altColorColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo altColor = value.getPackageInfo().getAltColor();
        if (altColor != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", altColor.toString());
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

    BaseLoadingColumn.configureColumn(altSoundColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        VPXZFileInfo altSound = value.getPackageInfo().getAltSound();
        if (altSound != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", altSound.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(sizeColumn, (value, model) -> {
      if (value.getSize() == 0) {
        return new Label("-");
      }
      return new Label(FileUtils.readableFileSize(value.getSize()));
    }, this, true);

    BaseLoadingColumn.configureColumn(createdAtColumn, (value, model) -> {
      return new Label(DateFormat.getInstance().format(value.getCreatedAt()));
    }, this, true);


    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      VPXZSourceRepresentation archiveSource = sourceCombo.getValue();

      deleteBtn.setDisable(!archiveSource.getType().equals(VPXZSourceType.Folder.name()) || newSelection == null);
      addArchiveBtn.setDisable(!archiveSource.getType().equals(VPXZSourceType.Folder.name()));
      downloadBtn.setDisable(!archiveSource.getType().equals(VPXZSourceType.Folder.name()) || tableView.getSelectionModel().getSelectedItems().size() == 0);
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
      addArchiveBtn.setDisable(!newValue.getType().equals(VPXZSourceType.Folder.name()));
      downloadBtn.setDisable(!newValue.getType().equals(VPXZSourceType.Folder.name()));

      VPXZModel selectedItem = tableView.getSelectionModel().getSelectedItem();
      tableView.getSelectionModel().clearSelection();
      doReload(selectedItem != null ? Optional.of(selectedItem.getBean()) : Optional.empty());
    };
    refreshRepositoryCombo();

    deleteBtn.setDisable(true);
    downloadBtn.setDisable(true);
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
  }

  public void refreshView(Optional<VPXZDescriptorRepresentation> selection) {
    toolbar.getItems().stream().forEach(i -> i.setDisable(true));
    JFXFuture.supplyAsync(() -> {
      return client.getAuthenticationService().isAuthenticated();
    }).thenAcceptLater(authenticated -> {
      if (authenticated) {
        toolbar.getItems().stream().forEach(i -> i.setDisable(false));
        this.doReload(selection);
      }
      else {
        WidgetFactory.showInformation(stage, "Authentication Required", "Go to the backup settings for more details.");
      }
    });
  }

  @Override
  public void onViewDeactivated() {
    EventManager.getInstance().removeListener(this);
  }

  private void updateSelection(Optional<VPXZDescriptorRepresentation> newSelection) {
    NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME));
    if (newSelection.isPresent()) {
      VPXZDescriptorRepresentation descriptorRepresentation = newSelection.get();
      NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME, descriptorRepresentation.getFilename()));
    }
    tablesController.getVpxzSidebarController().setVPXZDescriptor(newSelection);
  }

  private List<VPXZDescriptorRepresentation> filterArchives(List<VPXZDescriptorRepresentation> archives) {
    List<VPXZDescriptorRepresentation> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    for (VPXZDescriptorRepresentation archive : archives) {
      if (archive.getFilename() != null) {
        String filename = archive.getFilename().toLowerCase();
        if (!filename.endsWith("." + VPXZType.VPXZ.name().toLowerCase())) {
          continue;
        }

        if (filename.contains(filterValue.toLowerCase())) {
          filtered.add(archive);
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
    List<VPXZSourceRepresentation> repositories = new ArrayList<>(client.getVPXMobileService().getVPXZSources());
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

  public void selectBackup(VPXZDescriptorRepresentation backup) {
    NavigationOptions options = new NavigationOptions(backup);
    onViewActivated(options);
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
    if (preferenceType.equals(PreferenceType.backups)) {
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