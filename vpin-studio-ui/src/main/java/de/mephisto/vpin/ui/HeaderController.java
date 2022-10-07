package de.mephisto.vpin.ui;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.ResourceBundle;

public class HeaderController implements Initializable {

  private static boolean toggleMaximize = true;

  @FXML
  private BorderPane avatarPane;

  @FXML
  private void onCloseClick() {
    System.exit(0);
  }

  @FXML
  private void onMaximize() {
    VPinStudioApplication.stage.setMaximized(toggleMaximize);
    toggleMaximize = !toggleMaximize;
  }

  @FXML
  private void onHideClick() {
    VPinStudioApplication.stage.setIconified(true);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    Tile avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(300, 300)
        .backgroundColor(Color.TRANSPARENT)
        .image(new Image(MainController.class.getResourceAsStream("dashboard.png")))
        .imageMask(Tile.ImageMask.ROUND)
        .text("Whatever text")
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();
    avatarPane.setCenter(avatar);
  }
}
