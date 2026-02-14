package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class EventLogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    this.textArea.setText(game.getComment());
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
