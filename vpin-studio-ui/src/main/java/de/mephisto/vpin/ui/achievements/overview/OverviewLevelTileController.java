package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Overview tile: level circle, level name, prestige and the progress to the next level.
 */
public class OverviewLevelTileController {

  @FXML
  private StackPane circleBox;
  @FXML
  private Label nameLabel;
  @FXML
  private Label prestigeLabel;
  @FXML
  private StackPane progressBox;
  @FXML
  private Label nextLabel;

  public void render(PlayerAchievements p) {
    circleBox.getChildren().setAll(levelCircle(p.level.level, 72));
    nameLabel.setText(p.level.name);
    prestigeLabel.setText(p.level.prestigeDisplay);
    progressBox.getChildren().setAll(progress(p.level.progressPct, ORANGE, 8));
    nextLabel.setText(nextLevelText(p));
  }
}
