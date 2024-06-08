package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.DatabaseLockException;
import de.mephisto.vpin.restclient.popper.PopperCustomOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PopperCustomOptionsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PopperCustomOptionsPreferencesController.class);
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
  private Spinner<Integer> wheelUpdateMS;

  @FXML
  private ComboBox<FadeoutLoading> fadeoutLoading;

  @FXML
  private Spinner<Integer> launchTimeoutSecs;

  @FXML
  private CheckBox joyAxisMove;

  @FXML
  private CheckBox volumeChange;

  private PopperCustomOptions customOptions;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    customOptions = Studio.client.getPinUPPopperService().getCustomOptions();

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0);
    delayReturn.setValueFactory(factory);
    factory.valueProperty().set(customOptions.getDelayReturn());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("customOptions", () -> {
        customOptions.setDelayReturn(t1);
        save();
      }, DEBOUNCE_MS);
    });

    returnNext.setSelected(customOptions.isReturnNext());
    returnNext.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      debouncer.debounce("returnNext", () -> {
        customOptions.setReturnNext(t1);
        save();
      }, DEBOUNCE_MS);
    });

    noSysFavs.setSelected(customOptions.isNoSysFavs());
    noSysFavs.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setNoSysFavs(t1);
      save();
    });

    noSysLists.setSelected(customOptions.isNoSysLists());
    noSysLists.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setNoSysLists(t1);
      save();
    });


    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3, 1);
    emuExitCount.setValueFactory(factory);
    factory.valueProperty().set(customOptions.getEmuExitCount());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("emuExitCount", () -> {
        customOptions.setEmuExitCount(t1);
        save();
      }, DEBOUNCE_MS);
    });

    playOnlyMode.setSelected(customOptions.isPlayOnlyMode());
    playOnlyMode.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setPlayOnlyMode(t1);
      save();
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, DEBOUNCE_MS);
    wheelAniTimeMS.setValueFactory(factory);
    factory.valueProperty().set(customOptions.getWheelAniTimeMS());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("wheelAniTimeMS", () -> {
        customOptions.setWheelAniTimeMS(t1);
        save();
      }, DEBOUNCE_MS);
    });

    showInfoInGame.setSelected(customOptions.isShowInfoInGame());
    showInfoInGame.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setShowInfoInGame(t1);
      save();
    });

    popUPHideAnykey.setSelected(customOptions.isPopUPHideAnykey());
    popUPHideAnykey.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setPopUPHideAnykey(t1);
      save();
    });


    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
    rapidFireCount.setValueFactory(factory);
    factory.valueProperty().set(customOptions.getRapidFireCount());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("rapidFireCount", () -> {
        customOptions.setRapidFireCount(t1);
        save();
      }, DEBOUNCE_MS);
    });

    pauseOnLoad.setSelected(customOptions.isPauseOnLoad());
    pauseOnLoad.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setPauseOnLoad(t1);
      save();
    });

    pauseOnLoadPF.setSelected(customOptions.isPauseOnLoadPF());
    pauseOnLoadPF.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setPauseOnLoadPF(t1);
      save();
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 240, 0);
    autoExitEmuSeconds.setValueFactory(factory);
    factory.valueProperty().set(customOptions.getAutoExitEmuSeconds());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("autoExitEmuSeconds", () -> {
        customOptions.setAutoExitEmuSeconds(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, 0);
    introSkipSeconds.setValueFactory(factory);
    factory.valueProperty().set(customOptions.getIntroSkipSeconds());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      customOptions.setIntroSkipSeconds(t1);
      save();
    });

    attractOnStart.setSelected(customOptions.isAttractOnStart());
    attractOnStart.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setAttractOnStart(t1);
      save();
    });

    muteLaunchAudio.setSelected(customOptions.isMuteLaunchAudio());
    muteLaunchAudio.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setMuteLaunchAudio(t1);
      save();
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 99, 30);
    wheelUpdateMS.setValueFactory(factory);
    factory.valueProperty().set(customOptions.getWheelUpdateMS());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("wheelUpdateMS", () -> {
        customOptions.setWheelUpdateMS(t1);
        save();
      }, DEBOUNCE_MS);
    });

    fadeoutLoading.setItems(FXCollections.observableList(FADEOUT_LOADINGS));
    fadeoutLoading.setValue(FADEOUT_LOADINGS.stream().filter(v -> v.getId() == customOptions.getFadeoutLoading()).findFirst().get());
    fadeoutLoading.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setFadeoutLoading(t1.getId());
      save();
    });


    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 240, 60);
    launchTimeoutSecs.setValueFactory(factory);
    factory.valueProperty().set(customOptions.getLaunchTimeoutSecs());
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("launchTimeoutSecs", () -> {
        customOptions.setLaunchTimeoutSecs(t1);
        save();
      }, DEBOUNCE_MS);
    });

    joyAxisMove.setSelected(customOptions.isJoyAxisMove());
    joyAxisMove.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setJoyAxisMove(t1);
      save();
    });

    volumeChange.setSelected(customOptions.isVolumeChange());
    volumeChange.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setVolumeChange(t1);
      save();
    });

    useAltWheels.setSelected(customOptions.isUseAltWheels());
    useAltWheels.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      customOptions.setUseAltWheels(t1);
      save();
    });
  }

  private void save() {
    try {
      if (Studio.client.getPinUPPopperService().isPinUPPopperRunning()) {
        if (Dialogs.openPopperRunningWarning(Studio.stage)) {
          Studio.client.getPinUPPopperService().saveCustomOptions(customOptions);
        }
        return;
      }
      Studio.client.getPinUPPopperService().saveCustomOptions(customOptions);

    } catch (DatabaseLockException e) {
      LOG.error("Failed to save custom options: " + e.getMessage(), e);
      if (!Dialogs.openPopperRunningWarning(Studio.stage)) {
        this.setDisabled(true);
      }
    } catch (Exception e) {
      LOG.error("Failed to save PinUP Popper custom options: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save PinUP Popper custom options: " + e.getMessage());
    }
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
