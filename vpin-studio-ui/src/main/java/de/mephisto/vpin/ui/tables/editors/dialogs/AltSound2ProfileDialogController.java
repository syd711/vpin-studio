package de.mephisto.vpin.ui.tables.editors.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.altsound.AltSound2DuckingProfile;
import de.mephisto.vpin.restclient.altsound.AltSound2SampleType;
import de.mephisto.vpin.restclient.altsound.AltSoundDuckingProfileValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class AltSound2ProfileDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AltSound2ProfileDialogController.class);

  @FXML
  private Label sampleIdLabel;

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
  private AltSound2DuckingProfile editorProfile;

  @FXML
  private void onSaveClick(ActionEvent e) {
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

    sampleCombo.setItems(FXCollections.observableList(AltSound2SampleType.toStringValues()));
    sampleCombo.valueProperty().addListener((observableValue, s, t1) -> editorProfile.setType(AltSound2SampleType.valueOf(t1.toLowerCase())));

    musicCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      musicLabel.setDisable(!t1);
      musicSlider.setDisable(!t1);

      if (t1) {
        editorProfile.addProfileValue(AltSound2SampleType.music, 0);
      }
      else {
        musicSlider.valueProperty().set(0);
        musicLabel.setText("-");
        editorProfile.removeProfileValue(AltSound2SampleType.music);
      }
    });

    calloutCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      calloutLabel.setDisable(!t1);
      calloutSlider.setDisable(!t1);

      if (t1) {
        editorProfile.addProfileValue(AltSound2SampleType.callout, 0);
      }
      else {
        calloutSlider.valueProperty().set(0);
        calloutLabel.setText("-");
        editorProfile.removeProfileValue(AltSound2SampleType.callout);
      }
    });

    sfxCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      sfxLabel.setDisable(!t1);
      sfxSlider.setDisable(!t1);
      if (t1) {
        editorProfile.addProfileValue(AltSound2SampleType.sfx, 0);
      }
      else {
        sfxSlider.valueProperty().set(0);
        sfxLabel.setText("-");
        editorProfile.removeProfileValue(AltSound2SampleType.sfx);
      }
    });

    soloCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      soloLabel.setDisable(!t1);
      soloSlider.setDisable(!t1);
      if (t1) {
        editorProfile.addProfileValue(AltSound2SampleType.solo, 0);
      }
      else {
        soloSlider.valueProperty().set(0);
        soloLabel.setText("-");
        editorProfile.removeProfileValue(AltSound2SampleType.solo);
      }
    });

    overlayCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      overlayLabel.setDisable(!t1);
      overlaySlider.setDisable(!t1);
      if (t1) {
        editorProfile.addProfileValue(AltSound2SampleType.overlay, 0);
      }
      else {
        overlaySlider.valueProperty().set(0);
        overlayLabel.setText("-");
        editorProfile.removeProfileValue(AltSound2SampleType.overlay);
      }
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

  public void setProfile(AltSound2DuckingProfile profile) {
    if (profile != null) {
      this.editorProfile.setType(profile.getType());
      this.editorProfile.setId(profile.getId());
      this.editorProfile.setValues(profile.getValues());
      this.sampleCombo.setValue(profile.getType().name().toUpperCase());
      this.sampleIdLabel.setText(String.valueOf(profile.getId()));
      this.sampleCombo.setDisable(true);
    }
    else {
      this.sampleIdLabel.setText("-");
    }


    AltSoundDuckingProfileValue profileValue = profile.getProfileValue(AltSound2SampleType.music);
    if (profileValue != null) {
      musicSlider.setDisable(false);
      musicSlider.setValue(profileValue.getVolume());
      musicCheckbox.setSelected(true);
    }

    profileValue = profile.getProfileValue(AltSound2SampleType.callout);
    if (profileValue != null) {
      calloutSlider.setDisable(false);
      calloutSlider.setValue(profileValue.getVolume());
      calloutCheckbox.setSelected(true);
    }

    profileValue = profile.getProfileValue(AltSound2SampleType.solo);
    if (profileValue != null) {
      soloSlider.setDisable(false);
      soloSlider.setValue(profileValue.getVolume());
      soloCheckbox.setSelected(true);
    }

    profileValue = profile.getProfileValue(AltSound2SampleType.sfx);
    if (profileValue != null) {
      sfxSlider.setDisable(false);
      sfxSlider.setValue(profileValue.getVolume());
      sfxCheckbox.setSelected(true);
    }

    profileValue = profile.getProfileValue(AltSound2SampleType.overlay);
    if (profileValue != null) {
      overlaySlider.setDisable(false);
      overlaySlider.setValue(profileValue.getVolume());
      overlayCheckbox.setSelected(true);
    }
  }

  public AltSound2DuckingProfile editingFinished() {
    return editorProfile;
  }
}
