package de.mephisto.vpin.commons.fx.discord;

import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class DiscordUserEntryController extends WidgetController implements Initializable {

  @FXML
  private ImageView userImageView;

  @FXML
  private Label userNameLabel;

  // Add a public no-args constructor
  public DiscordUserEntryController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(PlayerRepresentation player) {
    Image image = new Image(player.getAvatarUrl());
    userImageView.setImage(image);
    CommonImageUtil.setClippedImage(userImageView, (int) (image.getWidth() / 2));

    userNameLabel.setText(player.getName());
  }
}