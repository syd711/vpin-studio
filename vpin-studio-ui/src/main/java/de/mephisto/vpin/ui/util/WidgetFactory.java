package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class WidgetFactory {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetFactory.class);

  public static File snapshot(Pane root) throws IOException {
    int offset = 7;
    SnapshotParameters snapshotParameters = new SnapshotParameters();
    Rectangle2D rectangle2D = new Rectangle2D(offset, offset, root.getWidth() - offset - offset, root.getHeight() - offset- offset);
    snapshotParameters.setViewport(rectangle2D);
    WritableImage snapshot = root.snapshot(snapshotParameters, null);
    BufferedImage bufferedImage = new BufferedImage((int) rectangle2D.getWidth(), (int) rectangle2D.getHeight(), BufferedImage.TYPE_INT_ARGB);
    File file = File.createTempFile("avatar", ".jpg");
    file.deleteOnExit();
    BufferedImage image = SwingFXUtils.fromFXImage(snapshot, bufferedImage);
    ImageIO.write(image, "png", file);
    return file;
  }

  public static Optional<ButtonType> showConfirmation(String msg, String header) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.CLOSE, ButtonType.OK);
    alert.getDialogPane().getStylesheets().add(Studio.class.getResource("stylesheet.css").toExternalForm());
    alert.getDialogPane().getStyleClass().add("base-component");
    alert.getDialogPane().setStyle("-fx-font-size: 14px;");
    alert.setHeaderText(header);
    alert.setGraphic(null);
    return alert.showAndWait();
  }

  public static Optional<ButtonType> showInformation(String msg, String header) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
    alert.getDialogPane().getStylesheets().add(Studio.class.getResource("stylesheet.css").toExternalForm());
    alert.getDialogPane().getStyleClass().add("base-component");
    alert.getDialogPane().setStyle("-fx-font-size: 14px;");
    alert.setHeaderText(header);
    alert.setGraphic(null);
    return alert.showAndWait();
  }

  public static void showAlert(String msg) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.CLOSE);
    alert.getDialogPane().getStylesheets().add(Studio.class.getResource("stylesheet.css").toExternalForm());
    alert.getDialogPane().getStyleClass().add("base-component");
    alert.getDialogPane().setStyle("-fx-font-size: 14px;");
    alert.setHeaderText(null);
    alert.setGraphic(null);
    alert.showAndWait();
  }

  public static String showInputDialog(String title, String description, String msg) {
    TextInputDialog td = new TextInputDialog(msg);
    td.setTitle(title);
    td.getDialogPane().getStylesheets().add(Studio.class.getResource("stylesheet.css").toExternalForm());
    td.getDialogPane().getStyleClass().add("base-component");
    td.getDialogPane().setStyle("-fx-font-size: 16px;");
    td.setHeaderText(description);
    td.setGraphic(null);
    td.showAndWait();

    return td.getResult();
  }

  public static class RationListCell extends ListCell<String> {
    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setText(null);
      if (item != null) {
        setText(item
            .replaceAll("_", " ")
            .replaceAll("ATIO", "atio")
            .replaceAll("x", " x ")
        );
      }
    }
  }

  public static Node createMediaContainer(BorderPane parent, GameMediaItemRepresentation mediaItem, boolean ignored) {
    if (parent.getCenter() != null) {
      disposeMediaBorderPane(parent);
    }

    Node top = parent.getTop();
    if (top != null) {
      top.setVisible(mediaItem != null && !ignored);
    }

    if (ignored) {
      Label label = new Label("Screen is ignored.");
      label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
      parent.setCenter(label);
      return parent;
    }

    if (mediaItem == null) {
      Label label = new Label("No media found.");
      label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
      parent.setCenter(label);
      return parent;
    }

    return addMediaItemToBorderPane(mediaItem, parent);
  }

  public static Node addMediaItemToBorderPane(GameMediaItemRepresentation mediaItem, BorderPane parent) {
    String mimeType = mediaItem.getMimeType();
    String url = Studio.client.getURL(mediaItem.getUri());
    String baseType = mimeType.split("/")[0];
    if (baseType.equals("image")) {
      ImageView imageView = new ImageView();
      imageView.setFitWidth(parent.getPrefWidth() - 10);
      imageView.setFitHeight(parent.getPrefWidth() - 20);
      imageView.setPreserveRatio(true);

      byte[] bytes = RestClient.getInstance().readBinary(url);
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
      Image image = new Image(byteArrayInputStream);
      imageView.setImage(image);
      imageView.setUserData(mediaItem);

      parent.setCenter(imageView);
      return imageView;
    }
    else if (baseType.equals("video") || baseType.equals("audio")) {
      Media media = new Media(url);
      MediaPlayer mediaPlayer = new MediaPlayer(media);
      mediaPlayer.setAutoPlay(baseType.equals("video"));
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(true);
      mediaPlayer.setOnError(() -> {
        LOG.error("Media player error: " + mediaPlayer.getError());
        mediaPlayer.stop();
        mediaPlayer.dispose();

        Label label = new Label("  Media Error\nReselect table.");
        label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
        parent.setCenter(label);
      });

      MediaView mediaView = new MediaView(mediaPlayer);
      mediaView.setUserData(mediaItem);
      mediaView.setPreserveRatio(true);

      if (parent.getId().equals("screenPlayField")) {
        mediaView.rotateProperty().set(90);
        mediaView.setFitWidth(440);
        mediaView.setX(0);
        mediaView.setY(0);
        mediaView.translateXProperty().set(mediaView.translateXProperty().get() - 96);
      }
      else if (parent.getId().equals("screenLoading")) {
        mediaView.rotateProperty().set(90);
        mediaView.setFitWidth(70);
        mediaView.setX(0);
        mediaView.setY(0);
      }
      else if (baseType.equals("video")) {
        mediaView.setFitWidth(parent.getPrefWidth() - 12);
        mediaView.setFitHeight(parent.getPrefHeight() - 50);
      }
      else if (baseType.equals("audio")) {
        mediaPlayer.setOnEndOfMedia(() -> {
          Button playButton = (Button) parent.getTop();
          FontIcon icon = (FontIcon) playButton.getChildrenUnmodifiable().get(0);
          icon.setIconLiteral("bi-play");
        });
      }

      parent.setCenter(mediaView);

      return mediaView;
    }
    else {
      throw new UnsupportedOperationException("Invalid media mime type " + mimeType);
    }
  }

  public static void disposeMediaBorderPane(BorderPane node) {
    Node center = node.getCenter();
    if (center != null) {
      if (center instanceof MediaView) {
        MediaView view = (MediaView) center;
        if (view.getMediaPlayer() != null) {
          view.getMediaPlayer().stop();
          view.getMediaPlayer().dispose();
        }
        node.setCenter(null);
      }
      else if (center instanceof ImageView) {
        ImageView view = (ImageView) center;
        view.setImage(null);
      }
    }

    Node top = node.getTop();
    if (top != null) {
      if (top instanceof Button) {
        Button button = (Button) top;
        button.setVisible(false);
      }
    }
  }

  public static class OverlayBackgroundImageListCell extends ListCell<String> {
    private final VPinStudioClient client;

    public OverlayBackgroundImageListCell(VPinStudioClient client) {
      this.client = client;
    }

    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        Image image = new Image(client.getOverlayBackgroundImage(item));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(80);

        int percentageWidth = (int) (80 * 100 / image.getWidth());
        int height = (int) (image.getHeight() * percentageWidth / 100);
        imageView.setFitHeight(height);
        setGraphic(imageView);
        setText(item);
      }
    }
  }

  public static class HighscoreBackgroundImageListCell extends ListCell<String> {
    private final VPinStudioClient client;

    public HighscoreBackgroundImageListCell(VPinStudioClient client) {
      this.client = client;
    }

    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        Image image = new Image(client.getHighscoreBackgroundImage(item));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(80);

        int percentageWidth = (int) (80 * 100 / image.getWidth());
        int height = (int) (image.getHeight() * percentageWidth / 100);
        imageView.setFitHeight(height);
        setGraphic(imageView);
        setText(item);
      }
    }
  }
}
