package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.fx.pausemenu.states.StateMananger;
import de.mephisto.vpin.commons.utils.FXUtil;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.fx.pausemenu.UIDefaults.*;

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
  private HBox menuItemsRow;

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
  private Node footer;

  @FXML
  private Node arrowRight;

  @FXML
  private Node arrowLeft;

  private int selectionIndex = 0;
  private List<ArchiveDescriptorRepresentation> archiveDescriptors;
  private List<PauseMenuItem> menuItems;
  private List<?> activeModels;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void toggleInstall() {
    baseSelector.setStyle("-fx-background-color: #33CC00;");
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(bluePanel).play();
  }

  public void enterMenuItemSelection() {
    StateMananger.getInstance().setInputBlocked(true);
    resetGameRow();
    blueLabel.setText("Archive Table");
    TransitionUtil.createOutFader(bluePanel).play();
    TransitionUtil.createOutFader(greenPanel).play();
    TransitionUtil.createInFader(menuItemsRow).play();
    TransitionUtil.createInFader(loadMask).play();
    TransitionUtil.createTranslateByYTransition(footer, FOOTER_ANIMATION_DURATION, FOOTER_HEIGHT).play();

    setLoadLabel("Loading...");

    menuItems = new ArrayList<>();
    PauseMenuItem item = new PauseMenuItem("Exit");
    menuItems.add(item);
    item = new PauseMenuItem("Highscores");
    menuItems.add(item);
    item = new PauseMenuItem("Instructions");
    menuItems.add(item);
    item = new PauseMenuItem("Statistics");
    menuItems.add(item);

    activeModels = menuItems; //TODO mpf
    Platform.runLater(() -> {
      loadMenuItems();
      initGameBarSelection();

      TransitionUtil.createOutFader(loadMask).play();
      StateMananger.getInstance().setInputBlocked(true, TransitionUtil.FADER_DEFAULT + 100);
    });
  }

  private void enterMainWithArchive() {
    redLabel.setText("");
    greenLabel.setText("Restore");
    blueLabel.setText("Archive");
    setLoadLabel("");
    resetGameRow();
    TransitionUtil.createOutFader(menuItemsRow).play();
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
    enterMainWithArchive();
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
      if (selectionIndex == (activeModels.size() - 1)) {
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

    updateLabel(updatedNode);
  }

  private void updateLabel(Node node) {
    PauseMenuItem menuItem = (PauseMenuItem) node.getUserData();
    nameLabel.setText(menuItem.getName());
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

    updateLabel(child);
  }

  public void resetGameRow() {
    menuItemsRow.getChildren().removeAll(menuItemsRow.getChildren());
  }

  private void loadMenuItems() {
    menuItemsRow.getChildren().clear();
    selectionIndex = 0;
    for (PauseMenuItem item : menuItems) {
      menuItemsRow.getChildren().add(createItemFor(item));
    }

    if (!menuItems.isEmpty()) {
      while (menuItemsRow.getChildren().size() * UIDefaults.THUMBNAIL_SIZE < UIDefaults.SCREEN_WIDTH * 2) {
        Label label = new Label();
        label.setMinWidth(THUMBNAIL_SIZE);
        menuItemsRow.getChildren().add(label);
      }
    }
  }

  private BorderPane createItemFor(PauseMenuItem menuItem) {
    Image wheel = new Image(PauseMenu.class.getResourceAsStream("avatar-blank.png"));
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
    stackPane.getChildren().add(label);
    borderPane.setCenter(stackPane);
    borderPane.setCache(true);
    borderPane.setCacheHint(CacheHint.SCALE_AND_ROTATE);
    return borderPane;
  }

  public GameRepresentation getGameSelection() {
    Node node = menuItemsRow.getChildren().get(selectionIndex);
    return (GameRepresentation) node.getUserData();
  }

  public ArchiveDescriptorRepresentation getArchiveSelection() {
    Node node = menuItemsRow.getChildren().get(selectionIndex);
    return (ArchiveDescriptorRepresentation) node.getUserData();
  }

  public void setArrowsVisible(boolean b) {
    arrowRight.setVisible(b);
    arrowLeft.setVisible(b);
  }
}
