package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.tables.vps.VpsDBDownloadProgressModel;
import de.mephisto.vpin.ui.tables.vps.VpsTableColumn;
import de.mephisto.vpin.ui.tables.vps.VpsTutorialColumn;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.vps.VpsTablesController.VpsTableModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class VpsTablesController extends BaseTableController<VpsTable, VpsTableModel>
    implements Initializable, StudioFXController, StudioEventListener {

  private final static Logger LOG = LoggerFactory.getLogger(VpsTablesController.class);

  @FXML
  private ComboBox<VpsTableFormat> emulatorCombo;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> installedColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> nameColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> commentColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> versionsColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> statusColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> directB2SColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> pupPackColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> romColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> topperColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> povColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> altSoundColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> altColorColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> tutorialColumn;

  @FXML
  TableColumn<VpsTableModel, VpsTableModel> updatedColumn;

  @FXML
  private Button vpsOpenBtn;

  @FXML
  private Button tableEditBtn;

  @FXML
  private Button tableDataBtn;

  //------------------------

  // calculated global KPIs on vpsTables
  private int unmapped = 0;

  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


  // Add a public no-args constructor
  public VpsTablesController() {
  }


  @FXML
  private void onOpenTable() {
    VpsTable vpstable = getSelection();
    if (vpstable != null) {
      GameRepresentation game = client.getGameService().getGameByVpsTable(vpstable, null);
      if (game != null) {
        Platform.runLater(() -> {
          NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(game.getId()));
        });
      }
    }
  }

  @FXML
  private void onTableEdit() {
    VpsTable vpstable = getSelection();
    if (vpstable != null) {
      GameRepresentation game = client.getGameService().getGameByVpsTable(vpstable, null);
      if (game != null) {
        Platform.runLater(() -> {
          TableDialogs.openTableDataDialog(tablesController.getTableOverviewController(), game);
        });
      }
    }
  }

  @FXML
  private void onOpen() {
    VpsTable vpstable = getSelection();
    if (vpstable != null) {
      Studio.browse(VPS.getVpsTableUrl(vpstable.getId()));
    }
  }

  /**
   * @return true if the filtered list did change and reload is required
   */
  @Override
  public void applyFilter() {
    super.applyFilter();

    // mind that it can be called by threads before data is even initialized
    if (this.filteredModels != null) {
      String text = labelCount.getText();

      // Now update other counters
      int installed = 0;
      for (VpsTableModel model : this.filteredModels) {
        if (model.isInstalled()) {
          installed++;
        }
      }
      labelCount.setText(text + " / " + installed + " installed / " + unmapped + " not mapped");
    }
  }

  @FXML
  public void onReload() {
    doReload(true);
  }

  public void doReload(boolean forceReload) {
    LOG.info("reload vps tables");

    startReload("Loading Tables...");

    VpsTableModel selection = tableView.getSelectionModel().getSelectedItem();

    if (forceReload) {
      ProgressDialog.createProgressDialog(new VpsDBDownloadProgressModel("Download VPS Database", Arrays.asList(new File("<vpsdb.json>"))));
    }

    // calculate unmapped in a non blocking thread
    JFXFuture.runAsync(() -> {
      this.unmapped = 0;
      List<GameRepresentation> gamesCached = client.getGameService().getVpxGamesCached();
      for (GameRepresentation gameRepresentation : gamesCached) {
        if (StringUtils.isEmpty(gameRepresentation.getExtTableId())) {
          unmapped++;
        }
      }
    }).thenLater(() -> {
      applyFilter();
    });

    //-----------
    // load tables in parallel
    JFXFuture.supplyAsync(() -> {
      return client.getVpsService().getTables();
    }).thenAcceptLater(data -> {

      setItems(data);

      VpsTableModel.loadAllThenLater(models, () -> {
        applyFilter();
      });

      // reapply selection
      if (selection != null && filteredModels.contains(selection)) {
        tableView.getSelectionModel().select(selection);
      }
      else {
        tableView.getSelectionModel().select(0);
      }

      endReload();
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("table", "tables", new VpsTablesColumnSorter(this));
    tableView.setPlaceholder(new Label("The list of VPS tables is shown here."));

    super.loadFilterPanel("scene-vps-tables-filter.fxml");

    BaseLoadingColumn.configureLoadingColumn(installedColumn, "", (value, model) -> {
      return model.isInstalled() ? WidgetFactory.createCheckIcon() : null;
    });

    BaseLoadingColumn.configureColumn(nameColumn, (value, model) -> {
      Label label = new Label(value.getDisplayName());
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    BaseLoadingColumn.configureLoadingColumn(statusColumn, "Loading...", (value, model) -> {
      return model.isInstalled() ? new VpsTableColumn(value.getId(), model.getVersionId(), false, model.getUpdates(), null) : null;
    });

    BaseLoadingColumn.configureLoadingColumn(commentColumn, "Loading...", (value, model) -> {
      String comment = value.getComment();
      if (!StringUtils.isEmpty(comment)) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Label label = new Label();
        FontIcon icon = WidgetFactory.createIcon("mdi2c-comment");
        label.setTooltip(new Tooltip(comment));
        label.setGraphic(icon);

        if (comment.toLowerCase().contains("//error")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
        }
        else if (comment.toLowerCase().contains("//todo")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.TODO_COLOR));
        }
        else if (comment.toLowerCase().contains("//outdated")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.OUTDATED_COLOR));
        }

        hBox.getChildren().add(label);
        return hBox;
      }
      return new Label();
    });

    BaseLoadingColumn.configureColumn(versionsColumn, (value, model) -> {
      Label label = new Label(Integer.toString(CollectionUtils.size(value.getTableFiles())));
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(directB2SColumn, (value, model) ->
        VpsUtil.isDataAvailable(value.getB2sFiles()) ? WidgetFactory.createCheckboxIcon() : null, this, true);

    BaseLoadingColumn.configureColumn(pupPackColumn, (value, model) ->
        VpsUtil.isDataAvailable(value.getPupPackFiles()) ? WidgetFactory.createCheckboxIcon() : null, this, true);

    BaseLoadingColumn.configureColumn(topperColumn, (value, model) ->
        VpsUtil.isDataAvailable(value.getTopperFiles()) ? WidgetFactory.createCheckboxIcon() : null, this, true);

    BaseLoadingColumn.configureColumn(povColumn, (value, model) ->
        VpsUtil.isDataAvailable(value.getPovFiles()) ? WidgetFactory.createCheckboxIcon() : null, this, true);

    BaseLoadingColumn.configureColumn(romColumn, (value, model) ->
        VpsUtil.isDataAvailable(value.getRomFiles()) ? WidgetFactory.createCheckboxIcon() : null, this, true);

    BaseLoadingColumn.configureColumn(altSoundColumn, (value, model) ->
        VpsUtil.isDataAvailable(value.getAltSoundFiles()) ? WidgetFactory.createCheckboxIcon() : null, this, true);

    BaseLoadingColumn.configureColumn(altColorColumn, (value, model) ->
        VpsUtil.isDataAvailable(value.getAltColorFiles()) ? WidgetFactory.createCheckboxIcon() : null, this, true);

    BaseLoadingColumn.configureLoadingColumn(tutorialColumn, "Loading...", (value, model) -> {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      return new VpsTutorialColumn(value.getId(), uiSettings);
    });

    BaseLoadingColumn.configureColumn(updatedColumn, (value, model) -> {
      Label label = new Label(dateFormat.format(new Date(value.getUpdatedAt())));
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(newSelection);
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

    List<VpsTableFormat> tableFormats = new ArrayList<>();
    tableFormats.add(new VpsTableFormat("All Emulators", (String[]) null));
    tableFormats.add(new VpsTableFormat("Visual Pinball X", "VPX"));
    tableFormats.add(new VpsTableFormat("Visual Pinball 9", "VP9"));
    tableFormats.add(new VpsTableFormat("Future Pinball", "FP"));
    tableFormats.add(new VpsTableFormat("Zen Studios", "FX", "FX2", "FX3"));

    emulatorCombo.setItems(FXCollections.observableList(tableFormats));
    emulatorCombo.getSelectionModel().select(0);
    ((VpsTablesFilterController) filterController).bindEmulatorCombo(emulatorCombo);

    EventManager.getInstance().addListener(this);
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    // first time activation
    if (models == null || models.isEmpty()) {
      this.doReload(false);
    }
    VpsTableModel selection = tableView.getSelectionModel().getSelectedItem();
    refreshView(selection);
  }

  @Override
  public void vpsTableChanged(@NonNull String vpsTableId) {
    Optional<VpsTableModel> first = models.stream().filter(m -> m.getVpsTableId().equals(vpsTableId)).findFirst();
    if (first.isPresent()) {
      reloadItem(first.get().getBean());
    }
  }

  @Override
  protected void refreshView(@Nullable VpsTableModel newSelection) {
    if (newSelection != null) {
      NavigationController.setBreadCrumb(Arrays.asList("VPS Tables", newSelection.getName()));
    }
    else {
      NavigationController.setBreadCrumb(Arrays.asList("VPS Tables"));
    }

    vpsOpenBtn.setDisable(newSelection == null);
    tableEditBtn.setDisable(newSelection == null || !newSelection.isInstalled());
    tableDataBtn.setDisable(newSelection == null || !newSelection.isInstalled());

    VpsTable vpsTable = newSelection != null ? newSelection.getVpsTable() : null;
    VpsTablesPredicateFactory predicates = ((VpsTablesFilterController) filterController).getPredicateFactory();
    tablesController.getVpsTablesSidebarController().setTable(Optional.ofNullable(vpsTable), predicates);
  }

  static class VpsTableFormat {
    private final String[] abbrev;
    private final String name;

    VpsTableFormat(String name, String... abbrev) {
      this.abbrev = abbrev;
      this.name = name;
    }

    public String[] getAbbrev() {
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

  //-------------------------

  protected VpsTableModel toModel(VpsTable table) {
    return new VpsTableModel(table);
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

    public boolean sameBean(VpsTable otherTable) {
      return StringUtils.equals(bean.getId(), otherTable.getId());
    }

    public boolean isInstalled() {
      return installed;
    }

    public String getVpsTableId() {
      return bean.getId();
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
      GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(-1, bean, null);
      installed = (gameByVpsTable != null);
      versionId = (gameByVpsTable != null) ? gameByVpsTable.getExtTableVersionId() : null;
      updates = (gameByVpsTable != null) ? gameByVpsTable.getVpsUpdates() : null;
    }
  }
}