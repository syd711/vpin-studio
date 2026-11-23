package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import de.mephisto.vpin.ui.achievements.AchievementsTab;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.Nullable;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.show;

/**
 * A1 - Overview: level, unlocked, cloud rank, recent unlocks, latest scores, player ranking, badges, closest to unlock.
 */
public class OverviewTabController implements AchievementsTab {

  @FXML
  private Label emptyLabel;
  @FXML
  private GridPane grid;

  @FXML
  private OverviewLevelTileController levelTileController;
  @FXML
  private OverviewUnlockedTileController unlockedTileController;
  @FXML
  private OverviewCloudRankTileController cloudRankTileController;
  @FXML
  private OverviewRecentUnlocksTileController recentUnlocksTileController;
  @FXML
  private OverviewLatestScoresTileController latestScoresTileController;
  @FXML
  private OverviewRankingTileController rankingTileController;
  @FXML
  private OverviewBadgesTileController badgesTileController;
  @FXML
  private OverviewClosestTileController closestTileController;

  private boolean dirty = true;

  @Override
  public void setAchievementsController(AchievementsController controller) {
    cloudRankTileController.setAchievementsController(controller);
    rankingTileController.setAchievementsController(controller);
    badgesTileController.setAchievementsController(controller);
  }

  @Override
  public void setDirty() {
    dirty = true;
  }

  @Override
  public void refresh(@Nullable PlayerAchievements p) {
    if (!dirty) {
      return;
    }
    dirty = false;
    show(emptyLabel, p == null);
    show(grid, p != null);
    if (p == null) {
      return;
    }
    levelTileController.render(p);
    unlockedTileController.render(p);
    cloudRankTileController.render(p);
    recentUnlocksTileController.render(p);
    latestScoresTileController.render();
    rankingTileController.render(p);
    badgesTileController.render(p);
    closestTileController.render(p);
  }
}
