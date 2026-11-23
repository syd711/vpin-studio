package de.mephisto.vpin.ui.achievements.badges;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.BadgeInfo;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Badges tile: the selected badge and all badges grouped by category.
 */
public class BadgesGridTileController implements Initializable {

  @FXML
  private VBox root;
  @FXML
  private Label countLabel;
  @FXML
  private ToggleGroup filterGroup;
  @FXML
  private ToggleButton earnedBtn;
  @FXML
  private ToggleButton lockedBtn;
  @FXML
  private HBox selectedPanel;
  @FXML
  private StackPane selectedMedalBox;
  @FXML
  private Label selectedNameLabel;
  @FXML
  private Label selectedDescriptionLabel;
  @FXML
  private Label selectedStateLabel;
  @FXML
  private Label displayBadgeLabel;
  @FXML
  private Button showBadgeBtn;
  @FXML
  private Button hideBadgeBtn;
  @FXML
  private VBox groupsBox;

  private PlayerAchievements player;
  private BadgeInfo selected;
  private String selectedId;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    filterGroup.selectedToggleProperty().addListener((obs, o, n) -> {
      if (n == null) {
        o.setSelected(true);
      }
      renderBadges();
    });
  }

  public void render(@Nullable PlayerAchievements p) {
    this.player = p;
    renderBadges();
  }

  private void renderBadges() {
    root.setVisible(player != null);
    groupsBox.getChildren().clear();
    if (player == null) {
      return;
    }
    countLabel.setText(player.badgesEarned + " / " + player.badgesTotal);

    selected = null;
    for (BadgeInfo b : player.badges) {
      if (b.id.equals(selectedId) || (selectedId == null && b.selected)) {
        selected = b;
      }
    }
    if (selected == null && !player.badges.isEmpty()) {
      selected = player.badges.stream().filter(b -> b.earned).findFirst().orElse(player.badges.get(0));
    }
    show(selectedPanel, selected != null);
    if (selected != null) {
      renderSelected(selected);
    }

    Map<String, List<BadgeInfo>> groups = new LinkedHashMap<>();
    for (BadgeInfo b : player.badges) {
      if (filterGroup.getSelectedToggle() == earnedBtn && !b.earned) continue;
      if (filterGroup.getSelectedToggle() == lockedBtn && b.earned) continue;
      groups.computeIfAbsent(b.category, c -> new ArrayList<>()).add(b);
    }
    String selId = selected != null ? selected.id : null;
    for (Map.Entry<String, List<BadgeInfo>> g : groups.entrySet()) {
      BadgeGroupController group = BadgeGroupController.load();
      group.render(g.getKey(), g.getValue(), selId, b -> {
        selectedId = b.id;
        renderBadges();
      });
      groupsBox.getChildren().add(group.getRoot());
    }
  }

  private void renderSelected(BadgeInfo b) {
    selectedMedalBox.getChildren().setAll(medal(b.category, b.earned, 48));
    selectedNameLabel.setText(b.name);
    selectedDescriptionLabel.setText(b.description);
    String state = b.earned ? (b.selected ? Messages.get("achievements.badge_shown") : Messages.get("achievements.badge_earned"))
        : Messages.get("achievements.badge_locked", b.progressText);
    selectedStateLabel.setText(state);
    selectedStateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (b.earned ? GREEN : ORANGE) + ";");
    String display = player.selectedBadge != null
        ? player.badges.stream().filter(x -> x.id.equals(player.selectedBadge)).map(x -> x.name).findFirst().orElse("-") : "-";
    displayBadgeLabel.setText(Messages.get("achievements.display_badge", display));
    show(showBadgeBtn, b.earned && !b.selected);
    show(hideBadgeBtn, b.selected);
  }

  @FXML
  private void onShowBadge() {
    if (selected != null) {
      selectBadge(selected.id);
    }
  }

  @FXML
  private void onHideBadge() {
    selectBadge(null);
  }

  private void selectBadge(@Nullable String badgeId) {
    if (player == null) {
      return;
    }
    PlayerAchievements p = player;
    JFXFuture.supplyAsync(() -> client.getAchievementsService().selectBadge(p.playerId, badgeId))
        .thenAcceptLater(ok -> {
          if (!ok) {
            return;
          }
          p.selectedBadge = badgeId;
          for (BadgeInfo b : p.badges) {
            b.selected = b.id.equals(badgeId);
          }
          renderBadges();
        });
  }
}
