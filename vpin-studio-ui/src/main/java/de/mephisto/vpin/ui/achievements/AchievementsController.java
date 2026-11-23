package de.mephisto.vpin.ui.achievements;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.AchievementsSyncResult;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.achievements.badges.BadgesTabController;
import de.mephisto.vpin.ui.achievements.leaderboards.LeaderboardsTabController;
import de.mephisto.vpin.ui.achievements.overview.OverviewTabController;
import de.mephisto.vpin.ui.achievements.tables.TablesTabController;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

/**
 * The achievements section (ported VPX Achievement Watcher), replaces the former dashboard.
 */
public class AchievementsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(AchievementsController.class);

  @FXML
  private TabPane tabPane;
  @FXML
  private Tab overviewTab;
  @FXML
  private Tab tablesTab;
  @FXML
  private Tab badgesTab;
  @FXML
  private Tab leaderboardsTab;
  @FXML
  private BorderPane overviewPane;
  @FXML
  private BorderPane tablesPane;
  @FXML
  private BorderPane badgesPane;
  @FXML
  private BorderPane leaderboardsPane;
  @FXML
  private ToolBar toolbar;
  @FXML
  private ComboBox<PlayerAchievements> playerCombo;
  @FXML
  private Separator syncSeparator;
  @FXML
  private Button syncBtn;
  @FXML
  private Label statusLabel;

  @FXML
  private OverviewTabController overviewController;
  @FXML
  private TablesTabController tablesController;
  @FXML
  private BadgesTabController badgesController;
  @FXML
  private LeaderboardsTabController leaderboardsController;

  private PlayerAchievements current;
  private boolean updatingCombo;
  private volatile boolean maniaRegistered = true;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    for (AchievementsTab tab : tabs()) {
      tab.setAchievementsController(this);
    }

    syncBtn.setVisible(Features.MANIA_ENABLED);
    syncBtn.setManaged(Features.MANIA_ENABLED);
    syncSeparator.setVisible(Features.MANIA_ENABLED);
    syncSeparator.setManaged(Features.MANIA_ENABLED);

    playerCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(PlayerAchievements p) {
        if (p == null) {
          return "";
        }
        return p.name + (p.initials != null ? " (" + p.initials + ")" : "") + (p.tracked ? "" : " – " + Messages.get("achievements.not_tracked"));
      }

      @Override
      public PlayerAchievements fromString(String s) {
        return null;
      }
    });
    playerCombo.valueProperty().addListener((obs, o, n) -> {
      if (!updatingCombo && n != null) {
        loadPlayer(n.playerId);
      }
    });
    tabPane.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
      attachToolbar();
      refreshSelectedTab();
    });
    attachToolbar();
  }

  /**
   * The toolbar is shared by all tabs, so it is moved into the top of the currently selected tab.
   */
  private void attachToolbar() {
    Tab tab = tabPane.getSelectionModel().getSelectedItem();
    BorderPane pane = tab == tablesTab ? tablesPane
        : tab == badgesTab ? badgesPane
        : tab == leaderboardsTab ? leaderboardsPane
        : overviewPane;
    for (BorderPane p : List.of(overviewPane, tablesPane, badgesPane, leaderboardsPane)) {
      if (p != pane && p.getTop() == toolbar) {
        p.setTop(null);
      }
    }
    if (pane.getTop() != toolbar) {
      pane.setTop(toolbar);
    }
  }

  @Override
  public void onViewActivated(@Nullable NavigationOptions options) {
    NavigationController.setBreadCrumb(List.of(Messages.get("navigation.achievements")));
    if (options != null && options.getModel() instanceof String rom) {
      showTable(rom);
    }
    reload();
  }

  public void reload() {
    Long selectedId = current != null ? current.playerId : null;
    JFXFuture.supplyAsync(() -> {
          // the cabinet registration check is blocking, so it is done once per reload in the background
          maniaRegistered = !AchievementsUI.isManiaAvailable() || ManiaHelper.isRegistered();
          return client.getAchievementsService().getPlayers();
        })
        .thenAcceptLater(players -> {
          updatingCombo = true;
          playerCombo.getItems().setAll(players);
          PlayerAchievements select = null;
          for (PlayerAchievements p : players) {
            if (selectedId != null && p.playerId == selectedId) {
              select = p;
            }
          }
          if (select == null) {
            select = players.stream().filter(p -> p.tracked).findFirst().orElse(players.isEmpty() ? null : players.get(0));
          }
          playerCombo.setValue(select);
          updatingCombo = false;
          if (select != null) {
            loadPlayer(select.playerId);
          }
          else {
            current = null;
            statusLabel.setText(Messages.get("achievements.no_players"));
            refreshSelectedTab();
          }
        })
        .onErrorLater(e -> LOG.error("Failed to load achievement players: {}", e.getMessage(), e));
  }

  private void loadPlayer(long playerId) {
    statusLabel.setText(Messages.get("achievements.loading"));
    JFXFuture.supplyAsync(() -> client.getAchievementsService().getPlayer(playerId))
        .thenAcceptLater(p -> {
          current = p;
          updateStatus();
          for (AchievementsTab tab : tabs()) {
            tab.setDirty();
          }
          refreshSelectedTab();
        })
        .onErrorLater(e -> {
          statusLabel.setText(e.getMessage());
          LOG.error("Failed to load achievements: {}", e.getMessage(), e);
        });
  }

  private void updateStatus() {
    if (current == null) {
      statusLabel.setText("");
      return;
    }
    StringBuilder sb = new StringBuilder();
    if (!current.tracked) {
      sb.append(Messages.get("achievements.status.not_tracked"));
    }
    else if (current.maniaAccountUuid == null) {
      sb.append(Messages.get("achievements.status.local_only"));
    }
    else if (current.pendingUploads > 0) {
      sb.append(Messages.get("achievements.status.pending", current.pendingUploads));
    }
    else if (current.lastSync != null) {
      sb.append(Messages.get("achievements.status.synced", AchievementsUI.date(current.lastSync)));
    }
    statusLabel.setText(sb.toString());
  }

  private void refreshSelectedTab() {
    Tab tab = tabPane.getSelectionModel().getSelectedItem();
    if (tab == overviewTab) {
      overviewController.refresh(current);
    }
    else if (tab == tablesTab) {
      tablesController.refresh(current);
    }
    else if (tab == badgesTab) {
      badgesController.refresh(current);
    }
    else if (tab == leaderboardsTab) {
      leaderboardsController.refresh(current);
    }
  }

  private List<AchievementsTab> tabs() {
    return List.of(overviewController, tablesController, badgesController, leaderboardsController);
  }

  public void showTab(int index) {
    tabPane.getSelectionModel().select(index);
  }

  public void showTable(String rom) {
    tablesController.selectRom(rom);
    tabPane.getSelectionModel().select(tablesTab);
  }

  public void showLeaderboard(String rom) {
    leaderboardsController.selectRom(rom);
    tabPane.getSelectionModel().select(leaderboardsTab);
  }

  @Nullable
  public PlayerAchievements getCurrent() {
    return current;
  }

  /**
   * True if VPin Mania is enabled but the cabinet has not been registered yet.
   */
  public boolean isRegistrationRequired() {
    return AchievementsUI.isManiaAvailable() && !maniaRegistered;
  }

  /**
   * Opens the VPin Mania cabinet registration (same as the Mania button of the header) and reloads all tabs on success.
   */
  public void registerCabinet() {
    if (ManiaHelper.register()) {
      maniaRegistered = true;
      for (AchievementsTab tab : tabs()) {
        tab.setDirty();
      }
      reload();
    }
  }

  public boolean isCloudAvailable() {
    return Features.MANIA_ENABLED && Studio.maniaClient != null && current != null && current.maniaAccountUuid != null;
  }

  @FXML
  private void onSync() {
    syncBtn.setDisable(true);
    statusLabel.setText(Messages.get("achievements.syncing"));
    JFXFuture.supplyAsync(() -> client.getAchievementsService().sync())
        .thenAcceptLater((AchievementsSyncResult result) -> {
          syncBtn.setDisable(false);
          statusLabel.setText(result != null ? result.message : "");
          if (current != null) {
            Platform.runLater(() -> loadPlayer(current.playerId));
          }
        })
        .onErrorLater(e -> {
          syncBtn.setDisable(false);
          statusLabel.setText(e.getMessage());
        });
  }

  @FXML
  private void onReload() {
    reload();
  }

  @FXML
  private void onSettings() {
    PreferencesController.open("achievements");
  }
}
