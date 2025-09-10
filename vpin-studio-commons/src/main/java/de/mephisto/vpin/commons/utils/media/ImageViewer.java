package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageViewer extends AssetMediaPlayer {

  private ImageView imageView;

  public void render(String url,  @Nullable String screenName, boolean invertPlayfield) {
    setLoading();
    JFXFuture.supplyAsync(() -> new Image(url)).thenAcceptLater(i -> { 
      renderImage(i, screenName, invertPlayfield);
    });
  }

  private void renderImage(@NonNull Image image, @Nullable String screenName, boolean invertPlayfield) {
    this.imageView = new ImageView(image);
    setCenter(imageView);
    imageView.setPreserveRatio(true);

    boolean rotated = false;
    if (VPinScreen.PlayField.getSegment().equalsIgnoreCase(screenName)) {
      if (image.getWidth() > image.getHeight()) {
        imageView.setRotate(90 + (invertPlayfield ? 180 : 0));
        rotated = true;
      }
    }
    else if (VPinScreen.Loading.getSegment().equalsIgnoreCase(screenName)) {
      if (image.getWidth() > image.getHeight()) {
        imageView.setRotate(90);
        rotated = true;
      }
    }
    setRotated(rotated);
  }

  public void scaleForTemplate(ImageView cardPreview) {
    if (imageView != null) {
      imageView.setPreserveRatio(false);
      imageView.setFitWidth(cardPreview.getFitWidth());
      imageView.setFitHeight(cardPreview.getFitWidth() / 16 * 9);
    }
  }

  @Override
  public void disposeMedia() {
    super.disposeMedia();
    if (imageView != null) {
      imageView.setImage(null);
    }
  }
}
