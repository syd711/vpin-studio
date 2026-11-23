package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.number;

/**
 * Overview tile: number of unlocked achievements.
 */
public class OverviewUnlockedTileController {

  @FXML
  private Label countLabel;
  @FXML
  private Label weekLabel;

  public void render(PlayerAchievements p) {
    countLabel.setText(number(p.unlockedCount));
    weekLabel.setText(Messages.get("achievements.this_week", p.unlockedThisWeek));
  }
}
