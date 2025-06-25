package de.mephisto.vpin.commons.fx.cards;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;

public class CardLayerBackground extends CardLayer {

  @Override
  protected void draw(GraphicsContext g, CardTemplate template, CardData data) throws Exception {
    double width = getWidth();
    double height = getHeight();
    BufferedImage backgroundImage = getBackgroundImage(template, data, width, height);

    if (!template.isTransparentBackground()) {
      if (template.getBlur() > 0) {
        backgroundImage = ImageUtil.blurImage(backgroundImage, template.getBlur());
      }

      if (template.isGrayScale()) {
        backgroundImage = ImageUtil.grayScaleImage(backgroundImage);
      }

      float alphaWhite = template.getAlphaWhite();
      float alphaBlack = template.getAlphaBlack();
      ImageUtil.applyAlphaComposites(backgroundImage, alphaWhite, alphaBlack);
    }

    Image background = SwingFXUtils.toFXImage(backgroundImage, null);
    g.drawImage(background, 0, 0, width, height);

    int borderWidth = template.getBorderWidth();
    g.setFill(Paint.valueOf(template.getFontColor()));
    ImageUtil.drawBorder(g, borderWidth, (int) width, (int) height);
  }

  private BufferedImage getBackgroundImage(CardTemplate template, CardData data, double width, double height) throws IOException {

    if (template.isTransparentBackground()) {
      BufferedImage bufferedImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = (Graphics2D) bufferedImage.getGraphics();
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));

      int value = 255 - (255 * template.getTransparentPercentage() / 100);
      g2.setBackground(new java.awt.Color(0, 0, 0, value));
      g2.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
      g2.dispose();
      return bufferedImage;
    }

    File sourceImage = data.getBackgroundImage();
    if (!sourceImage.exists()) {
      throw new UnsupportedOperationException("No background images found");
    }
    BufferedImage backgroundImage = ImageUtil.loadImage(sourceImage);
    return backgroundImage;
  }
}
