package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TablePauseTestDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TablePauseTestDialogController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Button cancelBtn;

  @FXML
  private Button testBtn;

  @FXML
  private ComboBox<GameRepresentation> tablesCombo;

  @FXML
  private Spinner<Integer> timeSpinner;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onTestClick(ActionEvent e) {
    JFXFuture.runAsync(() -> {
      client.getSystemService().testPauseMenu(tablesCombo.getSelectionModel().getSelectedItem(), timeSpinner.getValueFactory().getValue());
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);
    List<GameRepresentation> gamesCached = client.getGameService().getVpxGamesCached();
    Collections.sort(gamesCached, new Comparator<GameRepresentation>() {
      @Override
      public int compare(GameRepresentation o1, GameRepresentation o2) {
        return o1.getGameDisplayName().compareTo(o2.getGameDisplayName());
      }
    });

    tablesCombo.setItems(FXCollections.observableList(gamesCached));
    tablesCombo.getSelectionModel().select(0);

    for (GameRepresentation gameRepresentation : gamesCached) {
      if (gameRepresentation.getId() == pauseMenuSettings.getTestGameId()) {
        tablesCombo.setValue(gameRepresentation);
        break;
      }
    }

    tablesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setTestGameId(newValue.getId());
      client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, pauseMenuSettings.getTestDuration());
    timeSpinner.setValueFactory(factory);
    timeSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
      @Override
      public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
        debouncer.debounce("pauseMenuTestTime", () -> {
          pauseMenuSettings.setTestDuration(newValue);
        }, 500);
      }
    });

    testBtn.setDisable(gamesCached.isEmpty());
  }

  @Override
  public void onDialogCancel() {
  }
}
