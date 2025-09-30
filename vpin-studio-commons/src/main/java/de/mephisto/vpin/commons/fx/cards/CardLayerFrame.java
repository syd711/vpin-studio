package de.mephisto.vpin.commons.fx.cards;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class CardLayerFrame extends Canvas implements CardLayer {
  protected final static Logger LOG = LoggerFactory.getLogger(CardLayerDebug.class);

  private Image cacheFrame;

  @Override
  public boolean isSelectable() {
    return false;
  }

  /**
   * Indication on relative times
   CardLayerBackground/getBackgroundImage(): 132 ms  => moved to getCardData
   CardLayerBackground/blurImage(): 424 ms   => moved to FX
   CardLayerBackground/applyAlphaComposites(): 203 ms
   CardLayerBackground/toFXImage(): 52 ms
   CardLayerBackground/drawImage(): 0 ms
   CardLayerBackground/drawBorder(): 1 ms
   */
  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) {
    double width = getWidth();
    double height = getHeight();
    GraphicsContext g = getGraphicsContext2D();
    g.clearRect(0, 0, width, height);

    if (data != null && data.checkFrameUpdated()) {
      cacheFrame = null;
      if (data.getFrame() != null) {
        try {
          BufferedImage frameImage = ImageIO.read(new ByteArrayInputStream(data.getFrame()));
          cacheFrame = SwingFXUtils.toFXImage(frameImage, null);
        }
        catch (Exception e) {
          LOG.info("Cannot load frame image");
        }
      }
    }

    if (cacheFrame != null && template.isRenderFrame()) {
      g.drawImage(cacheFrame, 0, 0, width, height);
    }
  }
}
