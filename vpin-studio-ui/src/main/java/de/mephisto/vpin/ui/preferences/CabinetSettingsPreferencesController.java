package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.*;

public class CabinetSettingsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(CabinetSettingsPreferencesController.class);

  @FXML
  private BorderPane avatarBorderPane;

  @FXML
  private TextField vpinNameText;
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
      ProgressDialog.createProgressDialog(new AvatarUploadProgressModel(selection));
      refreshAvatar();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (Features.MANIA_ENABLED) {
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
    refreshAvatar();
  }

  private void refreshAvatar() {
    PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
    Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
    if (!StringUtils.isEmpty(avatarEntry.getValue())) {
      image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
    }

    avatarBorderPane.setCenter(null);
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
    avatar.setImage(image);
  }
}
