package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class NotificationsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationsPreferencesController.class);

  @FXML
  private CheckBox highscoresCheckbox;

  @FXML
  private CheckBox highscoresCheckedCheckbox;

  @FXML
  private CheckBox startUpCheckbox;

  @FXML
  private CheckBox iScoredCheckbox;

  @FXML
  private CheckBox discordCheckbox;

  @FXML
  private CheckBox competitionsCheckbox;

  @FXML
  private CheckBox desktopCheckbox;

  @FXML
  private CheckBox recordingStartCheckbox;

  @FXML
  private CheckBox recordingEndCheckbox;

  @FXML
  private Spinner<Integer> durationSpinner;

  @FXML
  private ComboBox<MonitorInfo> screenInfoComboBox;

  @FXML
  private VBox iScoredSettings;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NotificationSettings notificationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);

    screenInfoComboBox.setItems(FXCollections.observableList(client.getSystemService().getSystemSummary().getScreenInfos()));
    if (notificationSettings.getNotificationsScreenId() == -1) {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getPrimaryScreen());
    }
    else {
      screenInfoComboBox.setValue(client.getSystemService().getSystemSummary().getScreenInfo(notificationSettings.getNotificationsScreenId()));
    }
    screenInfoComboBox.valueProperty().addListener(new ChangeListener<MonitorInfo>() {
      @Override
      public void changed(ObservableValue<? extends MonitorInfo> observable, MonitorInfo oldValue, MonitorInfo newValue) {
        notificationSettings.setNotificationsScreenId(newValue.getId());
        client.getPreferenceService().setJsonPreference(notificationSettings);
      }
    });

    int durationSec = notificationSettings.getDurationSec();
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, durationSec);
    durationSpinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      notificationSettings.setDurationSec(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    }, 300));

    desktopCheckbox.setSelected(notificationSettings.isDesktopMode());
    desktopCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      notificationSettings.setDesktopMode(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    });

    highscoresCheckbox.setSelected(notificationSettings.isHighscoreUpdatedNotification());
    highscoresCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      notificationSettings.setHighscoreUpdatedNotification(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    });

    highscoresCheckedCheckbox.setSelected(notificationSettings.isHighscoreCheckedNotification());
    highscoresCheckedCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      notificationSettings.setHighscoreCheckedNotification(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    });

    startUpCheckbox.setSelected(notificationSettings.isStartupNotification());
    startUpCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      notificationSettings.setStartupNotification(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    });

    iScoredCheckbox.setSelected(notificationSettings.isiScoredNotification());
    iScoredCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      notificationSettings.setiScoredNotification(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    });

    discordCheckbox.setSelected(notificationSettings.isDiscordNotification());
    discordCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      notificationSettings.setDiscordNotification(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    });

    competitionsCheckbox.setSelected(notificationSettings.isCompetitionNotification());
    competitionsCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      notificationSettings.setCompetitionNotification(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    });

    recordingStartCheckbox.setSelected(notificationSettings.isCompetitionNotification());
    recordingStartCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      notificationSettings.setRecordingStartNotification(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    });

    recordingEndCheckbox.setSelected(notificationSettings.isCompetitionNotification());
    recordingEndCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      notificationSettings.setRecordingEndNotification(t1);
      client.getPreferenceService().setJsonPreference(notificationSettings);
    });

    iScoredSettings.managedProperty().bindBidirectional(iScoredSettings.visibleProperty());
    iScoredSettings.setVisible(Features.ISCORED_ENABLED);
  }
}
