package de.mephisto.vpin.ui.achievements.leaderboards;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.AchievementBoard;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboard;
import de.mephisto.vpin.connectors.mania.model.AchievementLeaderboardEntry;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievements;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Leaderboards tile: table search, recently played tables and the top 25 of the selected table.
 */
public class LeaderboardsTableTileController implements Initializable {

  @FXML
  private TextField searchField;
  @FXML
  private Button clearBtn;
  @FXML
  private ListView<AchievementBoard> suggestions;
  @FXML
  private FlowPane chipsPane;
  @FXML
  private HBox infoPanel;
  @FXML
  private Label tableNameLabel;
  @FXML
  private Label tableMetaLabel;
  @FXML
  private Label ownRankLabel;
  @FXML
  private Label ownProgressLabel;
  @FXML
  private Hyperlink vpsLink;
  @FXML
  private Label messageLabel;
  @FXML
  private VBox rankingBox;
  @FXML
  private VBox rowsBox;

  private AchievementsController controller;
  private String selectedRom;
  private String vpsTableId;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    searchField.textProperty().addListener((obs, o, n) -> clearBtn.setVisible(n != null && !n.isEmpty()));
    suggestions.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(AchievementBoard b, boolean empty) {
        super.updateItem(b, empty);
        setText(empty || b == null ? null : (b.tableName != null ? b.tableName : b.rom) + "  (" + b.rom + ", " + b.players + " " + Messages.get("achievements.players") + ")");
      }
    });
    suggestions.setOnMouseClicked(e -> {
      AchievementBoard b = suggestions.getSelectionModel().getSelectedItem();
      if (b != null) {
        show(suggestions, false);
        selectRom(b.rom);
      }
    });
  }

  public void setAchievementsController(AchievementsController controller) {
    this.controller = controller;
  }

  public void selectRom(String rom) {
    selectedRom = rom;
    reload();
  }

  public void clear() {
    show(infoPanel, false);
    show(messageLabel, false);
    show(rankingBox, false);
  }

  /**
   * Reloads the leaderboard of the selected table, if any.
   */
  public void reload() {
    if (selectedRom == null || !isManiaAvailable()) {
      return;
    }
    String rom = selectedRom;
    String uuid = ownUuid();
    showMessage(Messages.get("achievements.loading"));
    JFXFuture.supplyAsync(() -> Studio.maniaClient.getAchievementClient().getRomLeaderboard(rom, 25, uuid))
        .thenAcceptLater((AchievementLeaderboard board) -> {
          if (rom.equals(selectedRom)) {
            render(board, rom, uuid);
          }
        })
        .onErrorLater(e -> showMessage(Messages.get("achievements.cloud_unavailable")));
  }

  public void loadChips(@Nullable PlayerAchievements p) {
    chipsPane.getChildren().clear();
    if (p == null) {
      return;
    }
    long playerId = p.playerId;
    JFXFuture.supplyAsync(() -> client.getAchievementsService().getTables(playerId))
        .thenAcceptLater(tables -> {
          List<TableAchievements> played = new ArrayList<>();
          for (TableAchievements t : tables) {
            if (t.lastPlayed != null && t.hasMap) {
              played.add(t);
            }
          }
          for (TableAchievements t : played.subList(0, Math.min(6, played.size()))) {
            Button chip = new Button(t.gameName);
            chip.setStyle("-fx-font-size: 14px;");
            chip.setOnAction(e -> selectRom(t.rom));
            chipsPane.getChildren().add(chip);
          }
          if (selectedRom == null && !played.isEmpty()) {
            selectRom(played.get(0).rom);
          }
        });
  }

  @Nullable
  private String ownUuid() {
    PlayerAchievements p = controller.getCurrent();
    return p != null ? p.maniaAccountUuid : null;
  }

  @FXML
  private void onClear() {
    searchField.setText("");
    show(suggestions, false);
  }

  @FXML
  private void onSearch() {
    String term = searchField.getText() == null ? "" : searchField.getText().trim();
    if (term.length() < 2 || !isManiaAvailable()) {
      return;
    }
    JFXFuture.supplyAsync(() -> Studio.maniaClient.getAchievementClient().searchBoards(term))
        .thenAcceptLater(boards -> {
          if (boards.size() == 1) {
            show(suggestions, false);
            selectRom(boards.get(0).rom);
            return;
          }
          suggestions.getItems().setAll(boards);
          show(suggestions, !boards.isEmpty());
          if (boards.isEmpty()) {
            // the ROM may be typed directly
            selectRom(term.toLowerCase());
          }
        });
  }

  @FXML
  private void onVps() {
    if (vpsTableId != null) {
      Studio.browse(VPS.getVpsTableUrl(vpsTableId));
    }
  }

  private void showMessage(String message) {
    show(infoPanel, false);
    show(rankingBox, false);
    messageLabel.setText(message);
    show(messageLabel, true);
  }

  private void render(@Nullable AchievementLeaderboard board, String rom, @Nullable String uuid) {
    if (board == null) {
      showMessage(Messages.get("achievements.cloud_unavailable"));
      return;
    }
    tableNameLabel.setText(board.tableName != null ? board.tableName : rom);
    tableMetaLabel.setText("ROM " + rom + " · " + Messages.get("achievements.count_achievements", board.total) + " · " + board.players + " " + Messages.get("achievements.players"));
    ownRankLabel.setText(board.ownRank != null ? "#" + board.ownRank : "—");
    ownProgressLabel.setText(board.ownEntry != null ? pct(board.ownEntry.percentage) : "—");
    vpsTableId = board.vpsTableId;
    show(vpsLink, vpsTableId != null);
    show(infoPanel, true);

    if (board.entries.isEmpty()) {
      messageLabel.setText(Messages.get("achievements.no_cloud_data"));
      show(messageLabel, true);
      show(rankingBox, false);
      return;
    }
    show(messageLabel, false);
    show(rankingBox, true);
    rowsBox.getChildren().clear();
    int i = 0;
    boolean ownListed = false;
    for (AchievementLeaderboardEntry e : board.entries) {
      boolean own = uuid != null && uuid.equals(e.accountUuid);
      ownListed |= own;
      rowsBox.getChildren().add(tableRow(e, i++, own));
    }
    if (!ownListed && board.ownEntry != null) {
      HBox own = tableRow(board.ownEntry, 0, true);
      VBox.setMargin(own, new Insets(6, 0, 0, 0));
      rowsBox.getChildren().add(own);
    }
  }

  private HBox tableRow(AchievementLeaderboardEntry e, int index, boolean own) {
    HBox row = row(index, own);
    HBox rank = new HBox(rank(e.rank));
    rank.setMinWidth(56);
    HBox player = LeaderboardPlayerCell.create(e, own, controller.getCurrent());
    player.setMinWidth(220);
    player.setPrefWidth(220);
    HBox progress = new HBox(8);
    progress.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(progress, Priority.ALWAYS);
    Label text = colored(e.unlocked + " / " + e.total + " (" + pct(e.percentage) + ")", "white", 12, false);
    text.setMinWidth(130);
    progress.getChildren().addAll(progress(e.percentage, ORANGE, 6), text);
    Label date = muted(date(e.updatedAt));
    date.setMinWidth(100);
    row.getChildren().addAll(rank, player, progress, date);
    return row;
  }
}
