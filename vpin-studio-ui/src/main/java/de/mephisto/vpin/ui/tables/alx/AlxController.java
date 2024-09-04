package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlay;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.JFXFuture;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class AlxController implements Initializable, StudioFXController, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(AlxController.class);
  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 200;

  @FXML
  private StackPane loaderStack;

  @FXML
  private VBox mostPlayedWidget;

  @FXML
  private VBox timePlayedWidget;

  @FXML
  private VBox scoresWidget;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private boolean emulatorsLoaded = false;
  private List<Integer> ignoredEmulators = null;

  @FXML
  private VBox tileList;

  @FXML
  private VBox col1;

  @FXML
  private VBox col2;

  @FXML
  private VBox col3;

  @FXML
  private BorderPane root;

  @FXML
  private Button reloadBtn;
  @FXML
  private Button deleteBtn;

  private TablesController tablesController;

  private WaitOverlay waitOverlay;

  // Add a public no-args constructor
  public AlxController() {
  }

  @FXML
  private void onReload() {
    refreshAlxData();
  }

  @FXML
  private void onDelete() {
    AlxDialogs.openDeleteAlxDialog(this);
    refreshAlxData();
  }

  private void refreshEmulators(boolean uiSettingsChanged) {
    // if view not activated but UI change happens, just ignore the event 
    if (!emulatorsLoaded && uiSettingsChanged) {
      return;
    }
    if (!emulatorsLoaded) {
      List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getFrontendService().getGameEmulatorsUncached());
      List<GameEmulatorRepresentation> filtered = emulators.stream().filter(e -> !ignoredEmulators.contains(e.getId())).collect(Collectors.toList());

      GameEmulatorRepresentation allTables = new GameEmulatorRepresentation();
      allTables.setId(-1);
      allTables.setName("All Tables");
      filtered.add(0, allTables);

      // mind settings combo will also trigger refreshAlxData()
      this.emulatorCombo.setItems(FXCollections.observableList(filtered));
      this.emulatorCombo.getSelectionModel().select(0);
      emulatorsLoaded = true;
    }
    else {
      // simply refresh data in that case
      refreshAlxData();
    }
  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.waitOverlay = new WaitOverlay(loaderStack, "Loading Statistics...");

    this.emulatorCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshAlxData());

    client.getPreferenceService().addListener(this);
    NavigationController.setBreadCrumb(Arrays.asList("Analytics"));

    Studio.stage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (tablesController.isTabSelected(TablesController.TAB_STATISTICS)) {
          debouncer.debounce("prefWidth", () -> {
            Platform.runLater(() -> {
              refreshAlxData();
            });
          }, DEBOUNCE_MS);
        }
      }
    });
  }

  public void refreshAlxData() {
    waitOverlay.show();

    // empty screen
    tileList.getChildren().clear();
    mostPlayedWidget.getChildren().clear();
    timePlayedWidget.getChildren().clear();
    scoresWidget.getChildren().clear();

    reloadBtn.setDisable(true);
    deleteBtn.setDisable(true);

    JFXFuture.supplyAsync(() -> {
      AlxSummary alxSummary = client.getAlxService().getAlxSummary();
      List<TableAlxEntry> entries = alxSummary.getEntries();

      GameEmulatorRepresentation value = this.emulatorCombo.getValue();
      if (value == null) {
        entries.clear();
      }
      else if (value.getId() != -1) {
        List<TableAlxEntry> filtered = new ArrayList<>();
        List<Integer> collect = client.getGameService().getGameIdsCached(value.getId());
        for (TableAlxEntry entry : entries) {
          if (collect.contains(entry.getGameId())) {
            filtered.add(entry);
          }
        }
        alxSummary.setEntries(filtered);
      }
      return alxSummary;
    })
    .thenAcceptLater(alxSummary -> {
      refreshAlxData(alxSummary);
      waitOverlay.hide();
    })
    .onErrorLater(e -> {
      waitOverlay.hide();
      LOG.error("Failed to load ALX dashboard: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Cannot load Statistics to initialize dashboard: " + e.getMessage(), "Please submit a bug report with log files on github for this.");
      // only reload button is re-activated, to permit retry retrieving data 
      reloadBtn.setDisable(false);
      return null;
    });
  }
  private void refreshAlxData(AlxSummary alxSummary) {
    reloadBtn.setDisable(false);
    deleteBtn.setDisable(false);

    try {
      double v = AlxFactory.calculateColumnWidth();
      col1.setPrefWidth(v);
      col2.setPrefWidth(v);
      col3.setPrefWidth(v);

      List<TableAlxEntry> entries = alxSummary.getEntries();
    
      AlxFactory.createMostPlayed(mostPlayedWidget, entries);
      AlxFactory.createLongestPlayed(timePlayedWidget, entries);
      AlxFactory.createRecordedScores(scoresWidget, entries);

      AlxFactory.createTotalTimeTile(tileList, entries);
      AlxFactory.createTotalGamesPlayedTile(tileList, entries);
      AlxFactory.createTotalScoresTile(tileList, entries);
      AlxFactory.createTotalHighScoresTile(tileList, entries);
      Date alxStartDate = alxSummary.getStartDate() != null ? alxSummary.getStartDate() :
          new Date(System.currentTimeMillis() - 1 * 365 * 24 * 3600 * 1000);
      AlxFactory.createAvgWeekTimeTile(tileList, entries, alxStartDate);
    }
    catch (Exception e) {
      LOG.error("Failed to initialize ALX dashboard: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to initialize dashboard: " + e.getMessage(), "Please submit a bug report with log files on github for this.");
    }
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("Table Statistics"));
    refreshEmulators(false);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    // refresh emulators only when they have been loaded first time
    if (PreferenceNames.UI_SETTINGS.equals(key)) {
      UISettings uiSettings = (UISettings) value;
      if (ignoredEmulators == null || !ListUtils.isEqualList(ignoredEmulators, uiSettings.getIgnoredEmulatorIds())) {
        this.ignoredEmulators = uiSettings.getIgnoredEmulatorIds();
        this.emulatorsLoaded = false;
        refreshEmulators(true);
      }
    }
  }
}