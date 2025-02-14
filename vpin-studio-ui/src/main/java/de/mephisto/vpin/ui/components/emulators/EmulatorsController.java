package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EmulatorsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorsController.class);

  @FXML
  private BorderPane root;

  private EmulatorsTableController tableController;


  @FXML
  private void onReload() {
    tableController.reload();
  }

  public void setData(GameEmulatorRepresentation model) {
    tableController.setSelection(model);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(EmulatorsTableController.class.getResource("emulators-table.fxml"));
      Parent builtInRoot = loader.load();
      tableController = loader.getController();
      root.setLeft(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load emulator table: " + e.getMessage(), e);
    }
  }
}
