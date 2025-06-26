package de.mephisto.vpin.commons.fx.cards;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class CardLayerCanvas extends CardLayer {

  @Override
  protected void draw(GraphicsContext g, CardTemplate template, CardData data) throws Exception {

    int value = 255 - (255 * template.getCanvasAlphaPercentage() / 100);
    String hex = Integer.toHexString(value);
    String color = "#FFFFFF";
    if (template.getCanvasBackground() != null) {
      color = template.getCanvasBackground();
    }

    Paint paint = Paint.valueOf(color + hex);
    g.setFill(paint);

    g.fillRoundRect(0, 0, getWidth(), getHeight(), template.getCanvasBorderRadius(), template.getCanvasBorderRadius());
    g.setFill(Paint.valueOf(template.getFontColor()));
  }
}
