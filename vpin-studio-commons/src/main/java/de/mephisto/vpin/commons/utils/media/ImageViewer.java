package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class ImageViewer extends AssetMediaPlayer {
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ImageView imageView;

  public void render(String url,  @Nullable VPinScreen screen, boolean invertPlayfield) {
    setLoading();
    JFXFuture.supplyAsync(() -> CommonImageUtil.loadImageFromUrl(url)).thenAcceptLater(i -> {
        assert i != null;
        if (i.isError()) {
            LOG.error("Image failed to load: {}", i.getException().getMessage(), i.getException());
            return;
        }
      renderImage(i, screen, invertPlayfield);
    });
  }

  private void renderImage(@NonNull Image image, @Nullable VPinScreen screen, boolean invertPlayfield) {
    this.imageView = new ImageView(image);
    setCenter(imageView);
    imageView.setPreserveRatio(true);

    if (mediaOptions == null || mediaOptions.isAutoRotate()) {
      boolean rotated = false;
      if (VPinScreen.PlayField.equals(screen)) {
        if (image.getWidth() > image.getHeight()) {
          imageView.setRotate(90 + (invertPlayfield ? 180 : 0));
          rotated = true;
        }
      }
      else if (VPinScreen.Loading.equals(screen)) {
        if (image.getWidth() > image.getHeight()) {
          imageView.setRotate(90);
          rotated = true;
        }
      }
      setRotated(rotated);
    }
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
