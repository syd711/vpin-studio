package de.mephisto.vpin.ui.achievements.badges;

import de.mephisto.vpin.restclient.achievements.LevelDefinition;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Badges tile: the current player level, the table of all levels and the prestige.
 */
public class BadgesLevelTileController {

  @FXML
  private VBox root;
  @FXML
  private StackPane circleBox;
  @FXML
  private Label nameLabel;
  @FXML
  private StackPane progressBox;
  @FXML
  private Label nextLabel;
  @FXML
  private VBox levelRows;
  @FXML
  private Label prestigeLabel;

  public void render(@Nullable PlayerAchievements p) {
    root.setVisible(p != null);
    levelRows.getChildren().clear();
    if (p == null) {
      return;
    }
    circleBox.getChildren().setAll(levelCircle(p.level.level, 64));
    nameLabel.setText(p.level.name);
    progressBox.getChildren().setAll(progress(p.level.progressPct, ORANGE, 7));
    nextLabel.setText(nextLevelText(p));
    prestigeLabel.setText(p.level.prestigeDisplay);

    int i = 0;
    for (LevelDefinition lv : p.levels) {
      boolean current = lv.level == p.level.level;
      HBox row = row(i++, false);
      if (current) {
        row.setStyle("-fx-background-color: " + ROW_SELECTED + "; -fx-border-color: transparent transparent transparent " + ORANGE + "; -fx-border-width: 0 0 0 3;");
      }
      String color = lv.level > p.level.level ? "#9A9A9A" : "white";
      row.getChildren().addAll(fixed(colored(String.valueOf(lv.level), color, 14, true), 44), grow(colored(lv.name, color, 14, current)),
          colored(number(lv.from), color, 12, false));
      levelRows.getChildren().add(row);
    }
  }

  private static Node fixed(Label l, double width) {
    l.setMinWidth(width);
    l.setPrefWidth(width);
    return l;
  }

  private static Node grow(Label l) {
    HBox.setHgrow(l, Priority.ALWAYS);
    l.setMaxWidth(Double.MAX_VALUE);
    return l;
  }
}
