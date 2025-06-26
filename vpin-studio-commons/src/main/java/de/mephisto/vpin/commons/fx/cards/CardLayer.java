package de.mephisto.vpin.commons.fx.cards;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public abstract class CardLayer extends Canvas {

  protected final static Logger LOG = LoggerFactory.getLogger(CardLayer.class);

  private CardTemplate template;
  private CardData data;

  boolean debug = false;

  boolean enableRedraw = true;

  public CardLayer() {
    // Redraw canvas when size changes.
    widthProperty().addListener(evt -> {
      if (enableRedraw) { 
        draw(); 
      }
    });
    heightProperty().addListener(evt -> {
      if (enableRedraw) { 
        draw(); 
      }
    });
  }

  @Override
  public void resize(double width, double height) {
    enableRedraw = false;
    setWidth(width);
    setHeight(height);
    enableRedraw = true;
    draw();
  }

  public void setTemplate(CardTemplate template) {
    this.template = template;
  }

  public void setData(CardData data) {
    this.data = data;
  }

  @Override
  public boolean isResizable() {
    return true;
  }

  @Override
  public double prefWidth(double height) {
    return getWidth();
  }

  @Override
  public double prefHeight(double width) {
    return getHeight();
  }

  private void draw() {
    double width = getWidth();
    double height = getHeight();

    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect(0, 0, width, height);
    if (debug) {
      gc.setFill(getDebugColor());
      gc.fillRect(0, 0, width, height);
      gc.setStroke(Color.BLACK);
      gc.setLineWidth(1);
      gc.strokeRect(0, 0, width, height);
    }
    try {
      draw(gc, template, data);
    }
    catch (Exception e) {
      LOG.error("Error drawing layer {}", this, e);
    }
  }

  protected abstract void draw(GraphicsContext g, CardTemplate template, CardData data) throws Exception;

  //------------------------------------------

  protected Font createFont(String family, String posture, int size) {
    FontWeight fontWeight = FontWeight.findByName(posture);
    FontPosture fontPosture = FontPosture.findByName(posture);
    if (posture != null && posture.contains(" ")) {
      String[] split = posture.split(" ");
      fontWeight = FontWeight.findByName(split[0]);
      fontPosture = FontPosture.findByName(split[1]);
    }
    return Font.font(family, fontWeight, fontPosture, size);
  }

  protected int getTextWidth(String text, Font font) {
    Text theText = new Text(text);
    theText.setFont(font);
    return (int) theText.getBoundsInLocal().getWidth();
  }

  private Color getDebugColor() {
    switch (getClass().getSimpleName()) {
      case "CardLayerBackground": return Color.AQUA;
      case "CardLayerCanvas": return Color.YELLOW;
      case "CardLayerText": return Color.LIME;
      case "CardLayerScores": return Color.CYAN;
      default: return Color.TRANSPARENT;
    }
  }

}
