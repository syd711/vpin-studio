package de.mephisto.vpin.commons.fx.cards;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CardLayerDebug extends Canvas implements CardLayer {

  private List<CardLayer> layers;

  public CardLayerDebug(List<CardLayer> layers) {
    this.layers = layers;
  }

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception {
    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect(0, 0, getWidth(), getHeight());

    for (CardLayer layer : layers) {
      if (layer.isVisible()) {
        gc.setFill(getDebugColor(layer));
        gc.fillRect(layer.getLocX(), layer.getLocY(), layer.getWidth(), layer.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(layer.getLocX(), layer.getLocY(), layer.getWidth(), layer.getHeight());
      }
    }
  }

  private Color getDebugColor(CardLayer layer) {
    switch (layer.getClass().getSimpleName()) {
      case "CardLayerBackground": return Color.color(1.0f, 1.0f, 1.0f, 0.2f);
      case "CardLayerCanvas": return Color.color(1.0f, 1.0f, 0.0f, 0.5f);
      case "CardLayerText": return Color.color(0.0f, 1.0f, 0.0f, 0.5f);
      case "CardLayerScores": return Color.color(0.0f, 1.0f, 1.0f, 0.5f);
      case "CardLayerWheel": return Color.color(1.0f, 0.0f, 1.0f, 0.3f);
      default: return Color.TRANSPARENT;
    }
  }
}
