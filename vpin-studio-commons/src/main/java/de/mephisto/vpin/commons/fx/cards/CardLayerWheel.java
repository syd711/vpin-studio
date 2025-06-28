package de.mephisto.vpin.commons.fx.cards;

import java.io.File;
import java.io.FileInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CardLayerWheel extends ImageView implements CardLayer {

  private Image cacheImage;

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception {

    if (data != null && hasChanged(data)) {
      //file exists && there is place to render it
      File wheelIconFile = data.getWheelImage();
      if (wheelIconFile != null && wheelIconFile.exists()) {
        //BufferedImage wheelImage = ImageIO.read(wheelIconFile);
        //this.cacheImage = SwingFXUtils.toFXImage(wheelImage, null);

        try (FileInputStream f = new FileInputStream(wheelIconFile)) {
          this.cacheImage = new Image(f);
        }
      }
    }

    // draw part
    if (cacheImage != null) {
      super.setImage(cacheImage);
    }
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
