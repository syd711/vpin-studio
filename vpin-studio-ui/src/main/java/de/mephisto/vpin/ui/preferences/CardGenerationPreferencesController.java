package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.puppacks.PupPackRepresentation;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class CardGenerationPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(CardGenerationPreferencesController.class);
  public static Debouncer debouncer = new Debouncer();

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

  @FXML
  private VBox transparencyHelp;
  private PupPackRepresentation menuPupPack;

  @FXML
  private void onScreenHelp() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI("https://github.com/syd711/vpin-studio/wiki/Troubleshooting#why-do-my-transparent-highscore-cards-have-a-black-background"));
      } catch (Exception e) {
        LOG.error("Failed to open discord link: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    menuPupPack = client.getPupPackService().getMenuPupPack();
    validationError.managedProperty().bindBidirectional(validationError.visibleProperty());
    transparencyHelp.setVisible(false);

    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);

    popperScreenCombo.setItems(FXCollections.observableList(Arrays.asList("", PopperScreen.Other2.name(), PopperScreen.GameInfo.name(), PopperScreen.GameHelp.name())));
    popperScreenCombo.setValue(cardSettings.getPopperScreen() != null ? cardSettings.getPopperScreen() : "");
    popperScreenCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      cardSettings.setPopperScreen(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, cardSettings);
      onScreenChange();
    });

    rotationCombo.setItems(FXCollections.observableList(Arrays.asList("0", "90", "180", "270")));
    rotationCombo.setValue(cardSettings.getNotificationRotation());
    rotationCombo.valueProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        cardSettings.setNotificationRotation(newValue);
        client.getPreferenceService().setJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, cardSettings);
      }
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, cardSettings.getNotificationTime());
    highscoreCardDuration.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("cardDuration", () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      cardSettings.setNotificationTime(value1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, cardSettings);
    }, 500));

    boolean notificationOnPopperScreen = cardSettings.isNotificationOnPopperScreen();
    cardPosPopperRadio.setSelected(notificationOnPopperScreen);
    cardPosPlayfieldRadio.setSelected(!notificationOnPopperScreen);

    cardPosPlayfieldRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
      rotationCombo.setDisable(!newValue);
      cardPosPopperRadio.setSelected(!newValue);
      cardSettings.setNotificationOnPopperScreen(false);
      client.getPreferenceService().setJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, cardSettings);
    });

    cardPosPopperRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
      rotationCombo.setDisable(newValue);
      cardPosPlayfieldRadio.setSelected(!newValue);
      cardSettings.setNotificationOnPopperScreen(true);
      client.getPreferenceService().setJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, cardSettings);
    });

    onScreenChange();
  }

  private void onScreenChange() {
    String selectedItem = popperScreenCombo.getSelectionModel().getSelectedItem();
    highscoreCardDuration.setDisable(selectedItem == null);

    if (!StringUtils.isEmpty(selectedItem)) {
      PinUPControl fn = client.getPinUPPopperService().getPinUPControlFor(PopperScreen.valueOf(selectedItem));

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
    }
    else {
      validationError.setVisible(false);
    }

    transparencyHelp.setVisible(false);
    if (menuPupPack != null && popperScreenCombo.getValue() != null) {
      String value = popperScreenCombo.getValue();
      if(!StringUtils.isEmpty(value)) {PopperScreen screen = PopperScreen.valueOf(value);
      transparencyHelp.setVisible(
          (screen.equals(PopperScreen.GameHelp) && !menuPupPack.isHelpTransparency()) ||
              (screen.equals(PopperScreen.GameInfo) && !menuPupPack.isInfoTransparency()) ||
              (screen.equals(PopperScreen.Other2) && !menuPupPack.isOther2Transparency())
      );}
    }
  }
}
