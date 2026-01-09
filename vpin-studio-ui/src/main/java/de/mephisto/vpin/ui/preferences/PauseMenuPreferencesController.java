package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.*;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class PauseMenuPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenuPreferencesController.class);

  @FXML
  private CheckBox pauseMenuCheckbox;

  @FXML
  private Pane maniaScoresBox;

  @FXML
  private CheckBox maniaScoresCheckbox;

  @FXML
  private Pane iScoredScoresBox;

  @FXML
  private CheckBox iScoredScoresCheckbox;

  @FXML
  private CheckBox pauseMenuMuteCheckbox;

  @FXML
  private CheckBox pauseMenuOrientation;

  @FXML
  private CheckBox tutorialsCheckbox;

  @FXML
  private CheckBox triggerCheckbox;

  @FXML
  private CheckBox desktopModeCheckbox;

  @FXML
  private CheckBox apronModeCheckbox;

  @FXML
  private Spinner<Integer> delaySpinner;

  @FXML
  private Spinner<Integer> stageMarginLeftSpinner;

  @FXML
  private Spinner<Integer> stageMarginTopSpinner;

  @FXML
  private Spinner<Integer> marginTopSpinner;

  @FXML
  private Spinner<Integer> marginLeftSpinner;

  @FXML
  private ComboBox<MonitorInfo> screenInfoComboBox;

  @FXML
  private ComboBox<VPinScreen> screenTutorialComboBox;

  @FXML
  private ComboBox<Integer> rotationComboBox;

  @FXML
  private RadioButton tutorialScreenRadio;

  @FXML
  private RadioButton tutorialItemRadio;

  @FXML
  private Pane tutorialDetailsBox;

  @FXML
  private void onPauseTest() {
    PreferencesDialogs.openPauseMenuTestDialog();
  }


  @FXML
  private void onRestart() {
    Optional<ButtonType> result = WidgetFactory.showAlertOption(Studio.stage, "Server Restart", "Cancel", "Restart Server", "Are you sure you want to restart the VPin Studio Server?", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getSystemService().restart();
      ProgressDialog.createProgressDialog(new RestartProgressModel());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    maniaScoresBox.managedProperty().bindBidirectional(maniaScoresBox.visibleProperty());
    tutorialDetailsBox.managedProperty().bindBidirectional(tutorialDetailsBox.visibleProperty());
    maniaScoresBox.setVisible(Features.MANIA_ENABLED && maniaClient.getCabinetClient() != null);
    iScoredScoresBox.managedProperty().bindBidirectional(iScoredScoresBox.visibleProperty());
    iScoredScoresBox.setVisible(Features.ISCORED_ENABLED && pauseMenuSettings.isShowIscoredScores());

    screenInfoComboBox.setItems(FXCollections.observableList(client.getSystemService().getSystemSummary().getMonitorInfos()));
    if (pauseMenuSettings.getPauseMenuScreenId() == -1) {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getPrimaryMonitor());
    }
    else {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getMonitorInfo(pauseMenuSettings.getPauseMenuScreenId()));
    }
    screenInfoComboBox.valueProperty().addListener(new ChangeListener<MonitorInfo>() {
      @Override
      public void changed(ObservableValue<? extends MonitorInfo> observable, MonitorInfo oldValue, MonitorInfo newValue) {
        pauseMenuSettings.setPauseMenuScreenId(newValue.getId());
        client.getPreferenceService().setJsonPreference(pauseMenuSettings);
      }
    });

    iScoredScoresCheckbox.setSelected(pauseMenuSettings.isShowIscoredScores());
    iScoredScoresCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowIscoredScores(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    maniaScoresCheckbox.setSelected(pauseMenuSettings.isShowManiaScores());
    maniaScoresCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowManiaScores(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    tutorialsCheckbox.setSelected(pauseMenuSettings.isShowTutorials());
    tutorialDetailsBox.setVisible(tutorialsCheckbox.isSelected());
    tutorialsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowTutorials(newValue);
      tutorialDetailsBox.setVisible(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    pauseMenuCheckbox.setSelected(pauseMenuSettings.isUseOverlayKey());
    pauseMenuCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setUseOverlayKey(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    pauseMenuMuteCheckbox.setSelected(pauseMenuSettings.isMuteOnPause());
    pauseMenuMuteCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setMuteOnPause(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    pauseMenuOrientation.setSelected(pauseMenuSettings.getRotation() != 90);
    pauseMenuOrientation.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setRotation(newValue ? 0 : 90);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(500, 10000, pauseMenuSettings.getUnpauseDelay());
    delaySpinner.setValueFactory(factory1);
    factory1.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("delaySpinner", () -> {
      pauseMenuSettings.setUnpauseDelay(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-10000, 10000, pauseMenuSettings.getTutorialMarginTop());
    marginTopSpinner.setValueFactory(factory2);
    factory2.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("marginTopSpinner", () -> {
      pauseMenuSettings.setTutorialMarginTop(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory3 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-10000, 8000, pauseMenuSettings.getTutorialMarginLeft());
    marginLeftSpinner.setValueFactory(factory3);
    factory3.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("marginLeftSpinner", () -> {
      pauseMenuSettings.setTutorialMarginLeft(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory4 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-10000, 8000, pauseMenuSettings.getStageOffsetY());
    stageMarginTopSpinner.setValueFactory(factory4);
    factory4.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("stageMarginTopSpinner", () -> {
      pauseMenuSettings.setStageOffsetY(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory5 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-10000, 8000, pauseMenuSettings.getStageOffsetY());
    stageMarginLeftSpinner.setValueFactory(factory5);
    factory5.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("stageMarginLeftSpinner", () -> {
      pauseMenuSettings.setStageOffsetX(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));


    triggerCheckbox.setSelected(pauseMenuSettings.isPressPause());
    delaySpinner.setDisable(!pauseMenuSettings.isPressPause());
    triggerCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setPressPause(newValue);
      delaySpinner.setDisable(!newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    desktopModeCheckbox.setSelected(pauseMenuSettings.isDesktopUser());
    desktopModeCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setDesktopUser(newValue);
      try {
        client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        PreferencesController.markDirty(PreferenceType.competitionSettings);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });

    apronModeCheckbox.setSelected(pauseMenuSettings.isApronMode());
    apronModeCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setApronMode(newValue);
      try {
        client.getPreferenceService().setJsonPreference(pauseMenuSettings);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });


    screenTutorialComboBox.setDisable(!pauseMenuSettings.isTutorialsOnScreen());
    rotationComboBox.setDisable(!pauseMenuSettings.isTutorialsOnScreen());
    marginLeftSpinner.setDisable(!pauseMenuSettings.isTutorialsOnScreen());
    marginTopSpinner.setDisable(!pauseMenuSettings.isTutorialsOnScreen());
    Frontend frontend = client.getFrontendService().getFrontend();
    List<VPinScreen> screens = new ArrayList<>(frontend.getSupportedScreens());
    screens.remove(VPinScreen.Audio);
    screens.remove(VPinScreen.AudioLaunch);
    screens.remove(VPinScreen.Wheel);
    screens.remove(VPinScreen.Menu);
    screens.remove(VPinScreen.BackGlass);
    screens.remove(VPinScreen.PlayField);
    screens.remove(VPinScreen.Loading);
    screens.remove(VPinScreen.Logo);

    screenTutorialComboBox.setItems(FXCollections.observableList(screens));
    screenTutorialComboBox.setValue(pauseMenuSettings.getTutorialsScreen());
    screenTutorialComboBox.valueProperty().addListener(new ChangeListener<VPinScreen>() {
      @Override
      public void changed(ObservableValue<? extends VPinScreen> observable, VPinScreen oldValue, VPinScreen newValue) {
        pauseMenuSettings.setTutorialsScreen(newValue);
        client.getPreferenceService().setJsonPreference(pauseMenuSettings);
      }
    });

    rotationComboBox.setItems(FXCollections.observableList(Arrays.asList(0, 90, 180, 270)));
    rotationComboBox.setValue(pauseMenuSettings.getTutorialsRotation());
    rotationComboBox.valueProperty().addListener(new ChangeListener<Integer>() {
      @Override
      public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
        pauseMenuSettings.setTutorialsRotation(newValue);
        client.getPreferenceService().setJsonPreference(pauseMenuSettings);
      }
    });

    tutorialItemRadio.setSelected(!pauseMenuSettings.isTutorialsOnScreen());
    tutorialItemRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          screenTutorialComboBox.setDisable(true);
          rotationComboBox.setDisable(true);
          marginLeftSpinner.setDisable(true);
          marginTopSpinner.setDisable(true);
          pauseMenuSettings.setTutorialsOnScreen(false);
          client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        }
      }
    });

    tutorialScreenRadio.setSelected(pauseMenuSettings.isTutorialsOnScreen());
    tutorialScreenRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          screenTutorialComboBox.setDisable(false);
          rotationComboBox.setDisable(false);
          marginLeftSpinner.setDisable(false);
          marginTopSpinner.setDisable(false);
          pauseMenuSettings.setTutorialsOnScreen(true);
          client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        }
      }
    });
  }
}
