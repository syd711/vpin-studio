package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.*;

public class UISettingsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(UISettingsPreferencesController.class);

  @FXML
  private BorderPane avatarBorderPane;

  @FXML
  private TextField vpinNameText;

  @FXML
  private CheckBox uiShowVersion;

  @FXML
  private CheckBox uiShowVPSUpdates;

  @FXML
  private CheckBox uiShowEmuColCheckbox;

  @FXML
  private CheckBox autoEditCheckbox;

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

  private Tile avatar;
  public static Debouncer debouncer = new Debouncer();
  private Cabinet cabinet;

  @FXML
  private void onFileSelect(ActionEvent e) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));

    File selection = fileChooser.showOpenDialog(stage);
    if (selection != null) {
      try {
        client.getPreferenceService().uploadVPinAvatar(selection);
        if (Features.TOURNAMENTS_ENABLED && maniaClient.getCabinetClient() != null) {
          maniaClient.getCabinetClient().updateAvatar(selection, null);
        }
        refreshAvatar();
      } catch (Exception ex) {
        WidgetFactory.showAlert(Studio.stage, "Uploading avatar image failed.", "Please check the log file for details.", "Error: " + ex.getMessage());
      }
    }
  }

  @FXML
  private void onHideReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Reset \"Do not show again\" flags?", "All previously hidden dialogs or panels will be shown again.");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

      uiSettings.setHideComponentWarning(false);
      uiSettings.setHideDismissConfirmations(false);
      uiSettings.setHideVPXStartInfo(false);

      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
      EventManager.getInstance().notifyPreferenceChanged(PreferenceType.uiSettings);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (Features.TOURNAMENTS_ENABLED) {
      cabinet = maniaClient.getCabinetClient().getCabinet();
      if (cabinet != null) {
        vpinNameText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("cabinetName", () -> {
          if (StringUtils.isEmpty(t1)) {
            cabinet.setDisplayName(UIDefaults.VPIN_NAME);
          }
          else {
            cabinet.setDisplayName(t1);
          }

          try {
            maniaClient.getCabinetClient().update(cabinet);
          } catch (Exception e) {
            LOG.error("Failed to update cabinet name for VPin Mania: " + e.getMessage(), e);
            WidgetFactory.showAlert(stage, "Error", "Failed to update cabinet name for VPin Mania: " + e.getMessage());
          }
        }, 500));
      }
    }

    PreferenceBindingUtil.bindTextField(vpinNameText, PreferenceNames.SYSTEM_NAME, UIDefaults.VPIN_NAME);
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    uiShowVersion.setSelected(!uiSettings.isHideVersions());
    uiShowVersion.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideVersions(!t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    boolean disabled = uiSettings.isHideVPSUpdates();
    vpsAltSound.setDisable(disabled);
    vpsAltSound.setSelected(uiSettings.isVpsAltSound());
    vpsAltColor.setDisable(disabled);
    vpsAltColor.setSelected(uiSettings.isVpsAltColor());
    vpsBackglass.setDisable(disabled);
    vpsBackglass.setSelected(uiSettings.isVpsBackglass());
    vpsPOV.setDisable(disabled);
    vpsPOV.setSelected(uiSettings.isVpsPOV());
    vpsPUPPack.setDisable(disabled);
    vpsPUPPack.setSelected(uiSettings.isVpsPUPPack());
    vpsRom.setDisable(disabled);
    vpsRom.setSelected(uiSettings.isVpsRom());
    vpsSound.setDisable(disabled);
    vpsSound.setSelected(uiSettings.isVpsSound());
    vpsToppper.setDisable(disabled);
    vpsToppper.setSelected(uiSettings.isVpsToppper());
    vpsTutorial.setDisable(disabled);
    vpsTutorial.setSelected(uiSettings.isVpsTutorial());
    vpsWheel.setDisable(disabled);
    vpsWheel.setSelected(uiSettings.isVpsWheel());

    vpsAltSound.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsAltSound(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsAltColor.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsAltColor(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsBackglass.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsBackglass(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsPOV.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsPOV(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsPUPPack.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsPUPPack(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsRom.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsRom(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsSound.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsSound(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsToppper.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsToppper(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsTutorial.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsTutorial(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    vpsWheel.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setVpsWheel(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    uiShowVPSUpdates.setSelected(!uiSettings.isHideVPSUpdates());
    uiShowVPSUpdates.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideVPSUpdates(!t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);

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

    uiShowEmuColCheckbox.setSelected(!uiSettings.isHideEmulatorColumn());
    uiShowEmuColCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideEmulatorColumn(!t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    autoEditCheckbox.setSelected(uiSettings.isAutoEditTableData());
    autoEditCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setAutoEditTableData(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    refreshAvatar();
  }

  private void refreshAvatar() {
    PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
    Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
    if (!StringUtils.isEmpty(avatarEntry.getValue())) {
      image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
    }

    if (avatar == null) {
      avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(300, 300)
        .backgroundColor(Color.TRANSPARENT)
        .image(image)
        .imageMask(Tile.ImageMask.ROUND)
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();
      avatarBorderPane.setCenter(avatar);
    }
    avatar.setImage(image);
  }
}
