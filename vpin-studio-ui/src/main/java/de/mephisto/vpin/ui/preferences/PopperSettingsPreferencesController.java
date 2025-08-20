package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.DatabaseLockException;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.popper.PopperSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PopperSettingsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PopperSettingsPreferencesController.class);
  public static final int DEBOUNCE_MS = 500;

  private final Debouncer debouncer = new Debouncer();

  private final static List<FadeoutLoading> FADEOUT_LOADINGS = Arrays.asList(new FadeoutLoading(0, "Normal"),
      new FadeoutLoading(1, "Fade out video and audio"),
      new FadeoutLoading(2, "Continue playing until loading video ends or a key is pressed."));


  @FXML
  private Spinner<Integer> delayReturn;

  @FXML
  private CheckBox returnNext;

  @FXML
  private CheckBox noSysFavs;

  @FXML
  private CheckBox noSysLists;

  @FXML
  private Spinner<Integer> emuExitCount;

  @FXML
  private CheckBox playOnlyMode;

  @FXML
  private Spinner<Integer> wheelAniTimeMS;

  @FXML
  private CheckBox showInfoInGame;

  @FXML
  private CheckBox popUPHideAnykey;

  @FXML
  private Spinner<Integer> rapidFireCount;

  @FXML
  private CheckBox pauseOnLoad;

  @FXML
  private CheckBox pauseOnLoadPF;

  @FXML
  private Spinner<Integer> autoExitEmuSeconds;

  @FXML
  private Spinner<Integer> introSkipSeconds;

  @FXML
  private CheckBox attractOnStart;

  @FXML
  private CheckBox muteLaunchAudio;

  @FXML
  private CheckBox useAltWheels;

  @FXML
  private CheckBox watchDog;

  @FXML
  private TextField defaultMediaDirectory;

  @FXML
  private Spinner<Integer> wheelUpdateMS;

  @FXML
  private ComboBox<FadeoutLoading> fadeoutLoading;

  @FXML
  private Spinner<Integer> launchTimeoutSecs;

  @FXML
  private CheckBox joyAxisMove;

  @FXML
  private CheckBox volumeChange;

  private PopperSettings popperSettings;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    popperSettings = client.getFrontendService().getSettings(PopperSettings.class);

    defaultMediaDirectory.setText(popperSettings.getGlobalMediaDir());
    defaultMediaDirectory.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        debouncer.debounce("defaultMediaDirectory", () -> {
          popperSettings.setGlobalMediaDir(newValue);
          save();
        }, DEBOUNCE_MS);
      }
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0);
    delayReturn.setValueFactory(factory);
    factory.valueProperty().set(popperSettings.getDelayReturn());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("customOptions", () -> {
        popperSettings.setDelayReturn(t1);
        save();
      }, DEBOUNCE_MS);
    });

    returnNext.setSelected(popperSettings.isReturnNext());
    returnNext.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      debouncer.debounce("returnNext", () -> {
        popperSettings.setReturnNext(t1);
        save();
      }, DEBOUNCE_MS);
    });

    noSysFavs.setSelected(popperSettings.isNoSysFavs());
    noSysFavs.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setNoSysFavs(t1);
      save();
    });

    noSysLists.setSelected(popperSettings.isNoSysLists());
    noSysLists.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setNoSysLists(t1);
      save();
    });


    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3, 1);
    emuExitCount.setValueFactory(factory);
    factory.valueProperty().set(popperSettings.getEmuExitCount());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("emuExitCount", () -> {
        popperSettings.setEmuExitCount(t1);
        save();
      }, DEBOUNCE_MS);
    });

    playOnlyMode.setSelected(popperSettings.isPlayOnlyMode());
    playOnlyMode.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setPlayOnlyMode(t1);
      save();
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, DEBOUNCE_MS);
    wheelAniTimeMS.setValueFactory(factory);
    factory.valueProperty().set(popperSettings.getWheelAniTimeMS());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("wheelAniTimeMS", () -> {
        popperSettings.setWheelAniTimeMS(t1);
        save();
      }, DEBOUNCE_MS);
    });

    showInfoInGame.setSelected(popperSettings.isShowInfoInGame());
    showInfoInGame.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setShowInfoInGame(t1);
      save();
    });

    popUPHideAnykey.setSelected(popperSettings.isPopUPHideAnykey());
    popUPHideAnykey.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setPopUPHideAnykey(t1);
      save();
    });


    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
    rapidFireCount.setValueFactory(factory);
    factory.valueProperty().set(popperSettings.getRapidFireCount());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("rapidFireCount", () -> {
        popperSettings.setRapidFireCount(t1);
        save();
      }, DEBOUNCE_MS);
    });

    pauseOnLoad.setSelected(popperSettings.isPauseOnLoad());
    pauseOnLoad.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setPauseOnLoad(t1);
      save();
    });

    pauseOnLoadPF.setSelected(popperSettings.isPauseOnLoadPF());
    pauseOnLoadPF.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setPauseOnLoadPF(t1);
      save();
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 240, 0);
    autoExitEmuSeconds.setValueFactory(factory);
    factory.valueProperty().set(popperSettings.getAutoExitEmuSeconds());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("autoExitEmuSeconds", () -> {
        popperSettings.setAutoExitEmuSeconds(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, 0);
    introSkipSeconds.setValueFactory(factory);
    factory.valueProperty().set(popperSettings.getIntroSkipSeconds());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      popperSettings.setIntroSkipSeconds(t1);
      save();
    });

    attractOnStart.setSelected(popperSettings.isAttractOnStart());
    attractOnStart.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setAttractOnStart(t1);
      save();
    });

    muteLaunchAudio.setSelected(popperSettings.isMuteLaunchAudio());
    muteLaunchAudio.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setMuteLaunchAudio(t1);
      save();
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 99, 30);
    wheelUpdateMS.setValueFactory(factory);
    factory.valueProperty().set(popperSettings.getWheelUpdateMS());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("wheelUpdateMS", () -> {
        popperSettings.setWheelUpdateMS(t1);
        save();
      }, DEBOUNCE_MS);
    });

    fadeoutLoading.setItems(FXCollections.observableList(FADEOUT_LOADINGS));
    fadeoutLoading.setValue(FADEOUT_LOADINGS.stream().filter(v -> v.getId() == popperSettings.getFadeoutLoading()).findFirst().get());
    fadeoutLoading.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setFadeoutLoading(t1.getId());
      save();
    });


    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 240, 60);
    launchTimeoutSecs.setValueFactory(factory);
    factory.valueProperty().set(popperSettings.getLaunchTimeoutSecs());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("launchTimeoutSecs", () -> {
        popperSettings.setLaunchTimeoutSecs(t1);
        save();
      }, DEBOUNCE_MS);
    });

    joyAxisMove.setSelected(popperSettings.isJoyAxisMove());
    joyAxisMove.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setJoyAxisMove(t1);
      save();
    });

    volumeChange.setSelected(popperSettings.isVolumeChange());
    volumeChange.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setVolumeChange(t1);
      save();
    });

    useAltWheels.setSelected(popperSettings.isUseAltWheels());
    useAltWheels.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setUseAltWheels(t1);
      save();
    });

    watchDog.setSelected(popperSettings.isWatchDog());
    watchDog.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      popperSettings.setWatchDog(t1);
      save();
    });
  }

  private void save() {
    Platform.runLater(() -> {
      try {
        if (client.getFrontendService().isFrontendRunning()) {
          if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
            client.getFrontendService().saveSettings(popperSettings);
          }
          return;
        }
        client.getFrontendService().saveSettings(popperSettings);
      }
      catch (DatabaseLockException e) {
        LOG.error("Failed to save custom options: " + e.getMessage(), e);
        if (!Dialogs.openFrontendRunningWarning(Studio.stage)) {
          this.setDisabled(true);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to save PinUP Popper custom options: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save PinUP Popper custom options: " + e.getMessage());
      }
    });
  }

  private void setDisabled(boolean b) {
    delayReturn.setDisable(b);
    returnNext.setDisable(b);
    noSysFavs.setDisable(b);
    noSysLists.setDisable(b);
    emuExitCount.setDisable(b);
    playOnlyMode.setDisable(b);
    wheelAniTimeMS.setDisable(b);
    showInfoInGame.setDisable(b);
    popUPHideAnykey.setDisable(b);
    rapidFireCount.setDisable(b);
    pauseOnLoad.setDisable(b);
    pauseOnLoadPF.setDisable(b);
    autoExitEmuSeconds.setDisable(b);
    introSkipSeconds.setDisable(b);
    attractOnStart.setDisable(b);
    muteLaunchAudio.setDisable(b);
    fadeoutLoading.setDisable(b);
    launchTimeoutSecs.setDisable(b);
    wheelUpdateMS.setDisable(b);
    joyAxisMove.setDisable(b);
    volumeChange.setDisable(b);
  }

  static class FadeoutLoading {
    private int id;
    private String name;

    public FadeoutLoading(int id, String name) {
      this.id = id;
      this.name = name;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof FadeoutLoading)) return false;

      FadeoutLoading that = (FadeoutLoading) o;

      return id == that.id;
    }

    @Override
    public int hashCode() {
      return id;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
