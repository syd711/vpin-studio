package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.dmd.DMDDeviceIniConfiguration;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
  private Label networkStreamErrorLabel;

  @FXML
  private Button networkUrlUpdateBtn;

  @FXML
  private CheckBox virtualdmdEnabledCheckbox;
  @FXML
  private CheckBox virtualdmdStayOnTopCheckbox;
  @FXML
  private CheckBox virtualdmdIgnoreAspectRatioCheckbox;
  @FXML
  private CheckBox virtualdmdUseRegistryCheckbox;

  private DMDDeviceIniConfiguration dmdDeviceIni;

  private boolean inInitialize;


  @FXML
  private void onNetworkUrlUpdate(ActionEvent event) {
    // empty the url so that server put the correct one
    dmdDeviceIni.setWebSocketUrl(null);
    saveDmdDeviceIni();
  }  

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.inInitialize = true;

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
      if (!inInitialize) {
        saveDmdDeviceIni();
      }
    });


    virtualdmdEnabledCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdDeviceIni.setEnabled(t1);
      if (!inInitialize) {
        saveDmdDeviceIni();
      }
    });

    virtualdmdStayOnTopCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdDeviceIni.setStayOnTop(t1);
      if (!inInitialize) {
        saveDmdDeviceIni();
      }
    });

    virtualdmdIgnoreAspectRatioCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdDeviceIni.setIgnoreAspectRatio(t1);
      if (!inInitialize) {
        saveDmdDeviceIni();
      }
    });

    virtualdmdUseRegistryCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdDeviceIni.setUseRegistry(t1);
      if (!inInitialize) {
        saveDmdDeviceIni();
      }
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

    this.inInitialize = false;
  }

  private void saveDmdDeviceIni() {
    this.dmdDeviceIni = client.getDmdService().saveDmdDeviceIni(dmdDeviceIni);
    refresUI();
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

      boolean warningVisible = dmdDeviceIni.isNetworkStreamEnabled() && dmdDeviceIni.isWebSocketUrlInvalid();
      networkStreamErrorLabel.setVisible(warningVisible);
      networkUrlUpdateBtn.setVisible(warningVisible);

      virtualdmdEnabledCheckbox.setSelected(dmdDeviceIni.isEnabled());
      virtualdmdStayOnTopCheckbox.setSelected(dmdDeviceIni.isStayOnTop());
      virtualdmdIgnoreAspectRatioCheckbox.setSelected(dmdDeviceIni.isIgnoreAspectRatio());
      virtualdmdUseRegistryCheckbox.setSelected(dmdDeviceIni.isUseRegistry());
    }
  }
}
