package de.mephisto.vpin.ui.achievements.tables;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboard;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboardEntry;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Tables tile: the cloud top 5 of the selected table (VPin Mania).
 */
public class TableTop5TileController {

  @FXML
  private VBox rowsBox;

  private String rom;
  private AchievementsController controller;

  public void setAchievementsController(AchievementsController controller) {
    this.controller = controller;
  }

  public void load(String rom, @Nullable PlayerAchievements p) {
    this.rom = rom;
    if (!isManiaAvailable()) {
      rowsBox.getChildren().setAll(muted(Messages.get("achievements.cloud_unavailable")));
      return;
    }
    if (controller.isRegistrationRequired()) {
      rowsBox.getChildren().setAll(registrationBox(controller));
      return;
    }
    String uuid = p != null ? p.maniaAccountUuid : null;
    rowsBox.getChildren().setAll(muted(Messages.get("achievements.loading")));
    JFXFuture.supplyAsync(() -> Studio.maniaClient.getAchievementClient().getRomLeaderboard(rom, 5, uuid))
        .thenAcceptLater((AchievementLeaderboard board) -> {
          if (!rom.equals(this.rom)) {
            return;
          }
          rowsBox.getChildren().clear();
          if (board == null || board.entries.isEmpty()) {
            rowsBox.getChildren().add(muted(Messages.get("achievements.no_cloud_data")));
            return;
          }
          int i = 0;
          boolean ownListed = false;
          for (AchievementLeaderboardEntry e : board.entries) {
            boolean own = uuid != null && uuid.equals(e.accountUuid);
            ownListed |= own;
            rowsBox.getChildren().add(topRow(e, i++, own));
          }
          if (!ownListed && board.ownEntry != null) {
            HBox own = topRow(board.ownEntry, 0, true);
            VBox.setMargin(own, new Insets(4, 0, 0, 0));
            rowsBox.getChildren().add(own);
          }
        })
        .onErrorLater(e -> rowsBox.getChildren().setAll(muted(Messages.get("achievements.cloud_unavailable"))));
  }

  private HBox topRow(AchievementLeaderboardEntry e, int index, boolean own) {
    HBox row = row(index, own);
    Label pos = bold("#" + e.rank);
    pos.setMinWidth(34);
    Label name = text(e.displayName != null ? e.displayName : e.initials);
    HBox.setHgrow(name, Priority.ALWAYS);
    name.setMaxWidth(Double.MAX_VALUE);
    row.getChildren().addAll(pos, name, colored(e.unlocked + "/" + e.total, ORANGE, 13, true));
    return row;
  }
}
