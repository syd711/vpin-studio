package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
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

  public boolean save() {
    game.setComment(textArea.getText());
    try {
      client.getGameService().saveGame(game);
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to save notes: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save notes: " + e.getMessage());
      });
      return false;
    }
  }

  @FXML
  private void onDelete() {
    this.textArea.clear();
  }

  private void appendTextAndFocus(String text) {
    this.textArea.insertText(this.textArea.getCaretPosition(), text);
    this.textArea.requestFocus();
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    if (StringUtils.isNotEmpty(game.getComment())) {
      this.textArea.setText(game.getComment());
    }
    else {
      this.textArea.clear();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    useTodoLabel.setOnMouseClicked(mouseEvent -> appendTextAndFocus("//TODO "));
    useErrorLabel.setOnMouseClicked(mouseEvent -> appendTextAndFocus("//ERROR "));
    useOutdatedLabel.setOnMouseClicked(mouseEvent -> appendTextAndFocus("//OUTDATED "));
  }

  public void initBindings(TableDataController tableDataController) {
    this.textArea.textProperty().addListener((obs, oldValue, newValue) -> {
      tableDataController.setDialogDirty(true);
    });
  }

}
