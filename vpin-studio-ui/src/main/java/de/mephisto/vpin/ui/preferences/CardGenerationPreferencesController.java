package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.util.properties.ObservedProperties;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CardGenerationPreferencesController implements Initializable {

  @FXML
  private ComboBox<String> popperScreenCombo;

  @FXML
  private ComboBox<String> rotationCombo;

  @FXML
  private Label validationError;

  @FXML
  private Spinner<Integer> highscoreCardDuration;

  @FXML
  private RadioButton cardPosPopperRadio;

  @FXML
  private RadioButton cardPosPlayfieldRadio;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ObservedProperties properties = Studio.client.getProperties(PreferenceNames.HIGHSCORE_CARD_SETTINGS);

    popperScreenCombo.setItems(FXCollections.observableList(Arrays.asList("", PopperScreen.Other2.name(), PopperScreen.GameInfo.name(), PopperScreen.GameHelp.name())));
    BindingUtil.bindComboBox(popperScreenCombo, properties, "popperScreen");
    popperScreenCombo.valueProperty().addListener((observable, oldValue, newValue) -> onScreenChange());

    rotationCombo.setItems(FXCollections.observableList(Arrays.asList("0", "90", "180", "270")));
    BindingUtil.bindComboBox(rotationCombo, properties, "notificationRotation");

    BindingUtil.bindSpinner(highscoreCardDuration, properties, "notificationTime", 0, 30);

    cardPosPlayfieldRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        rotationCombo.setDisable(!newValue);
        cardPosPopperRadio.setSelected(!newValue);
        properties.set("notificationOnPopperScreen", String.valueOf(!newValue));
      }
    });

    cardPosPopperRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        rotationCombo.setDisable(newValue);
        cardPosPlayfieldRadio.setSelected(!newValue);
        properties.set("notificationOnPopperScreen", String.valueOf(newValue));
      }
    });

    boolean notificationOnPopperScreen = properties.getProperty("notificationOnPopperScreen", false);
    cardPosPopperRadio.setSelected(notificationOnPopperScreen);
    cardPosPlayfieldRadio.setSelected(!notificationOnPopperScreen);

    onScreenChange();
  }

  private void onScreenChange() {
    String selectedItem = popperScreenCombo.getSelectionModel().getSelectedItem();
    highscoreCardDuration.setDisable(selectedItem == null);

    if (!StringUtils.isEmpty(selectedItem)) {
      PinUPControl fn = Studio.client.getPinUPPopperService().getPinUPControlFor(PopperScreen.valueOf(selectedItem));

      String msg = null;
      if (fn != null) {
        if (!fn.isActive()) {
          msg = "The screen has not been activated in PinUP Popper.";
        }

        if (fn.getCtrlKey() == 0) {
          msg = "The screen is not bound to any key in PinUP Popper.";
        }
      }

      validationError.setVisible(msg != null);
      validationError.setText(msg);
    } else {
      validationError.setVisible(false);
    }
  }
}
