package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tables.vps.VpsDBDownloadProgressModel;
import de.mephisto.vpin.ui.tables.vps.VpsTableColumn;
import de.mephisto.vpin.ui.util.Keys;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
  private TableView<VpsTable> tableView;

  @FXML
  private ComboBox<VpsTableFormat> emulatorCombo;

  @FXML
  private TableColumn<VpsTable, String> installedColumn;

  @FXML
  private TableColumn<VpsTable, String> nameColumn;

  @FXML
  private TableColumn<VpsTable, String> versionsColumn;

  @FXML
  private TableColumn<VpsTable, Node> statusColumn;

  @FXML
  private TableColumn<VpsTable, String> directB2SColumn;

  @FXML
  private TableColumn<VpsTable, String> pupPackColumn;

  @FXML
  private TableColumn<VpsTable, String> romColumn;

  @FXML
  private TableColumn<VpsTable, String> topperColumn;

  @FXML
  private TableColumn<VpsTable, String> povColumn;

  @FXML
  private TableColumn<VpsTable, String> altSoundColumn;

  @FXML
  private TableColumn<VpsTable, String> altColorColumn;

  @FXML
  private TableColumn<VpsTable, String> tutorialColumn;

  @FXML
  private TableColumn<VpsTable, String> updatedColumn;

  @FXML
  private CheckBox installedOnlyCheckbox;

  @FXML
  private StackPane tableStack;

  @FXML
  private Button openBtn;

  @FXML
  private Button editBtn;

  @FXML
  private Label countLabel;

  private Parent loadingOverlay;


  private ObservableList<VpsTable> data;
  private List<VpsTable> vpsTables;
  private TablesController tablesController;

  // calculated KPIs on vpsTables
  private int installed = 0;
  private int unmapped = 0;

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";

  // Add a public no-args constructor
  public VpsTablesController() {
  }

  @FXML
  private void onTableEdit() {
    if (getSelection().isPresent()) {
      GameRepresentation game = client.getGameService().getGameByVpsTable(getSelection().get(), null);
      tablesController.getTableOverviewController().setSelection(game);
    }
  }

  @FXML
  private void onOpen() {
    Studio.browse(VPS.getVpsTableUrl(getSelection().get().getId()));
  }

  @FXML
  public void onReload() {
    doReload(true);
  }

  public void doReload(boolean forceReload) {
    this.searchTextField.setDisable(true);
    tableView.setVisible(false);

    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    VpsTable selection = tableView.getSelectionModel().getSelectedItem();

    if (forceReload) {
      ProgressDialog.createProgressDialog(new VpsDBDownloadProgressModel("Download VPS Database", Arrays.asList(new File("<vpsdb.json>"))));
    }

    new Thread(() -> {
      // get all tables
      vpsTables = client.getVpsService().getTables();

      VpsTableFormat value = emulatorCombo.getValue();
      String tableFormat = value.getAbbrev();
      vpsTables = vpsTables.stream().filter(t -> t.getAvailableTableFormats().contains(tableFormat)).collect(Collectors.toList());

      Collections.sort(vpsTables, Comparator.comparing(o -> o.getDisplayName().trim()));

      // and calculate installed and unmapped in the non blocking thread
      this.installed = 0;
      this.unmapped = 0;
      if (!client.getFrontendService().getVpxGameEmulators().isEmpty()) {
        for (VpsTable vpsTable : vpsTables) {
          GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(vpsTable, null);
          if (gameByVpsTable != null) {
            installed++;
          }
        }
      }

      List<GameRepresentation> gamesCached = client.getGameService().getVpxGamesCached();
      for (GameRepresentation gameRepresentation : gamesCached) {
        if (StringUtils.isEmpty(gameRepresentation.getExtTableId())) {
          unmapped++;
        }
      }

      Platform.runLater(() -> {
        data = FXCollections.observableList(filterTables(vpsTables));
        tableView.setItems(data);
        tableView.refresh();
        if (selection != null && data.contains(selection)) {
          tableView.getSelectionModel().select(selection);
        }
        else {
          tableView.getSelectionModel().select(0);
        }

        this.searchTextField.setDisable(false);
        tableStack.getChildren().remove(loadingOverlay);
        tableView.setVisible(true);

        countLabel.setText(vpsTables.size() + " tables / " + installed + " installed / " + unmapped + " not mapped");

        tableView.requestFocus();
      });
    }, "VPS Tables Load").start();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("VPS Tables"));
    tableView.setPlaceholder(new Label("The list of VPS tables is shown here."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tables...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    installedColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      if (!client.getFrontendService().getVpxGameEmulators().isEmpty()) {
        GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(value, null);
        if (gameByVpsTable != null) {
          return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
        }
      }
      return new SimpleObjectProperty("");
    });

    nameColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      return new SimpleStringProperty(value.getDisplayName());
    });

    versionsColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      List<VpsTableVersion> tableFiles = value.getTableFiles();
      if (tableFiles != null) {
        return new SimpleStringProperty(String.valueOf(tableFiles.size()));
      }
      return new SimpleStringProperty("0");
    });

    statusColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      String versionId = null;
      VPSChanges updates = null;
      if (!client.getFrontendService().getVpxGameEmulators().isEmpty()) {
        GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(value, null);
        if (gameByVpsTable != null) {
          versionId = gameByVpsTable.getExtTableVersionId();
          updates = gameByVpsTable.getVpsUpdates();
          return new SimpleObjectProperty<>(new VpsTableColumn(value.getId(), versionId, updates, null));
        }
      }

      return new SimpleObjectProperty("-");
    });

    directB2SColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      if (this.isDataAvailable(value.getB2sFiles())) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    pupPackColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      if (this.isDataAvailable(value.getPupPackFiles())) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    topperColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      if (this.isDataAvailable(value.getTopperFiles())) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    povColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      if (this.isDataAvailable(value.getPovFiles())) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    romColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      if (this.isDataAvailable(value.getRomFiles())) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    altSoundColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      if (this.isDataAvailable(value.getAltSoundFiles())) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    altColorColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      if (this.isDataAvailable(value.getAltColorFiles())) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    tutorialColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      if (value.getTutorialFiles() != null && !value.getTutorialFiles().isEmpty()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    updatedColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
      return new SimpleStringProperty(dateFormat.format(new Date(value.getUpdatedAt())));
    });

    tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (oldSelection == null || !oldSelection.equals(newSelection)) {
        refresh(Optional.ofNullable(newSelection));
      }
    });

    tableView.setRowFactory(tv -> {
      TableRow<VpsTable> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onOpen();
        }
      });
      return row;
    });

    installedOnlyCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        tableView.getSelectionModel().clearSelection();
        List<VpsTable> filtered = filterTables(vpsTables);
        tableView.setItems(FXCollections.observableList(filtered));
      }
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

        for (VpsTable table : data) {
          if (table.getDisplayName().toLowerCase().startsWith(text.toLowerCase())) {
            tableView.getSelectionModel().clearSelection();
            tableView.getSelectionModel().select(table);
            tableView.scrollTo(tableView.getSelectionModel().getSelectedItem());
            break;
          }
        }
      }
    });

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      List<VpsTable> filtered = filterTables(this.vpsTables);
      tableView.setItems(FXCollections.observableList(filtered));
    });

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

  private boolean isDataAvailable(List<? extends VpsAuthoredUrls> entries) {
    if (entries == null) {
      return false;
    }
    for (VpsAuthoredUrls entry : entries) {
      if (entry.getUrls() != null && !entry.getUrls().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public void refresh(Optional<VpsTable> newSelection) {
    if (tablesController.getTabPane().getSelectionModel().getSelectedIndex() == 1) {
      NavigationController.setBreadCrumb(Arrays.asList("VPS Tables"));
      if (newSelection.isPresent()) {
        VpsTable selection = newSelection.get();
        NavigationController.setBreadCrumb(Arrays.asList("VPS Tables", selection.getDisplayName()));
      }
    }

    editBtn.setDisable(true);
    openBtn.setDisable(newSelection.isEmpty());

    // run in a dedicated thread as getGameByVpsTable() can block UI
    new Thread(() -> {
      final GameRepresentation gameByVpsTable;
      if (newSelection.isPresent() && !client.getFrontendService().getVpxGameEmulators().isEmpty()) {
        gameByVpsTable = client.getGameService().getGameByVpsTable(newSelection.get(), null);
      }
      else {
        gameByVpsTable = null;
      }
      Platform.runLater(() -> {
        editBtn.setDisable(gameByVpsTable == null);
        tablesController.getVpsTablesSidebarController().setTable(newSelection, tablesController.getTablesSideBarController());
      });
    }).start();
  }

  private List<VpsTable> filterTables(List<VpsTable> tables) {
    List<VpsTable> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    boolean noVPX = client.getFrontendService().getVpxGameEmulators().isEmpty();
    for (VpsTable table : tables) {
      if (installedOnlyCheckbox.isSelected() && !noVPX) {
        GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(table, null);
        if (gameByVpsTable == null) {
          continue;
        }
      }

      if (table.getDisplayName() != null) {
        String filename = table.getDisplayName().toLowerCase();
        if (filename.contains(filterValue.toLowerCase())) {
          filtered.add(table);
        }
      }
    }
    return filtered;
  }

  public Optional<VpsTable> getSelection() {
    VpsTable selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
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
}