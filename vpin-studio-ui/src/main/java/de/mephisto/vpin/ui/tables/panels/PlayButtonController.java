package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.emulators.LaunchConfiguration;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.util.FrontendUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class PlayButtonController implements Initializable, ChangeListener<LaunchConfiguration> {
  private final static Logger LOG = LoggerFactory.getLogger(PlayButtonController.class);

  @FXML
  private ComboBox<LaunchConfiguration> launchCombo;

  @FXML
  private Button launchBtn;

  @FXML
  private HBox root;

  private GameRepresentation game;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.root.managedProperty().bindBidirectional(this.root.visibleProperty());
  }

  public void setVisible(boolean b) {
    this.root.setVisible(b);
  }

  @FXML
  public void onPlay() {
    LaunchConfiguration value = launchCombo.getValue();
    if (value != null) {
      if (value.isLaunchViaFrontend()) {
        new Thread(() -> {
          client.getFrontendService().launchGame(game.getId());
        }).start();
      }
      else {
        onPlay(game, value.getAltExe(), value.getOption());
      }
    }
  }

  public void setData(GameRepresentation game) {
    this.game = game;
    GameEmulatorRepresentation gameEmulator = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
    boolean disable = !isPlayable(game, gameEmulator);
    launchCombo.setDisable(disable);
    launchBtn.setDisable(disable);
    this.launchCombo.getItems().clear();

    JFXFuture.supplyAsync(
        () -> client.getEmulatorService().getAltExeNames(game.getEmulatorId())
    ).thenAcceptLater(objs -> {
      @SuppressWarnings("unchecked")
      List<String> altExeNames = objs;

      List<LaunchConfiguration> items = new ArrayList<>();

      String exeName = gameEmulator != null ? gameEmulator.getExeName() : null;
      if (!StringUtils.isEmpty(exeName)) {
        items.add(new LaunchConfiguration("Emulator Default", false, exeName, null));
      }

      if (client.getFrontendService().getFrontendCached().getFrontendType().equals(FrontendType.Popper)) {
        items.add(new LaunchConfiguration("Launch via PinUP Popper", true, null, null));
      }

      if (game.isVpxGame() && altExeNames != null) {
        for (String altExeName : altExeNames) {
          items.add(new LaunchConfiguration(altExeName, false, altExeName, null));
          if (isCameraModeSupported(altExeName)) {
            items.add(new LaunchConfiguration(altExeName + " [Camera Mode]", false, altExeName, "cameraMode"));
          }
        }
      }
      else if (game.isFpGame() && altExeNames != null) {
        for (String altExeName : altExeNames) {
          items.add(new LaunchConfiguration(altExeName, false, altExeName, null));
        }
      }

      launchCombo.setItems(FXCollections.observableList(items));
      launchCombo.setDisable(items.isEmpty());
      launchBtn.setDisable(items.isEmpty());

      if (!items.isEmpty()) {
        this.launchCombo.valueProperty().removeListener(this);
        launchCombo.getSelectionModel().select(0);
        UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
        LaunchConfiguration launchConfiguration = uiSettings.getLaunchConfiguration();
        if (launchConfiguration != null && launchCombo.getItems().contains(launchConfiguration)) {
          launchCombo.setValue(launchConfiguration);
        }
        this.launchCombo.valueProperty().addListener(this);
      }
    });
  }

  private boolean isPlayable(GameRepresentation game, GameEmulatorRepresentation gameEmulator) {
    if (game == null) {
      return false;
    }

    if (gameEmulator.isZaccariaEmulator() || gameEmulator.isFxEmulator()) {
      return true;
    }
    return game.getGameFilePath() != null;
  }

  private static boolean isCameraModeSupported(String altExeName) {
    return altExeName.startsWith("VPinballX");
  }

  public void setDisable(boolean b) {
    launchBtn.setDisable(b);
    launchCombo.setDisable(b);
  }

  public static void onPlay(GameRepresentation game, String altExe, String option) {
    if (game != null) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      if (uiSettings.isHideVPXStartInfo()) {
        client.getGameService().playGame(game.getId(), altExe, option);
        return;
      }

      Frontend frontend = client.getFrontendService().getFrontendCached();

      ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage,
          "Start playing table \"" + game.getGameDisplayName() + "\"?", "Start Table",
          FrontendUtil.replaceNames("All existing emulator and [Frontend] processes will be terminated.", frontend, "VPX"),
          null, "Do not show again", false);

      if (!confirmationResult.isApplyClicked()) {
        if (confirmationResult.isChecked()) {
          uiSettings.setHideVPXStartInfo(true);
          client.getPreferenceService().setJsonPreference(uiSettings);
        }
        client.getGameService().playGame(game.getId(), altExe, option);
      }
    }
  }

  @Override
  public void changed(ObservableValue<? extends LaunchConfiguration> observable, LaunchConfiguration oldValue, LaunchConfiguration newValue) {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    if (newValue != null) {
      uiSettings.setLaunchConfiguration(newValue);
      client.getPreferenceService().setJsonPreference(uiSettings);
    }
  }
}
