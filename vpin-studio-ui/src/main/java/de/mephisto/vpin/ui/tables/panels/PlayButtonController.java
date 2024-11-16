package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.util.FrontendUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class PlayButtonController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PlayButtonController.class);

  @FXML
  private SplitMenuButton playBtn;

  private GameRepresentation game;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.playBtn.managedProperty().bindBidirectional(this.playBtn.visibleProperty());
    this.playBtn.setDisable(true);
  }

  public void setVisible(boolean b) {
    this.playBtn.setVisible(b);
  }

  @FXML
  public void onPlay() {
    onPlay(null);
  }

  public void setData(GameRepresentation game) {
    this.game = game;
    playBtn.getItems().clear();
    playBtn.setDisable(game == null || game.getGameFilePath() == null);

    if (playBtn.isDisabled()) {
      return;
    }

    if (client.getFrontendService().getFrontendCached().getFrontendType().equals(FrontendType.Popper)) {
      MenuItem item = new MenuItem("Launch via PinUP Popper");
      item.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          if (game != null) {
            new Thread(() -> {
              client.getFrontendService().launchGame(game.getId());
            }).start();
          }
        }
      });
      playBtn.getItems().add(item);
      playBtn.getItems().add(new SeparatorMenuItem());
    }

    GameEmulatorRepresentation gameEmulator = client.getFrontendService().getGameEmulator(game.getEmulatorId());
    List<String> altExeNames = gameEmulator.getAltExeNames();
    for (String altExeName : altExeNames) {
      MenuItem item = new MenuItem(altExeName);
      item.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          onPlay(altExeName);
        }
      });
      playBtn.getItems().add(item);
    }
  }

  public void setDisable(boolean b) {
    playBtn.setDisable(b);
  }

  public void onPlay(String altExe) {
    if (game != null) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      if (uiSettings.isHideVPXStartInfo()) {
        client.getGameService().playGame(game.getId(), altExe);
        return;
      }

      Frontend frontend = client.getFrontendService().getFrontendCached();

      ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage,
          "Start playing table \"" + game.getGameDisplayName() + "\"?", "Start Table",
          FrontendUtil.replaceNames("All existing [Emulator] and [Frontend]  processes will be terminated.", frontend, "VPX"),
          null, "Do not show again", false);

      if (!confirmationResult.isApplyClicked()) {
        if (confirmationResult.isChecked()) {
          uiSettings.setHideVPXStartInfo(true);
          client.getPreferenceService().setJsonPreference(uiSettings);
        }
        client.getGameService().playGame(game.getId(), altExe);
      }
    }
  }
}
