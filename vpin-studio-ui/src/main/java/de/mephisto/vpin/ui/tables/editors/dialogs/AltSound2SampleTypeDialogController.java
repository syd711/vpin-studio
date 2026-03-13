package de.mephisto.vpin.ui.tables.editors.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.altsound.*;
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

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AltSound2SampleTypeDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    savePauses();
    saveStops();
    saveDuckingProfiles();

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  private void saveStops() {
    if (stopsMusic.isSelected()) {
      if (!this.group.getStops().contains(AltSound2SampleType.music)) {
        this.group.getStops().add(AltSound2SampleType.music);
      }
    }
    else {
      this.group.getStops().remove(AltSound2SampleType.music);
    }

    if (stopsCallout.isSelected()) {
      if (!this.group.getStops().contains(AltSound2SampleType.callout)) {
        this.group.getStops().add(AltSound2SampleType.callout);
      }
    }
    else {
      this.group.getStops().remove(AltSound2SampleType.callout);
    }

    if (stopsOverlay.isSelected()) {
      if (!this.group.getStops().contains(AltSound2SampleType.overlay)) {
        this.group.getStops().add(AltSound2SampleType.overlay);
      }
    }
    else {
      this.group.getStops().remove(AltSound2SampleType.overlay);
    }

    if (stopsSfx.isSelected()) {
      if (!this.group.getStops().contains(AltSound2SampleType.sfx)) {
        this.group.getStops().add(AltSound2SampleType.sfx);
      }
    }
    else {
      this.group.getStops().remove(AltSound2SampleType.sfx);
    }

    if (stopsSolo.isSelected()) {
      if (!this.group.getStops().contains(AltSound2SampleType.solo)) {
        this.group.getStops().add(AltSound2SampleType.solo);
      }
    }
    else {
      this.group.getStops().remove(AltSound2SampleType.solo);
    }
  }

  private void savePauses() {
    if (pausesMusic.isSelected()) {
      if (!this.group.getPauses().contains(AltSound2SampleType.music)) {
        this.group.getPauses().add(AltSound2SampleType.music);
      }
    }
    else {
      this.group.getPauses().remove(AltSound2SampleType.music);
    }

    if (pausesCallout.isSelected()) {
      if (!this.group.getPauses().contains(AltSound2SampleType.callout)) {
        this.group.getPauses().add(AltSound2SampleType.callout);
      }
    }
    else {
      this.group.getPauses().remove(AltSound2SampleType.callout);
    }

    if (pausesOverlay.isSelected()) {
      if (!this.group.getPauses().contains(AltSound2SampleType.overlay)) {
        this.group.getPauses().add(AltSound2SampleType.overlay);
      }
    }
    else {
      this.group.getPauses().remove(AltSound2SampleType.overlay);
    }

    if (pausesSfx.isSelected()) {
      if (!this.group.getPauses().contains(AltSound2SampleType.sfx)) {
        this.group.getPauses().add(AltSound2SampleType.sfx);
      }
    }
    else {
      this.group.getPauses().remove(AltSound2SampleType.sfx);
    }

    if (pausesSolo.isSelected()) {
      if (!this.group.getPauses().contains(AltSound2SampleType.solo)) {
        this.group.getPauses().add(AltSound2SampleType.solo);
      }
    }
    else {
      this.group.getPauses().remove(AltSound2SampleType.solo);
    }
  }

  private void saveDuckingProfiles() {
    if (ducksMusic.isSelected()) {
      this.group.addDuck(AltSound2SampleType.music);
      altSound.addDuckingProfileValue(this.group.getName(), AltSound2SampleType.music);
    }
    else {
      this.group.removeDuck(AltSound2SampleType.music);
      altSound.removeDuckingProfileValue(this.group.getName(), AltSound2SampleType.music);
    }

    if (ducksOverlay.isSelected()) {
      if (altSound.getOverlayDuckingProfiles().isEmpty()) {
        AltSound2DuckingProfile profile = new AltSound2DuckingProfile();
        profile.setId(1);
        profile.setType(AltSound2SampleType.overlay);
        profile.setValues(altSound.getOverlay().getDucks().stream().map(duck -> {
          AltSoundDuckingProfileValue value = new AltSoundDuckingProfileValue();
          value.setVolume(60);
          value.setSampleType(duck);
          return value;
        }).collect(Collectors.toList()));
        altSound.getOverlayDuckingProfiles().add(profile);
      }
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
      if (altSound.getSfxDuckingProfiles().isEmpty()) {
        AltSound2DuckingProfile profile = new AltSound2DuckingProfile();
        profile.setId(1);
        profile.setType(AltSound2SampleType.sfx);
        profile.setValues(altSound.getSfx().getDucks().stream().map(duck -> {
          AltSoundDuckingProfileValue value = new AltSoundDuckingProfileValue();
          value.setVolume(60);
          value.setSampleType(duck);
          return value;
        }).collect(Collectors.toList()));
        altSound.getSfxDuckingProfiles().add(profile);
      }
      this.group.addDuck(AltSound2SampleType.sfx);
      altSound.addDuckingProfileValue(this.group.getName(), AltSound2SampleType.sfx);
    }
    else {
      this.group.removeDuck(AltSound2SampleType.sfx);
      altSound.removeDuckingProfileValue(this.group.getName(), AltSound2SampleType.sfx);
    }

    if (ducksCallout.isSelected()) {
      if (altSound.getCalloutDuckingProfiles().isEmpty()) {
        AltSound2DuckingProfile profile = new AltSound2DuckingProfile();
        profile.setId(1);
        profile.setType(AltSound2SampleType.callout);
        profile.setValues(altSound.getCallout().getDucks().stream().map(duck -> {
          AltSoundDuckingProfileValue value = new AltSoundDuckingProfileValue();
          value.setVolume(60);
          value.setSampleType(duck);
          return value;
        }).collect(Collectors.toList()));
        altSound.getCalloutDuckingProfiles().add(profile);
      }
      this.group.addDuck(AltSound2SampleType.callout);
      altSound.addDuckingProfileValue(this.group.getName(), AltSound2SampleType.callout);
    }
    else {
      this.group.removeDuck(AltSound2SampleType.callout);
      altSound.removeDuckingProfileValue(this.group.getName(), AltSound2SampleType.callout);
    }
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
      editingGroup.setGroupVol(t1.intValue());
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
    ducksSolo.setDisable(true);
    List<AltSound2SampleType> ducks = group.getDucks();
    ducksCallout.setSelected(ducks.contains(AltSound2SampleType.callout));
    ducksMusic.setSelected(ducks.contains(AltSound2SampleType.music));
    ducksOverlay.setSelected(ducks.contains(AltSound2SampleType.overlay));
    ducksSfx.setSelected(ducks.contains(AltSound2SampleType.sfx));
    ducksSolo.setSelected(false);

    pausesCallout.setSelected(false);
    pausesMusic.setSelected(false);
    pausesOverlay.setSelected(false);
    pausesSfx.setSelected(false);
    pausesSolo.setSelected(false);
    List<AltSound2SampleType> pauses = group.getPauses();
    pausesCallout.setSelected(pauses.contains(AltSound2SampleType.callout));
    pausesMusic.setSelected(pauses.contains(AltSound2SampleType.music));
    pausesOverlay.setSelected(pauses.contains(AltSound2SampleType.overlay));
    pausesSfx.setSelected(false);
    pausesSolo.setSelected(false);
    pausesCallout.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.callout) || group.getName().equals(AltSound2SampleType.sfx) || group.getName().equals(AltSound2SampleType.overlay) || group.getName().equals(AltSound2SampleType.solo));
    pausesMusic.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.sfx) || group.getName().equals(AltSound2SampleType.overlay) || group.getName().equals(AltSound2SampleType.solo));
    pausesOverlay.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.overlay) || group.getName().equals(AltSound2SampleType.sfx) || group.getName().equals(AltSound2SampleType.solo));
    pausesSfx.setDisable(true);
    pausesSolo.setDisable(true);

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
    stopsCallout.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.callout) || group.getName().equals(AltSound2SampleType.sfx) || group.getName().equals(AltSound2SampleType.overlay));
    stopsMusic.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.sfx) || group.getName().equals(AltSound2SampleType.overlay));
    stopsOverlay.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.overlay) || group.getName().equals(AltSound2SampleType.sfx));
    stopsSfx.setDisable(group.getName().equals(AltSound2SampleType.music) || group.getName().equals(AltSound2SampleType.callout) || group.getName().equals(AltSound2SampleType.overlay));
    stopsSolo.setDisable(!group.getName().equals(AltSound2SampleType.solo));

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
