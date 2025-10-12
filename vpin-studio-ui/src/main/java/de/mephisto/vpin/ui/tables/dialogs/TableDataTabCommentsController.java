package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.tags.TagField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
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

  @FXML
  private Pane tags;

  private GameRepresentation game;
  private TagField tagField;

  public boolean save(TableDetails tableDetails) {
    game.setComment(textArea.getText());
    tableDetails.setTags(String.join(",", tagField.getTags()));
    return true;
  }

  @FXML
  private void onDelete() {
    this.textArea.clear();
  }

  private void appendTextAndFocus(String text) {
    this.textArea.insertText(this.textArea.getCaretPosition(), text);
    this.textArea.requestFocus();
  }

  public void setTags(List<String> tags) {
    tagField.setTags(tags);
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    if (StringUtils.isNotEmpty(game.getComment())) {
      this.textArea.setText(game.getComment());
    }
    else {
      this.textArea.clear();
    }

    this.tagField.setTags(game.getTags());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    useTodoLabel.setOnMouseClicked(mouseEvent -> appendTextAndFocus("//TODO "));
    useErrorLabel.setOnMouseClicked(mouseEvent -> appendTextAndFocus("//ERROR "));
    useOutdatedLabel.setOnMouseClicked(mouseEvent -> appendTextAndFocus("//OUTDATED "));

    List<String> initialTags = client.getTaggingService().getTags();
    tagField = new TagField(initialTags);
    tags.getChildren().add(tagField);
  }

  public void initBindings(TableDataController tableDataController) {
    this.textArea.textProperty().addListener((obs, oldValue, newValue) -> {
      tableDataController.setDialogDirty(true);
    });
  }

}
