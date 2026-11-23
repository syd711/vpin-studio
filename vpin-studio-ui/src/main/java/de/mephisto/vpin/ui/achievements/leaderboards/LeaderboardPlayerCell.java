package de.mephisto.vpin.ui.achievements.leaderboards;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboardEntry;
import de.mephisto.vpin.restclient.achievements.BadgeInfo;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.mania.util.ManiaAvatarCache;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import org.jspecify.annotations.Nullable;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Avatar, name and display badge of a leaderboard entry, shared by the leaderboard tiles.
 */
final class LeaderboardPlayerCell {

  private LeaderboardPlayerCell() {
  }

  static HBox create(AchievementLeaderboardEntry e, boolean own, @Nullable PlayerAchievements current) {
    HBox cell = new HBox(8);
    cell.setAlignment(Pos.CENTER_LEFT);
    StackPane avatar = initials(e.initials, 28);
    if (e.accountUuid != null) {
      String uuid = e.accountUuid;
      JFXFuture.supplyAsync(() -> ManiaAvatarCache.getAvatarImage(uuid)).thenAcceptLater(img -> {
        if (img != null) {
          ImageView view = new ImageView(img);
          view.setFitWidth(28);
          view.setFitHeight(28);
          view.setClip(new Circle(14, 14, 14));
          avatar.setStyle("");
          avatar.getChildren().setAll(view);
        }
      });
    }
    String name = (e.displayName != null ? e.displayName : e.initials) + (own ? " " + Messages.get("achievements.you") : "");
    cell.getChildren().addAll(avatar, text(name));
    if (e.selectedBadge != null) {
      BadgeInfo info = current == null ? null : current.badges.stream().filter(b -> b.id.equals(e.selectedBadge)).findFirst().orElse(null);
      Circle badge = dot(info != null ? CATEGORY_COLORS.getOrDefault(info.category, BLUE) : BLUE, 6);
      tooltip(badge, info != null ? info.name + " – " + info.description : e.selectedBadge);
      cell.getChildren().add(badge);
    }
    return cell;
  }
}
