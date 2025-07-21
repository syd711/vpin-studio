package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.frontend.pinbally.PinballYSettings;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class PinballYSettingsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PinballYSettingsPreferencesController.class);

  private final static List<String> CHARSETS = Charset.availableCharsets().values().stream().map(c -> c.name()).collect(Collectors.toList());

  @FXML
  private ComboBox<String> charsetsCombo;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    PinballYSettings settings = client.getFrontendService().getSettings(PinballYSettings.class);

    String charset = settings.getCharset() == null ? Charset.defaultCharset().name() : settings.getCharset();
    charsetsCombo.setItems(FXCollections.observableList(CHARSETS));
    charsetsCombo.setValue(charset);

    charsetsCombo.valueProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        settings.setCharset(newValue);
        client.getPreferenceService().setJsonPreference(settings);
        //force reload
        PreferencesController.markDirty(PreferenceType.uiSettings);
      }
    });
  }


}
