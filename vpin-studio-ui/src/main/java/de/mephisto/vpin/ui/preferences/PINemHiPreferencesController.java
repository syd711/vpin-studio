package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.util.ini.IniSettings;
import de.mephisto.vpin.restclient.util.ini.IniSettingsChangeListener;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Keys;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class PINemHiPreferencesController implements Initializable, IniSettingsChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(PINemHiPreferencesController.class);

  private final static String SETTING_REPLAYS = "replays";
  private final static String SETTING_BUYINS = "buyins";
  private final static String SETTING_NAME = "name";
  private final static String SETTING_CODE = "code";
  private final static String SETTING_KEY = "key";
  private final static String SETTING_CHALLENGE_KEY = "challengekey";
  private final static String SETTING_WEEKLY_CHALLENGE_KEY = "weeklychallengekey";
  private final static String SETTING_5MIN_KEY = "5min_key";
  private final static String SETTING_EXIT_KEY = "pinupsystem_exit_key";
  private final static String SETTING_5MIN_MODE_STATUS = "5minute_mode_status";
  private final static String SETTING_5MIN_MODE_WARNING = "5minute_mode_gamewarning";
  private final static String SETTING_VOICE = "voice";

  private final static String SETTING_PERSONAL_SCORES = "personal_scores";
  private final static String SETTING_PERSONAL_SPECIAL_SCORES = "personal_special_scores";
  private final static String SETTING_BEST_SCORES = "best_scores";
  private final static String SETTING_FRIEND_SCORES = "friend_scores";
  private final static String SETTING_CUP_SCORES = "cup_scores";
  private final static String SETTING_BADGES = "badges";
  private final static String SETTING_CHALLENGES_SKILL = "challengeskill";
  private final static String SETTING_FRIEND1 = "friend1";
  private final static String SETTING_FRIEND2 = "friend2";
  private final static String SETTING_FRIEND3 = "friend3";
  private final static String SETTING_FRIEND4 = "friend4";
  private final static String SETTING_FRIEND5 = "friend5";
  private final static String SETTING_FRIEND6 = "friend6";
  private final static String SETTING_FRIEND7 = "friend7";
  private final static String SETTING_FRIEND8 = "friend8";
  private final static String SETTING_FRIEND9 = "friend9";

  private final static List<Voice> VOICES = Arrays.asList(new Voice(1, "female (american english)"),
      new Voice(2, "male (american english)"),
      new Voice(3, "frau (deutsch)"),
      new Voice(4, "man (deutsch)"),
      new Voice(5, "femme (francais)"),
      new Voice(6, "homme (francais)"),
      new Voice(7, "man (nederlands)"));

  private final static List<ChallengeSkill> SKILLS = Arrays.asList(new ChallengeSkill(1, "kiddie"),
      new ChallengeSkill(2, "normal"),
      new ChallengeSkill(3, "insane"));

  @FXML
  private Button editBtn;

  @FXML
  private CheckBox autoStart;

  @FXML
  private CheckBox replays;

  @FXML
  private CheckBox buyins;

  @FXML
  private TextField nameField;

  @FXML
  private TextField codeField;

  @FXML
  private ComboBox<String> keyKey;

  @FXML
  private ComboBox<String> keyChallange;

  @FXML
  private ComboBox<String> keyWeeklyChallange;

  @FXML
  private ComboBox<String> key5Minutes;

  @FXML
  private ComboBox<String> keyExit;

  @FXML
  private CheckBox sound5MinStatus;

  @FXML
  private CheckBox sound5MinWarning;

  @FXML
  private ComboBox<Voice> voice;

  @FXML
  private CheckBox onlinePersonalScores;

  @FXML
  private CheckBox onlinePersonalSpecialScores;

  @FXML
  private CheckBox onlineBestScore;

  @FXML
  private CheckBox onlineFriendScores;

  @FXML
  private CheckBox onlineCubScores;

  @FXML
  private CheckBox onlineBadges;

  @FXML
  private ComboBox<ChallengeSkill> challengesSkill;

  @FXML
  private TextField friend1;

  @FXML
  private TextField friend2;

  @FXML
  private TextField friend3;

  @FXML
  private TextField friend4;

  @FXML
  private TextField friend5;

  @FXML
  private TextField friend6;

  @FXML
  private TextField friend7;

  @FXML
  private TextField friend8;

  @FXML
  private TextField friend9;

  @FXML
  private Button stopBtn;

  @FXML
  private Button restartBtn;

  private IniSettings settings;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    stopBtn.setDisable(!Studio.client.getPINemHiService().isRunning());

    editBtn.setDisable(!Studio.client.getSystemService().isLocal());

    settings = Studio.client.getPINemHiService().getSettings();
    settings.setChangeListener(this);

    autoStart.setSelected(Studio.client.getPINemHiService().isAutoStartEnabled());
    autoStart.selectedProperty().addListener((observableValue, aBoolean, t1) -> Studio.client.getPINemHiService().toggleAutoStart());

    replays.setSelected(settings.getBoolean(SETTING_REPLAYS));
    replays.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_REPLAYS, t1));

    buyins.setSelected(settings.getBoolean(SETTING_BUYINS));
    buyins.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_BUYINS, t1));

    nameField.setText(settings.getString(SETTING_NAME));
    nameField.textProperty().addListener((observableValue, s, t1) -> settings.set(SETTING_NAME, t1));

    codeField.setText(settings.getString(SETTING_CODE));
    codeField.textProperty().addListener((observableValue, s, t1) -> settings.set(SETTING_CODE, t1));

    List<String> keyNames = Keys.getUIKeyNames();
    keyKey.setItems(FXCollections.observableList(keyNames));
    keyKey.setValue(settings.getString(SETTING_KEY));
    keyKey.valueProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_KEY, toKeyValue(t1)));

    keyChallange.setItems(FXCollections.observableList(keyNames));
    keyChallange.setValue(settings.getString(SETTING_CHALLENGE_KEY));
    keyChallange.valueProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_CHALLENGE_KEY, toKeyValue(t1)));

    keyWeeklyChallange.setItems(FXCollections.observableList(keyNames));
    keyWeeklyChallange.setValue(settings.getString(SETTING_WEEKLY_CHALLENGE_KEY));
    keyWeeklyChallange.valueProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_WEEKLY_CHALLENGE_KEY, toKeyValue(t1)));

    key5Minutes.setItems(FXCollections.observableList(keyNames));
    key5Minutes.setValue(settings.getString(SETTING_5MIN_KEY));
    key5Minutes.valueProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_5MIN_KEY, toKeyValue(t1)));

    keyExit.setItems(FXCollections.observableList(keyNames));
    keyExit.setValue(settings.getString(SETTING_EXIT_KEY));
    keyExit.valueProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_EXIT_KEY, toKeyValue(t1)));

    sound5MinStatus.setSelected(settings.getBoolean(SETTING_5MIN_MODE_STATUS));
    sound5MinStatus.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_5MIN_MODE_STATUS, t1));

    sound5MinWarning.setSelected(settings.getBoolean(SETTING_5MIN_MODE_WARNING));
    sound5MinWarning.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_5MIN_MODE_WARNING, t1));

    voice.setItems(FXCollections.observableList(VOICES));
    voice.setValue(VOICES.stream().filter(v -> v.getId() == settings.getInt(SETTING_VOICE)).findFirst().get());
    voice.valueProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_VOICE, t1.getId()));

    onlinePersonalScores.setSelected(settings.getBoolean(SETTING_PERSONAL_SCORES));
    onlinePersonalScores.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_PERSONAL_SCORES, t1));

    onlinePersonalSpecialScores.setSelected(settings.getBoolean(SETTING_PERSONAL_SPECIAL_SCORES));
    onlinePersonalSpecialScores.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_PERSONAL_SPECIAL_SCORES, t1));

    onlineBestScore.setSelected(settings.getBoolean(SETTING_BEST_SCORES));
    onlineBestScore.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_BEST_SCORES, t1));

    onlineFriendScores.setSelected(settings.getBoolean(SETTING_FRIEND_SCORES));
    onlineFriendScores.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_FRIEND_SCORES, t1));

    onlineCubScores.setSelected(settings.getBoolean(SETTING_CUP_SCORES));
    onlineCubScores.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_CUP_SCORES, t1));

    onlineBadges.setSelected(settings.getBoolean(SETTING_BADGES));
    onlineBadges.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_BADGES, t1));

    challengesSkill.setItems(FXCollections.observableList(SKILLS));
    challengesSkill.setValue(SKILLS.stream().filter(v -> v.getId() == settings.getInt(SETTING_CHALLENGES_SKILL)).findFirst().get());
    challengesSkill.valueProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_CHALLENGES_SKILL, t1.getId()));

    friend1.setText(settings.getString(SETTING_FRIEND1));
    friend1.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(SETTING_FRIEND1, () -> {
      settings.set(SETTING_FRIEND1, t1);
    }, 300));

    friend2.setText(settings.getString(SETTING_FRIEND2));
    friend2.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(SETTING_FRIEND2, () -> {
      settings.set(SETTING_FRIEND2, t1);
    }, 300));

    friend3.setText(settings.getString(SETTING_FRIEND3));
    friend3.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(SETTING_FRIEND3, () -> {
      settings.set(SETTING_FRIEND3, t1);
    }, 300));

    friend4.setText(settings.getString(SETTING_FRIEND4));
    friend4.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(SETTING_FRIEND4, () -> {
      settings.set(SETTING_FRIEND4, t1);
    }, 300));

    friend5.setText(settings.getString(SETTING_FRIEND5));
    friend5.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(SETTING_FRIEND5, () -> {
      settings.set(SETTING_FRIEND5, t1);
    }, 300));

    friend6.setText(settings.getString(SETTING_FRIEND6));
    friend6.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(SETTING_FRIEND6, () -> {
      settings.set(SETTING_FRIEND6, t1);
    }, 300));

    friend7.setText(settings.getString(SETTING_FRIEND7));
    friend7.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(SETTING_FRIEND7, () -> {
      settings.set(SETTING_FRIEND7, t1);
    }, 300));

    friend8.setText(settings.getString(SETTING_FRIEND8));
    friend8.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(SETTING_FRIEND8, () -> {
      settings.set(SETTING_FRIEND8, t1);
    }, 300));

    friend9.setText(settings.getString(SETTING_FRIEND9));
    friend9.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(SETTING_FRIEND9, () -> {
      settings.set(SETTING_FRIEND9, t1);
    }, 300));
  }

  private String toKeyValue(String value) {
    return Keys.toKeyValue(value);
  }

  @FXML
  private void onLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI("http://pinemhi.com/hiscores.php"));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onStop() {
    stopBtn.setDisable(!Studio.client.getPINemHiService().kill());
    stopBtn.setDisable(true);
  }

  @FXML
  private void onRestart() {
    restartBtn.setDisable(true);
    Platform.runLater(() -> {
      stopBtn.setDisable(!Studio.client.getPINemHiService().restart());
      stopBtn.setDisable(false);
      restartBtn.setDisable(false);
    });
  }

  @FXML
  private void onUIEdit() {
    PreferencesDialogs.openPINemHiUIDialog(settings);
  }

  @FXML
  private void onEdit() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.EDIT)) {
      try {
        desktop.edit(new File("resources/pinemhi", "pinemhi.ini"));
      } catch (Exception e) {
        LOG.error("Failed to open pinemhi.ini: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void changed(String key, Object value) {
    try {
      Studio.client.getPINemHiService().save(settings);
    } catch (Exception e) {
      LOG.error("Failed to save PINemHi settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save PINemHi settings: " + e.getMessage());
    }
  }


  static class Voice {
    private int id;
    private String name;

    public Voice(int id, String name) {
      this.id = id;
      this.name = name;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Voice)) return false;

      Voice voice = (Voice) o;

      return id == voice.id;
    }

    @Override
    public int hashCode() {
      return id;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  static class ChallengeSkill {
    private int id;
    private String name;

    public ChallengeSkill(int id, String name) {
      this.id = id;
      this.name = name;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ChallengeSkill)) return false;

      ChallengeSkill that = (ChallengeSkill) o;

      return id == that.id;
    }

    @Override
    public int hashCode() {
      return id;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
