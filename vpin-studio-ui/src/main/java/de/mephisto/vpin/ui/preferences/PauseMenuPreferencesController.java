package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.tagging.TaggingSettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.tags.TagField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
  private CheckBox tutorialsCheckbox;

  @FXML
  private CheckBox triggerCheckbox;

  @FXML
  private CheckBox includeDmdCheckbox;

  @FXML
  private CheckBox todoCheckbox;

  @FXML
  private CheckBox rulesCheckbox;

  @FXML
  private CheckBox infoCardCheckbox;

  @FXML
  private Spinner<Integer> delaySpinner;

  @FXML
  private Spinner<Integer> scalingSpinner;

  @FXML
  private Spinner<Integer> visibleItemsSpinner;

  @FXML
  private Spinner<Integer> stageMarginLeftSpinner;

  @FXML
  private Spinner<Integer> stageMarginTopSpinner;

  @FXML
  private Spinner<Integer> tutorialMarginTopSpinner;

  @FXML
  private Spinner<Integer> tutorialMarginLeftSpinner;

  @FXML
  private ComboBox<MonitorInfo> screenInfoComboBox;

  @FXML
  private ComboBox<VPinScreen> screenTutorialComboBox;

  @FXML
  private ComboBox<Integer> rotationComboBox;

  @FXML
  private ComboBox<Integer> menuRotationComboBox;

  @FXML
  private RadioButton tutorialScreenRadio;

  @FXML
  private RadioButton tutorialItemRadio;

  @FXML
  private RadioButton viewModelDesktopRadio;
  @FXML
  private RadioButton viewModelCabinetRadio;
  @FXML
  private RadioButton viewModelApronRadio;

  @FXML
  private Pane tutorialDetailsBox;

  @FXML
  private VBox monitorsPane;

  @FXML
  private Pane tagPane;
  private TagField tagField;

  @FXML
  private void onPauseTest() {
    PreferencesDialogs.openPauseMenuTestDialog();
  }

  @FXML
  private void onDMDDeviceLink(ActionEvent event) {
    PreferencesController.navigate("dmd");
  }

  private TaggingSettings taggingSettings;

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

    SystemSummary systemSummary = client.getSystemService().getSystemSummary();
    for (MonitorInfo monitorInfo : systemSummary.getMonitorInfos()) {
      CheckBox checkBox = new CheckBox(monitorInfo.toDetailsString());
      checkBox.setSelected(pauseMenuSettings.getMultiScreenIds().contains(monitorInfo.getId()));
      checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          pauseMenuSettings.getMultiScreenIds().remove(Integer.valueOf(monitorInfo.getId()));
          if (newValue) {
            pauseMenuSettings.getMultiScreenIds().add(monitorInfo.getId());
          }
          client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        }
      });
      checkBox.getStyleClass().add("default-text");
      checkBox.setUserData(monitorInfo);
      monitorsPane.getChildren().add(checkBox);
    }

    taggingSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.TAGGING_SETTINGS, TaggingSettings.class);
    List<String> suggestions = client.getTaggingService().getTags();
    tagField = new TagField(suggestions);
    tagField.setAllowCustomTags(true);
    tagField.setTags(taggingSettings.getPauseMenuTags());
    tagField.addListener(new ListChangeListener<String>() {
      @Override
      public void onChanged(Change<? extends String> c) {
        List<String> list = (List<String>) c.getList();
        taggingSettings.setPauseMenuTags(new ArrayList<>(list));
        tagField.setDisable(list.size() == TaggingSettings.MAX_TODO_TAGS);
        client.getPreferenceService().setJsonPreference(taggingSettings);
      }
    });
    tagPane.getChildren().add(tagField);

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

    todoCheckbox.setSelected(pauseMenuSettings.isShowTodos());
    tagField.setDisable(!pauseMenuSettings.isShowTodos());
    todoCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowTodos(newValue);
      tagField.setDisable(!newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    tutorialsCheckbox.setSelected(pauseMenuSettings.isShowTutorials());
    setTutorialsDisabled(!pauseMenuSettings.isShowTutorials());
    tutorialsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowTutorials(newValue);
      setTutorialsDisabled(!newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    rulesCheckbox.setSelected(pauseMenuSettings.isShowRules());
    rulesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowRules(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    infoCardCheckbox.setSelected(pauseMenuSettings.isShowInfoCard());
    infoCardCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setShowInfoCard(newValue);
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

    SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(500, 10000, pauseMenuSettings.getUnpauseDelay());
    delaySpinner.setValueFactory(factory1);
    factory1.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("delaySpinner", () -> {
      pauseMenuSettings.setUnpauseDelay(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factoryScaling = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500, pauseMenuSettings.getScaling());
    scalingSpinner.setValueFactory(factoryScaling);
    factoryScaling.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("scalingSpinner", () -> {
      pauseMenuSettings.setScaling(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-10000, 10000, pauseMenuSettings.getTutorialMarginTop());
    tutorialMarginTopSpinner.setValueFactory(factory2);
    factory2.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("marginTopSpinner", () -> {
      pauseMenuSettings.setTutorialMarginTop(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory3 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-10000, 8000, pauseMenuSettings.getTutorialMarginLeft());
    tutorialMarginLeftSpinner.setValueFactory(factory3);
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

    SpinnerValueFactory.IntegerSpinnerValueFactory factory5 = new SpinnerValueFactory.IntegerSpinnerValueFactory(-10000, 8000, pauseMenuSettings.getStageOffsetX());
    stageMarginLeftSpinner.setValueFactory(factory5);
    factory5.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("stageMarginLeftSpinner", () -> {
      pauseMenuSettings.setStageOffsetX(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));


    SpinnerValueFactory.IntegerSpinnerValueFactory factory6 = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 6, pauseMenuSettings.getVisibleItemCount());
    visibleItemsSpinner.setValueFactory(factory6);
    factory6.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("visibleItemsSpinner", () -> {
      pauseMenuSettings.setVisibleItemCount(t1);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }, 300));

    menuRotationComboBox.setItems(FXCollections.observableList(Arrays.asList(0, 90, 180, 270)));
    menuRotationComboBox.setValue(pauseMenuSettings.getRotation());
    menuRotationComboBox.setDisable(pauseMenuSettings.isDesktopMode());
    menuRotationComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setRotation(newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    triggerCheckbox.setSelected(pauseMenuSettings.isPressPause());
    delaySpinner.setDisable(!pauseMenuSettings.isPressPause());
    triggerCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setPressPause(newValue);
      delaySpinner.setDisable(!newValue);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    includeDmdCheckbox.setSelected(pauseMenuSettings.isIncludeDmdFrame());
    includeDmdCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setIncludeDmdFrame(newValue);
      try {
        client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        PreferencesController.markDirty(PreferenceType.competitionSettings);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });

    //migration of old settings
    if (!pauseMenuSettings.isDesktopMode() && !pauseMenuSettings.isApronMode() && pauseMenuSettings.getRotation() == 0) {
      pauseMenuSettings.setDesktopMode(true);
      pauseMenuSettings.setCabinetMode(false);
      menuRotationComboBox.setDisable(true);
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    }

    viewModelDesktopRadio.setSelected(pauseMenuSettings.isDesktopMode());
    viewModelApronRadio.setSelected(pauseMenuSettings.isApronMode());
    viewModelCabinetRadio.setSelected(pauseMenuSettings.isCabinetMode());

    viewModelDesktopRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          pauseMenuSettings.setDesktopMode(true);
          pauseMenuSettings.setCabinetMode(false);
          pauseMenuSettings.setApronMode(false);
          menuRotationComboBox.setDisable(true);
          client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        }
      }
    });
    viewModelCabinetRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          pauseMenuSettings.setCabinetMode(true);
          pauseMenuSettings.setDesktopMode(false);
          pauseMenuSettings.setApronMode(false);
          menuRotationComboBox.setDisable(false);
          client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        }
      }
    });
    viewModelApronRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          pauseMenuSettings.setApronMode(true);
          pauseMenuSettings.setDesktopMode(false);
          pauseMenuSettings.setCabinetMode(false);
          menuRotationComboBox.setDisable(false);
          client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        }
      }
    });

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
          tutorialMarginLeftSpinner.setDisable(true);
          tutorialMarginTopSpinner.setDisable(true);
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
          tutorialMarginLeftSpinner.setDisable(false);
          tutorialMarginTopSpinner.setDisable(false);
          pauseMenuSettings.setTutorialsOnScreen(true);
          client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        }
      }
    });
  }

  private void setTutorialsDisabled(boolean b) {
    tutorialItemRadio.setDisable(b);
    tutorialScreenRadio.setDisable(b);
    screenTutorialComboBox.setDisable(b);
    rotationComboBox.setDisable(b);
    tutorialMarginLeftSpinner.setDisable(b);
    tutorialMarginTopSpinner.setDisable(b);
  }
}
