package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.UIDefaults;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class AvatarFactory {

  public static Control create(Image image) {
    Tile avatar = TileBuilder.create()
      .skinType(Tile.SkinType.IMAGE)
      .maxSize(UIDefaults.DEFAULT_AVATARSIZE, UIDefaults.DEFAULT_AVATARSIZE)
      .backgroundColor(Color.TRANSPARENT)
      .image(image)
      .imageMask(Tile.ImageMask.ROUND)
      .textSize(Tile.TextSize.BIGGER)
      .textAlignment(TextAlignment.CENTER)
      .build();
    return avatar;
  }
}
