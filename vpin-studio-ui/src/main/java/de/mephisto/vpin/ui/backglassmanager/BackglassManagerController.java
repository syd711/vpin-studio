package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlay;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.models.B2SGlowing;
import de.mephisto.vpin.ui.tables.models.B2SLedType;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingTableCell;
import de.mephisto.vpin.ui.util.JFXFuture;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

/**
 * FIXME Rename at the end in BackglassManagerController
 */
public class BackglassManagerController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassManagerController.class);

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
  private BorderPane thumbnailImagePane;

  @FXML
  private ImageView thumbnailImage;

  @FXML
  private BorderPane dmdThumbnailImagePane;

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
  private StackPane loaderStack;
  @FXML
  private StackPane tableStack;

  @FXML
  private TableView<DirectB2SEntryModel> directb2sList;

  @FXML
  TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> statusColumn;

  @FXML
  TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> displayNameColumn;

  @FXML
  TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> fullDmdColumn;

  @FXML
  TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> grillColumn;

  @FXML
  TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> scoreColumn;

  @FXML
  private ScrollPane main;

  @FXML
  private Label labelBackglassCount;

  //--------------- Filters

  @FXML
  private Button filterButton;

  @FXML
  private TextField searchField;

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
  private boolean refreshing;

  private WaitOverlay loadingOverlay;

  private TablesSidebarController tablesSidebarController;
  
  private List<DirectB2S> backglasses;
  private ObservableList<DirectB2SEntryModel> models;
  private FilteredList<DirectB2SEntryModel> data;

  private BackglassManagerColumnSorter columnSorter;

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
      DirectB2SEntryModel selectedItem = getSelection();
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
      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(tablesSidebarController.getTableOverviewController(), this.game);
      });
    }
  }

  @FXML
  private void onRename(ActionEvent e) {
    DirectB2SEntryModel selectedItem = getSelection();
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
    DirectB2SEntryModel selectedItem = getSelection();
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
      DirectB2SEntryModel selectedItem = getSelection();
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
  private void onReload() {
    this.refreshing = true;
    client.getBackglassServiceClient().clearCache();

    doReload();
  }

  public void doReload() {
    loadingOverlay.setBusy("Loading Backglasses...", true);
    JFXFuture.runAsync(() -> {
      this.backglasses = client.getBackglassServiceClient().getBackglasses();
    }).thenLater(() -> {
      loadingOverlay.setBusy("", false);
      setItems();
    });
  }

  @FXML
  private void onFilter() {
    backglassFilterController.toggle();
  }

  public void applyFilter() {
    // mind that it can be called by threads before data is even initialized
    if (this.data != null) {
      this.data.setPredicate(backglassFilterController.buildPredicate());
    }
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

    //root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    this.dataManagerBtn.setDisable(true);
    this.renameBtn.setDisable(true);
    this.uploadBtn.setDisable(true);
    this.duplicateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.reloadBackglassBtn.setDisable(true);

    bindTable();

    try {
      FXMLLoader loader = new FXMLLoader(BackglassManagerFilterController.class.getResource("scene-directb2s-admin-filter.fxml"));
      loader.load();
      backglassFilterController = loader.getController();
      backglassFilterController.setTableController(this, filterButton, searchField, tableStack, directb2sList);
    }
    catch (IOException e) {
      LOG.error("Failed to load filters: " + e.getMessage(), e);
    }

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
    skipLampFrames.valueProperty().addListener((observableValue, integer, t1) -> {
      if (tableSettings == null) {
        return;
      }
      debounceAndSave("skipLampFrames", () -> tableSettings.setLampsSkipFrames(t1));
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipGIFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (tableSettings == null) {
        return;
      }
      debounceAndSave("skipGIFrames", () ->  tableSettings.setGiStringsSkipFrames(t1));
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipSolenoidFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (tableSettings == null) {
        return;
      }
      debounceAndSave("skipSolenoidFrames", () -> tableSettings.setSolenoidsSkipFrames(t1));
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLEDFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (tableSettings == null) {
        return;
      }
      debounceAndSave("skipLEDFrames", () -> tableSettings.setLedsSkipFrames(t1));
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
      tableSettings.setUsedLEDType(t1 != null ? t1.getId() : 0);
      glowing.setDisable(t1 != null ? t1.getId() == 1 : true);
      lightBulbOn.setDisable(t1 != null ? t1.getId() == 1 : true);
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

    // Install the handler for backglass selection
    this.directb2sList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      refresh(newValue != null ? newValue.getBacklass() : null);
    });

    this.columnSorter = new BackglassManagerColumnSorter(this);

    // add the loading overlay
    loadingOverlay = new WaitOverlay(loaderStack, directb2sList, null);

    // add the overlay for drag and drop
    new BackglassManagerDragDropHandler(this, directb2sList, tableStack);
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("Backglasses"));

    // first time activation 
    if (this.backglasses == null) {
      doReload();

      if (this.directb2sList.getItems().isEmpty()) {
        this.directb2sList.getSelectionModel().clearSelection();
      }
      else {
        this.directb2sList.getSelectionModel().select(0);
      }  
    }
    refreshBackglass();
  }

  private void bindTable() {

     BaseLoadingColumn.configureColumn(statusColumn, (value, model) -> {
      if (!model.isVpxAvailable()) {
        Label icon = new Label();
        icon.setTooltip(new Tooltip("The backglass file \"" + model.getName() + "\n has no matching VPX file."));
        icon.setGraphic(WidgetFactory.createExclamationIcon());
        return icon;
      }
      // else
      return WidgetFactory.createCheckIcon();
    }, true);

    BaseLoadingColumn.configureColumn(displayNameColumn, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      return label;
    }, true);

    BaseLoadingColumn.configureLoadingColumn(fullDmdColumn, cell -> new LoadingCheckTableCell() {
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
    });

    BaseLoadingColumn.configureLoadingColumn(grillColumn, cell -> new LoadingCheckTableCell() {
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
    });

    BaseLoadingColumn.configureLoadingColumn(scoreColumn, cell -> new LoadingCheckTableCell() {
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
    });
  }

  private void setItems() {

    this.models = FXCollections.observableArrayList();
    for (DirectB2S b2s : backglasses) {
      models.add(new DirectB2SEntryModel(b2s));
    }

    // Wrap games in a FilteredList
    this.data = new FilteredList<>(models);
    // When predicate change, update data count
    this.data.predicateProperty().addListener((o, oldP, newP) -> {
      labelBackglassCount.setText(data.size() + " backglasses");
    });

    // Wrap the FilteredList in a SortedList
    SortedList<DirectB2SEntryModel> sortedData = new SortedList<>(this.data);
    // Bind the SortedList comparator to the TableView comparator.
    sortedData.comparatorProperty().bind(Bindings.createObjectBinding(
        () -> columnSorter.buildComparator(directb2sList),
        directb2sList.comparatorProperty()));
    // Set a dummy SortPolicy to tell the TableView data is successfully sorted
    directb2sList.setSortPolicy(tableView -> true);

    // Set the items in the TableView
    directb2sList.setItems(sortedData);

    // filter the list and refresh number of items
    this.data.setPredicate(backglassFilterController.buildPredicate());
  }

  private static boolean isFullDmd(double imageWidth, double imageHeight) {
    double ratio = imageWidth / imageHeight;
    return ratio < 3.0;
  }

  private JFXFuture refresh(@Nullable DirectB2S newValue) {
  if (newValue != null) {
      NavigationController.setBreadCrumb(Arrays.asList("Backglasses", newValue.getName()));
    }
    else {
      NavigationController.setBreadCrumb(Arrays.asList("Backglasses"));
    }

    this.refreshing = true;
    this.dataManagerBtn.setDisable(true);

    this.renameBtn.setDisable(true);
    this.duplicateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.reloadBackglassBtn.setDisable(true);
    this.uploadBtn.setDisable(true);

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

    glowing.setDisable(true);
    startAsExe.setDisable(true);
    dataManagerBtn.setDisable(true);
    hideB2SDMD.setDisable(true);
    hideB2SBackglass.setDisable(true);
    hideGrill.setDisable(true);
    hideDMD.setDisable(true);
    startBackground.setDisable(true);
    bringBGFromTop.setDisable(true);
    skipGIFrames.setDisable(true);
    skipLampFrames.setDisable(true);
    skipSolenoidFrames.setDisable(true);
    skipLEDFrames.setDisable(true);
    bulbsLabel.setDisable(true);
    usedLEDType.setDisable(true);

    game = null;
    tableSettings = null;

    if (newValue != null) {

      thumbnailImagePane.setCenter(new ProgressIndicator());
      dmdThumbnailImagePane.setCenter(new ProgressIndicator());

      return JFXFuture.runAsync(() -> {
        try {
          this.tableData = client.getBackglassServiceClient().getDirectB2SData(newValue);
          if (this.tableData.getGameId() > 0) {
            this.game = client.getGame(this.tableData.getGameId());
            this.tableSettings = client.getBackglassServiceClient().getTableSettings(this.tableData.getGameId());
          }
        }
        catch (Exception e) {
          this.tableData = new DirectB2SData();
        }

        loadImages(tableSettings);

      }).thenLater(() -> {
        if (game != null) {
          gameLabel.setText(game.getGameDisplayName());
          gameFilenameLabel.setText(game.getGameFileName());
          dataManagerBtn.setDisable(false);
          this.uploadBtn.setDisable(false);
        }
        else {
          //VPX is not installed, but available!
          if (newValue.isVpxAvailable()) {
            gameLabel.setText("?");
            gameFilenameLabel.setText("(Available, but not installed)");
          }
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

        modificationDateLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tableData.getModificationDate()));

        if (tableSettings != null) {
          boolean serverLaunchAsExe = serverSettings != null && serverSettings.getDefaultStartMode() == DirectB2ServerSettings.EXE_START_MODE;
          boolean tableLaunchAsExe = tableSettings.getStartAsEXE() != null && tableSettings.getStartAsEXE();
          startAsExe.setSelected(tableLaunchAsExe);
          startAsExeServer.setSelected(serverLaunchAsExe);
          startAsExeServer.setDisable(true);

          hideGrill.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideGrill()).findFirst().orElse(null));
          hideB2SDMD.selectedProperty().setValue(tableSettings.isHideB2SDMD());
          hideB2SBackglass.selectedProperty().setValue(tableSettings.isHideB2SBackglass());
          hideDMD.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideDMD()).findFirst().orElse(null));
          skipLampFrames.getValueFactory().valueProperty().set(tableSettings.getLampsSkipFrames());
          skipGIFrames.getValueFactory().valueProperty().set(tableSettings.getGiStringsSkipFrames());
          skipSolenoidFrames.getValueFactory().valueProperty().set(tableSettings.getSolenoidsSkipFrames());
          skipLEDFrames.getValueFactory().valueProperty().set(tableSettings.getLedsSkipFrames());
          lightBulbOn.selectedProperty().setValue(tableSettings.isGlowBulbOn());
          glowing.setValue(TablesSidebarDirectB2SController.GLOWINGS.stream().filter(v -> v.getId() == tableSettings.getGlowIndex()).findFirst().get());
          usedLEDType.setValue(TablesSidebarDirectB2SController.LED_TYPES.stream().filter(v -> v.getId() == tableSettings.getUsedLEDType()).findFirst().orElse(null));
          startBackground.selectedProperty().setValue(tableSettings.isStartBackground());
          bringBGFromTop.selectedProperty().setValue(tableSettings.isFormToFront());
        }

        hideGrill.setDisable(tableSettings == null);
        hideB2SDMD.setSelected(false);
        hideB2SDMD.setDisable(tableSettings == null);
        hideB2SBackglass.setSelected(false);
        hideB2SBackglass.setDisable(tableSettings == null);
        hideDMD.setDisable(tableSettings == null);
        skipLampFrames.getValueFactory().setValue(0);
        skipLampFrames.setDisable(tableSettings == null || tableData.getIlluminations() == 0);
        skipGIFrames.getValueFactory().setValue(0);
        skipGIFrames.setDisable(tableSettings == null || tableData.getIlluminations() == 0);
        skipSolenoidFrames.getValueFactory().setValue(0);
        skipSolenoidFrames.setDisable(tableSettings == null || tableData.getIlluminations() == 0);
        skipLEDFrames.getValueFactory().setValue(0);
        skipLEDFrames.setDisable(tableSettings == null || tableData.getIlluminations() == 0 || usedLEDType.getValue() == null || usedLEDType.getValue().getId() == 2);
        lightBulbOn.setSelected(false);
        lightBulbOn.setDisable(tableSettings == null || (usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2));
        glowing.setDisable(tableSettings == null || (usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2));
        usedLEDType.setDisable(tableSettings == null);
        startBackground.setSelected(false);
        startBackground.setDisable(tableSettings == null);
        startAsExe.setSelected(false);
        startAsExe.setDisable(tableSettings == null);
        bringBGFromTop.setSelected(false);
        bringBGFromTop.setDisable(tableSettings == null);

        this.refreshing = false;
      });
    }
    else {
      return new JFXFuture(CompletableFuture.completedFuture(null));
    }
  }


  private void loadImages(DirectB2STableSettings tmpTableSettings) {
    Image thumbnail = null;
    String thumbnailError = null;
    if (tableData.isDmdImageAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
        thumbnail = new Image(in);
        if (tableData.getGrillHeight() > 0 && tmpTableSettings != null && tmpTableSettings.getHideGrill() == 1) {
          PixelReader reader = thumbnail.getPixelReader();
          thumbnail = new WritableImage(reader, 0, 0, (int) thumbnail.getWidth(), (int) (thumbnail.getHeight() - tableData.getGrillHeight()));
        }
      }
      catch (IOException ioe) {
        LOG.error("Cannot download background image for game " + tableData.getGameId(), ioe);
        thumbnail = null;
        thumbnailError = "Failed to read image data.";
      }
    } else {
      thumbnailError = "No Image data available.";
    }
    final Image _thumbnail = thumbnail;
    final String _thumbnailError = thumbnailError;
    Platform.runLater(() -> {
      if (_thumbnail != null) {
        thumbnailImage.setImage(_thumbnail);
        thumbnailImagePane.setCenter(thumbnailImage);
        downloadBackglassBtn.setDisable(false);
        resolutionLabel.setText("Resolution: " + (int) _thumbnail.getWidth() + " x " + (int) _thumbnail.getHeight());
      }
      else {
        thumbnailImage.setImage(null);
        thumbnailImagePane.setCenter(null);
        resolutionLabel.setText(_thumbnailError);
      }
    });

    Image dmdThumbnail = null; 
    String dmdThumbnailError = null;
    if (tableData.isDmdImageAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(tableData)) {
        dmdThumbnail = new Image(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download DMD image for game " + tableData.getGameId(), ioe);
        dmdThumbnailError = "Failed to read DMD image data.";
      }
    }  else {
      dmdThumbnailError = "No DMD background available.";
    }
    final Image _dmdThumbnail = dmdThumbnail;
    final String _dmdThumbnailError = dmdThumbnailError;
    Platform.runLater(() -> {
      if (_dmdThumbnail != null) {
        dmdThumbnailImage.setImage(_dmdThumbnail);
        dmdThumbnailImagePane.setCenter(dmdThumbnailImage);
        downloadDMDBtn.setDisable(false);
        dmdResolutionLabel.setText("Resolution: " + (int) _dmdThumbnail.getWidth() + " x " + (int) _dmdThumbnail.getHeight());
        fullDmdLabel.setText(isFullDmd(_dmdThumbnail.getWidth(), _dmdThumbnail.getHeight()) ? "Yes" : "No");
      }
      else {
        dmdThumbnailImage.setImage(null);
        dmdThumbnailImagePane.setCenter(null);
        dmdResolutionLabel.setText(_dmdThumbnailError);
        fullDmdLabel.setText("No");
      }
    });
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  public void selectGame(@Nullable GameRepresentation game) {
    // try to select first backglass of the game, do the selection by name
    if (game != null) {
      String gameBaseName = FilenameUtils.getBaseName(game.getGameFileName());

      // at calling time, the list may not have been populated so register a listener in that case
      if (backglasses != null) {
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
    for (DirectB2SEntryModel backglass : models) {
      if (StringUtils.startsWithIgnoreCase(backglass.getFileName(), gameBaseName)) {
        directb2sList.scrollTo(backglass);
        directb2sList.getSelectionModel().select(backglass);
        break;
      }
    }
  }

  private DirectB2SEntryModel getSelection() {
    return directb2sList.getSelectionModel().getSelectedItem();
  }

  private void debounceAndSave(String debounceKey, Runnable r) {
    if (refreshing) {
      r.run();
    }
    else {
      debouncer.debounce(debounceKey, () -> {
        r.run();
        save();
      }, DEBOUNCE_MS);
    }
  }

  private void save() {
    if (!this.refreshing && this.game != null) {
      try {
        client.getBackglassServiceClient().saveTableSettings(game.getId(), this.tableSettings);
        //DirectB2SEntryModel selectedItem = getSelection();
        //if (selectedItem != null) {
          Platform.runLater(() -> {
            //JFXFuture future = this.refresh(selectedItem.getBacklass());
            //future.thenLater(() -> {
              EventManager.getInstance().notifyTableChange(game.getId(), null);
            //});
          });
        //}
      }
      catch (Exception e) {
        LOG.error("Failed to save B2STableSettings.xml: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save B2STableSettings.xml: " + e.getMessage());
      }
    }
  }

  public GameRepresentation getGame() {
    return game;  
  }

  //------------------------------------------------


  public static class DirectB2SEntryModel extends BaseLoadingModel<DirectB2S, DirectB2SEntryModel> {

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

    private DirectB2SEntryModel(DirectB2S backglass) {
      super(backglass);
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

    public int getHideGrill() {
      return hideGrill;
    }
    public boolean isHideB2SDMD() {
      return hideB2SDMD;
    }
    public boolean isHideBackglass() {
      return hideBackglass;
    }
    public int getHideDMD() {
      return hideDMD;
    }
    public boolean hasDmd() {
      return hasDmd;
    }
    public boolean isFullDmd() {
      return BackglassManagerController.isFullDmd(dmdWidth, dmdHeight);
    }

    public int getDmdWidth() {
      return dmdWidth;
    }
    public int getDmdHeight() {
      return dmdHeight;
    }
    public int getGrillHeight() {
      return grillHeight;
    }
    public int getNbScores() {
      return nbScores;
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
