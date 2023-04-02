package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class TablesSidebarAudioController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarAudioController.class);

  @FXML
  private Slider volumeSlider;

  @FXML
  private Button altSoundBtn;

  @FXML
  private Label entriesLabel;

  @FXML
  private Label filesLabel;

  @FXML
  private Label bundleSizeLabel;

  private VPinStudioClient client;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private AltSound altSound;

  // Add a public no-args constructor
  public TablesSidebarAudioController() {
  }

  @FXML
  private void onAltSoundEdit() {
    if(game.isPresent() && game.get().isAltSoundAvailable()) {
      Dialogs.openAltSoundEditor(altSound);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = Studio.client;

    volumeSlider.valueProperty().addListener((observableValue, number, t1) -> {
      if (game.isPresent()) {
        final GameRepresentation g = game.get();
        debouncer.debounce("tableVolume" + g.getId(), () -> {
          int value = t1.intValue();
          if (value == 0) {
            value = 1;
          }

          if (g.getVolume() == value) {
            return;
          }

          g.setVolume(value);
          LOG.info("Updates volume of " + g.getGameDisplayName() + " to " + value);
          try {
            client.saveGame(g);
          } catch (Exception e) {
            WidgetFactory.showAlert(Studio.stage, e.getMessage());
          }
        }, 500);
      }
    });
  }


  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.altSound = null;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    altSoundBtn.setDisable(!game.isPresent() || !game.get().isAltSoundAvailable());

    if (g.isPresent()) {
      GameRepresentation game = g.get();

      if(game.isAltSoundAvailable()) {
        altSound = client.getAltSound(game.getId());
      }

      volumeSlider.setDisable(false);
      volumeSlider.setValue(game.getVolume());
    }
    else {
      volumeSlider.setValue(100);
      volumeSlider.setDisable(true);
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}