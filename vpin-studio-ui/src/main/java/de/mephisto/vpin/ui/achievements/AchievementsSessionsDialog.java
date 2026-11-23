package de.mephisto.vpin.ui.achievements;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.AchievementInfo;
import de.mephisto.vpin.restclient.achievements.AchievementSessionDetails;
import de.mephisto.vpin.restclient.achievements.AchievementSessionInfo;
import de.mephisto.vpin.restclient.achievements.SessionHighlight;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * The tracked games of a player on a table: list of games, highlights, unlocked achievements and audit changes.
 */
public class AchievementsSessionsDialog implements Initializable, DialogController {

  private static final int MAX_SESSIONS = 200;
  /** highlight key of the best ball (score-server plugin) */
  private static final String BEST_BALL = "best_ball";
  private static final Map<String, String> CATEGORY_ICONS = Map.of(
      "Power", "mdi2f-fire", "Precision", "mdi2t-target", "Fun", "mdi2e-emoticon-happy-outline");
  private static final Map<String, String> CATEGORY_COLORS = Map.of(
      "Power", ORANGE, "Precision", BLUE, "Fun", GREEN);

  @FXML
  private BorderPane root;

  private final VBox sessionList = new VBox(0);
  private final VBox detailsBox = new VBox(10);

  private long playerId;
  private final List<HBox> rows = new ArrayList<>();
  private long selectedId = -1;

  public static void open(long playerId, String rom, String tableName) {
    Stage stage = Dialogs.createStudioDialogStage(AchievementsSessionsDialog.class, "dialog-achievements-sessions.fxml",
        Messages.get("achievements.sessions.title", tableName));
    AchievementsSessionsDialog controller = (AchievementsSessionsDialog) stage.getUserData();
    controller.load(playerId, rom);
    stage.showAndWait();
  }

  public AchievementsSessionsDialog() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    VBox left = new VBox(6, headline(Messages.get("achievements.played_games")), scroll(sessionList));
    VBox.setVgrow(left.getChildren().get(1), Priority.ALWAYS);
    left.setPrefWidth(330);
    left.setMinWidth(330);

    detailsBox.setPadding(new Insets(0, 4, 0, 0));
    VBox right = new VBox(scroll(detailsBox));
    VBox.setVgrow(right.getChildren().get(0), Priority.ALWAYS);
    HBox.setHgrow(right, Priority.ALWAYS);

    HBox center = new HBox(14, left, right);
    center.setPadding(new Insets(16));
    center.setPrefSize(1060, 640);
    center.setStyle("-fx-font-size: 14px;");

    Button close = new Button(Messages.get("achievements.sessions.close"));
    close.getStyleClass().add("default-button");
    close.setStyle("-fx-font-size: 14px;");
    close.setOnAction(e -> ((Stage) root.getScene().getWindow()).close());
    HBox buttons = new HBox(close);
    buttons.setAlignment(Pos.CENTER_RIGHT);
    buttons.setPadding(new Insets(0, 16, 16, 16));

    root.setCenter(center);
    root.setBottom(buttons);
    sessionList.getChildren().add(muted(Messages.get("achievements.loading")));
  }

  private void load(long playerId, String rom) {
    this.playerId = playerId;
    JFXFuture.supplyAsync(() -> client.getAchievementsService().getSessions(playerId, rom, MAX_SESSIONS))
        .thenAcceptLater(this::renderSessions)
        .onErrorLater(e -> sessionList.getChildren().setAll(muted(e.getMessage())));
  }

  private void renderSessions(List<AchievementSessionInfo> sessions) {
    sessionList.getChildren().clear();
    rows.clear();
    if (sessions.isEmpty()) {
      Label none = muted(Messages.get("achievements.sessions.none"));
      none.setWrapText(true);
      sessionList.getChildren().add(none);
      return;
    }
    for (int i = 0; i < sessions.size(); i++) {
      AchievementSessionInfo s = sessions.get(i);
      int index = i;
      HBox row = row(index, false);
      row.setUserData(s.id);
      VBox textBox = new VBox(2, bold(dateTime(s.startedAt)), muted(duration(s.durationSec)
          + (s.score != null ? " · " + number(s.score) : "")));
      row.getChildren().addAll(textBox, spacer());
      if (s.playerCount != null && s.playerCount > 1) {
        row.getChildren().add(icon("mdi2a-account-multiple", MUTED, 18));
      }
      if (s.unlocks > 0) {
        row.getChildren().add(chip("+" + s.unlocks, ORANGE));
      }
      row.setOnMouseClicked(e -> select(s));
      row.setStyle(rowStyle(index, false) + " -fx-cursor: hand;");
      rows.add(row);
      sessionList.getChildren().add(row);
    }
    select(sessions.get(0));
  }

  private void select(AchievementSessionInfo s) {
    selectedId = s.id;
    for (int i = 0; i < rows.size(); i++) {
      HBox row = rows.get(i);
      row.setStyle(rowStyle(i, Long.valueOf(s.id).equals(row.getUserData())) + " -fx-cursor: hand;");
    }
    detailsBox.getChildren().setAll(muted(Messages.get("achievements.loading")));
    JFXFuture.supplyAsync(() -> client.getAchievementsService().getSession(playerId, s.id))
        .thenAcceptLater(d -> {
          if (d != null && d.session != null && d.session.id == selectedId) {
            renderDetails(d);
          }
        })
        .onErrorLater(e -> detailsBox.getChildren().setAll(muted(e.getMessage())));
  }

  private void renderDetails(AchievementSessionDetails d) {
    detailsBox.getChildren().clear();
    AchievementSessionInfo s = d.session;

    // summary
    HBox stats = new HBox(30);
    stats.getChildren().add(stat(Messages.get("achievements.sessions.duration"), duration(s.durationSec)));
    stats.getChildren().add(stat(Messages.get("achievements.sessions.score"), s.score != null ? number(s.score) : "—"));
    stats.getChildren().add(stat(Messages.get("achievements.sessions.unlocked_count"), String.valueOf(s.unlocks)));
    if (s.playerCount != null) {
      stats.getChildren().add(stat(Messages.get("achievements.sessions.players"), String.valueOf(s.playerCount)));
    }
    VBox summary = card();
    summary.getChildren().addAll(headline(dateTime(s.startedAt) + (s.tableName != null ? " · " + s.tableName : "")), stats);
    if (s.playerCount != null && s.playerCount > 1) {
      Label mp = colored(Messages.get("achievements.sessions.multiplayer", s.playerCount), ORANGE, 14, false);
      mp.setWrapText(true);
      summary.getChildren().add(mp);
    }
    if (s.playerScores.size() > 1) {
      HBox scores = new HBox(30);
      for (Map.Entry<Integer, Long> e : s.playerScores.entrySet()) {
        scores.getChildren().add(stat(Messages.get("achievements.sessions.player_score", e.getKey()), number(e.getValue())));
      }
      summary.getChildren().add(scores);
    }
    if (s.liveTracked) {
      summary.getChildren().add(muted(Messages.get("achievements.sessions.live_tracked")));
    }
    detailsBox.getChildren().add(summary);

    // highlights
    VBox highlights = card();
    highlights.getChildren().add(headline(Messages.get("achievements.sessions.highlights")));
    boolean any = d.highlights.values().stream().anyMatch(l -> !l.isEmpty());
    if (any) {
      HBox columns = new HBox(10);
      for (Map.Entry<String, List<SessionHighlight>> e : d.highlights.entrySet()) {
        columns.getChildren().add(highlightColumn(e.getKey(), e.getValue(), s.bestBallNo));
      }
      highlights.getChildren().add(columns);
    }
    else {
      highlights.getChildren().add(muted(Messages.get("achievements.sessions.no_highlights")));
    }
    detailsBox.getChildren().add(highlights);

    // unlocked achievements
    VBox unlocked = card();
    unlocked.getChildren().add(headline(Messages.get("achievements.sessions.unlocked")));
    if (d.unlocks.isEmpty()) {
      unlocked.getChildren().add(muted(Messages.get("achievements.sessions.no_unlocks")));
    }
    for (int i = 0; i < d.unlocks.size(); i++) {
      AchievementInfo a = d.unlocks.get(i);
      HBox row = row(i, false);
      row.getChildren().addAll(icon("rom".equals(a.scope) ? "mdi2t-trophy-outline" : "mdi2e-earth", ORANGE, 18),
          text(a.displayTitle != null ? a.displayTitle : a.title), spacer());
      Node chip = rarityChip(a.rarity);
      if (chip != null) {
        row.getChildren().add(chip);
      }
      unlocked.getChildren().add(row);
    }
    detailsBox.getChildren().add(unlocked);

    // audit changes
    VBox audits = card();
    audits.getChildren().add(headline(Messages.get("achievements.sessions.audits")));
    if (d.deltas.isEmpty()) {
      audits.getChildren().add(muted(Messages.get("achievements.sessions.no_audits")));
    }
    int i = 0;
    for (Map.Entry<String, Long> e : d.deltas.entrySet()) {
      HBox row = row(i++, false);
      row.setPadding(new Insets(4, 10, 4, 10));
      row.getChildren().addAll(text(e.getKey()), spacer(), colored("+" + number(e.getValue()), GREEN, 14, true));
      audits.getChildren().add(row);
    }
    detailsBox.getChildren().add(audits);
  }

  private VBox highlightColumn(String category, List<SessionHighlight> list, Integer bestBallNo) {
    String color = CATEGORY_COLORS.getOrDefault(category, MUTED);
    HBox title = new HBox(6, icon(CATEGORY_ICONS.getOrDefault(category, "mdi2s-star"), color, 18),
        colored(translate("achievements.highlight.category." + category, category), color, 14, true));
    title.setAlignment(Pos.CENTER_LEFT);
    VBox col = new VBox(6, title);
    col.setPadding(new Insets(10));
    col.setStyle("-fx-background-color: " + TRACK + "; -fx-background-radius: 6;");
    HBox.setHgrow(col, Priority.ALWAYS);
    col.setMaxWidth(Double.MAX_VALUE);
    col.setPrefWidth(200);
    if (list.isEmpty()) {
      col.getChildren().add(muted("—"));
    }
    for (SessionHighlight h : list) {
      String label = translate("achievements.highlight." + h.key, h.label);
      if (BEST_BALL.equals(h.key) && bestBallNo != null && bestBallNo > 0 && !label.equals(h.label)) {
        label = label + " (B" + bestBallNo + ")";
      }
      HBox line = new HBox(6, text(label), spacer());
      if (!h.flag) {
        line.getChildren().add(bold(number(h.value)));
      }
      col.getChildren().add(line);
    }
    return col;
  }

  private static VBox stat(String label, String value) {
    return new VBox(2, muted(label), big(value));
  }

  private static String translate(String key, String fallback) {
    try {
      return Messages.getBundle().getString(key);
    }
    catch (MissingResourceException e) {
      return fallback;
    }
  }

  private static String duration(long sec) {
    long h = sec / 3600;
    long m = (sec % 3600) / 60;
    long s = sec % 60;
    return h > 0 ? String.format("%d:%02d:%02d", h, m, s) : String.format("%d:%02d", m, s);
  }

  @Override
  public void onDialogCancel() {
  }
}
