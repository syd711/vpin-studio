package de.mephisto.vpin.commons.utils.media;

import java.io.ByteArrayInputStream;

import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class ImageViewer extends BorderPane {
  
  @NonNull
  protected final BorderPane parent;
  
  private Image image;
  private String url;

  public ImageViewer(@NonNull BorderPane parent, String url, Object userdata, double width, double height) {
    this.parent = parent;
    this.url = url;

    render(userdata, width, height);
  }
  public ImageViewer(BorderPane parent, FrontendMediaItemRepresentation mediaItem, Image image, double width, double height) {
    this.parent = parent;
    this.image = image;

    render(mediaItem, width, height);
  }

  private void render(Object userdata, double width, double height) {

    this.setCenter(new ProgressIndicator());
    parent.setCenter(this);

    new Thread(() -> {
      if (image == null) {
        image = new Image(url);
      }

      Platform.runLater(() -> {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setUserData(userdata);

        this.setCenter(imageView);
      });
    }).start();
  }
}
