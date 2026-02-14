package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class PlayerRankingsPreferencesController implements Initializable {

  @FXML
  private Spinner<Integer> spinner1;

  @FXML
  private Spinner<Integer> spinner2;

  @FXML
  private Spinner<Integer> spinner3;

  @FXML
  private Spinner<Integer> spinnerCompetitions;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
    spinner1.setValueFactory(factory1);

    SpinnerValueFactory.IntegerSpinnerValueFactory factory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
    spinner2.setValueFactory(factory2);

    SpinnerValueFactory.IntegerSpinnerValueFactory factory3 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
    spinner3.setValueFactory(factory3);

    SpinnerValueFactory.IntegerSpinnerValueFactory factoryC = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
    spinnerCompetitions.setValueFactory(factoryC);

    PreferenceEntryRepresentation idle = ServerFX.client.getPreferenceService().getPreference(PreferenceNames.RANKING_POINTS);
    String pointsString = idle.getValue();
    if(pointsString == null) {
      pointsString = UIDefaults.DEFAULT_POINTS;
    }

    String[] split = pointsString.split(",");
    if(split.length == 4) {
        spinner1.getValueFactory().setValue(Integer.parseInt(split[0]));
        spinner2.getValueFactory().setValue(Integer.parseInt(split[1]));
        spinner3.getValueFactory().setValue(Integer.parseInt(split[2]));
        spinnerCompetitions.getValueFactory().setValue(Integer.parseInt(split[3]));
    }
    else {
      client.getPreferenceService().setPreference(PreferenceNames.RANKING_POINTS, UIDefaults.DEFAULT_POINTS);
    }

    factory1.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      savePoints();
    }, 500));

    factory2.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      savePoints();
    }, 500));

    factory3.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      savePoints();
    }, 500));

    spinnerCompetitions.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      savePoints();
    }, 500));
  }

  private void savePoints() {
    String points = spinner1.getValue() + "," + spinner2.getValue() + "," + spinner3.getValue() + "," + spinnerCompetitions.getValue();
    client.getPreferenceService().setPreference(PreferenceNames.RANKING_POINTS, points);
  }
}
