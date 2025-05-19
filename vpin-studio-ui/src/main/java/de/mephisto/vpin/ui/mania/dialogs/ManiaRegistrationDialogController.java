package de.mephisto.vpin.ui.mania.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.mania.ManiaRegistration;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ManiaRegistrationDialogController implements DialogController, Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaRegistrationDialogController.class);

  @FXML
  private Button okButton;

  @FXML
  private VBox playerList;

  @FXML
  private VBox playersRoot;

  @FXML
  private CheckBox registrationCheckbox;

  @FXML
  private CheckBox synchronizePlayCountCheckbox;

  @FXML
  private CheckBox synchronizeTablesCheckbox;

  @FXML
  private CheckBox synchronizeRatingsCheckbox;

  private final List<CheckBox> playerCheckboxes = new ArrayList<>();

  private ManiaRegistration maniaRegistration;

  @Override
  public void onDialogCancel() {

  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDialogSubmit(ActionEvent e) {
    maniaRegistration = new ManiaRegistration();
    maniaRegistration.setSubmitRatings(synchronizeRatingsCheckbox.isSelected());
    maniaRegistration.setSubmitPlayCount(synchronizeRatingsCheckbox.isSelected());
    maniaRegistration.setSubmitTables(synchronizeTablesCheckbox.isSelected());

    for (CheckBox playerCheckbox : playerCheckboxes) {
      if (playerCheckbox.isSelected()) {
        PlayerRepresentation playerRepresentation = (PlayerRepresentation) playerCheckbox.getUserData();
        maniaRegistration.getPlayerIds().add(playerRepresentation.getId());
      }
    }

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    playerList.managedProperty().bindBidirectional(playerList.visibleProperty());
    playersRoot.managedProperty().bindBidirectional(playersRoot.visibleProperty());

    synchronizeRatingsCheckbox.setDisable(true);
    synchronizeRatingsCheckbox.setSelected(true);
    synchronizePlayCountCheckbox.setDisable(true);
    synchronizePlayCountCheckbox.setSelected(true);
    synchronizeTablesCheckbox.setDisable(true);
    synchronizeTablesCheckbox.setSelected(true);

    registrationCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        synchronizeRatingsCheckbox.setDisable(!newValue);
        synchronizePlayCountCheckbox.setDisable(!newValue);
        synchronizeTablesCheckbox.setDisable(!newValue);
        okButton.setDisable(!newValue);

        for (CheckBox playerCheckbox : playerCheckboxes) {
          playerCheckbox.setDisable(!newValue);
        }
      }
    });

    List<PlayerRepresentation> players = client.getPlayerService().getPlayers();
    playersRoot.setVisible(!players.isEmpty());

    for (PlayerRepresentation player : players) {
      CheckBox checkBox = new CheckBox(player.getName());
      checkBox.setUserData(player);
      checkBox.getStyleClass().add("default-text");
      checkBox.setSelected(true);
      checkBox.setDisable(true);

      playerList.getChildren().add(checkBox);
      playerCheckboxes.add(checkBox);
    }
  }

  public ManiaRegistration getManiaRegistration() {
    return maniaRegistration;
  }
}
