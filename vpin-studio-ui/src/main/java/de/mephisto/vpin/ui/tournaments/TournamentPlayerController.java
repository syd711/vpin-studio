package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentPlayer;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentPlayerController extends WidgetController implements Initializable {

  @FXML
  private ImageView userImageView;

  @FXML
  private Label userNameLabel;

  @FXML
  private Label scoreCountLabel;

  // Add a public no-args constructor
  public TournamentPlayerController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(ManiaTournamentRepresentation tournament, ManiaTournamentPlayer player) {
    Image image = new Image(maniaClient.getAccountClient().getAvatarUrl(player.getUuid()));
    userImageView.setImage(image);
    CommonImageUtil.setClippedImage(userImageView, (int) (image.getWidth() / 2));

    userNameLabel.setText(player.getDisplayName());
    scoreCountLabel.setText(String.valueOf(maniaClient.getHighscoreClient().getPlayerHighscores(tournament, player).size()));
  }
}