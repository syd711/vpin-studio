package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class ImageViewer extends BorderPane {

  @NonNull
  protected final BorderPane parent;

  private ImageView imageView;
  private Image image;
  private String url;

  public ImageViewer(@NonNull BorderPane parent, String url, Object userdata, String screenName, boolean invertPlayfield) {
    this.parent = parent;
    this.url = url;

    double prefWidth = parent.getPrefWidth();
    double prefHeight = parent.getPrefHeight();

    render(userdata, screenName, invertPlayfield, prefWidth - 10, prefHeight - 10);
  }

  public ImageViewer(BorderPane parent, FrontendMediaItemRepresentation mediaItem, Image image, boolean invertPlayfield) {
    this.parent = parent;
    this.image = image;

    double prefWidth = parent.getPrefWidth();
    if (prefWidth <= 0) {
      prefWidth = ((Pane) parent.getParent()).getWidth();
    }
    double prefHeight = parent.getPrefHeight();
    if (prefHeight <= 0) {
      prefHeight = ((Pane) parent.getParent()).getHeight();
    }

    render(mediaItem, mediaItem.getScreen(), invertPlayfield, prefWidth - 10, prefHeight - 60);
  }

  private void render(Object userdata, String screenName, boolean invertPlayfield, double width, double height) {

    this.setCenter(new ProgressIndicator());
    parent.setCenter(this);

    new Thread(() -> {
      if (image == null) {
        image = new Image(url);
      }

      Platform.runLater(() -> {
        this.imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setUserData(userdata);

        if (VPinScreen.PlayField.getSegment().equalsIgnoreCase(screenName)) {
          if (image.getWidth() > image.getHeight()) {
            imageView.setRotate(90 + (invertPlayfield ? 180 : 0));
          }
        }
        else if (VPinScreen.Loading.getSegment().equalsIgnoreCase(screenName)) {
          if (image.getWidth() > image.getHeight()) {
            imageView.setRotate(90);
          }
        }
        this.setCenter(imageView);
      });
    }).start();
  }

  public void scaleForTemplate(ImageView cardPreview) {
    imageView.setPreserveRatio(false);
    imageView.setFitWidth(cardPreview.getFitWidth());
    imageView.setFitHeight(cardPreview.getFitWidth() / 16 * 9);
  }

  public void disposeImage() {
    if (imageView != null) {
      this.imageView.setImage(null);
    }
  }
}
