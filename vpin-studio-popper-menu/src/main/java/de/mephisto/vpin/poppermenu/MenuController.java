package de.mephisto.vpin.poppermenu;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MenuController.class);
  public static final int THUMBNAIL_SIZE = 440;
  public static final int SCROLL_OFFSET = 220;

  @FXML
  private Node greenPanel;

  @FXML
  private Node redPanel;

  @FXML
  private Node baseSelector;

  @FXML
  private HBox gameRow;

  @FXML
  private Label negativLabel;

  @FXML
  private Node loadMask;

  @FXML
  private Label positivLabel;

  private boolean installToggle = true;
  private int selectionIndex = 0;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void toggleInstall() {
    if (installToggle) {
      baseSelector.setStyle("-fx-background-color: #33CC00;");
      TransitionUtil.createOutFader(greenPanel).play();
      TransitionUtil.createInFader(redPanel).play();
    }
    else {
      baseSelector.setStyle("-fx-background-color: #FF3333;");
      TransitionUtil.createOutFader(redPanel).play();
      TransitionUtil.createInFader(greenPanel).play();
    }
    installToggle = !installToggle;
  }

  public boolean isInstallSelected() {
    return this.installToggle;
  }

  public void enterInstall() {
    resetGameRow();
    positivLabel.setText("Install Table");
    TransitionUtil.createOutFader(redPanel).play();
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(gameRow).play();


    List<VpaDescriptorRepresentation> vpaDescriptors = Menu.client.getVpaDescriptors();
    loadArchivedItems(vpaDescriptors);
    initGameBarSelection();
  }

  public void enterArchive() {
    resetGameRow();
    negativLabel.setText("Archive Table");
    TransitionUtil.createOutFader(redPanel).play();
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(gameRow).play();
    TransitionUtil.createInFader(loadMask).play();

    new Thread(() -> {
      List<GameRepresentation> games = Menu.client.getGames();
      Platform.runLater(() -> {
        loadGameItems(games);
        initGameBarSelection();

        TransitionUtil.createOutFader(loadMask).play();
      });
    }).start();
  }

  public void enterMainWithInstall() {
    positivLabel.setText("Install Table");
    negativLabel.setText("Archive Table");
    resetGameRow();
    TransitionUtil.createOutFader(gameRow).play();
    TransitionUtil.createOutFader(redPanel).play();
    TransitionUtil.createInFader(greenPanel).play();
  }

  public void enterMainWithArchive() {
    positivLabel.setText("Install Table");
    negativLabel.setText("Archive Table");
    resetGameRow();
    TransitionUtil.createOutFader(gameRow).play();
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(redPanel).play();
  }

  public void enterTableInstallConfirmation() {
    positivLabel.setText("Install Table?");
//    TransitionUtil.createOutFader(gameRow).play();
    TransitionUtil.createOutFader(redPanel).play();
    TransitionUtil.createInFader(greenPanel, 0.9, 100).play();
  }

  public void enterArchiveInstallConfirmation() {
    positivLabel.setText("Archive Table?");
//    TransitionUtil.createOutFader(gameRow).play();
    TransitionUtil.createOutFader(redPanel).play();
    TransitionUtil.createInFader(greenPanel, 0.9, 100).play();
  }

  public void leaveConfirmation() {
    TransitionUtil.createOutFader(redPanel).play();
    TransitionUtil.createOutFader(greenPanel).play();
  }

  public void scrollGameBarRight() {
    scroll(false);
  }

  public void scrollGameBarLeft() {
    scroll(true);
  }

  private void scroll(boolean left) {
    int oldIndex = selectionIndex;
    if (left) {
      if (selectionIndex <= 0) {
        selectionIndex = 0;
        return;
      }
      selectionIndex--;
    }
    else {
      if (selectionIndex == (gameRow.getChildren().size() - 1)) {
        return;
      }
      selectionIndex++;
    }

    Node node = gameRow.getChildren().get(oldIndex);
    TransitionUtil.createTranslateByXTransition(node, 60, left ? SCROLL_OFFSET : -SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(node, UIDefaults.SELECTION_SCALE_DEFAULT, 100).play();
    TransitionUtil.createTranslateByYTransition(node, 60, UIDefaults.SELECTION_HEIGHT_OFFSET).play();

    TransitionUtil.createTranslateByXTransition(gameRow, 60, left ? THUMBNAIL_SIZE : -THUMBNAIL_SIZE).play();

    node = gameRow.getChildren().get(selectionIndex);
    TransitionUtil.createTranslateByXTransition(node, 60, left ? SCROLL_OFFSET : -SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(node, UIDefaults.SELECTION_SCALE, 100).play();
    TransitionUtil.createTranslateByYTransition(node, 60, -UIDefaults.SELECTION_HEIGHT_OFFSET).play();
    System.out.println("Selected "  + node.getUserData());
  }

  private void initGameBarSelection() {
    Pane node = (Pane) gameRow.getChildren().get(0);
    int size = gameRow.getChildren().size() * THUMBNAIL_SIZE;
    if(size < UIDefaults.SCREEN_WIDTH) {
      gameRow.setTranslateX(UIDefaults.SCREEN_WIDTH / 2);
    }
    else {
      gameRow.setTranslateX(size/2);
    }

    BorderPane child = (BorderPane) gameRow.getChildren().get(selectionIndex);
    TransitionUtil.createTranslateByXTransition(child, 60, -SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(child, UIDefaults.SELECTION_SCALE, 100).play();
    TransitionUtil.createTranslateByYTransition(node, 60, -UIDefaults.SELECTION_HEIGHT_OFFSET).play();
  }

  public void resetGameRow() {
    gameRow.getChildren().removeAll(gameRow.getChildren());
    gameRow.setTranslateX(0);
  }

  private void loadArchivedItems(List<VpaDescriptorRepresentation> vpaDescriptors) {
    gameRow.getChildren().clear();
    selectionIndex = 0;
    for (VpaDescriptorRepresentation vpaDescriptor : vpaDescriptors) {
      String icon = vpaDescriptor.getManifest().getIcon();
      Image wheel = null;
      if (icon == null) {
        wheel = new Image(Menu.class.getResourceAsStream("avatar-blank.png"));
      }
      else {
        byte[] decode = Base64.getDecoder().decode(icon);
        wheel = new Image(new ByteArrayInputStream(decode));
      }
      gameRow.getChildren().add(createItem(wheel, vpaDescriptor, null));
    }
  }

  private void loadGameItems(List<GameRepresentation> games) {
    gameRow.getChildren().clear();
    selectionIndex = 0;

    for (GameRepresentation game : games) {
      GameMediaRepresentation gameMedia = game.getGameMedia();
      GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Wheel);
      Image wheel = null;
      String text= null;
      if (item != null) {

        ByteArrayInputStream gameMediaItem = Menu.client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
        wheel = new Image(gameMediaItem);
      }
      else {
        text = game.getGameDisplayName();
        wheel = new Image(Menu.class.getResourceAsStream("avatar-blank.png"));
      }
      gameRow.getChildren().add(createItem(wheel, game, text));
    }
  }

  private BorderPane createItem(Image image, Object data, String text) {
    BorderPane borderPane = new BorderPane();
    borderPane.setUserData(data);
    ImageView imageView = new ImageView();
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(THUMBNAIL_SIZE);
    imageView.setFitHeight(THUMBNAIL_SIZE);

    imageView.setImage(image);
    StackPane stackPane = new StackPane();
    stackPane.getChildren().add(imageView);

    if(text != null && text.length() > 16) {
      text = text.substring(0, 16) +  "...";
    }
    Label label = new Label(text);
    label.setStyle("-fx-font-size: 36px;-fx-text-fill: #444444;");
    stackPane.getChildren().add(label);
    borderPane.setCenter(stackPane);
    return borderPane;
  }
}
