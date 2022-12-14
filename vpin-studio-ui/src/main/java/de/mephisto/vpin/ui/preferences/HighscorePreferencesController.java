package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class HighscorePreferencesController implements Initializable {

  @FXML
  private TextField titlesField;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    PreferenceEntryRepresentation entry = OverlayWindowFX.client.getPreference(PreferenceNames.HIGHSCORE_TITLES);

    String titles = entry.getValue();
    if (StringUtils.isEmpty(titles)) {
      titles = "GRAND CHAMPION"; //always valid
    }
    titlesField.setText(titles);

    titlesField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(PreferenceNames.HIGHSCORE_TITLES, () -> {
      client.setPreference(PreferenceNames.HIGHSCORE_TITLES, t1);
    }, 500));
  }
}
