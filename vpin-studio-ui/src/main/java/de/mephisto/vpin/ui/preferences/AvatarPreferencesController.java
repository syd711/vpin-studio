package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class AvatarPreferencesController implements Initializable {


  @FXML
  private BorderPane avatarBorderPane;

  private Tile avatar;

  @FXML
  private void onFileSelect(ActionEvent e) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));

    File selection = fileChooser.showOpenDialog(stage);
    if (selection != null) {
      try {
        client.uploadVPinAvatar(selection);
        refreshAvatar();
      } catch (Exception ex) {
        WidgetFactory.showAlert(Studio.stage, "Uploading avatar image failed.", "Please check the log file for details.", "Error: " + ex.getMessage());
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    refreshAvatar();
  }

  private void refreshAvatar() {
    PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
    Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
    if (!StringUtils.isEmpty(avatarEntry.getValue())) {
      image = new Image(client.getAsset(avatarEntry.getValue()));
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
