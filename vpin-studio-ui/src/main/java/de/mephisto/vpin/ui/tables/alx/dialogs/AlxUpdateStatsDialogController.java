package de.mephisto.vpin.ui.tables.alx.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class AlxUpdateStatsDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private Button saveBtn;

  @FXML
  private Spinner<Integer> timeSpinner;

  @FXML
  private Spinner<Integer> playsSpinner;
  @Nullable
  private GameRepresentation gameRepresentation;

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    try {
      client.getAlxService().updateTimePlayedForGame(gameRepresentation.getId(), timeSpinner.getValue());
    }
    catch (Exception ex) {
      LOG.error("Failed to store stats: " + ex.getMessage(), ex);
    }
    try {
      client.getAlxService().updateNumberOfPlaysForGame(gameRepresentation.getId(), playsSpinner.getValue());
    }
    catch (Exception ex) {
      LOG.error("Failed to store stats: " + ex.getMessage(), ex);
    }
    stage.close();

    EventManager.getInstance().notifyAlxUpdate(gameRepresentation);
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }


  public void setData(@Nullable GameRepresentation gameRepresentation) {
    this.gameRepresentation = gameRepresentation;

    AlxSummary alxSummary = client.getAlxService().getAlxSummary(gameRepresentation.getId());
    int timePlayedSecs = 0;
    int numberOfPlays = 0;
    if (alxSummary != null && !alxSummary.getEntries().isEmpty()) {
      TableAlxEntry tableAlxEntry = alxSummary.getEntries().get(0);
      timePlayedSecs = tableAlxEntry.getTimePlayedSecs() / 60;
      numberOfPlays = tableAlxEntry.getNumberOfPlays();
    }
    else {
      this.timeSpinner.setDisable(true);
      this.playsSpinner.setDisable(true);
      this.saveBtn.setDisable(true);
    }

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, timePlayedSecs);
    timeSpinner.setValueFactory(factory);
    SpinnerValueFactory.IntegerSpinnerValueFactory factory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, numberOfPlays);
    playsSpinner.setValueFactory(factory2);
  }

  @Override
  public void onDialogCancel() {

  }
}
