package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class AliasMappingController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AliasMappingController.class);


  @FXML
  private TextField romNameField;

  @FXML
  private TextField aliasNameField;

  @FXML
  private Button saveBtn;

  private boolean result = false;
  private GameRepresentation game;
  private String existingAlias;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    String romName = this.romNameField.getText();
    String aliasName = this.aliasNameField.getText();

    Studio.client.getRomService().saveAliasMapping(existingAlias, aliasName, romName);
    EventManager.getInstance().notifyTableChange(game.getId(), null);

    EventManager.getInstance().notifyTableChange(game.getId(), romName);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;

    this.romNameField.textProperty().addListener((observable, oldValue, newValue) -> validate());
    this.aliasNameField.textProperty().addListener((observable, oldValue, newValue) -> validate());
  }

  private void validate() {
    String romName = this.romNameField.getText();
    String aliasName = this.aliasNameField.getText();

    saveBtn.setDisable(StringUtils.isEmpty(romName) || StringUtils.isEmpty(aliasName));
  }

  public void setValues(GameRepresentation game, String existingAlias, String rom) {
    this.game = game;
    if (StringUtils.isEmpty(rom)) {
      rom = existingAlias;
      existingAlias = null;
    }

    if (existingAlias != null) {
      this.aliasNameField.setText(existingAlias);
    }
    if (rom != null) {
      this.romNameField.setText(rom);
    }

    this.existingAlias  = this.aliasNameField.getText();
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }
}
