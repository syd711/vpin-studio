package de.mephisto.vpin.ui.achievements.badges;

import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import de.mephisto.vpin.ui.achievements.AchievementsTab;
import javafx.fxml.FXML;
import org.jspecify.annotations.Nullable;

/**
 * A3 - Badges: level table, prestige, badge groups with progress and the badge shown next to the name.
 */
public class BadgesTabController implements AchievementsTab {

  @FXML
  private BadgesLevelTileController levelTileController;
  @FXML
  private BadgesGridTileController badgesTileController;

  private boolean dirty = true;

  @Override
  public void setAchievementsController(AchievementsController controller) {
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
    levelTileController.render(p);
    badgesTileController.render(p);
  }
}
