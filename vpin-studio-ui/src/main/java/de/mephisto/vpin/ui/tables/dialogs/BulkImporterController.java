package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.util.ThemeColors;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportItem;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMatchProgressModel;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMatcher;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMatcher.MatchMode;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMode;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMoveStructure;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportProgressModel;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportScanProgressModel;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FilterPills;
import de.mephisto.vpin.ui.util.SegmentedToggles;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Matches media files from a folder to tables and uploads them to the selected screens.
 */
public class BulkImporterController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final Set<String> IMAGE_EXT = Set.of("png", "jpg", "jpeg", "gif", "apng", "webp");
  private static final Set<String> VIDEO_EXT = Set.of("mp4", "avi", "mov", "webm", "mkv");
  private static final Set<String> AUDIO_EXT = Set.of("mp3", "wav", "ogg");
  private static final int SUGGESTION_COUNT = 8;
  private static final String SOURCE_MANUAL = "manual";
  private static final int PREVIEW_CACHE_SIZE = 300;
  private static final String MOVE_ENABLED = "bulkImporterMoveEnabled";
  private static final String MOVE_TARGET = "bulkImporterMoveTarget";
  // legacy setting, replaced by MOVE_STRUCTURE
  private static final String MOVE_KEEP_STRUCTURE = "bulkImporterMoveKeepStructure";
  private static final String MOVE_STRUCTURE = "bulkImporterMoveStructure";
  private static final String EMULATOR_ID = "bulkImporterEmulatorId";
  private static final String PREVIEW_SIZE = "bulkImporterPreviewSize";
  /**
   * Automatic matches from this score on are "good", below they should be checked.
   */
  private static final double GOOD_SCORE = 0.8;
  private static final int MATCH_BAR_WIDTH = 64;

  /**
   * The filter pills above the review table.
   */
  private enum ReviewFilter {
    ALL, GOOD, CHECK, NO_TABLE;

    boolean test(BulkImportItem item) {
      switch (this) {
        case GOOD:
          return item.gameProperty().get() != null && isGoodMatch(item);
        case CHECK:
          return item.gameProperty().get() != null && !isGoodMatch(item);
        case NO_TABLE:
          return item.gameProperty().get() == null;
        default:
          return true;
      }
    }
  }

  private static boolean isGoodMatch(BulkImportItem item) {
    return SOURCE_MANUAL.equals(item.matchSourceProperty().get()) || item.scoreProperty().get() >= GOOD_SCORE;
  }

  @FXML
  private TextField sourceField;
  @FXML
  private CheckBox recursiveCheck;
  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;
  @FXML
  private ComboBox<MatchMode> matchModeCombo;
  @FXML
  private Slider scoreSlider;
  @FXML
  private Label scoreLabel;
  @FXML
  private CheckBox detectScreenCheck;
  @FXML
  private ComboBox<VPinScreen> defaultScreenCombo;
  @FXML
  private ToggleGroup defaultModeGroup;
  @FXML
  private ToggleButton modeAppendBtn;
  @FXML
  private ToggleButton modeOverwriteBtn;
  @FXML
  private ToggleButton modeIfMissingBtn;
  @FXML
  private CheckBox moveCheck;
  @FXML
  private TextField moveTargetField;
  @FXML
  private Button moveTargetBtn;
  @FXML
  private ToggleButton moveFlatBtn;
  @FXML
  private ToggleButton moveKeepStructureBtn;
  @FXML
  private ToggleButton moveTableFolderBtn;
  @FXML
  private ToggleGroup moveStructureGroup;
  @FXML
  private HBox moveStructureBox;
  @FXML
  private Label movePreviewLabel;
  @FXML
  private ToggleGroup previewSizeGroup;
  @FXML
  private ToggleButton sizeOffBtn;
  @FXML
  private ToggleButton sizeSmallBtn;
  @FXML
  private ToggleButton sizeMediumBtn;
  @FXML
  private ToggleButton sizeLargeBtn;
  @FXML
  private TableView<BulkImportItem> tableView;
  @FXML
  private TableColumn<BulkImportItem, BulkImportItem> columnSelect;
  @FXML
  private TableColumn<BulkImportItem, BulkImportItem> columnPreview;
  @FXML
  private TableColumn<BulkImportItem, BulkImportItem> columnFile;
  @FXML
  private TableColumn<BulkImportItem, BulkImportItem> columnTable;
  @FXML
  private TableColumn<BulkImportItem, BulkImportItem> columnScore;
  @FXML
  private TableColumn<BulkImportItem, BulkImportItem> columnScreen;
  @FXML
  private TableColumn<BulkImportItem, BulkImportItem> columnMode;

  @FXML
  private HBox filterBox;
  @FXML
  private Label summaryLabel;
  @FXML
  private Label skipLabel;
  @FXML
  private Button importBtn;
  @FXML
  private Button removeBtn;
  @FXML
  private Button removeUnmatchedBtn;

  private final ObservableList<BulkImportItem> items = FXCollections.observableArrayList(
      item -> new javafx.beans.Observable[]{item.gameProperty(), item.screenProperty(), item.selectedProperty()});
  private final FilteredList<BulkImportItem> filteredItems = new FilteredList<>(items);
  private final CheckBox selectAllCheck = new CheckBox();
  private final ToggleGroup filterGroup = new ToggleGroup();
  private final Map<ReviewFilter, ToggleButton> filterPills = new EnumMap<>(ReviewFilter.class);

  // tables of all active emulators, allGames holds the ones of the selected emulator
  private List<GameRepresentation> loadedGames = Collections.emptyList();
  private List<GameRepresentation> allGames = Collections.emptyList();
  private BulkImportMatcher matcher;
  private File sourceFolder;
  private int previewSize = 96;
  private boolean updating = false;
  private boolean scanning = false;
  private List<VPinScreen> screenOptions = Collections.emptyList();
  private StringConverter<VPinScreen> screenConverter;

  // decoded previews, so scrolling does not load the images again
  private final Map<File, Image> previewCache = new LinkedHashMap<>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<File, Image> eldest) {
      return size() > PREVIEW_CACHE_SIZE;
    }
  };

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    matchModeCombo.setItems(FXCollections.observableArrayList(MatchMode.values()));
    matchModeCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(MatchMode mode) {
        return mode == null ? "" : Messages.get("bulk_importer.match_mode." + mode.name().toLowerCase());
      }

      @Override
      public MatchMode fromString(String string) {
        return null;
      }
    });
    matchModeCombo.setValue(MatchMode.FILENAME_AND_FOLDER);
    matchModeCombo.valueProperty().addListener((obs, o, n) -> rematch(false, true));

    StringConverter<VPinScreen> screenConverter = new StringConverter<>() {
      @Override
      public String toString(VPinScreen screen) {
        return screen == null ? "" : TableAssetManagerPane.getScreenLabel(screen, false);
      }

      @Override
      public VPinScreen fromString(String string) {
        return null;
      }
    };
    List<VPinScreen> screens = new ArrayList<>();
    screens.add(null);
    List<VPinScreen> supportedScreens = client.getFrontendService().getFrontendCached().getSupportedScreens();
    Arrays.stream(VPinScreen.values()).filter(supportedScreens::contains).forEach(screens::add);
    defaultScreenCombo.setItems(FXCollections.observableArrayList(screens));
    // without a default screen the screen is chosen per file
    defaultScreenCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(VPinScreen screen) {
        return screen == null ? Messages.get("bulk_importer.screen_ask") : screenConverter.toString(screen);
      }

      @Override
      public VPinScreen fromString(String string) {
        return null;
      }
    });
    defaultScreenCombo.valueProperty().addListener((obs, o, n) -> rematch(false, false));
    screenOptions = screens;
    this.screenConverter = screenConverter;

    modeAppendBtn.setUserData(BulkImportMode.APPEND);
    modeOverwriteBtn.setUserData(BulkImportMode.OVERWRITE);
    modeIfMissingBtn.setUserData(BulkImportMode.IF_MISSING);
    SegmentedToggles.keepSelected(modeAppendBtn, modeOverwriteBtn, modeIfMissingBtn);
    defaultModeGroup.selectedToggleProperty().addListener((obs, o, n) -> {
      if (n == null) {
        return;
      }
      // the setting applies to all files, single rows can be changed afterwards
      for (BulkImportItem item : items) {
        item.modeProperty().set(getDefaultMode());
      }
      tableView.refresh();
    });

    detectScreenCheck.selectedProperty().addListener((obs, o, n) -> rematch(false, false));
    recursiveCheck.selectedProperty().addListener((obs, o, n) -> {
      if (sourceFolder != null) {
        loadFolder(sourceFolder);
      }
    });
    scoreSlider.valueProperty().addListener((obs, o, n) -> {
      scoreLabel.setText(Math.round(n.doubleValue() * 100) + "%");
      if (!scoreSlider.isValueChanging()) {
        rematch(false, false);
      }
    });
    scoreSlider.valueChangingProperty().addListener((obs, o, changing) -> {
      if (!changing) {
        rematch(false, false);
      }
    });

    sizeOffBtn.setUserData(0);
    sizeSmallBtn.setUserData(48);
    sizeMediumBtn.setUserData(96);
    sizeLargeBtn.setUserData(160);
    SegmentedToggles.keepSelected(sizeOffBtn, sizeSmallBtn, sizeMediumBtn, sizeLargeBtn);
    String savedSize = LocalUISettings.getString(PREVIEW_SIZE);
    for (Toggle toggle : previewSizeGroup.getToggles()) {
      if (String.valueOf(toggle.getUserData()).equals(savedSize)) {
        toggle.setSelected(true);
      }
    }
    previewSize = (Integer) previewSizeGroup.getSelectedToggle().getUserData();
    previewSizeGroup.selectedToggleProperty().addListener((obs, old, toggle) -> {
      if (toggle == null) {
        return;
      }
      previewSize = (Integer) toggle.getUserData();
      LocalUISettings.saveProperty(PREVIEW_SIZE, String.valueOf(previewSize));
      applyPreviewSize();
    });

    initFilterPills();
    initMoveSettings();
    initTable();
    applyPreviewSize();

    tableView.setOnDragOver((DragEvent e) -> {
      if (e.getDragboard().hasFiles()) {
        e.acceptTransferModes(TransferMode.COPY);
      }
      e.consume();
    });
    tableView.setOnDragDropped((DragEvent e) -> {
      List<File> files = e.getDragboard().getFiles();
      if (files != null && !files.isEmpty()) {
        File f = files.get(0);
        // finish the drop first, the modal scan dialog would otherwise block the drag source
        Platform.runLater(() -> loadFolder(f.isDirectory() ? f : f.getParentFile()));
        e.setDropCompleted(true);
      }
      e.consume();
    });

    // during a rematch the summary is updated once at the end instead of for every changed row
    items.addListener((javafx.collections.ListChangeListener<BulkImportItem>) c -> {
      if (!updating) {
        updateSummary();
      }
    });
    emulatorCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(GameEmulatorRepresentation emulator) {
        return emulator == null ? Messages.get("bulk_importer.emulator_all") : emulator.getName();
      }

      @Override
      public GameEmulatorRepresentation fromString(String string) {
        return null;
      }
    });
    emulatorCombo.setDisable(true);

    new Thread(() -> {
      // enabled emulators that are not hidden in the UI settings
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      List<GameEmulatorRepresentation> emulators = client.getEmulatorService().getFilteredEmulatorsWithoutAllVpx(uiSettings);
      List<GameRepresentation> games = new ArrayList<>();
      for (GameEmulatorRepresentation emulator : emulators) {
        games.addAll(client.getGameService().getGamesByEmulator(emulator.getId()));
      }
      games.sort(Comparator.comparing(g -> StringUtils.defaultString(g.getGameDisplayName()).toLowerCase()));
      Platform.runLater(() -> {
        loadedGames = games;
        initEmulatorFilter(emulators);
        applyEmulatorFilter();
        if (sourceFolder != null) {
          rematch(true, true);
        }
      });
    }, "bulk-importer-games").start();
  }

  private void initEmulatorFilter(List<GameEmulatorRepresentation> emulators) {
    List<GameEmulatorRepresentation> options = new ArrayList<>();
    options.add(null);
    options.addAll(emulators);
    emulatorCombo.setItems(FXCollections.observableArrayList(options));

    String savedId = LocalUISettings.getString(EMULATOR_ID);
    GameEmulatorRepresentation selection = emulators.stream()
        .filter(e -> String.valueOf(e.getId()).equals(savedId))
        .findFirst().orElse(null);
    emulatorCombo.setValue(selection);
    // there is nothing to choose with a single emulator
    emulatorCombo.setDisable(emulators.size() <= 1);

    emulatorCombo.valueProperty().addListener((obs, o, n) -> {
      LocalUISettings.saveProperty(EMULATOR_ID, n == null ? "" : String.valueOf(n.getId()));
      applyEmulatorFilter();
      // manually assigned tables of other emulators are dropped and matched again
      Set<Integer> gameIds = allGames.stream().map(GameRepresentation::getId).collect(Collectors.toSet());
      for (BulkImportItem item : items) {
        GameRepresentation game = item.gameProperty().get();
        if (game != null && !gameIds.contains(game.getId()) && SOURCE_MANUAL.equals(item.matchSourceProperty().get())) {
          item.matchSourceProperty().set(null);
        }
      }
      rematch(false, true);
    });
  }

  /**
   * Restricts the tables used for matching and for the table search to the selected emulator.
   */
  private void applyEmulatorFilter() {
    GameEmulatorRepresentation emulator = emulatorCombo.getValue();
    allGames = emulator == null ? loadedGames
        : loadedGames.stream().filter(g -> g.getEmulatorId() == emulator.getId()).collect(Collectors.toList());
    matcher = new BulkImportMatcher(allGames);
  }

  private void initMoveSettings() {
    moveCheck.setSelected(Boolean.parseBoolean(LocalUISettings.getString(MOVE_ENABLED)));
    moveTargetField.setText(StringUtils.defaultString(LocalUISettings.getString(MOVE_TARGET)));
    moveStructureGroup.selectToggle(getStructureToggle(loadMoveStructure()));
    SegmentedToggles.keepSelected(moveFlatBtn, moveKeepStructureBtn, moveTableFolderBtn);

    moveTargetField.disableProperty().bind(moveCheck.selectedProperty().not());
    moveTargetBtn.disableProperty().bind(moveCheck.selectedProperty().not());
    moveStructureBox.disableProperty().bind(moveCheck.selectedProperty().not());
    movePreviewLabel.disableProperty().bind(moveCheck.selectedProperty().not());

    moveCheck.selectedProperty().addListener((obs, o, n) -> LocalUISettings.saveProperty(MOVE_ENABLED, String.valueOf(n)));
    moveStructureGroup.selectedToggleProperty().addListener((obs, old, toggle) -> {
      if (toggle == null) {
        return;
      }
      LocalUISettings.saveProperty(MOVE_STRUCTURE, getMoveStructure().name());
      updateMovePreview();
    });
    moveTargetField.textProperty().addListener((obs, o, n) -> updateMovePreview());
    updateMovePreview();
  }

  /**
   * Shows where a file ends up with the chosen structure, using a file of the list as example.
   */
  private void updateMovePreview() {
    BulkImportItem sample = items.stream().filter(BulkImportItem::isImported).findFirst()
        .orElse(items.stream().filter(i -> i.gameProperty().get() != null).findFirst().orElse(null));
    String target = StringUtils.defaultIfBlank(moveTargetField.getText(), "…");
    String fileName = sample != null ? sample.getFile().getName() : Messages.get("bulk_importer.move_preview_file");
    String path;
    switch (getMoveStructure()) {
      case KEEP_STRUCTURE:
        path = sample != null ? sample.getRelativePath()
            : Messages.get("bulk_importer.move_preview_subfolder") + File.separator + fileName;
        break;
      case TABLE_FOLDER:
        String folder = sample != null ? BulkImportProgressModel.folderName(sample.gameProperty().get())
            : Messages.get("bulk_importer.move_preview_table");
        path = folder + File.separator + fileName;
        break;
      default:
        path = fileName;
    }
    String text = StringUtils.removeEnd(target, File.separator) + File.separator + path;
    movePreviewLabel.setText(text);
    movePreviewLabel.setTooltip(new Tooltip(text));
  }

  private BulkImportMode getDefaultMode() {
    Toggle toggle = defaultModeGroup.getSelectedToggle();
    return toggle != null ? (BulkImportMode) toggle.getUserData() : BulkImportMode.APPEND;
  }

  //---------------- review filter -----------------------------------------------------------------------------------

  private void initFilterPills() {
    addFilterPill(ReviewFilter.ALL, "bulk_importer.filter_all", null);
    addFilterPill(ReviewFilter.GOOD, "bulk_importer.filter_good", ThemeColors.OK);
    addFilterPill(ReviewFilter.CHECK, "bulk_importer.filter_check", ThemeColors.WARN);
    addFilterPill(ReviewFilter.NO_TABLE, "bulk_importer.filter_no_table", ThemeColors.ERROR);
    filterPills.get(ReviewFilter.ALL).setSelected(true);
    filterGroup.selectedToggleProperty().addListener((obs, o, n) -> {
      if (n != null) {
        ReviewFilter filter = (ReviewFilter) n.getUserData();
        filteredItems.setPredicate(filter::test);
      }
    });
  }

  private void addFilterPill(ReviewFilter filter, String messageKey, String dotColor) {
    ToggleButton pill = FilterPills.create(filterGroup, Messages.get(messageKey), dotColor, 0);
    pill.setUserData(filter);
    filterPills.put(filter, pill);
    filterBox.getChildren().add(pill);
  }

  private static BulkImportMoveStructure loadMoveStructure() {
    String value = LocalUISettings.getString(MOVE_STRUCTURE);
    if (StringUtils.isNotBlank(value)) {
      try {
        return BulkImportMoveStructure.valueOf(value);
      }
      catch (IllegalArgumentException e) {
        LOG.warn("Unknown bulk importer move structure \"{}\"", value);
      }
    }
    return Boolean.parseBoolean(LocalUISettings.getString(MOVE_KEEP_STRUCTURE)) ? BulkImportMoveStructure.KEEP_STRUCTURE : BulkImportMoveStructure.FLAT;
  }

  private ToggleButton getStructureToggle(BulkImportMoveStructure structure) {
    switch (structure) {
      case KEEP_STRUCTURE:
        return moveKeepStructureBtn;
      case TABLE_FOLDER:
        return moveTableFolderBtn;
      default:
        return moveFlatBtn;
    }
  }

  private BulkImportMoveStructure getMoveStructure() {
    if (moveKeepStructureBtn.isSelected()) {
      return BulkImportMoveStructure.KEEP_STRUCTURE;
    }
    if (moveTableFolderBtn.isSelected()) {
      return BulkImportMoveStructure.TABLE_FOLDER;
    }
    return BulkImportMoveStructure.FLAT;
  }

  private static StringConverter<BulkImportMode> modeConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(BulkImportMode mode) {
        return mode == null ? "" : Messages.get(mode.getMessageKey());
      }

      @Override
      public BulkImportMode fromString(String string) {
        return null;
      }
    };
  }

  private void initTable() {
    SortedList<BulkImportItem> sortedItems = new SortedList<>(filteredItems);
    sortedItems.comparatorProperty().bind(tableView.comparatorProperty());
    tableView.setItems(sortedItems);
    tableView.setPlaceholder(new Label(Messages.get("bulk_importer.placeholder")));
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    removeBtn.disableProperty().bind(javafx.beans.binding.Bindings.isEmpty(tableView.getSelectionModel().getSelectedItems()));

    // import checkboxes, the header checkbox toggles all complete rows
    selectAllCheck.setTooltip(new Tooltip(Messages.get("bulk_importer.select_all_tooltip")));
    selectAllCheck.setOnAction(e -> {
      boolean select = selectAllCheck.isSelected();
      for (BulkImportItem item : items) {
        if (item.isImportable()) {
          item.selectedProperty().set(select);
          item.setSelectionManual(true);
        }
      }
      tableView.refresh();
      updateSummary();
    });
    columnSelect.setGraphic(selectAllCheck);
    columnSelect.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    columnSelect.setCellFactory(c -> new TableCell<>() {
      private final CheckBox check = new CheckBox();

      {
        setAlignment(Pos.CENTER);
        check.setOnAction(e -> {
          BulkImportItem item = getItem();
          if (item == null) {
            return;
          }
          boolean select = check.isSelected();
          for (BulkImportItem target : affectedRows(item)) {
            if (target.isImportable()) {
              target.selectedProperty().set(select);
              target.setSelectionManual(true);
            }
          }
          Platform.runLater(() -> {
            tableView.refresh();
            updateSummary();
          });
        });
      }

      @Override
      protected void updateItem(BulkImportItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          return;
        }
        check.setSelected(item.isImported());
        check.setDisable(!item.isImportable());
        setGraphic(check);
      }
    });
    tableView.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<BulkImportItem>) c -> {
      if (!updating) {
        updateSummary();
      }
    });

    columnPreview.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    columnPreview.setCellFactory(c -> new TableCell<>() {
      {
        setAlignment(Pos.CENTER);
      }

      @Override
      protected void updateItem(BulkImportItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          return;
        }
        setGraphic(previewSize > 0 ? createPreview(item.getFile()) : null);
      }
    });

    columnFile.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    columnFile.setComparator(Comparator.comparing(BulkImportItem::getRelativePath, String.CASE_INSENSITIVE_ORDER));
    columnFile.setCellFactory(c -> new TableCell<>() {
      private final Label name = new Label();
      private final Label folder = new Label();
      private final VBox box = new VBox(2, name, folder);

      {
        setAlignment(Pos.CENTER_LEFT);
        name.getStyleClass().addAll("sm-text", "sm-mono");
        folder.getStyleClass().add("sm-small");
        box.setAlignment(Pos.CENTER_LEFT);
      }

      @Override
      protected void updateItem(BulkImportItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          setTooltip(null);
          return;
        }
        name.setText(item.getFile().getName());
        String parent = new File(item.getRelativePath()).getParent();
        folder.setText(parent != null ? parent + File.separator : "");
        folder.setVisible(parent != null);
        folder.setManaged(parent != null);
        setTooltip(new Tooltip(item.getFile().getAbsolutePath()));
        setGraphic(box);
      }
    });

    columnTable.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    columnTable.setCellFactory(c -> new TableMatchCell());

    columnScore.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    columnScore.setComparator(Comparator.comparingDouble(i -> i.scoreProperty().get()));
    // match bar + percentage: green = good match, orange = check, red = no table
    columnScore.setCellFactory(c -> new TableCell<>() {
      private final Region fill = new Region();
      private final StackPane track = new StackPane(fill);
      private final Label value = new Label();
      private final HBox box = new HBox(10, track, value);
      private final Tooltip tooltip = new Tooltip();

      {
        setAlignment(Pos.CENTER_LEFT);
        track.setAlignment(Pos.CENTER_LEFT);
        track.setMinSize(MATCH_BAR_WIDTH, 6);
        track.setMaxSize(MATCH_BAR_WIDTH, 6);
        track.setStyle("-fx-background-color: " + ThemeColors.INSET + "; -fx-background-radius: 3;");
        fill.setMaxHeight(6);
        value.getStyleClass().addAll("sm-text-bold", "sm-mono");
        box.setAlignment(Pos.CENTER_LEFT);
      }

      @Override
      protected void updateItem(BulkImportItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          setTooltip(null);
          return;
        }
        String color;
        double score;
        if (item.gameProperty().get() == null) {
          color = ThemeColors.ERROR;
          score = 0;
          value.setText("–");
          tooltip.setText(Messages.get("bulk_importer.no_match"));
        }
        else if (SOURCE_MANUAL.equals(item.matchSourceProperty().get())) {
          color = ThemeColors.OK;
          score = 1;
          value.setText(Messages.get("bulk_importer.manual"));
          tooltip.setText(Messages.get("bulk_importer.manual"));
        }
        else {
          score = item.scoreProperty().get();
          color = score >= GOOD_SCORE ? ThemeColors.OK : ThemeColors.WARN;
          value.setText(Math.round(score * 100) + "%");
          tooltip.setText(Messages.get("bulk_importer.matched_by_" + item.matchSourceProperty().get()));
        }
        double width = Math.max(0, Math.min(1, score)) * MATCH_BAR_WIDTH;
        fill.setMinWidth(width);
        fill.setMaxWidth(width);
        fill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 3;");
        value.setStyle("-fx-text-fill: " + color + ";");
        setTooltip(tooltip);
        setGraphic(box);
      }
    });

    columnScreen.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    // the cells keep their controls, scrolling only updates the values
    columnScreen.setCellFactory(c -> new TableCell<>() {
      private final ComboBox<VPinScreen> combo = new ComboBox<>(FXCollections.observableArrayList(screenOptions));
      private final Tooltip tooltip = new Tooltip();

      {
        setAlignment(Pos.CENTER_LEFT);
        combo.setConverter(screenConverter);
        combo.setPromptText(Messages.get("bulk_importer.choose_screen"));
        combo.setTooltip(tooltip);
        combo.setOnAction(e -> {
          BulkImportItem item = getItem();
          VPinScreen value = combo.getValue();
          if (item == null || value == item.screenProperty().get()) {
            return; // programmatic refresh, nothing changed
          }
          for (BulkImportItem target : affectedRows(item)) {
            target.screenProperty().set(value);
            target.screenDetectedProperty().set(false);
            updateImportSelection(target);
          }
          Platform.runLater(() -> {
            tableView.refresh();
            updateSummary();
          });
        });
      }

      @Override
      protected void updateItem(BulkImportItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          return;
        }
        combo.setValue(item.screenProperty().get());
        tooltip.setText(Messages.get(item.screenDetectedProperty().get() ? "bulk_importer.screen_detected" : "bulk_importer.screen_default"));
        setGraphic(combo);
      }
    });

    columnMode.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    columnMode.setCellFactory(c -> new TableCell<>() {
      private final ComboBox<BulkImportMode> combo = new ComboBox<>(FXCollections.observableArrayList(BulkImportMode.values()));

      {
        setAlignment(Pos.CENTER_LEFT);
        combo.setConverter(modeConverter());
        combo.setOnAction(e -> {
          BulkImportItem item = getItem();
          BulkImportMode value = combo.getValue();
          if (item == null || value == null || value == item.modeProperty().get()) {
            return;
          }
          for (BulkImportItem target : affectedRows(item)) {
            target.modeProperty().set(value);
          }
          Platform.runLater(() -> tableView.refresh());
        });
      }

      @Override
      protected void updateItem(BulkImportItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          return;
        }
        combo.setValue(item.modeProperty().get());
        setGraphic(combo);
      }
    });
  }

  private Node createPreview(File file) {
    String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
    StackPane pane = new StackPane();
    pane.setMinSize(previewSize, previewSize);
    pane.setPrefSize(previewSize, previewSize);
    pane.setMaxSize(previewSize, previewSize);
    if (IMAGE_EXT.contains(ext)) {
      // background loading, scaled down on decode so big files do not exhaust memory;
      // decoded once for the largest preview size so switching the size needs no reload
      int decodeSize = (Integer) sizeLargeBtn.getUserData() * 2;
      Image image = previewCache.computeIfAbsent(file, f -> new Image(f.toURI().toString(), decodeSize, decodeSize, true, true, true));
      ImageView view = new ImageView(image);
      view.setFitWidth(previewSize);
      view.setFitHeight(previewSize);
      view.setPreserveRatio(true);
      pane.getChildren().add(view);
    }
    else {
      // file type badge, the format icon only from the medium size on
      Label badge = new Label(ext.toUpperCase());
      badge.getStyleClass().add("am-format-badge");
      badge.setMinSize(Math.max(46, previewSize * 0.7), Math.max(24, previewSize * 0.45));
      badge.setMaxSize(Math.max(46, previewSize * 0.7), Math.max(24, previewSize * 0.45));
      if (previewSize >= 96) {
        String icon = VIDEO_EXT.contains(ext) ? "mdi2v-video-outline" : AUDIO_EXT.contains(ext) ? "mdi2m-music-note" : "mdi2f-file-outline";
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(18);
        ThemeColors.setIconColor(fontIcon, ThemeColors.TEXT_LIGHT);
        badge.setGraphic(fontIcon);
        badge.setContentDisplay(ContentDisplay.TOP);
      }
      pane.getChildren().add(badge);
    }
    return pane;
  }

  private void applyPreviewSize() {
    boolean show = previewSize > 0;
    columnPreview.setVisible(show);
    // two text lines in the file column and the 30px combo boxes need at least 52px
    tableView.setFixedCellSize(show ? Math.max(52, previewSize + 12) : 52);
    columnPreview.setPrefWidth(previewSize + 24);
    columnPreview.setMinWidth(previewSize + 24);
    columnPreview.setMaxWidth(previewSize + 24);
    tableView.refresh();
  }


  //------------------------------------------------------------------------------------------------------------------

  @FXML
  private void onBrowse() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle(Messages.get("bulk_importer.select_folder"));
    if (sourceFolder != null) {
      chooser.setInitialDirectory(sourceFolder);
    }
    File folder = chooser.showOpenDialog(getStage());
    if (folder != null) {
      loadFolder(folder);
    }
  }

  @FXML
  private void onMoveTargetBrowse() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle(Messages.get("bulk_importer.move_target_select"));
    File current = getMoveTarget();
    if (current != null && current.isDirectory()) {
      chooser.setInitialDirectory(current);
    }
    File folder = chooser.showOpenDialog(getStage());
    if (folder != null) {
      moveTargetField.setText(folder.getAbsolutePath());
      LocalUISettings.saveProperty(MOVE_TARGET, folder.getAbsolutePath());
    }
  }

  private File getMoveTarget() {
    return StringUtils.isBlank(moveTargetField.getText()) ? null : new File(moveTargetField.getText());
  }

  public void setSourceFolder(File folder) {
    // the progress dialog needs the importer dialog showing as its owner
    Platform.runLater(() -> loadFolder(folder));
  }

  private void loadFolder(File folder) {
    if (folder == null || !folder.isDirectory()) {
      return;
    }
    Set<String> extensions = new HashSet<>(IMAGE_EXT);
    extensions.addAll(VIDEO_EXT);
    extensions.addAll(AUDIO_EXT);
    BulkImportScanProgressModel model = new BulkImportScanProgressModel(folder, recursiveCheck.isSelected(), extensions,
        matcher, matchModeCombo.getValue(), SUGGESTION_COUNT);
    scanning = true;
    try {
      ProgressDialog.createProgressDialog(getStage(), model);
    }
    finally {
      scanning = false;
    }
    if (model.isCancelled()) {
      return; // keep the previous file list
    }
    sourceFolder = folder;
    sourceField.setText(folder.getAbsolutePath());
    items.setAll(model.getItems());
    // the games may have been loaded only while the folder was scanned
    rematch(true, !model.isMatched());
  }

  @FXML
  private void onRematch() {
    rematch(true, true);
  }

  /**
   * Runs the matching for all items. Manually corrected rows are kept unless force is set.
   * The table suggestions are recomputed in a progress dialog when recompute is set, otherwise the existing ones
   * are applied with the current threshold and screen settings.
   */
  private void rematch(boolean force, boolean recompute) {
    if (matcher == null || updating || scanning) {
      return;
    }
    if (recompute) {
      List<BulkImportItem> targets = items.stream()
          .filter(i -> force || !SOURCE_MANUAL.equals(i.matchSourceProperty().get()))
          .collect(Collectors.toList());
      if (!targets.isEmpty()) {
        ProgressDialog.createProgressDialog(getStage(),
            new BulkImportMatchProgressModel(matcher, targets, sourceFolder, matchModeCombo.getValue(), SUGGESTION_COUNT));
      }
    }
    updating = true;
    try {
      double threshold = scoreSlider.getValue();
      boolean detect = detectScreenCheck.isSelected();
      VPinScreen defaultScreen = defaultScreenCombo.getValue();
      for (BulkImportItem item : items) {
        boolean manual = SOURCE_MANUAL.equals(item.matchSourceProperty().get());
        if (!manual || force) {
          List<BulkImportMatcher.Match> suggestions = item.getSuggestions();
          if (!suggestions.isEmpty() && suggestions.get(0).score >= threshold) {
            BulkImportMatcher.Match best = suggestions.get(0);
            item.gameProperty().set(best.game);
            item.scoreProperty().set(best.score);
            item.matchSourceProperty().set(best.source);
          }
          else {
            item.gameProperty().set(null);
            item.scoreProperty().set(suggestions.isEmpty() ? 0 : suggestions.get(0).score);
            item.matchSourceProperty().set(suggestions.isEmpty() ? "filename" : suggestions.get(0).source);
          }
        }

        if (force || item.screenProperty().get() == null || item.screenDetectedProperty().get()) {
          VPinScreen screen = detect ? BulkImportMatcher.detectScreen(item.getFile(), sourceFolder) : null;
          item.screenDetectedProperty().set(screen != null);
          item.screenProperty().set(screen != null ? screen : defaultScreen);
        }
        if (force) {
          item.modeProperty().set(getDefaultMode());
          item.setSelectionManual(false);
        }
        updateImportSelection(item);
      }
    }
    finally {
      updating = false;
    }
    tableView.refresh();
    updateSummary();
  }

  /**
   * Checks complete rows and unchecks incomplete ones, unless the user decided about the row.
   */
  private void updateImportSelection(BulkImportItem item) {
    if (!item.isImportable()) {
      item.selectedProperty().set(false);
    }
    else if (!item.isSelectionManual()) {
      item.selectedProperty().set(true);
    }
  }

  private void updateSummary() {
    long matched = items.stream().filter(i -> i.gameProperty().get() != null).count();
    long importable = items.stream().filter(BulkImportItem::isImportable).count();
    long selected = items.stream().filter(BulkImportItem::isImported).count();
    long noTable = items.size() - matched;
    long noScreen = items.stream().filter(i -> i.gameProperty().get() != null && i.screenProperty().get() == null).count();

    summaryLabel.setText(items.isEmpty() ? "" : Messages.get("bulk_importer.summary", items.size(), selected));
    List<String> skipped = new ArrayList<>();
    if (noTable > 0) {
      skipped.add(Messages.get("bulk_importer.summary_no_table", noTable));
    }
    if (noScreen > 0) {
      skipped.add(Messages.get("bulk_importer.summary_no_screen", noScreen));
    }
    skipLabel.setText(String.join("  ", skipped));
    skipLabel.setStyle("-fx-text-fill: " + ThemeColors.WARN + ";");

    importBtn.setDisable(selected == 0);
    importBtn.setText(selected > 0 ? Messages.get("bulk_importer.import_count", selected) : Messages.get("bulk_importer.import"));
    removeUnmatchedBtn.setDisable(matcher == null || noTable == 0);

    for (Map.Entry<ReviewFilter, ToggleButton> pill : filterPills.entrySet()) {
      FilterPills.setCount(pill.getValue(), items.stream().filter(pill.getKey()::test).count());
    }

    selectAllCheck.setDisable(importable == 0);
    selectAllCheck.setIndeterminate(selected > 0 && selected < importable);
    selectAllCheck.setSelected(importable > 0 && selected == importable);
    updateMovePreview();
  }

  //---------------- bulk actions ------------------------------------------------------------------------------------

  private List<BulkImportItem> targets() {
    return new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
  }

  /**
   * Rows that follow an edit in a screen or mode cell: all selected rows when the edited row is one of them,
   * otherwise only the edited row.
   */
  private List<BulkImportItem> affectedRows(BulkImportItem edited) {
    List<BulkImportItem> selection = targets();
    return selection.contains(edited) ? selection : Collections.singletonList(edited);
  }

  @FXML
  private void onRemove() {
    List<BulkImportItem> selection = targets();
    int index = tableView.getSelectionModel().getSelectedIndex();
    items.removeAll(selection);
    int rows = tableView.getItems().size();
    if (rows > 0) {
      tableView.getSelectionModel().clearAndSelect(Math.max(0, Math.min(index, rows - 1)));
    }
    updateSummary();
  }

  @FXML
  private void onRemoveUnmatched() {
    items.removeIf(i -> i.gameProperty().get() == null);
    updateSummary();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    getStage().close();
  }

  @FXML
  private void onImport() {
    List<BulkImportItem> selection = items.stream().filter(BulkImportItem::isImported).collect(Collectors.toList());
    if (selection.isEmpty()) {
      return;
    }
    File moveTarget = moveCheck.isSelected() ? getMoveTarget() : null;
    if (moveCheck.isSelected() && moveTarget == null) {
      WidgetFactory.showAlert(getStage(), Messages.get("bulk_importer.move_target_missing"));
      return;
    }
    BulkImportMoveStructure structure = getMoveStructure();
    long overwrites = selection.stream().filter(i -> i.modeProperty().get() == BulkImportMode.OVERWRITE).count();
    if (overwrites > 0) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(getStage(), Messages.get("bulk_importer.confirm_overwrite", overwrites, selection.size()));
      if (result.isEmpty() || result.get() != ButtonType.OK) {
        return;
      }
    }
    logImportParameters(selection, moveTarget, structure, overwrites);
    // the table folders are matched against all tables, so a folder is not used for a similar named table
    BulkImportMatcher folderMatcher = matcher;
    double minScore = scoreSlider.getValue();
    Stage stage = getStage();
    stage.close();
    Platform.runLater(() -> ProgressDialog.createProgressDialog(new BulkImportProgressModel(selection, moveTarget, structure, folderMatcher, minScore)));
  }

  private void logImportParameters(List<BulkImportItem> selection, File moveTarget, BulkImportMoveStructure structure, long overwrites) {
    GameEmulatorRepresentation emulator = emulatorCombo.getValue();
    LOG.info("Bulk import started: sourceFolder=\"{}\", recursive={}, emulator={}, matchMode={}, minScore={}%, "
            + "detectScreen={}, defaultScreen={}, defaultMode={}, moveFiles={}, moveTarget=\"{}\", moveStructure={}, "
            + "files={}/{}, overwrites={}",
        sourceFolder != null ? sourceFolder.getAbsolutePath() : null,
        recursiveCheck.isSelected(),
        emulator != null ? emulator.getName() + " (" + emulator.getId() + ")" : "all",
        matchModeCombo.getValue(),
        Math.round(scoreSlider.getValue() * 100),
        detectScreenCheck.isSelected(),
        defaultScreenCombo.getValue(),
        getDefaultMode(),
        moveCheck.isSelected(),
        moveTarget != null ? moveTarget.getAbsolutePath() : null,
        structure,
        selection.size(), items.size(),
        overwrites);
    for (BulkImportItem item : selection) {
      GameRepresentation game = item.gameProperty().get();
      LOG.debug("Bulk import item: file=\"{}\", game=\"{}\" ({}), screen={}, mode={}, match={}, score={}%",
          item.getFile().getAbsolutePath(),
          game != null ? game.getGameDisplayName() : null,
          game != null ? game.getId() : null,
          item.screenProperty().get(),
          item.modeProperty().get(),
          item.matchSourceProperty().get(),
          Math.round(item.scoreProperty().get() * 100));
    }
  }

  private Stage getStage() {
    return (Stage) tableView.getScene().getWindow();
  }

  @Override
  public void onDialogCancel() {
  }

  //------------------------------------------------------------------------------------------------------------------

  /**
   * Table match editor: suggestions in a combo box plus a button to search all tables.
   */
  private class TableMatchCell extends TableCell<BulkImportItem, BulkImportItem> {
    private final ComboBox<GameRepresentation> combo = new ComboBox<>();
    private final HBox box;
    private boolean refreshing = false;

    TableMatchCell() {
      setAlignment(Pos.CENTER_LEFT);
      combo.setMaxWidth(Double.MAX_VALUE);
      combo.setPromptText(Messages.get("bulk_importer.no_match"));
      combo.setOnAction(e -> {
        BulkImportItem item = getItem();
        // replacing the options of a reused cell may select another table for a moment
        if (!refreshing && item != null && combo.getValue() != null && combo.getValue() != item.gameProperty().get()) {
          assign(item, combo.getValue());
        }
      });

      Button search = new Button();
      FontIcon searchIcon = new FontIcon("mdi2m-magnify");
      searchIcon.setIconSize(18);
      ThemeColors.setIconColor(searchIcon, ThemeColors.WHITE);
      search.setGraphic(searchIcon);
      search.setTooltip(new Tooltip(Messages.get("bulk_importer.search_table")));
      search.setOnAction(e -> {
        BulkImportItem item = getItem();
        GameRepresentation game = item != null ? chooseGame() : null;
        if (game != null) {
          assign(item, game);
        }
      });

      box = new HBox(4, combo, search);
      box.setAlignment(Pos.CENTER_LEFT);
      HBox.setHgrow(combo, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(BulkImportItem item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setGraphic(null);
        return;
      }
      LinkedHashSet<GameRepresentation> options = new LinkedHashSet<>();
      if (item.gameProperty().get() != null) {
        options.add(item.gameProperty().get());
      }
      item.getSuggestions().forEach(m -> options.add(m.game));
      refreshing = true;
      try {
        combo.getItems().setAll(options);
        combo.setValue(item.gameProperty().get());
      }
      finally {
        refreshing = false;
      }
      setGraphic(box);
    }

    private void assign(BulkImportItem item, GameRepresentation game) {
      item.gameProperty().set(game);
      item.matchSourceProperty().set(SOURCE_MANUAL);
      item.scoreProperty().set(1.0);
      // choosing a table means the file should be imported
      item.setSelectionManual(false);
      updateImportSelection(item);
      tableView.refresh();
      updateSummary();
    }
  }

  private GameRepresentation chooseGame() {
    Stage stage = Dialogs.createStudioDialogStage(BulkImporterTableSearchController.class, "dialog-bulk-importer-table-search.fxml", Messages.get("bulk_importer.search_table"));
    BulkImporterTableSearchController controller = (BulkImporterTableSearchController) stage.getUserData();
    controller.setGames(allGames);
    stage.showAndWait();
    return controller.getResult();
  }
}
