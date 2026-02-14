package de.mephisto.vpin.commons.fx.cards;

import java.io.ByteArrayInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class CardLayerBaseImage extends Canvas implements CardLayer {

  private Image cacheImage;

  protected abstract byte[] getImage(@Nonnull CardTemplate template, @Nullable CardData data);

  protected abstract boolean keepAspectRatio(@Nonnull CardTemplate template);

  protected abstract int getAlignment(@Nonnull CardTemplate template);


  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) {
    byte[] image = getImage(template, data);

    if (image != null) {
      if (hasChanged(image)) {
        this.cacheImage = new Image(new ByteArrayInputStream(image));
      }
    } else {
      this.cacheImage = null;
      this.cacheBytesImage = null;
    }

    // draw part

    double x = 0;
    double y = 0;
    double width = getWidth();
    double height = getHeight();

    GraphicsContext g = getGraphicsContext2D();
    g.clearRect(x, y, width, height);

    if (cacheImage != null) {

      int alignment = getAlignment(template);

      if (keepAspectRatio(template)) {
        double imgwidth = cacheImage.getWidth();
        double imgheight = cacheImage.getHeight();

        if (width / height > imgwidth / imgheight) {
          // adjust width
          width = height * imgwidth / imgheight;
          if (CardTemplate.isOn(alignment, CardTemplate.CENTER)) {
            x = (getWidth() - width) / 2;
          }
          else if (CardTemplate.isOn(alignment, CardTemplate.RIGHT)) {
            x = getWidth() - width;
          }
        }
        else {
          height = width * imgheight / imgwidth;
          if (CardTemplate.isOn(alignment, CardTemplate.MIDDLE)) {
            y = (getHeight() - height) / 2;
          }
          else if (CardTemplate.isOn(alignment, CardTemplate.BOTTOM)) {
            y = getHeight() - height;
          }
        } 
      }
      g.drawImage(cacheImage, x, y, width, height);
    }
  }

  //------------------------------------ Detetection of layer changes

  private byte[] cacheBytesImage = null;

  protected boolean hasChanged(byte[] image) {
    boolean hasChanged = false;
    // check on 
    if (cacheBytesImage == null || !Arrays.equals(cacheBytesImage, image)) {
      cacheBytesImage = image;
      hasChanged = true;
    }
    return hasChanged;
  }

  @Override
  public void forceRefresh() {
    cacheBytesImage = null;
  }
}
