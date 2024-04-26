package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TablePauseTestDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TablePauseTestDialogController.class);

  @FXML
  private Button cancelBtn;

  @FXML
  private Button testBtn;

  @FXML
  private ComboBox<GameRepresentation> tablesCombo;

  @FXML
  private Spinner<Integer> timeSpinner;

  private static GameRepresentation selection;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onTestClick(ActionEvent e) {
    client.getSystemService().testPauseMenu(tablesCombo.getSelectionModel().getSelectedItem(), timeSpinner.getValueFactory().getValue());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<GameRepresentation> gamesCached = client.getGameService().getVpxGamesCached();

    List<GameRepresentation> filtered = gamesCached.stream().filter(g -> g.getHighscoreType() != null).collect(Collectors.toList());
    tablesCombo.setItems(FXCollections.observableList(filtered));

    if(selection != null) {
      tablesCombo.setValue(selection);
    }
    else {
      tablesCombo.getSelectionModel().select(0);
    }

    tablesCombo.valueProperty().addListener((observable, oldValue, newValue) -> selection = newValue);

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, 5);
    factory.setValue(5);
    timeSpinner.setValueFactory(factory);

    testBtn.setDisable(filtered.isEmpty());
  }

  @Override
  public void onDialogCancel() {
  }
}
