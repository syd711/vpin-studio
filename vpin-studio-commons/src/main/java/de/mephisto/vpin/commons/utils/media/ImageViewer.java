package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageViewer extends AssetMediaPlayer {

  private ImageView imageView;

  public void render(String url, Object userdata, String screenName, boolean invertPlayfield) {
    setLoading();
    JFXFuture.supplyAsync(() -> new Image(url)).thenAcceptLater(i -> { 
      render(i, userdata, screenName, invertPlayfield);
    });
  }

  public void render(@NonNull Image image) {
    render(image, null, null, false);
  }

  public void render(@NonNull Image image, String screenName, boolean invertPlayfield) {
    render(image, null, screenName, invertPlayfield);
  }

  public void render(@NonNull FrontendMediaItemRepresentation mediaItem, @NonNull Image image, boolean invertPlayfield) {
    render(image, mediaItem, mediaItem.getScreen(), invertPlayfield);
  }

  public ImageView getImageView() {
    return imageView;
  }

  private void render(@NonNull Image image, @Nullable Object userdata, @Nullable String screenName, boolean invertPlayfield) {
    this.imageView = new ImageView(image);
    setCenter(imageView);
    imageView.setPreserveRatio(true);
    imageView.setUserData(userdata);

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

  public void disposeImage() {
    if (imageView != null) {
      imageView.setImage(null);
    }
  }
}
