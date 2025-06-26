package de.mephisto.vpin.commons.fx.cards;

import de.mephisto.vpin.commons.fx.cards.CardLayerText.CardLayerTextType;
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

  public CardGraphicsHighscore() {
    getChildren().addAll(layers);
  }

  public void setTemplate(CardTemplate template) {
    this.template = template;
    layers.forEach(l -> l.setTemplate(template));
  }

  public void setData(CardData data) {
    //this.data = data;
    layers.forEach(l -> l.setData(data));
  }

  @Override protected void layoutChildren() {
    double width = getWidth();
    double height = getHeight();
    
    backgroundLayer.resizeRelocate(0, 0, width, height);

    if (template.isRenderCanvas()) {
      canvasLayer.setVisible(true);
      double xcenter = template.getCanvasX() == 0 ? (width / 2) - (template.getCanvasWidth() / 2) : template.getCanvasX();
      canvasLayer.resizeRelocate(xcenter, template.getCanvasY(), template.getCanvasWidth(), template.getCanvasHeight());
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
            width - template.getMarginLeft() - template.getMarginRight(), 
            titleFontSize + template.getPadding());
      currentY += titleFontSize + template.getPadding();
    }
    else {
      titleLayer.setVisible(false);
    }

    if (template.isRenderTableName()) {
      tableNameLayer.setVisible(true);
      int tableFontSize = template.getTableFontSize();
      tableNameLayer.resizeRelocate(template.getMarginLeft(), currentY, 
            width - template.getMarginLeft() - template.getMarginRight(), 
            tableFontSize + template.getPadding());
      currentY += tableFontSize + template.getPadding();
    }
    else {
      tableNameLayer.setVisible(false);
    }

    int scoreX = template.getMarginLeft();

    if (template.isRenderWheelIcon()) {
      wheelLayer.setVisible(true);
      int wheelY = currentY + template.getTableFontSize() / 2;
      wheelLayer.resizeRelocate(template.getMarginLeft(), wheelY, template.getWheelSize(), template.getWheelSize());
      scoreX += template.getWheelSize() + template.getWheelPadding();
    }
    else {
      wheelLayer.setVisible(false);
    }

    scoresLayer.resizeRelocate(scoreX, currentY, 
      width - scoreX - template.getMarginRight(), 
      height - currentY - template.getMarginBottom());
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
}

