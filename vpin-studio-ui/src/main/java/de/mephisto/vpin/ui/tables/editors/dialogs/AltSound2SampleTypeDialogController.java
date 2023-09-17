package de.mephisto.vpin.ui.tables.editors.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSound2Group;
import de.mephisto.vpin.restclient.altsound.AltSound2SampleType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AltSound2SampleTypeDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AltSound2SampleTypeDialogController.class);

  @FXML
  private Label sampleTypeLabel;

  @FXML
  private Label volumeLabel;

  @FXML
  private Slider volumeSlider;

  @FXML
  private CheckBox ducksCallout;
  @FXML
  private CheckBox ducksMusic;
  @FXML
  private CheckBox ducksOverlay;
  @FXML
  private CheckBox ducksSfx;
  @FXML
  private CheckBox ducksSolo;

  @FXML
  private CheckBox pausesCallout;
  @FXML
  private CheckBox pausesMusic;
  @FXML
  private CheckBox pausesOverlay;
  @FXML
  private CheckBox pausesSfx;
  @FXML
  private CheckBox pausesSolo;

  @FXML
  private CheckBox stopsCallout;
  @FXML
  private CheckBox stopsMusic;
  @FXML
  private CheckBox stopsOverlay;
  @FXML
  private CheckBox stopsSfx;
  @FXML
  private CheckBox stopsSolo;

  private AltSound2Group group;
  private AltSound2Group editingGroup;
  private AltSound altSound;

  @FXML
  private void onSaveClick(ActionEvent e) {
    this.group.setProfiles(editingGroup.getProfiles());
    this.group.setGroupVol(editingGroup.getGroupVol());
    this.group.setName(editingGroup.getName());
    this.group.setStops(editingGroup.getStops());
    this.group.setPauses(editingGroup.getPauses());
    this.group.setDucks(editingGroup.getDucks());

    if (ducksMusic.isSelected()) {
      this.group.addDuck(AltSound2SampleType.music);
      altSound.addDuckingProfileValue(this.group.getName(), AltSound2SampleType.music);
    }
    else {
      this.group.removeDuck(AltSound2SampleType.music);
      altSound.removeDuckingProfileValue(this.group.getName(), AltSound2SampleType.music);
    }

    if (ducksOverlay.isSelected()) {
      this.group.addDuck(AltSound2SampleType.overlay);
      altSound.addDuckingProfileValue(this.group.getName(), AltSound2SampleType.overlay);
    }
    else {
      this.group.removeDuck(AltSound2SampleType.overlay);
      altSound.removeDuckingProfileValue(this.group.getName(), AltSound2SampleType.overlay);
    }

    if (ducksSolo.isSelected()) {
      this.group.addDuck(AltSound2SampleType.solo);
      altSound.addDuckingProfileValue(this.group.getName(), AltSound2SampleType.solo);
    }
    else {
      this.group.removeDuck(AltSound2SampleType.solo);
      altSound.removeDuckingProfileValue(this.group.getName(), AltSound2SampleType.solo);
    }

    if (ducksSfx.isSelected()) {
      this.group.addDuck(AltSound2SampleType.sfx);
      altSound.addDuckingProfileValue(this.group.getName(), AltSound2SampleType.sfx);
    }
    else {
      this.group.removeDuck(AltSound2SampleType.sfx);
      altSound.removeDuckingProfileValue(this.group.getName(), AltSound2SampleType.sfx);
    }

    if (ducksCallout.isSelected()) {
      this.group.addDuck(AltSound2SampleType.callout);
      altSound.addDuckingProfileValue(this.group.getName(), AltSound2SampleType.callout);
    }
    else {
      this.group.removeDuck(AltSound2SampleType.callout);
      altSound.removeDuckingProfileValue(this.group.getName(), AltSound2SampleType.callout);
    }

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }


  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    volumeSlider.valueProperty().addListener((observableValue, number, t1) -> {
      group.setGroupVol(t1.intValue());
      volumeLabel.setText(String.valueOf(t1.intValue()));
    });
  }

  private void refresh() {
    volumeLabel.setText(String.valueOf(group.getGroupVol()));
    volumeSlider.setValue(group.getGroupVol());
    sampleTypeLabel.setText(group.getName().name().toUpperCase());

    ducksCallout.setSelected(false);
    ducksMusic.setSelected(false);
    ducksOverlay.setSelected(false);
    ducksSfx.setSelected(false);
    ducksSolo.setSelected(false);
    ducksCallout.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.solo));
    ducksMusic.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.solo));
    ducksOverlay.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.solo));
    ducksSfx.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.solo));
    ducksSolo.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.solo));
    List<AltSound2SampleType> ducks = group.getDucks();
    ducksCallout.setSelected(ducks.contains(AltSound2SampleType.callout));
    ducksMusic.setSelected(ducks.contains(AltSound2SampleType.music));
    ducksOverlay.setSelected(ducks.contains(AltSound2SampleType.overlay));
    ducksSfx.setSelected(ducks.contains(AltSound2SampleType.sfx));
    ducksSolo.setSelected(ducks.contains(AltSound2SampleType.solo));

    pausesCallout.setSelected(false);
    pausesMusic.setSelected(false);
    pausesOverlay.setSelected(false);
    pausesSfx.setSelected(false);
    pausesSolo.setSelected(false);
    List<AltSound2SampleType> pauses = group.getPauses();
    pausesCallout.setSelected(pauses.contains(AltSound2SampleType.callout));
    pausesMusic.setSelected(pauses.contains(AltSound2SampleType.music));
    pausesOverlay.setSelected(pauses.contains(AltSound2SampleType.overlay));
    pausesSfx.setSelected(pauses.contains(AltSound2SampleType.sfx));
    pausesSolo.setSelected(pauses.contains(AltSound2SampleType.solo));
    pausesCallout.setDisable(group.getName().equals(AltSound2SampleType.music));
    pausesMusic.setDisable(group.getName().equals(AltSound2SampleType.music));
    pausesOverlay.setDisable(group.getName().equals(AltSound2SampleType.music));
    pausesSfx.setDisable(group.getName().equals(AltSound2SampleType.music));
    pausesSolo.setDisable(group.getName().equals(AltSound2SampleType.music));

    stopsCallout.setSelected(false);
    stopsMusic.setSelected(false);
    stopsOverlay.setSelected(false);
    stopsSfx.setSelected(false);
    stopsSolo.setSelected(false);
    List<AltSound2SampleType> stops = group.getStops();
    stopsCallout.setSelected(stops.contains(AltSound2SampleType.callout));
    stopsMusic.setSelected(stops.contains(AltSound2SampleType.music));
    stopsOverlay.setSelected(stops.contains(AltSound2SampleType.overlay));
    stopsSfx.setSelected(stops.contains(AltSound2SampleType.sfx));
    stopsSolo.setSelected(stops.contains(AltSound2SampleType.solo));
    stopsCallout.setDisable(group.getName().equals(AltSound2SampleType.music));
    stopsMusic.setDisable(group.getName().equals(AltSound2SampleType.music));
    stopsOverlay.setDisable(group.getName().equals(AltSound2SampleType.music));
    stopsSfx.setDisable(group.getName().equals(AltSound2SampleType.music));
    stopsSolo.setDisable(group.getName().equals(AltSound2SampleType.music));

    AltSound2SampleType groupSampleType = group.getName();
    switch (groupSampleType) {
      case sfx: {
        ducksSfx.setDisable(true);
        ducksSfx.setSelected(false);
        break;
      }
      case callout: {
        ducksCallout.setDisable(true);
        ducksCallout.setSelected(false);
        break;
      }
      case overlay: {
        ducksOverlay.setDisable(true);
        ducksOverlay.setSelected(false);
        break;
      }
    }
  }

  @Override
  public void onDialogCancel() {
  }

  public void setProfile(AltSound altSound, AltSound2SampleType profile) {
    this.altSound = altSound;
    this.group = altSound.getGroup(profile);
    this.editingGroup = new AltSound2Group();
    this.editingGroup.setProfiles(group.getProfiles());
    this.editingGroup.setGroupVol(group.getGroupVol());
    this.editingGroup.setName(group.getName());
    this.editingGroup.setStops(group.getStops());
    this.editingGroup.setPauses(group.getPauses());
    this.editingGroup.setDucks(group.getDucks());
    refresh();
  }
}
