package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportItem;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMatchProgressModel;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMatcher;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMatcher.MatchMode;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportProgressModel;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportScanProgressModel;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

  @FXML
  private TextField sourceField;
  @FXML
  private CheckBox recursiveCheck;
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
  private ComboBox<Boolean> defaultModeCombo;
  @FXML
  private CheckBox previewCheck;
  @FXML
  private ToggleGroup previewSizeGroup;
  @FXML
  private ToggleButton sizeSmallBtn;
  @FXML
  private ToggleButton sizeMediumBtn;
  @FXML
  private ToggleButton sizeLargeBtn;
  @FXML
  private TableView<BulkImportItem> tableView;
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
  private Label summaryLabel;
  @FXML
  private Button importBtn;
  @FXML
  private Button removeBtn;
  @FXML
  private Button removeUnmatchedBtn;

  private final ObservableList<BulkImportItem> items = FXCollections.observableArrayList(
      item -> new javafx.beans.Observable[]{item.gameProperty(), item.screenProperty()});

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
        return screen == null ? "" : screen.getSegment();
      }

      @Override
      public VPinScreen fromString(String string) {
        return null;
      }
    };
    List<VPinScreen> screens = new ArrayList<>();
    screens.add(null);
    screens.addAll(Arrays.asList(VPinScreen.values()));
    defaultScreenCombo.setItems(FXCollections.observableArrayList(screens));
    defaultScreenCombo.setConverter(screenConverter);
    defaultScreenCombo.valueProperty().addListener((obs, o, n) -> rematch(false, false));
    screenOptions = screens;
    this.screenConverter = screenConverter;

    defaultModeCombo.setItems(FXCollections.observableArrayList(false, true));
    defaultModeCombo.setConverter(modeConverter());
    defaultModeCombo.setValue(true);

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

    sizeSmallBtn.setUserData(48);
    sizeMediumBtn.setUserData(96);
    sizeLargeBtn.setUserData(160);
    previewSizeGroup.selectedToggleProperty().addListener((obs, old, toggle) -> {
      if (toggle == null) {
        old.setSelected(true); // one size is always active
        return;
      }
      previewSize = (Integer) toggle.getUserData();
      applyPreviewSize();
    });

    previewCheck.selectedProperty().addListener((obs, o, n) -> applyPreviewSize());
    sizeSmallBtn.disableProperty().bind(previewCheck.selectedProperty().not());
    sizeMediumBtn.disableProperty().bind(previewCheck.selectedProperty().not());
    sizeLargeBtn.disableProperty().bind(previewCheck.selectedProperty().not());

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
    new Thread(() -> {
      List<GameRepresentation> games = new ArrayList<>(client.getGameService().getVpxGamesCached());
      games.addAll(client.getGameService().getFPGamesCached());
      games.sort(Comparator.comparing(g -> StringUtils.defaultString(g.getGameDisplayName()).toLowerCase()));
      Platform.runLater(() -> {
        allGames = games;
        matcher = new BulkImportMatcher(games);
        if (sourceFolder != null) {
          rematch(true, true);
        }
      });
    }, "bulk-importer-games").start();
  }

  private static StringConverter<Boolean> modeConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(Boolean append) {
        return append == null ? "" : Messages.get(append ? "bulk_importer.mode_append" : "bulk_importer.mode_overwrite");
      }

      @Override
      public Boolean fromString(String string) {
        return null;
      }
    };
  }

  private void initTable() {
    tableView.setItems(items);
    tableView.setPlaceholder(new Label(Messages.get("bulk_importer.placeholder")));
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    removeBtn.disableProperty().bind(javafx.beans.binding.Bindings.isEmpty(tableView.getSelectionModel().getSelectedItems()));
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
        setGraphic(createPreview(item.getFile()));
      }
    });

    columnFile.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    columnFile.setComparator(Comparator.comparing(BulkImportItem::getRelativePath, String.CASE_INSENSITIVE_ORDER));
    columnFile.setCellFactory(c -> new TableCell<>() {
      {
        setAlignment(Pos.CENTER_LEFT);
      }

      @Override
      protected void updateItem(BulkImportItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          setTooltip(null);
          return;
        }
        setText(item.getRelativePath());
        setTooltip(new Tooltip(item.getFile().getAbsolutePath()));
      }
    });

    columnTable.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    columnTable.setCellFactory(c -> new TableMatchCell());

    columnScore.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
    columnScore.setComparator(Comparator.comparingDouble(i -> i.scoreProperty().get()));
    columnScore.setCellFactory(c -> new TableCell<>() {
      {
        setAlignment(Pos.CENTER_LEFT);
      }

      @Override
      protected void updateItem(BulkImportItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          return;
        }
        if (item.gameProperty().get() == null) {
          setText("-");
          setStyle("-fx-text-fill: #FF9900;");
        }
        else if (SOURCE_MANUAL.equals(item.matchSourceProperty().get())) {
          setText(Messages.get("bulk_importer.manual"));
          setStyle("-fx-text-fill: #66FF66;");
        }
        else {
          int pct = (int) Math.round(item.scoreProperty().get() * 100);
          setText(pct + "% (" + Messages.get("bulk_importer.matched_by_" + item.matchSourceProperty().get()) + ")");
          setStyle(pct >= 80 ? "-fx-text-fill: #66FF66;" : "-fx-text-fill: #FFFF66;");
        }
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
      private final ComboBox<Boolean> combo = new ComboBox<>(FXCollections.observableArrayList(false, true));

      {
        setAlignment(Pos.CENTER_LEFT);
        combo.setConverter(modeConverter());
        combo.setOnAction(e -> {
          BulkImportItem item = getItem();
          Boolean value = combo.getValue();
          if (item == null || value == null || value == item.appendProperty().get()) {
            return;
          }
          for (BulkImportItem target : affectedRows(item)) {
            target.appendProperty().set(value);
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
        combo.setValue(item.appendProperty().get());
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
      String icon = VIDEO_EXT.contains(ext) ? "mdi2v-video" : AUDIO_EXT.contains(ext) ? "mdi2m-music-note" : "mdi2f-file-outline";
      FontIcon fontIcon = new FontIcon(icon);
      fontIcon.setIconColor(javafx.scene.paint.Color.WHITE);
      fontIcon.setIconSize(Math.max(24, previewSize / 2));
      Label label = new Label(ext.toUpperCase());
      label.setStyle("-fx-text-fill: #CCCCCC;");
      javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(4, fontIcon, label);
      box.setAlignment(Pos.CENTER);
      pane.getChildren().add(box);
    }
    return pane;
  }

  private void applyPreviewSize() {
    boolean show = previewCheck.isSelected();
    columnPreview.setVisible(show);
    // without previews the rows fall back to the stylesheet's default row height
    tableView.setFixedCellSize(show ? previewSize + 14 : -1);
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
          item.appendProperty().set(Boolean.TRUE.equals(defaultModeCombo.getValue()));
        }
      }
      selectImportable();
    }
    finally {
      updating = false;
    }
    tableView.refresh();
    updateSummary();
  }

  private void updateSummary() {
    long matched = items.stream().filter(i -> i.gameProperty().get() != null).count();
    long selected = tableView.getSelectionModel().getSelectedItems().stream().filter(BulkImportItem::isImportable).count();
    long noScreen = items.stream().filter(i -> i.gameProperty().get() != null && i.screenProperty().get() == null).count();
    summaryLabel.setText(Messages.get("bulk_importer.summary", items.size(), matched, selected)
        + (noScreen > 0 ? "  " + Messages.get("bulk_importer.summary_no_screen", noScreen) : ""));
    importBtn.setDisable(items.isEmpty() || !items.stream().allMatch(BulkImportItem::isImportable));
    removeUnmatchedBtn.setDisable(matcher == null || matched == items.size());
  }

  //---------------- bulk actions ------------------------------------------------------------------------------------

  private List<BulkImportItem> targets() {
    return new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
  }

  private void selectImportable() {
    TableView.TableViewSelectionModel<BulkImportItem> model = tableView.getSelectionModel();
    model.clearSelection();
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).isImportable()) {
        model.select(i);
      }
    }
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
    if (!items.isEmpty()) {
      tableView.getSelectionModel().select(Math.min(index, items.size() - 1));
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
    List<BulkImportItem> selection = new ArrayList<>(items);
    if (selection.isEmpty() || !selection.stream().allMatch(BulkImportItem::isImportable)) {
      return;
    }
    long overwrites = selection.stream().filter(i -> !i.appendProperty().get()).count();
    if (overwrites > 0) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(getStage(), Messages.get("bulk_importer.confirm_overwrite", overwrites, selection.size()));
      if (result.isEmpty() || result.get() != ButtonType.OK) {
        return;
      }
    }
    Stage stage = getStage();
    stage.close();
    Platform.runLater(() -> ProgressDialog.createProgressDialog(new BulkImportProgressModel(selection)));
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
      searchIcon.setIconColor(javafx.scene.paint.Color.WHITE);
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
