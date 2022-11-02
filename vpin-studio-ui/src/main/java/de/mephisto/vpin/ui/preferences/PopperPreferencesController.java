package de.mephisto.vpin.ui.preferences;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PopperPreferencesController implements Initializable {

  @FXML
  private CheckBox pref_Audio;
  @FXML
  private CheckBox pref_AudioLaunch;
  @FXML
  private CheckBox pref_Other2;
  @FXML
  private CheckBox pref_GameInfo;
  @FXML
  private CheckBox pref_GameHelp;
  @FXML
  private CheckBox pref_Topper;
  @FXML
  private CheckBox pref_DMD;
  @FXML
  private CheckBox pref_Menu;

  @FXML
  private void onPreferenceChange(ActionEvent event) {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}
