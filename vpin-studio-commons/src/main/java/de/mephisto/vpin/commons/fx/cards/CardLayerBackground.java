package de.mephisto.vpin.commons.fx.cards;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

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

    LogTime lt = new LogTime("   CardLayerBackground");

    boolean imageDirty = false;
    if (hasBackroundChanged(template, data)) {
      this.cacheBackgroundImage = getBackgroundImage(template, data);
      lt.pulse("getBackgroundImage()");
      imageDirty = true;
    }
    // if the backgroundImage has Changed, effect must be re-applied
    if (imageDirty || hasEffectsChanged(template)) {
      // if background has not change, just reapply effect on cached background image
      cacheFinalImage = ImageUtil.clone(cacheBackgroundImage);

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

    if (imageDirty) {
      cacheBackground = SwingFXUtils.toFXImage(cacheFinalImage, null);
      lt.pulse("toFXImage()");
    }

    //--------------------------
    // Draw Part

    double framex = template.getMarginLeft() * zoomX;
    double framey = template.getMarginTop() * zoomY;
    double framewidth = width - (template.getMarginLeft() + template.getMarginRight()) * zoomX;
    double frameheight = height - (template.getMarginTop() + template.getMarginBottom()) * zoomY;
    int border = template.getBorderWidth() > 0 ? (int) (template.getBorderWidth() * zoomX) : 0;
    double radiusx = template.getBorderRadius() * 2 * zoomX;
    double radiusy = template.getBorderRadius() * 2 * zoomY;

    // Set the clip region
    Rectangle rect = new Rectangle(framex, framey, framewidth, frameheight);
    rect.setArcWidth(radiusx);
    rect.setArcHeight(radiusy);
    this.setClip(rect);

    if (cacheBackground != null) {

      // Use java fx for blur effect, much quicker !
      GaussianBlur blur = null;
      if (template.getBlur() > 0) {
        blur = new GaussianBlur(template.getBlur());
      }
      g.setEffect(blur);

      // Use java fx for transparency
      g.setGlobalAlpha(1.0 - template.getTransparentPercentage() / 100.0);

      // coords in image
      double imgZoom = template.getZoom() / 100;    // max zoom is 100
      double imgWidth = cacheBackground.getWidth();
      double imgHeight = cacheBackground.getHeight();

      // Resize image in proportion of the frame and calculate offset to center
      if (framewidth * imgHeight > frameheight * imgWidth) {
        imgHeight = imgWidth * frameheight / framewidth;
      } else {
        imgWidth = imgHeight * framewidth / frameheight;
      }

      // zoom the image and center it
      imgWidth *= imgZoom;
      imgHeight *= imgZoom;
      //double imgX = (cacheBackground.getWidth() - imgWidth) / 2.0;
      //double imgY = (cacheBackground.getHeight() - imgHeight) / 2.0;

      double imgX = (1 + template.getBackgroundX()) * (cacheBackground.getWidth() - imgWidth) / 2.0;
      double imgY = (1 + template.getBackgroundY()) * (cacheBackground.getHeight() - imgHeight) / 2.0;

/*
System.out.println("framewidth x frameheight = " + framewidth + " x " + frameheight + "(ratio " + (framewidth/frameheight) + ")");
System.out.println("ImgW x ImhH = " + cacheBackground.getWidth() + " x " + cacheBackground.getHeight() + "(ratio " + (cacheBackground.getWidth()/cacheBackground.getHeight()) + ")");
System.out.println("Img Zoom = " + imgZoom);
System.out.println("W x H = " + framewidth + " x " + frameheight  + "(ratio " + (framewidth/frameheight) + ")");
System.out.println("w x h = " + imgWidth + " x " + imgHeight  + "(ratio " + (imgWidth/imgHeight) + ")");
System.out.println("");
*/

      g.drawImage(cacheBackground, imgX, imgY, imgWidth, imgHeight, framex  + border, framey + border, framewidth - 2 * border, frameheight - 2 * border);
      lt.pulse("drawImage()");
    }

    // remove applied effects for drawing the border
    g.setEffect(null);
    g.setGlobalAlpha(1.0);

    if (template.getBorderWidth() > 0) {
      g.setStroke(Paint.valueOf(template.getBorderColor()));
      double strokeWidth = template.getBorderWidth() * zoomX;

      g.setLineWidth(strokeWidth);

      g.strokeRoundRect(framex + strokeWidth / 2, framey + strokeWidth / 2, framewidth - strokeWidth, frameheight -  + strokeWidth, 
          template.getBorderRadius() * 2 * zoomX, template.getBorderRadius() * 2 * zoomY);


      lt.pulse("drawBorder()");
    }
  }

  private @Nullable BufferedImage getBackgroundImage(@Nonnull CardTemplate template, @Nullable CardData data) {

    if (template.isUseColoredBackground()) {
      BufferedImage bufferedImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = (Graphics2D) bufferedImage.getGraphics();
      g2.setColor(java.awt.Color.decode(template.getBackgroundColor()));
      g2.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
      g2.dispose();
      return bufferedImage;
    }

    BufferedImage backgroundImage = null;
    if (template.isUseDefaultBackground() && data != null) {
      try {
        backgroundImage = ImageIO.read(new ByteArrayInputStream(data.getBackground()));
      }
      catch (Exception e) {
        LOG.info("Cannot load image, Using default image as fallback instead of default backgroundUrl");
      }
    }
    // fall back or !isUseDefaultBackground()
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

    return backgroundImage;
  }

  //------------------------------------ Detetection of layer changes

  private int cacheHashTemplate = 0;

  private boolean hasBackroundChanged(CardTemplate template, @Nullable CardData data) {
    int hashTemplate = Objects.hash(
      data != null ? data.getGameId() : -1,
      template.isUseDefaultBackground(),
      template.isUseColoredBackground(),
      template.getBackgroundColor(),
      template.getBackground()
    );
    if (cacheHashTemplate == 0 || cacheHashTemplate != hashTemplate) {
      cacheHashTemplate = hashTemplate;
      return true;
    }
    return false;
  }

  private int cacheHashEffect = 0;

  private boolean hasEffectsChanged(@Nonnull CardTemplate template) {
    boolean hasChanged = false;

    int hashEffect = Objects.hash(
      template.isGrayScale(),
      template.getTransparentPercentage(),
      template.getAlphaWhite(),
      template.getAlphaBlack()
    );
    if (cacheHashEffect == 0 || cacheHashEffect != hashEffect) {
      cacheHashEffect = hashEffect;
      hasChanged = true;
    }
    return hasChanged;
  }

  @Override
  public void forceRefresh() {
    cacheHashTemplate = 0;
    cacheHashEffect = 0;
  }

}
