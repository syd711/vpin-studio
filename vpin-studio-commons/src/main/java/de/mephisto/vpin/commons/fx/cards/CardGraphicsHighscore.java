package de.mephisto.vpin.commons.fx.cards;

import de.mephisto.vpin.commons.fx.cards.CardLayerText.CardLayerTextType;
import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardGraphicsHighscore extends StackPane {
  protected final static Logger LOG = LoggerFactory.getLogger(CardGraphicsHighscore.class);

  /** Whether autosize is active or not, depends on how it is embeded ? */
  private boolean resizable;

  private boolean maintainAspectRatio = false;

  private CardTemplate template;
  private CardData data;
  private CardResolution res;

  CardLayerBackground backgroundLayer = new CardLayerBackground();
  CardLayerCanvas canvasLayer = new CardLayerCanvas();
  CardLayerText titleLayer = new CardLayerText(CardLayerTextType.Title);
  CardLayerText tableNameLayer = new CardLayerText(CardLayerTextType.TableName);
  CardLayerWheel wheelLayer = new CardLayerWheel();
  CardLayerScores scoresLayer = new CardLayerScores();

  List<CardLayer> layers = Arrays.asList(backgroundLayer, canvasLayer, wheelLayer, titleLayer, 
      tableNameLayer, scoresLayer);

  /** Activate the layers to visualize the disposition */
  private boolean debug = false;
  private CardLayerDebug debugLayer;

  public CardGraphicsHighscore(boolean resizable) {
    this.resizable = resizable;
    for (CardLayer layer : layers) {
      getChildren().add((Node) layer);
    }
  }

  public void setTemplate(CardTemplate template) {
    this.template = template;
    this.requestLayout();
  }

  public boolean isMaintainAspectRatio() {
    return maintainAspectRatio;
  }

  public void setMaintainAspectRatio(boolean maintainAspectRatio) {
    this.maintainAspectRatio = maintainAspectRatio;
  }

  public CardResolution getCardResolution() {
    return res;
  }

  @Override
  public boolean isResizable() {
    return resizable;
  }

  public void setData(CardData data, CardResolution res) {
    this.data = data;
    this.res = res;
    this.requestLayout();
  }

  @Override protected void layoutChildren() {
    double width = getWidth();
    double height = getHeight();
    if (width == 0 || height == 0) {
      return;
    }

    if (template == null || data == null) {
      layers.forEach(l -> l.setVisible(false));
      return;
    }

    // calculate zoom factors and calculate WIDTH/HEIGHT in the dimensions of the template
    // As we manipulate both dimension in template coordinate system and image coordinate, 
    // a WIDTH uppercase refers to template coordinate and width lowercase, refer to the image
    // then width = WIDTH * zoomX and height = HEIGHT * zoomY

    double zoomX = res == null ? 1.0 : width / res.toWidth();
    double WIDTH = width / zoomX;
    double zoomY = res == null ? 1.0 : height / res.toHeight();
    double HEIGHT = height / zoomY;

    if (maintainAspectRatio) {
      zoomX = Math.min(zoomX, zoomY);
      zoomY = zoomX;
    }

    //-----------
    // From here, below system of coordinate is template dimensions

    if (template.isRenderBackground()) {
      backgroundLayer.setVisible(true);
      resizeRelocate(backgroundLayer, 0, 0, WIDTH, HEIGHT, zoomX, zoomY);
    }
    else {
      backgroundLayer.setVisible(false);
    }

    if (template.isRenderCanvas()) {
      canvasLayer.setVisible(true);
      resizeRelocate(canvasLayer, 
        WIDTH * template.getCanvasX(), HEIGHT * template.getCanvasY(), 
        WIDTH * template.getCanvasWidth(), HEIGHT * template.getCanvasHeight(), 
        zoomX, zoomY);
    }
    else {
      canvasLayer.setVisible(false);
    }

    if (template.isRenderWheelIcon()) {
      wheelLayer.setVisible(true);

      double wheelSize = WIDTH * template.getWheelSize();
      resizeRelocate(wheelLayer, 
        WIDTH * template.getWheelX(), HEIGHT * template.getWheelY(), 
        wheelSize, wheelSize, zoomX, zoomY);
    }
    else {
      wheelLayer.setVisible(false);
    }

    // textLayers occupy the full width of the card and text is centered in it
    if (template.isRenderTitle()) {
      titleLayer.setVisible(true);
      resizeRelocate(titleLayer, 
        WIDTH * template.getTitleX(), HEIGHT * template.getTitleY(), 
        WIDTH * template.getTitleWidth(), HEIGHT * template.getTitleHeight(), 
        zoomX, zoomY);
    }
    else {
      titleLayer.setVisible(false);
    }

    if (template.isRenderTableName()) {
      tableNameLayer.setVisible(true);
      resizeRelocate(tableNameLayer, 
        WIDTH * template.getTableX(), HEIGHT * template.getTableY(), 
        WIDTH * template.getTableWidth(), HEIGHT * template.getTableHeight(), 
        zoomX, zoomY);
    }
    else {
      tableNameLayer.setVisible(false);
    }

    if (template.isRenderScores()) {
      scoresLayer.setVisible(true);
      resizeRelocate(scoresLayer, 
        WIDTH * template.getScoresX(), HEIGHT * template.getScoresY(), 
        WIDTH * template.getScoresWidth(), HEIGHT * template.getScoresHeight(), 
        zoomX, zoomY);
    }
    else {
      scoresLayer.setVisible(false);
    }

    if (debug) {
      // add a temporary debug layer
      if (debugLayer == null) {
        debugLayer = new CardLayerDebug(layers);
        getChildren().add(debugLayer);
      }
      resizeRelocate(debugLayer, 0, 0, WIDTH, HEIGHT, zoomX, zoomY);
    } else {
      if (debugLayer != null) {
        getChildren().remove(debugLayer);
        debugLayer = null;
      }
    }
  }


  public void resizeRelocate(CardLayer layer, double x, double y, double width, double height, double zoomX, double zoomY) {
    // don't change the order 
    layer.setWidth(width * zoomX);
    layer.setHeight(height * zoomY);
    layer.relocate(x * zoomX, y * zoomY);

    if (template != null) {
      long startTime = System.currentTimeMillis();
      layer.draw(template, data, zoomX, zoomY);
      LOG.debug(this.getClass().getSimpleName() + " drawn in (ms) " + (System.currentTimeMillis() - startTime));
    }
  }
  
  //----------------------------------------------------------------

  /**
   * Must be called on javaFX thread !
   */
  public BufferedImage snapshot() {
    SnapshotParameters snapshotParameters = new SnapshotParameters();
    Rectangle2D rectangle2D = new Rectangle2D(0, 0, getWidth(), getHeight());
    snapshotParameters.setViewport(rectangle2D);
    snapshotParameters.setFill(Color.TRANSPARENT);
    WritableImage snapshot = this.snapshot(snapshotParameters, null);
    BufferedImage bufferedImage = new BufferedImage((int) rectangle2D.getWidth(), (int) rectangle2D.getHeight(), BufferedImage.TYPE_INT_ARGB);
    return SwingFXUtils.fromFXImage(snapshot, bufferedImage);
  }

  public List<CardLayer> getLayers() {
    return layers;
  }

  public CardLayer selectCardLayer(double x, double y) {
    // look from top to bottom
    for (int i = layers.size() - 1; i>=0; i--) {
      CardLayer layer = layers.get(i);
      if (layer.isSelectable() && layer.isVisible() && contains(layer, x, y)) {
        return layer;
      }
    }
    return null;
  }

  private boolean contains(CardLayer layer, double x, double y) {
    double lx = layer.getLocX();
    double ly = layer.getLocY();
    double lw = layer.getWidth();
    double lh = layer.getHeight();
    return (x >= lx && x < lx + lw && y >= ly && y < ly + lh);
  }

}