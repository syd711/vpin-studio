package de.mephisto.vpin.commons.fx.cards;

import de.mephisto.vpin.commons.fx.cards.CardLayerText.CardLayerTextType;
import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.*;

public class CardGraphicsHighscore extends Pane {

  /** Whether autosize is active or not, depends on how it is embeded ? */
  private boolean resizable;

  private boolean maintainAspectRatio = true;

  private double zoomX = 1;
  private double zoomY = 1;

  private CardTemplate template;
  //private CardData data;

  CardLayerBackground backgroundLayer = new CardLayerBackground();
  CardLayerCanvas canvasLayer = new CardLayerCanvas();
  CardLayerText titleLayer = new CardLayerText(CardLayerTextType.Title);
  CardLayerText tableNameLayer = new CardLayerText(CardLayerTextType.TableName);
  CardLayerWheel wheelLayer = new CardLayerWheel();
  CardLayerScores scoresLayer = new CardLayerScores();

  List<CardLayer> layers = Arrays.asList(backgroundLayer, canvasLayer, titleLayer, 
      tableNameLayer, wheelLayer, scoresLayer);

  public CardGraphicsHighscore(boolean resizable) {
    this.resizable = resizable;
    // configure layers
    backgroundLayer.setSelectable(false);

    getChildren().addAll(layers);
  }

  public void setTemplate(CardTemplate template) {
    this.template = template;
    layers.forEach(l -> l.setTemplate(template));
    this.requestLayout();
  }

  public boolean isMaintainAspectRatio() {
    return maintainAspectRatio;
  }

  public void setMaintainAspectRatio(boolean maintainAspectRatio) {
    this.maintainAspectRatio = maintainAspectRatio;
  }

  public double getZoomX() {
    return zoomX;
  }

  public double getZoomY() {
    return zoomY;
  }

  @Override
  public boolean isResizable() {
    return resizable;
  }

  public void setData(CardData data) {
    //this.data = data;
    layers.forEach(l -> l.setData(data));
    this.requestLayout();
  }

  @Override protected void layoutChildren() {
    double width = getWidth();
    double height = getHeight();
    if (width == 0 || height == 0) {
      return;
    }
   
    if (template == null) {
      layers.forEach(l -> l.setVisible(false));
      return;
    }

    // calculate zoom factors and calculate WIDTH/HEIGHT in the dimensions of the template
    // As we manipulate both dimension in template coordinate system and image coordinate, 
    // a WIDTH uppercase refers to template coordinate and width lowercase, refer to the image
    // then width = WIDTH * zoomX and height = HEIGHT * zoomY

    this.zoomX = template.getRatioXFor(width);
    double WIDTH = width / zoomX;
    this.zoomY = template.getRatioYFor(height);
    double HEIGHT = height / zoomY;

    if (maintainAspectRatio) {
      zoomX = Math.min(zoomX, zoomY);
      zoomY = zoomX;
    }

    //-----------
    // From here, below system of coordinate is template dimensions
    
    backgroundLayer.setVisible(true);
    backgroundLayer.resizeRelocate(0, 0, WIDTH, HEIGHT, zoomX, zoomY);

    if (template.isRenderCanvas()) {
      canvasLayer.setVisible(true);
      canvasLayer.resizeRelocate(template.getCanvasX(), template.getCanvasY(), template.getCanvasWidth(), template.getCanvasHeight(), zoomX, zoomY);
    }
    else {
      canvasLayer.setVisible(false);
    }

    // textLayers occupy the full width of the card and text is centered in it
    int currentY = template.getMarginTop();
    if (template.isRenderTitle()) {
      titleLayer.setVisible(true);
      int titleFontSize = template.getTitleFontSize();
      titleLayer.resizeRelocate(template.getMarginLeft(), currentY, 
            WIDTH - template.getMarginLeft() - template.getMarginRight(), 
            titleFontSize + template.getPadding(), zoomX, zoomY);
      currentY += titleFontSize + template.getPadding();
    }
    else {
      titleLayer.setVisible(false);
    }

    if (template.isRenderTableName()) {
      tableNameLayer.setVisible(true);
      int tableFontSize = template.getTableFontSize();
      tableNameLayer.resizeRelocate(template.getMarginLeft(), currentY, 
            WIDTH - template.getMarginLeft() - template.getMarginRight(), 
            tableFontSize + template.getPadding(), zoomX, zoomY);
      currentY += tableFontSize + template.getPadding();
    }
    else {
      tableNameLayer.setVisible(false);
    }

    int scoreX = template.getMarginLeft();

    if (template.isRenderWheelIcon()) {
      wheelLayer.setVisible(true);
      int wheelY = currentY + template.getTableFontSize() / 2;
      wheelLayer.resizeRelocate(template.getMarginLeft(), wheelY, template.getWheelSize(), template.getWheelSize(), zoomX, zoomY);
      scoreX += template.getWheelSize() + template.getWheelPadding();
    }
    else {
      wheelLayer.setVisible(false);
    }

    scoresLayer.setVisible(true);
    scoresLayer.resizeRelocate(scoreX, currentY, 
      WIDTH - scoreX - template.getMarginRight(), 
      HEIGHT - currentY - template.getMarginBottom(), zoomX, zoomY);
  }

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

  public CardLayer selectCardLayer(double x, double y) {
    // look from top to bottom
    for (int i = layers.size() - 1; i>=0; i--) {
      CardLayer layer = layers.get(i);
      if (layer.isSelectable() && layer.contains(x, y)) {
        return layer;
      }
    }
    return null;
  }
}