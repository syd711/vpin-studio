package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.commons.fx.*;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.commons.utils.media.AudioMediaPlayer;
import de.mephisto.vpin.commons.utils.media.VideoMediaPlayer;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.popper.Playlist;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
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

public class WidgetFactory {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetFactory.class);

  public static final String DISABLED_TEXT_STYLE = "-fx-font-color: #B0ABAB;-fx-text-fill:#B0ABAB;";
  public static final String DISABLED_COLOR = "#B0ABAB";
  public static final String ERROR_COLOR = "#FF3333";
  public static final String UPDATE_COLOR = "#CCFF66";
  public static final String TODO_COLOR = UPDATE_COLOR;
  public static final String OK_COLOR = "#66FF66";

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
    File file = File.createTempFile("avatar", ".png");
    file.deleteOnExit();
    BufferedImage image = SwingFXUtils.fromFXImage(snapshot, bufferedImage);
    ImageIO.write(image, "png", file);
    LOG.info("Written avatar temp file " + file.getAbsolutePath() + " [" + FileUtils.readableFileSize(file.length()) + "]");
    return file;
  }

  public static FontIcon createCheckIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#66FF66"));
    fontIcon.setIconLiteral("bi-check-circle");
    return fontIcon;
  }

  public static HBox createCheckAndUpdateIcon(String tooltip) {
    HBox root = new HBox(3);
    root.setAlignment(Pos.CENTER);
    Label icon1 = new Label();
    icon1.setGraphic(createCheckIcon("#FFFFFF"));
    Label icon2 = new Label();
    icon2.setTooltip(new Tooltip(tooltip));
    icon2.setGraphic(createUpdateIcon());

    root.getChildren().addAll(icon2, icon1);
    return root;
  }

  public static FontIcon createUpdateStar() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(14);
    fontIcon.setIconColor(Paint.valueOf(UPDATE_COLOR));
    fontIcon.setIconLiteral("mdi2f-flare");
    return fontIcon;
  }


  public static FontIcon createUpdateIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf(UPDATE_COLOR));
    fontIcon.setIconLiteral("mdi2a-arrow-up-thick");
    return fontIcon;
  }


  public static FontIcon createCheckIcon(@Nullable String color) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconLiteral("bi-check-circle");
    fontIcon.setIconColor(Paint.valueOf("#66FF66"));
    if (color != null) {
      fontIcon.setIconColor(Paint.valueOf(color));
    }
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
    fontIcon.setIconColor(Paint.valueOf(ERROR_COLOR));
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


  public static FontIcon createCheckboxIcon(@Nullable String color) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconLiteral("bi-check-circle");
    fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
    if (color != null) {
      fontIcon.setIconColor(Paint.valueOf(color));
    }
    return fontIcon;
  }

  public static FontIcon createExclamationIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#FF3333"));
    fontIcon.setIconLiteral("bi-exclamation-circle");
    return fontIcon;
  }

  public static FontIcon createUnsupportedIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#FF9933"));
    fontIcon.setIconLiteral("bi-x-circle");
    return fontIcon;
  }

  public static FontIcon createExclamationIcon(@Nullable String color) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconLiteral("bi-exclamation-circle");
    fontIcon.setIconColor(Paint.valueOf("#FF3333"));
    if (color != null) {
      fontIcon.setIconColor(Paint.valueOf(color));
    }
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

  public static Label createPlaylistIcon(Playlist playlist) {
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
    stage.getIcons().add(new Image(ServerFX.class.getResourceAsStream("logo-64.png")));
    return stage;
  }

  public static Stage createDialogStage(Class clazz, Stage owner, String title, String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    return createDialogStage(fxmlLoader, owner, title, null);
  }

  public static Stage createDialogStage(FXMLLoader fxmlLoader, Stage owner, String title) {
    return createDialogStage(fxmlLoader, owner, title, null);
  }

  public static Stage createDialogStage(FXMLLoader fxmlLoader, Stage owner, String title, String stateId) {
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

    if (stateId != null) {
      stage.setResizable(true);

      Rectangle position = LocalUISettings.getPosition(stateId);
      if (position != null) {
        stage.setX(position.getX());
        stage.setY(position.getY());

        stage.setWidth(position.getWidth());
        stage.setHeight(position.getHeight());
      }

      stage.setOnShowing(new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
          Platform.runLater(() -> {
            dialogHeaderController.enableStateListener(stage, controller, stateId);
          });
        }
      });
    }

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

  public static Optional<ButtonType> showConfirmationWithOption(Stage owner, String text, String help1, String help2, String btnText, String optionText) {
    Stage stage = createDialogStage(ConfirmationDialogWithOptionController.class, owner, "Confirmation", "dialog-confirmation-with-option.fxml");
    ConfirmationDialogWithOptionController controller = (ConfirmationDialogWithOptionController) stage.getUserData();
    controller.initDialog(stage, optionText, btnText, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
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

  public static ConfirmationResult showAlertOptionWithCheckbox(Stage owner, String msg, String altOptionText, String okText, String help1, String help2, String checkBoxText) {
    return showAlertOptionWithCheckbox(owner, msg, altOptionText, okText, help1, help2, checkBoxText, true);
  }

  public static ConfirmationResult showAlertOptionWithCheckbox(Stage owner, String msg, String altOptionText, String okText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage(ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-alert-option-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showConfirmationWithCheckbox(Stage owner, String msg, String okText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage(ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-confirmation-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, null, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showAlertOptionWithMandatoryCheckbox(Stage owner, String msg, String altOptionText, String okText, String help1, String help2, String checkBoxText) {
    Stage stage = createDialogStage(ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-alert-option-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2, checkBoxText);
    controller.setCheckboxMandatory();
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

  public static void createMediaContainer(VPinStudioClient client, BorderPane parent, GameMediaItemRepresentation mediaItem, boolean previewEnabled) {
    if (parent.getCenter() != null) {
      Node node = parent.getCenter();
      if (node instanceof AssetMediaPlayer) {
        ((AssetMediaPlayer) node).disposeMedia();
      }
    }

    if (mediaItem == null) {
      createNoMediaLabel(parent);
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

    if (previewEnabled && mediaItem != null) {
      addMediaItemToBorderPane(client, mediaItem, parent);
    }
  }

  public static void createNoMediaLabel(BorderPane parent) {
    Label label = new Label("No media found");
    label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
    parent.setCenter(label);
  }

  public static AssetMediaPlayer addMediaItemToBorderPane(VPinStudioClient client, GameMediaItemRepresentation mediaItem, BorderPane parent) {
    String mimeType = mediaItem.getMimeType();
    if (mimeType == null) {
      LOG.info("Failed to resolve mime type for " + mediaItem);
      return null;
    }

    boolean audioOnly = parent.getId().equalsIgnoreCase("screenAudioLaunch") || parent.getId().equalsIgnoreCase("screenAudio");
    String baseType = mimeType.split("/")[0];
    String url = client.getURL(mediaItem.getUri());

    double prefWidth = parent.getPrefWidth();
    if(prefWidth <= 0) {
      prefWidth = ((Pane)parent.getParent()).getWidth();
    }
    double prefHeight = parent.getPrefHeight();
    if(prefHeight <= 0) {
      prefHeight =  ((Pane)parent.getParent()).getHeight();
    }

    if (baseType.equals("image") && !audioOnly) {
      ImageView imageView = new ImageView();
      imageView.setFitWidth(prefWidth - 10);
      imageView.setFitHeight(prefHeight - 60);
      imageView.setPreserveRatio(true);

      ByteArrayInputStream gameMediaItem = client.getAssetService().getGameMediaItem(mediaItem.getGameId(), PopperScreen.valueOf(mediaItem.getScreen()));
      Image image = new Image(gameMediaItem);
      imageView.setImage(image);
      imageView.setUserData(mediaItem);

      parent.setCenter(imageView);
    }
    else if (baseType.equals("audio")) {
      new AudioMediaPlayer(parent, mediaItem, url);
    }
    else if (baseType.equals("video") && !audioOnly) {
      return new VideoMediaPlayer(parent, mediaItem, url, mimeType, false);
    }
    else {
      LOG.error("Invalid media mime type " + mimeType + " of asset used for popper media panel " + parent.getId());
    }

    return null;
  }

  public static class PlaylistBackgroundImageListCell extends ListCell<Playlist> {

    public PlaylistBackgroundImageListCell() {
    }

    protected void updateItem(Playlist item, boolean empty) {
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
