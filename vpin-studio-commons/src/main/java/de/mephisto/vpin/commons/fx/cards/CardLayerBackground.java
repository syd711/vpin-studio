package de.mephisto.vpin.commons.fx.cards;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;

public class CardLayerBackground extends Canvas implements CardLayer {
  protected final static Logger LOG = LoggerFactory.getLogger(CardLayerDebug.class);

  private Image cacheBackground;
  private BufferedImage cacheBackgroundImage;
  private BufferedImage cacheFinalImage;

  @Override
  public boolean isSelectable() {
    return false;
  }

  /**
   * Indication on relative times
   CardLayerBackground/getBackgroundImage(): 132 ms
   CardLayerBackground/blurImage(): 424 ms
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

    LogTime lt = new LogTime("   CardLayerBackground");

    boolean imageDirty = false;
    if (hasBackroundChanged(template, data)) {
      this.cacheBackgroundImage = getBackgroundImage(template, data);
      lt.pulse("getBackgroundImage()");
      imageDirty = true;
    }
    if (!template.isTransparentBackground()) {
     // if the backgroundImage has Changed, in all cases effect must be re-applied
      if (imageDirty || hasEffectsChanged(template)) {
        cacheFinalImage = ImageUtil.clone(cacheBackgroundImage);
        if (template.getBlur() > 0) {
          //cacheFinalImage = ImageUtil.blurImage(cacheFinalImage, template.getBlur());
          cacheFinalImage = ImageUtil.fastBlur(cacheFinalImage, template.getBlur() / 2);
          lt.pulse("blurImage()");
        }

        if (template.isGrayScale()) {
          cacheFinalImage = ImageUtil.grayScaleImage(cacheFinalImage);
          lt.pulse("grayScaleImage()");
        }

        float alphaWhite = template.getAlphaWhite();
        float alphaBlack = template.getAlphaBlack();
        ImageUtil.applyAlphaComposites(cacheFinalImage, alphaWhite, alphaBlack);
        lt.pulse("applyAlphaComposites()");
  
        imageDirty = true;
      }
    } else {
      cacheFinalImage = cacheBackgroundImage;
    }

    if (imageDirty) {
      cacheBackground = SwingFXUtils.toFXImage(cacheFinalImage, null);
      lt.pulse("toFXImage()");
    }

    //--------------------------
    // Draw Part

    if (cacheBackground != null) {
      g.drawImage(cacheBackground, 0, 0, width, height);
      lt.pulse("drawImage()");
    }

    if (template.getBorderWidth() > 0) {
      g.setStroke(Paint.valueOf(template.getFontColor()));
      double strokeWidthX = template.getBorderWidth() * zoomX / 2;
      double strokeWidthY = template.getBorderWidth() * zoomY / 2;
      g.setLineWidth(strokeWidthX * 2);
      // left
      g.strokeLine(strokeWidthX, strokeWidthX, strokeWidthX, height - strokeWidthX);
      // Right
      g.strokeLine(width - strokeWidthX, strokeWidthX, width - strokeWidthX, height - strokeWidthX);

      g.setLineWidth(strokeWidthY * 2);
      // Top 
      g.strokeLine(strokeWidthY, strokeWidthY, width - strokeWidthY, strokeWidthY);
      // Bottom
      g.strokeLine(strokeWidthY, height - strokeWidthY, width - strokeWidthY, height - strokeWidthY);

      lt.pulse("drawBorder()");
    }
  }

  private @Nullable BufferedImage getBackgroundImage(@Nonnull CardTemplate template, @Nullable CardData data) {

    if (template.isTransparentBackground()) {
      BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = (Graphics2D) bufferedImage.getGraphics();
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));

      int value = 255 - (255 * template.getTransparentPercentage() / 100);
      g2.setBackground(new java.awt.Color(0, 0, 0, value));
      g2.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
      g2.dispose();
      return bufferedImage;
    }

    BufferedImage backgroundImage = null;
    if (template.isUseDefaultBackground() && data != null && data.getBackgroundUrl() != null) {
      try {
        URL url = new URL(data.getBackgroundUrl());
        backgroundImage = ImageIO.read(url);
      }
      catch (Exception e) {
        LOG.info("Using default image as fallback instead of " + data.getBackgroundUrl());
      }
    }
    // fall back or !isUseDirectB2S()
    if (backgroundImage ==  null) {
      File backgroundsFolder = new File(SystemInfo.RESOURCES + "backgrounds");
      File sourceImage = new File(backgroundsFolder, template.getBackground() + ".jpg");
      if (!sourceImage.exists()) {
        sourceImage = new File(backgroundsFolder, template.getBackground() + ".png");
      }
      if (!sourceImage.exists()) {
        File[] backgrounds = backgroundsFolder.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
        if (backgrounds != null && backgrounds.length > 0) {
          sourceImage = backgrounds[0];
        }
      }
      if (!sourceImage.exists()) {
        throw new UnsupportedOperationException("No background images have been found, " +
            "make sure that folder " + backgroundsFolder.getAbsolutePath() + " contains valid images.");
      }
      try {
        backgroundImage = ImageUtil.loadImage(sourceImage);
      } catch (IOException e) {
        LOG.error("Cannot load image from source %s", sourceImage.getAbsolutePath(), e);
      }
    }

    if (backgroundImage != null) {
      backgroundImage = ImageUtil.crop(backgroundImage, 16, 9);
    }

    return backgroundImage;
  }

  //------------------------------------ Detetection of layer changes

  private String cacheBackgroundUrl = null;
  private int cacheHashTemplate = 0;

  private boolean hasBackroundChanged(CardTemplate template, @Nullable CardData data) {
    boolean hasChanged = false;
    // check on CardData
    if (template.isUseDefaultBackground() && data != null) {
      if (cacheBackgroundUrl == null || !cacheBackgroundUrl.equals(data.getBackgroundUrl())) {
        cacheBackgroundUrl = data.getBackgroundUrl();
        hasChanged = true;
      }
    }
    else {
      cacheBackgroundUrl = null;
    }

    // Check on Template
    int hashTemplate = Objects.hash(
      template.isTransparentBackground(),
      template.getTransparentPercentage(),
      template.getBackground()
    );
    if (cacheHashTemplate == 0 || cacheHashTemplate != hashTemplate) {
      cacheHashTemplate = hashTemplate;
      hasChanged = true;
    }

    return hasChanged;
  }

  private int cacheHashEffect = 0;

  private boolean hasEffectsChanged(@Nonnull CardTemplate template) {
    boolean hasChanged = false;

    int hashEffect = Objects.hash(
      template.getBlur(),
      template.isGrayScale(),
      template.getAlphaWhite(),
      template.getAlphaBlack()
    );
    if (cacheHashEffect == 0 || cacheHashEffect != hashEffect) {
      cacheHashEffect = hashEffect;
      hasChanged = true;
    }
    return hasChanged;
  }

}
