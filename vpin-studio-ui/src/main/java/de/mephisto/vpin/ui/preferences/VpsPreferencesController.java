package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vps.VpsSettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.vps.VpsDBDownloadProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class VpsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VpsPreferencesController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private CheckBox vpsAltSound;
  @FXML
  private CheckBox vpsAltColor;
  @FXML
  private CheckBox vpsBackglass;
  @FXML
  private CheckBox vpsPOV;
  @FXML
  private CheckBox vpsPUPPack;
  @FXML
  private CheckBox vpsRom;
  @FXML
  private CheckBox vpsSound;
  @FXML
  private CheckBox vpsToppper;
  @FXML
  private CheckBox vpsTutorial;
  @FXML
  private CheckBox vpsWheel;

  @FXML
  private CheckBox uiShowVPSUpdates;

  @FXML
  private TextField authorDenyList;

  @FXML
  public void onReload() {
    ProgressDialog.createProgressDialog(new VpsDBDownloadProgressModel("Download VPS Database", Arrays.asList(new File("<vpsdb.json>"))));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    vpsPUPPack.managedProperty().bindBidirectional(vpsPUPPack.visibleProperty());
    vpsPUPPack.setVisible(Features.PUPPACKS_ENABLED);

    VpsSettings vpsSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPS_SETTINGS, VpsSettings.class);

    boolean disabled = vpsSettings.isHideVPSUpdates();
    vpsAltSound.setDisable(disabled);
    vpsAltSound.setSelected(vpsSettings.isVpsAltSound());
    vpsAltColor.setDisable(disabled);
    vpsAltColor.setSelected(vpsSettings.isVpsAltColor());
    vpsBackglass.setDisable(disabled);
    vpsBackglass.setSelected(vpsSettings.isVpsBackglass());
    vpsPOV.setDisable(disabled);
    vpsPOV.setSelected(vpsSettings.isVpsPOV());
    vpsPUPPack.setDisable(disabled);
    vpsPUPPack.setSelected(vpsSettings.isVpsPUPPack());
    vpsRom.setDisable(disabled);
    vpsRom.setSelected(vpsSettings.isVpsRom());
    vpsSound.setDisable(disabled);
    vpsSound.setSelected(vpsSettings.isVpsSound());
    vpsToppper.setDisable(disabled);
    vpsToppper.setSelected(vpsSettings.isVpsToppper());
    vpsTutorial.setDisable(disabled);
    vpsTutorial.setSelected(vpsSettings.isVpsTutorial());
    vpsWheel.setDisable(disabled);
    vpsWheel.setSelected(vpsSettings.isVpsWheel());

    vpsAltSound.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsAltSound(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });
    vpsAltColor.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsAltColor(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });
    vpsBackglass.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsBackglass(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });
    vpsPOV.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsPOV(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });
    vpsPUPPack.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsPUPPack(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });
    vpsRom.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsRom(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });
    vpsSound.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsSound(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });
    vpsToppper.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsToppper(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });
    vpsTutorial.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsTutorial(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });
    vpsWheel.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setVpsWheel(t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);
    });

    uiShowVPSUpdates.setSelected(!vpsSettings.isHideVPSUpdates());
    uiShowVPSUpdates.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      vpsSettings.setHideVPSUpdates(!t1);
      PreferencesController.markDirty(PreferenceType.vpsSettings);
      client.getPreferenceService().setJsonPreference(vpsSettings);

      boolean disabledSelection = !t1;
      vpsAltSound.setDisable(disabledSelection);
      vpsAltColor.setDisable(disabledSelection);
      vpsBackglass.setDisable(disabledSelection);
      vpsPOV.setDisable(disabledSelection);
      vpsPUPPack.setDisable(disabledSelection);
      vpsRom.setDisable(disabledSelection);
      vpsSound.setDisable(disabledSelection);
      vpsToppper.setDisable(disabledSelection);
      vpsTutorial.setDisable(disabledSelection);
      vpsWheel.setDisable(disabledSelection);
    });

    authorDenyList.setText(vpsSettings.getAuthorDenyList());
    authorDenyList.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("authorDenyList", () -> {
        try {
          vpsSettings.setAuthorDenyList(t1);
          client.getPreferenceService().setJsonPreference(vpsSettings);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 300);
    });

  }
}
