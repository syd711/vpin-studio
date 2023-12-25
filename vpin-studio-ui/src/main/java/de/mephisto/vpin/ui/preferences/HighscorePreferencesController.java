package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class HighscorePreferencesController implements Initializable {

  @FXML
  private TextField titlesField;

  @FXML
  private CheckBox filterCheckbox;

  private List<String> allowList = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    PreferenceEntryRepresentation entry = OverlayWindowFX.client.getPreference(PreferenceNames.HIGHSCORE_TITLES);

    String titles = entry.getValue();
    if (StringUtils.isEmpty(titles)) {
      titles = String.join(",", DefaultHighscoresTitles.DEFAULT_TITLES);
    }
    titlesField.setText(titles);

    titlesField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(PreferenceNames.HIGHSCORE_TITLES, () -> {
      client.getPreferenceService().setPreference(PreferenceNames.HIGHSCORE_TITLES, t1);
      PreferencesController.markDirty();
    }, 500));


    boolean filerEnabled = OverlayWindowFX.client.getPreference(PreferenceNames.HIGHSCORE_FILTER_ENABLED).getBooleanValue(false);
    filterCheckbox.setSelected(filerEnabled);
    filterCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      client.getPreferenceService().setPreference(PreferenceNames.HIGHSCORE_FILTER_ENABLED, t1);
    });
  }
}
