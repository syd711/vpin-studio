package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class PauseMenuItemComponentFactory {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Pane createMenuItemFor(PauseMenuItem menuItem) {
    Image wheel = menuItem.getImage();
    if (wheel == null) {
      wheel = new Image(PauseMenu.class.getResourceAsStream("avatar-blank.png"));
    }
    String text = menuItem.getName();
    return createMenuItemComponent(wheel, text, menuItem);
  }

  private static BorderPane createMenuItemComponent(Image image, String text, Object data) {
    BorderPane borderPane = new BorderPane();
    borderPane.setUserData(data);
    ImageView imageView = new ImageView();
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(PauseMenuUIDefaults.THUMBNAIL_SIZE);
    imageView.setFitHeight(PauseMenuUIDefaults.THUMBNAIL_SIZE);

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
