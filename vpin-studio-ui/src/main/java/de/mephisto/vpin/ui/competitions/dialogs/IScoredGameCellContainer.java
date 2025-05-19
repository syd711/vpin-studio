package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.InputStream;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredGameCellContainer extends HBox {

  public IScoredGameCellContainer(List<GameRepresentation> games, VpsTable vpsTable, String customStyles) {
    super(3);

    setPadding(new Insets(3, 0, 6, 0));

    String name = vpsTable.getName();
    if (name.length() > 40) {
      name = name.substring(0, 39) + "...";
    }

    InputStream gameMediaItem = ServerFX.class.getResourceAsStream("avatar-blank.png");
    for (GameRepresentation game : games) {
      InputStream gameItem = client.getGameMediaItem(game.getId(), VPinScreen.Wheel);
      if (gameItem != null) {
        gameMediaItem = gameItem;
        break;
      }
    }


    Image image = new Image(gameMediaItem);
    ImageView imageView = new ImageView(image);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(100);
    Tooltip.install(imageView, new Tooltip(name));

    this.getChildren().add(imageView);

    VBox column = new VBox(3);
    this.getChildren().add(column);

    Label title = new Label(name);
    title.setTooltip(new Tooltip(name));
    title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;" + customStyles);
    column.getChildren().add(title);

    if (games.isEmpty()) {
      Label label = new Label("No matching table installed");
      label.setStyle("-fx-padding: 3 6 3 6;" + customStyles);
      label.getStyleClass().add("error-title");
      column.getChildren().add(label);
    }
    else if (games.size() > 1) {
      Label label = new Label("+");
      label.getStyleClass().add("default-text");
      label.setStyle(customStyles);
      label.setTooltip(new Tooltip((games.size() - 1) + " additional matching tables have been found."));
      label.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
      FontIcon icon = WidgetFactory.createIcon("mdi2n-numeric-" + games.size() + "-box-multiple-outline");
      icon.setIconSize(26);
      label.setGraphic(icon);
      label.setStyle("-fx-padding: 3 6 3 6; -fx-font-size: 24px;");
      column.getChildren().add(label);
    }
  }
}
