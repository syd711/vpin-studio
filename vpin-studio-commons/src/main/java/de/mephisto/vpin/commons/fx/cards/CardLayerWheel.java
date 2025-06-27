package de.mephisto.vpin.commons.fx.cards;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class CardLayerWheel extends CardLayer {

  private Image cacheImage;

  @Override
  protected void draw(GraphicsContext g, @Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception {
    if (data != null && hasChanged(data)) {
      //file exists && there is place to render it
      File wheelIconFile = data.getWheelImage();
      if (wheelIconFile != null && wheelIconFile.exists()) {
        BufferedImage wheelImage = ImageIO.read(wheelIconFile);
        this.cacheImage = SwingFXUtils.toFXImage(wheelImage, null);
      }
    }

    // draw part
    if (cacheImage != null) {
      g.drawImage(cacheImage, 0, 0, getWidth(), getHeight());
    }
  }

  //------------------------------------ Detetection of layer changes

  private File cacheWheelFile = null;

  private boolean hasChanged(@Nonnull CardData data) {
    boolean hasChanged = false;
    // check on 
    if (cacheWheelFile == null || !cacheWheelFile.equals(data.getWheelImage())) {
      cacheWheelFile = data.getWheelImage();
      hasChanged = true;
    }
    return hasChanged;
  }
}
