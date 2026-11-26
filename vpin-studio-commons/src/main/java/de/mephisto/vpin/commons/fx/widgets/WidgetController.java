package de.mephisto.vpin.commons.fx.widgets;

import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public class WidgetController {

  /**
   * Corner radius of the score items, the same as for tiles and panels (see UI-STYLEGUIDE.md).
   */
  public static final double TILE_RADIUS = 6;

  public WidgetController() {
  }

  /**
   * Clips the region to a rounded rectangle that follows its size. Needed for regions whose background image
   * is set in code, because a BackgroundImage ignores the -fx-background-radius.
   */
  public static void roundCorners(Region region, double radius) {
    Rectangle clip = new Rectangle();
    clip.setArcWidth(radius * 2);
    clip.setArcHeight(radius * 2);
    clip.widthProperty().bind(region.widthProperty());
    clip.heightProperty().bind(region.heightProperty());
    region.setClip(clip);
  }
}
