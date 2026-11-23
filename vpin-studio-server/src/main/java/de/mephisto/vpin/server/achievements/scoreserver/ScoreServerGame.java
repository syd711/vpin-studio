package de.mephisto.vpin.server.achievements.scoreserver;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * One pinball game reported by the score-server plugin: players, scores, points per ball and the live NVRAM
 * snapshots (port of the Watcher's {@code _Game}).
 */
public class ScoreServerGame {
  /** more snapshots are thinned out, the first one (baseline) is kept */
  private static final int MAX_SNAPSHOTS = 400;

  public record Snapshot(@NonNull Instant ts, int player, int ball, int players, byte @NonNull [] data) {
  }

  public record BestBall(long points, int ball) {
  }

  private final String rom;
  private final Instant started;
  private final boolean fromRoundStart;
  private Instant ended;
  private String endReason = "";
  private int players = 1;
  private final Map<Integer, Long> scores = new TreeMap<>();
  /** player -> ball -> points scored on that ball */
  private final Map<Integer, Map<Integer, Long>> ballPoints = new TreeMap<>();
  private final List<Snapshot> snapshots = new ArrayList<>();
  private int curPlayer;
  private int curBall;
  private long curStartScore;

  public ScoreServerGame(@NonNull String rom, @NonNull Instant started, boolean fromRoundStart) {
    this.rom = rom;
    this.started = started;
    this.fromRoundStart = fromRoundStart;
  }

  public synchronized void update(int players, int currentPlayer, int currentBall, @NonNull Map<Integer, Long> scores) {
    if (players > this.players) {
      this.players = players;
    }
    this.scores.putAll(scores);
    boolean inPlay = currentPlayer > 0 && currentBall > 0;
    if (!inPlay || currentPlayer != curPlayer || currentBall != curBall) {
      closeBall();
      if (inPlay) {
        curPlayer = currentPlayer;
        curBall = currentBall;
        curStartScore = this.scores.getOrDefault(currentPlayer, 0L);
      }
    }
  }

  public synchronized void finish(@NonNull Instant ts, @Nullable Map<Integer, Long> finalScores, @Nullable String reason) {
    if (finalScores != null) {
      for (Map.Entry<Integer, Long> e : finalScores.entrySet()) {
        if (e.getValue() > 0 || !scores.containsKey(e.getKey())) {
          scores.put(e.getKey(), e.getValue());
        }
      }
    }
    closeBall();
    ended = ts;
    endReason = reason != null ? reason : "";
  }

  public synchronized void addSnapshot(@NonNull Snapshot snapshot) {
    snapshots.add(snapshot);
    if (snapshots.size() > MAX_SNAPSHOTS) {
      snapshots.subList(1, 101).clear();
    }
  }

  private void closeBall() {
    if (curPlayer <= 0) {
      return;
    }
    long pts = Math.max(0, scores.getOrDefault(curPlayer, 0L) - curStartScore);
    ballPoints.computeIfAbsent(curPlayer, p -> new TreeMap<>()).merge(curBall, pts, Long::sum);
    curPlayer = 0;
    curBall = 0;
  }

  @NonNull
  public String getRom() {
    return rom;
  }

  @NonNull
  public Instant getStarted() {
    return started;
  }

  public boolean isFromRoundStart() {
    return fromRoundStart;
  }

  @Nullable
  public synchronized Instant getEnded() {
    return ended;
  }

  public synchronized boolean isRunning() {
    return ended == null;
  }

  @NonNull
  public synchronized String getEndReason() {
    return endReason;
  }

  /**
   * @return the number of players, at least 1
   */
  public synchronized int getPlayers() {
    int maxScored = scores.keySet().stream().filter(p -> scores.get(p) > 0).max(Integer::compare).orElse(1);
    return Math.max(1, Math.max(players, maxScored));
  }

  /**
   * @return player (1 based) -> score
   */
  @NonNull
  public synchronized Map<Integer, Long> getScores() {
    Map<Integer, Long> result = new TreeMap<>();
    for (int p = 1; p <= getPlayers(); p++) {
      result.put(p, scores.getOrDefault(p, 0L));
    }
    return result;
  }

  /**
   * @return the ball with the most points of the player, null if no ball was finished
   */
  @Nullable
  public synchronized BestBall getBestBall(int player) {
    Map<Integer, Long> balls = new TreeMap<>(ballPoints.getOrDefault(player, Collections.emptyMap()));
    if (curPlayer == player) {
      // the running ball counts too
      balls.merge(curBall, Math.max(0, scores.getOrDefault(curPlayer, 0L) - curStartScore), Long::sum);
    }
    BestBall best = null;
    for (Map.Entry<Integer, Long> e : balls.entrySet()) {
      if (e.getValue() > 0 && (best == null || e.getValue() > best.points())) {
        best = new BestBall(e.getValue(), e.getKey());
      }
    }
    return best;
  }

  @NonNull
  public synchronized List<Snapshot> getSnapshots() {
    return new ArrayList<>(snapshots);
  }
}
