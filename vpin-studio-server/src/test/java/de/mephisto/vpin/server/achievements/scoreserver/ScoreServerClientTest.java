package de.mephisto.vpin.server.achievements.scoreserver;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ScoreServerClientTest {

  private final List<String> events = new ArrayList<>();
  private final List<ScoreServerGame> ended = new ArrayList<>();
  private final ScoreServerClient client = new ScoreServerClient(new ScoreServerClient.Listener() {
    @Override
    public void roundStarted(@NonNull ScoreServerGame game) {
      events.add("start");
    }

    @Override
    public void snapshot(@NonNull ScoreServerGame game, ScoreServerGame.@NonNull Snapshot snapshot) {
      events.add("snapshot " + snapshot.player() + "/" + snapshot.ball() + " " + snapshot.data().length);
    }

    @Override
    public void roundEnded(@NonNull ScoreServerGame game) {
      events.add("end");
      ended.add(game);
    }
  });

  private Instant t = Instant.parse("2026-11-23T20:00:00Z");

  private void send(String json) {
    t = t.plusSeconds(1);
    client.handle(JsonMapper.builder().build().readTree(json), t);
  }

  private void scores(int players, int player, int ball, long... scores) {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < scores.length; i++) {
      s.append(i > 0 ? "," : "").append("{\"player\":\"Player ").append(i + 1).append("\",\"score\":\"").append(String.format("%,d", scores[i])).append("\"}");
    }
    send("{\"type\":\"current_scores\",\"rom\":\"afm_113b\",\"players\":" + players + ",\"current_player\":" + player
        + ",\"current_ball\":" + ball + ",\"scores\":[" + s + "]}");
  }

  private void snapshot(int players, int player, int ball) {
    String data = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4});
    send("{\"type\":\"nvram_snapshot\",\"rom\":\"afm_113b\",\"players\":" + players + ",\"current_player\":" + player
        + ",\"current_ball\":" + ball + ",\"data\":\"" + data + "\"}");
  }

  @Test
  public void tracksRoundsWithBallPointsAndSnapshots() {
    send("{\"type\":\"game_start\",\"rom\":\"afm_113b\"}");
    scores(1, 0, 0, 0); // attract mode before the first round_start: no game yet
    send("{\"type\":\"round_start\",\"rom\":\"afm_113b\",\"players\":1,\"current_player\":1,\"current_ball\":1}");
    snapshot(1, 1, 1);
    scores(1, 1, 1, 1_000_000);
    scores(2, 1, 1, 1_500_000, 0);       // second player joins during ball 1
    scores(2, 2, 1, 1_500_000, 200_000);
    snapshot(2, 2, 1);
    scores(2, 1, 2, 1_500_000, 900_000);
    scores(2, 1, 2, 9_500_000, 900_000); // 8M on player 1 ball 2
    send("{\"type\":\"game_end\",\"rom\":\"afm_113b\",\"reason\":\"game_over\",\"scores\":[{\"player\":\"Player 1\",\"score\":\"9,600,000\"},{\"player\":\"Player 2\",\"score\":\"900,000\"}]}");
    snapshot(2, 0, 0);                   // late snapshot after the end still belongs to the game

    assertThat(events).containsExactly("start", "snapshot 1/1 4", "snapshot 2/1 4", "end");
    ScoreServerGame game = ended.get(0);
    assertThat(game.isFromRoundStart()).isTrue();
    assertThat(game.getEndReason()).isEqualTo("game_over");
    assertThat(game.getPlayers()).isEqualTo(2);
    assertThat(game.getScores()).isEqualTo(Map.of(1, 9_600_000L, 2, 900_000L));
    // a ball starts with the score of the message that reports it, the score of the message that reports the
    // next ball still counts for the old one (like the Watcher):
    // player 1 ball 1: 1.5M - 1M, ball 2: 9.6M (final score) - 1.5M; player 2 ball 1: 900k - 200k
    assertThat(game.getBestBall(1)).isEqualTo(new ScoreServerGame.BestBall(8_100_000L, 2));
    assertThat(game.getBestBall(2)).isEqualTo(new ScoreServerGame.BestBall(700_000L, 1));
    assertThat(game.getSnapshots()).hasSize(3);
    assertThat(game.getEnded()).isNotNull();

    // attract mode after the game: no new game until the next round_start
    scores(2, 0, 0, 0, 0);
    scores(1, 1, 1, 0);
    assertThat(client.getGamesSince(game.getStarted())).containsExactly(game);
  }

  @Test
  public void startsGameWithScoresForPluginsWithoutRoundStart() {
    scores(1, 1, 1, 100);
    scores(1, 1, 2, 5_000);
    send("{\"type\":\"game_end\",\"rom\":\"afm_113b\",\"reason\":\"plugin_unload\",\"scores\":[]}");

    assertThat(events).isEmpty(); // no round protocol: the session is evaluated at table exit
    List<ScoreServerGame> games = client.getGamesSince(Instant.EPOCH);
    assertThat(games).hasSize(1);
    assertThat(games.get(0).isFromRoundStart()).isFalse();
    assertThat(games.get(0).getPlayers()).isEqualTo(1);
    assertThat(games.get(0).getBestBall(1)).isEqualTo(new ScoreServerGame.BestBall(4_900L, 1));
    client.clearFinishedGames();
    assertThat(client.getGamesSince(Instant.EPOCH)).isEmpty();
  }

  @Test
  public void parsesNumbers() {
    assertThat(ScoreServerClient.toLong(JsonMapper.builder().build().readTree("\"1,234,567\""))).isEqualTo(1_234_567L);
    assertThat(ScoreServerClient.toLong(JsonMapper.builder().build().readTree("\"1.234.567\""))).isEqualTo(1_234_567L);
    assertThat(ScoreServerClient.toLong(JsonMapper.builder().build().readTree("42"))).isEqualTo(42L);
    assertThat(ScoreServerClient.toLong(JsonMapper.builder().build().readTree("\"n/a\""))).isZero();
    assertThat(ScoreServerClient.toLong(null)).isZero();
  }
}
