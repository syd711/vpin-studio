package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.commons.fx.widgets.WidgetPlayerRankController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboard;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboardEntry;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Overview tile: player ranking of the cabinet (commons widget) or the cloud top 5.
 */
public class OverviewRankingTileController {

  @FXML
  private ToggleButton cabinetBtn;
  @FXML
  private ToggleButton cloudBtn;
  @FXML
  private Node playerRank;
  @FXML
  private WidgetPlayerRankController playerRankController;
  @FXML
  private VBox cloudBox;

  private AchievementsController controller;
  private PlayerAchievements player;
  private boolean cloudRanking = false;

  @FXML
  private void initialize() {
    // the tile renders the title next to the toggle buttons
    if (playerRank instanceof BorderPane widgetRoot) {
      widgetRoot.setTop(null);
      widgetRoot.setPadding(Insets.EMPTY);
    }
  }

  public void setAchievementsController(AchievementsController controller) {
    this.controller = controller;
  }

  public void render(PlayerAchievements p) {
    this.player = p;
    boolean cloud = controller.isCloudAvailable();
    cloudBtn.setDisable(!cloud);
    if (cloudRanking && cloud) {
      showCloudRanking();
    }
    else {
      showCabinetRanking();
    }
  }

  @FXML
  private void onCabinet() {
    cloudRanking = false;
    showCabinetRanking();
  }

  @FXML
  private void onCloud() {
    cloudRanking = true;
    showCloudRanking();
  }

  private void showCabinetRanking() {
    cabinetBtn.setSelected(true);
    show(cloudBox, false);
    show(playerRank, true);
    playerRankController.refresh();
  }

  private void showCloudRanking() {
    cloudBtn.setSelected(true);
    show(playerRank, false);
    show(cloudBox, true);
    if (player == null) {
      return;
    }
    PlayerAchievements p = player;
    String uuid = p.maniaAccountUuid;
    cloudBox.getChildren().setAll(muted(Messages.get("achievements.loading")));
    JFXFuture.supplyAsync(() -> Studio.maniaClient.getAchievementClient().getGlobalLeaderboard(5, uuid))
        .thenAcceptLater((AchievementLeaderboard board) -> {
          if (player != p) {
            return;
          }
          cloudBox.getChildren().clear();
          if (board == null || board.entries.isEmpty()) {
            cloudBox.getChildren().add(muted(Messages.get("achievements.no_cloud_data")));
            return;
          }
          int i = 0;
          boolean ownListed = false;
          for (AchievementLeaderboardEntry e : board.entries) {
            boolean own = uuid != null && uuid.equals(e.accountUuid);
            ownListed |= own;
            cloudBox.getChildren().add(rankingRow(e, i++, own));
          }
          if (!ownListed && board.ownEntry != null) {
            HBox own = rankingRow(board.ownEntry, 0, true);
            VBox.setMargin(own, new Insets(4, 0, 0, 0));
            cloudBox.getChildren().add(own);
          }
        })
        .onErrorLater(e -> cloudBox.getChildren().setAll(muted(Messages.get("achievements.cloud_unavailable"))));
  }

  private HBox rankingRow(AchievementLeaderboardEntry e, int index, boolean own) {
    HBox row = row(index, own);
    Label pos = bold("#" + e.rank);
    pos.setMinWidth(44);
    Label name = bold(e.displayName != null ? e.displayName : e.initials);
    HBox.setHgrow(name, Priority.ALWAYS);
    name.setMaxWidth(Double.MAX_VALUE);
    row.getChildren().addAll(pos, name, muted(Messages.get("achievements.ach_count", number(e.count))));
    return row;
  }
}
