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
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TablesController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
  private VBox mostPlayedWidget;

  @FXML
  private VBox timePlayedWidget;

  @FXML
  private VBox scoresWidget;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

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
  private TablesController tablesController;


  // Add a public no-args constructor
  public AlxController() {
  }

  @FXML
  private void onReload() {
    refreshAlxData();
  }

  @FXML
  private void onDelete() {
    AlxDialogs.openTableDeleteDialog(this);
    refreshAlxData();
  }


  private void refreshEmulators() {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getFrontendService().getGameEmulatorsUncached());
    List<GameEmulatorRepresentation> filtered = emulators.stream().filter(e -> !uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(e.getId()))).collect(Collectors.toList());

    GameEmulatorRepresentation allTables = new GameEmulatorRepresentation();
    allTables.setId(-1);
    allTables.setName("All Tables");
    filtered.add(0, allTables);

    this.emulatorCombo.setItems(FXCollections.observableList(filtered));
    this.emulatorCombo.getSelectionModel().select(0);

    refreshAlxData();
  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.emulatorCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshAlxData());

    client.getPreferenceService().addListener(this);
    NavigationController.setBreadCrumb(Arrays.asList("Analytics"));

    Studio.stage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (tablesController.getTabPane().getSelectionModel().getSelectedIndex() == 3) {
          debouncer.debounce("position", () -> {
            Platform.runLater(() -> {
              refreshAlxData();
            });
          }, DEBOUNCE_MS);
        }
      }
    });
  }

  public void refreshAlxData() {
    try {
      double v = AlxFactory.calculateColumnWidth();
      col1.setPrefWidth(v);
      col2.setPrefWidth(v);
      col3.setPrefWidth(v);

      AlxSummary alxSummary = client.getAlxService().getAlxSummary();
      List<TableAlxEntry> entries = alxSummary.getEntries();

      GameEmulatorRepresentation value = this.emulatorCombo.getValue();
      if (value == null) {
        return;
      }

      if (value.getId() != -1) {
        List<TableAlxEntry> filtered = new ArrayList<>();
        List<Integer> collect = client.getGameService().getGameIdsCached(value.getId());
        for (TableAlxEntry entry : entries) {
          if (collect.contains(entry.getGameId())) {
            filtered.add(entry);
          }
        }
        entries = filtered;
      }

      AlxFactory.createMostPlayed(mostPlayedWidget, entries);
      AlxFactory.createLongestPlayed(timePlayedWidget, entries);
      AlxFactory.createRecordedScores(scoresWidget, entries);

      tileList.getChildren().removeAll(tileList.getChildren());
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
    refreshAlxData();
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.UI_SETTINGS.equals(key)) {
      refreshEmulators();
    }
  }
}