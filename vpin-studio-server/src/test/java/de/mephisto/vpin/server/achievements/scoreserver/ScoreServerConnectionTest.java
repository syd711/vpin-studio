package de.mephisto.vpin.server.achievements.scoreserver;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class ScoreServerConnectionTest {

  @Test
  public void receivesMessagesOverWebSocket() throws Exception {
    List<String> events = new CopyOnWriteArrayList<>();
    try (TestScoreServer server = new TestScoreServer()) {
      ScoreServerClient client = new ScoreServerClient(server.getUri(), new ScoreServerClient.Listener() {
        @Override
        public void roundStarted(@NonNull ScoreServerGame game) {
          events.add("start " + game.getRom());
        }

        @Override
        public void snapshot(@NonNull ScoreServerGame game, ScoreServerGame.@NonNull Snapshot snapshot) {
          events.add("snapshot " + snapshot.data().length);
        }

        @Override
        public void roundEnded(@NonNull ScoreServerGame game) {
          events.add("end");
        }
      });
      client.start();
      try {
        for (int i = 0; i < 100 && !client.isConnected(); i++) {
          Thread.sleep(100);
        }
        assertThat(client.isConnected()).isTrue();
        // a large message (> 64 KB) is delivered in parts
        String data = java.util.Base64.getEncoder().encodeToString(new byte[100_000]);
        server.send("{\"type\":\"round_start\",\"rom\":\"afm_113b\",\"current_ball\":1}");
        server.send("{\"type\":\"nvram_snapshot\",\"rom\":\"afm_113b\",\"players\":1,\"current_player\":1,\"current_ball\":1,\"data\":\"" + data + "\"}");
        server.send("{\"type\":\"game_end\",\"rom\":\"afm_113b\",\"reason\":\"game_over\"}");
        for (int i = 0; i < 100 && events.size() < 3; i++) {
          Thread.sleep(50);
        }
        assertThat(events).containsExactly("start afm_113b", "snapshot 100000", "end");
      }
      finally {
        client.stop();
      }
    }
  }
}
