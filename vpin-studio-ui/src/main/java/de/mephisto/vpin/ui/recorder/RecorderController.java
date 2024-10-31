package de.mephisto.vpin.ui.recorder;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.recorder.panels.ScreenRecorderPanelController;
import de.mephisto.vpin.ui.tables.*;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.JFXFuture;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;
import static de.mephisto.vpin.ui.tables.TableOverviewController.ALL_VPX_ID;
import static de.mephisto.vpin.ui.tables.TableOverviewController.createAssetStatus;

public class RecorderController extends BaseTableController<GameRepresentation, GameRepresentationModel>
    implements Initializable, StudioFXController, ListChangeListener<GameRepresentationModel>, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderController.class);
  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 200;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnSelection;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnPlayfield;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnBackglass;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnDMD;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnTopper;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnFullDMD;

  @FXML
  private CheckBox selectAllCheckbox;

  @FXML
  private StackPane loaderStack;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private BorderPane root;

  @FXML
  private VBox recordingOptions;

  @FXML
  private Button recordBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Spinner<Integer> refreshInterval;

  private List<ScreenRecorderPanelController> screenRecorderPanelControllers = new ArrayList<>();

  private List<Integer> ignoredEmulators = null;

  private GameEmulatorChangeListener gameEmulatorChangeListener;

  private TablesController tablesController;
  private ScreenSizeChangeListener screenSizeChangeListener;

  private Thread screenRefresher;
  private boolean active = false;

  private List<GameRepresentation> selection = new ArrayList<>();

  // Add a public no-args constructor
  public RecorderController() {
  }

  @FXML
  private void onRecord() {
    RecorderDialogs.openRecordingDialog(this, selection);
  }


  @FXML
  private void onReload() {
    ProgressDialog.createProgressDialog(new CacheInvalidationProgressModel());
    this.doReload();
  }


  @FXML
  private void onReload(ActionEvent e) {
    ProgressDialog.createProgressDialog(new CacheInvalidationProgressModel());
    this.doReload();
  }

  public void doReload() {
    client.getGameService().clearCache();
    doReload(true);

    refreshScreens();
  }

  @FXML
  private void onDelete() {
  }

  public void doReload(boolean clearCache) {
    startReload("Loading Tables...");

    refreshEmulators();

    this.searchTextField.setDisable(true);
    this.reloadBtn.setDisable(true);

    GameRepresentation selection = getSelection();
    GameRepresentationModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    GameEmulatorRepresentation value = this.emulatorCombo.getSelectionModel().getSelectedItem();
    int id = value != null ? value.getId() : ALL_VPX_ID;

    JFXFuture.supplyAsync(() -> {
          if (clearCache) {
            client.getGameService().clearCache(id);
          }

          return id == ALL_VPX_ID
              ? client.getGameService().getVpxGamesCached()
              : client.getGameService().getGamesByEmulator(id);
        })
        .onErrorSupply(e -> {
          Platform.runLater(() -> WidgetFactory.showAlert(stage, "Error", "Loading tables failed: " + e.getMessage()));
          return Collections.emptyList();
        })
        .thenAcceptLater(data -> {
          // as the load of tables could take some time, users may have switched to another emulators in between
          // if this is the case, do not refresh the UI with the results
          GameEmulatorRepresentation valueAfterSearch = this.emulatorCombo.getValue();
          if (valueAfterSearch != null && valueAfterSearch.getId() != id) {
            return;
          }

          setItems(data);
          refreshFilters();

          if (data.isEmpty()) {
            tableView.setPlaceholder(new Label("No tables found"));
          }
          this.searchTextField.setDisable(false);
          this.reloadBtn.setDisable(false);
          tableView.requestFocus();

          if (selectedItem == null) {
            tableView.getSelectionModel().select(0);
          }
          else {
            tableView.getSelectionModel().select(selectedItem);
          }
          endReload();
        });
  }

  public GameEmulatorRepresentation getEmulatorSelection() {
    GameEmulatorRepresentation selectedEmu = this.emulatorCombo.getSelectionModel().getSelectedItem();
    return selectedEmu == null || selectedEmu.getId() == ALL_VPX_ID ? null : selectedEmu;
  }

  public void refreshFilters() {
    getFilterController().applyFilters();
  }


  private BaseFilterController getFilterController() {
    return filterController;
  }


  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        //TODO
      }
    }
  }


  @Override
  protected GameRepresentationModel toModel(GameRepresentation game) {
    return new GameRepresentationModel(game);
  }

  private void refreshEmulators() {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    this.emulatorCombo.valueProperty().removeListener(gameEmulatorChangeListener);
    GameEmulatorRepresentation selectedEmu = this.emulatorCombo.getSelectionModel().getSelectedItem();

    this.emulatorCombo.setDisable(true);
    List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getFrontendService().getGameEmulatorsUncached());
    List<GameEmulatorRepresentation> filtered = emulators.stream().filter(e -> !uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(e.getId()))).collect(Collectors.toList());

    this.emulatorCombo.setItems(FXCollections.observableList(filtered));
    this.emulatorCombo.setDisable(false);

    if (selectedEmu == null) {
      this.emulatorCombo.getSelectionModel().selectFirst();
    }

    this.emulatorCombo.valueProperty().addListener(gameEmulatorChangeListener);
  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("Media Recorder"));
    refreshEmulators();

    if (tableView.getItems().isEmpty()) {
      doReload();
    }

    Studio.stage.widthProperty().addListener(screenSizeChangeListener);
    Studio.stage.heightProperty().addListener(screenSizeChangeListener);

    Platform.runLater(() -> {
      refreshScreens();
    });

    this.active = true;
    screenRefresher = new Thread(() -> {
      try {
        LOG.info("Launched preview refresh thread.");
        while (active) {
          Platform.runLater(() -> {
            invalidateScreens();
          });

          RecorderSettings recorderSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
          Thread.sleep(recorderSettings.getRefreshInterval() * 1000);
        }
      }
      catch (Exception e) {
        LOG.error("Error in screen refresh thread: " + e.getMessage(), e);
      }
      finally {
        LOG.info("Exited preview refresh thread.");
      }
    });
    screenRefresher.start();
  }

  @Override
  public void onViewDeactivated() {
    Studio.stage.widthProperty().removeListener(screenSizeChangeListener);
    Studio.stage.heightProperty().removeListener(screenSizeChangeListener);

    this.active = false;
  }

  @Override
  public void onChanged(Change<? extends GameRepresentationModel> c) {

  }

  @Override
  public void preferencesChanged(String key, Object value) {
    // refresh emulators only when they have been loaded first time
    if (PreferenceNames.UI_SETTINGS.equals(key)) {
      UISettings uiSettings = (UISettings) value;
      if (!ListUtils.isEqualList(ignoredEmulators, uiSettings.getIgnoredEmulatorIds())) {
        this.ignoredEmulators = uiSettings.getIgnoredEmulatorIds();
        refreshEmulators();
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("table", "tables", new RecorderColumnSorter(this));
    recordingOptions.setFillWidth(true);

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    this.ignoredEmulators = uiSettings.getIgnoredEmulatorIds();

    gameEmulatorChangeListener = new GameEmulatorChangeListener();

    this.emulatorCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      //TODO
    });

    client.getPreferenceService().addListener(this);
    NavigationController.setBreadCrumb(Arrays.asList("Media Recorder"));

    super.loadFilterPanel("scene-recorder-filter.fxml");

    BaseLoadingColumn.configureColumn(columnDisplayName, (value, model) -> {
      Label label = new Label(value.getGameDisplayName());
      label.getStyleClass().add("default-text");
      label.setStyle(TableOverviewController.getLabelCss(value));

      String tooltip = value.getGameFilePath();
      if (value.getGameFilePath() != null) {
        label.setTooltip(new Tooltip(tooltip));
      }
      return label;
    }, true);

    BaseLoadingColumn.configureColumn(columnSelection, (value, model) -> {
      CheckBox columnCheckbox = new CheckBox();
      columnCheckbox.setUserData(value);
      columnCheckbox.setSelected(selection.contains(value));
      columnCheckbox.getStyleClass().add("default-text");
      columnCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (!newValue) {
            selection.remove(value);
          }
          else if (!selection.contains(value)) {
            selection.add(value);
          }
          refreshSelection();
        }
      });
      return columnCheckbox;
    }, true);
    BaseLoadingColumn.configureColumn(columnPlayfield, (value, model) -> createAssetStatus(value, model, VPinScreen.PlayField, event -> {
      TableOverviewController overviewController = tablesController.getTableOverviewController();
      TableDialogs.openTableAssetsDialog(overviewController, value, VPinScreen.PlayField);
    }), true);
    BaseLoadingColumn.configureColumn(columnBackglass, (value, model) -> createAssetStatus(value, model, VPinScreen.BackGlass, event -> {
      TableOverviewController overviewController = tablesController.getTableOverviewController();
      TableDialogs.openTableAssetsDialog(overviewController, value, VPinScreen.BackGlass);
    }), true);
    BaseLoadingColumn.configureColumn(columnDMD, (value, model) -> createAssetStatus(value, model, VPinScreen.DMD, event -> {
      TableOverviewController overviewController = tablesController.getTableOverviewController();
      TableDialogs.openTableAssetsDialog(overviewController, value, VPinScreen.DMD);
    }), true);
    BaseLoadingColumn.configureColumn(columnTopper, (value, model) -> createAssetStatus(value, model, VPinScreen.Topper, event -> {
      TableOverviewController overviewController = tablesController.getTableOverviewController();
      TableDialogs.openTableAssetsDialog(overviewController, value, VPinScreen.Topper);
    }), true);
    BaseLoadingColumn.configureColumn(columnFullDMD, (value, model) -> createAssetStatus(value, model, VPinScreen.Menu, event -> {
      TableOverviewController overviewController = tablesController.getTableOverviewController();
      TableDialogs.openTableAssetsDialog(overviewController, value, VPinScreen.Menu);
    }), true);


    List<RecordingScreen> recordingScreens = client.getRecorderService().getRecordingScreens();
    for (RecordingScreen recordingScreen : recordingScreens) {
      try {
        FXMLLoader loader = new FXMLLoader(ScreenRecorderPanelController.class.getResource("screen-recorder-panel.fxml"));
        Parent panelRoot = loader.load();
        ScreenRecorderPanelController screenPanelController = loader.getController();
        screenRecorderPanelControllers.add(screenPanelController);
        screenPanelController.setData(this, recordingScreen);
        recordingOptions.getChildren().add(panelRoot);
      }
      catch (IOException e) {
        LOG.error("failed to load recorder options tab: " + e.getMessage(), e);
      }
    }

    screenSizeChangeListener = new ScreenSizeChangeListener();

    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, settings.getRefreshInterval());
    refreshInterval.setValueFactory(factory);
    refreshInterval.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("refresh", () -> {
        refreshScreens();
        settings.setRefreshInterval(newValue.intValue());
        client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, settings);
      }, 300);
    });

    selectAllCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (!newValue) {
          selection.clear();
        }
        else {
          selection.clear();

          ObservableList<GameRepresentationModel> items = tableView.getItems();
          selection.addAll(items.stream().map(GameRepresentationModel::getGame).collect(Collectors.toList()));
        }
        refreshSelection();
      }
    });

    this.recordBtn.setDisable(true);
    labelCount.setText("No tables selected");
  }

  @Override
  protected void applyTableCount() {
    //ignore
  }

  public void refreshSelection() {
    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    boolean hasEnabledRecording = settings.getRecordingScreenOptions().stream().anyMatch(RecordingScreenOptions::isEnabled);

    this.recordBtn.setDisable(selection.isEmpty() || !hasEnabledRecording);
    labelCount.setText("No tables selected");
    if (!this.selection.isEmpty()) {
      if (this.selection.size() == 1) {
        labelCount.setText(this.selection.size() + " table selected");
      }
      else {
        labelCount.setText(this.selection.size() + " tables selected");
      }
    }
    tableView.refresh();
  }

  public void refreshScreens() {
    for (ScreenRecorderPanelController screenRecorderPanelController : screenRecorderPanelControllers) {
      screenRecorderPanelController.refresh();
    }
  }

  private void invalidateScreens() {
    for (ScreenRecorderPanelController screenRecorderPanelController : screenRecorderPanelControllers) {
      screenRecorderPanelController.invalidate();
    }
    refreshScreens();
  }

  class ScreenSizeChangeListener implements ChangeListener<Number> {
    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
      refreshScreens();
    }
  }

  class GameEmulatorChangeListener implements ChangeListener<GameEmulatorRepresentation> {
    @Override
    public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
      // callback to filter tables, once the data has been reloaded
      Platform.runLater(() -> {
        // just reload from cache
        doReload(false);
      });
    }
  }
}