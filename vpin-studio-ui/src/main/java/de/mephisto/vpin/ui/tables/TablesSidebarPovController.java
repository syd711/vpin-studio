package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.POV;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.representations.POVRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class TablesSidebarPovController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox dataBox;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private ComboBox<POVComboModel> povSSAACombo;

  @FXML
  private ComboBox<POVPostProcComboModel> povPostprocAACombo;

  @FXML
  private ComboBox<POVComboModel> povIngameAOCombo;

  @FXML
  private ComboBox<POVComboModel> povScSpReflectCombo;

  @FXML
  private ComboBox<POVComboModel> povFpsLimiterCombo;

  @FXML
  private CheckBox povOverwriteDetailCheckbox;

  @FXML
  private Slider povDetailsSlider;

  @FXML
  private Slider povSoundVolumeSlider;

  @FXML
  private Slider povMusicVolumeSlider;

  @FXML
  private ComboBox<POVComboModel> povBallReflectionCombobox;

  @FXML
  private ComboBox<POVComboModel> povBallTrailCombobox;

  @FXML
  private Spinner<Integer> povBallTrailStrengthSpinner;

  @FXML
  private CheckBox povOverwriteNightDayCheckbox;

  @FXML
  private Spinner<Integer> povNighDaySpinner;

  @FXML
  private Spinner<Double> povGameDifficultySpinner;


  @FXML
  private Spinner<Integer> povRotationFullscreenSpinner;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private POVRepresentation pov;

  // Add a public no-args constructor
  public TablesSidebarPovController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    emptyDataBox.managedProperty().bind(emptyDataBox.visibleProperty());
    dataBox.managedProperty().bind(dataBox.visibleProperty());

    povSSAACombo.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povSSAACombo.valueProperty().addListener((observable, oldValue, newValue) -> Studio.client.getVpxService().setPOVPreference(game.get().getId(), getPOV(), POV.SSAA, newValue.getValue()));

    povPostprocAACombo.setItems(FXCollections.observableList(POVPostProcComboModel.MODELS));
    povPostprocAACombo.valueProperty().addListener((observable, oldValue, newValue) -> Studio.client.getVpxService().setPOVPreference(game.get().getId(), getPOV(), POV.POST_PROC_AA, newValue.getValue()));

    povIngameAOCombo.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povIngameAOCombo.valueProperty().addListener((observable, oldValue, newValue) -> Studio.client.getVpxService().setPOVPreference(game.get().getId(), getPOV(), POV.INGAME_AO, newValue.getValue()));

    povScSpReflectCombo.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povScSpReflectCombo.valueProperty().addListener((observable, oldValue, newValue) -> Studio.client.getVpxService().setPOVPreference(game.get().getId(), getPOV(), POV.SCSP_REFLECT, newValue.getValue()));

    povFpsLimiterCombo.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povFpsLimiterCombo.valueProperty().addListener((observable, oldValue, newValue) -> Studio.client.getVpxService().setPOVPreference(game.get().getId(), getPOV(), POV.FPS_LIMITER, newValue.getValue()));

    povOverwriteDetailCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      int result = 0;
      if (newValue) {
        result = 1;
      }
      Studio.client.getVpxService().setPOVPreference(game.get().getId(), getPOV(), POV.OVERWRITE_DETAILS_LEVEL, result);
      povDetailsSlider.setDisable(!newValue);
    });

    povDetailsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      final GameRepresentation g = game.get();
      debouncer.debounce(POV.DETAILS_LEVEL, () -> {
        int value1 = ((Double) newValue).intValue();
        Studio.client.getVpxService().setPOVPreference(g.getId(), getPOV(), POV.DETAILS_LEVEL, value1);
      }, 500);
    });

    povBallReflectionCombobox.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povBallReflectionCombobox.valueProperty().addListener((observable, oldValue, newValue) -> Studio.client.getVpxService().setPOVPreference(game.get().getId(), getPOV(), POV.BALL_REFLECTION, newValue.getValue()));

    povBallTrailCombobox.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povBallTrailCombobox.valueProperty().addListener((observable, oldValue, newValue) -> Studio.client.getVpxService().setPOVPreference(game.get().getId(), getPOV(), POV.BALL_TRAIL, newValue.getValue()));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    povBallTrailStrengthSpinner.setValueFactory(factory);
    povBallTrailStrengthSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      final GameRepresentation g = game.get();
      debouncer.debounce(POV.BALL_TRAIL_STRENGTH, () -> {
        double formattedValue = Double.valueOf(newValue) / 100;
        Studio.client.getVpxService().setPOVPreference(g.getId(), getPOV(), POV.BALL_TRAIL_STRENGTH, formattedValue);
      }, 500);
    });

    povOverwriteNightDayCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      int result = 0;
      if (newValue) {
        result = 1;
      }
      Studio.client.getVpxService().setPOVPreference(game.get().getId(), getPOV(), POV.OVERWRITE_NIGHTDAY, result);
      povNighDaySpinner.setDisable(!newValue);
    });
    ;

    SpinnerValueFactory.IntegerSpinnerValueFactory factoryNightDay = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    povNighDaySpinner.setValueFactory(factoryNightDay);
    factoryNightDay.valueProperty().addListener((observable, oldValue, newValue) -> {
      final GameRepresentation g = game.get();
      debouncer.debounce(POV.NIGHTDAY_LEVEL, () -> {
        Studio.client.getVpxService().setPOVPreference(g.getId(), getPOV(), POV.NIGHTDAY_LEVEL, newValue);
      }, 500);
    });

    SpinnerValueFactory.DoubleSpinnerValueFactory factoryDifficulty = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 0);
    povGameDifficultySpinner.setValueFactory(factoryDifficulty);
    povGameDifficultySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      final GameRepresentation g = game.get();
      debouncer.debounce(POV.GAMEPLAY_DIFFICULTY, () -> {
        Studio.client.getVpxService().setPOVPreference(g.getId(), getPOV(), POV.GAMEPLAY_DIFFICULTY, newValue);
      }, 500);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factoryRotation = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 360, 0);
    povRotationFullscreenSpinner.setValueFactory(factoryRotation);
    povRotationFullscreenSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      final GameRepresentation g = game.get();
      debouncer.debounce(POV.FULLSCREEN_ROTATION, () -> {
        Studio.client.getVpxService().setPOVPreference(g.getId(), getPOV(), POV.FULLSCREEN_ROTATION, newValue);
      }, 500);
    });


    povSoundVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      final GameRepresentation g = game.get();
      debouncer.debounce(POV.SOUND_VOLUME, () -> {
        int v = ((Double) newValue).intValue();
        Studio.client.getVpxService().setPOVPreference(g.getId(), getPOV(), POV.SOUND_VOLUME, v);
      }, 500);
    });

    povMusicVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      final GameRepresentation g = game.get();
      debouncer.debounce(POV.MUSIC_VOLUME, () -> {
        int v = ((Double) newValue).intValue();
        Studio.client.getVpxService().setPOVPreference(g.getId(), getPOV(), POV.MUSIC_VOLUME, v);
      }, 500);
    });
  }

  @FXML
  private void onPOVUpload() {
    if (game.isPresent()) {
      TableDialogs.directUpload(Studio.stage, AssetType.POV, game.get(), null);
    }
  }


  @FXML
  private void onPOVReload() {
    this.refreshView(this.game);
  }

  @FXML
  private void onPOVDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete POV file for table '" + this.game.get().getGameDisplayName() + "'?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Studio.client.getVpxService().deletePOV(this.game.get().getId());
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
    }
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public POVRepresentation getPOV() {
    return pov;
  }

  public void refreshView(Optional<GameRepresentation> g) {
    uploadBtn.setDisable(g.isEmpty());
    deleteBtn.setDisable(g.isEmpty());
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    deleteBtn.setDisable(true);
    reloadBtn.setDisable(true);

    povSoundVolumeSlider.setDisable(true);
    povMusicVolumeSlider.setDisable(true);

    if (g.isPresent()) {
      GameRepresentation game = g.get();

      boolean povAvailable = game.getPovPath() != null;
      deleteBtn.setDisable(!povAvailable);
      dataBox.setVisible(povAvailable);
      emptyDataBox.setVisible(!povAvailable);

      if (povAvailable) {
        pov = Studio.client.getVpxService().getPOV(game.getId());

        povSSAACombo.valueProperty().setValue(POVComboModel.forValue(pov.getValue(POV.SSAA)));
        povPostprocAACombo.valueProperty().setValue(POVPostProcComboModel.forValue(pov.getValue(POV.POST_PROC_AA)));
        povIngameAOCombo.valueProperty().setValue(POVComboModel.forValue(pov.getValue(POV.INGAME_AO)));
        povScSpReflectCombo.valueProperty().setValue(POVComboModel.forValue(pov.getValue(POV.SCSP_REFLECT)));
        povFpsLimiterCombo.valueProperty().setValue(POVComboModel.forValue(pov.getValue(POV.FPS_LIMITER)));

        povOverwriteDetailCheckbox.setSelected(pov.getBooleanValue(POV.OVERWRITE_DETAILS_LEVEL));
        povDetailsSlider.setValue(pov.getIntValue(POV.DETAILS_LEVEL));
        povDetailsSlider.setDisable(!pov.getBooleanValue(POV.OVERWRITE_DETAILS_LEVEL));

        povBallReflectionCombobox.setValue(POVComboModel.forValue(pov.getValue(POV.BALL_REFLECTION)));
        povBallTrailCombobox.setValue(POVComboModel.forValue(pov.getValue(POV.BALL_TRAIL)));
        int ballStrengthValue = (int) (pov.getDoubleValue(POV.BALL_TRAIL_STRENGTH) * 100);
        povBallTrailStrengthSpinner.getValueFactory().setValue(ballStrengthValue);

        povOverwriteNightDayCheckbox.setSelected(pov.getBooleanValue(POV.OVERWRITE_NIGHTDAY));
        povNighDaySpinner.setDisable(!pov.getBooleanValue(POV.OVERWRITE_NIGHTDAY));
        povNighDaySpinner.getValueFactory().setValue(pov.getIntValue(POV.NIGHTDAY_LEVEL));

        povGameDifficultySpinner.getValueFactory().setValue(pov.getDoubleValue(POV.GAMEPLAY_DIFFICULTY));

        povSoundVolumeSlider.setDisable(false);
        povSoundVolumeSlider.setValue(pov.getIntValue(POV.SOUND_VOLUME));

        povMusicVolumeSlider.setDisable(false);
        povMusicVolumeSlider.setValue(pov.getIntValue(POV.MUSIC_VOLUME));

        povRotationFullscreenSpinner.getValueFactory().setValue(pov.getIntValue(POV.FULLSCREEN_ROTATION));
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}