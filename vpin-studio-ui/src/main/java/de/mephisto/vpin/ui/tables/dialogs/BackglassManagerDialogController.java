package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.models.B2SGlowing;
import de.mephisto.vpin.ui.tables.models.B2SLedType;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingTableCell;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.annotation.Nullable;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class BackglassManagerDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassManagerDialogController.class);

  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 100;

  @FXML
  private BorderPane root;

  @FXML
  private Label nameLabel;

  @FXML
  private Label typeLabel;

  @FXML
  private Label authorLabel;

  @FXML
  private Label artworkLabel;

  @FXML
  private Label grillLabel;

  @FXML
  private Label b2sElementsLabel;

  @FXML
  private Label scoresLabel;

  @FXML
  private Label playersLabel;

  @FXML
  private Label bulbsLabel;

  @FXML
  private Label filesizeLabel;

  @FXML
  private Label resolutionLabel;

  @FXML
  private Label dmdResolutionLabel;

  @FXML
  private Label fullDmdLabel;

  @FXML
  private Label modificationDateLabel;

  @FXML
  private ImageView thumbnailImage;

  @FXML
  private ImageView dmdThumbnailImage;

  @FXML
  private Button downloadBackglassBtn;

  @FXML
  private Button downloadDMDBtn;

  //-- Editors

  @FXML
  private ComboBox<B2SVisibility> hideGrill;

  @FXML
  private CheckBox hideB2SDMD;

  @FXML
  private CheckBox hideB2SBackglass;

  @FXML
  private ComboBox<B2SVisibility> hideDMD;

  @FXML
  private Spinner<Integer> skipLampFrames;

  @FXML
  private Spinner<Integer> skipGIFrames;

  @FXML
  private Spinner<Integer> skipSolenoidFrames;

  @FXML
  private Spinner<Integer> skipLEDFrames;

  @FXML
  private CheckBox lightBulbOn;

  @FXML
  private CheckBox startAsExe;

  @FXML
  private CheckBox startAsExeServer;

  @FXML
  private ComboBox<B2SGlowing> glowing;

  @FXML
  private ComboBox<B2SLedType> usedLEDType;

  @FXML
  private CheckBox startBackground;

  @FXML
  private CheckBox bringBGFromTop;

  @FXML
  private Button renameBtn;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button duplicateBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button reloadBackglassBtn;

  @FXML
  private Label gameLabel;

  @FXML
  private Label gameFilenameLabel;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private StackPane tableStack;

  @FXML
  private TableView<DirectB2SEntryModel> directb2sList;

  @FXML
  private TableColumn<DirectB2SEntryModel, Object> statusColumn;

  @FXML
  private TableColumn<DirectB2SEntryModel, Object> displayNameColumn;

  @FXML
  private TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> fullDmdColumn;

  @FXML
  private TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> grillColumn;

  @FXML
  private TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> scoreColumn;

  @FXML
  private ScrollPane main;

  //--------------- Filters

  @FXML
  private Button filterButton;

  @FXML
  private TextField searchField;

  ObservableList<GameEmulatorRepresentation> selectedEmulators;

  Property<Boolean> missingDMDImageFilter = new SimpleBooleanProperty(false);
  Property<Boolean> notFullDMDRatioFilter = new SimpleBooleanProperty(false);
  Property<Boolean> scoresAvailableFilter = new SimpleBooleanProperty(false);
  Property<Boolean> missingTableFilter = new SimpleBooleanProperty(false);

  Property<B2SVisibility> grillVisibilityFilter = new SimpleObjectProperty<B2SVisibility>();
  Property<Boolean> b2sdmdVisibilityFilter = new SimpleBooleanProperty(false);
  Property<Boolean> backglassVisibilityFilter = new SimpleBooleanProperty(false);
  Property<B2SVisibility> dmdVisibilityFilter = new SimpleObjectProperty<B2SVisibility>();

  /**
   * dedicated controller for backglass filtering
   */
  private BackglassManagerFilterController backglassFilterController;

//-------------

  private DirectB2SData tableData;
  private DirectB2STableSettings tableSettings;
  private boolean saveEnabled;

  private TablesSidebarController tablesSidebarController;
  private List<DirectB2SEntryModel> unfilteredBackglasses;
  private GameRepresentation game;
  private DirectB2ServerSettings serverSettings;

  @FXML
  private void onUpload(ActionEvent e) {
    if (game != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      TableDialogs.directBackglassUpload(stage, game);
      // when done, force refresh
      refreshBackglass();
    }
  }

  @FXML
  private void onBackglassReload(ActionEvent e) {
    refreshBackglass();
  }

  public void refreshBackglass() {
    try {
      DirectB2SEntryModel selectedItem = directb2sList.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        selectedItem.reload();
        refresh(selectedItem.getBacklass());
      }
    }
    catch (Exception ex) {
      LOG.error("Refreshing backglass failed: " + ex.getMessage(), ex);
      WidgetFactory.showAlert(stage, "Error", "Refreshing backglass failed: " + ex.getMessage());
    }
  }

  @FXML
  private void onBackglassDownload() {
    if (tableData.isBackgroundAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
        export(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download background image for game " + tableData.getGameId(), ioe);
      }
    }
  }

  @FXML
  private void onDMDDownload() {
    if (tableData.isDmdImageAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(tableData)) {
        export(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download DMD image for game " + tableData.getGameId(), ioe);
      }
    }
  }

  private void export(InputStream in) {
    if (in != null) {
      StudioFolderChooser chooser = new StudioFolderChooser();
      chooser.setTitle("Select Target Folder");
      File targetFolder = chooser.showOpenDialog(stage);

      if (targetFolder != null) {
        try {
          File targetFile = new File(targetFolder, this.tableData.getName() + ".png");
          targetFile = FileUtils.uniqueFile(targetFile);
          FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
          IOUtils.copy(in, fileOutputStream);
          fileOutputStream.close();

          WidgetFactory.showInformation(stage, "Export Finished", "Written \"" + targetFile.getName() + "\".");
        }
        catch (IOException e) {
          LOG.error("Failed to download backglass image: " + e.getMessage(), e);
          WidgetFactory.showAlert(stage, "Error", "Failed to download backglass image: " + e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onTableDataManager(ActionEvent e) {
    if (game != null) {
      onCancel(e);
      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(tablesSidebarController.getTableOverviewController(), this.game);
      });
    }
  }

  @FXML
  private void onRename(ActionEvent e) {
    DirectB2SEntryModel selectedItem = directb2sList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      String newName = WidgetFactory.showInputDialog(stage, "Rename Backglass", "Enter new name for backglass file \"" + selectedItem.getFileName() + "\"", null, null, selectedItem.getName());
      if (newName != null) {
        if (!FileUtils.isValidFilename(newName)) {
          WidgetFactory.showAlert(stage, "Invalid Filename", "The specified file name contains invalid characters.");
          return;
        }

        try {
          if (!newName.endsWith(".directb2s")) {
            newName = newName + ".directb2s";
          }
          client.getBackglassServiceClient().renameBackglass(selectedItem.getBacklass(), newName);
        }
        catch (Exception ex) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onDuplicate(ActionEvent e) {
    DirectB2SEntryModel selectedItem = directb2sList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Duplicate Backglass", "Duplicate backglass file \"" + selectedItem.getFileName() + "\"?", null, "Duplicate");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getBackglassServiceClient().duplicateBackglass(selectedItem.getBacklass());
        }
        catch (Exception ex) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    try {
      DirectB2SEntryModel selectedItem = directb2sList.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete Backglass", "Delete backglass file \"" + selectedItem.getFileName() + "\"?", null, "Delete");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          client.getBackglassServiceClient().deleteBackglass(selectedItem.getBacklass());
          if (game != null) {
            EventManager.getInstance().notifyTableChange(game.getId(), null);
          }
          directb2sList.getSelectionModel().clearSelection();
          onReload();
        }
      }
    }
    catch (Exception ex) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to delete backglass file: " + ex.getMessage());
    }
  }

  @FXML
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onReload() {
    client.getBackglassServiceClient().clearCache();
    setSaveEnabled(false);
    unfilteredBackglasses = toModels(client.getBackglassServiceClient().getBackglasses());
    applyFilter();
    setSaveEnabled(true);
  }

  @FXML
  private void onFilter() {
    backglassFilterController.toggle();
  }

  public void applyFilter() {
    DirectB2SEntryModel selectedItem = directb2sList.getSelectionModel().getSelectedItem();
    List<DirectB2SEntryModel> filtered = filterEntries(unfilteredBackglasses);
    directb2sList.setItems(FXCollections.observableList(filtered));

    if (selectedItem != null && filtered.contains(selectedItem)) {
      directb2sList.getSelectionModel().select(selectedItem);
    }
    else {
      directb2sList.getSelectionModel().select(0);
    }
  }

  private List<DirectB2SEntryModel> toModels(List<DirectB2S> backglasses) {
    List<DirectB2SEntryModel> models = new ArrayList<>(backglasses.size());
    for (DirectB2S b2s : backglasses) {
      models.add(new DirectB2SEntryModel(this, b2s));
    }
    return models;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getFrontendService().getBackglassGameEmulators();
    if (gameEmulators.isEmpty()) {
      LOG.error("No backglass server game emulator found!");
    }
    else {
      serverSettings = client.getBackglassServiceClient().getServerSettings(gameEmulators.get(0).getId());
    }


    root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    this.dataManagerBtn.setDisable(true);
    this.renameBtn.setDisable(true);
    this.uploadBtn.setDisable(true);
    this.duplicateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.reloadBackglassBtn.setDisable(true);

    bindTable();

    bindToolbarAndSearch();

    hideGrill.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    hideGrill.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setHideGrill(t1.getId());
      save();
    });

    hideB2SDMD.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setHideB2SDMD(newValue);
      save();
    });

    hideB2SBackglass.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setHideB2SBackglass(newValue);
      save();
    });

    hideDMD.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    hideDMD.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setHideDMD(t1.getId());
      save();
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLampFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("skipLampFrames", () -> {
        if (tableSettings == null) {
          return;
        }
        tableSettings.setLampsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipGIFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("skipGIFrames", () -> {
        if (tableSettings == null) {
          return;
        }
        tableSettings.setGiStringsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipSolenoidFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (tableSettings == null) {
        return;
      }
      debouncer.debounce("skipSolenoidFrames", () -> {
        tableSettings.setSolenoidsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLEDFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (tableSettings == null) {
        return;
      }

      debouncer.debounce("skipLEDFrames", () -> {
        tableSettings.setLedsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });


    glowing.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.GLOWINGS));
    glowing.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setGlowIndex(t1.getId());
      save();
    });

    startAsExe.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      if (newValue) {
        tableSettings.setStartAsEXE(newValue);
      }
      else {
        tableSettings.setStartAsEXE(null);
      }
      save();
    });

    lightBulbOn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setGlowBulbOn(newValue);
      save();
    });

    usedLEDType.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.LED_TYPES));
    usedLEDType.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setUsedLEDType(t1.getId());
      glowing.setDisable(t1.getId() == 1);
      lightBulbOn.setDisable(t1.getId() == 1);
      lightBulbOn.setSelected(false);
      save();
    });

    startBackground.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setStartBackground(newValue);
      save();
    });

    bringBGFromTop.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setFormToFront(newValue);
      save();
    });

    new Thread(() -> {
      this.unfilteredBackglasses = toModels(client.getBackglassServiceClient().getBackglasses());
      Platform.runLater(() -> {
        this.directb2sList.setItems(FXCollections.observableList(unfilteredBackglasses));

      });
    }).start();

    this.directb2sList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      refresh(null);

      Platform.runLater(() -> {
        if (newValue != null) {
          refresh(newValue.getBacklass());
        }
      });
    });

    // add the overlay for drag and drop
    new BackglassManagerDragDropHandler(this, directb2sList, tableStack);

    if (this.directb2sList.getItems().isEmpty()) {
      this.directb2sList.getSelectionModel().clearSelection();
    }
    else {
      this.directb2sList.getSelectionModel().select(0);
    }
  }

  private void bindToolbarAndSearch() {

    // get first the emulators
    this.selectedEmulators = FXCollections.observableList(client.getFrontendService().getVpxGameEmulators());

    try {
      FXMLLoader loader = new FXMLLoader(BackglassManagerFilterController.class.getResource("dialog-directb2s-admin-filter.fxml"));
      loader.load();
      backglassFilterController = loader.getController();
      backglassFilterController.setTableController(this, filterButton, tableStack, directb2sList);

    }
    catch (IOException e) {
      LOG.error("Failed to load filters: " + e.getMessage(), e);
    }

    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      applyFilter();
    });
  }

  private void bindTable() {
    statusColumn.setCellValueFactory(cellData -> {
      DirectB2SEntryModel backglass = cellData.getValue();
      if (!backglass.isVpxAvailable()) {
        Label icon = new Label();
        icon.setTooltip(new Tooltip("The backglass file \"" + backglass.getName() + "\n has no matching VPX file."));
        icon.setGraphic(WidgetFactory.createExclamationIcon());
        return new SimpleObjectProperty<Object>(icon);
      }
      // else
      return new SimpleObjectProperty<Object>(WidgetFactory.createCheckIcon());
    });

    displayNameColumn.setCellValueFactory(cellData -> {
      DirectB2SEntryModel backglass = cellData.getValue();
      Label label = new Label(backglass.getName());
      label.getStyleClass().add("default-text");
      return new SimpleObjectProperty<Object>(label);
    });

    fullDmdColumn.setCellValueFactory(cellData -> {
      return cellData.getValue();
    });
    fullDmdColumn.setCellFactory(cellData -> {
      return new LoadingCheckTableCell() {
        @Override
        protected String getLoading(DirectB2SEntryModel model) {
          return "loading...";
        }

        @Override
        protected int isChecked(DirectB2SEntryModel model) {
          return model.hasDmd ? (isFullDmd(model.dmdWidth, model.dmdHeight) ? 1 : 2) : 0;
        }

        @Override
        protected String getTooltip(DirectB2SEntryModel model) {
          return (isFullDmd(model.dmdWidth, model.dmdHeight) ?
              "Full DMD backglass" : "DMD backglass present, but not Full-DMD Aspect Ratio")
              + ", resolution " + model.dmdWidth + "x" + model.dmdHeight;
        }
      };
    });

    grillColumn.setCellValueFactory(cellData -> {
      return cellData.getValue();
    });
    grillColumn.setCellFactory(cellData -> {
      return new LoadingCheckTableCell() {
        @Override
        protected String getLoading(DirectB2SEntryModel model) {
          return "";
        }

        @Override
        protected int isChecked(DirectB2SEntryModel model) {
          return model.grillHeight > 0 ? 1 : 0;
        }

        @Override
        protected String getTooltip(DirectB2SEntryModel model) {
          return "Grill Height set to " + model.grillHeight;
        }
      };
    });

    scoreColumn.setCellValueFactory(cellData -> {
      return cellData.getValue();
    });
    scoreColumn.setCellFactory(cellData -> {
      return new LoadingCheckTableCell() {
        @Override
        protected String getLoading(DirectB2SEntryModel model) {
          return "";
        }

        @Override
        protected int isChecked(DirectB2SEntryModel model) {
          return model.nbScores > 0 ? 1 : 0;
        }

        @Override
        protected String getTooltip(DirectB2SEntryModel model) {
          return model.nbScores > 0 ? "Backglass contains " + model.nbScores + " scores" : "";
        }
      };
    });

  }

  private boolean isFullDmd(double imageWidth, double imageHeight) {
    double ratio = imageWidth / imageHeight;
    return ratio < 3.0;
  }

  private List<DirectB2SEntryModel> filterEntries(List<DirectB2SEntryModel> backglasses) {
    List<Integer> emuIds = new ArrayList<>();
    for (GameEmulatorRepresentation emulatorRepresentation : selectedEmulators) {
      emuIds.add(emulatorRepresentation.getId());
    }

    List<DirectB2SEntryModel> filtered = new ArrayList<>();
    for (DirectB2SEntryModel backglass : backglasses) {
      if (!emuIds.contains(backglass.getEmulatorId())) {
        continue;
      }

      if (!StringUtils.isEmpty(searchField.getText().trim()) && !StringUtils.containsIgnoreCase(backglass.getName(), searchField.getText())) {
        continue;
      }

      // a non loaded backglass is not filtered, but loading is launched
      if (backglass.isLoaded() && !backglass.match()) {
        continue;
      }

      filtered.add(backglass);
    }

    return filtered;
  }

  @Override
  public void onDialogCancel() {
  }

  private void refresh(@Nullable DirectB2S newValue) {
    setSaveEnabled(false);
    this.dataManagerBtn.setDisable(true);

    this.renameBtn.setDisable(newValue == null);
    this.uploadBtn.setDisable(newValue == null);
    this.duplicateBtn.setDisable(newValue == null);
    this.deleteBtn.setDisable(newValue == null);
    this.reloadBackglassBtn.setDisable(newValue == null);

    this.tableSettings = null;

    nameLabel.setText("-");
    typeLabel.setText("-");
    authorLabel.setText("-");
    artworkLabel.setText("-");
    b2sElementsLabel.setText("-");
    scoresLabel.setText("-");
    grillLabel.setText("-");
    playersLabel.setText("-");
    bulbsLabel.setText("-");
    filesizeLabel.setText("-");
    modificationDateLabel.setText("-");
    thumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    dmdThumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    downloadBackglassBtn.setDisable(true);
    downloadDMDBtn.setDisable(true);
    resolutionLabel.setText("");
    dmdResolutionLabel.setText("");
    fullDmdLabel.setText("");
    gameLabel.setText("-");
    gameFilenameLabel.setText("-");

    glowing.setDisable(newValue == null);
    startAsExe.setDisable(newValue == null);
    dataManagerBtn.setDisable(newValue == null);
    hideB2SDMD.setDisable(newValue == null);
    hideB2SBackglass.setDisable(newValue == null);
    hideGrill.setDisable(newValue == null);
    hideDMD.setDisable(newValue == null);
    startBackground.setDisable(newValue == null);
    bringBGFromTop.setDisable(newValue == null);
    skipGIFrames.setDisable(newValue == null);
    skipLampFrames.setDisable(newValue == null);
    skipSolenoidFrames.setDisable(newValue == null);
    skipLEDFrames.setDisable(newValue == null);
    bulbsLabel.setDisable(newValue == null);
    usedLEDType.setDisable(newValue == null);

    game = null;

    if (newValue != null) {
      DirectB2STableSettings tmpTableSettings;
      try {
        this.tableData = client.getBackglassServiceClient().getDirectB2SData(newValue);
      }
      catch (Exception e) {
        this.tableData = new DirectB2SData();
      }

      if (this.tableData.getGameId() > 0) {
        tmpTableSettings = client.getBackglassServiceClient().getTableSettings(this.tableData.getGameId());
        game = client.getGame(this.tableData.getGameId());
        gameLabel.setText(game.getGameDisplayName());
        gameFilenameLabel.setText(game.getGameFileName());
        dataManagerBtn.setDisable(false);
        modificationDateLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tableData.getModificationDate()));
      }
      else {
        //VPX is not installed, but available!
        if (newValue.isVpxAvailable()) {
          gameLabel.setText("?");
          gameFilenameLabel.setText("(Available, but not installed)");
        }
        tmpTableSettings = null;
      }

      nameLabel.setText(tableData.getName());
      typeLabel.setText(DirectB2SData.getTableType(tableData.getTableType()));
      authorLabel.setText(tableData.getAuthor());
      artworkLabel.setText(tableData.getArtwork());
      grillLabel.setText(String.valueOf(tableData.getGrillHeight()));
      b2sElementsLabel.setText(String.valueOf(tableData.getB2sElements()));
      scoresLabel.setText(String.valueOf(tableData.getScores()));
      playersLabel.setText(String.valueOf(tableData.getNumberOfPlayers()));
      filesizeLabel.setText(FileUtils.readableFileSize(tableData.getFilesize()));
      bulbsLabel.setText(String.valueOf(tableData.getIlluminations()));

      hideGrill.setDisable(tableData.getGrillHeight() == 0);

      if (tableData.isBackgroundAvailable()) {
        try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
          Image image = new Image(in);
          if (tableData.getGrillHeight() > 0 && tmpTableSettings != null && tmpTableSettings.getHideGrill() == 1) {
            PixelReader reader = image.getPixelReader();
            image = new WritableImage(reader, 0, 0, (int) image.getWidth(), (int) (image.getHeight() - tableData.getGrillHeight()));
          }
          thumbnailImage.setImage(image);
          downloadBackglassBtn.setDisable(false);
          resolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
        }
        catch (IOException ioe) {
          LOG.error("Cannot download background image for game " + tableData.getGameId(), ioe);
        }
      }
      else {
        thumbnailImage.setImage(null);
        resolutionLabel.setText("Failed to read image data.");
      }

      if (tableData.isDmdImageAvailable()) {
        try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(tableData)) {
          Image image = new Image(in);
          dmdThumbnailImage.setImage(image);
          downloadDMDBtn.setDisable(false);
          dmdResolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
          fullDmdLabel.setText(isFullDmd(image.getWidth(), image.getHeight()) ? "Yes" : "No");
        }
        catch (IOException ioe) {
          LOG.error("Cannot download DMD image for game " + tableData.getGameId(), ioe);
        }
      }
      else {
        dmdResolutionLabel.setText("No DMD background available.");
        fullDmdLabel.setText("No");
      }

      hideGrill.setDisable(tmpTableSettings == null);
      hideB2SDMD.setSelected(false);
      hideB2SDMD.setDisable(tmpTableSettings == null);
      hideB2SBackglass.setSelected(false);
      hideB2SBackglass.setDisable(tmpTableSettings == null);
      hideDMD.setDisable(tmpTableSettings == null);
      skipLampFrames.getValueFactory().setValue(0);
      skipLampFrames.setDisable(tmpTableSettings == null || tableData.getIlluminations() == 0);
      skipGIFrames.getValueFactory().setValue(0);
      skipGIFrames.setDisable(tmpTableSettings == null || tableData.getIlluminations() == 0);
      skipSolenoidFrames.getValueFactory().setValue(0);
      skipSolenoidFrames.setDisable(tmpTableSettings == null || tableData.getIlluminations() == 0);
      skipLEDFrames.getValueFactory().setValue(0);
      skipLEDFrames.setDisable(tmpTableSettings == null || tableData.getIlluminations() == 0 || usedLEDType.getValue() == null || usedLEDType.getValue().getId() == 2);
      lightBulbOn.setSelected(false);
      lightBulbOn.setDisable(tmpTableSettings == null || (usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2));
      glowing.setDisable(tmpTableSettings == null || (usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2));
      usedLEDType.setDisable(tmpTableSettings == null);
      startBackground.setSelected(false);
      startBackground.setDisable(tmpTableSettings == null);
      bringBGFromTop.setSelected(false);
      bringBGFromTop.setDisable(tmpTableSettings == null);

      if (tmpTableSettings != null) {
        boolean serverLaunchAsExe = serverSettings != null && serverSettings.getDefaultStartMode() == DirectB2ServerSettings.EXE_START_MODE;
        boolean tableLaunchAsExe = tmpTableSettings.getStartAsEXE() != null && tmpTableSettings.getStartAsEXE();
        startAsExe.setSelected(tableLaunchAsExe);
        startAsExeServer.setSelected(serverLaunchAsExe);
        startAsExeServer.setDisable(true);

        hideGrill.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tmpTableSettings.getHideGrill()).findFirst().orElse(null));
        hideB2SDMD.selectedProperty().setValue(tmpTableSettings.isHideB2SDMD());
        hideB2SBackglass.selectedProperty().setValue(tmpTableSettings.isHideB2SBackglass());
        hideDMD.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tmpTableSettings.getHideDMD()).findFirst().orElse(null));
        skipLampFrames.getValueFactory().valueProperty().set(tmpTableSettings.getLampsSkipFrames());
        skipGIFrames.getValueFactory().valueProperty().set(tmpTableSettings.getGiStringsSkipFrames());
        skipSolenoidFrames.getValueFactory().valueProperty().set(tmpTableSettings.getSolenoidsSkipFrames());
        skipLEDFrames.getValueFactory().valueProperty().set(tmpTableSettings.getLedsSkipFrames());
        lightBulbOn.selectedProperty().setValue(tmpTableSettings.isGlowBulbOn());
        glowing.setValue(TablesSidebarDirectB2SController.GLOWINGS.stream().filter(v -> v.getId() == tmpTableSettings.getGlowIndex()).findFirst().get());
        usedLEDType.setValue(TablesSidebarDirectB2SController.LED_TYPES.stream().filter(v -> v.getId() == tmpTableSettings.getUsedLEDType()).findFirst().orElse(null));
        startBackground.selectedProperty().setValue(tmpTableSettings.isStartBackground());
        bringBGFromTop.selectedProperty().setValue(tmpTableSettings.isFormToFront());
      }

      this.tableSettings = tmpTableSettings;

      setSaveEnabled(true);
    }
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  public void selectGame(@Nullable GameRepresentation game) {
    // try to select first backglass of the game, do the selection by name
    if (game != null) {
      String gameBaseName = FilenameUtils.getBaseName(game.getGameFileName());

      // at calling time, the list may not have been populated so register a listener in that case
      if (unfilteredBackglasses != null) {
        selectGame(gameBaseName);
      }
      else {
        ChangeListener<ObservableList<DirectB2SEntryModel>> listener = new ChangeListener<ObservableList<DirectB2SEntryModel>>() {
          @Override
          public void changed(ObservableValue<? extends ObservableList<DirectB2SEntryModel>> observable,
              ObservableList<DirectB2SEntryModel> oldValue, ObservableList<DirectB2SEntryModel> newValue) {
            selectGame(gameBaseName);
            directb2sList.itemsProperty().removeListener(this);
          }
        };
        this.directb2sList.itemsProperty().addListener(listener);
      }
    }
  }
  private void selectGame(String gameBaseName) {
    for (DirectB2SEntryModel backglass : unfilteredBackglasses) {
      if (StringUtils.startsWithIgnoreCase(backglass.getFileName(), gameBaseName)) {
        directb2sList.scrollTo(backglass);
        directb2sList.getSelectionModel().select(backglass);
        break;
      }
    }
  }

  private void save() {
    if (!saveEnabled) {
      return;
    }

    if (this.game != null) {
      try {
        if (this.saveEnabled) {
          client.getBackglassServiceClient().saveTableSettings(game.getId(), this.tableSettings);
          Platform.runLater(() -> {
            this.refresh(this.directb2sList.getSelectionModel().getSelectedItem().getBacklass());
            EventManager.getInstance().notifyTableChange(game.getId(), null);
          });
        }
      }
      catch (Exception e) {
        LOG.error("Failed to save B2STableSettings.xml: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save B2STableSettings.xml: " + e.getMessage());
      }
    }
  }

  private void setSaveEnabled(boolean b) {
    if (b) {
      try {
        Thread.sleep(DEBOUNCE_MS + 100);
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    this.saveEnabled = b;
  }

  public GameRepresentation getGame() {
    return game;  
  }

  //------------------------------------------------


  private static class DirectB2SEntryModel extends BaseLoadingModel<DirectB2S, DirectB2SEntryModel> {

    private BackglassManagerDialogController controller;

    // not null when loaded
    DirectB2SData backglassData;

    private boolean hasDmd;

    private int dmdWidth;
    private int dmdHeight;
    private int grillHeight;
    private int nbScores;

    private int hideGrill;
    private boolean hideB2SDMD;
    private boolean hideBackglass;
    private int hideDMD;

    private DirectB2SEntryModel(BackglassManagerDialogController controller, DirectB2S backglass) {
      super(backglass);
      this.controller = controller;
    }

    @Override
    public void load() {
      this.backglassData = client.getBackglassServiceClient().getDirectB2SData(bean);
      if (backglassData != null) {

        this.grillHeight = backglassData.getGrillHeight();

        if (backglassData.isDmdImageAvailable()) {
          try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(backglassData)) {
            Image image = new Image(in);
            this.hasDmd = true;
            this.dmdWidth = (int) image.getWidth();
            this.dmdHeight = (int) image.getHeight();
          }
          catch (IOException ioe) {
            LOG.error("Cannot download DMD image for game " + backglassData.getGameId(), ioe);
          }
        } else {
          this.hasDmd = false;
        }

        this.nbScores = backglassData.getScores();

        DirectB2STableSettings tmpTableSettings = null;
        if (backglassData.getGameId() > 0) {
          tmpTableSettings = client.getBackglassServiceClient().getTableSettings(backglassData.getGameId());
          if (tmpTableSettings != null) {
            this.hideGrill = tmpTableSettings.getHideGrill();
            this.hideB2SDMD = tmpTableSettings.isHideB2SDMD();
            this.hideBackglass = tmpTableSettings.isHideB2SBackglass();
            this.hideDMD = tmpTableSettings.getHideDMD();
          }
        }
      }
    }

    @Override
    public void loaded() {
      if (!match()) {
        // self removal on load in case item is filtered
        controller.directb2sList.getItems().remove(this);
      }
    }

    public DirectB2S getBacklass() {
      return getBean();
    }

    @Override
    public String getName() {
      return bean.getName();
    }

    public int getEmulatorId() {
      return bean.getEmulatorId();
    }

    public String getFileName() {
      return bean.getFileName();
    }

    public boolean isVpxAvailable() {
      return bean.isVpxAvailable();
    }

    /**
     * For non nullable filter, table is matched. Else filter must be met
     *
     * @return
     */
    public boolean match() {
      if (controller.missingDMDImageFilter.getValue() && hasDmd) {
        return false;
      }
      if (controller.notFullDMDRatioFilter.getValue() && (!hasDmd || controller.isFullDmd(dmdWidth, dmdHeight))) {
        return false;
      }
      if (controller.scoresAvailableFilter.getValue() && nbScores <= 0) {
        return false;
      }
      if (controller.missingTableFilter.getValue() && bean.isVpxAvailable()) {
        return false;
      }
      if (equalsVisibility(controller.grillVisibilityFilter.getValue(), this.hideGrill)) {
        return false;
      }
      if (controller.backglassVisibilityFilter.getValue() && !this.hideBackglass) {
        return false;
      }
      if (controller.b2sdmdVisibilityFilter.getValue() && !this.hideB2SDMD) {
        return false;
      }
      if (equalsVisibility(controller.dmdVisibilityFilter.getValue(), this.hideDMD)) {
        return false;
      }

      return true;
    }

    private boolean equalsVisibility(B2SVisibility value, int hide) {
      return value != null && value.getId() >= 0 && value.getId() != hide;
    }

  }

  private static abstract class LoadingCheckTableCell extends BaseLoadingTableCell<DirectB2SEntryModel> {

    /**
     * should return true if the checked mark is visible. Model is never null
     */
    protected abstract int isChecked(DirectB2SEntryModel model);

    /**
     * should return a contextualized tooltip for given model. Model is never null
     */
    protected abstract String getTooltip(DirectB2SEntryModel model);

    @Override
    protected void renderItem(DirectB2SEntryModel model) {
      int check = isChecked(model);
      if (check == 1) {
        setText(null);
        setTooltip(new Tooltip(getTooltip(model)));
        setGraphic(WidgetFactory.createCheckboxIcon());
      }
      else if (check == 2) {
        setText(null);
        setTooltip(new Tooltip(getTooltip(model)));
        setGraphic(WidgetFactory.createExclamationIcon());
      }
      else {
        setText("");
        setTooltip(null);
        setGraphic(null);
      }
    }
  }

}
