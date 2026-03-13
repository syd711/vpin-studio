package de.mephisto.vpin.ui.tables.editors.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.altsound.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class AltSound2ProfileDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private Label profileIdLabel;

  @FXML
  private ComboBox<String> sampleCombo;

  @FXML
  private Label musicLabel;

  @FXML
  private Label calloutLabel;

  @FXML
  private Label sfxLabel;

  @FXML
  private Label soloLabel;

  @FXML
  private Label overlayLabel;

  @FXML
  private Slider musicSlider;

  @FXML
  private Slider calloutSlider;

  @FXML
  private Slider sfxSlider;

  @FXML
  private Slider soloSlider;

  @FXML
  private Slider overlaySlider;

  @FXML
  private CheckBox musicCheckbox;

  @FXML
  private CheckBox calloutCheckbox;

  @FXML
  private CheckBox sfxCheckbox;

  @FXML
  private CheckBox soloCheckbox;

  @FXML
  private CheckBox overlayCheckbox;

  @FXML
  private Button saveBtn;

  private AltSound2DuckingProfile editorProfile;
  private AltSound altSound;

  @FXML
  private void onSaveClick(ActionEvent e) {
    //no action needed since we already have a full copy
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    editorProfile = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    editorProfile = new AltSound2DuckingProfile();
    sfxSlider.setDisable(true);
    sfxSlider.setDisable(true);

    sampleCombo.setItems(FXCollections.observableList(AltSound2SampleType.toProfilesStringValues()));
    sampleCombo.valueProperty().addListener((observableValue, s, t1) -> {
      AltSound2SampleType sampleType = AltSound2SampleType.valueOf(t1.toLowerCase());

      //apply sample type
      editorProfile.setType(sampleType);

      //apply profile id
      int profileId = 1;
      AltSound2DuckingProfile existingProfile = altSound.getProfile(sampleType, profileId);
      while (existingProfile != null) {
        profileId++;
        existingProfile = altSound.getProfile(sampleType, profileId);
      }

      this.saveBtn.setDisable(false);
      this.editorProfile.setId(profileId);
      this.profileIdLabel.setText(String.valueOf(profileId));

      AltSound2Group group = altSound.getGroup(sampleType);
      List<AltSound2SampleType> ducks = group.getDucks();
      for (AltSound2SampleType duck : ducks) {
        editorProfile.addProfileValue(duck, 0);
      }

      //disable the channel itself for selection
      refreshChannelDisable(Arrays.asList(AltSound2SampleType.valueOf(this.sampleCombo.getValue().toLowerCase())));
    });


    musicSlider.valueProperty().addListener((observableValue, number, t1) -> {
      musicLabel.setText(String.valueOf(t1.intValue()));
      editorProfile.getProfileValue(AltSound2SampleType.music).setVolume(t1.intValue());
    });
    calloutSlider.valueProperty().addListener((observableValue, number, t1) -> {
      calloutLabel.setText(String.valueOf(t1.intValue()));
      editorProfile.getProfileValue(AltSound2SampleType.callout).setVolume(t1.intValue());
    });
    sfxSlider.valueProperty().addListener((observableValue, number, t1) -> {
      sfxLabel.setText(String.valueOf(t1.intValue()));
      editorProfile.getProfileValue(AltSound2SampleType.sfx).setVolume(t1.intValue());
    });
    soloSlider.valueProperty().addListener((observableValue, number, t1) -> {
      soloLabel.setText(String.valueOf(t1.intValue()));
      editorProfile.getProfileValue(AltSound2SampleType.solo).setVolume(t1.intValue());
    });
    overlaySlider.valueProperty().addListener((observableValue, number, t1) -> {
      overlayLabel.setText(String.valueOf(t1.intValue()));
      editorProfile.getProfileValue(AltSound2SampleType.overlay).setVolume(t1.intValue());
    });
  }

  @Override
  public void onDialogCancel() {
    editorProfile = null;
  }

  public void setProfile(AltSound altSound, AltSound2DuckingProfile profile) {
    this.altSound = altSound;

    if (profile != null) {
      //copy all values to the working copy
      this.editorProfile.setType(profile.getType());
      this.editorProfile.setId(profile.getId());
      this.editorProfile.setValues(profile.getValues());
      this.sampleCombo.setValue(profile.getType().name().toUpperCase());
      this.profileIdLabel.setText(String.valueOf(profile.getId()));
      this.sampleCombo.setDisable(true);

      AltSound2Group group = altSound.getGroup(AltSound2SampleType.valueOf(this.sampleCombo.getValue().toLowerCase()));
      List<AltSound2SampleType> ducks = group.getDucks();
      refreshChannelDisable(ducks);
    }
    else {
      this.profileIdLabel.setText("-");
      this.saveBtn.setDisable(true);
    }


    AltSoundDuckingProfileValue profileValue = this.editorProfile.getProfileValue(AltSound2SampleType.music);
    if (profileValue != null) {
      musicSlider.setDisable(false);
      musicSlider.setValue(profileValue.getVolume());
      musicCheckbox.setSelected(true);
    }

    profileValue = this.editorProfile.getProfileValue(AltSound2SampleType.callout);
    if (profileValue != null) {
      calloutSlider.setDisable(false);
      calloutSlider.setValue(profileValue.getVolume());
      calloutCheckbox.setSelected(true);
    }

    profileValue = this.editorProfile.getProfileValue(AltSound2SampleType.solo);
    if (profileValue != null) {
      soloSlider.setDisable(false);
      soloSlider.setValue(profileValue.getVolume());
      soloCheckbox.setSelected(true);
    }

    profileValue = this.editorProfile.getProfileValue(AltSound2SampleType.sfx);
    if (profileValue != null) {
      sfxSlider.setDisable(false);
      sfxSlider.setValue(profileValue.getVolume());
      sfxCheckbox.setSelected(true);
    }

    profileValue = this.editorProfile.getProfileValue(AltSound2SampleType.overlay);
    if (profileValue != null) {
      overlaySlider.setDisable(false);
      overlaySlider.setValue(profileValue.getVolume());
      overlayCheckbox.setSelected(true);
    }
  }

  private void refreshChannelDisable(List<AltSound2SampleType> sampleTypes) {
    sfxCheckbox.setSelected(false);
    sfxSlider.setDisable(true);
    sfxLabel.setText("-");
    sfxLabel.setDisable(true);

    musicCheckbox.setSelected(false);
    musicSlider.setDisable(true);
    musicLabel.setText("-");
    musicLabel.setDisable(true);

    calloutCheckbox.setSelected(false);
    calloutSlider.setDisable(true);
    calloutLabel.setText("-");
    calloutLabel.setDisable(true);

    soloCheckbox.setSelected(false);
    soloSlider.setDisable(true);
    soloLabel.setText("-");
    soloLabel.setDisable(true);

    overlayCheckbox.setSelected(false);
    overlaySlider.setDisable(true);
    overlayLabel.setText("-");
    overlayLabel.setDisable(true);

    for (AltSound2SampleType duck : sampleTypes) {
      switch (duck) {
        case sfx: {
          sfxCheckbox.setSelected(true);
          sfxLabel.setDisable(false);
          sfxLabel.setText("0");
          sfxSlider.setDisable(false);
          sfxSlider.setValue(0);
          break;
        }
        case music: {
          musicCheckbox.setSelected(true);
          musicLabel.setDisable(false);
          musicLabel.setText("0");
          musicSlider.setDisable(false);
          musicSlider.setValue(0);
          break;
        }
        case callout: {
          calloutCheckbox.setSelected(true);
          calloutLabel.setDisable(false);
          calloutLabel.setText("0");
          calloutSlider.setDisable(false);
          calloutSlider.setValue(0);
          break;
        }
        case solo: {
          soloCheckbox.setSelected(true);
          soloLabel.setDisable(false);
          soloLabel.setText("0");
          soloSlider.setDisable(false);
          soloSlider.setValue(0);
          break;
        }
        case overlay: {
          overlayCheckbox.setSelected(true);
          overlayLabel.setDisable(false);
          overlayLabel.setText("0");
          overlaySlider.setDisable(false);
          overlaySlider.setValue(0);
          break;
        }
        default: {
          throw new UnsupportedOperationException("Invalid sample type");
        }
      }
    }
  }

  public AltSound2DuckingProfile editingFinished() {
    return editorProfile;
  }
}
