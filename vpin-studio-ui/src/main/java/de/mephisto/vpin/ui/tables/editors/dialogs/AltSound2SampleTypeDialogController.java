package de.mephisto.vpin.ui.tables.editors.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSound2Group;
import de.mephisto.vpin.restclient.altsound.AltSound2SampleType;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AltSound2SampleTypeDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AltSound2SampleTypeDialogController.class);

  @FXML
  private ComboBox<String> sampleCombo;

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
  private AltSound altSound;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    sampleCombo.setItems(FXCollections.observableList(AltSound2SampleType.toStringValues()));
    sampleCombo.valueProperty().addListener((observableValue, s, t1) -> {
      AltSound2Group group = altSound.getGroup(AltSound2SampleType.valueOf(t1.toLowerCase()));
      setGroup(group);
    });

    volumeSlider.valueProperty().addListener((observableValue, number, t1) -> {
      group.setGroupVol(t1.intValue());
      volumeLabel.setText(String.valueOf(t1.intValue()));
    });
  }

  private void refresh() {
    volumeLabel.setText(String.valueOf(group.getGroupVol()));
    volumeSlider.setValue(group.getGroupVol());
    sampleCombo.setValue(group.getName().name().toUpperCase());

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
    pausesCallout.setDisable(group.getName().equals(AltSound2SampleType.music) );
    pausesMusic.setDisable(group.getName().equals(AltSound2SampleType.music) );
    pausesOverlay.setDisable(group.getName().equals(AltSound2SampleType.music));
    pausesSfx.setDisable(group.getName().equals(AltSound2SampleType.music) );
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
    if (profile == null) {
      profile = AltSound2SampleType.music;
    }

    AltSound2Group selectedGroup = altSound.getGroup(profile);
    this.setGroup(selectedGroup);
  }

  private void setGroup(AltSound2Group group) {
    this.group = group;
    refresh();
  }
}
