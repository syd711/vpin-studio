package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.AchievementInfo;
import de.mephisto.vpin.restclient.achievements.AchievementSessionDetails;
import de.mephisto.vpin.restclient.achievements.AchievementSessionInfo;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.restclient.achievements.SessionHighlight;
import de.mephisto.vpin.restclient.achievements.TableAchievementsDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.fx.ServerFX.client;

/**
 * Pause menu entry "Achievements": progress of the tracked player on the table, highlights and unlocks of the last
 * tracked game and the next achievements. The running game is evaluated when the table is exited, so it is not
 * part of this view.
 */
public class AchievementsMenuItemController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String ORANGE = "#DD9646";
  private static final String BLUE = "#7FB2FF";
  private static final String GREEN = "#5CC46A";
  private static final String MUTED = "#BDBDBD";
  private static final Map<String, String> CATEGORY_COLORS = Map.of("Power", ORANGE, "Precision", BLUE, "Fun", GREEN);
  private static final int MAX_LINES = 5;

  @FXML
  private VBox root;

  private record Data(PlayerAchievements player, TableAchievementsDetails table, @Nullable AchievementSessionDetails lastGame) {
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }

  public void setData(@NonNull GameRepresentation game) {
    ProgressIndicator indicator = new ProgressIndicator();
    indicator.setPrefSize(200, 200);
    StackPane loading = new StackPane(indicator);
    VBox.setVgrow(loading, Priority.ALWAYS);
    root.getChildren().setAll(loading);

    String rom = romOf(game);
    JFXFuture.supplyAsync(() -> load(rom))
        .thenAcceptLater(data -> render(game, data))
        .onErrorLater(e -> {
          LOG.error("Failed to load achievements for the pause menu: {}", e.getMessage(), e);
          root.getChildren().setAll(text(Messages.get("pausemenu.achievements.error"), MUTED, 24, false));
        });
  }

  @Nullable
  public static String romOf(@NonNull GameRepresentation game) {
    return StringUtils.isEmpty(game.getRom()) ? null : game.getRom().trim().toLowerCase();
  }

  /**
   * The player shown in the pause menu: the administrative player if achievements are tracked for it, else the
   * first tracked player.
   */
  @Nullable
  public static PlayerRepresentation trackedPlayer() {
    List<PlayerRepresentation> players = client.getPlayerService().getPlayers();
    return players.stream().filter(p -> p.isAchievementsEnabled() && p.isAdministrative()).findFirst()
        .orElse(players.stream().filter(PlayerRepresentation::isAchievementsEnabled).findFirst().orElse(null));
  }

  @Nullable
  private static Data load(@Nullable String rom) {
    PlayerRepresentation p = trackedPlayer();
    if (p == null || rom == null) {
      return null;
    }
    PlayerAchievements player = client.getAchievementsService().getPlayer(p.getId());
    TableAchievementsDetails table = client.getAchievementsService().getTable(p.getId(), rom);
    AchievementSessionDetails lastGame = null;
    List<AchievementSessionInfo> sessions = client.getAchievementsService().getSessions(p.getId(), rom, 1);
    if (!sessions.isEmpty()) {
      lastGame = client.getAchievementsService().getSession(p.getId(), sessions.get(0).id);
    }
    return new Data(player, table, lastGame);
  }

  private void render(GameRepresentation game, @Nullable Data data) {
    root.getChildren().clear();

    ImageView icon = new ImageView(new Image(PauseMenu.class.getResourceAsStream("achievements.png")));
    icon.setFitWidth(130);
    icon.setFitHeight(130);
    icon.setPreserveRatio(true);
    VBox headerText = new VBox(4, text(game.getGameDisplayName(), "white", 36, true));
    HBox header = new HBox(24, icon, headerText);
    header.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(headerText, Priority.ALWAYS);
    root.getChildren().add(header);

    if (data == null || data.player() == null) {
      headerText.getChildren().add(text(Messages.get("pausemenu.achievements.no_player"), MUTED, 24, false));
      return;
    }
    PlayerAchievements player = data.player();
    String levelName = player.level != null && player.level.name != null ? player.level.name : "";
    headerText.getChildren().add(text(player.name + " · " + Messages.get("pausemenu.achievements.level",
        player.level != null ? player.level.level : 1, levelName), MUTED, 24, false));

    TableAchievementsDetails details = data.table();
    if (details == null || details.table == null || !details.table.hasMap || details.table.total == 0) {
      headerText.getChildren().add(text(Messages.get("pausemenu.achievements.no_achievements"), MUTED, 22, false));
      return;
    }
    int unlocked = details.table.unlocked;
    int total = details.table.total;
    double pct = unlocked * 100d / total;
    headerText.getChildren().add(text(Messages.get("pausemenu.achievements.progress", unlocked, total,
        String.format(Locale.ENGLISH, "%.1f", pct)), "white", 24, true));
    headerText.getChildren().add(progress(pct));

    AchievementSessionDetails last = data.lastGame();
    if (last == null || last.session == null) {
      root.getChildren().add(panel(text(Messages.get("pausemenu.achievements.no_game"), MUTED, 22, false)));
    }
    else {
      AchievementSessionInfo s = last.session;
      String info = Messages.get("pausemenu.achievements.last_game", dateTime(s.startedAt), duration(s.durationSec))
          + (s.score != null ? " · " + Messages.get("pausemenu.achievements.score", number(s.score)) : "");
      HBox columns = new HBox(18);
      for (Map.Entry<String, List<SessionHighlight>> e : last.highlights.entrySet()) {
        columns.getChildren().add(highlightColumn(e.getKey(), e.getValue()));
      }
      root.getChildren().add(panel(text(info, "white", 24, true), columns));
    }

    // unlocked in the last game | next achievements of the table
    VBox unlockedBox = new VBox(6, text(Messages.get("pausemenu.achievements.unlocked_last_game"), ORANGE, 24, true));
    if (last == null || last.unlocks.isEmpty()) {
      unlockedBox.getChildren().add(text("—", MUTED, 22, false));
    }
    else {
      last.unlocks.stream().limit(MAX_LINES).forEach(a -> unlockedBox.getChildren().add(text(title(a), "white", 22, false)));
      if (last.unlocks.size() > MAX_LINES) {
        unlockedBox.getChildren().add(text(Messages.get("pausemenu.achievements.more", last.unlocks.size() - MAX_LINES), MUTED, 20, false));
      }
    }

    VBox nextBox = new VBox(6, text(Messages.get("pausemenu.achievements.next"), BLUE, 24, true));
    List<AchievementInfo> next = details.achievements.stream()
        .filter(a -> !a.unlocked && !a.legacy && "rom".equals(a.scope))
        .sorted(Comparator.comparingDouble(AchievementInfo::getProgressPct).reversed())
        .limit(MAX_LINES)
        .toList();
    if (next.isEmpty()) {
      nextBox.getChildren().add(text(Messages.get("pausemenu.achievements.all_unlocked"), GREEN, 22, false));
    }
    for (AchievementInfo a : next) {
      HBox line = new HBox(10, text(title(a), "white", 22, false), spacer());
      if (a.progressTarget > 0) {
        line.getChildren().add(text(number(Math.min(a.progressCurrent, a.progressTarget)) + " / " + number(a.progressTarget), MUTED, 22, false));
      }
      nextBox.getChildren().add(line);
    }
    HBox lower = new HBox(18, panel(unlockedBox), panel(nextBox));
    lower.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
    root.getChildren().add(lower);
  }

  private VBox highlightColumn(String category, List<SessionHighlight> list) {
    String color = CATEGORY_COLORS.getOrDefault(category, MUTED);
    VBox col = new VBox(4, text(translate("achievements.highlight.category." + category, category), color, 24, true));
    HBox.setHgrow(col, Priority.ALWAYS);
    col.setMaxWidth(Double.MAX_VALUE);
    col.setPrefWidth(360);
    if (list.isEmpty()) {
      col.getChildren().add(text("—", MUTED, 22, false));
    }
    for (SessionHighlight h : list) {
      HBox line = new HBox(8, text(translate("achievements.highlight." + h.key, h.label), "white", 22, false), spacer());
      if (!h.flag) {
        line.getChildren().add(text(number(h.value), "white", 22, true));
      }
      col.getChildren().add(line);
    }
    return col;
  }

  private static VBox panel(Node... children) {
    VBox box = new VBox(10, children);
    box.getStyleClass().add("subpanel");
    box.setPadding(new Insets(16, 20, 16, 20));
    box.setMaxWidth(Double.MAX_VALUE);
    return box;
  }

  private static Node progress(double pct) {
    Region track = new Region();
    track.setPrefSize(600, 12);
    track.setMaxWidth(600);
    track.setStyle("-fx-background-color: #161616; -fx-background-radius: 6;");
    Region bar = new Region();
    bar.setPrefSize(600 * Math.max(0, Math.min(100, pct)) / 100, 12);
    bar.setMaxWidth(Region.USE_PREF_SIZE);
    bar.setStyle("-fx-background-color: " + ORANGE + "; -fx-background-radius: 6;");
    StackPane p = new StackPane(track, bar);
    p.setAlignment(Pos.CENTER_LEFT);
    p.setMaxWidth(600);
    return p;
  }

  private static Label text(String value, String color, int size, boolean bold) {
    Label l = new Label(value);
    l.setStyle("-fx-font-size: " + size + "px; -fx-text-fill: " + color + ";" + (bold ? " -fx-font-weight: bold;" : ""));
    return l;
  }

  private static Region spacer() {
    Region r = new Region();
    HBox.setHgrow(r, Priority.ALWAYS);
    return r;
  }

  private static String title(AchievementInfo a) {
    return a.displayTitle != null ? a.displayTitle : a.title;
  }

  private static String translate(String key, String fallback) {
    try {
      return Messages.getBundle().getString(key);
    }
    catch (MissingResourceException e) {
      return fallback;
    }
  }

  private static String number(long value) {
    return String.format(Locale.getDefault(), "%,d", value);
  }

  private static String dateTime(@Nullable Date date) {
    return date == null ? "—" : new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date);
  }

  private static String duration(long sec) {
    long h = sec / 3600;
    long m = (sec % 3600) / 60;
    long s = sec % 60;
    return h > 0 ? String.format("%d:%02d:%02d", h, m, s) : String.format("%d:%02d", m, s);
  }
}
