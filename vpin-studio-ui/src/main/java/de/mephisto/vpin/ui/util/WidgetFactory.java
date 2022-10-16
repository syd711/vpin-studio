package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.ui.VPinStudioApplication;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

public class WidgetFactory {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetFactory.class);

  public static void createProgressDialog(ProgressModel model,
                                          Stage owner) throws IOException {
    Parent root = FXMLLoader.load(VPinStudioApplication.class.getResource("dialog-progress.fxml"));

    final Label titleLabel = (Label) root.lookup("#titleLabel");
    final Label progressBarLabel = (Label) root.lookup("#progressBarLabel");
    final ToolBar toolBar = (ToolBar) root.lookup("#bottomToolbar");
    final Button cancelButton = (Button) toolBar.getItems().get(0);
    titleLabel.setText(model.getTitle());

    final ProgressResultModel progressResultModel = new ProgressResultModel();
    final Service service = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            Iterator iterator = model.getIterator();
            int index = 0;
            while (iterator.hasNext() && !this.isCancelled()) {
              String result = model.processNext(progressResultModel);
              long percent = index * 100 / model.getMax();
              updateProgress(percent, 100);
              final int uiIndex = index;
              Platform.runLater(() -> {
                titleLabel.setText(model.getTitle() + " (" + uiIndex + "/" + model.getMax() + ")");
                progressBarLabel.setText("Processing: " + result);
              });
              index++;
            }
            return null;
          }
        };
      }
    };

    final Stage stage = new Stage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initOwner(owner);

    final ProgressBar progressBar = (ProgressBar) root.lookup("#progressBar");
    progressBar.progressProperty().bind(service.progressProperty());
    service.stateProperty().addListener((ChangeListener<Worker.State>) (observable, oldValue, newValue) -> {
      if (newValue == Worker.State.CANCELLED || newValue == Worker.State.FAILED
          || newValue == Worker.State.SUCCEEDED) {
        stage.hide();

        String msg = model.getTitle() + " finished.\n\nProcessed " + progressResultModel.getProcessed() + " of " + model.getMax() + " elements.";
        WidgetFactory.showAlert(msg);
      }
    });

    cancelButton.setOnAction(event -> service.cancel());
    stage.onHidingProperty().addListener((observableValue, windowEventEventHandler, t1) -> service.cancel());

    Scene scene = new Scene(root);
    stage.setScene(scene);
    service.start();
    stage.show();

    scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent -> service.cancel());
  }


  public static void showAlert(String msg) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.CLOSE);
    alert.getDialogPane().getStylesheets().add(VPinStudioApplication.class.getResource("stylesheet.css").toExternalForm());
    alert.getDialogPane().getStyleClass().add("base-component");
    alert.setHeaderText(null);
    alert.setGraphic(null);
    alert.showAndWait();
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

  public static Node createMediaContainer(@NonNull BorderPane parent, @NonNull VPinStudioClient client, @Nullable GameMediaItemRepresentation item) {
    if(item == null) {
      parent.setCenter(null);
      parent.setTop(null);
      return parent;
    }

    String mimeType = item.getMimeType();
    String url = client.getURL(item.getUri());
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

      parent.setCenter(imageView);
      return imageView;
    }
    else if (baseType.equals("video")) {
      Media media = new Media(url);
      MediaPlayer mediaPlayer = new MediaPlayer(media);
      mediaPlayer.setAutoPlay(true);
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(true);
      mediaPlayer.setOnError(() -> {
        LOG.error("Media player error: " + mediaPlayer.getError());
        mediaPlayer.getError().printStackTrace();
      });

      MediaView mediaView = new MediaView(mediaPlayer);
      mediaView.setPreserveRatio(true);

      if(parent.getId().equals("screenPlayfield")) {
        mediaView.rotateProperty().set(90);
        mediaView.setFitWidth(445);
        mediaView.setX(0);
        mediaView.setY(0);
        mediaView.translateXProperty().set(mediaView.translateXProperty().get()-98);
      }
      else if(parent.getId().equals("screenLoading")) {
        mediaView.rotateProperty().set(90);
        mediaView.setFitWidth(76);
        mediaView.setX(0);
        mediaView.setY(0);
      }
      else {
        mediaView.setFitWidth(parent.getPrefWidth() - 10);
        mediaView.setFitHeight(parent.getPrefHeight() - 20);
      }

      parent.setCenter(mediaView);

      return mediaView;
    }
    else {
      throw new UnsupportedOperationException("Invalid media mime type " + mimeType);
    }
  }

  public static class ImageListCell extends ListCell<String> {
    private final VPinStudioClient client;

    public ImageListCell(VPinStudioClient client) {
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
