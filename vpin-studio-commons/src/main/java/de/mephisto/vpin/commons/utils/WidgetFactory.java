package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.commons.fx.*;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class WidgetFactory {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetFactory.class);

  public static Label createDefaultLabel(String msg) {
    Label label = new Label(msg);
    label.setStyle("-fx-font-size: 14px;");
    return label;
  }

  public static File snapshot(Pane root) throws IOException {
    int offset = 7;
    SnapshotParameters snapshotParameters = new SnapshotParameters();
    Rectangle2D rectangle2D = new Rectangle2D(offset, offset, root.getWidth() - offset - offset, root.getHeight() - offset - offset);
    snapshotParameters.setViewport(rectangle2D);
    WritableImage snapshot = root.snapshot(snapshotParameters, null);
    BufferedImage bufferedImage = new BufferedImage((int) rectangle2D.getWidth(), (int) rectangle2D.getHeight(), BufferedImage.TYPE_INT_ARGB);
    File file = File.createTempFile("avatar", ".jpg");
    file.deleteOnExit();
    BufferedImage image = SwingFXUtils.fromFXImage(snapshot, bufferedImage);
    ImageIO.write(image, "png", file);
    return file;
  }

  public static FontIcon createCheckIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#66FF66"));
    fontIcon.setIconLiteral("bi-check-circle");
    return fontIcon;
  }


  public static FontIcon createBotIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#5865F2"));
    fontIcon.setIconLiteral("mdi2r-robot");
    return fontIcon;
  }

  public static FontIcon createAlertIcon(String s) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#FF3333"));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createGreenIcon(String s) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#66FF66"));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createIcon(String s) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createCheckboxIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
    fontIcon.setIconLiteral("bi-check-circle");
    return fontIcon;
  }

  public static FontIcon createExclamationIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#FF3333"));
    fontIcon.setIconLiteral("bi-exclamation-circle");
    return fontIcon;
  }

  public static String hexColor(Integer color) {
    String hex = "FFFFFF";
    if (color != null) {
      if (color == 0) {
        hex = "000000";
      }
      else {
        hex = "" + Integer.toHexString(color);
      }
    }
    while (hex.length() < 6) {
      hex = "0" + hex;
    }
    return "#" + hex;
  }

  public static Label createPlaylistIcon(PlaylistRepresentation playlist) {
    Label label = new Label();
    label.setTooltip(new Tooltip(playlist.getName()));
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(24);
    fontIcon.setIconColor(Paint.valueOf(hexColor(playlist.getMenuColor())));
    fontIcon.setIconLiteral("mdi2v-view-list");
    label.setGraphic(fontIcon);
    return label;
  }

  public static Stage createStage() {
    Stage stage = new Stage();
    stage.getIcons().add(new Image(OverlayWindowFX.class.getResourceAsStream("logo-64.png")));
    return stage;
  }

  public static Stage createDialogStage(Class clazz, Stage owner, String title, String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    return createDialogStage(fxmlLoader, owner, title);
  }

  public static Stage createDialogStage(FXMLLoader fxmlLoader, Stage owner, String title) {
    Parent root = null;

    try {
      root = fxmlLoader.load();
    } catch (IOException e) {
      LOG.error("Error loading: " + e.getMessage(), e);
    }

    DialogController controller = fxmlLoader.getController();

    Node header = root.lookup("#header");
    DialogHeaderController dialogHeaderController = (DialogHeaderController) header.getUserData();
    dialogHeaderController.setTitle(title);

    final Stage stage = createStage();
    dialogHeaderController.setStage(stage);
    stage.initOwner(owner);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initStyle(StageStyle.UNDECORATED);
    stage.setTitle(title);
    stage.setUserData(controller);

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    scene.getRoot().setStyle("-fx-border-width: 1;-fx-border-color: #605E5E;");
    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        if (controller != null) {
          controller.onDialogCancel();
        }
        stage.close();
      }
    });
    return stage;
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text) {
    return showConfirmation(owner, text, null, null);
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1) {
    return showConfirmation(owner, text, help1, null);
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1, String help2) {
    return showConfirmation(owner, text, help1, help2, null);
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1, String help2, String btnText) {
    Stage stage = createDialogStage(ConfirmationDialogController.class, owner, "Confirmation", "dialog-confirmation.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.initDialog(stage, null, btnText, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<ButtonType> showInformation(Stage owner, String text, String help1) {
    return showInformation(owner, text, help1, null);
  }

  public static Optional<ButtonType> showInformation(Stage owner, String text, String help1, String help2) {
    Stage stage = createDialogStage(ConfirmationDialogController.class, owner, "Information", "dialog-confirmation.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static void showAlert(Stage owner, String msg) {
    showAlert(owner, msg, null, null);
  }

  public static void showAlert(Stage owner, String msg, String help1) {
    showAlert(owner, msg, help1, null);
  }

  public static void showAlert(Stage owner, String msg, String help1, String help2) {
    Stage stage = createDialogStage(ConfirmationDialogController.class, owner, "Information", "dialog-alert.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, msg, help1, help2);
    stage.showAndWait();
  }

  public static Optional<ButtonType> showAlertOption(Stage owner, String msg, String altOptionText, String okText, String help1, String help2) {
    Stage stage = createDialogStage(ConfirmationDialogController.class, owner, "Information", "dialog-alert-option.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static String showInputDialog(Stage owner, String dialogTitle, String innerTitle, String description, String helpText, String defaultValue) {
    Stage stage = createDialogStage(InputDialogController.class, owner, dialogTitle, "dialog-input.fxml");
    InputDialogController controller = (InputDialogController) stage.getUserData();
    controller.initDialog(stage, innerTitle, description, helpText, defaultValue);
    stage.showAndWait();
    Optional<ButtonType> result = controller.getResult();
    if (result.get().equals(ButtonType.OK)) {
      return controller.getText();
    }

    return null;
  }

  public static void showOutputDialog(Stage owner, String dialogTitle, String innerTitle, String description, String defaultValue) {
    Stage stage = createDialogStage(OutputDialogController.class, owner, dialogTitle, "dialog-output.fxml");
    OutputDialogController controller = (OutputDialogController) stage.getUserData();
    controller.initDialog(stage, innerTitle, description, defaultValue);
    stage.showAndWait();
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

  public static void createMediaContainer(VPinStudioClient client, BorderPane parent, GameMediaItemRepresentation mediaItem, boolean ignored, boolean previewEnabled) {
    if (parent.getCenter() != null) {
      disposeMediaBorderPane(parent);
    }

    if (ignored) {
      Label label = new Label("Screen is ignored");
      label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
      parent.setCenter(label);
    }

    if (mediaItem == null) {
      Label label = new Label("No media found");
      label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
      parent.setCenter(label);
    }

    if (!previewEnabled) {
      Label label = new Label("Preview disabled");
      if (mediaItem == null) {
        label.setText("No media found");
      }
      label.setUserData(mediaItem);
      label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");

      if (mediaItem != null) {
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
      }
      parent.setCenter(label);
    }

    if (!ignored && previewEnabled && mediaItem != null) {
      addMediaItemToBorderPane(client, mediaItem, parent);
    }

    if (parent.getId().equalsIgnoreCase("screenAudioLaunch") || parent.getId().equalsIgnoreCase("screenAudio")) {
      VBox bottom = (VBox) parent.getBottom();
      Button playButton = (Button) bottom.getChildren().get(0);
      playButton.setVisible(!ignored && previewEnabled && mediaItem != null);
    }
  }

  public static void addMediaItemToBorderPane(VPinStudioClient client, GameMediaItemRepresentation mediaItem, BorderPane parent) {
    boolean portraitMode = client.getSystemService().getScreenInfo().isPortraitMode();

    String mimeType = mediaItem.getMimeType();
    String baseType = mimeType.split("/")[0];
    String url = client.getURL(mediaItem.getUri());

    if (baseType.equals("image")) {
      ImageView imageView = new ImageView();
      imageView.setFitWidth(parent.getPrefWidth() - 10);
      imageView.setFitHeight(parent.getPrefHeight() - 60);
      imageView.setPreserveRatio(true);

      ByteArrayInputStream gameMediaItem = client.getAssetService().getGameMediaItem(mediaItem.getGameId(), PopperScreen.valueOf(mediaItem.getScreen()));
      Image image = new Image(gameMediaItem);
      imageView.setImage(image);
      imageView.setUserData(mediaItem);

      parent.setCenter(imageView);
    }
    else if (baseType.equals("audio")) {
      Media media = new Media(url);
      MediaPlayer mediaPlayer = new MediaPlayer(media);
      mediaPlayer.setAutoPlay(false);
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(true);
      mediaPlayer.setOnError(() -> {
        LOG.error("Media player error: " + mediaPlayer.getError() + ", URL: " + url);
        mediaPlayer.stop();
        mediaPlayer.dispose();

        Label label = new Label("Media Error");
        label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
        label.setUserData(mediaItem);
        parent.setCenter(label);
      });

      mediaPlayer.setOnEndOfMedia(() -> {
        VBox bottom = (VBox) parent.getBottom();
        Button playButton = (Button) bottom.getChildren().get(0);
        playButton.setVisible(true);
        FontIcon icon = (FontIcon) playButton.getChildrenUnmodifiable().get(0);
        icon.setIconLiteral("bi-play");
      });

      MediaView mediaView = new MediaView(mediaPlayer);
      parent.setCenter(mediaView);
    }
    else if (baseType.equals("video")) {
      Media media = new Media(url);
      MediaPlayer mediaPlayer = new MediaPlayer(media);
      mediaPlayer.setAutoPlay(baseType.equals("video"));
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(true);
      mediaPlayer.setOnError(() -> {
        LOG.error("Media player error: " + mediaPlayer.getError() + ", URL: " + url);
        mediaPlayer.stop();
        mediaPlayer.dispose();

        Label label = new Label("  Media available\n(but not playable)");
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
        label.setUserData(mediaItem);
        parent.setCenter(label);
      });

      MediaView mediaView = new MediaView(mediaPlayer);
      mediaView.setUserData(mediaItem);
      mediaView.setPreserveRatio(true);

      if (parent.getId().equals("screenPlayField")) {
        mediaView.setFitWidth(250);
        if (!portraitMode) {
          mediaView.rotateProperty().set(90);
          mediaView.setFitWidth(440);
          mediaView.setX(0);
          mediaView.setY(0);
          mediaView.translateXProperty().set(mediaView.translateXProperty().get() - 96);
        }
      }
      else if (parent.getId().equals("screenLoading")) {
        mediaView.setFitWidth(150);
        if (!portraitMode) {
          mediaView.rotateProperty().set(90);
          mediaView.setFitWidth(70);
          mediaView.setX(0);
          mediaView.setY(0);
        }
      }
      else {
        mediaView.setFitWidth(parent.getPrefWidth() - 12);
        mediaView.setFitHeight(parent.getPrefHeight() - 50);
      }

      parent.setCenter(mediaView);
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
          String source = view.getMediaPlayer().getMedia().getSource();
          view.getMediaPlayer().stop();
          final ExecutorService executor = Executors.newFixedThreadPool(1);
          final Future<?> future = executor.submit(() -> {
            view.getMediaPlayer().dispose();
          });
          try {
            future.get(500, TimeUnit.MILLISECONDS);
          } catch (Exception e) {
            LOG.error("Error disposing media view (" + source + "): " + e.getMessage());
          }
          executor.shutdownNow();
        }
        node.setCenter(null);
      }
      else if (center instanceof ImageView) {
        ImageView view = (ImageView) center;
        view.setImage(null);
      }
      else {
        node.setCenter(null);
      }
    }
  }

  public static class PlaylistBackgroundImageListCell extends ListCell<PlaylistRepresentation> {

    public PlaylistBackgroundImageListCell() {
    }

    protected void updateItem(PlaylistRepresentation item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(24);
        fontIcon.setIconColor(Paint.valueOf(WidgetFactory.hexColor(item.getMenuColor())));
        fontIcon.setIconLiteral("mdi2v-view-list");
        setGraphic(fontIcon);

        setText(" " + item.toString());
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
        Image image = new Image(client.getHighscoreCardsService().getHighscoreBackgroundImage(item));
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

  public static class VpsTableListCell extends ListCell<String> {

    private final String comment;
    private final List<String> authors;
    private final String version;
    private final List<String> features;

    public VpsTableListCell(String comment, List<String> authors, String version, List<String> features) {
      this.comment = comment;
      this.authors = authors;
      this.version = version;
      this.features = features;
    }

    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        VBox root = new VBox();
        root.setStyle("-fx-padding: 3 3 3 3");

        if (comment != null) {
          Label label = new Label(comment);
          root.getChildren().add(label);
        }

        setGraphic(root);
        setText(item);
      }
    }
  }
}
