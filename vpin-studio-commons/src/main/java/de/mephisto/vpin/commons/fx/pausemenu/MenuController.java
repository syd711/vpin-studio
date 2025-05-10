package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItemTypes;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItemsFactory;
import de.mephisto.vpin.commons.fx.pausemenu.states.StateMananger;
import de.mephisto.vpin.commons.utils.FXUtil;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults.*;

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

  @FXML
  private BorderPane scoreView;

  private int selectionIndex = 0;

  private GameStatus gameStatus;
  private VpsTable vpsTable;
  private VPinScreen cardScreen;
  private FrontendPlayerDisplay tutorialScreen;
  private PauseMenuSettings pauseMenuSettings;
  private GameRepresentation game;
  private FrontendMediaRepresentation frontendMedia;
  private PauseMenuItem activeSelection;

  private final List<PauseMenuItem> pauseMenuItems = new ArrayList<>();

  private MenuCustomViewController customViewController;
  private Node currentSelection;

  public void setGame(@NonNull GameRepresentation game,
                      @NonNull FrontendMediaRepresentation frontendMedia,
                      GameStatus gameStatus,
                      VpsTable vpsTable,
                      @Nullable VPinScreen cardScreen,
                      @Nullable FrontendPlayerDisplay tutorialScreen,
                      @NonNull PauseMenuSettings pauseMenuSettings) {
    this.game = game;
    this.frontendMedia = frontendMedia;
    this.gameStatus = gameStatus;
    this.vpsTable = vpsTable;
    this.cardScreen = cardScreen;
    this.tutorialScreen = tutorialScreen;
    this.pauseMenuSettings = pauseMenuSettings;
    this.customViewController.setGame(game, frontendMedia, gameStatus, vpsTable);
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
    scroll(false);
  }

  public void scrollGameBarLeft() {
    scroll(true);
  }

  public boolean isAtEnd() {
    return selectionIndex == (pauseMenuItems.size() - 1);
  }

  public boolean isAtStart() {
    return selectionIndex == 0;
  }

  private synchronized void scroll(boolean left) {
    if (menuItemsRow.getChildren().isEmpty() || pauseMenuItems.size() == 1) {
      return;
    }

    int duration = SELECTION_SCALE_DURATION;
    int oldIndex = selectionIndex;
    int steps = 1;
    if (left) {
      if (selectionIndex <= 0) {
        selectionIndex = pauseMenuItems.size() - 1;
        duration = duration / pauseMenuItems.size();
        steps = pauseMenuItems.size() - 1;
        left = false;
      }
      else {
        selectionIndex--;
      }
    }
    else {
      if (selectionIndex == (pauseMenuItems.size() - 1)) {
        selectionIndex = 0;
        duration = duration / pauseMenuItems.size();
        steps = pauseMenuItems.size() - 1;
        left = true;
      }
      else {
        selectionIndex++;
      }
    }

    animateMenuSteps(left, oldIndex, steps, duration);
  }

  private AtomicBoolean animating = new AtomicBoolean(false);

  private void animateMenuSteps(boolean left, final int oldIndex, final int steps, int duration) {
    if (animating.get()) {
      return;
    }
    animating.set(true);

    final Node node = menuItemsRow.getChildren().get(oldIndex);
    Transition t1 = TransitionUtil.createTranslateByXTransition(node, duration, left ? PauseMenuUIDefaults.SCROLL_OFFSET : -PauseMenuUIDefaults.SCROLL_OFFSET);
    Transition t2 = TransitionUtil.createScaleTransition(node, PauseMenuUIDefaults.SELECTION_SCALE_DEFAULT, duration);
    Transition t3 = TransitionUtil.createTranslateByYTransition(node, duration, PauseMenuUIDefaults.SELECTION_HEIGHT_OFFSET);

    //scroll whole game row
    Transition t4 = TransitionUtil.createTranslateByXTransition(menuItemsRow, duration, left ? PauseMenuUIDefaults.THUMBNAIL_SIZE : -PauseMenuUIDefaults.THUMBNAIL_SIZE);

    Node oldSelection = currentSelection;
    currentSelection = null;
    if (left) {
      currentSelection = menuItemsRow.getChildren().get(oldIndex - 1);
    }
    else {
      currentSelection = menuItemsRow.getChildren().get(oldIndex + 1);
    }
    Transition t5 = TransitionUtil.createTranslateByXTransition(currentSelection, duration, left ? PauseMenuUIDefaults.SCROLL_OFFSET : -PauseMenuUIDefaults.SCROLL_OFFSET);
    Transition t6 = TransitionUtil.createScaleTransition(currentSelection, PauseMenuUIDefaults.SELECTION_SCALE, duration);
    Transition t7 = TransitionUtil.createTranslateByYTransition(currentSelection, duration, -PauseMenuUIDefaults.SELECTION_HEIGHT_OFFSET);

    ParallelTransition parallelTransition = new ParallelTransition(t1, t2, t3, t4, t5, t6, t7);
    parallelTransition.onFinishedProperty().set(event -> {
      animating.set(false);
      int updatedSteps = steps - 1;
      int updatedOldIndex = left ? oldIndex - 1 : oldIndex + 1;
      if (updatedSteps > 0) {
        animateMenuSteps(left, updatedOldIndex, updatedSteps, duration);
        return;
      }
      updateSelection(oldSelection, currentSelection);
      System.out.println("ending");
      animating.set(false);
    });
    System.out.println("Starting");
    parallelTransition.play();
  }

  private void updateSelection(Node oldNode, Node node) {
    if (oldNode != null) {
      PauseMenuItem oldSelection = (PauseMenuItem) node.getUserData();
      if (activeSelection.getVideoUrl() != null && mediaView != null && mediaView.getMediaPlayer() != null) {
        try {
          mediaView.getMediaPlayer().stop();
          mediaView.getMediaPlayer().dispose();
        }
        catch (Exception e) {
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
    scoreView.setVisible(false);

    if (activeSelection.getItemType().equals(PauseMenuItemTypes.exit)) {
      customView.setVisible(true);
    }
    else if (activeSelection.getItemType().equals(PauseMenuItemTypes.iScored)) {
      try {
        Image sectionImage = new Image(PauseMenu.class.getResourceAsStream("iScored-wheel.png"));
        String resource = "menu-score-view.fxml";
        FXMLLoader loader = new FXMLLoader(MenuScoreViewController.class.getResource(resource));
        Pane widgetRoot = loader.load();
        MenuScoreViewController customViewController = loader.getController();
        customViewController.setData(game, gameStatus, vpsTable, activeSelection, sectionImage);
//        return widgetRoot;
        scoreView.setCenter(widgetRoot);
        scoreView.setVisible(true);
      }
      catch (IOException e) {
        LOG.error("Failed to init pause component: " + e.getMessage(), e);
      }
    }
    else if (activeSelection.getItemType().equals(PauseMenuItemTypes.maniaScores)) {
      try {
        Image sectionImage = new Image(PauseMenu.class.getResourceAsStream("mania-wheel.png"));
        String resource = "menu-score-view.fxml";
        FXMLLoader loader = new FXMLLoader(MenuScoreViewController.class.getResource(resource));
        Pane widgetRoot = loader.load();
        MenuScoreViewController customViewController = loader.getController();
        customViewController.setData(game, gameStatus, vpsTable, activeSelection, sectionImage);
//        return widgetRoot;
        scoreView.setCenter(widgetRoot);
        scoreView.setVisible(true);
      }
      catch (IOException e) {
        LOG.error("Failed to init pause component: " + e.getMessage(), e);
      }
    }
    else if (activeSelection.getVideoUrl() != null) {
      mediaView.setVisible(true);

      if (StateMananger.getInstance().isRunning()) {
        Media media = new Media(activeSelection.getVideoUrl());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setCycleCount(-1);
        mediaPlayer.setMute(false);
        mediaView.setMediaPlayer(mediaPlayer);
        LOG.info("Started streaming of {}", activeSelection.getVideoUrl());
      }
    }
    else if (activeSelection.getDataImage() != null) {
      screenImageView.setVisible(true);
      screenImageView.setImage(activeSelection.getDataImage());
    }

    StateMananger.getInstance().checkAutoPlay();
  }

  public void reset() {
    LOG.info("Resetting pause menu media items.");
    this.screenImageView.setImage(null);

    try {
      if (mediaView != null && mediaView.getMediaPlayer() != null) {
        LOG.info("Stopping active pause menu media player.");
        mediaView.getMediaPlayer().stop();
        mediaView.getMediaPlayer().dispose();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to dispose pause menu media: " + e.getMessage());
    }


    Platform.runLater(() -> {
      try {
        Thread.sleep(SELECTION_SCALE_DURATION * 2);
      }
      catch (InterruptedException e) {
        //
      }
      try {
        if (mediaView != null && mediaView.getMediaPlayer() != null) {
          LOG.info("Stopping active pause menu media player.");
          mediaView.getMediaPlayer().stop();
          mediaView.getMediaPlayer().dispose();
        }
      }
      catch (Exception e) {
        LOG.error("Failed to dispose pause menu media: " + e.getMessage());
      }

      this.mediaView.setMediaPlayer(null);
      this.mediaView.setVisible(false);
    });

    this.webView.setVisible(false);
    this.webView.getEngine().load(null);
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
    int size = menuItemsRow.getChildren().size() * PauseMenuUIDefaults.THUMBNAIL_SIZE;
    if (size < PauseMenuUIDefaults.SCREEN_WIDTH) {
      menuItemsRow.setTranslateX(PauseMenuUIDefaults.SCREEN_WIDTH / 2 + PauseMenuUIDefaults.THUMBNAIL_SIZE + SCROLL_OFFSET);
    }
    else {
      menuItemsRow.setTranslateX(size / 2);
    }

    BorderPane child = (BorderPane) menuItemsRow.getChildren().get(selectionIndex);
    TransitionUtil.createTranslateByXTransition(child, SELECTION_SCALE_DURATION, -PauseMenuUIDefaults.SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(child, PauseMenuUIDefaults.SELECTION_SCALE, SELECTION_SCALE_DURATION).play();
    TransitionUtil.createTranslateByYTransition(node, SELECTION_SCALE_DURATION, -PauseMenuUIDefaults.SELECTION_HEIGHT_OFFSET).play();

    updateSelection(null, child);
  }

  public void resetGameRow() {
    menuItemsRow.getChildren().removeAll(menuItemsRow.getChildren());
  }

  private void loadMenuItems() {
    pauseMenuItems.clear();
    pauseMenuItems.addAll(PauseMenuItemsFactory.createPauseMenuItems(game, pauseMenuSettings, cardScreen, frontendMedia));

    menuItemsRow.getChildren().clear();
    selectionIndex = 0;
    for (PauseMenuItem pItem : pauseMenuItems) {
      menuItemsRow.getChildren().add(PauseMenuItemComponentFactory.createMenuItemFor(pItem));
    }

    while (menuItemsRow.getChildren().size() * PauseMenuUIDefaults.THUMBNAIL_SIZE < PauseMenuUIDefaults.SCREEN_WIDTH * 2) {
      Label label = new Label();
      label.setMinWidth(THUMBNAIL_SIZE);
      menuItemsRow.getChildren().add(label);
    }
  }

  public boolean isVisible() {
    return PauseMenu.visible;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    webView.getEngine().setUserStyleSheetLocation(PauseMenu.class.getResource("web-style.css").toString());

    try {
      String resource = "menu-custom-view.fxml";
      FXMLLoader loader = new FXMLLoader(MenuCustomViewController.class.getResource(resource));
      Parent widgetRoot = loader.load();
      customView.setCenter(widgetRoot);
      customViewController = loader.getController();
    }
    catch (IOException e) {
      LOG.error("Failed to init custom controller: " + e.getMessage(), e);
    }
  }
}
