package de.mephisto.vpin.ui.achievements.tables;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievementsDetails;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import de.mephisto.vpin.ui.achievements.AchievementsSessionsDialog;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.util.Map;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Tables tile: wheel, name, meta data and progress of the selected table.
 */
public class TableSummaryTileController {

  @FXML
  private StackPane wheelBox;
  @FXML
  private Label nameLabel;
  @FXML
  private Label metaLabel;
  @FXML
  private VBox progressBox;
  @FXML
  private Label countLabel;
  @FXML
  private Label pctLabel;
  @FXML
  private StackPane progressBarBox;
  @FXML
  private HBox distributionBox;
  @FXML
  private VBox buttonsBox;
  @FXML
  private Button leaderboardBtn;
  @FXML
  private Button sessionsBtn;

  private static final int WHEEL_SIZE = 112;

  private AchievementsController controller;
  private TableAchievements table;

  public void setAchievementsController(AchievementsController controller) {
    this.controller = controller;
  }

  public void render(TableAchievements t, boolean global, @Nullable TableAchievementsDetails details) {
    this.table = t;
    renderWheel(t, global);

    nameLabel.setText(t.gameName);
    show(metaLabel, !global);
    if (!global) {
      String vps = t.vpsTableId != null ? Messages.get("achievements.vps_matched") : Messages.get("achievements.vps_missing");
      String last = t.lastPlayed != null ? Messages.get("achievements.last_played", date(t.lastPlayed)) : Messages.get("achievements.never_played");
      metaLabel.setText("ROM " + t.rom + (t.mapRom != null && !t.mapRom.equalsIgnoreCase(t.rom) ? " (map " + t.mapRom + ")" : "") + " · " + vps + " · " + last);
    }

    double pct = t.total > 0 ? t.unlocked * 100d / t.total : 0;
    countLabel.setText(Messages.get("achievements.count", t.unlocked, t.total));
    pctLabel.setText(pct(pct));
    progressBarBox.getChildren().setAll(progress(pct, ORANGE, 8));
    distributionBox.getChildren().clear();
    boolean distribution = details != null && details.rarityAvailable;
    if (distribution) {
      for (Map.Entry<String, Integer> e : details.rarityDistribution.entrySet()) {
        HBox item = new HBox(4);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getChildren().addAll(dot(RARITY_COLORS.getOrDefault(e.getKey(), MUTED), 4), colored(e.getKey() + " " + e.getValue(), MUTED, 12, false));
        distributionBox.getChildren().add(item);
      }
    }
    show(distributionBox, distribution);
    show(progressBox, t.hasMap && (global || t.total > 0));

    show(buttonsBox, !global && t.gameId > 0);
    show(leaderboardBtn, t.hasMap && t.total > 0 && isManiaAvailable());
    show(sessionsBtn, t.hasMap && t.lastPlayed != null);
  }

  private void renderWheel(TableAchievements t, boolean global) {
    wheelBox.getChildren().clear();
    if (global) {
      wheelBox.setStyle("");
      wheelBox.getChildren().add(icon("mdi2t-trophy-variant", ORANGE, WHEEL_SIZE));
      return;
    }
    wheelBox.setStyle("");
    wheelBox.getChildren().add(new ImageView(new Image(Studio.class.getResourceAsStream("avatar-blank.png"), WHEEL_SIZE, WHEEL_SIZE, true, true)));
    int gameId = t.gameId;
    JFXFuture.supplyAsync(() -> {
          try (InputStream in = client.getWheelIcon(gameId, true)) {
            return in != null ? new Image(in, WHEEL_SIZE, WHEEL_SIZE, true, true) : null;
          }
          catch (Exception e) {
            return null;
          }
        })
        .thenAcceptLater(img -> {
          if (img != null && !img.isError() && table == t) {
            wheelBox.getChildren().setAll(new ImageView(img));
          }
        });
  }

  @FXML
  private void onOpen() {
    if (table != null) {
      NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(table.gameId));
    }
  }

  @FXML
  private void onSessions() {
    PlayerAchievements p = controller.getCurrent();
    if (table != null && p != null) {
      AchievementsSessionsDialog.open(p.playerId, table.rom, table.gameName);
    }
  }

  @FXML
  private void onLeaderboard() {
    if (table != null) {
      controller.showLeaderboard(table.rom);
    }
  }
}
