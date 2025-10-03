package de.mephisto.vpin.ui.backups;

import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.backups.*;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.components.emulators.EmulatorsTableColumnSorter;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.SystemUtil;
import de.mephisto.vpin.ui.vps.VpsTablesController;
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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class BackupsController extends BaseTableController<BackupDescriptorRepresentation, BackupModel> implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(BackupsController.class);
  public static final String TAB_NAME = "Table Backups";

  @FXML
  private Button deleteBtn;

  @FXML
  private Button restoreBtn;

  @FXML
  private Button openFolderButton;

  @FXML
  private Button addArchiveBtn;

  @FXML
  private Button downloadBtn;

  @FXML
  private ComboBox<BackupSourceRepresentation> sourceCombo;

  @FXML
  private TableColumn<BackupModel, BackupModel> iconColumn;

  @FXML
  TableColumn<BackupModel, BackupModel> nameColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> directB2SColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> pupPackColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> romColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> popperColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> povColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> iniColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> vbsColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> resColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> musicColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> highscoreColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> dmdColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> registryColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> altSoundColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> altColorColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> sizeColumn;

  @FXML
  private TableColumn<BackupModel, BackupModel> createdAtColumn;

  @FXML
  private Separator endSeparator;

  @FXML
  private ToolBar toolbar;


  private List<BackupModel> filteredData;

  private TablesController tablesController;
  private ChangeListener<BackupSourceRepresentation> sourceComboChangeListener;
  private SystemSummary systemSummary;
  private List<BackupDescriptorRepresentation> data;

  // Add a public no-args constructor
  public BackupsController() {
  }

  @FXML
  public final void onFolder() {
    ObservableList<BackupModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      BackupDescriptorRepresentation descriptor = selectedItems.get(0).getBean();
      BackupSourceRepresentation source = sourceCombo.getValue();

      File file = new File(source.getLocation(), descriptor.getFilename());
      if (file.exists()) {
        SystemUtil.openFile(file);
      }
    }
  }

  @FXML
  private void onRestore() {
    ObservableList<BackupModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      List<BackupDescriptorRepresentation> backups = selectedItems.stream().map(s -> s.getBean()).collect(Collectors.toList());
      BackupDialogs.openArchiveRestoreDialog(backups);
    }
  }

  @FXML
  private void onArchiveAdd() {
    boolean uploaded = BackupDialogs.openArchiveUploadDialog();
    if (uploaded) {
      doReload(Optional.empty());
    }
  }

  @FXML
  private void onDownload() {
    ObservableList<BackupModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      List<BackupDescriptorRepresentation> backups = selectedItems.stream().map(s -> s.getBean()).collect(Collectors.toList());
      BackupDialogs.openArchiveDownloadDialog(backups);
    }
  }

  @FXML
  public void onReload() {
    doReload(true, Optional.empty());
  }

  public void doReload(Optional<BackupDescriptorRepresentation> selection) {
    if (selection.isEmpty()) {
      BackupModel selectedItem = this.tableView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        selection = Optional.of(selectedItem.getBean());
      }
    }

    this.tableView.getSelectionModel().clearSelection();
    doReload(false, selection);
  }

  public void doReload(boolean invalidate, Optional<BackupDescriptorRepresentation> value) {
    if (value.isEmpty()) {
      BackupModel selectedItem = this.tableView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        value = Optional.of(selectedItem.getBean());
      }
    }

    this.searchTextField.setDisable(true);

    BackupSourceRepresentation selectedItem = sourceCombo.getSelectionModel().getSelectedItem();
    final BackupDescriptorRepresentation selection = value.orElse(null);
    tableView.getSelectionModel().clearSelection();
    boolean disable = value.isEmpty();
    deleteBtn.setDisable(disable);
    restoreBtn.setDisable(disable);

    tableView.setVisible(false);

    startReload("Loading Backups...");
    JFXFuture.supplyAsync(() -> {
      if (selectedItem != null && invalidate) {
        client.getArchiveService().invalidateBackupCache();
      }

      BackupSourceRepresentation backupSource = sourceCombo.getValue();
      data = client.getArchiveService().getBackupsForSource(backupSource.getId());
      List<BackupDescriptorRepresentation> filteredBackups = filterArchives(data);
      return filteredBackups;
    }).thenAcceptLater((filteredBackups) -> {
      this.filteredData = filteredBackups.stream().map(e -> toModel(e)).collect(Collectors.toList());
      tableView.setItems(FXCollections.observableList(filteredData));
      tableView.refresh();
      if (filteredData.contains(selection)) {
        deleteBtn.setDisable(false);
        tableView.getSelectionModel().select(toModel(selection));
        restoreBtn.setDisable(false);
      }

      this.searchTextField.setDisable(false);

      tableStack.getChildren().remove(loadingOverlay);
      tableView.setVisible(true);
      endReload();
    });
  }

  @FXML
  private void onDelete() {
    List<BackupModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      String title = "Delete the " + selectedItems.size() + " selected archives?";
      if (selectedItems.size() == 1) {
        title = "Delete Archive \"" + selectedItems.get(0).getName() + "\"?";
      }
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title, null, null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        List<BackupDescriptorRepresentation> backups = selectedItems.stream().map(s -> s.getBean()).collect(Collectors.toList());
        ProgressDialog.createProgressDialog(new BackupDeleteProgressModel(backups));
        tableView.getSelectionModel().clearSelection();
        doReload(Optional.empty());
        tablesController.getRepositorySideBarController().setArchiveDescriptor(Optional.empty());
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("backup", "backups", new BackupsColumnSorter(this));

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

    restoreBtn.setDisable(true);

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      clearSelection();
      List<BackupDescriptorRepresentation> filteredBackups = filterArchives(data);
      filteredData = filteredBackups.stream().map(b -> toModel(b)).collect(Collectors.toList());
      tableView.setItems(FXCollections.observableList(filteredData));
      tableView.refresh();
      clearBtn.setVisible(filterValue != null && !filterValue.isEmpty());
    });
    searchTextField.setOnKeyPressed(event -> {
      if (event.getCode().toString().equalsIgnoreCase("ESCAPE")) {
        searchTextField.setText("");
        tableView.requestFocus();
        tableView.setItems(FXCollections.observableList(data.stream().map(b -> toModel(b)).collect(Collectors.toList())));
        tableView.refresh();
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
        BackupFileInfo directb2s = value.getPackageInfo().getDirectb2s();
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

    BaseLoadingColumn.configureColumn(pupPackColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo pupPack = value.getPackageInfo().getPupPack();
        if (pupPack != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", pupPack.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(popperColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo media = value.getPackageInfo().getPopperMedia();
        if (media != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", media.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(povColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo pov = value.getPackageInfo().getPov();
        if (pov != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", pov.toString());
        }
      }
      return new Label("");
    }, this, true);


    BaseLoadingColumn.configureColumn(iniColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo ini = value.getPackageInfo().getIni();
        if (ini != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", ini.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(vbsColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo vbs = value.getPackageInfo().getVbs();
        if (vbs != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", vbs.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(resColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo res = value.getPackageInfo().getRes();
        if (res != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", res.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(musicColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo mus = value.getPackageInfo().getMusic();
        if (mus != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", mus.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(highscoreColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo highscore = value.getPackageInfo().getHighscore();
        if (highscore != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", highscore.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(dmdColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo dmd = value.getPackageInfo().getDmd();
        if (dmd != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", dmd.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(registryColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo reg = value.getPackageInfo().getMameData();
        if (reg != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", reg.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(altColorColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo altColor = value.getPackageInfo().getAltColor();
        if (altColor != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", altColor.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(romColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo rom = value.getPackageInfo().getRom();
        if (rom != null) {
          return WidgetFactory.createCheckboxIcon("#FFFFFF", rom.toString());
        }
      }
      return new Label("");
    }, this, true);

    BaseLoadingColumn.configureColumn(altSoundColumn, (value, model) -> {
      if (value.getPackageInfo() != null) {
        BackupFileInfo altSound = value.getPackageInfo().getAltSound();
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
      BackupSourceRepresentation archiveSource = sourceCombo.getValue();

      deleteBtn.setDisable(!archiveSource.getType().equals(BackupSourceType.Folder.name()) || newSelection == null);
      addArchiveBtn.setDisable(!archiveSource.getType().equals(BackupSourceType.Folder.name()));
      restoreBtn.setDisable(!archiveSource.getType().equals(BackupSourceType.Folder.name()) || newSelection == null);
      downloadBtn.setDisable(!archiveSource.getType().equals(BackupSourceType.Folder.name()) || tableView.getSelectionModel().getSelectedItems().size() == 0);
      openFolderButton.setDisable(newSelection == null);


      if (oldSelection == null || !oldSelection.equals(newSelection)) {
        updateSelection(newSelection != null ? Optional.of(newSelection.getBean()) : Optional.empty());
      }
    });

    tableView.setRowFactory(tv -> {
      TableRow<BackupModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {

        }
      });
      return row;
    });

    sourceComboChangeListener = (observable, oldValue, newValue) -> {
      addArchiveBtn.setDisable(!newValue.getType().equals(BackupSourceType.Folder.name()));
      restoreBtn.setDisable(!newValue.getType().equals(BackupSourceType.Folder.name()));
      downloadBtn.setDisable(!newValue.getType().equals(BackupSourceType.Folder.name()));

      BackupModel selectedItem = tableView.getSelectionModel().getSelectedItem();
      tableView.getSelectionModel().clearSelection();
      doReload(selectedItem != null ? Optional.of(selectedItem.getBean()) : Optional.empty());
    };
    refreshRepositoryCombo();

    deleteBtn.setDisable(true);
    restoreBtn.setDisable(true);
    downloadBtn.setDisable(true);
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME));
    if (options == null) {
      refreshView(Optional.empty());
    }
    else {
      refreshView(Optional.of((BackupDescriptorRepresentation) options.getModel()));
    }
    EventManager.getInstance().removeListener(this);
    EventManager.getInstance().addListener(this);
  }

  public void refreshView(Optional<BackupDescriptorRepresentation> selection) {
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

  private void updateSelection(Optional<BackupDescriptorRepresentation> newSelection) {
    NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME));
    if (newSelection.isPresent()) {
      BackupDescriptorRepresentation descriptorRepresentation = newSelection.get();
      NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME, descriptorRepresentation.getFilename()));
    }
    tablesController.getRepositorySideBarController().setArchiveDescriptor(newSelection);
  }

  private List<BackupDescriptorRepresentation> filterArchives(List<BackupDescriptorRepresentation> archives) {
    List<BackupDescriptorRepresentation> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    for (BackupDescriptorRepresentation archive : archives) {
      if (archive.getFilename() != null) {
        String filename = archive.getFilename().toLowerCase();
        if (systemSummary.getBackupType().equals(BackupType.VPA) && !filename.endsWith("." + BackupType.VPA.name().toLowerCase())) {
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
    if (jobType.equals(JobType.TABLE_BACKUP)) {
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
    List<BackupSourceRepresentation> repositories = new ArrayList<>(client.getArchiveService().getBackupSources());
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

  public void selectBackup(BackupDescriptorRepresentation backup) {
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
  protected BackupModel toModel(BackupDescriptorRepresentation backupDescriptorRepresentation) {
    return new BackupModel(backupDescriptorRepresentation);
  }
}