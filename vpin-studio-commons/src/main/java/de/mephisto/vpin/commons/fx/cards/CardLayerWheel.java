package de.mephisto.vpin.commons.fx.cards;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class CardLayerWheel extends CardLayer {

  @Override
  protected void draw(GraphicsContext g, CardTemplate template, CardData data) throws Exception {

    //file exists && there is place to render it
    File wheelIconFile = data.getWheelImage();
    if (wheelIconFile != null && wheelIconFile.exists()) {
      BufferedImage wheelImage = ImageIO.read(wheelIconFile);
      Image wImage = SwingFXUtils.toFXImage(wheelImage, null);
      g.drawImage(wImage, 0, 0, getWidth(), getHeight());
    }
  }
}
