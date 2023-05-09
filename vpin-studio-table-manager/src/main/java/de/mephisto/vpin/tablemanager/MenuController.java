package de.mephisto.vpin.tablemanager;

import de.mephisto.vpin.commons.utils.FXUtil;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.tablemanager.states.StateMananger;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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

import static de.mephisto.vpin.tablemanager.UIDefaults.*;

public class MenuController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MenuController.class);


  @FXML
  private Node greenPanel;

  @FXML
  private Node bluePanel;

  @FXML
  private Node redPanel;

  @FXML
  private Node baseSelector;

  @FXML
  private HBox gameRow;

  @FXML
  private Label blueLabel;

  @FXML
  private Node loadMask;

  @FXML
  private Label greenLabel;

  @FXML
  private Label redLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Node progressStack;

  @FXML
  private ProgressBar progressBar;

  @FXML
  private Label progressLabel;

  @FXML
  private Node footer;

  @FXML
  private Node arrowRight;

  @FXML
  private Node arrowLeft;

  private boolean installToggle = true;
  private int selectionIndex = 0;
  private List<VpaDescriptorRepresentation> vpaDescriptors;
  private List<GameRepresentation> games;
  private List<?> activeModels;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void toggleInstall() {
    if (installToggle) {
      baseSelector.setStyle("-fx-background-color: #33CC00;");
      TransitionUtil.createOutFader(greenPanel).play();
      TransitionUtil.createInFader(bluePanel).play();
    }
    else {
      baseSelector.setStyle("-fx-background-color: #FF3333;");
      TransitionUtil.createOutFader(bluePanel).play();
      TransitionUtil.createInFader(greenPanel).play();
    }
    installToggle = !installToggle;
  }

  public boolean isInstallSelected() {
    return this.installToggle;
  }

  public void enterInstall() {
    StateMananger.getInstance().setInputBlocked(true);
    resetGameRow();
    greenLabel.setText("Install");
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(gameRow).play();
    TransitionUtil.createInFader(loadMask).play();
    TransitionUtil.createTranslateByYTransition(footer, FOOTER_ANIMATION_DURATION, FOOTER_HEIGHT).play();

    new Thread(() -> {
      vpaDescriptors = Menu.client.getVpaDescriptorsFiltered();
      activeModels = vpaDescriptors; //TODO mpf
      Platform.runLater(() -> {
        loadArchivedItems();
        initGameBarSelection();

        TransitionUtil.createOutFader(loadMask).play();
        StateMananger.getInstance().setInputBlocked(true, TransitionUtil.FADER_DEFAULT + 100);
      });
    }).start();
  }

  public void enterArchive() {
    StateMananger.getInstance().setInputBlocked(true);
    resetGameRow();
    blueLabel.setText("Archive Table");
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(gameRow).play();
    TransitionUtil.createInFader(loadMask).play();
    TransitionUtil.createTranslateByYTransition(footer, FOOTER_ANIMATION_DURATION, FOOTER_HEIGHT).play();

    setLoadLabel("Loading...");

    new Thread(() -> {
      games = Menu.client.getGames();
      activeModels = games; //TODO mpf
      Platform.runLater(() -> {
        loadGameItems();
        initGameBarSelection();

        TransitionUtil.createOutFader(loadMask).play();
        StateMananger.getInstance().setInputBlocked(true, TransitionUtil.FADER_DEFAULT + 100);
      });
    }).start();
  }

  private void enterMainWithInstall() {
    redLabel.setText("");
    greenLabel.setText("Install");
    blueLabel.setText("Archive");
    setLoadLabel("");
    resetGameRow();
    TransitionUtil.createOutFader(gameRow).play();
    TransitionUtil.createOutFader(redPanel).play();
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createInFader(greenPanel).play();
  }

  private void enterMainWithArchive() {
    redLabel.setText("");
    greenLabel.setText("Install");
    blueLabel.setText("Archive");
    setLoadLabel("");
    resetGameRow();
    TransitionUtil.createOutFader(gameRow).play();
    TransitionUtil.createOutFader(redPanel).play();
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(bluePanel).play();
  }

  public void resetFooter() {
    StateMananger.getInstance().setInputBlocked(true, FOOTER_ANIMATION_DURATION + 100);
    if (footer.getTranslateY() != 0) {
      TransitionUtil.createTranslateByYTransition(footer, FOOTER_ANIMATION_DURATION, -FOOTER_HEIGHT).play();
    }
  }

  public void enterMainMenu() {
    resetFooter();
    if (this.installToggle) {
      enterMainWithInstall();
    }
    else {
      enterMainWithArchive();
    }
  }

  public void enterTableInstallConfirmation() {
    greenLabel.setText("Install Table?");
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createInFader(greenPanel, 0.9, 100).play();
  }

  public void enterArchiveInstallConfirmation() {
    greenLabel.setText("Archive Table?");
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createInFader(greenPanel, 0.9, 100).play();
  }

  public void leaveConfirmation() {
    setLoadLabel("");
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createOutFader(loadMask).play();
  }

  public void enterArchiving() {
    greenLabel.setText("");
    blueLabel.setText("");
    setLoadLabel("Archiving Table...");
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(bluePanel, 0.9, 100).play();
    TransitionUtil.createInFader(loadMask).play();
  }


  public void enterInstalling() {
    greenLabel.setText("");
    blueLabel.setText("");
    setLoadLabel("Installing Table...");
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(bluePanel, 0.9, 100).play();
    TransitionUtil.createInFader(loadMask).play();
  }


  public void enterExitConfirmation() {
    blueLabel.setText("");
    greenLabel.setText("");
    redLabel.setText("Back To Popper?");
    TransitionUtil.createInFader(redPanel).play();
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createOutFader(loadMask).play();
  }

  public void setLoadLabel(String text) {
    Label label = FXUtil.findChildByID((Parent) loadMask, "loadLabel");
    label.setText(text);
  }

  public void scrollGameBarRight() {
    scroll(false);
  }

  public void scrollGameBarLeft() {
    scroll(true);
  }

  private void scroll(boolean left) {
    if (gameRow.getChildren().isEmpty()) {
      return;
    }

    int oldIndex = selectionIndex;
    if (left) {
      if (selectionIndex <= 0) {
        selectionIndex = 0;
        return;
      }
      selectionIndex--;
    }
    else {
      if (selectionIndex == (activeModels.size() - 1)) {
        return;
      }
      selectionIndex++;
    }

    final Node node = gameRow.getChildren().get(oldIndex);
    Transition t1 = TransitionUtil.createTranslateByXTransition(node, SELECTION_SCALE_DURATION, left ? UIDefaults.SCROLL_OFFSET : -UIDefaults.SCROLL_OFFSET);
    Transition t2 = TransitionUtil.createScaleTransition(node, UIDefaults.SELECTION_SCALE_DEFAULT, SELECTION_SCALE_DURATION);
    Transition t3 = TransitionUtil.createTranslateByYTransition(node, SELECTION_SCALE_DURATION, UIDefaults.SELECTION_HEIGHT_OFFSET);

    //scroll whole game row
    Transition t4 = TransitionUtil.createTranslateByXTransition(gameRow, SELECTION_SCALE_DURATION, left ? UIDefaults.THUMBNAIL_SIZE : -UIDefaults.THUMBNAIL_SIZE);

    final Node updatedNode = gameRow.getChildren().get(selectionIndex);
    Transition t5 = TransitionUtil.createTranslateByXTransition(updatedNode, SELECTION_SCALE_DURATION, left ? UIDefaults.SCROLL_OFFSET : -UIDefaults.SCROLL_OFFSET);
    Transition t6 = TransitionUtil.createScaleTransition(updatedNode, UIDefaults.SELECTION_SCALE, SELECTION_SCALE_DURATION);
    Transition t7 = TransitionUtil.createTranslateByYTransition(updatedNode, SELECTION_SCALE_DURATION, -UIDefaults.SELECTION_HEIGHT_OFFSET);

    ParallelTransition parallelTransition = new ParallelTransition(t1, t2, t3, t4, t5, t6, t7);
    parallelTransition.play();

    updateLabel(updatedNode);
  }

  private void updateLabel(Node node) {
    Object userData = node.getUserData();
    if (userData instanceof GameRepresentation) {
      nameLabel.setText(((GameRepresentation) userData).getGameDisplayName());
    }
    else {
      nameLabel.setText(((VpaDescriptorRepresentation) userData).getManifest().getGameDisplayName());
    }
  }

  /**
   * Centers the row start back to the center.
   */
  private void initGameBarSelection() {
    if (gameRow.getChildren().isEmpty()) {
      return;
    }

    Pane node = (Pane) gameRow.getChildren().get(0);
    int size = gameRow.getChildren().size() * UIDefaults.THUMBNAIL_SIZE;
    if (size < UIDefaults.SCREEN_WIDTH) {
      gameRow.setTranslateX(UIDefaults.SCREEN_WIDTH / 2 + UIDefaults.THUMBNAIL_SIZE + SCROLL_OFFSET);
    }
    else {
      gameRow.setTranslateX(size / 2);
    }

    BorderPane child = (BorderPane) gameRow.getChildren().get(selectionIndex);
    TransitionUtil.createTranslateByXTransition(child, SELECTION_SCALE_DURATION, -UIDefaults.SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(child, UIDefaults.SELECTION_SCALE, SELECTION_SCALE_DURATION).play();
    TransitionUtil.createTranslateByYTransition(node, SELECTION_SCALE_DURATION, -UIDefaults.SELECTION_HEIGHT_OFFSET).play();

    updateLabel(child);
  }

  public void resetGameRow() {
    gameRow.getChildren().removeAll(gameRow.getChildren());
  }

  private void loadArchivedItems() {
    gameRow.getChildren().clear();
    selectionIndex = 0;
    for (VpaDescriptorRepresentation vpaDescriptor : vpaDescriptors) {
      gameRow.getChildren().add(createItemFor(vpaDescriptor));
    }

    if (!vpaDescriptors.isEmpty()) {
      while (gameRow.getChildren().size() * UIDefaults.THUMBNAIL_SIZE < UIDefaults.SCREEN_WIDTH * 2) {
        Label label = new Label();
        label.setMinWidth(THUMBNAIL_SIZE);
        gameRow.getChildren().add(label);
      }
    }

  }

  private void loadGameItems() {
    gameRow.getChildren().clear();
    selectionIndex = 0;
    for (GameRepresentation game : games) {
      gameRow.getChildren().add(createItemFor(game));
    }

    if (!games.isEmpty()) {
      while (gameRow.getChildren().size() * UIDefaults.THUMBNAIL_SIZE < UIDefaults.SCREEN_WIDTH * 2) {
        Label label = new Label();
        label.setMinWidth(THUMBNAIL_SIZE);
        gameRow.getChildren().add(label);
      }
    }
  }

  private BorderPane createItemFor(Object o) {
    Image wheel = null;
    String text = null;
    if (o instanceof GameRepresentation) {
      GameRepresentation game = (GameRepresentation) o;
      GameMediaRepresentation gameMedia = game.getGameMedia();
      GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Wheel);
      if (item == null) {
        text = game.getGameDisplayName();
        wheel = new Image(Menu.class.getResourceAsStream("avatar-blank.png"));
      }
      else {
        ByteArrayInputStream gameMediaItem = Menu.client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
        wheel = new Image(gameMediaItem);
      }
    }
    else if (o instanceof VpaDescriptorRepresentation) {
      VpaDescriptorRepresentation vpaDescriptor = (VpaDescriptorRepresentation) o;
      //TODO
//      String icon = vpaDescriptor.getManifest().getIcon();
//      if (icon == null) {
//        text = vpaDescriptor.getManifest().getGameDisplayName();
//        wheel = new Image(Menu.class.getResourceAsStream("avatar-blank.png"));
//      }
//      else {
//        byte[] decode = Base64.getDecoder().decode(icon);
//        wheel = new Image(new ByteArrayInputStream(decode));
//      }
    }
    else {
      throw new UnsupportedOperationException("Invalid item");
    }

    return createItem(wheel, text, o);
  }

  private BorderPane createItem(Image image, String text, Object data) {
    BorderPane borderPane = new BorderPane();
    borderPane.setUserData(data);
    ImageView imageView = new ImageView();
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(UIDefaults.THUMBNAIL_SIZE);
    imageView.setFitHeight(UIDefaults.THUMBNAIL_SIZE);

    imageView.setImage(image);
    StackPane stackPane = new StackPane();
    stackPane.getChildren().add(imageView);

    if (text != null && text.length() > 16) {
      text = text.substring(0, 16) + "...";
    }
    Label label = new Label(text);
    label.setStyle("-fx-font-size: 22px;-fx-text-fill: #444444;");
    stackPane.getChildren().add(label);
    borderPane.setCenter(stackPane);
    borderPane.setCache(true);
    borderPane.setCacheHint(CacheHint.SCALE_AND_ROTATE);
    return borderPane;
  }

  public GameRepresentation getGameSelection() {
    Node node = gameRow.getChildren().get(selectionIndex);
    return (GameRepresentation) node.getUserData();
  }

  public VpaDescriptorRepresentation getVpaSelection() {
    Node node = gameRow.getChildren().get(selectionIndex);
    return (VpaDescriptorRepresentation) node.getUserData();
  }

  public void showProgressbar() {
    TransitionUtil.createTranslateByYTransition(progressStack, FOOTER_ANIMATION_DURATION, 70).play();
  }

  public void hideProgressbar() {
    this.progressBar.setProgress(1);
    TransitionUtil.createTranslateByYTransition(progressStack, FOOTER_ANIMATION_DURATION, -70).play();
  }

  public void setNameLabelText(String text) {
    nameLabel.setText(text);
  }

  public void setStatus(String status, double progress) {
    this.progressBar.setProgress(progress / 100);
    this.progressLabel.setText(status);
    LOG.info("Setting status: \"" + status + "\" / " + progress);
  }

  public void enterArchiveOptionsSelection() {
    arrowRight.setVisible(true);
    arrowLeft.setVisible(true);
    greenLabel.setText("Archive Options");
  }

  public void enterPlaylistSelection() {
    arrowRight.setVisible(true);
    arrowLeft.setVisible(true);
    greenLabel.setText("Select Playlist");
  }

  public void setArrowsVisible(boolean b) {
    arrowRight.setVisible(b);
    arrowLeft.setVisible(b);
  }

  public void leaveArchiveSubSelection() {
    setArrowsVisible(false);
    nameLabel.setText(this.getGameSelection().getGameDisplayName());
  }

  public void leaveInstallSubSelection() {
    setArrowsVisible(false);
    nameLabel.setText(this.getVpaSelection().getManifest().getGameDisplayName());
  }
}
