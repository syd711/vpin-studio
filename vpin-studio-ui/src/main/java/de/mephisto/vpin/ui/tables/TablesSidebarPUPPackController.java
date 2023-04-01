package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarPUPPackController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPUPPackController.class);

  private VPinStudioClient client;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarPUPPackController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = Studio.client;
  }


  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}