package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

  /** FALSE FOR PROD !!!!!
   * render a semi-transparent colored background to help visualize the resized effect */
  private boolean debug = false;

  /** Whether this layer can be selected */
  private boolean selectable = true;

  public CardLayer() {
  }

  @Override
  public boolean isResizable() {
    return false;
  }

  public void setSelectable(boolean selectable) {
    this.selectable = selectable;
  }
  public boolean isSelectable() {
    return selectable;
  }

  @Override
  public boolean contains(double x, double y) {
    double lx = getLayoutX();
    double ly = getLayoutY();
    double lw = getWidth();
    double lh = getHeight();
    return (x >= lx && x < lx + lw && y >= ly && y < ly + lh);
  }

  public void setTemplate(CardTemplate template) {
    this.template = template;
  }

  public void setData(CardData data) {
    this.data = data;
  }

  public void resizeRelocate(double x, double y, double width, double height, double zoomX, double zoomY) {
    super.relocate(x * zoomX, y * zoomY);
    super.setWidth(width * zoomX);
    super.setHeight(height * zoomY);

    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect(0, 0, width * zoomX, height * zoomY);
    if (debug) {
      gc.setFill(getDebugColor());
      gc.fillRect(0, 0, width * zoomX, height * zoomY);
      gc.setStroke(Color.BLACK);
      gc.setLineWidth(1);
      gc.strokeRect(0, 0, width * zoomX, height * zoomY);
    }
    try {
      if (template != null) {
        long startTime = System.currentTimeMillis();
        draw(gc, template, data, zoomX, zoomY);
        LOG.debug(this.getClass().getSimpleName() + " drawn in (ms) " + (System.currentTimeMillis() - startTime));
      }
    }
    catch (Exception e) {
      LOG.error("Error drawing layer {}", this, e);
    }
  }

  protected abstract void draw(GraphicsContext g, @Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception;

  //------------------------------------------

  protected Font createFont(String family, String posture, double size) {
    FontWeight fontWeight = FontWeight.findByName(posture);
    FontPosture fontPosture = FontPosture.findByName(posture);
    if (posture != null && posture.contains(" ")) {
      String[] split = posture.split(" ");
      fontWeight = FontWeight.findByName(split[0]);
      fontPosture = FontPosture.findByName(split[1]);
    }
    return Font.font(family, fontWeight, fontPosture, (int) size);
  }

  protected int getTextWidth(String text, Font font) {
    Text theText = new Text(text);
    theText.setFont(font);
    return (int) theText.getBoundsInLocal().getWidth();
  }

  //------------------------------------------

  private Color getDebugColor() {
    switch (getClass().getSimpleName()) {
      case "CardLayerBackground": return Color.color(0.0f, 1.0f, 1.0f, 0.5f);
      case "CardLayerCanvas": return Color.color(1.0f, 1.0f, 0.0f, 0.5f);
      case "CardLayerText": return Color.color(0.0f, 1.0f, 0.0f, 0.5f);
      case "CardLayerScores": return Color.color(0.0f, 1.0f, 1.0f, 0.5f);
      case "CardLayerWheel": return Color.color(1.0f, 0.0f, 1.0f, 0.3f);
      default: return Color.TRANSPARENT;
    }
  }
}
