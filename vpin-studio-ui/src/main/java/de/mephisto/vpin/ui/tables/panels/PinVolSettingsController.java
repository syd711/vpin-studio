package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.pinvol.PinVolPreferences;
import de.mephisto.vpin.restclient.pinvol.PinVolTableEntry;
import de.mephisto.vpin.restclient.pinvol.PinVolUpdate;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class PinVolSettingsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolSettingsController.class);

  @FXML
  private VBox root;

  @FXML
  private VBox systemVolumeRoot;

  @FXML
  private VBox tableSettingsBox;

  @FXML
  private Label tableLabel;

  @FXML
  private Label systemVolumeLabel;

  @FXML
  private Button saveBtn;

  @FXML
  private Spinner<Integer> systemVolPrimarySpinner;
  @FXML
  private Spinner<Integer> systemVolSecondarySpinner;
  @FXML
  private Spinner<Integer> systemVolBassSpinner;
  @FXML
  private Spinner<Integer> systemVolRearSpinner;
  @FXML
  private Spinner<Integer> systemVolFrontSpinner;

  @FXML
  private Spinner<Integer> tableVolPrimarySpinner;
  @FXML
  private Spinner<Integer> tableVolSecondarySpinner;
  @FXML
  private Spinner<Integer> tableVolBassSpinner;
  @FXML
  private Spinner<Integer> tableVolRearSpinner;
  @FXML
  private Spinner<Integer> tableVolFrontSpinner;

  private Stage stage;
  private List<GameRepresentation> games;

  private PinVolTableEntry entry;
  private PinVolTableEntry systemVolume;

  private boolean dirty = false;

  @FXML
  private void onSave() {
    save();
  }

  @FXML
  private void onDefaults() {
    PinVolPreferences pinVolTablePreferences = client.getPinVolService().getPinVolTablePreferences();

    int primary = 0;
    int secondary = 0;
    int bass = 0;
    int rear = 0;
    int front = 0;
    List<PinVolTableEntry> tableEntries = pinVolTablePreferences.getTableEntries();
    for (PinVolTableEntry tableEntry : tableEntries) {
      primary += tableEntry.getPrimaryVolume();
      secondary += tableEntry.getSecondaryVolume();
      bass += tableEntry.getSsfBassVolume();
      rear += tableEntry.getSsfRearVolume();
      front += tableEntry.getSsfFrontVolume();
    }

    if (!tableEntries.isEmpty()) {
      primary = primary / tableEntries.size();
      secondary = secondary / tableEntries.size();
      bass = bass / tableEntries.size();
      rear = rear / tableEntries.size();
      front = front / tableEntries.size();
    }

    tableVolPrimarySpinner.getValueFactory().setValue(primary);
    tableVolSecondarySpinner.getValueFactory().setValue(secondary);
    tableVolBassSpinner.getValueFactory().setValue(bass);
    tableVolRearSpinner.getValueFactory().setValue(rear);
    tableVolFrontSpinner.getValueFactory().setValue(front);
  }

  public void setData(Stage stage, List<GameRepresentation> games, boolean showSystemVolume) {
    this.stage = stage;
    this.games = games;


    try {
      if (!showSystemVolume) {
        systemVolumeRoot.setVisible(false);
        tableLabel.setVisible(false);
      }

      tableSettingsBox.setVisible(!games.isEmpty());
      systemVolumeLabel.setVisible(!games.isEmpty());
      saveBtn.setVisible(games.isEmpty());

      if (games.size() == 1) {
        tableLabel.setText("PinVol Settings for \"" + games.get(0).getGameDisplayName() + "\"");
      }
      else {
        tableLabel.setText("PinVol Settings for " + games.size() + " tables");
      }

      PinVolPreferences pinVolTablePreferences = client.getPinVolService().getPinVolTablePreferences();
      systemVolume = pinVolTablePreferences.getSystemVolume();
      int ssfDbLimit = pinVolTablePreferences.getSsfDbLimit();

      SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, systemVolume.getPrimaryVolume());
      systemVolPrimarySpinner.setValueFactory(factory1);
      factory1.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("systemVolPrimarySpinner", () -> {
        systemVolume.setPrimaryVolume(t1);
        dirty = true;
      }, 300));

      SpinnerValueFactory.IntegerSpinnerValueFactory factory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, systemVolume.getSecondaryVolume());
      systemVolSecondarySpinner.setValueFactory(factory2);
      factory2.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("systemVolSecondarySpinner", () -> {
        systemVolume.setSecondaryVolume(t1);
        dirty = true;
      }, 300));

      SpinnerValueFactory.IntegerSpinnerValueFactory factory3 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-ssfDbLimit, ssfDbLimit, systemVolume.getSsfBassVolume());
      systemVolBassSpinner.setValueFactory(factory3);
      factory3.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("systemVolBassSpinner", () -> {
        systemVolume.setSsfBassVolume(t1);
        dirty = true;
      }, 300));

      SpinnerValueFactory.IntegerSpinnerValueFactory factory4 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-ssfDbLimit, ssfDbLimit, systemVolume.getSsfFrontVolume());
      systemVolFrontSpinner.setValueFactory(factory4);
      factory4.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("systemVolFrontSpinner", () -> {
        systemVolume.setSsfFrontVolume(t1);
        dirty = true;
      }, 300));

      SpinnerValueFactory.IntegerSpinnerValueFactory factory5 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-ssfDbLimit, ssfDbLimit, systemVolume.getSsfRearVolume());
      systemVolRearSpinner.setValueFactory(factory5);
      factory5.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("systemVolRearSpinner", () -> {
        systemVolume.setSsfRearVolume(t1);
        dirty = true;
      }, 300));

      if (games.size() == 1) {
        GameRepresentation game = games.get(0);
        entry = pinVolTablePreferences.getTableEntry(game.getGameFileName(), game.isVpxGame(), game.isFpGame());
      }
      else {
        for (GameRepresentation game : games) {
          entry = pinVolTablePreferences.getTableEntry(game.getGameFileName(), game.isVpxGame(), game.isFpGame());
          if (entry != null) {
            break;
          }
        }
      }

      if (!games.isEmpty()) {
        setTableValues(systemVolume, ssfDbLimit);
      }
    }
    catch (Exception e) {
      LOG.error("Failed initializing PinVOL panel: {}", e.getMessage(), e);
    }
  }

  private void setTableValues(PinVolTableEntry systemVolume, int ssfDbLimit) {
    if (entry == null) {
      entry = new PinVolTableEntry();
      entry.setPrimaryVolume(client.getPinVolService().getPinVolTablePreferences().getDefaultVol());
      entry.setSecondaryVolume(systemVolume.getSecondaryVolume());
      entry.setSsfBassVolume(systemVolume.getSsfBassVolume());
      entry.setSsfFrontVolume(systemVolume.getSsfFrontVolume());
      entry.setSsfRearVolume(systemVolume.getSsfRearVolume());
    }

    SpinnerValueFactory.IntegerSpinnerValueFactory factory6 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, entry.getPrimaryVolume());
    tableVolPrimarySpinner.setValueFactory(factory6);
    factory6.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      entry.setPrimaryVolume(t1);
      dirty = true;
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory7 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, entry.getSecondaryVolume());
    tableVolSecondarySpinner.setValueFactory(factory7);
    factory7.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      entry.setSecondaryVolume(t1);
      dirty = true;
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory8 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-ssfDbLimit, ssfDbLimit, entry.getSsfBassVolume());
    tableVolBassSpinner.setValueFactory(factory8);
    factory8.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      entry.setSsfBassVolume(t1);
      dirty = true;
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory9 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-ssfDbLimit, ssfDbLimit, entry.getSsfFrontVolume());
    tableVolFrontSpinner.setValueFactory(factory9);
    factory9.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      entry.setSsfFrontVolume(t1);
      dirty = true;
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory10 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-ssfDbLimit, ssfDbLimit, entry.getSsfRearVolume());
    tableVolRearSpinner.setValueFactory(factory10);
    factory10.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      entry.setSsfRearVolume(t1);
      dirty = true;
    }, 300));
  }

  public void save() {
    if (!dirty) {
      return;
    }

    if (systemVolume == null) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save PinVol settings, no system volume set.");
      return;
    }

    PinVolUpdate update = new PinVolUpdate();
    update.setGameIds(games.stream().map(GameRepresentation::getId).collect(Collectors.toList()));
    update.setSystemVolume(systemVolume);
    update.setTableVolume(entry);

    try {
      client.getPinVolService().save(update);
    }
    catch (Exception e) {
      LOG.error("Failed to save pinvol update: {}", e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Failed to save PinVol update: " + e.getMessage());
    }

    for (GameRepresentation game : games) {
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    systemVolumeRoot.managedProperty().bindBidirectional(systemVolumeRoot.visibleProperty());
    tableLabel.managedProperty().bindBidirectional(tableLabel.visibleProperty());
    systemVolumeLabel.managedProperty().bindBidirectional(systemVolumeLabel.visibleProperty());
    tableSettingsBox.managedProperty().bindBidirectional(tableSettingsBox.visibleProperty());
    saveBtn.managedProperty().bindBidirectional(saveBtn.visibleProperty());
  }
}
