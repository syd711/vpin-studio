package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.restclient.achievements.BadgeInfo;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.badgeTooltip;
import static de.mephisto.vpin.ui.achievements.AchievementsUI.medal;
import static de.mephisto.vpin.ui.achievements.AchievementsUI.tooltip;

/**
 * Overview tile: all badges as medals.
 */
public class OverviewBadgesTileController {

  private static final int BADGES_TAB_INDEX = 2;
  private static final int MEDAL_SIZE = 60;

  @FXML
  private Label countLabel;
  @FXML
  private FlowPane medalsPane;

  private AchievementsController controller;

  public void setAchievementsController(AchievementsController controller) {
    this.controller = controller;
  }

  @FXML
  private void initialize() {
    // The FlowPane's pref height is computed against prefWrapLength (default 400px), which made the
    // bottom grid row (vgrow NEVER) far too tall. Wrap against the actual width instead, so the tile
    // only claims the height its medals really need and the middle row gets the remaining space.
    medalsPane.setPrefWrapLength(Double.MAX_VALUE);
    medalsPane.widthProperty().addListener((obs, o, w) -> {
      if (w.doubleValue() > 0) {
        medalsPane.setPrefWrapLength(w.doubleValue());
      }
    });
  }

  public void render(PlayerAchievements p) {
    countLabel.setText(p.badgesEarned + " / " + p.badgesTotal);
    medalsPane.getChildren().clear();
    for (BadgeInfo b : p.badges) {
      StackPane medal = medal(b.category, b.earned, MEDAL_SIZE);
      tooltip(medal, badgeTooltip(b));
      medalsPane.getChildren().add(medal);
    }
  }

  @FXML
  private void onAllBadges() {
    controller.showTab(BADGES_TAB_INDEX);
  }
}
