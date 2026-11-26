package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.ui.util.ThemeColors;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.AchievementInfo;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Overview tile: the achievements closest to unlock.
 */
public class OverviewClosestTileController {

  private static final String[] COLORS = {BLUE, ORANGE, ThemeColors.RARITY_EPIC, GREEN, ThemeColors.MEDAL_GOLD};

  @FXML
  private VBox entriesBox;

  public void render(PlayerAchievements p) {
    entriesBox.getChildren().clear();
    if (p.closest.isEmpty()) {
      entriesBox.getChildren().add(muted(Messages.get("achievements.closest_empty")));
      return;
    }
    int c = 0;
    for (AchievementInfo a : p.closest) {
      VBox entry = new VBox(4);
      HBox line = new HBox(8);
      String title = a.tableName != null ? a.tableName + " – " + a.displayTitle : a.displayTitle;
      Label t = bold(title);
      t.setWrapText(true);
      line.getChildren().addAll(t, spacer(), muted(Math.round(a.getProgressPct()) + "%"));
      entry.getChildren().addAll(line, progress(a.getProgressPct(), COLORS[c++ % COLORS.length], 6));
      tooltip(entry, a.title + "\n" + (a.condition != null ? a.condition + "\n" : "") + a.progressCurrent + " / " + a.progressTarget);
      entriesBox.getChildren().add(entry);
    }
  }
}
