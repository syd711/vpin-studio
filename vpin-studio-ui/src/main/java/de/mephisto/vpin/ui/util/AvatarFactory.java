package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.UIDefaults;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;

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

  public static ImageView create(InputStream in) {
    Image image = new Image(in);
    ImageView imageView = new ImageView(image);
    imageView.setFitWidth(UIDefaults.DEFAULT_AVATARSIZE);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);

    Rectangle clip = new Rectangle();
    clip.setWidth(UIDefaults.DEFAULT_AVATARSIZE);
    clip.setHeight(UIDefaults.DEFAULT_AVATARSIZE);

    clip.setArcHeight(UIDefaults.DEFAULT_AVATARSIZE);
    clip.setArcWidth(UIDefaults.DEFAULT_AVATARSIZE);
    clip.setStroke(Color.WHITE);
    imageView.setClip(clip);
    return imageView;
  }
}
