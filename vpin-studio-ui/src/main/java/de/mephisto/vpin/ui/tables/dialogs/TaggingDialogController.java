package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.tags.TagField;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TaggingDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TaggingDialogController.class);

  @FXML
  private Pane tagsPane;

  @FXML
  private Button saveBtn;

  private TagField tagField;
  private List<GameRepresentation> games;

  @FXML
  private void onSave(ActionEvent e) {
    List<String> tags = tagField.getTags();
    List<GameRepresentation> g = this.games;

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    Platform.runLater(() -> {
      ProgressDialog.createProgressDialog(new BulkTaggingProgressModel(g, tags));
    });
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<String> initialTags = client.getTaggingService().getTags();
    tagField = new TagField(initialTags);
    tagField.setPreferredWidth(450);
    tagField.setPreferredHeight(96);
    tagsPane.getChildren().add(tagField);
    tagField.addListener(new ListChangeListener<String>() {
      @Override
      public void onChanged(Change<? extends String> c) {
        saveBtn.setDisable(c.getList().isEmpty());
      }
    });

    Platform.runLater(() -> {
      tagField.focus();
    });
  }

  public void setGames(List<GameRepresentation> games) {
    this.games = games;
  }

  @Override
  public void onDialogCancel() {

  }
}
