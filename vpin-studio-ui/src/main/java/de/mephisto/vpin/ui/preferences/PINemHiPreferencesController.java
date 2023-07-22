package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PINemHiPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PINemHiPreferencesController.class);

  private final static String SETTING_REPLAYS = "replays";
  private final static String SETTING_HIGHSCORES = "hiscores";
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

  private final static String SETTING_PERSONAL_SCIRES = "personal_scores";
  private final static String SETTING_SPECIAL_SCORES = "personal_special_scores";
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

  @FXML
  private Button editBtn;

  @FXML
  private CheckBox autoStart;

  @FXML
  private CheckBox replays;

  @FXML
  private CheckBox hiscores;

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
  private ComboBox<String> voice;

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
  private ComboBox<String> challengesSkill;

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
  private Button restartBtn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  @FXML
  private void onStartToggle() {

  }

  @FXML
  private void onEdit() {

  }

  @FXML
  private void onRestart() {

  }

  private void saveOptions() {

    try {
//      Studio.client.getMameService().saveOptions(options);
    } catch (Exception e) {
      LOG.error("Failed to save mame settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save mame settings: " + e.getMessage());
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
  }
}
