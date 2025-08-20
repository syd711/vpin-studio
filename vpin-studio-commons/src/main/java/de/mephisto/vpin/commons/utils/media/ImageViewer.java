package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageViewer extends MediaViewPane {

  private ImageView imageView;
  private Image image;
  private String url;

  public ImageViewer(String url, Object userdata, String screenName, boolean invertPlayfield) {
    this.url = url;
    render(userdata, screenName, invertPlayfield);
  }

  public ImageViewer(String url, Image image) {
    this.url = url;
    this.image = image;
    render(null, null, false);
  }

  public ImageViewer(FrontendMediaItemRepresentation mediaItem, Image image, boolean invertPlayfield) {
    this.image = image;

    render(mediaItem, mediaItem.getScreen(), invertPlayfield);
  }

  public ImageView getImageView() {
    return imageView;
  }

  private void render(Object userdata, String screenName, boolean invertPlayfield) {
    if (image == null) {
      setLoading();

      JFXFuture.supplyAsync(() -> new Image(url)).thenAcceptLater(i -> { 
        this.image = i; 
        displayImage(userdata, screenName, invertPlayfield);
      });
    }
    else {
      displayImage(userdata, screenName, invertPlayfield);
    }
  }

  private void displayImage(@Nullable Object userdata, @Nullable String screenName, boolean invertPlayfield) {
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
