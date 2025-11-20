package de.mephisto.vpin.commons.fx.cards;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CardLayerWheel extends ImageView implements CardLayer {

  private Image cacheImage;

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) {

    byte[] image = data != null ? data.getWheel() : null;

    if (image != null) {
      if (hasChanged(image)) {
        this.cacheImage = new Image(new ByteArrayInputStream(image));
        this.setPreserveRatio(true);
      }
    } else {
      this.cacheImage = null;
      this.cacheBytesImage = null;
    }

    // draw part

    // set image even when null, so that the card of a game without wheel is correctly rendered
    super.setImage(cacheImage);
  }

  //------------------------------------ 

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
}
