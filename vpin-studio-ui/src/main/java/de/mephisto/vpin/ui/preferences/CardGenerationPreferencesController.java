package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.frontend.FrontendControl;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.puppacks.PupPackRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class CardGenerationPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(CardGenerationPreferencesController.class);
  public static Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<String> cardTargetScreenCombo;

  @FXML
  private CheckBox cardBackupAssetCheckbox;

  @FXML
  private ComboBox<String> rotationCombo;

  @FXML
  private ComboBox<String> cardSizeCombo;

  @FXML
  private Label validationError;

  @FXML
  private Label popperScreenInfo;

  @FXML
  private Spinner<Integer> highscoreCardDuration;

  @FXML
  private RadioButton cardPosPopperRadio;

  @FXML
  private RadioButton cardPosPlayfieldRadio;

  @FXML
  private VBox transparencyHelp;
  private PupPackRepresentation menuPupPack;
  private CardSettings cardSettings;
  private ResolutionChangeListener resolutionChangeListener;

  @FXML
  private void onScreenHelp() {
    Studio.browse("https://github.com/syd711/vpin-studio/wiki/Troubleshooting#why-do-my-transparent-highscore-cards-have-a-black-background");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    validationError.managedProperty().bindBidirectional(validationError.visibleProperty());
    transparencyHelp.setVisible(false);

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    popperScreenInfo.setVisible(frontendType.equals(FrontendType.Popper));

    ObservableList<String> screenNames = FXCollections.observableList(new ArrayList<>());
    if (frontendType.equals(FrontendType.Popper)) {
      menuPupPack = client.getPupPackService().getMenuPupPack();
      screenNames.addAll("", VPinScreen.Other2.name(), VPinScreen.GameInfo.name(), VPinScreen.GameHelp.name());
    }
    // for other frontends supporting medias (pinballX and pinballY)
    else if (Features.MEDIA_ENABLED) {
      screenNames.addAll("", VPinScreen.Topper.name(), VPinScreen.DMD.name(), VPinScreen.Menu.name());
    }

    cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);

    cardTargetScreenCombo.setItems(screenNames);
    cardTargetScreenCombo.setValue(cardSettings.getPopperScreen() != null ? cardSettings.getPopperScreen() : "");
    cardTargetScreenCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      Platform.runLater(() -> {
        cardSettings.setPopperScreen(newValue);
        client.getPreferenceService().setJsonPreference(cardSettings);
        onScreenChange();
      });
    });

    cardBackupAssetCheckbox.setSelected(cardSettings.isBackupAsset());
    cardBackupAssetCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      Platform.runLater(() -> {
        cardSettings.setBackupAsset(newValue);
        client.getPreferenceService().setJsonPreference(cardSettings);
      });
    });

    resolutionChangeListener = new ResolutionChangeListener();
    cardSizeCombo.setItems(FXCollections.observableList(Arrays.asList(CardResolution.qHD.toString(), CardResolution.HDReady.toString(), CardResolution.HD.toString())));
    cardSizeCombo.setValue(cardSettings.getCardResolution() != null ? cardSettings.getCardResolution().toString() : CardResolution.HDReady.toString());
    cardSizeCombo.valueProperty().addListener(resolutionChangeListener);

    rotationCombo.setItems(FXCollections.observableList(Arrays.asList("0", "90", "180", "270")));
    rotationCombo.setValue(cardSettings.getNotificationRotation());
    rotationCombo.valueProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        cardSettings.setNotificationRotation(newValue);
        client.getPreferenceService().setJsonPreference(cardSettings);
      }
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, cardSettings.getNotificationTime());
    highscoreCardDuration.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce("cardDuration", () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      cardSettings.setNotificationTime(value1);
      client.getPreferenceService().setJsonPreference(cardSettings);
    }, 500));

    boolean notificationOnScreen = cardSettings.isNotificationOnPopperScreen();
    cardPosPopperRadio.setSelected(notificationOnScreen);
    cardPosPlayfieldRadio.setSelected(!notificationOnScreen);

    cardPosPlayfieldRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
      rotationCombo.setDisable(!newValue);
      cardPosPopperRadio.setSelected(!newValue);
      cardSettings.setNotificationOnPopperScreen(false);
      client.getPreferenceService().setJsonPreference(cardSettings);
    });

    cardPosPopperRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
      rotationCombo.setDisable(newValue);
      cardPosPlayfieldRadio.setSelected(!newValue);
      cardSettings.setNotificationOnPopperScreen(true);
      client.getPreferenceService().setJsonPreference(cardSettings);
    });

    onScreenChange();
  }

  private void onScreenChange() {
    String selectedItem = cardTargetScreenCombo.getSelectionModel().getSelectedItem();
    highscoreCardDuration.setDisable(selectedItem == null);

    validationError.setVisible(false);

    if(Features.CONTROLS_ENABLED) {
      if (!StringUtils.isEmpty(selectedItem)) {
        FrontendControl fn = client.getFrontendService().getPinUPControlFor(VPinScreen.valueOf(selectedItem));

        String msg = null;
        if (fn != null) {
          if (!fn.isActive()) {
            msg = "The screen has not been activated in PinUP Popper.";
          }
          else if (fn.getCtrlKey() <= 0 && fn.getJoyCode() <= 0) {
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
      if (menuPupPack != null && cardTargetScreenCombo.getValue() != null) {
        String value = cardTargetScreenCombo.getValue();
        if (!StringUtils.isEmpty(value)) {
          VPinScreen screen = VPinScreen.valueOf(value);
          transparencyHelp.setVisible(
              (screen.equals(VPinScreen.GameHelp) && !menuPupPack.isHelpTransparency()) ||
                  (screen.equals(VPinScreen.GameInfo) && !menuPupPack.isInfoTransparency()) ||
                  (screen.equals(VPinScreen.Other2) && !menuPupPack.isOther2Transparency())
          );
        }
      }
    }

    EventManager.getInstance().notifyTableChange(-1, null);
  }

  class ResolutionChangeListener implements ChangeListener<String> {
    @Override
    public void changed(ObservableValue observable, String oldValue, String newValue) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Change Highscore Card Size?", "Change the default highscore card size to " + newValue + "?", "All table default backgrounds will be re-genererated. Revisit you highscore card layouts afterwards.", "Change Resolution");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        cardSettings.setCardResolution(CardResolution.valueOfString(newValue));
        client.getPreferenceService().setJsonPreference(cardSettings);
        Platform.runLater(() -> {
          ProgressDialog.createProgressDialog(new RegenerateMediaCacheProgressModel(client.getGameService().getVpxGamesCached()));
        });
      }
      else {
        cardSizeCombo.valueProperty().removeListener(resolutionChangeListener);
        cardSizeCombo.setValue(oldValue);
        cardSizeCombo.valueProperty().addListener(resolutionChangeListener);
      }
    }
  }
}
