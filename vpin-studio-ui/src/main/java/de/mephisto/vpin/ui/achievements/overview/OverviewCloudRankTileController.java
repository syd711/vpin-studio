package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Overview tile: the global cloud rank of the player (VPin Mania).
 */
public class OverviewCloudRankTileController implements Initializable {

  @FXML
  private Label rankLabel;
  @FXML
  private Label detailLabel;
  @FXML
  private VBox registerBox;

  private AchievementsController controller;
  private PlayerAchievements player;

  public void setAchievementsController(AchievementsController controller) {
    this.controller = controller;
  }

  public void render(PlayerAchievements p) {
    this.player = p;
    rankLabel.setText("—");
    boolean registrationRequired = controller.isRegistrationRequired();
    show(detailLabel, !registrationRequired);
    show(registerBox, registrationRequired);
    if (registrationRequired) {
      rankLabel.setVisible(false);
      registerBox.getChildren().setAll(registrationBox(controller));
      return;
    }
    if (!controller.isCloudAvailable()) {
      detailLabel.setText(Messages.get("achievements.cloud_not_linked"));
      return;
    }
    detailLabel.setText(Messages.get("achievements.loading"));
    String uuid = p.maniaAccountUuid;
    JFXFuture.supplyAsync(() -> Studio.maniaClient.getAchievementClient().getGlobalLeaderboard(1, uuid))
        .thenAcceptLater(board -> {
          if (player != p) {
            return;
          }
          if (board != null && board.ownRank != null) {
            rankLabel.setText("#" + number(board.ownRank));
            detailLabel.setText(Messages.get("achievements.of_players", number(board.players)));
          }
          else {
            detailLabel.setText(Messages.get("achievements.no_cloud_rank"));
          }
        })
        .onErrorLater(e -> detailLabel.setText(Messages.get("achievements.cloud_unavailable")));
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    rankLabel.managedProperty().bindBidirectional(rankLabel.visibleProperty());
  }
}
