package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.dialogs.ROMUploadController;
import de.mephisto.vpin.ui.util.UIDefaults;
import de.mephisto.vpin.ui.util.WidgetFactory;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.stage;

public class AvatarPreferencesController implements Initializable {

  @FXML
  private TextField vpinNameText;

  @FXML
  private BorderPane avatarBorderPane;

  @FXML
  private void onFileSelect(ActionEvent e) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image", "*.png", ".jpg", "*.jpeg"));

    File selection = fileChooser.showOpenDialog(stage);
    try {
      Studio.client.uploadAvatar(selection);
    } catch (Exception ex) {
      WidgetFactory.showAlert("Uploading avatar image failed, check log file for details:\n\n" + ex.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    vpinNameText.setText(UIDefaults.VPIN_NAME);

    Tile avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(300, 300)
        .backgroundColor(Color.TRANSPARENT)
        .image(new Image(DashboardController.class.getResourceAsStream("avatar-default.png")))
        .imageMask(Tile.ImageMask.ROUND)
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();
    avatarBorderPane.setCenter(avatar);
  }
}
