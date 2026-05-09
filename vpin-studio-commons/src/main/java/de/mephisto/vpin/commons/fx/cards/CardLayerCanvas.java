package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class CardLayerCanvas extends Rectangle implements CardLayer {

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) {

    int value = 255 - (255 * template.getCanvasAlphaPercentage() / 100);
    String hex = Integer.toHexString(value);
    String color = Objects.toString(template.getCanvasBackground(), "#FFFFFF");
    this.setFill(Paint.valueOf(color + hex));
    this.setArcWidth(template.getCanvasBorderRadius() * zoomX);
    this.setArcHeight(template.getCanvasBorderRadius() * zoomY);
  }
}
