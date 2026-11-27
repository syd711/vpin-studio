package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.commons.fx.widgets.WidgetPlayerRankController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboard;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboardEntry;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import de.mephisto.vpin.ui.util.SegmentedToggles;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Overview tile: player ranking of the cabinet (commons widget) or the cloud top 5.
 */
public class OverviewRankingTileController {

  @FXML
  private ToggleButton cabinetBtn;
  @FXML
  private ToggleButton cloudBtn;
  @FXML
  private Node playerRank;
  @FXML
  private WidgetPlayerRankController playerRankController;
  @FXML
  private VBox cloudBox;

  private AchievementsController controller;
  private PlayerAchievements player;
  private boolean cloudRanking = false;

  @FXML
  private void initialize() {
    // the tile renders the title next to the toggle buttons
    if (playerRank instanceof BorderPane widgetRoot) {
      widgetRoot.setTop(null);
      widgetRoot.setPadding(Insets.EMPTY);
    }
    playerRankController.useStudioStyle();
    VBox.setVgrow(cloudBox, Priority.ALWAYS);
    SegmentedToggles.keepSelected(cabinetBtn, cloudBtn);
  }

  public void setAchievementsController(AchievementsController controller) {
    this.controller = controller;
  }

  public void render(PlayerAchievements p) {
    this.player = p;
    boolean cloud = controller.isCloudAvailable() || controller.isRegistrationRequired();
    cloudBtn.setDisable(!cloud);
    if (cloudRanking && cloud) {
      showCloudRanking();
    }
    else {
      showCabinetRanking();
    }
  }

  @FXML
  private void onCabinet() {
    cloudRanking = false;
    showCabinetRanking();
  }

  @FXML
  private void onCloud() {
    cloudRanking = true;
    showCloudRanking();
  }

  private void showCabinetRanking() {
    cabinetBtn.setSelected(true);
    show(cloudBox, false);
    show(playerRank, true);
    playerRankController.refresh();
  }

  private void showCloudRanking() {
    cloudBtn.setSelected(true);
    show(playerRank, false);
    show(cloudBox, true);
    if (controller.isRegistrationRequired()) {
      cloudBox.getChildren().setAll(registrationBox(controller));
      return;
    }
    if (player == null) {
      return;
    }
    PlayerAchievements p = player;
    String uuid = p.maniaAccountUuid;
    cloudBox.getChildren().setAll(muted(Messages.get("achievements.loading")));
    JFXFuture.supplyAsync(() -> Studio.maniaClient.getAchievementClient().getGlobalLeaderboard(5, uuid))
        .thenAcceptLater((AchievementLeaderboard board) -> {
          if (player != p) {
            return;
          }
          if (board == null || board.entries.isEmpty()) {
            cloudBox.getChildren().setAll(muted(Messages.get("achievements.no_cloud_data")));
            return;
          }
          List<AchievementLeaderboardEntry> entries = new ArrayList<>(board.entries);
          AchievementLeaderboardEntry own = entries.stream().filter(e -> isOwn(e, uuid)).findFirst().orElse(null);
          if (own == null && board.ownEntry != null) {
            own = board.ownEntry;
            entries.add(own);
          }
          cloudBox.getChildren().setAll(cloudTable(entries, own));
        })
        .onErrorLater(e -> cloudBox.getChildren().setAll(muted(Messages.get("achievements.cloud_unavailable"))));
  }

  private static boolean isOwn(AchievementLeaderboardEntry e, String uuid) {
    return uuid != null && uuid.equals(e.accountUuid);
  }

  /**
   * Cloud top 5 in the same flat table look as the cabinet ranking; the own entry is shown as the selected row
   * (appended below the top 5 when it is not part of it).
   */
  private TableView<AchievementLeaderboardEntry> cloudTable(List<AchievementLeaderboardEntry> entries, AchievementLeaderboardEntry own) {
    TableView<AchievementLeaderboardEntry> table = new TableView<>(FXCollections.observableList(entries));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    VBox.setVgrow(table, Priority.ALWAYS);

    TableColumn<AchievementLeaderboardEntry, Node> rankColumn = fixedColumn(Messages.get("achievements.rank"), 56);
    rankColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(bold("#" + c.getValue().rank)));

    TableColumn<AchievementLeaderboardEntry, Node> nameColumn = new TableColumn<>(Messages.get("achievements.player"));
    nameColumn.setPrefWidth(220);
    nameColumn.setCellValueFactory(c -> {
      AchievementLeaderboardEntry e = c.getValue();
      Label name = text(e.displayName != null ? e.displayName : e.initials);
      HBox box = new HBox(8, initials(e.initials, 32), name);
      box.setAlignment(Pos.CENTER_LEFT);
      return new SimpleObjectProperty<>(box);
    });

    TableColumn<AchievementLeaderboardEntry, Node> levelColumn = fixedColumn(Messages.get("achievements.lvl"), 56);
    levelColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(text(String.valueOf(c.getValue().level))));

    TableColumn<AchievementLeaderboardEntry, Node> countColumn = fixedColumn(Messages.get("achievements.unlocked"), 96);
    countColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(bold(number(c.getValue().count))));

    table.getColumns().setAll(List.of(rankColumn, nameColumn, levelColumn, countColumn));
    if (own != null) {
      table.getSelectionModel().select(own);
    }
    return table;
  }

  private static TableColumn<AchievementLeaderboardEntry, Node> fixedColumn(String title, double width) {
    TableColumn<AchievementLeaderboardEntry, Node> column = new TableColumn<>(title);
    column.setMinWidth(width);
    column.setPrefWidth(width);
    column.setMaxWidth(width);
    column.setStyle("-fx-alignment: CENTER;");
    return column;
  }
}
