package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.panels.PinVolSettingsController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PinVolSettingsDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox center;

  private PinVolSettingsController pinVolController;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Platform.runLater(() -> {
      pinVolController.save(true);
    });

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(PinVolSettingsController.class.getResource("pinvol-settings.fxml"));
      Parent builtInRoot = loader.load();
      pinVolController = loader.getController();
      center.getChildren().add(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load pinvol settings panel: " + e.getMessage(), e);
    }
  }

  @Override
  public void onDialogCancel() {
  }

  public void setData(Stage stage, List<GameRepresentation> games) {
    pinVolController.setData(stage, games, true);
  }
}
