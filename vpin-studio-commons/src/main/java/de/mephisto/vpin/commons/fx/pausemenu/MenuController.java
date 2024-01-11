package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.fx.pausemenu.states.StateMananger;
import de.mephisto.vpin.commons.utils.FXUtil;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
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
  private Node baseSelector;

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

  private int selectionIndex = 0;

  private PopperScreen cardScreen;
  private PopperScreen infoCardScreen;
  private GameRepresentation game;

  private List<PauseMenuItem> pauseMenuItems = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setGame(GameRepresentation game, PopperScreen cardScreen, PopperScreen infoCardScreen) {
    this.game = game;
    this.cardScreen = cardScreen;
    this.infoCardScreen = infoCardScreen;
  }

  public void enterMenuItemSelection() {
    StateMananger.getInstance().setInputBlocked(true);
    resetGameRow();
    blueLabel.setText("Archive Table");
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createInFader(menuItemsRow).play();
    TransitionUtil.createInFader(loadMask).play();
    TransitionUtil.createTranslateByYTransition(footer, FOOTER_ANIMATION_DURATION, FOOTER_HEIGHT).play();

    setLoadLabel("Loading...");

    Platform.runLater(() -> {
      loadMenuItems();
      initGameBarSelection();

      TransitionUtil.createOutFader(loadMask).play();
      StateMananger.getInstance().setInputBlocked(true, TransitionUtil.FADER_DEFAULT + 100);
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

    final Node updatedNode = menuItemsRow.getChildren().get(selectionIndex);
    Transition t5 = TransitionUtil.createTranslateByXTransition(updatedNode, SELECTION_SCALE_DURATION, left ? UIDefaults.SCROLL_OFFSET : -UIDefaults.SCROLL_OFFSET);
    Transition t6 = TransitionUtil.createScaleTransition(updatedNode, UIDefaults.SELECTION_SCALE, SELECTION_SCALE_DURATION);
    Transition t7 = TransitionUtil.createTranslateByYTransition(updatedNode, SELECTION_SCALE_DURATION, -UIDefaults.SELECTION_HEIGHT_OFFSET);

    ParallelTransition parallelTransition = new ParallelTransition(t1, t2, t3, t4, t5, t6, t7);
    parallelTransition.play();

    updateSelection(updatedNode);
  }

  private void updateSelection(Node node) {
    PauseMenuItem menuItem = (PauseMenuItem) node.getUserData();
    nameLabel.setText(menuItem.getDescription());
    System.out.println("Selected " + menuItem.getName());
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

    updateSelection(child);
  }

  public void resetGameRow() {
    menuItemsRow.getChildren().removeAll(menuItemsRow.getChildren());
  }

  private void loadMenuItems() {
    pauseMenuItems.clear();

    InputStream gameMediaItem = PauseMenu.client.getGameMediaItem(game.getId(), cardScreen);
    Image scoreImage = new Image(gameMediaItem);
    screenImageView.setImage(scoreImage);

    PauseMenuItem item = new PauseMenuItem("Exit", "Return to Game Selection", new Image(PauseMenu.class.getResourceAsStream("exit.png")));
    pauseMenuItems.add(item);
    item = new PauseMenuItem("Highscores", "Highscore Card", new Image(PauseMenu.class.getResourceAsStream("highscores.png")));
    pauseMenuItems.add(item);
    item = new PauseMenuItem("Instructions", "Instruction Card or Media", new Image(PauseMenu.class.getResourceAsStream("infocard.png")));
    pauseMenuItems.add(item);
    item = new PauseMenuItem("Rules", "Table Rules", new Image(PauseMenu.class.getResourceAsStream("rules.png")));
    pauseMenuItems.add(item);

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
}
