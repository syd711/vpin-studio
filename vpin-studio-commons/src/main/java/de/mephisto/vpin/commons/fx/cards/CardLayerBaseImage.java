package de.mephisto.vpin.commons.fx.cards;

import java.io.ByteArrayInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class CardLayerBaseImage extends ImageView implements CardLayer {

  private Image cacheImage;

  protected abstract byte[] getImage(@Nonnull CardTemplate template, @Nullable CardData data);

  protected abstract boolean keepAspectRatio(@Nonnull CardTemplate template);

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) {
    byte[] image = getImage(template, data);

    if (image != null) {
      if (hasChanged(image)) {
        this.cacheImage = new Image(new ByteArrayInputStream(image));
        if (keepAspectRatio(template)) {
          this.setPreserveRatio(true);
          double imgwidth = cacheImage.getWidth();
          double imgheight = cacheImage.getHeight();
          double height = getWidth() * imgheight / imgwidth;
          setHeight(height);
        }
        else {
          this.setPreserveRatio(false);
          double imgwidth = getImageWidth(template);
          double imgheight = getImageHeight(template);
          setHeight(imgwidth);
          setHeight(imgheight);
        }
      }
    } else {
      this.cacheImage = null;
      this.cacheBytesImage = null;
    }

    // draw part

    // set image even when null, so that the card of a game without wheel is correctly rendered
    super.setImage(cacheImage);
  }

  //------------------------------------ Detetection of layer changes

  @Override
  public double getWidth() {
    return getFitWidth();
  }

  @Override
  public void setWidth(double w) {
    setFitWidth(w);
  }

  @Override
  public double getHeight() {
    return getFitHeight();
  }

  @Override
  public void setHeight(double h) {
    setFitHeight(h);
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

  protected abstract double getImageWidth(@Nonnull CardTemplate template);

  protected abstract double getImageHeight(@Nonnull CardTemplate template);
}
