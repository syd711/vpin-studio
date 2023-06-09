package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarVpsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarVpsController.class);

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarVpsController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
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