package de.mephisto.vpin.ui.recorder;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.recorder.*;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.monitor.MonitoringManager;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.recorder.panels.ScreenRecorderPanelController;
import de.mephisto.vpin.ui.tables.*;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.ListUtils;
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
    implements Initializable, StudioFXController, ListChangeListener<GameRepresentationModel>, PreferenceChangeListener, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderController.class);
  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 200;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnSelection;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDisplayName;

  @FXML
  private CheckBox selectAllCheckbox;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private BorderPane root;

  @FXML
  private VBox recordingOptions;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private Button tableNavigateBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Button recordBtn;

  @FXML
  private MenuButton screenMenuButton;

  @FXML
  private Spinner<Integer> refreshInterval;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDateModified;

  @FXML
  private ToolBar toolbar;

  private Map<VPinScreen, TableColumn<GameRepresentationModel, GameRepresentationModel>> screenColumns;

  private final List<ScreenRecorderPanelController> screenRecorderPanelControllers = new ArrayList<>();

  private List<Integer> ignoredEmulators = null;

  private GameEmulatorChangeListener gameEmulatorChangeListener;

  private PlayButtonController playButtonController;

  private boolean active = false;

  private final RecordingDataSummary selection = new RecordingDataSummary();

  // Add a public no-args constructor
  public RecorderController() {
  }


  @FXML
  private void onAutoSelect(ActionEvent e) {
    selection.clear();

    ValidationSettings validationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);
    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();
    List<GameRepresentationModel> items = tableView.getItems();

    Set<VPinScreen> vPinScreens = screenColumns.keySet();

    for (GameRepresentationModel item : items) {
      FrontendMediaRepresentation gameMedia = client.getGameMediaService().getGameMedia(item.getGameId());
      RecordingData data = null;
      List<VPinScreen> screens = new ArrayList<>();

      for (VPinScreen screen : vPinScreens) {
        TableColumn<GameRepresentationModel, GameRepresentationModel> column = screenColumns.get(screen);
        if (!column.isVisible()) {
          continue;
        }

        ValidationConfig config = defaultProfile.getOrCreateConfig(screen.getValidationCode());
        ValidatorMedia media = config.getMedia();

        if (media.equals(ValidatorMedia.image) || media.equals(ValidatorMedia.audio)) {
          continue;
        }

        FrontendMediaItemRepresentation defaultMediaItem = gameMedia.getDefaultMediaItem(screen);
        if (defaultMediaItem == null && config.getOption().equals(ValidatorOption.mandatory)) {
          if (data == null) {
            data = new RecordingData();
            data.setGameId(item.getGameId());
          }
          screens.add(screen);
        }
      }
      if (data != null) {
        data.setScreens(screens);
        selection.add(data);
      }
    }

    this.doReload(false);
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
      if (result.isEmpty() || !result.get().equals(ButtonType.OK)) {
        client.getRecorderService().stopRecording(recording);
      }
    }
    RecorderDialogs.openRecordingDialog(this, selection);
  }

  @FXML
  private void onReload() {
    ProgressDialog.createProgressDialog(ClearCacheProgressModel.getReloadGamesClearCacheModel(true));
    this.doReload(true);
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

  private void doReload(boolean clearCache) {
    startReload("Loading Tables...");

    if (clearCache) {
      client.getFrontendService().getScreenSummary(true);
    }
    refreshEmulators();
    refreshPlaylists();

    this.searchTextField.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.dataManagerBtn.setDisable(true);
    this.tableNavigateBtn.setDisable(true);

    //GameRepresentation selection = getSelection();
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

          RecorderSettings recorderSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
          boolean hasEnabledRecording = recorderSettings.isEnabled() && !this.selection.isEmpty();
          this.recordBtn.setDisable(selection.isEmpty() || !hasEnabledRecording);
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
    List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getEmulatorService().getGameEmulatorsUncached());
    List<GameEmulatorRepresentation> filtered = emulators.stream().filter(e -> !uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(e.getId()))).collect(Collectors.toList());

    this.emulatorCombo.setItems(FXCollections.observableList(filtered));
    this.emulatorCombo.setDisable(false);

    if (selectedEmu == null) {
      this.emulatorCombo.getSelectionModel().selectFirst();
    }

    this.emulatorCombo.valueProperty().addListener(gameEmulatorChangeListener);
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    MonitoringManager.getInstance().setRecordingRefreshIntervalSec(refreshInterval.getValue());
    NavigationController.setBreadCrumb(Arrays.asList("Media Recorder"));
    refreshEmulators();

    if (tableView.getItems().isEmpty()) {
      doReload(false);
    }

    Platform.runLater(this::refreshScreens);

    this.active = true;
    Thread screenRefresher = new Thread(() -> {
      try {
        LOG.info("Launched preview refresh thread.");
        while (active) {
          Platform.runLater(this::refreshScreens);
          Thread.sleep(1000);
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
    NavigationController.setBreadCrumb(List.of("Media Recorder"));

    super.loadFilterPanel(TableFilterController.class, "scene-tables-overview-filter.fxml");
    super.loadPlaylistCombo();

    RecorderSettings recorderSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    List<RecordingScreenOptions> options = new ArrayList<>();
    List<FrontendPlayerDisplay> recordingScreens = client.getRecorderService().getRecordingScreens();
    for (FrontendPlayerDisplay recordingScreen : recordingScreens) {
      try {
        FXMLLoader loader = new FXMLLoader(ScreenRecorderPanelController.class.getResource("screen-recorder-panel.fxml"));
        Parent panelRoot = loader.load();
        ScreenRecorderPanelController screenPanelController = loader.getController();
        screenRecorderPanelControllers.add(screenPanelController);
        screenPanelController.setVisible(recorderSettings.isEnabled(recordingScreen.getScreen()));
        screenPanelController.setData(this, recordingScreen);
        recordingOptions.getChildren().add(panelRoot);
      }
      catch (IOException e) {
        LOG.error("failed to load recorder options tab: " + e.getMessage(), e);
      }
      RecordingScreenOptions recordingScreenOption = recorderSettings.getRecordingScreenOption(recordingScreen);
      if (recordingScreenOption != null) {
        options.add(recordingScreenOption);
      }
      else {
        RecordingScreenOptions opt = new RecordingScreenOptions();
        opt.setDisplayName(recordingScreen.getName());
        options.add(opt);
      }
    }
    // get only the options that have a valid RecordingScreen and ignore all other ones
    recorderSettings.setRecordingScreenOptions(options);
    client.getPreferenceService().setJsonPreference(recorderSettings);


    /*
     * Configure columns after creating the screen panels, the settings are only initialized then
     */
    BaseLoadingColumn.configureColumn(columnDisplayName, (value, model) -> {
      Label label = new Label(value.getGameDisplayName());
      label.getStyleClass().add("default-text");
      label.setStyle(TableOverviewController.getLabelCss(value));

      String tooltip = value.getGameFilePath();
      if (value.getGameFilePath() != null) {
        label.setTooltip(new Tooltip(tooltip));
      }
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnDateModified, (value, model) -> {
      Label label = null;
      if (value.getDateAdded() != null) {
        label = new Label(TableOverviewController.dateFormat.format(value.getDateUpdated()));
      }
      else {
        label = new Label("-");
      }
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);


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
    }, this, true);

    screenColumns = new HashMap<>();
    Collections.reverse(recordingScreens);
    for (FrontendPlayerDisplay screen : recordingScreens) {
      TableColumn<GameRepresentationModel, GameRepresentationModel> column = new TableColumn<>(screen.getName());
      column.setPrefWidth(130);
      column.setId(screen.getScreen().name());
      if (screen.getScreen().equals(VPinScreen.DMD) || screen.getScreen().equals(VPinScreen.Topper)) {
        column.setPrefWidth(90);
      }
      column.setStyle("-fx-alignment: CENTER;");
      BaseLoadingColumn.configureColumn(column, (value, model) -> createScreenCell(value, model, screen.getScreen()), this, recorderSettings.isEnabled(screen.getScreen()));

      CheckBox cb = new CheckBox();
      column.setGraphic(cb);
      cb.selectedProperty().addListener(new ColumnCheckboxListener(screen.getScreen(), column));

      tableView.getColumns().add(2, column);
      screenColumns.put(screen.getScreen(), column);
    }

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, recorderSettings.getRefreshInterval());
    refreshInterval.setValueFactory(factory);
    refreshInterval.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("refresh", () -> {
        refreshScreens();
        recorderSettings.setRefreshInterval(newValue.intValue());
        MonitoringManager.getInstance().setRecordingRefreshIntervalSec(refreshInterval.getValue());
        client.getPreferenceService().setJsonPreference(recorderSettings);
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
        if (newValue != null) {
          playButtonController.setData(newValue.getGame());
        }
        else {
          playButtonController.setData(null);
        }

        if (newValue != null) {
          EventManager.getInstance().notifyTableSelectionChanged(Collections.singletonList(newValue.getGame()));
        }
      }
    });

    refreshScreenMenu();

    this.recordBtn.setDisable(true);
    labelCount.setText("No tables selected");

    try {
      FXMLLoader loader = new FXMLLoader(PlayButtonController.class.getResource("play-btn.fxml"));
      Parent playBtnRoot = loader.load();
      playButtonController = loader.getController();
      playButtonController.setDisable(true);
      int i = toolbar.getItems().indexOf(stopBtn);
      toolbar.getItems().add(i, playBtnRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load play button: " + e.getMessage(), e);
    }

    EventManager.getInstance().addListener(this);
  }

  private void refreshScreenMenu() {
    RecorderSettings recorderSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    List<FrontendPlayerDisplay> recordingScreens = client.getRecorderService().getRecordingScreens();

    screenMenuButton.getItems().clear();
    for (FrontendPlayerDisplay recordingScreen : recordingScreens) {
      VPinScreen screen = recordingScreen.getScreen();
      CustomMenuItem item = new CustomMenuItem();
      CheckBox checkBox = new CheckBox();
      checkBox.setText(recordingScreen.getName());
      checkBox.getStyleClass().add("default-text");
      checkBox.setStyle("-fx-font-size: 14px;-fx-padding: 0 6 0 6;");
      checkBox.setPrefHeight(30);
      checkBox.setSelected(recorderSettings.isEnabled(screen));
      item.setContent(checkBox);
      item.setGraphic(WidgetFactory.createIcon("mdi2m-monitor"));
      item.setOnAction(actionEvent -> {
        RecorderSettings rSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        rSettings.getRecordingScreenOption(recordingScreen).setEnabled(checkBox.isSelected());
        client.getPreferenceService().setJsonPreference(rSettings);

        TableColumn<?, ?> column = screenColumns.get(screen);
        if (column != null) {
          column.setVisible(checkBox.isSelected());
        }
        refreshSelection();
      });

      TableColumn<?, ?> column = screenColumns.get(screen);
      if (column != null) {
        column.setVisible(recorderSettings.getRecordingScreenOption(recordingScreen).isEnabled());
      }

      screenMenuButton.getItems().add(item);
    }
  }

  @NonNull
  private RecordingData createRecordingData(int id) {
    RecordingData recordingData = new RecordingData();
    recordingData.setGameId(id);
    recordingData.setScreens(getEnabledScreens());
    return recordingData;
  }

  @NonNull
  private HBox createScreenCell(GameRepresentation value, GameRepresentationModel model, VPinScreen screen) {
    HBox column = new HBox(3);

    if (this.active) {
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

      Node assetStatus = createAssetStatus(value, model, screen, event -> {
        TableOverviewController overviewController = tablesController.getTableOverviewController();
        TableDialogs.openTableAssetsDialog(overviewController, value, screen);
      });
      column.getChildren().add(columnCheckbox);
      column.getChildren().add(assetStatus);
    }

    return column;
  }

  private List<VPinScreen> getEnabledScreens() {
    List<VPinScreen> result = new ArrayList<>();
    for (Map.Entry<VPinScreen, TableColumn<GameRepresentationModel, GameRepresentationModel>> entry : screenColumns.entrySet()) {
      if (entry.getValue().isVisible()) {
        result.add(entry.getKey());
      }
    }
    return result;
  }

  VPinScreen screenFromColumn(TableColumn<?, ?> column) {
    for (Map.Entry<VPinScreen, TableColumn<GameRepresentationModel, GameRepresentationModel>> sc : screenColumns.entrySet()) {
      if (column.equals(sc.getValue())) {
        return sc.getKey();
      }
    }
    return null;
  }

  @Override
  protected void applyTableCount() {
    //ignore
  }

  public void refreshSelection() {
    RecorderSettings recorderSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    for (ScreenRecorderPanelController screenRecorderPanelController : screenRecorderPanelControllers) {
      screenRecorderPanelController.setVisible(recorderSettings.
          isEnabled(screenRecorderPanelController.getScreen()));
    }

    boolean hasEnabledRecording = recorderSettings.isEnabled() && !this.selection.isEmpty();

    this.recordBtn.setDisable(selection.isEmpty() || !hasEnabledRecording);
    playButtonController.setDisable(selection.isEmpty() || !hasEnabledRecording);
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

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  @Override
  public void tableChanged(int id, @Nullable String rom, @Nullable String gameName) {
    if (!active) {
      return;
    }

    GameRepresentation refreshedGame = client.getGameService().getGame(id);
    if (refreshedGame != null) {
      GameRepresentationModel model = getModel(refreshedGame);
      if (model != null) {
        model.setBean(refreshedGame);
        model.reload(null);
      }
      else {
        // new table, add it to the list only if the emulator is matching
        GameEmulatorRepresentation value = this.emulatorCombo.getValue();
        if (value != null && (value.getId() == refreshedGame.getEmulatorId() || value.getType().equals(value.getType()))) {
          models.add(0, new GameRepresentationModel(refreshedGame));
        }
      }

      // force refresh the view for elements not observed by the table
      tableView.refresh();
    }
  }

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (PreferenceType.validationSettings.equals(preferenceType)) {
      Platform.runLater(() -> {
        refreshSelection();
        refreshScreenMenu();
      });
    }
  }

  @Override
  public void tablesChanged() {
    Platform.runLater(() -> {
      tableView.refresh();
    });
  }

  @Override
  protected FilterSettings getFilterSettings() {
    return client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDINGS_FILTER_SETTINGS, RecorderFilterSettings.class);
  }

  //----------------------- Model classes ------------------------------------------------------------------------------

  class GameEmulatorChangeListener implements ChangeListener<GameEmulatorRepresentation> {
    @Override
    public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
      selection.clear();
      // callback to filter tables, once the data has been reloaded
      Platform.runLater(() -> {
        // just reload from cache
        doReload(false);
      });
    }
  }

  class ColumnCheckboxListener implements ChangeListener<Boolean> {
    private final VPinScreen screen;
    private final TableColumn<GameRepresentationModel, GameRepresentationModel> column;

    ColumnCheckboxListener(VPinScreen screen, TableColumn<GameRepresentationModel, GameRepresentationModel> column) {
      this.screen = screen;
      this.column = column;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      if (newValue) {
        ObservableList<GameRepresentationModel> items = tableView.getItems();
        for (GameRepresentationModel item : items) {
          if (!selection.contains(item.getGameId())) {
            RecordingData data = new RecordingData();
            data.setGameId(item.getGameId());
            selection.add(data);
          }
        }

        selection.getRecordingData().forEach(data -> data.addScreen(screen));
      }
      else {
        selection.getRecordingData().forEach(data -> data.removeScreen(screen));
      }
      refreshSelection();
    }
  }
}