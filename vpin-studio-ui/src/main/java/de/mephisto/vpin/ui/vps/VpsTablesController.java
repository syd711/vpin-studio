package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlay;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;
import de.mephisto.vpin.ui.tables.vps.VpsDBDownloadProgressModel;
import de.mephisto.vpin.ui.tables.vps.VpsTableColumn;
import de.mephisto.vpin.ui.util.JFXFuture;
import de.mephisto.vpin.ui.util.Keys;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import static de.mephisto.vpin.ui.Studio.client;

public class VpsTablesController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(VpsTablesController.class);

  private final static List<VpsTableFormat> TABLE_FORMATS = new ArrayList<>();

  static {

    TABLE_FORMATS.add(new VpsTableFormat("VPX", "Visual Pinball X"));
    TABLE_FORMATS.add(new VpsTableFormat("VP9", "Visual Pinball 9"));
    TABLE_FORMATS.add(new VpsTableFormat("FP", "Future Pinball"));
    TABLE_FORMATS.add(new VpsTableFormat("FX", "Pinball FX"));
    TABLE_FORMATS.add(new VpsTableFormat("FX2", "Pinball FX2"));
    TABLE_FORMATS.add(new VpsTableFormat("FX3", "Pinball FX3"));
  }


  @FXML
  private TextField searchTextField;

  @FXML
  private TableView<VpsTableModel> tableView;

  @FXML
  private ComboBox<VpsTableFormat> emulatorCombo;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> installedColumn;

  @FXML
  TableColumn<VpsTableModel, String> nameColumn;

  @FXML
  TableColumn<VpsTableModel, Integer> versionsColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> statusColumn;

  @FXML
  TableColumn<VpsTableModel, Object> directB2SColumn;

  @FXML
  TableColumn<VpsTableModel, Object> pupPackColumn;

  @FXML
  TableColumn<VpsTableModel, Object> romColumn;

  @FXML
  TableColumn<VpsTableModel, Object> topperColumn;

  @FXML
  TableColumn<VpsTableModel, Object> povColumn;

  @FXML
  TableColumn<VpsTableModel, Object> altSoundColumn;

  @FXML
  TableColumn<VpsTableModel, Object> altColorColumn;

  @FXML
  TableColumn<VpsTableModel, Object> tutorialColumn;

  @FXML
  TableColumn<VpsTableModel, String> updatedColumn;

  @FXML
  private StackPane loaderStack;

  @FXML
  private StackPane tableStack;

  @FXML
  private Button openBtn;

  @FXML
  private Button editBtn;

  @FXML
  private Label countLabel;

  //--------------- Filters

  @FXML
  private Button filterButton;

  private VpsTablesFilterController vpsTablesFilterController;


  //--------------- Sorter

  private VpsTablesColumnSorter vpsTablesColumnSorter; 

  //------------------------  

  private ObservableList<VpsTableModel> vpsTables;

  private FilteredList<VpsTableModel> data;

  private WaitOverlay loadingOverlay;

  private TablesController tablesController;

  // calculated global KPIs on vpsTables
  private int unmapped = 0;

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";

  // Add a public no-args constructor
  public VpsTablesController() {
  }

  @FXML
  private void onTableEdit() {
    VpsTableModel model = getSelection();
    if (model != null) {
      GameRepresentation game = client.getGameService().getGameByVpsTable(model.getVpsTable(), null);
      tablesController.getTableOverviewController().setSelection(game);
    }
  }

  @FXML
  private void onOpen() {
    VpsTableModel model = getSelection();
    if (model != null) {
      Studio.browse(VPS.getVpsTableUrl(model.getVpsTable().getId()));
    }
  }

  @FXML
  private void onFilter() {
    vpsTablesFilterController.toggle();
  }

  /**
   * @return true if the filtered list did change and reload is required
   */
  public synchronized void applyFilters() {
    // mind that it can be called by threads before data is even initialized
    if (this.data != null) {
      VpsFilterSettings filters = vpsTablesFilterController.getFilterSettings();

      boolean noVPX = client.getFrontendService().getVpxGameEmulators().isEmpty();
      this.data.setPredicate(filters.buildPredicate(noVPX));

      // Now update counters
      int installed = 0;
      for (VpsTableModel model : this.data) {
        if (model.isInstalled()) {
          installed++;
        }
      }    
      countLabel.setText(data.size() + " tables / " + installed + " installed / " + unmapped + " not mapped");
    }
  }

  @FXML
  public void onReload() {
    doReload(true);
  }

  public void doReload(boolean forceReload) {
    this.searchTextField.setDisable(true);
    loadingOverlay.show();
    VpsTableModel selection = tableView.getSelectionModel().getSelectedItem();

    if (forceReload) {
      ProgressDialog.createProgressDialog(new VpsDBDownloadProgressModel("Download VPS Database", Arrays.asList(new File("<vpsdb.json>"))));
    }

    // calculate installed and unmapped in a non blocking thread
     JFXFuture.runAsync(()-> {
      this.unmapped = 0;
      List<GameRepresentation> gamesCached = client.getGameService().getVpxGamesCached();
      for (GameRepresentation gameRepresentation : gamesCached) {
        if (StringUtils.isEmpty(gameRepresentation.getExtTableId())) {
          unmapped++;
        }
      }
    }).thenLater(() -> {
      applyFilters();
    });

    //-----------
    // load tables in parallel
    JFXFuture.runAsync(()-> {
      // get all tables and filter by format
      VpsTableFormat value = emulatorCombo.getValue();
      String tableFormat = value.getAbbrev();

      List<VpsTable> _vpsTables = client.getVpsService().getTables();
      vpsTables = FXCollections.observableList(_vpsTables.stream()
        .filter(t -> t.getAvailableTableFormats().contains(tableFormat))
        .map(t -> new VpsTableModel(t))
        .collect(Collectors.toList())
      );

      VpsTableModel.loadAllThenLater(vpsTables, () -> {
        applyFilters();
      });

    }).thenLater(() -> {

      this.data = new FilteredList<>(vpsTables);
  
      // Wrap the FilteredList in a SortedList
      SortedList<VpsTableModel> sortedData = new SortedList<>(this.data);
      // Bind the SortedList comparator to the TableView comparator.
      sortedData.comparatorProperty().bind(Bindings.createObjectBinding(
          () -> vpsTablesColumnSorter .buildComparator(tableView),
          tableView.comparatorProperty()));
      // Set a dummy SortPolicy to tell the TableView data is successfully sorted
      tableView.setSortPolicy(tableView -> true);

      // Set the items in the TableView
      tableView.setItems(sortedData);

      // filter the list and refresh number of items
      applyFilters();

      // reapply selection
      if (selection != null && data.contains(selection)) {
        tableView.getSelectionModel().select(selection);
      }
      else {
        tableView.getSelectionModel().select(0);
      }

      this.searchTextField.setDisable(false);
      loadingOverlay.hide();

      tableView.requestFocus();
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("VPS Tables"));
    tableView.setPlaceholder(new Label("The list of VPS tables is shown here."));

    this.loadingOverlay = new WaitOverlay(loaderStack, tableView, "Loading Tables...");

    this.vpsTablesColumnSorter = new VpsTablesColumnSorter(this);

    try {
      FXMLLoader loader = new FXMLLoader(VpsTablesFilterController.class.getResource("scene-vps-tables-filter.fxml"));
      loader.load();
      vpsTablesFilterController = loader.getController();
      vpsTablesFilterController.setVpsTablesController(this, filterButton, tableStack, tableView);
    }
    catch (IOException e) {
      LOG.error("Failed to load loading filter: " + e.getMessage(), e);
    }

    BaseLoadingColumn.configureLoadingColumn(installedColumn, "", (value, model) -> {
      return model.isInstalled() ? WidgetFactory.createCheckIcon() : null;
    });

    nameColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      return new SimpleStringProperty(value.getVpsTable().getDisplayName());
    });

    versionsColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      return new SimpleObjectProperty<Integer>(CollectionUtils.size(value.getVpsTable().getTableFiles()));
    });

    BaseLoadingColumn.configureLoadingColumn(statusColumn, "Loading...", (value, model) -> {
      return model.isInstalled() ? new VpsTableColumn(value.getId(), model.getVersionId(), model.getUpdates(), null) : null;
    });

    directB2SColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      if (VpsUtil.isDataAvailable(value.getVpsTable().getB2sFiles())) {
        return new SimpleObjectProperty<>(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleObjectProperty<>("");
    });

    pupPackColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      if (VpsUtil.isDataAvailable(value.getVpsTable().getPupPackFiles())) {
        return new SimpleObjectProperty<>(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleObjectProperty<>("");
    });

    topperColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      if (VpsUtil.isDataAvailable(value.getVpsTable().getTopperFiles())) {
        return new SimpleObjectProperty<>(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleObjectProperty<>("");
    });

    povColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      if (VpsUtil.isDataAvailable(value.getVpsTable().getPovFiles())) {
        return new SimpleObjectProperty<>(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleObjectProperty<>("");
    });

    romColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      if (VpsUtil.isDataAvailable(value.getVpsTable().getRomFiles())) {
        return new SimpleObjectProperty<>(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleObjectProperty<>("");
    });

    altSoundColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      if (VpsUtil.isDataAvailable(value.getVpsTable().getAltSoundFiles())) {
        return new SimpleObjectProperty<>(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleObjectProperty<>("");
    });

    altColorColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      if (VpsUtil.isDataAvailable(value.getVpsTable().getAltColorFiles())) {
        return new SimpleObjectProperty<>(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleObjectProperty<>("");
    });

    tutorialColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      if (VpsUtil.isDataAvailable(value.getVpsTable().getTutorialFiles())) {
        return new SimpleObjectProperty<>(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleObjectProperty<>("");
    });

    updatedColumn.setCellValueFactory(cellData -> {
      VpsTableModel value = cellData.getValue();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
      return new SimpleStringProperty(dateFormat.format(new Date(value.getVpsTable().getUpdatedAt())));
    });

    tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (oldSelection == null || !oldSelection.equals(newSelection)) {
        refresh(newSelection);
      }
    });

    tableView.setRowFactory(tv -> {
      TableRow<VpsTableModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onOpen();
        }
      });
      return row;
    });

    tableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (Keys.isSpecial(event)) {
          return;
        }

        String text = event.getText();

        long timeDiff = System.currentTimeMillis() - lastKeyInputTime;
        if (timeDiff > 800) {
          lastKeyInputTime = System.currentTimeMillis();
          lastKeyInput = text;
        }
        else {
          lastKeyInputTime = System.currentTimeMillis();
          lastKeyInput = lastKeyInput + text;
          text = lastKeyInput;
        }

        for (VpsTableModel table : data) {
          if (table.getName().toLowerCase().startsWith(text.toLowerCase())) {
            clearSelection();
            tableView.getSelectionModel().select(table);
            tableView.scrollTo(tableView.getSelectionModel().getSelectedItem());
            break;
          }
        }
      }
    });

    vpsTablesFilterController.bindSearchField(searchTextField);

    emulatorCombo.setItems(FXCollections.observableList(TABLE_FORMATS));
    emulatorCombo.getSelectionModel().select(0);
    emulatorCombo.valueProperty().addListener(new ChangeListener<VpsTableFormat>() {
      @Override
      public void changed(ObservableValue<? extends VpsTableFormat> observable, VpsTableFormat oldValue, VpsTableFormat newValue) {
        doReload(false);
      }
    });

    EventManager.getInstance().addListener(this);
    this.doReload(false);
  }

  public void refresh(@Nullable VpsTableModel newSelection) {
    if (tablesController.getTabPane().getSelectionModel().getSelectedIndex() == 1) {
      NavigationController.setBreadCrumb(Arrays.asList("VPS Tables"));
      if (newSelection != null) {
        NavigationController.setBreadCrumb(Arrays.asList("VPS Tables", newSelection.getName()));
      }
    }

    openBtn.setDisable(newSelection != null);
    
    editBtn.setDisable(newSelection != null && newSelection.isInstalled());

    VpsTable vpsTable = newSelection != null? newSelection.getVpsTable(): null;
    tablesController.getVpsTablesSidebarController().setTable(Optional.ofNullable(vpsTable));
  }

  public void clearSelection() {
    tableView.getSelectionModel().clearSelection();
  }

  public @Nullable VpsTableModel getSelection() {
    return tableView.getSelectionModel().getSelectedItem();
  }

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  static class VpsTableFormat {
    private final String abbrev;
    private final String name;

    VpsTableFormat(String abbrev, String name) {
      this.abbrev = abbrev;
      this.name = name;
    }

    public String getAbbrev() {
      return abbrev;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

   public static class VpsTableModel extends BaseLoadingModel<VpsTable, VpsTableModel> {

    private boolean installed;

    private String versionId = null;

    private VPSChanges updates = null;

    public VpsTableModel(VpsTable table) {
      super(table);
    }

    public VpsTable getVpsTable() {
      return getBean();
    }

    public boolean isInstalled() {
      return installed;
    }

    public String getVersionId() {
      return versionId;
    }

    public VPSChanges getUpdates() {
      return updates;
    }

    @Override
    public String getName() {
      return bean.getDisplayName();
    }

    @Override
    public void load() {
      GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(bean, null);
      installed  = (gameByVpsTable != null);
      versionId = (gameByVpsTable != null) ? gameByVpsTable.getExtTableVersionId() : null;
      updates = (gameByVpsTable != null) ? gameByVpsTable.getVpsUpdates() : null;
    }
   }
}