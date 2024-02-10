package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.Keys;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.List;
import java.util.*;

import static de.mephisto.vpin.connectors.vps.VPS.getInstance;
import static de.mephisto.vpin.ui.Studio.client;

public class VpsTablesController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(VpsTablesController.class);

  @FXML
  private TextField searchTextField;

  @FXML
  private TableView<VpsTable> tableView;

  @FXML
  private TableColumn<VpsTable, String> installedColumn;

  @FXML
  private TableColumn<VpsTable, String> nameColumn;

  @FXML
  private TableColumn<VpsTable, String> versionsColumn;

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
    if (getSelection().isPresent()) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(VPS.getVpsTableUrl(getSelection().get().getId())));
        } catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    }
  }

  @FXML
  public void onReload() {
    doReload();
  }

  public void doReload() {
    this.searchTextField.setDisable(true);
    tableView.setVisible(false);

    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    VpsTable selection = tableView.getSelectionModel().getSelectedItem();

    new Thread(() -> {
      getInstance().reload();
      vpsTables = getInstance().getTables();
      Collections.sort(vpsTables, Comparator.comparing(o -> o.getDisplayName().trim()));

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

        int installed = 0;
        int unmapped = 0;
        for (VpsTable vpsTable : vpsTables) {
          GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(vpsTable, null);
          if (gameByVpsTable != null) {
            installed++;
          }
        }

        List<GameRepresentation> gamesCached = client.getGameService().getGamesCached();
        for (GameRepresentation gameRepresentation : gamesCached) {
          if (StringUtils.isEmpty(gameRepresentation.getExtTableId())) {
            unmapped++;
          }
        }

        countLabel.setText(vpsTables.size() + " tables / " + installed + " installed / " + unmapped + " not mapped");

        tableView.requestFocus();
      });
    }).start();
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
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    installedColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(value, null);
      if (gameByVpsTable != null) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
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

    updatedColumn.setVisible(false);
    updatedColumn.setCellValueFactory(cellData -> {
      VpsTable value = cellData.getValue();
      return new SimpleStringProperty(DateFormat.getDateInstance().format(new Date(value.getUpdatedAt())));
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

    EventManager.getInstance().addListener(this);
    this.doReload();
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

    if (newSelection.isPresent()) {
      GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(newSelection.get(), null);
      editBtn.setDisable(gameByVpsTable == null);
    }
    tablesController.getVpsTablesSidebarController().setTable(newSelection);
  }

  private List<VpsTable> filterTables(List<VpsTable> tables) {
    List<VpsTable> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    for (VpsTable table : tables) {
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
}