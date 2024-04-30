package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItemTypes;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItemsFactory;
import de.mephisto.vpin.commons.utils.FXUtil;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.fx.pausemenu.UIDefaults.*;

public class MenuController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MenuController.class);

  @FXML
  private Node bluePanel;

  @FXML
  private Node rootBorderPane;

  @FXML
  private HBox menuItemsRow;

  @FXML
  private Label blueLabel;

  @FXML
  private Node loadMask;

  @FXML
  private Label nameLabel;

  @FXML
  private Node footer;

  @FXML
  private ImageView screenImageView;

  @FXML
  private MediaView mediaView;

  @FXML
  private WebView webView;

  @FXML
  private BorderPane customView;

  private int selectionIndex = 0;

  private PopperScreen cardScreen;
  private PinUPPlayerDisplay tutorialScreen;
  private PauseMenuSettings pauseMenuSettings;
  private GameRepresentation game;
  private PauseMenuItem activeSelection;

  private final List<PauseMenuItem> pauseMenuItems = new ArrayList<>();

  private MenuCustomViewController customViewController;
  private Node currentSelection;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    webView.getEngine().setUserStyleSheetLocation(PauseMenu.class.getResource("web-style.css").toString());

    try {
      String resource = "menu-custom-view.fxml";
      FXMLLoader loader = new FXMLLoader(MenuCustomViewController.class.getResource(resource));
      Parent widgetRoot = loader.load();
      customView.setCenter(widgetRoot);
      customViewController = loader.getController();
    } catch (IOException e) {
      LOG.error("Failed to init custom controller: " + e.getMessage(), e);
    }
  }

  public PauseMenuSettings getPauseMenuSettings() {
    return pauseMenuSettings;
  }

  public void setGame(@NonNull GameRepresentation game, GameStatus gameStatus, @Nullable PopperScreen cardScreen, @Nullable PinUPPlayerDisplay tutorialScreen, PauseMenuSettings pauseMenuSettings) {
    this.game = game;
    this.cardScreen = cardScreen;
    this.tutorialScreen = tutorialScreen;
    this.pauseMenuSettings = pauseMenuSettings;
    this.customViewController.setGame(game, gameStatus);
    enterMenuItemSelection();
  }

  public void setVisible(boolean b) {
    this.rootBorderPane.setVisible(b);
  }

  private void enterMenuItemSelection() {
    resetGameRow();
    blueLabel.setText("Loading...");
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createInFader(menuItemsRow).play();
    TransitionUtil.createInFader(loadMask).play();
    footer.setTranslateY(310);
    setLoadLabel("Loading...");

    Platform.runLater(() -> {
      loadMenuItems();
      initGameBarSelection();

      TransitionUtil.createOutFader(loadMask).play();
    });
  }

  public void setLoadLabel(String text) {
    Label label = FXUtil.findChildByID((Parent) loadMask, "loadLabel");
    label.setText(text);
  }

  public void scrollGameBarRight() {
    resetBrowser();
    scroll(false);
  }

  public void scrollGameBarLeft() {
    resetBrowser();
    scroll(true);
  }

  public boolean isAtEnd() {
    return selectionIndex == (pauseMenuItems.size() - 1);
  }

  public boolean isAtStart() {
    return selectionIndex == 0;
  }

  private void scroll(boolean left) {
    if (menuItemsRow.getChildren().isEmpty()) {
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
      if (selectionIndex == (pauseMenuItems.size() - 1)) {
        return;
      }
      selectionIndex++;
    }

    final Node node = menuItemsRow.getChildren().get(oldIndex);
    Transition t1 = TransitionUtil.createTranslateByXTransition(node, SELECTION_SCALE_DURATION, left ? UIDefaults.SCROLL_OFFSET : -UIDefaults.SCROLL_OFFSET);
    Transition t2 = TransitionUtil.createScaleTransition(node, UIDefaults.SELECTION_SCALE_DEFAULT, SELECTION_SCALE_DURATION);
    Transition t3 = TransitionUtil.createTranslateByYTransition(node, SELECTION_SCALE_DURATION, UIDefaults.SELECTION_HEIGHT_OFFSET);

    //scroll whole game row
    Transition t4 = TransitionUtil.createTranslateByXTransition(menuItemsRow, SELECTION_SCALE_DURATION, left ? UIDefaults.THUMBNAIL_SIZE : -UIDefaults.THUMBNAIL_SIZE);

    Node oldSelection = currentSelection;
    currentSelection = menuItemsRow.getChildren().get(selectionIndex);
    Transition t5 = TransitionUtil.createTranslateByXTransition(currentSelection, SELECTION_SCALE_DURATION, left ? UIDefaults.SCROLL_OFFSET : -UIDefaults.SCROLL_OFFSET);
    Transition t6 = TransitionUtil.createScaleTransition(currentSelection, UIDefaults.SELECTION_SCALE, SELECTION_SCALE_DURATION);
    Transition t7 = TransitionUtil.createTranslateByYTransition(currentSelection, SELECTION_SCALE_DURATION, -UIDefaults.SELECTION_HEIGHT_OFFSET);

    ParallelTransition parallelTransition = new ParallelTransition(t1, t2, t3, t4, t5, t6, t7);
    parallelTransition.play();

    updateSelection(oldSelection, currentSelection);
  }

  private void updateSelection(Node oldNode, Node node) {
    if (oldNode != null) {
      PauseMenuItem oldSelection = (PauseMenuItem) node.getUserData();
      if (activeSelection.getYouTubeUrl() != null) {
        webView.setVisible(true);
        WebEngine engine = webView.getEngine();
        engine.loadContent("");
      }
      else if (activeSelection.getVideoUrl() != null) {
        try {
          mediaView.getMediaPlayer().stop();
          mediaView.getMediaPlayer().dispose();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }

    activeSelection = (PauseMenuItem) node.getUserData();
    nameLabel.setText(activeSelection.getDescription());
    screenImageView.setVisible(false);
    mediaView.setVisible(false);
    webView.setVisible(false);
    customView.setVisible(false);

    if (activeSelection.getItemType().equals(PauseMenuItemTypes.exit)) {
      customView.setVisible(true);
    }
    else if (activeSelection.getVideoUrl() != null) {
      mediaView.setVisible(true);

      Media media = new Media(activeSelection.getVideoUrl());
      MediaPlayer mediaPlayer = new MediaPlayer(media);
      mediaPlayer.setAutoPlay(true);
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(false);
      mediaView.setMediaPlayer(mediaPlayer);
    }
    else if (activeSelection.getYouTubeUrl() != null) {
//      if (!pauseMenuSettings.isUseInternalBrowser()) {
      screenImageView.setVisible(true);
      screenImageView.setImage(activeSelection.getDataImage());
//      }
//      else {
//        webView.setVisible(true);
//        WebEngine engine = webView.getEngine();
//        engine.load(activeSelection.getYouTubeUrl());
//      }

      LOG.info("Loading YT video: " + activeSelection.getYouTubeUrl());
    }
    else if (activeSelection.getDataImage() != null) {
      screenImageView.setVisible(true);
      screenImageView.setImage(activeSelection.getDataImage());
    }
  }

  public void reset() {
    this.resetBrowser();
    this.screenImageView.setImage(null);
    this.mediaView.setMediaPlayer(null);
    this.mediaView.setVisible(false);
    this.webView.setVisible(false);
    this.webView.getEngine().load(null);
    LOG.info("Reset pause menu media items.");
  }

  public PauseMenuItem getSelection() {
    return activeSelection;
  }

  /**
   * Centers the row start back to the center.
   */
  private void initGameBarSelection() {
    if (menuItemsRow.getChildren().isEmpty()) {
      return;
    }

    Pane node = (Pane) menuItemsRow.getChildren().get(0);
    int size = menuItemsRow.getChildren().size() * UIDefaults.THUMBNAIL_SIZE;
    if (size < UIDefaults.SCREEN_WIDTH) {
      menuItemsRow.setTranslateX(UIDefaults.SCREEN_WIDTH / 2 + UIDefaults.THUMBNAIL_SIZE + SCROLL_OFFSET);
    }
    else {
      menuItemsRow.setTranslateX(size / 2);
    }

    BorderPane child = (BorderPane) menuItemsRow.getChildren().get(selectionIndex);
    TransitionUtil.createTranslateByXTransition(child, SELECTION_SCALE_DURATION, -UIDefaults.SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(child, UIDefaults.SELECTION_SCALE, SELECTION_SCALE_DURATION).play();
    TransitionUtil.createTranslateByYTransition(node, SELECTION_SCALE_DURATION, -UIDefaults.SELECTION_HEIGHT_OFFSET).play();

    updateSelection(null, child);
  }

  public void resetGameRow() {
    menuItemsRow.getChildren().removeAll(menuItemsRow.getChildren());
  }

  private void loadMenuItems() {
    pauseMenuItems.clear();
    pauseMenuItems.addAll(PauseMenuItemsFactory.createPauseMenuItems(game, pauseMenuSettings, cardScreen));

    menuItemsRow.getChildren().clear();
    selectionIndex = 0;
    for (PauseMenuItem pItem : pauseMenuItems) {
      menuItemsRow.getChildren().add(createItemFor(pItem));
    }

    while (menuItemsRow.getChildren().size() * UIDefaults.THUMBNAIL_SIZE < UIDefaults.SCREEN_WIDTH * 2) {
      Label label = new Label();
      label.setMinWidth(THUMBNAIL_SIZE);
      menuItemsRow.getChildren().add(label);
    }
  }

  private BorderPane createItemFor(PauseMenuItem menuItem) {
    Image wheel = menuItem.getImage();
    if (wheel == null) {
      wheel = new Image(PauseMenu.class.getResourceAsStream("avatar-blank.png"));
    }
    String text = menuItem.getName();
    return createItem(wheel, text, menuItem);
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
//    stackPane.getChildren().add(label);
    borderPane.setCenter(stackPane);
    borderPane.setCache(true);
    borderPane.setCacheHint(CacheHint.SCALE_AND_ROTATE);
    return borderPane;
  }

  public void showYouTubeVideo(PauseMenuItem item) {
    if (pauseMenuSettings != null) {
      ChromeLauncher.showYouTubeVideo(tutorialScreen, item.getYouTubeUrl(), item.getName());
    }
  }

  public void resetBrowser() {
    ChromeLauncher.exitBrowser();
  }
}
