package de.mephisto.vpin.ui.recorder;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingData;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.monitor.MonitoringManager;
import de.mephisto.vpin.ui.recorder.panels.ScreenRecorderPanelController;
import de.mephisto.vpin.ui.tables.*;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FrontendUtil;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.ListUtils;
import org.jetbrains.annotations.NotNull;
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
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnPlayfield;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnBackglass;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDMD;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnTopper;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnFullDMD;

  @FXML
  CheckBox checkBoxPlayfield;

  @FXML
  CheckBox checkBoxBackglass;

  @FXML
  CheckBox checkBoxDMD;

  @FXML
  CheckBox checkBoxTopper;

  @FXML
  CheckBox checkBoxFullDMD;

  @FXML
  private CheckBox selectAllCheckbox;

  @FXML
  private StackPane loaderStack;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private BorderPane root;

  @FXML
  private ComboBox<?> viewModeCombo;

  @FXML
  private VBox recordingOptions;

  @FXML
  private Button recordBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private Button tableNavigateBtn;

  @FXML
  private MenuButton screenMenuButton;

  @FXML
  private Spinner<Integer> refreshInterval;

  private List<ScreenRecorderPanelController> screenRecorderPanelControllers = new ArrayList<>();

  private List<Integer> ignoredEmulators = null;

  private GameEmulatorChangeListener gameEmulatorChangeListener;

  private TablesController tablesController;
  private ScreenSizeChangeListener screenSizeChangeListener;

  private Thread screenRefresher;
  private boolean active = false;

  private RecordingDataSummary selection = new RecordingDataSummary();

  // Add a public no-args constructor
  public RecorderController() {
  }


  @FXML
  private void onOpenTable(ActionEvent e) {
    GameRepresentationModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(selectedItem.getGame().getId()));
    }
  }

  @FXML
  public void onStop() {
    Frontend frontend = client.getFrontendService().getFrontendCached();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        FrontendUtil.replaceNames("Stop all emulators and [Frontend] processes?", frontend, null));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getFrontendService().terminateFrontend();
    }
  }

  @FXML
  private void onRecord() {
    JobDescriptor recording = client.getRecorderService().isRecording();
    if (recording != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Recorder Active", "Another recording is still active.", "Please wait or cancel all active recordings.", "Stop Recordings");
      if (!result.isPresent() || !result.get().equals(ButtonType.OK)) {
        client.getRecorderService().stopRecording(recording);
      }
    }
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

  @FXML
  public void onTableEdit() {
    GameRepresentation selectedItems = getSelection();
    if (selectedItems != null) {
      if (Studio.client.getFrontendService().isFrontendRunning()) {
        if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
          TableDialogs.openTableDataDialog(null, selectedItems);
        }
        return;
      }
      TableDialogs.openTableDataDialog(null, selectedItems);
    }
  }

  public void doReload(boolean clearCache) {
    startReload("Loading Tables...");

    refreshEmulators();

    this.searchTextField.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.dataManagerBtn.setDisable(true);
    this.tableNavigateBtn.setDisable(true);

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
  public void onMediaEdit() {
    GameRepresentation selectedItems = getSelection();
    if (selectedItems != null) {
      TableDialogs.openTableAssetsDialog(tablesController.getTableOverviewController(), selectedItems, VPinScreen.BackGlass);
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        if (mouseEvent.isShiftDown()) {
          onMediaEdit();
          return;
        }
        onTableEdit();
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
    MonitoringManager.getInstance().setRecordingRefreshIntervalSec(refreshInterval.getValue());
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
            refreshScreens();
          });

          Thread.sleep(500);
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
    MonitoringManager.getInstance().setRecordingRefreshIntervalSec(Integer.MAX_VALUE);
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

    super.loadFilterPanel(TableFilterController.class, "scene-tables-overview-filter.fxml");

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
      columnCheckbox.setSelected(selection.contains(value.getId()));
      columnCheckbox.getStyleClass().add("default-text");
      columnCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (newValue) {
            RecordingData recordingData = createRecordingData(value.getId());
            selection.add(recordingData);
          }
          else {
            selection.remove(value.getId());
          }
          refreshSelection();
        }
      });
      return columnCheckbox;
    }, true);


    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    BaseLoadingColumn.configureColumn(columnPlayfield, (value, model) -> createScreenCell(value, model, VPinScreen.PlayField), settings.isEnabled(VPinScreen.PlayField));
    BaseLoadingColumn.configureColumn(columnBackglass, (value, model) -> createScreenCell(value, model, VPinScreen.BackGlass), settings.isEnabled(VPinScreen.BackGlass));
    BaseLoadingColumn.configureColumn(columnDMD, (value, model) -> createScreenCell(value, model, VPinScreen.DMD), settings.isEnabled(VPinScreen.DMD));
    BaseLoadingColumn.configureColumn(columnTopper, (value, model) -> createScreenCell(value, model, VPinScreen.Topper), settings.isEnabled(VPinScreen.Topper));
    BaseLoadingColumn.configureColumn(columnFullDMD, (value, model) -> createScreenCell(value, model, VPinScreen.Menu), settings.isEnabled(VPinScreen.Menu));

    List<RecordingScreenOptions> options = new ArrayList<>();
    List<RecordingScreen> recordingScreens = client.getRecorderService().getRecordingScreens();
    for (RecordingScreen recordingScreen : recordingScreens) {
      try {
        FXMLLoader loader = new FXMLLoader(ScreenRecorderPanelController.class.getResource("screen-recorder-panel.fxml"));
        Parent panelRoot = loader.load();
        ScreenRecorderPanelController screenPanelController = loader.getController();
        screenRecorderPanelControllers.add(screenPanelController);
        screenPanelController.setVisible(settings.isEnabled(recordingScreen.getScreen()));
        screenPanelController.setData(this, recordingScreen);
        recordingOptions.getChildren().add(panelRoot);
      }
      catch (IOException e) {
        LOG.error("failed to load recorder options tab: " + e.getMessage(), e);
      }
      options.add(settings.getRecordingScreenOption(recordingScreen));
    }
    // get only the options that have a valid RecordingScreen and ignore all other ones
    settings.setRecordingScreenOptions(options);
    client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, settings);

    screenSizeChangeListener = new ScreenSizeChangeListener();

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, settings.getRefreshInterval());
    refreshInterval.setValueFactory(factory);
    refreshInterval.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("refresh", () -> {
        refreshScreens();
        settings.setRefreshInterval(newValue.intValue());
        MonitoringManager.getInstance().setRecordingRefreshIntervalSec(refreshInterval.getValue());
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
          selection.addAll(items.stream().map(g -> createRecordingData(g.getGameId())).collect(Collectors.toList()));
        }
        refreshSelection();
      }
    });

    tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<GameRepresentationModel>() {
      @Override
      public void changed(ObservableValue<? extends GameRepresentationModel> observable, GameRepresentationModel oldValue, GameRepresentationModel newValue) {
        dataManagerBtn.setDisable(newValue == null);
        tableNavigateBtn.setDisable(newValue == null);
      }
    });

    List<RecordingScreen> supportedRecordingScreens = client.getRecorderService().getRecordingScreens();
    for (RecordingScreen recordingScreen : supportedRecordingScreens) {
      VPinScreen screen = recordingScreen.getScreen();
      CustomMenuItem item = new CustomMenuItem();
      CheckBox checkBox = new CheckBox();
      checkBox.setText(recordingScreen.getName());
      checkBox.getStyleClass().add("default-text");
      checkBox.setStyle("-fx-font-size: 14px;-fx-padding: 0 6 0 6;");
      checkBox.setPrefHeight(30);
      checkBox.setSelected(settings.isEnabled(screen));
      item.setContent(checkBox);
      item.setGraphic(WidgetFactory.createIcon("mdi2m-monitor"));
      item.setOnAction(actionEvent -> {
        RecorderSettings recorderSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        recorderSettings.getRecordingScreenOption(recordingScreen).setEnabled(checkBox.isSelected());
        client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, recorderSettings);

        switch (screen) {
          case PlayField: {
            columnPlayfield.setVisible(checkBox.isSelected());
            break;
          }
          case BackGlass: {
            columnBackglass.setVisible(checkBox.isSelected());
            break;
          }
          case DMD: {
            columnDMD.setVisible(checkBox.isSelected());
            break;
          }
          case Menu: {
            columnFullDMD.setVisible(checkBox.isSelected());
            break;
          }
          case Topper: {
            columnTopper.setVisible(checkBox.isSelected());
            break;
          }
        }
        refreshSelection();
      });
      screenMenuButton.getItems().add(item);
    }

    checkBoxPlayfield.selectedProperty().addListener(new ColumnCheckboxListener(VPinScreen.PlayField));
    checkBoxBackglass.selectedProperty().addListener(new ColumnCheckboxListener(VPinScreen.BackGlass));
    checkBoxFullDMD.selectedProperty().addListener(new ColumnCheckboxListener(VPinScreen.Menu));
    checkBoxTopper.selectedProperty().addListener(new ColumnCheckboxListener(VPinScreen.Topper));
    checkBoxDMD.selectedProperty().addListener(new ColumnCheckboxListener(VPinScreen.DMD));

    this.recordBtn.setDisable(true);
    labelCount.setText("No tables selected");
  }

  @NotNull
  private RecordingData createRecordingData(int id) {
    RecordingData recordingData = new RecordingData();
    recordingData.setGameId(id);
    recordingData.setScreens(getEnabledScreens());
    return recordingData;
  }

  @NotNull
  private HBox createScreenCell(GameRepresentation value, GameRepresentationModel model, VPinScreen screen) {
    HBox column = new HBox(3);
    column.setAlignment(Pos.CENTER);
    CheckBox columnCheckbox = new CheckBox();
    columnCheckbox.setUserData(value);
    columnCheckbox.setSelected(selection.contains(value.getId()) && selection.get(model.getGameId()).containsScreen(screen));
    columnCheckbox.getStyleClass().add("default-text");
    columnCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (!newValue) {
          if (selection.contains(value.getId())) {
            selection.get(value.getId()).removeScreen(screen);
          }
        }
        else {
          if (!selection.contains(value.getId())) {
            RecordingData recordingData = createRecordingData(value.getId());
            recordingData.clear();
            selection.add(recordingData);
          }
          selection.get(value.getId()).addScreen(screen);
        }
        refreshSelection();
      }
    });
    Node assetStatus = createAssetStatus(value, model, VPinScreen.PlayField, event -> {
      TableOverviewController overviewController = tablesController.getTableOverviewController();
      TableDialogs.openTableAssetsDialog(overviewController, value, VPinScreen.PlayField);
    });
    column.getChildren().add(columnCheckbox);
    column.getChildren().add(assetStatus);
    return column;
  }

  private List<VPinScreen> getEnabledScreens() {
    List<VPinScreen> result = new ArrayList<>();
    if (columnPlayfield.isVisible()) {
      result.add(VPinScreen.PlayField);
    }
    if (columnBackglass.isVisible()) {
      result.add(VPinScreen.BackGlass);
    }
    if (columnDMD.isVisible()) {
      result.add(VPinScreen.DMD);
    }
    if (columnFullDMD.isVisible()) {
      result.add(VPinScreen.Menu);
    }
    if (columnTopper.isVisible()) {
      result.add(VPinScreen.Topper);
    }
    return result;
  }

  @Override
  protected void applyTableCount() {
    //ignore
  }

  public void refreshSelection() {
    RecorderSettings recorderSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    for (ScreenRecorderPanelController screenRecorderPanelController : screenRecorderPanelControllers) {
      screenRecorderPanelController.setVisible(recorderSettings.isEnabled(screenRecorderPanelController.getScreen()));
    }

    boolean hasEnabledRecording = recorderSettings.isEnabled() && !this.selection.isEmpty();

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
    long start = System.currentTimeMillis();
    for (ScreenRecorderPanelController screenRecorderPanelController : screenRecorderPanelControllers) {
      screenRecorderPanelController.refresh();
    }
    long end = System.currentTimeMillis() - start;
    if (end > 0 && end > (refreshInterval.getValue() * 1000)) {
      LOG.warn("Total refresh time {}ms vs refresh interval of {}", end, refreshInterval.getValue());
    }
  }

  class ScreenSizeChangeListener implements ChangeListener<Number> {
    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
      refreshScreens();
    }
  }

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
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

  class ColumnCheckboxListener implements ChangeListener<Boolean> {
    private final VPinScreen screen;

    ColumnCheckboxListener(VPinScreen screen) {
      this.screen = screen;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      if(newValue) {
        selection.getRecordingData().forEach(data -> data.addScreen(screen));
      }
      else {
        selection.getRecordingData().forEach(data -> data.removeScreen(screen));
      }
      refreshSelection();
    }
  }
}