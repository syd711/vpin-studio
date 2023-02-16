package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.DeleteDescriptor;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class TableDeleteController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableDeleteController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private Button deleteBtn;

  @FXML
  private CheckBox vpxFileCheckbox;

  @FXML
  private CheckBox directb2sCheckbox;

  @FXML
  private CheckBox popperCheckbox;

  @FXML
  private CheckBox confirmationCheckbox;

  @FXML
  private CheckBox pupPackCheckbox;

  @FXML
  private CheckBox musicCheckbox;

  @FXML
  private CheckBox highscoreCheckbox;

  @FXML
  private CheckBox altSoundCheckbox;

  @FXML
  private CheckBox dmdCheckbox;

  @FXML
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Label validationDescription;

  private boolean result = false;
  private GameRepresentation game;

  @FXML
  private void onDeleteClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    DeleteDescriptor descriptor = new DeleteDescriptor();
    descriptor.setDeleteTable(vpxFileCheckbox.isSelected());
    descriptor.setDeleteDirectB2s(directb2sCheckbox.isSelected());
    descriptor.setDeleteFromPopper(popperCheckbox.isSelected());
    descriptor.setDeletePupPack(pupPackCheckbox.isSelected());
    descriptor.setDeleteDMDs(dmdCheckbox.isSelected());
    descriptor.setDeleteHighscores(highscoreCheckbox.isSelected());
    descriptor.setDeleteMusic(musicCheckbox.isSelected());
    descriptor.setDeleteAltSound(altSoundCheckbox.isSelected());
    descriptor.setGameId(game.getId());

    Studio.client.deleteGame(descriptor);
    result = true;
    stage.close();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.deleteBtn.setDisable(true);
    confirmationCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> deleteBtn.setDisable(!newValue));
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean tableDeleted() {
    return result;
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.titleLabel.setText(game.getGameDisplayName());

    boolean hasVpa = !game.getVpaUuids().isEmpty();
    this.validationContainer.setVisible(!hasVpa);
  }
}
