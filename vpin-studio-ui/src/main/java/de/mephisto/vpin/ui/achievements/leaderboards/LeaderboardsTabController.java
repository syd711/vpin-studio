package de.mephisto.vpin.ui.achievements.leaderboards;

import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import de.mephisto.vpin.ui.achievements.AchievementsTab;
import javafx.fxml.FXML;
import org.jspecify.annotations.Nullable;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.isManiaAvailable;

/**
 * A4 - Leaderboards: global top 50 by different achievements and the top 25 per table (VPin Mania).
 */
public class LeaderboardsTabController implements AchievementsTab {

  @FXML
  private LeaderboardsGlobalTileController globalTileController;
  @FXML
  private LeaderboardsTableTileController tableTileController;

  private boolean dirty = true;

  @Override
  public void setAchievementsController(AchievementsController controller) {
    tableTileController.setAchievementsController(controller);
  }

  @Override
  public void setDirty() {
    dirty = true;
  }

  public void selectRom(String rom) {
    tableTileController.selectRom(rom);
  }

  @Override
  public void refresh(@Nullable PlayerAchievements p) {
    if (!dirty) {
      return;
    }
    dirty = false;
    if (!isManiaAvailable()) {
      globalTileController.showUnavailable();
      tableTileController.clear();
      return;
    }
    globalTileController.load(p);
    tableTileController.loadChips(p);
    tableTileController.reload();
  }
}
