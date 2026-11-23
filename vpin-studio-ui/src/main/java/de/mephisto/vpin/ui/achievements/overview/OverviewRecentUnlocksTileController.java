package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.AchievementInfo;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Overview tile: the latest unlocked achievements.
 */
public class OverviewRecentUnlocksTileController {

  @FXML
  private VBox rowsBox;

  public void render(PlayerAchievements p) {
    rowsBox.getChildren().clear();
    if (p.recentUnlocks.isEmpty()) {
      rowsBox.getChildren().add(muted(Messages.get("achievements.none_yet")));
      return;
    }
    int i = 0;
    for (AchievementInfo a : p.recentUnlocks) {
      HBox row = row(i++, false);
      row.getChildren().add(dot(a.rarity != null ? RARITY_COLORS.getOrDefault(a.rarity, MUTED) : BLUE, 5));
      VBox texts = new VBox(1);
      texts.getChildren().addAll(bold(a.displayTitle), muted(a.tableName != null ? a.tableName : (a.rom != null ? a.rom : Messages.get("achievements.global"))));
      HBox.setHgrow(texts, Priority.ALWAYS);
      row.getChildren().add(texts);
      Node chip = rarityChip(a.rarity);
      if (chip != null) {
        row.getChildren().add(chip);
      }
      tooltip(row, a.title + "\n" + date(a.unlockedAt));
      rowsBox.getChildren().add(row);
    }
  }
}
