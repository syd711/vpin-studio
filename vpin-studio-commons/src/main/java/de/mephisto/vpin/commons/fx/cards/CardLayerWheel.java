package de.mephisto.vpin.commons.fx.cards;

import java.io.ByteArrayInputStream;

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

    if (data != null && hasChanged(data)) {
      if (data.getWheel() != null) {
        this.cacheImage = new Image(new ByteArrayInputStream(data.getWheel()));
      } else {
        this.cacheImage = null;
      }
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

  private int cacheGameId = -1;

  private boolean hasChanged(@Nonnull CardData data) {
    boolean hasChanged = false;
    // check on 
    if (cacheGameId == -1 || cacheGameId != data.getGameId()) {
      cacheGameId = data.getGameId();
      hasChanged = true;
    }
    return hasChanged;
  }

  @Override
  public void forceRefresh() {
    cacheGameId = -1;
  }
}
