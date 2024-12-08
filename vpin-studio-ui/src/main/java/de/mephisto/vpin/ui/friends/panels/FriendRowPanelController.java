package de.mephisto.vpin.ui.friends.panels;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class FriendRowPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(FriendRowPanelController.class);

  @FXML
  private HBox root;

  public void setData(Cabinet contact) {
    Image image = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
    ImageView view = new ImageView(image);

    view.setPreserveRatio(true);
    view.setFitWidth(50);
    view.setFitHeight(50);

    root.setAlignment(Pos.CENTER_LEFT);
    root.getChildren().add(view);
    root.setSpacing(6);

    Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
    Label label = new Label("bubu");
    label.setFont(defaultFont);
    label.getStyleClass().add("default-text-color");
    root.getChildren().add(label);

    new Thread(() -> {
      InputStream in = null; //client.getCachedUrlImage(maniaClient.getAccountClient().getAvatarUrl(value.getAccountUUID()));
      if (in == null) {
        in = ServerFX.class.getResourceAsStream("avatar-blank.png");
      }
      final InputStream data = in;
      if (data != null) {
        Platform.runLater(() -> {
          Image i = new Image(data);
          view.setImage(i);
          CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
        });
      }
    }).start();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }
}
