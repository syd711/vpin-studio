package de.mephisto.vpin.ui.achievements.tables;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievementsDetails;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import de.mephisto.vpin.ui.achievements.AchievementsTab;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.achievements.AchievementsUI.show;

/**
 * A2 - Tables: per table progress, achievements of the selected table and the cloud top 5.
 */
public class TablesTabController implements AchievementsTab, Initializable {

  /**
   * Pseudo ROM of the list entry for the global (non table) achievements.
   */
  public static final String GLOBAL = "__global__";

  @FXML
  private Label messageLabel;
  @FXML
  private Node summaryTile;
  @FXML
  private Node infoTile;
  @FXML
  private HBox lowerBox;
  @FXML
  private Node top5Tile;

  @FXML
  private TableListTileController tableListTileController;
  @FXML
  private TableSummaryTileController summaryTileController;
  @FXML
  private TableInfoTileController infoTileController;
  @FXML
  private TableAchievementsTileController achievementsTileController;
  @FXML
  private TableTop5TileController top5TileController;

  private AchievementsController controller;
  private TableAchievementsDetails details;
  private boolean dirty = true;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableListTileController.setOnSelect(this::loadDetails);
  }

  @Override
  public void setAchievementsController(AchievementsController controller) {
    this.controller = controller;
    summaryTileController.setAchievementsController(controller);
    top5TileController.setAchievementsController(controller);
  }

  @Override
  public void setDirty() {
    dirty = true;
  }

  public ReadOnlyObjectProperty<TableAchievements> selectedTableProperty() {
    return tableListTileController.selectedTableProperty();
  }

  public void selectRom(String rom) {
    tableListTileController.selectRom(rom);
  }

  @Override
  public void refresh(@Nullable PlayerAchievements p) {
    if (!dirty) {
      return;
    }
    dirty = false;
    if (p == null) {
      tableListTileController.setTables(new ArrayList<>());
      showMessage(Messages.get("achievements.no_players"));
      return;
    }
    showMessage(Messages.get("achievements.loading"));
    long playerId = p.playerId;
    JFXFuture.supplyAsync(() -> client.getAchievementsService().getTables(playerId))
        .thenAcceptLater(result -> {
          List<TableAchievements> tables = new ArrayList<>();
          TableAchievements global = new TableAchievements();
          global.rom = GLOBAL;
          global.gameName = Messages.get("achievements.global_achievements");
          global.hasMap = true;
          global.status = TableAchievements.STATUS_IN_PROGRESS;
          tables.add(global);
          tables.addAll(result);
          tableListTileController.setTables(tables);
        })
        .onErrorLater(e -> showMessage(e.getMessage()));
  }

  private void loadDetails(TableAchievements table) {
    PlayerAchievements p = controller.getCurrent();
    if (p == null) {
      return;
    }
    if (!table.hasMap) {
      details = new TableAchievementsDetails();
      details.table = table;
      renderDetails();
      return;
    }
    long playerId = p.playerId;
    JFXFuture.supplyAsync(() -> client.getAchievementsService().getTable(playerId, table.rom))
        .thenAcceptLater(d -> {
          details = d;
          if (details != null && details.table == null) {
            details.table = table;
          }
          renderDetails();
        })
        .onErrorLater(e -> showMessage(e.getMessage()));
  }

  private void renderDetails() {
    show(messageLabel, false);
    if (details == null || details.table == null) {
      showTiles(false, false, false);
      return;
    }
    TableAchievements t = details.table;
    boolean global = GLOBAL.equals(t.rom);
    summaryTileController.render(t, global, details);

    if (t.hasMap && !global && t.total == 0) {
      infoTileController.render(Messages.get("achievements.no_rules_title"), Messages.get("achievements.no_rules_text", t.rom));
      showTiles(true, true, false);
      return;
    }
    if (!t.hasMap) {
      infoTileController.render(Messages.get("achievements.no_map_title"), Messages.get("achievements.no_map_text", t.rom));
      showTiles(true, true, false);
      return;
    }

    showTiles(true, false, true);
    achievementsTileController.render(details);
    show(top5Tile, !global);
    if (!global) {
      top5TileController.load(t.rom, controller.getCurrent());
    }
  }

  private void showMessage(@Nullable String message) {
    messageLabel.setText(message);
    show(messageLabel, true);
    showTiles(false, false, false);
  }

  private void showTiles(boolean summary, boolean info, boolean lower) {
    show(summaryTile, summary);
    show(infoTile, info);
    show(lowerBox, lower);
  }
}
