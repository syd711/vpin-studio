package de.mephisto.vpin.ui.achievements.leaderboards;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboard;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboardEntry;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Leaderboards tile: the global top 50 by unlocked achievements.
 */
public class LeaderboardsGlobalTileController {

  @FXML
  private Label infoLabel;
  @FXML
  private HBox headerRow;
  @FXML
  private VBox rowsBox;

  private PlayerAchievements player;

  public void showUnavailable() {
    infoLabel.setText(Messages.get("achievements.cloud_unavailable"));
    show(headerRow, false);
    rowsBox.getChildren().clear();
  }

  public void showRegistration(AchievementsController controller) {
    infoLabel.setText(Messages.get("dialog.registration_required"));
    show(headerRow, false);
    rowsBox.getChildren().setAll(registrationBox(controller));
  }

  public void load(@Nullable PlayerAchievements p) {
    this.player = p;
    String uuid = p != null ? p.maniaAccountUuid : null;
    infoLabel.setText(Messages.get("achievements.loading"));
    show(headerRow, false);
    rowsBox.getChildren().clear();
    JFXFuture.supplyAsync(() -> Studio.maniaClient.getAchievementClient().getGlobalLeaderboard(50, uuid))
        .thenAcceptLater((AchievementLeaderboard board) -> {
          if (player != p) {
            return;
          }
          infoLabel.setText(Messages.get("achievements.global_info", board != null ? number(board.players) : "0"));
          if (board == null || board.entries.isEmpty()) {
            rowsBox.getChildren().add(muted(Messages.get("achievements.no_cloud_data")));
            return;
          }
          show(headerRow, true);
          int i = 0;
          boolean ownListed = false;
          for (AchievementLeaderboardEntry e : board.entries) {
            boolean own = uuid != null && uuid.equals(e.accountUuid);
            ownListed |= own;
            rowsBox.getChildren().add(globalRow(e, i++, own));
          }
          if (!ownListed && board.ownEntry != null) {
            HBox own = globalRow(board.ownEntry, 0, true);
            VBox.setMargin(own, new Insets(6, 0, 0, 0));
            rowsBox.getChildren().add(own);
          }
        })
        .onErrorLater(e -> showUnavailable());
  }

  private HBox globalRow(AchievementLeaderboardEntry e, int index, boolean own) {
    HBox row = row(index, own);
    HBox rank = new HBox(rank(e.rank));
    rank.setMinWidth(40);
    HBox playerCell = LeaderboardPlayerCell.create(e, own, player);
    HBox.setHgrow(playerCell, Priority.ALWAYS);
    Label lvl = muted(String.valueOf(e.level));
    lvl.setMinWidth(44);
    Label count = colored(number(e.count), ORANGE, 14, true);
    count.setMinWidth(80);
    row.getChildren().addAll(rank, playerCell, lvl, count);
    return row;
  }
}
