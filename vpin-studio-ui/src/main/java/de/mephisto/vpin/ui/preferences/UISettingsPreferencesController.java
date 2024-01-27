package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.BindingUtil;
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

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class UISettingsPreferencesController implements Initializable {

  @FXML
  private BorderPane avatarBorderPane;

  @FXML
  private TextField vpinNameText;

  @FXML
  private CheckBox uiShowVersion;

  @FXML
  private CheckBox uiHideVPSUpdates;

  @FXML
  private CheckBox uiHideEmuColCheckbox;

  private Tile avatar;

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
      EventManager.getInstance().notifyPreferenceChanged();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BindingUtil.bindTextField(vpinNameText, PreferenceNames.SYSTEM_NAME, UIDefaults.VPIN_NAME);

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    uiShowVersion.setSelected(uiSettings.isHideVersions());
    uiShowVersion.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideVersions(t1);
      PreferencesController.markDirty();
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    uiHideVPSUpdates.setSelected(uiSettings.isHideVPSUpdates());
    uiHideVPSUpdates.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideVPSUpdates(t1);
      PreferencesController.markDirty();
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    uiHideEmuColCheckbox.setSelected(uiSettings.isHideEmulatorColumn());
    uiHideEmuColCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideEmulatorColumn(t1);
      PreferencesController.markDirty();
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
