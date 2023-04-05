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

public class TablesSidebarPopperController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPopperController.class);

  @FXML
  private Slider volumeSlider;

  private VPinStudioClient client;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarPopperController() {
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
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    if (g.isPresent()) {
      GameRepresentation game = g.get();
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