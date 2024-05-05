package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TableNotesController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableNotesController.class);

  @FXML
  private Button saveBtn;

  @FXML
  private TextArea textArea;

  private GameRepresentation game;

  @FXML
  private void onSaveClick(ActionEvent ev) {
    game.setNotes(textArea.getText());
    try {
      client.getGameService().saveGame(game);
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    } catch (Exception e) {
      LOG.error("Failed to save notes: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save notes: " + e.getMessage());
      });
    }

    Stage stage = (Stage) ((Button) ev.getSource()).getScene().getWindow();
    stage.close();
  }


  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete", "Delete this comment?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      this.textArea.setText(null);
      onSaveClick(e);
    }
  }

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
  }

  @Override
  public void onDialogCancel() {

  }
}
