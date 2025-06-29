package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class CardLayerCanvas extends Rectangle implements CardLayer {

  @Override
  public boolean isSelectable() {
    return true;
  }

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception {

    int value = 255 - (255 * template.getCanvasAlphaPercentage() / 100);
    String hex = Integer.toHexString(value);
    String color = StringUtils.defaultString(template.getCanvasBackground(), "#FFFFFF");
    this.setFill(Paint.valueOf(color + hex));
    this.setArcWidth(template.getCanvasBorderRadius() * zoomX);
    this.setArcHeight(template.getCanvasBorderRadius() * zoomY);
  }
}
