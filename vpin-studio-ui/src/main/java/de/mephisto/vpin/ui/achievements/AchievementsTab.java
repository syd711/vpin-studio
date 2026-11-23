package de.mephisto.vpin.ui.achievements;

import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import org.jspecify.annotations.Nullable;

/**
 * Controller of a tab of the achievements section, each tab is loaded from its own FXML and included by scene-achievements.fxml.
 */
public interface AchievementsTab {

  void setAchievementsController(AchievementsController controller);

  /**
   * Marks the tab for a re-render on the next {@link #refresh(PlayerAchievements)}.
   */
  void setDirty();

  void refresh(@Nullable PlayerAchievements player);
}
