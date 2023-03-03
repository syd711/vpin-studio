package de.mephisto.vpin.poppermenu;

import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
  private Node installPanel;

  @FXML
  private Node uninstallPanel;

  @FXML
  private Node baseSelector;

  @FXML
  private HBox gameRow;

  private boolean installToggle = true;
  private boolean loadedArchives = false;
  private int selectionIndex = 0;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void toggleInstall() {
    if (installToggle) {
      baseSelector.setStyle("-fx-background-color: #33CC00;");
      TransitionUtil.createOutFader(installPanel).play();
      TransitionUtil.createInFader(uninstallPanel).play();
    }
    else {
      baseSelector.setStyle("-fx-background-color: #FF3333;");
      TransitionUtil.createOutFader(uninstallPanel).play();
      TransitionUtil.createInFader(installPanel).play();
    }
    installToggle = !installToggle;
  }

  public boolean isInstallSelected() {
    return this.installToggle;
  }

  public void enterInstall() {
    TransitionUtil.createOutFader(uninstallPanel).play();
    TransitionUtil.createOutFader(installPanel).play();
    TransitionUtil.createInFader(gameRow).play();

    if (!loadedArchives) {
      List<VpaDescriptorRepresentation> vpaDescriptors = MenuMain.client.getVpaDescriptors();
      loadedArchives = true;
      loadArchivedItems(vpaDescriptors);

      Pane node = (Pane) gameRow.getChildren().get(0);
      Bounds boundsInScene = node.localToScene(node.getBoundsInLocal());
      System.out.println(boundsInScene.getMinX());
      TransitionUtil.createTranslateByXTransition(gameRow, 100, UIDefaults.SCREEN_WIDTH / 2).play();

      BorderPane child = (BorderPane) gameRow.getChildren().get(selectionIndex);
      TransitionUtil.createTranslateByXTransition(child, 60, -SCROLL_OFFSET).play();
      TransitionUtil.createScaleTransition(child, UIDefaults.SELECTION_SCALE, 100).play();
    }
  }

  public void enterMainWithInstall() {
    TransitionUtil.createOutFader(gameRow).play();
    TransitionUtil.createOutFader(uninstallPanel).play();
    TransitionUtil.createInFader(installPanel).play();
  }

  public void scrollGameBarRight() {
    if(selectionIndex == (gameRow.getChildren().size() - 1)) {
      return;
    }

    TranslateTransition t = TransitionUtil.createTranslateByXTransition(gameRow, 60, -THUMBNAIL_SIZE);
    t.statusProperty().addListener(new ChangeListener<Animation.Status>() {
      @Override
      public void changed(ObservableValue<? extends Animation.Status> observable, Animation.Status oldValue, Animation.Status newValue) {
//        Node node = gameRow.getChildren().get(gameRow.getChildren().size() - 1);
//        gameRow.getChildren().remove(node);
//        gameRow.getChildren().add(0, node);
      }
    });

    Node node = gameRow.getChildren().get(selectionIndex);
    TransitionUtil.createTranslateByXTransition(node, 60, -SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(node, UIDefaults.SELECTION_SCALE_DEFAULT, 100).play();

    selectionIndex++;
    t.play();
    node = gameRow.getChildren().get(selectionIndex);
    TransitionUtil.createTranslateByXTransition(node, 60, -SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(node, UIDefaults.SELECTION_SCALE, 100).play();
  }

  public void scrollGameBarLeft() {
    if(selectionIndex <= 0) {
      selectionIndex = 0;
      return;
    }

    TranslateTransition t = TransitionUtil.createTranslateByXTransition(gameRow, 60, THUMBNAIL_SIZE);
    t.statusProperty().addListener(new ChangeListener<Animation.Status>() {
      @Override
      public void changed(ObservableValue<? extends Animation.Status> observable, Animation.Status oldValue, Animation.Status newValue) {
//        Node node = gameRow.getChildren().get(0);
//        gameRow.getChildren().remove(node);
//        gameRow.getChildren().add(node);
      }
    });


    Node node = gameRow.getChildren().get(selectionIndex);
    TransitionUtil.createTranslateByXTransition(node, 60, SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(node, UIDefaults.SELECTION_SCALE_DEFAULT, 100).play();

    selectionIndex--;
    t.play();

    node = gameRow.getChildren().get(selectionIndex);
    TransitionUtil.createTranslateByXTransition(node, 60, SCROLL_OFFSET).play();
    TransitionUtil.createScaleTransition(node, UIDefaults.SELECTION_SCALE, 100).play();
  }



  private void loadArchivedItems(List<VpaDescriptorRepresentation> vpaDescriptors) {
    for (VpaDescriptorRepresentation vpaDescriptor : vpaDescriptors) {
//        FXMLLoader loader = new FXMLLoader(ArchiveItemController.class.getResource("menu-archive-item.fxml"));
//        BorderPane root = loader.load();
//        gameRow.getChildren().add(root);
//
//        ArchiveItemController controller = loader.getController();
//        controller.setData(vpaDescriptor);

      BorderPane borderPane = new BorderPane();
      ImageView imageView = new ImageView();
      imageView.setPreserveRatio(true);
      imageView.setFitWidth(THUMBNAIL_SIZE);
      imageView.setFitHeight(THUMBNAIL_SIZE);
      String thumbnail = vpaDescriptor.getManifest().getIcon();
      if (thumbnail == null) {
        Image wheel = new Image(MenuMain.class.getResourceAsStream("avatar-blank.png"));
        imageView.setImage(wheel);
        borderPane.setCenter(imageView);
      }
      else {
        byte[] decode = Base64.getDecoder().decode(thumbnail);
        Image wheel = new Image(new ByteArrayInputStream(decode));
        imageView.setImage(wheel);
        borderPane.setCenter(imageView);
      }
      gameRow.getChildren().add(borderPane);
    }
  }
}
