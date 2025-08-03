package de.mephisto.vpin.ui.backups;

import de.mephisto.vpin.commons.BackupSourceType;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.restclient.backups.BackupFileInfo;
import de.mephisto.vpin.restclient.backups.BackupSourceRepresentation;
import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.SystemUtil;
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

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class BackupsController implements Initializable, StudioFXController, StudioEventListener {
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
  private Button reloadBtn;

  @FXML
  private TextField searchTextField;

  @FXML
  private ComboBox<BackupSourceRepresentation> sourceCombo;

  @FXML
  private TableView<BackupDescriptorRepresentation> tableView;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> iconColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> nameColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> directB2SColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> pupPackColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> romColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> popperColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> povColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> iniColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> vbsColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> resColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> musicColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> highscoreColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> dmdColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> registryColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> altSoundColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> altColorColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> sizeColumn;

  @FXML
  private TableColumn<BackupDescriptorRepresentation, String> createdAtColumn;

  @FXML
  private StackPane tableStack;

  @FXML
  private Separator endSeparator;

  @FXML
  private Button clearBtn;

  @FXML
  private ToolBar toolbar;

  private Parent loadingOverlay;


  private ObservableList<BackupDescriptorRepresentation> data;
  private List<BackupDescriptorRepresentation> archives;
  private TablesController tablesController;
  private ChangeListener<BackupSourceRepresentation> sourceComboChangeListener;
  private SystemSummary systemSummary;

  // Add a public no-args constructor
  public BackupsController() {
  }

  @FXML
  private void onClear() {
    searchTextField.setText("");
  }

  @FXML
  public final void onFolder() {
    ObservableList<BackupDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      BackupDescriptorRepresentation descriptor = selectedItems.get(0);
      BackupSourceRepresentation source = sourceCombo.getValue();

      File file = new File(source.getLocation(), descriptor.getFilename());
      if (file.exists()) {
        SystemUtil.openFile(file);
      }
    }
  }

  @FXML
  private void onRestore() {
    ObservableList<BackupDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      BackupDialogs.openArchiveRestoreDialog(selectedItems);
    }
  }

  @FXML
  private void onArchiveAdd() {
    boolean uploaded = BackupDialogs.openArchiveUploadDialog();
    if (uploaded) {
      doReload();
    }
  }

  @FXML
  private void onDownload() {
    ObservableList<BackupDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      BackupDialogs.openArchiveDownloadDialog(selectedItems);
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

    BackupSourceRepresentation selectedItem = sourceCombo.getSelectionModel().getSelectedItem();
    final BackupDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    tableView.getSelectionModel().clearSelection();
    boolean disable = selection == null;
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

      BackupSourceRepresentation value = sourceCombo.getValue();
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
    List<BackupDescriptorRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      String title = "Delete the " + selectedItems.size() + " selected archives?";
      if (selectedItems.size() == 1) {
        title = "Delete Archive \"" + selectedItems.get(0).getFilename() + "\"?";
      }
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title, null, null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          for (BackupDescriptorRepresentation selectedItem : selectedItems) {
            boolean b = client.getArchiveService().deleteBackup(selectedItem.getSource().getId(), selectedItem.getFilename());
            if (!b) {
              WidgetFactory.showAlert(stage, "Error", "Failed to delete \"" + selectedItem.getFilename() + "\"");
            }
          }
        }
        catch (Exception e) {
          WidgetFactory.showAlert(stage, "Error", "Error deleting archives: " + e.getMessage());
        }
        tableView.getSelectionModel().clearSelection();
        doReload();
        tablesController.getRepositorySideBarController().setArchiveDescriptor(Optional.empty());
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
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
      BackupDescriptorRepresentation value = cellData.getValue();
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
      BackupDescriptorRepresentation value = cellData.getValue();
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

      return new SimpleObjectProperty(vBox);
    });

    directB2SColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
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
          return new SimpleObjectProperty(iconLabel);
        }
      }
      return new SimpleStringProperty("");
    });

    pupPackColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo pupPack = value.getPackageInfo().getPupPack();
        if (pupPack != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", pupPack.toString()));
        }
      }

      return new SimpleStringProperty("");
    });

    popperColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo media = value.getPackageInfo().getPopperMedia();
        if (media != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", media.toString()));
        }
      }

      return new SimpleStringProperty("");
    });

    povColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo pov = value.getPackageInfo().getPov();
        if (pov != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", pov.toString()));
        }
      }
      return new SimpleStringProperty("");
    });

    iniColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo ini = value.getPackageInfo().getIni();
        if (ini != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", ini.toString()));
        }
      }
      return new SimpleStringProperty("");
    });

    vbsColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo vbs = value.getPackageInfo().getVbs();
        if (vbs != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", vbs.toString()));
        }
      }
      return new SimpleStringProperty("");
    });

    resColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo res = value.getPackageInfo().getRes();
        if (res != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", res.toString()));
        }
      }
      return new SimpleStringProperty("");
    });

    musicColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo mus = value.getPackageInfo().getMusic();
        if (mus != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", mus.toString()));
        }
      }
      return new SimpleStringProperty("");
    });


    highscoreColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo highscore = value.getPackageInfo().getHighscore();
        if (highscore != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", highscore.toString()));
        }
      }
      return new SimpleStringProperty("");
    });

    dmdColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo dmd = value.getPackageInfo().getDmd();
        if (dmd != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", dmd.toString()));
        }
      }
      return new SimpleStringProperty("");
    });

    registryColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo reg = value.getPackageInfo().getMameData();
        if (reg != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", reg.toString()));
        }
      }
      return new SimpleStringProperty("");
    });


    altColorColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo altColor = value.getPackageInfo().getAltColor();
        if (altColor != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", altColor.toString()));
        }
      }
      return new SimpleStringProperty("");
    });

    romColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo rom = value.getPackageInfo().getRom();
        if (rom != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", rom.toString()));
        }
      }
      return new SimpleStringProperty("");
    });

    altSoundColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getPackageInfo() != null) {
        BackupFileInfo altSound = value.getPackageInfo().getAltSound();
        if (altSound != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon("#FFFFFF", altSound.toString()));
        }
      }

      return new SimpleStringProperty("");
    });

    sizeColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      if (value.getSize() == 0) {
        return new SimpleStringProperty("-");
      }
      return new SimpleStringProperty(FileUtils.readableFileSize(value.getSize()));
    });

    createdAtColumn.setCellValueFactory(cellData -> {
      BackupDescriptorRepresentation value = cellData.getValue();
      return new SimpleStringProperty(DateFormat.getInstance().format(value.getCreatedAt()));
    });

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      BackupSourceRepresentation archiveSource = sourceCombo.getValue();

      deleteBtn.setDisable(!archiveSource.getType().equals(BackupSourceType.Folder.name()) || newSelection == null);
      addArchiveBtn.setDisable(!archiveSource.getType().equals(BackupSourceType.Folder.name()));
      restoreBtn.setDisable(!archiveSource.getType().equals(BackupSourceType.Folder.name()) || newSelection == null);
      downloadBtn.setDisable(!archiveSource.getType().equals(BackupSourceType.Folder.name()) || tableView.getSelectionModel().getSelectedItems().size() == 0);
      openFolderButton.setDisable(newSelection == null);


      if (oldSelection == null || !oldSelection.equals(newSelection)) {
        updateSelection(Optional.ofNullable(newSelection));
      }
    });

    tableView.setRowFactory(tv -> {
      TableRow<BackupDescriptorRepresentation> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {

        }
      });
      return row;
    });

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      clearBtn.setVisible(filterValue != null && filterValue.length() > 0);
      tableView.getSelectionModel().clearSelection();

      List<BackupDescriptorRepresentation> filtered = filterArchives(this.archives);
      tableView.setItems(FXCollections.observableList(filtered));
    });

    sourceComboChangeListener = (observable, oldValue, newValue) -> {
      addArchiveBtn.setDisable(!newValue.getType().equals(BackupSourceType.Folder.name()));
      restoreBtn.setDisable(!newValue.getType().equals(BackupSourceType.Folder.name()));
      downloadBtn.setDisable(!newValue.getType().equals(BackupSourceType.Folder.name()));

      tableView.getSelectionModel().clearSelection();
      doReload();
    };
    refreshRepositoryCombo();

    deleteBtn.setDisable(true);
    restoreBtn.setDisable(true);
    downloadBtn.setDisable(true);
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList(TAB_NAME));
    refreshView();
    EventManager.getInstance().addListener(this);
  }

  public void refreshView() {
    toolbar.getItems().stream().forEach(i -> i.setDisable(true));
    JFXFuture.supplyAsync(() -> {
      return client.getBackupService().authenticate();
    }).thenAcceptLater(authenticate -> {
      if (authenticate == null) {
        toolbar.getItems().stream().forEach(i -> i.setDisable(false));
        this.doReload();
      }
      else {
        WidgetFactory.showInformation(stage, "Authentication Required", authenticate);
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
        if (systemSummary.getArchiveType().equals(BackupType.VPA) && !filename.endsWith("." + BackupType.VPA.name().toLowerCase())) {
          continue;
        }

        if (filename.contains(filterValue.toLowerCase())) {
          filtered.add(archive);
        }
      }
    }
    return filtered;
  }

  public Optional<BackupDescriptorRepresentation> getSelection() {
    BackupDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    JobType jobType = event.getJobType();
    if (jobType.equals(JobType.TABLE_BACKUP)) {
      Platform.runLater(() -> {
        onReload();
        EventManager.getInstance().notifyTableChange(event.getGameId(), null);
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
    List<BackupSourceRepresentation> repositories = new ArrayList<>(client.getArchiveService().getArchiveSources());
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

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (preferenceType.equals(PreferenceType.backups)) {
      Platform.runLater(() -> {
        onReload();
      });
    }
  }
}