package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class EventLogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(EventLogController.class);

  @FXML
  private TextArea textArea;

  @FXML
  private Label titleLabel;

  private GameRepresentation game;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.textArea.setText(game.getNotes());
    this.titleLabel.setText("Event Log for \"" + game.getGameDisplayName() + "\"");

    HighscoreEventLog eventLog = client.getGameService().getEventLog(game.getId());

    if (eventLog != null) {
      textArea.setText(eventLog.toString());
    }
  }

  @Override
  public void onDialogCancel() {

  }
}
