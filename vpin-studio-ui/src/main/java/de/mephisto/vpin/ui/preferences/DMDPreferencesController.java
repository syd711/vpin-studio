package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.dmd.DMDDeviceIniConfiguration;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DMDPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPreferencesController.class);

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private TextField networkUrlText;

  @FXML
  private CheckBox networkStreamCheckbox;

  @FXML
  private CheckBox virtualdmdEnabledCheckbox;
  @FXML
  private CheckBox virtualdmdStayOnTopCheckbox;
  @FXML
  private CheckBox virtualdmdIgnoreAspectRatioCheckbox;
  @FXML
  private CheckBox virtualdmdUseRegistryCheckbox;

  private DMDDeviceIniConfiguration dmdDeviceIni;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<GameEmulatorRepresentation> filtered = new ArrayList<>(client.getEmulatorService().getVpxGameEmulators());
    this.emulatorCombo.setItems(FXCollections.observableList(filtered));

    this.emulatorCombo.valueProperty().addListener(new ChangeListener<GameEmulatorRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
        dmdDeviceIni = null;
        if (newValue != null) {
          dmdDeviceIni = client.getDmdService().getDMDDeviceIniConfiguration(newValue.getId());
        }
        refresUI();
      }
    });

    networkStreamCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdDeviceIni.setNetworkStreamEnabled(t1);
      client.getDmdService().saveDmdDeviceIni(dmdDeviceIni);
    });


    virtualdmdEnabledCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdDeviceIni.setEnabled(t1);
      client.getDmdService().saveDmdDeviceIni(dmdDeviceIni);
    });

    virtualdmdStayOnTopCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdDeviceIni.setStayOnTop(t1);
      client.getDmdService().saveDmdDeviceIni(dmdDeviceIni);
    });

    virtualdmdIgnoreAspectRatioCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdDeviceIni.setIgnoreAspectRatio(t1);
      client.getDmdService().saveDmdDeviceIni(dmdDeviceIni);
    });

    virtualdmdUseRegistryCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdDeviceIni.setUseRegistry(t1);
      client.getDmdService().saveDmdDeviceIni(dmdDeviceIni);
    });

    networkUrlText.setDisable(true);
    networkStreamCheckbox.setDisable(true);
    virtualdmdEnabledCheckbox.setDisable(true);
    virtualdmdStayOnTopCheckbox.setDisable(true);
    virtualdmdIgnoreAspectRatioCheckbox.setDisable(true);
    virtualdmdUseRegistryCheckbox.setDisable(true);

    if (!filtered.isEmpty()) {
      emulatorCombo.getSelectionModel().select(0);
    }
  }

  private void refresUI() {
    networkStreamCheckbox.setDisable(dmdDeviceIni == null);
    virtualdmdEnabledCheckbox.setDisable(dmdDeviceIni == null);
    virtualdmdStayOnTopCheckbox.setDisable(dmdDeviceIni == null);
    virtualdmdIgnoreAspectRatioCheckbox.setDisable(dmdDeviceIni == null);
    virtualdmdUseRegistryCheckbox.setDisable(dmdDeviceIni == null);

    if (dmdDeviceIni != null) {
      networkStreamCheckbox.setSelected(dmdDeviceIni.isNetworkStreamEnabled());
      networkUrlText.setText(dmdDeviceIni.getWebSocketUrl());

      virtualdmdEnabledCheckbox.setSelected(dmdDeviceIni.isEnabled());
      virtualdmdStayOnTopCheckbox.setSelected(dmdDeviceIni.isStayOnTop());
      virtualdmdIgnoreAspectRatioCheckbox.setSelected(dmdDeviceIni.isIgnoreAspectRatio());
      virtualdmdUseRegistryCheckbox.setSelected(dmdDeviceIni.isUseRegistry());
    }
  }
}
