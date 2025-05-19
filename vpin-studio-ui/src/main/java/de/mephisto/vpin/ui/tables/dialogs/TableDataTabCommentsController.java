package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
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

public class TableDataTabCommentsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TableDataTabCommentsController.class);

  @FXML
  private TextArea textArea;

  @FXML
  private Label useTodoLabel;

  @FXML
  private Label useErrorLabel;

  @FXML
  private Label useOutdatedLabel;

  private GameRepresentation game;

  public void save() {
    game.setComment(textArea.getText());
    try {
      client.getGameService().saveGame(game);
    }
    catch (Exception e) {
      LOG.error("Failed to save notes: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save notes: " + e.getMessage());
      });
    }
  }


  @FXML
  private void onDelete() {
    this.textArea.setText(null);
    game.setComment(null);
  }

  private void appendTextAndFocus(String text) {
    this.textArea.insertText(this.textArea.getCaretPosition(), text);
    this.textArea.requestFocus();
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.textArea.setText(game.getComment());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    useTodoLabel.setOnMouseClicked(mouseEvent -> appendTextAndFocus("//TODO "));
    useErrorLabel.setOnMouseClicked(mouseEvent -> appendTextAndFocus("//ERROR "));
    useOutdatedLabel.setOnMouseClicked(mouseEvent -> appendTextAndFocus("//OUTDATED "));
  }
}
