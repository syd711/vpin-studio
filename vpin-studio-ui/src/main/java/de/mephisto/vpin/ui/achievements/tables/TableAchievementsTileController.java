package de.mephisto.vpin.ui.achievements.tables;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.AchievementInfo;
import de.mephisto.vpin.restclient.achievements.TableAchievementsDetails;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Tables tile: the achievements of the selected table.
 */
public class TableAchievementsTileController implements Initializable {

  @FXML
  private CheckBox showLockedCheckbox;
  @FXML
  private VBox rowsBox;

  private TableAchievementsDetails details;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    showLockedCheckbox.selectedProperty().addListener((obs, o, n) -> renderRows());
  }

  public void render(TableAchievementsDetails details) {
    this.details = details;
    renderRows();
  }

  private void renderRows() {
    rowsBox.getChildren().clear();
    if (details == null) {
      return;
    }
    List<AchievementInfo> items = new ArrayList<>(details.achievements);
    items.sort((a, b) -> Boolean.compare(b.unlocked, a.unlocked));
    int i = 0;
    for (AchievementInfo a : items) {
      if (!a.unlocked && !showLockedCheckbox.isSelected()) continue;
      rowsBox.getChildren().add(achievementRow(a, i++));
    }
  }

  private HBox achievementRow(AchievementInfo a, int index) {
    HBox row = row(index, false);
    StackPane state = new StackPane();
    state.setMinSize(22, 22);
    state.setMaxSize(22, 22);
    state.setStyle("-fx-background-color: " + (a.unlocked ? "#1F3A26" : "#222222") + ";");
    state.getChildren().add(icon(a.unlocked ? "mdi2c-check-bold" : "mdi2l-lock", a.unlocked ? GREEN : "#9A9A9A", 13));
    VBox titleBox = new VBox(2);
    HBox.setHgrow(titleBox, Priority.ALWAYS);
    titleBox.setMaxWidth(Double.MAX_VALUE);
    Label title = a.unlocked ? bold(a.displayTitle) : colored(a.displayTitle, MUTED, 14, false);
    titleBox.getChildren().add(title);
    if (a.legacy) {
      titleBox.getChildren().add(colored(Messages.get("achievements.legacy"), "#9A9A9A", 11, false));
    }
    else if (!a.unlocked && a.progressTarget > 0 && a.progressCurrent > 0) {
      HBox prog = new HBox(6);
      prog.setAlignment(Pos.CENTER_LEFT);
      prog.getChildren().addAll(progress(a.getProgressPct(), BLUE, 4), colored(a.progressCurrent + " / " + a.progressTarget, "#9A9A9A", 11, false));
      titleBox.getChildren().add(prog);
    }
    Label when = muted(a.unlocked ? date(a.unlockedAt) : "—");
    when.setMinWidth(100);
    HBox rarity = new HBox();
    rarity.setMinWidth(90);
    Node chip = rarityChip(a.rarity);
    if (chip != null) {
      rarity.getChildren().add(chip);
    }
    row.getChildren().addAll(state, titleBox, when, rarity);
    tooltip(row, a.title + (a.condition != null ? "\n" + a.condition : "") + (a.rarity != null ? "\n" + a.rarity + " (" + pct(a.rarityPct) + ")" : ""));
    return row;
  }
}
